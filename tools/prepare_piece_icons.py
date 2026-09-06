"""Prepare Bagh/Bakri piece drawables from standalone token images."""
from __future__ import annotations

from pathlib import Path

import numpy as np
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
ASSETS = Path(
    r"C:\Users\zkasamani.WOQODDEV\.cursor\projects\d-Sohrab-Github-BaghBakri\assets"
)
TIGER_SRC = ASSETS / (
    "c__Users_zkasamani.WOQODDEV_AppData_Roaming_Cursor_User_workspaceStorage_"
    "empty-window_images_ChatGPT_Image_Sep_6__2026__03_48_53_PM-47bb59cb-c27d-4db0-9848-ede696f9966a.png"
)
GOAT_SRC = ASSETS / (
    "c__Users_zkasamani.WOQODDEV_AppData_Roaming_Cursor_User_workspaceStorage_"
    "empty-window_images_ChatGPT_Image_Sep_6__2026__03_49_01_PM-2c2aa99a-f60a-434c-bae6-693be3a7fccc.png"
)

OUT_DRAWABLE = ROOT / "app" / "src" / "main" / "res" / "drawable-xxxhdpi"
OUT_SOURCE = ROOT / "playstore-assets" / "pieces"


def remove_solid_bg(im: Image.Image, mode: str) -> Image.Image:
    """Knock out flat black (tiger) or white (goat) backdrop; keep soft shadow."""
    arr = np.array(im.convert("RGBA")).astype(np.float32)
    r, g, b, a = arr[:, :, 0], arr[:, :, 1], arr[:, :, 2], arr[:, :, 3]
    lum = 0.299 * r + 0.587 * g + 0.114 * b

    if mode == "black":
        # Near-black backdrop -> transparent; keep shadowed midtones near token
        bg = (r < 28) & (g < 28) & (b < 28)
        # Soften near-black fringe
        soft = np.clip((lum - 8.0) / 22.0, 0.0, 1.0)
        a = np.where(bg, 0.0, a * soft)
    else:
        # Near-white backdrop
        bg = (r > 245) & (g > 245) & (b > 245)
        soft = np.clip((250.0 - lum) / 30.0, 0.0, 1.0)
        a = np.where(bg, 0.0, a * soft)

    arr[:, :, 3] = a
    clear = a < 6
    arr[clear, 0:3] = 0
    return Image.fromarray(np.clip(arr, 0, 255).astype(np.uint8), "RGBA")


def tight_square(im: Image.Image, pad: int = 8) -> Image.Image:
    arr = np.array(im)
    alpha = arr[:, :, 3]
    ys, xs = np.where(alpha > 12)
    if len(xs) == 0:
        return im
    left, right = int(xs.min()), int(xs.max())
    top, bottom = int(ys.min()), int(ys.max())
    # Make square around content
    cx = (left + right) / 2.0
    cy = (top + bottom) / 2.0
    half = max(right - left, bottom - top) / 2.0 + pad
    l = int(round(cx - half))
    t = int(round(cy - half))
    size = int(round(half * 2))
    canvas = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    region = im.crop(
        (
            max(0, l),
            max(0, t),
            min(im.width, l + size),
            min(im.height, t + size),
        )
    )
    canvas.paste(region, (max(0, -l), max(0, -t)))
    return canvas


def export(src: Path, mode: str, out_name: str) -> None:
    im = remove_solid_bg(Image.open(src), mode)
    im = tight_square(im, pad=10)
    # Keep high-res source copy
    OUT_SOURCE.mkdir(parents=True, exist_ok=True)
    source_path = OUT_SOURCE / out_name.replace("piece_", "source_")
    im.resize((512, 512), Image.Resampling.LANCZOS).save(source_path)

    drawable = im.resize((256, 256), Image.Resampling.LANCZOS)
    OUT_DRAWABLE.mkdir(parents=True, exist_ok=True)
    out_path = OUT_DRAWABLE / out_name
    drawable.save(out_path)
    print("wrote", out_path, "and", source_path)


def main() -> None:
    if not TIGER_SRC.exists() or not GOAT_SRC.exists():
        raise SystemExit(f"missing sources\n{TIGER_SRC}\n{GOAT_SRC}")
    export(TIGER_SRC, "black", "piece_bagh.png")
    export(GOAT_SRC, "white", "piece_bakri.png")


if __name__ == "__main__":
    main()
