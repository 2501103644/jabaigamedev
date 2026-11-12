PICKLE SMASH — IDLE FIX, NO CARS, SMOOTH ANIMATION
====================================================
Built 2026-11-08 from PickleSmash_Animations_4Dir.zip
(md5 be4c538b7ab9da1c5a2cc4ccb8077898 — verified before building).
Nothing else changed: the 5-location exact map, Home/Avatar/Settings/
Credits, the selected-avatar map character and the save file are all
exactly as in that build.

1. IDLE FIXED — two real bugs
-----------------------------
(a) He snapped to the FRONT view whenever he stopped. IDLE always
    used the front idle sheet, whatever direction he was facing —
    walk up (back view), stop, and he suddenly turned to face the
    camera. Now idle is direction-aware: DOWN uses the idle sheet
    (front view, breathing + blink); UP / LEFT / RIGHT hold the
    standing frame (legs together, frame 1) of that direction's
    walk sheet, with a gentle breathing bob. Stopping while
    walking up leaves him facing away; stopping while walking
    left leaves him facing left. He never turns on his own now.
(b) He GREW ~7% every time he stopped or swung. The idle and
    swing sheets filled the whole 512 px cell (510-512 px tall,
    feet at the very bottom edge), while the walk sheets are
    480 px tall with feet at y=500 — so the character got bigger
    when he stopped and smaller when he walked. Both sheets were
    re-normalised to the walk standard (480 px max height, feet
    exactly at y=500, measured after resampling). The swing's
    crouch frame and the idle's breathing variation are kept.

2. CARS REMOVED
---------------
The four moving cars (blue, red, taxi, green) are gone from the
map: the cars list, buildCars(), the car update in the game loop
and the car drawing are all removed from GamePanel.java. The
roads are empty now. Car.java is still in the project, unused —
    to bring the cars back, restore these 4 lines in GamePanel:
      private final List<Car> cars = new ArrayList<>();
      buildCars();                       (in the constructor)
      for (Car c : cars) c.update(dt, player.x, player.y);
      for (Car c : cars) c.draw(g);      (in paintComponent)
    plus the buildCars() method (it was 4 cars on the two wide
    horizontal roads). The faint red oval on the middle road is
    NOT a car — it is the Provincial Level entrance marker (red
    because the level is locked); it was hidden behind a car
    before, so it shows now that the cars are gone.

3. SMOOTH ANIMATION — why it lagged, and what changed
-------------------------------------------------------
The game repaints the whole map 60 times a second. Three things
in every repaint were far too expensive, and together they cost
more than the 16 ms a 60 fps frame allows:
  - Every animation frame was scaled from 512 px down to 96 px
    with bicubic interpolation, EVERY frame. Now SpriteSheet
    scales each frame ONCE when the sheet loads (bicubic, so the
    edges stay crisp) and the game loop only draws the 96 px
    copy — a plain blit. The player draw went from the most
    expensive part of the frame to 0.07 ms.
  - The 1642x958 map background was resampled to the panel size
    with bicubic interpolation EVERY frame. Now GamePanel keeps
    a copy pre-scaled to the panel's pixels and rebuilds it only
    when the window size changes; each frame is a 1:1 blit.
  - The daylight pulse was a translucent fillRect over the whole
    screen EVERY frame (~6 ms, for an effect nobody can see
    change — alpha 4-10). It is now baked into the background
    once, at its average alpha 7. The warm glow around the
    player is also rendered once and blitted, instead of building
    a new radial gradient every frame.
  - The per-frame interpolation hint is now BILINEAR instead of
    bicubic: everything drawn per frame is already at (or near)
    its final size, so bicubic only cost time.
Measured headless (pure software rendering, worst case):
  before: ~20 ms per paint — over budget, visibly choppy.
  after:  13.7 ms per paint — inside the 16 ms budget for 60 fps.
  On a real screen the display pipeline is faster than headless,
  so there is headroom. The timer is unchanged (16 ms, ~60 fps);
  the clouds, birds, leaves, glow, HUD, labels and entrances
  all still animate exactly as before.

CODE CHANGES (4 files + 2 sheets)
----------------------------------
  SpriteSheet.java     — frames pre-scaled to 96x96 at load.
  Player.java          — directional idle (standing frame + bob),
                         getFrameIndex/currentSheet are sheet-aware.
  GamePanel.java       — cars removed; panel-sized background
                         cache; bilinear per-frame hint; pulse
                         baked into the background cache.
  CityBackground.java  — glow image cached; pulse fillRect removed.
  assets/sprites/pickle-smash-idle-4x2-512.png       (normalised)
  assets/sprites/pickle-smash-paddle-swing-4x2-512.png (normalised)

HOW TO RUN
----------
  cd GameNew/PickleSmashMap
  javac -d classes src/*.java
  java -cp classes Main
Or double-click run.bat (Windows) / run.sh.

VERIFICATION (headless, java.awt.headless=true, -ea)
----------------------------------------------------
68/68 checks passed (test/HeadlessAnimTest.java — not shipped
in the zip, kept in the build folder). The 48 checks from the
last build still pass (sheets per direction, frame counts,
left-is-mirror-of-right, paddle sides, swing, Elite fallback),
plus 20 new ones:
  - idle after walking up/left/right uses that direction's
    walk sheet and holds the standing frame (index 1); idle
    after walking down uses the idle sheet (frames 0-7)
  - idle and swing sheets normalised: every frame 461-480 px
    tall, feet exactly at y=500
  - no cars field in GamePanel
  - average paint 13.67 ms over 60 paints (was ~20 ms)
Compile: javac 21.0.12.1, 41 classes, no errors (only the
pre-existing unchecked note in GameState.java). No missing-asset
warnings. The zip was re-extracted and recompiled clean, and
the same 68 checks passed from the fresh copy.
Previews: ../previews/map-idle-{up,right,down,left}[-closeup].png
(no cars) and map-anim-{up,right,down,left}[-closeup].png.

KNOWN LIMITS
------------
- Stopping snaps the legs to the standing frame in one frame
  (a small pose change, no flip and no size change). Walking
  again resumes mid-cycle because the phase keeps running.
- Run up/down is still the walk up/down sheet at run speed
  (no up/down run sheet yet), and down is 4 frames, up 5,
  vs 6 for the sides.
- HD animation is Male Classic only.
