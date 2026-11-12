PICKLE SMASH — DIRECTIONAL ANIMATION (walk in all 4 directions + new run)
=====================================================================
Built 2026-11-08 from PickleSmash_Map_Exact_Image.zip
(md5 c5a8ae30e9d21ebe2b6fe8e23f822d68 — verified before building).
Nothing else changed: the 5-location exact map, Home/Avatar/Settings/
Credits, the selected-avatar map character and the save file are all
exactly as in that build.

WHAT IS NEW
-----------
Male Classic now really walks in all four directions on the map,
instead of flipping the side view for LEFT and faking UP/DOWN with
front idle frames and a bob:

  WALK (10 fps)          RUN (12 fps, hold Shift)
  RIGHT  6 frames        RIGHT/LEFT  6 frames (new run sheet)
  LEFT   6 frames        UP/DOWN     walk up/down sheet at run speed
  UP     5 frames        (there is no run sheet for up/down yet)
  DOWN   4 frames

  UP is the fixed straight-back walk: in every frame he faces
  directly away (back of head, back of jersey, no face), paddle
  in his right hand. It is the 5-frame strip from the image Jeruld
  sent back ("use this one in up movement") — the generator drew
  a 6th frame that turned three-quarter with his face showing, and
  that frame was dropped.

  The old 4x2 walk/run sheets (pickle-smash-walk/run-4x2-512.png)
  are still in assets/sprites/ but are no longer loaded. The old
  walk sheet mixed front three-quarter and side views mid-stride,
  and the old run sheet had a few frames where the paddle looked
  like a tennis racket. Idle (4x2) and paddle-swing (4x2) are
  unchanged, and Elite / Special / Epic still use their single
  full-body sprite walk, exactly as before.

NEW FILES
---------
assets/sprites/walk-right-1x6-512.png   (3072x512, 6 frames)
assets/sprites/walk-left-1x6-512.png    (3072x512, 6 frames, exact mirror)
assets/sprites/walk-up-v2-1x5-512.png   (2560x512, 5 frames)
assets/sprites/walk-down-1x4-512.png    (2048x512, 4 frames)
assets/sprites/run-3x2-512.png          (1536x1024, 6 frames)
All 512 px cells, transparent PNG, feet on one ground line.

CODE CHANGES (only 2 files)
---------------------------
SpriteSheet.java
  - Now takes a grid: new SpriteSheet(name, cols, rows). The old
    one-argument form stays 4x2 for idle/swing. isValid() and
    getFrame() use that sheet's own frame count (getFrameCount()),
    so a 5-frame up sheet and a 4-frame down sheet both work.

Player.java
  - loadHdSheets() loads the 7 sheets above (idle, swing, walk
    right/left/up/down, run). hdAvailable needs the core four
    (idle, swing, walk right, run); a missing directional sheet
    falls back per direction (UP/DOWN -> idle, LEFT -> right
    walk sheet) instead of breaking the character.
  - currentSheet() picks the sheet by animation AND direction.
    getFrameIndex() uses the current sheet's frame count, so the
    frame index can never exceed what the sheet has.
  - drawHd() does NOT flip the left walk sheet (it already faces
    left); flipping it would turn him right again and put the
    paddle in the wrong hand. Right-facing sheets (run, swing)
    still flip for LEFT, as before.
  - The frame phase (animTime) is still never reset on
    IDLE/WALK/RUN changes, and it is time-based, so switching
    between sheets with different frame counts (6 right, 5 up,
    4 down) never makes the legs jump.

HOW TO RUN
----------
  cd GameNew/PickleSmashMap
  javac -d classes src/*.java
  java -cp classes Main
Or double-click run.bat (Windows) / run.sh.

VERIFICATION (headless, java.awt.headless=true, -ea)
----------------------------------------------------
48/48 checks passed (test/HeadlessAnimTest.java — not shipped
in the zip, kept in the build folder):
  - Classic hasHd; every direction selects the right sheet with
    the right frame count (6/6/5/4 walk, 6 run, 8 idle/swing)
  - left render is the mirror of the right render (1.05% pixel
    diff — bicubic resampling noise, not a facing error)
  - up render differs from down render; paddle on the viewer's
    right in the up render and on the viewer's left in the down
    render (blue-pixel bands: up 110-120=29 vs 80-90=10;
    down 80-90=22 vs 110-120=9)
  - up animation advances between frames; phase carries over
    when the direction changes; swing plays once (0.67 s) and
    returns to the pre-swing animation
  - Elite still uses the single-sprite walk (hasHd false)
  - Full map renders in all four directions (previews in
    ../previews/map-anim-{up,right,down,left}[-closeup].png)
Compile: javac 21.0.12.1, 41 classes, no errors (only the
pre-existing unchecked note in GameState.java). No missing-asset
warnings. The zip was re-extracted and recompiled clean.

KNOWN LIMITS
------------
- Run up/down is the walk up/down sheet at 12 fps, not a true
  run cycle — there is no up/down run sheet yet.
- Down is 4 frames and up is 5, so their cycles are shorter
  than the 6-frame side walks; at 10 fps on the map this is
  not noticeable, but a 6-frame down/up would match exactly.
- HD animation is Male Classic only. To animate another
  character the same way, make their 7 sheets and add them in
  Player.setAvatar().
