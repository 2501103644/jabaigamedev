PICKLE SMASH - AVATAR / SETTINGS / CREDITS, BUILT ONLY FROM YOUR UPLOADED ZIP
(2026-11-07, PickleSmash_HomeScreen__1.zip)

YOU ASKED: use only the zip I uploaded.
DONE: this build starts from exactly that zip — PickleSmash_HomeScreen__1.zip,
md5 e0a55b2fdbd18ee947cf215cfab9c9e7. Nothing was taken from the earlier
workspace build (no straight-rectangle PLAY, no larger PLAY, no animated
home character, no assets/avatars/ folder, no avatar sheet).

WHAT WAS IN YOUR UPLOADED ZIP (checked before building)
- 10 Java files: Main, HomeMenuPanel, GamePanel, GameState, Hud, Player,
  Car, CityBackground, Location, Assets.
- HomeMenuPanel from 21:31: hover straightens by ROTATING 1.9 degrees
  (TILT_RADIANS), plank-shaped glow/border. That code is UNTOUCHED in
  this build, except for the navigation wiring below.
- Assets: home_menu_bg.png (with the painted player still in it) and
  ONE character sprite, player_classic.png (265 x 372).
- NO avatar sheet, NO assets/avatars/ folder, NO player_male_classic.png,
  NO paddle or ball pictures. Player name in GameState is "Player 1".

WHAT THAT MEANS FOR THE AVATAR SCREEN
- Characters: 4 variants of player_classic.png — Classic (free), Elite
  (400 coins), Special (700), Epic (1000). AvatarPanel tints the one
  sprite a different colour per rarity (gold / purple / dark blue), so
  they still look different. The screen itself says this on the grid.
- Paddles and balls: 4 of each, drawn in code (coloured paddle and
  pickleball shapes) because the zip has no pictures for them.
- If you want the 10 characters from your avatar sheet instead, send
  that sheet / zip again and I will swap them in: add the PNGs to
  assets/avatars/ and add one line per character in AvatarCatalog.java.
  Nothing else needs to change — the screen, the shop logic and the
  save file already handle any number of characters.

WHAT WAS BUILT (same layouts as before, on this base only)
- AVATAR (AvatarPanel.java): animated preview left, character row right,
  4 paddles, 4 balls, name + coins on top, BACK + SELECT at the bottom.
  Click a locked item to buy it, click an owned one to select it.
  SORT cycles Name / Cost / Rarity. Type a name + Enter to search.
- SETTINGS (SettingsPanel.java): drag Music / Sound Effects sliders,
  Vibration ON/OFF, Language English/Filipino, Graphics Low/Medium/High,
  Controls Touch/Buttons/Keyboard/Gamepad, double-click RESET to reset
  progress, SAVE writes the save file and goes back.
- CREDITS (CreditsPanel.java): 8 entries in a List<CreditEntry>, scroll
  with the mouse wheel. Jeruld credited for design, programming and art.
- Home screen buttons AVATAR / SETTINGS / CREDITS now open these real
  screens instead of popups. PLAY still opens the city map, EXIT quits.
- Everything saves to picklesmash-save.properties next to the game.

OOP + DSA — same lessons as the earlier build, in the code comments
- Encapsulation: GameState / Settings fields are private; spendCoins()
  refuses when you cannot afford it, volumes clamp to 0..100.
  Hud.java and GamePanel.java now use the getters.
- Inheritance: Avatar / Paddle / Ball extend abstract CosmeticItem;
  AvatarPanel / SettingsPanel / CreditsPanel extend BaseMenuPanel.
- Polymorphism: one pickItem(CosmeticItem) works for all three kinds.
- Abstraction: ScreenNavigator interface; panels know nothing about JFrame.
- Composition: Main creates ONE shared GameState for every screen.
- Generics + ArrayList + HashMap: Catalog<T> keeps a List (for the grid)
  and a Map by id (O(1) getById).
- Stack: ScreenManager keeps history in an ArrayDeque; BACK is a pop.
- Sorting: sortedByCost / sortedByName with a Comparator, on a copy.
- Binary search: findByName() on the name-sorted list, O(log n).

NEW / CHANGED FILES (in PickleSmashMap/src/)
- New: Rarity, CosmeticItem, Avatar, Paddle, Ball, Catalog,
  AvatarCatalog, PaddleCatalog, BallCatalog, Settings, ScreenNavigator,
  ScreenManager, CreditEntry, BaseMenuPanel, AvatarPanel, SettingsPanel,
  CreditsPanel
- Changed: GameState (rewritten, same starting values as your zip:
  "Player 1", 1200 coins), Main (one shared GameState + ScreenManager),
  HomeMenuPanel (only the constructor + performAction/openMap wiring;
  hover code untouched), GamePanel (new GamePanel(GameState) constructor;
  restoreHp()), Hud (getters, not public fields)
- Unchanged: everything else, and every asset file, exactly as uploaded.

HOW TO RUN
1. Extract this ZIP.
2. In Eclipse: File > Import > Existing Projects into Workspace, select
   the extracted GameNew folder, Finish.
3. Open Main.java > Run As > Java Application.
   Or from a terminal, inside PickleSmashMap: javac -d out src/*.java
   then java -cp out Main

VERIFIED (javac 21, headless test, 2026-11-07, from this zip only)
- javac -d out src/*.java — OK.
- Headless test rendered Home, Avatar, Settings and Credits at 1280x720
  with no "Missing asset" warnings — every picture used comes from
  the uploaded zip.
- catalogs: 4 avatars, 4 paddles, 4 balls; getById("epic") = Epic,
  1000 coins, image player_classic.png; findByName("Epic") = epic;
  sortedByCost first Classic, last Epic; spendCoins(9999) refused;
  setMusicVolume(150) clamped to 100; clicked SORT then the Epic card:
  unlocked for 1000 coins, 1200 -> 200, selected = epic; typed
  "Elite" + Enter with only 200 coins: correctly refused; Music
  slider click at x=700 gave 22%; Credits: 8 entries, wheel scroll works.
  ALL CHECKS PASSED. The finished zip was re-extracted and recompiled
  clean as well.

ONE THING TO KNOW
- The Avatar nameplate says "Player 1" because that is the name in
  your uploaded zip's GameState. The home background still says "Jxui".
  If you want the nameplate to say Jxui too, change one line in
  GameState.java: private String playerName = "Jxui";

---
UPDATE — LAYOUT 1 SETTINGS + MUTE ALL + FULLSCREEN (2026-11-07)
Jeruld picked Layout 1 and chose two options to add: Mute All and
Fullscreen — ON/OFF.

- SettingsPanel.java is now Layout 1 only: tall centred panel, Music
  Volume on top, Sound Effects Volume below it, both with big
  percentages (75% / 60%), then Mute All and Fullscreen toggles,
  then BACK and SAVE. No Vibration, Language, Graphics Quality,
  Controls or Reset Progress anywhere.
- Settings.java now has only four settings: musicVolume, sfxVolume,
  muteAll, fullscreen. The old vibration / language / graphics /
  controls fields and enums were removed, because those options
  were removed from the screen.
- Mute All: one switch silences everything. The two volumes are NOT
  set to 0 — they are kept, so un-muting brings the sound back
  exactly as it was. Settings.effectiveMusicVolume() /
  effectiveSfxVolume() return 0 when muted, the slider value
  otherwise — that is the value future audio code should use.
  While muted the sliders are dimmed and show MUTED, with a note
  "Volume kept: 50% — un-mute to hear it again". Clicking a slider
  while muted un-mutes first.
- Fullscreen: ON maximises the real JFrame (MAXIMIZED_BOTH), OFF
  goes back to a 1280 x 720 window, centred. See
  SettingsPanel.applyFullscreen(). It is also applied when the
  Settings screen opens, so a saved Fullscreen = true takes effect
  on the next run. In a headless test there is no frame, so it
  just saves the setting and does not crash.
- Save file now stores: music, sfx, muteAll, fullscreen.
  Old save files that still have vibration / language / graphics /
  controls lines are ignored — they are simply not read any more.

VERIFIED (javac 21, headless test, from the uploaded zip only)
- javac -d out src/*.java — OK.
- Default: music 75, sfx 60, mute OFF, fullscreen OFF.
- Music slider click at x=560 gave 50%.
- Mute All click: mute ON, music kept at 50 (not zeroed),
  effective music/sfx = 0, screen shows MUTED.
- Slider click while muted: un-muted first, music became 89%.
- Fullscreen click: fullscreen ON, no crash headless.
- SAVE then reload in a fresh GameState: music 89, sfx 33,
  mute OFF, fullscreen ON — all saved and loaded correctly.
- Home and Avatar screens still render — no regression.
- ALL CHECKS PASSED.

---
UPDATE — UI IMPROVEMENT (2026-11-07)
Jeruld asked to improve the UI. Same settings, same Layout 1,
better looking and easier to use.

SettingsPanel.java:
- "GAME OPTIONS" header with a gold divider at the top of the panel,
  thin divider lines between rows, and a small description under
  Mute All ("Silence music and sound effects") and Fullscreen
  ("Fill the whole screen").
- Gold icon badges: every row starts with a gold circle holding its
  icon (music note, speaker, mute, fullscreen).
- Better sliders: thicker track, gold gradient fill, bigger knob
  with a highlight and soft shadow. The knob grows a little on
  hover, so it is obvious it can be dragged. Bigger percentage
  with a soft shadow.
- Better toggles: wider pill, knob with a shadow, ON in gold,
  OFF in grey. Hover gives the toggle a gold glow.
- Muted state is unmistakable: dimmed sliders, a MUTED pill
  instead of a percentage, and "Volume kept: 75% — un-mute to
  hear it again".
- Keyboard shortcuts, printed at the bottom of the panel:
  M = Mute All, F = Fullscreen, Esc = Back. ScreenManager
  already gives the panel focus when it opens, so these work
  in the real game (same KeyListener pattern as AvatarPanel).

BaseMenuPanel.java (shared, so Avatar and Credits got it too):
- Panels now have a drop shadow, a vertical navy gradient and a
  gold border. Title banner has a drop shadow, a text shadow and
  a small gold pickleball dot on each side. Wooden BACK / SAVE /
  SELECT buttons have a drop shadow, lift 2px and glow brighter
  on hover. Toasts have a gold border.
- No layout, click area or save format changed.

VERIFIED (javac 21, headless test with -ea, from the uploaded zip only)
- javac -d out src/*.java — OK.
- M key: mute ON, effective music 0. M again: mute OFF.
  F key: fullscreen ON.
- Mouse still works: music slider click gave 50%, mute toggle
  and fullscreen toggle clicked, save/reload gave music 50,
  sfx 33, mute ON, fullscreen OFF.
- Avatar, Credits and Home still render with the new shared UI.
- ALL CHECKS PASSED.

---
UPDATE — IMPROVED AVATAR + EQUAL CREDITS, SETTINGS KEPT (2026-11-07)
Jeruld liked the equal 3-card Credits mockup and asked to update
the code with it, plus the Avatar and Settings sections.

CREDITS — rebuilt as 3 EQUAL cards (CreditsPanel.java + TeamMember.java):
- Three identical gold-framed cards, same size, same layout: avatar
  on top, big gold name, gold ★ role badge, divider, two detail
  rows with gold icon badges. Nobody is featured bigger.
- Team is a List<TeamMember>: Jeruld — Game Developer (Game
  Design, Programming); Member 2 — Game Artist (Characters, UI
  Design); Member 3 — Sound Designer (Music, Sound Effects).
  Adding a 4th member is one line in the constructor; changing a
  name or role is one string, the drawing code never changes.
- Footer pill under the cards: Special Thanks to the Pickleball
  Community • Powered by Java • Pickle Smash v1.0.
- No scroll any more — the 3 cards fit. The old 8-entry scroll
  list and CreditEntry are no longer used by this screen.
- Avatars are the one player_classic.png sprite, tinted per
  member (Jeruld plain, Member 2 purple, Member 3 gold) — the
  same trick AvatarPanel uses. Member 2 / Member 3 are
  placeholders: Jeruld has not given their real names, roles
  or photos yet. Send them and change 3 strings.

AVATAR — improved, same layout and clicks (AvatarPanel.java):
- Gold ★ SELECTED tag on the selected character card, and a gold
  ★ YOUR PICK pill on the preview card.
- Character names are now in their rarity colour (Elite gold,
  Special purple, Epic blue) — dark brown on the gold selected
  card, because gold-on-gold was unreadable.
- Locked cards now have a gold coin price pill (🪙 1000 / 🪙 700)
  at the top-right, plus the dim overlay and 🔒 Locked.
- PADDLES and BALLS headers now have a gold icon badge with a
  real mini paddle / ball drawn in code (the emoji rendered blank).
- Player pill now reads ★ Player 1.
- Search hint moved from under the title — where it overlapped
  the AVATAR banner — to the free strip at the bottom centre.

SETTINGS — unchanged in this update. Layout 1 with Mute All and
Fullscreen, plus the earlier UI polish, is already in this zip.

VERIFIED (javac 21, headless test with -ea, from the uploaded zip only)
- javac -d out src/*.java — OK.
- Credits team: 3 members, Jeruld / Member 2 / Member 3,
  roles Game Developer / Game Artist / Sound Designer.
- Avatar: clicked Elite, coins 1200 -> 800, selected = elite;
  SORT button clicked; preview, grid, paddles and balls render.
- Settings: music slider click gave 50%, Mute All ON, effective
  music 0. Home renders.
- ALL CHECKS PASSED. Zip re-extracted and recompiled clean below.

---
FINAL LAYOUTS CHOSEN (2026-11-07)
Jeruld confirmed these three as the FINAL layouts:
  1. SETTINGS — Layout 1, improved UI, Mute All + Fullscreen.
  2. AVATAR — improved UI mockup (preview + grid + paddles + balls).
  3. CREDITS — 3 equal team cards, no lead.
This zip is saved as PickleSmash_Final.zip (identical copy).
Final verification: fresh extract, javac 21 clean, headless test
with -ea ALL CHECKS PASSED (Credits team of 3, Elite click
1200 -> 800 coins, SORT, Settings slider 50% + mute effective 0).

---
UPDATE — MOCKUP PORTRAITS CUT FROM THE IMAGES, IN CODE (2026-11-07)
Jeruld asked: can this be made in code, using what's from the
image for now, final assets to be uploaded later. Yes — done.

What was cut from the mockup images (assets/mockup_*.png):
- mockup_classic / elite / epic / special — the 4 character
  portraits from the Avatar mockup grid, head-and-shoulders,
  trimmed so no name text bleeds in.
- mockup_jeruld / member2 / member3 — the 3 faces from the
  Credits mockup cards.
These are STAND-INS. When Jeruld uploads the final assets, put
their PNGs in assets/ and change the file name in AvatarCatalog
(for characters) or in CreditsPanel's team list (for members) —
one line each, nothing else needs to change.

Code changes:
- Avatar.java: the image file is now a constructor argument.
- AvatarCatalog.java: the 4 avatars now use mockup_classic /
  elite / special / epic. Elite is FEMALE in the mockup.
- AvatarPanel.java: mockup portraits are used as-is (NO tint —
  they are already 4 different people). The preview now fits
  the portrait keeping its aspect ratio, in a big box, so it
  fills the preview panel instead of floating in it.
- PaddleCatalog / BallCatalog: names now match the mockup —
  paddles Classic, Pro Strike, Power+, Control; balls Classic,
  Speed, Bounce+, Training. Icons are still drawn in code.
- TeamMember.java: now carries an imageFile; the tint is gone.
- CreditsPanel.java: each card loads its member's mockup
  portrait as-is, in a bigger box so the faces fill the cards.

VERIFIED (javac 21, headless test with -ea, from the uploaded zip only)
- javac -d out src/*.java — OK, no "Missing asset" warnings.
- Credits team of 3 with the mockup faces; Avatar clicked
  Elite, coins 1200 -> 800, selected = elite; SORT works;
  Settings slider 50% + Mute All effective 0; Home renders.
- ALL CHECKS PASSED.

---
UPDATE — MAP USES YOUR AVATAR PICK (2026-11-07)
Jeruld asked: implement it in code — specially in the map, replace
the little blue chibi character (the one in his screenshot) with
whoever he picks in the Avatar section.

DONE:
- Player.java now draws the SELECTED avatar's full-body sprite on
  the map instead of always drawing the code-drawn chibi. The chibi
  is kept only as a fallback if a sprite file is missing.
- Avatar.java has a new field mapSpriteFile (full-body sprite), in
  addition to imageFile (the head-and-shoulders portrait used in the
  Avatar screen). The portrait alone could never walk on the map.
- AvatarCatalog.java wires each character to its full-body sprite,
  cut from the avatar sheet Jeruld sent (and re-sent in this chat):
  Classic = avatars/male_classic, Elite = avatars/female_elite
  (Elite is female in the mockup), Special = avatars/male_special,
  Epic = avatars/male_epic. New files in assets/avatars/.
- GamePanel.java: when the map opens (PLAY), the player is set to
  state.getSelectedAvatar(). Every game tick it also calls
  player.refreshAvatar(state), so if the pick changes while the map
  is open, the walker switches on the next tick — no restart needed.
- Hud.java: the player card at the bottom-left of the map now shows
  the selected avatar's portrait instead of the generic portrait.png.
- Sprite behaviour on the map: ~72 px tall (same size as the old
  chibi, so speed, collision and entrances are unchanged), flips
  horizontally when walking LEFT, bobs while walking. The sheet
  only has a FRONT view, so walking UP still shows the front —
  there is no back view to use yet.

HOW TO USE IT IN THE GAME:
  Home > AVATAR > click a character (buy it first if it is locked)
  > SELECT > BACK > PLAY. The character walking on the map is the
  one you picked. Pick another one and open the map again — it
  changes with you. Locked characters cannot be picked, so the map
  can never show one you have not unlocked.

ONE THING TO KNOW:
- The map sprite and the bottom-left portrait do not match each
  other yet for Elite / Special / Epic: the map uses the avatar
  SHEET character, the portrait uses the MOCKUP face (that was
  already true in the Avatar screen itself). When Jeruld uploads
  the final assets, change the two file names for that character
  in AvatarCatalog.java to the same final character and they will
  match everywhere. Classic already matches.

VERIFIED (javac 21, headless test with -ea, 2026-11-07)
- javac -d out src/*.java — OK, no "Missing asset" warnings for
  the game itself (the one "Missing asset" line in the test log is
  the test deliberately using a fake file to check the fallback).
- All 4 avatars: unlock+select, map Player avatarId matches,
  hasSprite true, player area has detail, no magenta placeholder,
  LEFT flip renders. Classic vs Epic and Classic vs Elite render
  differently (different blue-pixel counts).
- LIVE SWITCH: map opened as Classic, pick changed to Epic,
  after one game tick Player avatarId = epic, still has a sprite.
- FALLBACK: avatar with a missing sprite file -> hasSprite false,
  old chibi draws, no magenta, no crash.
- NO REGRESSION: Home, Avatar, Settings and Credits still render.
- ALL CHECKS PASSED (45 checks). Zip re-extracted and recompiled
  clean below.

---
UPDATE — HD VISUALS + ANIMATION SYSTEM (2026-11-07)
Jeruld asked: improve the visual quality and animation of the
gameplay, without redesigning the map or removing any feature.

DONE — CHARACTER (Player.java + SpriteSheet.java, new)
- The 4 HD sheets from the last step are now IN the game, in
  assets/sprites/: idle / walk / run / paddle-swing, each 4x2 = 8
  frames, 512 px per cell, transparent PNG. SpriteSheet.java splits
  each sheet once into its 8 frames, so the game loop never crops
  or resizes per frame.
- Character is now 96 px tall on the map (was 72 px), so it is
  easier to see. Collision, speed (walk) and entrances are
  unchanged — only the picture got bigger. 512 px frames drawn
  down to 96 px stay sharp, not pixelated. Rendering is now
  BICUBIC + QUALITY in GamePanel (was BILINEAR).
- Animation state machine: IDLE 10 fps (breathing/blinking in the
  frames), WALK 10 fps, RUN 12 fps, SWING 12 fps, 8 frames = 0.67 s.
  The frame phase is NEVER reset when switching IDLE/WALK/RUN — it
  carries over, so the legs do not jump mid-step. Only SWING
  starts at frame 0, and when it ends the old phase is restored.
  Every state is drawn at the same size and the same feet anchor,
  so there is no position jump or size flicker.
- Controls (new, nothing old removed): hold Shift to run
  (340 px/s, was walk 230 px/s), press J or click empty road to
  swing the paddle. Swing locks movement for its 0.67 s, then
  returns to idle/walk/run. The Settings modal in the map now
  lists these controls.
- HD animation is MALE CLASSIC ONLY, because the sheets were made
  for Male Classic only. Elite / Special / Epic still walk with
  their single full-body sprite (bobbing, flipped for LEFT), so
  the Avatar pick still works for everyone. To give another
  character HD animation, make the same 4 sheets for them and add
  them in Player.setAvatar() — one place.
- DIRECTIONS, honest limitation: walk/run sheets are side /
  three-quarter view, idle/swing are front view. LEFT/RIGHT use
  walk/run (flipped for LEFT). UP/DOWN have no sheet of their own
  (no back view yet), so they use the front idle frames with a
  walk bob, like the old version did, instead of turning sideways
  while walking up a road.

DONE — ENVIRONMENT (CityBackground.java + Car.java + GamePanel.java)
- Moving clouds drifting across the sky (4 clouds, 9-14 px/s,
  wrapping), two birds crossing the sky, fountain sparkles rising
  from the two fountains in the Two Player park, leaves drifting
  down across the city, and a gentle light: a warm glow following
  the player plus a very slow daylight pulse. All are drawn every
  frame from the game clock, so speeds are consistent at any
  frame rate. The static background, roads, buildings, locations
  and HUD are untouched.
- Cars: still the same 4 cars, same speeds and same "wait when
  the player is close" rule — now with a soft shadow under each
  car, so they sit on the road instead of floating.

VERIFIED (javac 21, headless test with -ea, 2026-11-07)
- javac -d out src/*.java — OK.
- All 4 sheets valid, 8 frames each, frame 0 is 512 px; a missing
  sheet is invalid (fallback, not magenta).
- Classic: HD on, starts IDLE, idle frame advances; walk right
  220 px in 0.96 s (~230 px/s), feet y never jumps; walk -> idle
  keeps animTime (1.96 -> 2.56, not reset); Shift run 326 px in
  0.96 s (~340 px/s), faster than walk; swing starts frame 0,
  locks movement, advances, finishes in ~0.67 s and returns to
  the previous animation; LEFT renders; UP uses the front idle
  sheet, as documented.
- Epic: HD off, single sprite on, map renders — Avatar pick
  still works for non-Classic characters.
- Environment: clouds move over 10 s (22,015 px differ); full
  map at t=0 vs t=5 differs (190,365 px sampled) — water,
  clouds, leaves and light all animate.
- NO REGRESSION: Home, Avatar, Settings, Credits still render.
- ALL CHECKS PASSED (43 checks). Zip re-extracted and recompiled
  clean below.

================================================================
MAP 5 LOCATIONS (2026-11-08) — built from PickleSmash_HD_Visuals.zip
(md5 fa5c53124eecc1f05b4dad5227cf9970, verified before building)
================================================================
Approved mockup: layouts/media-generation-pickle-smash-map-reorder-baran-0-cee7569e-d110-44a8-b408-5c5bee2a5f51.png
(top row Barangay / City / Provincial, Training + Home below, profile top left,
coin top right, giant yellow PLAY bottom right).

WHAT CHANGED
- Locations: ONLY 5 now — Barangay Level, City Level, Provincial Level,
  Training, Home. Regional, National, International, Store and Two Player
  Mode are gone completely: no location, no label, no collision, no toast,
  no modal text, no debug-unlock handling for them. Location.Kind is now
  only HOME / TRAINING / LEVEL.
- Layout: top row left to right Barangay (48,150,400,200), City
  (528,150,480,200), Provincial (1088,150,500,200); bottom row Training
  (48,655,440,200), Home (568,655,480,200). Player still starts at Home.
  Every location keeps its original building/court/house sprite and lock
  state (3 levels locked, Training unlocked, Home current). ALL 5 labels
  are drawn — the mockup dropped the Training and Home pills, they are
  back in Java.
- Freed space is filled, not empty: the middle band and the bottom-right
  corner are park blocks (CityBackground.PARK_BLOCKS — sidewalk, lawn,
  trees, benches, lamps, flower bushes). The middle park has the two
  fountains, so the fountain sparkles still rise from the same spots.
  Roads, lane lines and crosswalks were re-drawn to the new block gaps.
- Water GONE: the bottom-right water / swimming pool is removed —
  CityBackground.WATER, drawWaterLip() and drawWater() are deleted, the
  corner is now land (park), and the bottom road lane runs the full width.
- HUD on the map screen: HP bar REMOVED (no heart, no HP text, no HP bar
  anywhere on the map). Top left: profile card — selected avatar portrait,
  "Player 1", "Lv. 1", XP bar 0/100 (the old bottom player card, moved up).
  Top right: coin pill only, 1200 (stars, gear and the Current Location
  panel are gone). Bottom right: GIANT yellow PLAY button, 470x140
  (was 282x112 blue) with pickleball icon, PLAY and a chevron.
  Clicking it does exactly what the old Play did: interact(current) —
  P key / E / Enter / Space behaviour unchanged, no new gameplay mode.
  The bottom nav bar (Map/Inventory/Quests/Achievements/Settings) is
  removed from the map screen only; Settings is still reachable from
  the Home menu. The gear/settings modal on the map is gone with it.
- Cars: still 4, same speeds and wait-near-player rule. The red car used
  to drive vertically at x=1198, but that street is gone (Provincial and
  the parks now sit there), so it drives the top road the other way.
- Kept working, unchanged: HD Male Classic animation (IDLE/WALK/RUN/
  SWING), selected-avatar map character, clouds/birds/fountains/leaves/
  light, WASD/arrows, Shift run, J or click to swing, Home, Avatar,
  Settings and Credits screens. GameState still keeps hp in the save
  file (for compatibility); it is just never shown on the map any more,
  and the Home rest text no longer mentions HP.

VERIFIED (javac 21.0.12.1, headless test with -ea, 2026-11-08)
- javac -d out src/*.java — OK, 41 classes. Only note is the pre-existing
  "unchecked operations" note in GameState.java (was already there).
  No "Missing asset" warnings at runtime.
- Headless test (TestMap5, run from PickleSmashMap/): 53/53 checks passed —
  exactly 5 locations in order Barangay/City/Provincial/Training/Home;
  no Regional/National/International/Store/Two Player anywhere; top row
  y=150 in order, bottom row y=655 Training left of Home; levels locked,
  Training/Home unlocked; player spawns at Home entrance; WATER field
  and drawWater gone; 4 park blocks; no location in the old water corner;
  NAV_BAR/GEAR gone; drawTop 2-arg, drawBottom 3-arg; PLAY 470x140
  bottom right; profile card top left; coin pill top right; all 5 labels
  drawn; 0 HP-green pixels in the old HP bar area; coin gold visible;
  PLAY yellow 33,922 px; 78 water-blue px in the bottom-right corner —
  that is the decorative building strip at the far right edge (x=1598),
  not water; Classic HD on, IDLE start, walk 220 px / 0.96 s, run 326 px,
  swing locks movement; map animates over 10 s (201,080 sampled px);
  Home, Avatar, Settings, Credits all still render.
- Previews: previews/map-5loc-full.png, map-5loc-toprow-closeup.png,
  map-5loc-hud-top.png.

KNOWN DIFFERENCES VS THE MOCKUP (honest list)
- The mockup still shows the bottom-right water under PLAY; in Java it
  is land, as Jeruld asked earlier. The mockup's bottom nav bar is gone
  in Java, as specified. The mockup dropped the Training/Home labels;
  Java puts them back.
- The Java map keeps its own code-drawn style (skyline, roads, block
  proportions) — it is a re-layout of the real game map, not a copy of
  the AI mockup image, so the buildings sit in the same style as before.

================================================================
MAP EXACT IMAGE (2026-11-08) — the map IS the approved mockup
================================================================
YOU ASKED: generate new assets for this map / copy the assets — the
map exactly like the approved reordered mockup image.
DONE: the mockup image itself is now the map background
(GameNew/PickleSmashMap/assets/map_bg_exact.png, 2016x1152, copied
from the approved mockup). GamePanel draws it scaled to the
1642x958 world (x0.8145, y0.8316, ~2% stretch) instead of calling
CityBackground.render(), so no code-drawn buildings/roads/trees can
double-draw over the picture. The baked-in character in front of
the Home house was painted out of the image (fence/walkway cloned
over it), so only the live player stands there.

MEASURED FROM THE IMAGE (image px -> world, x0.8145 y0.8316)
- Profile card baked (26,12)-(602,178) -> world (21,10,469,138);
  avatar circle (20,10,170,170) -> (16,8,140).
- Coin pill baked (1584,18)-(1978,122) -> world (1290,15,321,87);
  coin icon centre (1346,59) r 35.
- PLAY button baked (1412,913)-(1984,1127) -> world (1150,759,466,178);
  pickleball centre (1218,850) r 67; "PLAY" baseline y 892, font 88;
  chevron (1577,841).
- Label pills baked: Barangay (77,274,362x68), City (643,248,326x67),
  Provincial (1238,274,387x68). Live pills are drawn at those exact
  rects (Location.labelX/labelY/labelW), so they cover the baked ones.
  Training and Home labels are drawn in code in the same style above
  their blocks (y 578) — they have no baked pill in the image.
- Building blocks: Barangay (7,328,482,254), City (570,324,485,258),
  Provincial (1181,324,452,258), Training (7,632,474,250),
  Home (570,632,485,250). Entrances: top-row levels on the middle
  road just below their blocks (y 610), Training on the middle road
  just above its block (y 610), Home on the front walkway (802,810)
  — Home's collision rect stops 92px above the block bottom so that
  walkway is walkable. Walkable band: x 42-1600, y 252-920 (the
  image's roads: top y 267-290, middle y 582-637, bottom y 871-925).
- Cars: 4, only on the two wide roads — middle road y 610 (blue,
  red), bottom road y 898 (taxi, green). The top road is only ~23px
  tall, so no cars there. The green car drives behind the PLAY button
  on the bottom road (the button covers that stretch).
- Overlays kept: moving clouds, birds, leaves, warm light (all sit
  above the image and still look right). Fountain sparkles DROPPED:
  the image has no fountains, and the old sparkle spots would rise
  out of the City Hall building.
- Water: the bottom-right water is part of the image and kept as-is
  for the exact look (this overrides the earlier remove-water note
  for this build, per "exactly like this").

VERIFIED (javac 21.0.12.1, headless test with -ea, 2026-11-08)
- javac -d bin src/*.java — OK, 41 classes. Only note is the
  pre-existing "unchecked operations" note in GameState.java.
  No "Missing asset" warnings at runtime.
- Headless test (TestMapExact, run from PickleSmashMap/): 65/65 checks
  passed — exactly 5 locations in order; no dead locations; blocks
  measured from the image; background pixels match the mockup at 7
  sampled points; player spawns at Home (802,816); Home walkway
  walkable, house solid; 4 cars on middle/bottom roads only; all 5
  labels drawn and the 3 level pills cover the baked ones; HUD rects
  are the measured ones; coin gold / PLAY yellow / navy pills visible;
  no HP-bar-length green run; walk 220 px / 0.96 s, run 326 px, swing
  locks movement; Home, Avatar, Settings, Credits all still render.
- Previews: previews/map-exact-full.png, map-exact-toprow-closeup.png,
  map-exact-play-closeup.png.

KNOWN DIFFERENCES / HONEST NOTES
- The live HUD sits on top of the baked HUD in the picture: the
  baked profile/coin/PLAY/labels are still underneath, covered by the
  live versions. If Jeruld's coins, level or XP change, only the live
  HUD updates — the baked numbers underneath stay hidden behind it.
- The image is stretched ~2% (2016x1152 -> 1642x958) to fill the
  world; the stretch is not noticeable.
- The baked chibi was painted out of the background image; two small
  red mailboxes now stand in front of the house (one is from the
  clone) — they read as normal props.
- PLAY still does the old Play action (toast/modal/XP, no real match
  gameplay yet) — unchanged from the 5-location build.
- The green car on the bottom road passes behind the giant PLAY
  button for one stretch, then reappears — the button covers the road
  there, as in the mockup.
