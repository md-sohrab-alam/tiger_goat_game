# Bagh Bakri — Google Play Store Release Guide

> Step-by-step checklist from development to production on Google Play.  
> Target: v1.0 release.  
> Last updated: September 2026

---

## Prerequisites

### Accounts & fees
- [ ] Google Play Developer account ($25 one-time) — [play.google.com/console](https://play.google.com/console)
- [ ] Google account with 2FA enabled
- [ ] AdMob account (linked to Play Console for ads)
- [ ] Firebase project (Analytics + Crashlytics)

### Legal & compliance
- [ ] Privacy policy hosted at a public URL (required even without login)
  - Must cover: analytics, ads, data collected, contact email
  - Free hosting: GitHub Pages, Firebase Hosting, or Notion public page
- [ ] App content rating questionnaire completed in Play Console
- [ ] Target audience: not designed for children under 13 (unless COPPA compliant)
- [ ] Export compliance: app uses standard encryption only (declare in Play Console)

---

## Pre-Release Technical Checklist

### Build quality
- [ ] `versionCode` incremented for every upload
- [ ] `versionName` follows semver (e.g. `1.0.0`)
- [ ] Release build signed with upload key (not debug key)
- [ ] ProGuard/R8 rules tested (if minification enabled)
- [ ] APK/AAB size under 30 MB
- [ ] Tested on Android 10 (API 29) through Android 14+ (API 36)

### Performance (Android Vitals targets)
- [ ] Crash rate < 1%
- [ ] ANR rate < 0.5%
- [ ] No memory leaks on repeated game sessions
- [ ] Cold start under 3 seconds on mid-range device

### Functionality smoke test
- [ ] New game starts correctly
- [ ] Full game playable (placement → movement → win)
- [ ] AI mode works on all difficulty levels
- [ ] Pass-and-play works
- [ ] Tutorial completable
- [ ] Settings persist after app restart
- [ ] Ads load (or fail gracefully offline)
- [ ] "Remove ads" purchase works (test with license testers)
- [ ] Back button / rotation handled correctly
- [ ] App works offline (core gameplay)

---

## Signing & Build

### 1. Create upload keystore (one time)

```bash
keytool -genkey -v -keystore baghbakri-upload.jks -keyalg RSA -keysize 2048 -validity 10000 -alias baghbakri
```

Store the keystore and passwords securely (password manager + backup). **If lost, you cannot update the app.**

### 2. Configure signing in `app/build.gradle.kts`

Use `keystore.properties` (gitignored) for credentials:

```properties
storeFile=../baghbakri-upload.jks
storePassword=***
keyAlias=baghbakri
keyPassword=***
```

### 3. Build release AAB

```bash
./gradlew bundleRelease
```

Output: `app/build/outputs/bundle/release/app-release.aab`

Upload AAB (not APK) to Play Console — Google requires AAB for new apps.

---

## Play Console Setup

### App identity
| Field            | Value                                      |
|------------------|--------------------------------------------|
| App name         | Bagh Bakri - Tiger & Goat                  |
| Package name     | `com.sohrab.baghbakri` (already set)       |
| Category         | Games → Board                              |
| Tags             | Board, Strategy, Offline, Single player    |

### Store listing — text

**Title** (30 chars max):
```
Bagh Bakri - Tiger & Goat
```

**Short description** (80 chars max):
```
Play Bagh Chal offline. Ancient tiger vs goats strategy board game from Nepal.
```

**Full description** (4000 chars max) — structure:

```
🐯 Bagh Bakri — The Ancient Game of Tigers and Goats

Play Bagh Chal (Bagh Bakri), the traditional strategy board game from Nepal and rural India. Four powerful tigers hunt twenty clever goats on a 5×5 board. Easy to learn in 5 minutes — hard to master for years!

🎮 GAME MODES
• Play vs Smart AI (Easy, Medium, Hard)
• Pass & Play with a friend on one device
• Interactive tutorial for beginners

🐅 HOW TO PLAY
• Goats place 20 pieces and trap the tigers
• Tigers capture goats by jumping over them
• Tigers win by capturing 5 goats
• Goats win by blocking all tiger moves

✨ FEATURES
• Beautiful traditional board design
• Smooth animations and sound effects
• Works fully offline — no internet needed
• Learn the rules with step-by-step tutorial
• Track your wins and achievements

📜 A GAME OF HERITAGE
Bagh Chal has been played in the Himalayas for over a thousand years. Also known as Wagh Bakri, Aadu Puli Aattam, and Puli-Meka across South Asia.

Download now and experience this unique asymmetric strategy game!

Keywords: bagh bakri, bagh chal, tiger goat game, nepali board game, strategy game offline, traditional indian game, puli meka, aadu puli attam
```

### Store listing — graphics

| Asset              | Spec                          | Status |
|--------------------|-------------------------------|--------|
| App icon           | 512×512 PNG                   | [ ]    |
| Feature graphic    | 1024×500 PNG/JPG              | [ ]    |
| Phone screenshots  | 2–8 images, min 320px short side | [ ] |
| 7-inch tablet      | Optional                      | [ ]    |
| 10-inch tablet     | Optional                      | [ ]    |

**Screenshot headlines (overlay text on each):**
1. "Ancient Nepali Strategy Game"
2. "4 Tigers vs 20 Goats"
3. "Play Offline — No Internet"
4. "Challenge Smart AI"
5. "Pass & Play with Friends"
6. "Learn in 5 Minutes"

### Contact details
- [ ] Developer email (public, monitored)
- [ ] Privacy policy URL
- [ ] Optional: website / social links

---

## Policy Forms (Play Console)

### Data safety
Declare accurately:

| Data type        | Collected? | Shared? | Purpose           |
|------------------|------------|---------|-------------------|
| Device ID        | Yes        | Yes     | Advertising       |
| App interactions | Yes        | No      | Analytics         |
| Crash logs       | Yes        | No      | App functionality |
| Purchase history | Yes        | No      | IAP verification  |

- [ ] Data encrypted in transit: Yes
- [ ] Users can request deletion: Yes (email contact)
- [ ] Data safety form published

### Ads declaration
- [ ] App contains ads: Yes
- [ ] Ads comply with Families Policy (if targeting kids: No)

### Content rating (IARC)
- [ ] Complete questionnaire
- [ ] Expected rating: Everyone / PEGI 3 / USK 0
- [ ] No violence beyond abstract board game captures

### Target audience
- [ ] Primary: Ages 13+ (recommended for ads + simplicity)
- [ ] Or Ages 6+ if no personalized ads and family-friendly

---

## Release Tracks

### Stage 1 — Internal testing (Day 1)
- [ ] Upload AAB to Internal testing track
- [ ] Add testers by email (up to 100)
- [ ] Fix critical bugs found
- [ ] Duration: 1–3 days

### Stage 2 — Closed testing (Day 3–7)
- [ ] Promote to Closed testing (alpha)
- [ ] Invite 20–50 testers (friends, Reddit, Facebook groups)
- [ ] Collect feedback on tutorial clarity, AI difficulty, ads frequency
- [ ] Duration: 1–2 weeks

### Stage 3 — Open testing (Optional, Week 3)
- [ ] Open testing track for broader feedback
- [ ] Monitor crash reports and reviews
- [ ] Duration: 1 week

### Stage 4 — Production (Launch)
- [ ] Promote release to Production
- [ ] Start with **staged rollout: 20%** → monitor 48h → 50% → 100%
- [ ] Monitor Android Vitals daily for first week

---

## Post-Launch Monitoring (First 30 Days)

### Daily checks
- [ ] Crashlytics — new crash clusters
- [ ] Play Console → Android Vitals
- [ ] User reviews (respond within 48h)
- [ ] AdMob revenue + fill rate

### Weekly checks
- [ ] Play Store Listing Experiments (A/B test icon, screenshot #1)
- [ ] Retention metrics (D1, D7) in Firebase
- [ ] Keyword ranking for "bagh chal", "bagh bakri", "tiger goat game"

### Update cadence
- Bug fix releases: within 1 week of critical issues
- Feature releases: monthly (v1.1 online, v1.2 localization)

---

## In-App Review Strategy

Prompt for rating when:
- User wins their **3rd game** (not first — they need to enjoy it first)
- User completes tutorial
- Never prompt after a loss or on first launch

Use Google Play In-App Review API (no custom dialog before the API).

---

## Monetization Setup

### AdMob
- [ ] Create ad units: `banner` (optional), `interstitial`, `rewarded`
- [ ] Interstitial: show **between games only**, max 1 per 2 games
- [ ] Rewarded: optional undo or hint (player choice)
- [ ] Test with AdMob test ad IDs during development

### In-App Purchase
- [ ] Product: `remove_ads` — one-time non-consumable
- [ ] Price: ₹99–199 (India) / $1.99 (US) — adjust by market
- [ ] License testers configured in Play Console

---

## Localization (Store Listing)

Priority languages for Play Store metadata:

| Priority | Language | Market                |
|----------|----------|-----------------------|
| 1        | English  | Global                |
| 2        | Nepali   | Nepal                 |
| 3        | Hindi    | India                 |
| 4        | Bengali  | Bangladesh / India    |
| 5        | Tamil    | South India (Aadu Puli)|

Localize: title, short description, full description, screenshots (optional).

---

## Launch Day Checklist

- [ ] Production release approved and rolling out
- [ ] Store listing live in all target countries
- [ ] Social posts scheduled (see `MARKETING.md`)
- [ ] Product Hunt post live
- [ ] Reddit posts in r/Nepal, r/india, r/boardgames
- [ ] Monitor Play Console for approval status
- [ ] Team/friends ready to leave honest reviews (after playing)

---

## Rollback Plan

If critical bug found after launch:
1. Halt staged rollout in Play Console
2. Fix bug, increment `versionCode`, upload hotfix AAB
3. Prioritize fix within 24 hours for crash rate > 2%
4. Post update note in release notes

---

## Release Notes Template

```
Version 1.0.0 — Initial Release
• Play Bagh Bakri offline against AI or with a friend
• Interactive tutorial for new players
• Three AI difficulty levels
• Traditional and modern board themes
• Achievements and game statistics
```

---

## Related Documents

- Development plan: `docs/ROADMAP.md`
- Marketing strategy: `docs/MARKETING.md`
