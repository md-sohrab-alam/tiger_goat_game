# Play Store Screenshots Guide

Capture these from a real device or emulator before public launch.  
**Do not** use default Android Studio icon screenshots — use the new Bagh Bakri icon.

## Required

| # | Scene | Overlay text (optional) |
|---|--------|-------------------------|
| 1 | Home screen | Ancient tiger vs goats game |
| 2 | Mid-game board (placement) | 4 Tigers vs 20 Goats |
| 3 | Goat tray + tiger catch slots | Clear piece trays |
| 4 | AI thinking / move animation | Play vs AI offline |
| 5 | 2 Players Offline mid-game | Pass & play on one phone |
| 6 | Tutorial step | Learn in 5 steps |
| 7 | Landscape board | Works in landscape |

## Specs (phone)

- Portrait: at least **1080 × 1920** (or device native)
- JPEG or 24-bit PNG
- No device frames required (nice to have)
- Feature graphic separately: **1024 × 500**

## How to capture (quick)

1. Install debug/release build on phone  
2. `adb shell screencap -p /sdcard/screen.png` then `adb pull /sdcard/screen.png`  
   Or use Android Studio **Running Devices → Screenshot**  
3. Crop status bar if needed; add short headline in Canva/Figma  

## Listing reminder

- Title: `Bagh Bakri - Tiger & Goat`  
- Short description: offline Bagh Chal / tiger goat board game  
- Privacy policy URL: publish `docs/PRIVACY_POLICY.md` publicly and paste the link in Play Console  
