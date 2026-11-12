PICKLE SMASH - HOME SCREEN UPDATE (image background)

This update makes the home screen use your supplied artwork
(home_menu_bg.png) instead of drawing the scene in code.

WHAT CHANGED (straighten on hover, 2026-11-07)
- Kept the hover effect (lift, brighten ~30%, golden glow/border,
  white sheen, animated pop), but the hovered button now straightens.
- The painted buttons are slanted, rising ~10 px to the right
  (~1.9 degrees). On hover the button is rotated clockwise by 1.9
  degrees around its centre, so its top/bottom and its PLAY text
  become level. The rotation is scaled by hoverAnim, so it
  straightens smoothly as it pops in.
- The brightened button, its plank clip, glow and gold border are
  all drawn in the same rotated context, so the outline and the
  button straighten together and always match. Un-hovered buttons
  stay slanted, as painted.
- Pop is now 14% with only 4 px lift, so the straightened button
  stays big enough to fully cover the slanted original underneath.
- Verified: compiled with javac 21, headless test hovered PLAY
  (hoverIndex=0, hoverAnim=1.0); PLAY top edge measured level
  (274-278 px across, vs 290-297 px slanted when not hovered),
  and the render shows PLAY straight, brighter and larger than
  the other buttons.

WHAT CHANGED (hover outline fix, 2026-11-07)
- The hover used a rounded-rectangle clip/border/glow, so it looked
  like a square box around the button.
- Measured from the artwork, each button is a slanted wooden plank:
  it rises ~10-12 px to the right, with chamfered/pointed ends at
  mid-height - not a rectangle.
- HomeMenuPanel.java now has plankShape(): a slanted hexagon used for
  the clip, the golden glow strokes and the gold border, so the hover
  traces the plank outline itself. No rectangle is drawn.
- Verified: compiled with javac 21, headless test hovered PLAY
  (hoverIndex=0, hoverAnim=1.0); the render shows PLAY brighter,
  larger, and outlined along its pointed, slanted plank shape.

WHAT CHANGED (hover effect fix, 2026-11-07)
- The old hover was only a thin faint gold outline (alpha ~90) + a
  26-alpha white fill, drawn as a straight rectangle. Against the
  bright wood it was almost invisible in a recording.
- New hover in HomeMenuPanel.java: the painted button itself lifts
  ~8 px, grows ~8%, is brightened ~30% and warmed, with a soft golden
  glow behind it, a bright gold border, and a white sheen on top.
  The grow/shrink is animated, so the button visibly pops in.
  Pressing a button sinks it back a little.
- The brightened button is clipped to a rounded rect so no sky/court
  shows in the corners when it is enlarged.
- Verified: compiled with javac 21, headless test moved the mouse to
  PLAY (hoverIndex=0, hoverAnim=1.0) and to AVATAR (hoverIndex=1),
  and the renders show PLAY clearly brighter/larger than the others.

WHAT CHANGED
- Added: PickleSmashMap/assets/home_menu_bg.png (your image, 1672 x 941)
- Changed: PickleSmashMap/src/HomeMenuPanel.java
  * Draws home_menu_bg.png scaled to fill the 1280 x 720 screen.
  * The player, logo, court and wooden buttons are already in the
    image, so the old code-drawn sky / court / logo / player / buttons
    were removed.
  * The five buttons are invisible click areas measured from the
    artwork itself, so clicking the painted PLAY / AVATAR / SETTINGS /
    CREDITS / EXIT buttons still works:
      PLAY     (866, 282, 304, 80)
      AVATAR   (866, 368, 304, 72)
      SETTINGS (866, 442, 304, 70)
      CREDITS  (866, 516, 304, 66)
      EXIT     (866, 586, 304, 72)
  * Hovering a button shows a soft golden glow and a hand cursor.
  * A few twinkling sparkles are drawn on top to keep it alive.
  * If home_menu_bg.png is missing, a blue fallback screen is drawn
    with a message telling you to put the file in assets/.

ECLIPSE SETUP
1. Extract this ZIP.
2. In Eclipse choose File > Import > Existing Projects into Workspace.
3. Select the extracted Pickle_Smash folder (the folder containing .project).
4. Finish.
5. Expand PickleSmashMap/src and open Main.java.
6. Right-click Main.java > Run As > Java Application.

CONTROLS
Home screen: click PLAY, AVATAR, SETTINGS, CREDITS, or EXIT.
City map: WASD / arrow keys to move; E / Enter / Space to interact; P to play.

VERIFIED
- Compiled with javac 21 (OpenJDK 21.0.12): javac -d out src/*.java — OK
- Headless render test of HomeMenuPanel at 1280 x 720 confirmed the
  full artwork draws and the buttons line up with the painted buttons.

NOTES
- Keep the assets folder at PickleSmashMap/assets.
- The AVATAR button is still a placeholder for the next step.
