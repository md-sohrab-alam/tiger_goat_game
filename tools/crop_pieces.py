"""Crop clean circular Bagh/Bakri tokens from the Play Store logo."""
from __future__ import annotations

from pathlib import Path

import numpy as np
from PIL import Image, ImageFilter

ROOT = Path(__file__).resolve().parents[1]
SRC = ROOT / "playstore-assets" / "icon-512.png"
OUT_DIR = ROOT / "app" / "src" / "main" / "res" / "drawable-xxxhdpi"

# Tuned against playstore-assets/icon-512.png
TIGER = (103.0, 103.0, 49.0)  # cx, cy, radius of disc face
GOAT = (402.0, 395.0, 49.0)


def is_board_pixel(r: float, g: float, b: float) -> bool:
    """True for dark wood / grid (should not remain in token PNG)."""
    # Dark wood
    if r < 155 and g < 115 and b < 90 and r > g * 0.85:
        return True
    # Light wood grid inlay (beige but duller than cream token)
    if 140 < r < 210 and 110 < g < 175 and 70 < b < 130 and (r - b) > 35 and g < r - 5:
        # Exclude tiger orange (higher saturation) and bright cream token
        sat = (r - min(g, b))
        if sat < 95 and r < 200:
            return True
    return False


def crop_token(
    im: Image.Image,
    cx: float,
    cy: float,
    radius: float,
    kind: str,
) -> Image.Image:
    pad = 8
    r = int(np.ceil(radius + pad))
    size = 2 * r
    left = int(round(cx - r))
    top = int(round(cy - r))

    canvas = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    src = im.crop(
        (
            max(0, left),
            max(0, top),
            min(im.width, left + size),
            min(im.height, top + size),
        )
    )
    canvas.paste(src, (max(0, -left), max(0, -top)))
    arr = np.array(canvas).astype(np.float32)

    yy, xx = np.ogrid[:size, :size]
    dist = np.sqrt((xx - r) ** 2 + (yy - r) ** 2)

    rgb = arr[:, :, :3]
    # Vectorized board detection
    rr, gg, bb = rgb[:, :, 0], rgb[:, :, 1], rgb[:, :, 2]
    dark_wood = (rr < 155) & (gg < 115) & (bb < 90) & (rr > gg * 0.85)
    dull_inlay = (
        (rr > 140)
        & (rr < 205)
        & (gg > 110)
        & (gg < 175)
        & (bb > 70)
        & (bb < 130)
        & ((rr - bb) > 35)
        & (gg < rr - 5)
        & ((rr - np.minimum(gg, bb)) < 95)
        & (rr < 200)
    )
    board = dark_wood | dull_inlay

    # Keep token face: punch board pixels inside circle to transparent
    keep = dist <= (radius + 0.8)
    if kind == "tiger":
        # Tiger is mostly vivid orange / white / black — never dull brown wood
        tokenish = (
            ((rr > 160) & (gg > 50) & (bb < 130) & (rr > gg + 25))  # orange
            | ((rr > 200) & (gg > 190) & (bb > 170))  # white muzzle
            | ((rr < 70) & (gg < 60) & (bb < 55))  # black stripes
            | ((rr > 180) & (gg > 140) & (bb < 80))  # amber eyes
        )
        keep = keep & (tokenish | ~board)
    else:
        cream = (rr > 185) & (gg > 165) & (bb > 125) & (np.abs(rr - gg) < 50)
        ink = (rr < 120) & (gg < 100) & (bb < 90)
        horn = (rr > 120) & (rr < 190) & (gg > 90) & (gg < 160) & (bb < 120) & (rr >= gg)
        keep = keep & ((cream | ink | horn) | ~board)

    # Soft drop-shadow halo: only darker pixels just outside disc
    halo = (dist > radius + 0.8) & (dist <= radius + pad)
    lum = 0.299 * rr + 0.587 * gg + 0.114 * bb
    shadow = np.clip((88 - lum) / 50.0, 0.0, 1.0) * np.clip(
        (radius + pad - dist) / pad, 0, 1
    )

    alpha = np.zeros((size, size), dtype=np.float32)
    alpha[keep] = 1.0
    alpha[halo] = np.maximum(alpha[halo], shadow[halo] * 0.55)
    # Feather disc rim
    rim = np.abs(dist - radius) <= 1.5
    alpha[rim & keep] = 1.0

    arr[:, :, 3] = arr[:, :, 3] * alpha
    # Clear RGB where fully transparent to avoid fringing
    clear = arr[:, :, 3] < 8
    arr[clear, 0:3] = 0

    out = Image.fromarray(np.clip(arr, 0, 255).astype(np.uint8), "RGBA")
    out = out.filter(ImageFilter.UnsharpMask(radius=1.0, percent=110, threshold=2))
    return out.resize((256, 256), Image.Resampling.LANCZOS)


def main() -> None:
    im = Image.open(SRC).convert("RGBA")
    OUT_DIR.mkdir(parents=True, exist_ok=True)

    tiger = crop_token(im, *TIGER, kind="tiger")
    goat = crop_token(im, *GOAT, kind="goat")
    tiger_path = OUT_DIR / "piece_bagh.png"
    goat_path = OUT_DIR / "piece_bakri.png"
    tiger.save(tiger_path)
    goat.save(goat_path)
    print("wrote", tiger_path)
    print("wrote", goat_path)


if __name__ == "__main__":
    main()
