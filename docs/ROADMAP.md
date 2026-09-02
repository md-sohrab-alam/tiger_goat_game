# Bagh Bakri — Development Roadmap

> **App:** Bagh Bakri (Bagh Chal / Tiger & Goat)  
> **Platform:** Android (Kotlin + Jetpack Compose)  
> **Target release:** v1.0 on Google Play Store  
> **Last updated:** September 2026

---

## Vision

Build the best mobile experience for Bagh Bakri — an ancient asymmetric strategy game where 4 Tigers hunt 20 Goats on a 5×5 board. Offline-first, culturally authentic, monetized without pay-to-win.

---

## Version Overview

| Version | Codename   | Focus                          | Status      |
|---------|------------|--------------------------------|-------------|
| v0.1    | Foundation | Game engine & rules            | Done        |
| v0.2    | Board      | Interactive board UI           | Done        |
| v0.3    | Solo       | AI opponent                    | Done        |
| v0.4    | Together   | Pass-and-play + tutorial       | Planned     |
| v0.5    | Polish     | Animations, sounds, themes     | Planned     |
| v0.6    | Retention  | Stats, puzzles, achievements   | Planned     |
| v1.0    | Launch     | Ads, analytics, Play Store     | Planned     |
| v1.1    | Online     | Multiplayer & leaderboards     | Post-launch |
| v1.2    | Culture    | Localization & festival events | Post-launch |

---

## v0.1 — Foundation (Game Engine)

**Goal:** Pure Kotlin game logic with no UI dependency. Fully unit-tested.

### Deliverables
- [x] 5×5 board graph (25 intersections, valid adjacency)
- [x] Game state: tigers, goats, phase, turn, captured count
- [x] Phase 1: goat placement + tiger move/capture
- [x] Phase 2: both sides move
- [x] Capture logic (tiger jumps over adjacent goat)
- [x] Win detection (5 goats captured OR all tigers blocked)
- [x] Anti-stalemate: position history (no repeated board states in Phase 2)
- [x] Legal move generation for current player
- [x] Unit tests for rules, captures, wins, phase transitions

### Files
```
app/src/main/java/com/sohrab/baghbakri/game/
  Board.kt
  GamePhase.kt
  PlayerSide.kt
  Position.kt
  Move.kt
  GameState.kt
  BaghBakriGame.kt
app/src/test/java/com/sohrab/baghbakri/game/
  BaghBakriGameTest.kt
```

### Acceptance criteria
- All unit tests pass (`./gradlew test`)
- Can simulate a full game programmatically
- No Android/Compose imports in `game/` package

---

## v0.2 — Board (Interactive UI)

**Goal:** Playable board on screen with tap-to-move.

### Deliverables
- [ ] Board composable (5×5 grid with lines)
- [ ] Tiger and goat piece rendering
- [ ] Tap piece → highlight legal moves → tap destination to move
- [ ] Game status bar (phase, turn, goats captured X/5, goats remaining to place)
- [ ] New game / reset button
- [ ] ViewModel wiring game engine to UI
- [ ] Home screen with "Play" entry point

### Acceptance criteria
- Two humans can pass the device and play a full game
- Illegal moves are rejected with visual feedback
- Win dialog shows correct winner

---

## v0.3 — Solo (AI Opponent)

**Goal:** Single-player mode against computer.

### Deliverables
- [ ] Minimax AI with alpha-beta pruning (depth 4–6)
- [ ] Difficulty levels: Easy (depth 2), Medium (depth 4), Hard (depth 6)
- [ ] Side selection screen (play as Tiger or Goat)
- [ ] AI move delay + highlight last move
- [ ] Evaluation function (mobility, captures, traps for goats)

### Acceptance criteria
- AI completes moves within 2 seconds on mid-range device
- Hard AI wins against random player >90% of games
- Easy AI beatable by beginners

---

## v0.4 — Together (Pass-and-Play + Tutorial)

**Goal:** Onboard new players and support local multiplayer modes.

### Deliverables
- [ ] Mode selection: vs AI / Pass & Play
- [ ] Interactive tutorial (5 steps with animations)
  1. Board intro
  2. Phase 1 — placing goats
  3. Tiger capture
  4. Phase 2 — moving goats
  5. Win conditions
- [ ] Undo last move (single step, optional)
- [ ] Role swap prompt after game ends

### Acceptance criteria
- New user can complete tutorial in under 5 minutes
- Tutorial covers both tiger and goat roles

---

## v0.5 — Polish (Look & Feel)

**Goal:** Premium feel that differentiates from existing apps.

### Deliverables
- [ ] Capture animation (goat removed, tiger slides)
- [ ] Move sound effects + optional background music
- [ ] Traditional board theme (wood/temple art)
- [ ] Modern minimal theme (system default)
- [ ] Haptic feedback on capture and win
- [ ] App icon design (tiger + goat on board grid)
- [ ] Splash screen

### Acceptance criteria
- 60fps board interactions on target devices (minSdk 29)
- APK size under 30 MB

---

## v0.6 — Retention (Engagement)

**Goal:** Give players reasons to return daily.

### Deliverables
- [ ] Game statistics (wins/losses as tiger vs goat, total games)
- [ ] Achievements (first win, win as goats, trap in Phase 1, etc.)
- [ ] Daily puzzle mode ("Goats win in 3 moves")
- [ ] Share win card (text/image for WhatsApp)
- [ ] Settings screen (sound, theme, language placeholder)

### Acceptance criteria
- Stats persist across app restarts (DataStore)
- At least 10 achievements defined

---

## v1.0 — Launch (Play Store Release)

**Goal:** Production-ready app on Google Play.

### Deliverables
- [ ] AdMob integration (rewarded + interstitial between games)
- [ ] "Remove ads" one-time IAP
- [ ] Firebase Analytics (sessions, game completion, mode usage)
- [ ] Crash reporting (Firebase Crashlytics)
- [ ] In-App Review prompt (after 3rd win, not first launch)
- [ ] Privacy policy URL
- [ ] Play Store listing assets (see `PLAYSTORE_RELEASE.md`)
- [ ] Closed testing → Open testing → Production rollout

### Acceptance criteria
- Crash rate < 1%
- ANR rate < 0.5%
- All Play Console policy forms completed
- See `PLAYSTORE_RELEASE.md` for full checklist

---

## v1.1 — Online (Post-Launch)

**Goal:** Multiplayer to drive growth and retention.

### Deliverables
- [ ] Firebase Auth (anonymous or Google sign-in)
- [ ] Real-time multiplayer (Firebase Realtime DB or Firestore)
- [ ] Room code / friend invite
- [ ] Quick match (automatch)
- [ ] ELO rating + global leaderboard
- [ ] Play Games Services achievements sync

---

## v1.2 — Culture (Post-Launch)

**Goal:** Expand audience via localization and events.

### Deliverables
- [ ] Nepali, Hindi, Bengali, Tamil string resources
- [ ] Festival themes (Dashain, Tihar board skins)
- [ ] Aadu Puli Aattam board variant (optional toggle)
- [ ] "About the game" cultural story screen

---

## Technical Stack

| Layer        | Technology                          |
|--------------|-------------------------------------|
| Language     | Kotlin 2.2                          |
| UI           | Jetpack Compose + Material 3        |
| Architecture | MVVM (ViewModel + StateFlow)        |
| Persistence  | DataStore Preferences               |
| Ads          | Google AdMob                        |
| Analytics    | Firebase Analytics + Crashlytics    |
| Multiplayer  | Firebase (v1.1)                     |
| Testing      | JUnit 4 (unit), Compose UI tests    |
| Min SDK      | 29 (Android 10)                     |
| Target SDK   | 36                                  |

---

## Milestones & Timeline (Estimate)

| Milestone        | Target     | Version |
|------------------|------------|---------|
| Engine complete  | Week 1     | v0.1    |
| Playable UI      | Week 2     | v0.2    |
| AI working       | Week 3     | v0.3    |
| Tutorial + modes | Week 4     | v0.4    |
| Polish pass      | Week 5     | v0.5    |
| Retention features | Week 6   | v0.6    |
| Play Store live  | Week 7–8   | v1.0    |
| Online multiplayer | Month 3  | v1.1    |

*Timeline assumes part-time development. Adjust as needed.*

---

## Definition of Done (per version)

1. Code compiles without errors
2. Unit tests pass (where applicable)
3. Manual smoke test on emulator + one physical device
4. Version noted in this doc (Status column updated)
5. User notified to review before next version starts

---

## References

- Game rules: [Wikipedia — Bagh-chal](https://en.wikipedia.org/wiki/Bagh-chal)
- Video rules: [YouTube tutorial](https://www.youtube.com/watch?v=EIIjpB76mg4)
- Play Store guide: `docs/PLAYSTORE_RELEASE.md`
- Marketing plan: `docs/MARKETING.md`
