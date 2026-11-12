import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

/**
 * The controllable character on the city map — now with a real
 * HD animation system instead of one static sprite.
 *
 * ANIMATION SYSTEM (new)
 * ----------------------
 * HD sheets live in assets/sprites/ (512 px per cell, transparent
 * PNG — see SpriteSheet.java). They were made for MALE CLASSIC
 * only, so they are used only when the selected avatar IS Classic.
 * Every other avatar (Elite / Special / Epic) keeps the previous
 * behaviour — its single full-body sprite, bobbing while it
 * walks — so the Avatar pick still works for everyone, and
 * Classic simply gets the full animation. To give another
 * character HD animation, make the same sheets for them and add
 * them in setAvatar(); nothing else in this file needs to change.
 *
 * Sheets (all Male Classic):
 *   idle:  4x2 = 8 frames, front view.
 *   swing: 4x2 = 8 frames, front view.
 *   walk right: 1x6 = 6 frames, side view, facing right.
 *   walk left:  1x6 = 6 frames, side view, facing left
 *               (an exact mirror of the right sheet, so the
 *               two sides always match frame for frame).
 *   walk up:    1x5 = 5 frames, straight back view, facing
 *               directly away — never turns, only legs/arms move.
 *   walk down:  1x4 = 4 frames, front view, walking toward you.
 *   run:   3x2 = 6 frames, side view, facing right (flipped
 *               for LEFT). There is no run sheet for UP/DOWN yet,
 *               so running up/down plays the walk up/down sheet
 *               at run speed instead of the idle bob.
 * Every direction has its own sheet now, so UP/DOWN no longer
 * use the front idle frames with a walk bob.
 *
 * State machine: IDLE -> WALK -> RUN -> SWING -> back.
 *   - IDLE: the character idles in the direction he was facing,
 *     instead of snapping to the front view when he stops:
 *     DOWN uses the idle sheet (8 frames, breathing + blink,
 *     10 fps); UP / LEFT / RIGHT hold the standing frame of that
 *     direction's walk sheet (legs together, frame 1) with a
 *     gentle breathing bob. So stopping while walking up leaves
 *     him facing away, and stopping while walking left leaves him
 *     facing left — he never turns to face the camera on his own.
 *   - WALK: 10 fps. RUN: 12 fps, hold Shift (see GamePanel).
 *   - SWING: 12 fps, plays once (J key or click, see GamePanel),
 *     8 frames = 0.67 s, then returns to whatever came before.
 *   - The frame phase (animTime) is NEVER reset when switching
 *     between IDLE / WALK / RUN — it carries over, so the legs do
 *     not jump to a new pose mid-step. Only SWING resets to frame 0
 *     (it must start at the preparation pose), and when it ends
 *     the old phase is restored. The phase is time-based, so
 *     switching between sheets with different frame counts
 *     (6 right, 5 up, 4 down) still never jumps. The idle hold
 *     is the one exception: stopping freezes the display on the
 *     standing frame (a one-frame pose change, no flip and no
 *     size change), while animTime keeps running underneath, so
 *     walking again resumes mid-cycle instead of from frame 0.
 *   - Every animation is drawn at the SAME size and the SAME anchor
 *     (feet at x,y, 96 px tall), so there is no position jump and
 *     no size flicker when the state changes. The idle and swing
 *     sheets were re-normalised to the walk sheets' size (480 px
 *     tall in a 512 cell, feet at y=500) — they used to fill the
 *     whole cell, so the character grew ~7% every time he stopped
 *     or swung, and shrank again when he walked.
 *
 * DIRECTIONS:
 *   - WALK picks a sheet per direction: RIGHT, LEFT, UP, DOWN.
 *   - RUN picks the run sheet for LEFT/RIGHT (flipped for LEFT)
 *     and the walk up/down sheet for UP/DOWN.
 *   - The LEFT walk sheet already faces left, so it is NOT
 *     flipped when drawn — only right-facing sheets are flipped
 *     for LEFT (run, swing; idle is front view, so flipping it
 *     changes nothing). Flipping a directional sheet would put
 *     the paddle in the wrong hand, so drawHd() flips only
 *     when the sheet is not the left walk sheet.
 *   - If a directional sheet is missing, that direction falls
 *     back to the idle sheet (UP/DOWN) or the right walk sheet
 *     (LEFT), exactly like the old "no sheet yet" behaviour —
 *     the map never shows a magenta box.
 *   - SWING is front-facing for every direction, flipped for LEFT.
 *
 * SIZE: the character is 96 px tall on the map.
 * Collision (footBox), speed and entrances are unchanged.
 * The 512 px frames are drawn down to 96 px, so they stay sharp
 * instead of pixelated.
 *
 * MAP AVATAR (from before): whoever you pick in the Avatar section
 * is who walks here — GamePanel calls setAvatar() with
 * state.getSelectedAvatar(), and refreshAvatar() in every game tick
 * picks up a change if the selection changes while the map is open.
 *
 * FALLBACK: if a sprite file is missing, Assets.get() returns a
 * 32x32 magenta square — that is detected here and the old
 * code-drawn chibi (cap + blue shirt, drawChibi) is used instead,
 * so the map never shows a magenta box or crashes.
 */
public class Player {
    public enum Dir { DOWN, UP, LEFT, RIGHT }
    public enum Anim { IDLE, WALK, RUN, SWING }

    /** Lets the player ask the map whether a spot is walkable. */
    public interface World {
        boolean isBlocked(double footX, double footY);
    }

    public double x, y;            // position of the feet
    public Dir dir = Dir.DOWN;
    public boolean moving;
    private double walkT;
    public static final double SPEED = 230;
    public static final double RUN_SPEED = 340;

    /** Height of the character on the map, in map pixels. HD frames are drawn square at this size. */
    public static final int SPRITE_HEIGHT = 96;
    public static final int FRAME_SIZE = 96;

    private static final double IDLE_FPS = 10;
    private static final double WALK_FPS = 10;
    private static final double RUN_FPS = 12;
    private static final double SWING_FPS = 12;
    private static final double SWING_DURATION = SpriteSheet.FRAMES / SWING_FPS; // 8 / 12 = 0.67 s

    // HD sheets, loaded once and shared by every Player (they are big).
    private static SpriteSheet idleSheet, swingSheet;
    private static SpriteSheet walkRightSheet, walkLeftSheet, walkUpSheet, walkDownSheet, runSheet;
    private static boolean hdSheetsTried;

    private BufferedImage sprite;   // single full-body sprite, for non-Classic avatars
    private String avatarId;
    private boolean hdAvailable;

    private Anim anim = Anim.IDLE;
    private double animTime;        // continuous frame phase, in seconds — never reset on IDLE/WALK/RUN change
    private boolean running;
    private boolean swinging;
    private double swingTime;
    private Anim preSwingAnim = Anim.IDLE;
    private double preSwingAnimTime;

    private static final Color SKIN = new Color(0xF2C29B);
    private static final Color HAIR = new Color(0x3B2417);
    private static final Color SHIRT = new Color(0x2F6FDE);
    private static final Color SHIRT_D = new Color(0x1F4FA6);
    private static final Color PANTS = new Color(0x1B2750);
    private static final Color CAP = new Color(0xF4F6FA);

    public Player(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /** Which avatar this player is currently drawn as (null = chibi fallback). */
    public String getAvatarId() { return avatarId; }

    /** True when a real full-body sprite is loaded (false = chibi fallback). */
    public boolean hasSprite() { return sprite != null; }

    /** True when the HD sheet animation is in use (Male Classic only, for now). */
    public boolean hasHd() { return hdAvailable; }
    public Anim getAnim() { return anim; }
    public boolean isSwinging() { return swinging; }
    public boolean isRunning() { return running; }
    public double getAnimTime() { return animTime; }

    /** The walk frame with the legs together — the standing pose,
     *  used as the frozen idle frame for UP / LEFT / RIGHT. */
    private static final int STANDING_FRAME = 1;

    /** Current frame index for the animation that is playing right now.
     *  The range follows the CURRENT sheet (6 right/left walk, 5 up,
     *  4 down, 6 run, 8 idle/swing), so a direction change can never
     *  ask for a frame the sheet does not have.
     *  IDLE is special: on the idle sheet it plays at 10 fps
     *  (breathing + blink are in the frames); on a walk sheet it
     *  holds the standing frame, and the breathing is a bob in
     *  drawHd() instead. */
    public int getFrameIndex() {
        if (swinging) {
            int n = swingSheet != null ? swingSheet.getFrameCount() : SpriteSheet.FRAMES;
            return Math.min(n - 1, (int) (swingTime * SWING_FPS));
        }
        SpriteSheet sheet = currentSheet();
        if (anim == Anim.IDLE && sheet != idleSheet) return STANDING_FRAME;
        int n = sheet != null ? sheet.getFrameCount() : SpriteSheet.FRAMES;
        double fps = switch (anim) {
            case IDLE -> IDLE_FPS;
            case WALK -> WALK_FPS;
            case RUN -> RUN_FPS;
            case SWING -> SWING_FPS;
        };
        return Math.floorMod((int) (animTime * fps), n);
    }

    /** Switch the map character to this avatar's full-body sprite (and HD sheets, for Classic). */
    public void setAvatar(Avatar avatar) {
        if (avatar == null) return;
        avatarId = avatar.getId();
        BufferedImage img = Assets.get(avatar.getMapSpriteFile());
        // Assets.get() returns a 32x32 magenta square for a missing file.
        // A real character sprite is ~170x340, so anything that small is a miss.
        sprite = (img != null && img.getWidth() > 32 && img.getHeight() > 32) ? img : null;
        hdAvailable = false;
        if ("classic".equals(avatarId)) {
            loadHdSheets();
            // Core sheets: idle, swing, walk right and run. The other
            // directional walk sheets are optional — currentSheet()
            // falls back per direction when one is missing.
            hdAvailable = idleSheet != null && swingSheet != null && walkRightSheet != null && runSheet != null
                    && idleSheet.isValid() && swingSheet.isValid() && walkRightSheet.isValid() && runSheet.isValid();
        }
    }

    private static synchronized void loadHdSheets() {
        if (hdSheetsTried) return;
        hdSheetsTried = true;
        idleSheet = new SpriteSheet("sprites/pickle-smash-idle-4x2-512");
        swingSheet = new SpriteSheet("sprites/pickle-smash-paddle-swing-4x2-512");
        walkRightSheet = new SpriteSheet("sprites/walk-right-1x6-512", 6, 1);
        walkLeftSheet = new SpriteSheet("sprites/walk-left-1x6-512", 6, 1);
        walkUpSheet = new SpriteSheet("sprites/walk-up-v2-1x5-512", 5, 1);
        walkDownSheet = new SpriteSheet("sprites/walk-down-1x4-512", 4, 1);
        runSheet = new SpriteSheet("sprites/run-3x2-512", 3, 2);
    }

    /** Pick up a new Avatar selection, if it changed since last tick. Cheap: only reloads on change. */
    public void refreshAvatar(GameState state) {
        if (state == null) return;
        Avatar selected = state.getSelectedAvatar();
        if (selected != null && !selected.getId().equals(avatarId)) setAvatar(selected);
    }

    /** Start the paddle swing (J key / click). Ignored while a swing is already playing. */
    public void startSwing() {
        if (swinging) return;
        swinging = true;
        swingTime = 0;
        preSwingAnim = anim;
        preSwingAnimTime = animTime;
        anim = Anim.SWING;
    }

    /** Old signature, kept for callers that do not know about running: walk speed, no swing. */
    public void update(double dt, boolean up, boolean down, boolean left, boolean right, World world) {
        update(dt, up, down, left, right, false, world);
    }

    public void update(double dt, boolean up, boolean down, boolean left, boolean right, boolean runHeld, World world) {
        // SWING: movement is locked for its 0.67 s, then the old animation resumes
        // with its old frame phase — no jump back to frame 0.
        if (swinging) {
            swingTime += dt;
            moving = false;
            running = false;
            if (swingTime >= SWING_DURATION) {
                swinging = false;
                anim = preSwingAnim;
                animTime = preSwingAnimTime;
            }
            walkT = animTime;
            return;
        }

        double mx = (right ? 1 : 0) - (left ? 1 : 0);
        double my = (down ? 1 : 0) - (up ? 1 : 0);
        moving = mx != 0 || my != 0;
        running = moving && runHeld;
        if (!moving) {
            // Idle: keep the phase running, so breathing never freezes mid-breath
            // when you stop, and restarts smoothly when you move again.
            anim = Anim.IDLE;
            animTime += dt;
            walkT = animTime;
            return;
        }
        double speed = running ? RUN_SPEED : SPEED;
        double len = Math.hypot(mx, my);
        double stepX = mx / len * speed * dt, stepY = my / len * speed * dt;
        moveAxis(stepX, true, world);
        moveAxis(stepY, false, world);
        if (Math.abs(mx) > Math.abs(my) || (my == 0)) dir = mx > 0 ? Dir.RIGHT : Dir.LEFT;
        else dir = my > 0 ? Dir.DOWN : Dir.UP;
        // State change WITHOUT resetting animTime: the legs keep their
        // place in the cycle, so IDLE -> WALK -> RUN never jumps.
        anim = running ? Anim.RUN : Anim.WALK;
        animTime += dt;
        walkT = animTime;
    }

    private void moveAxis(double distance, boolean horizontal, World world) {
        int steps = Math.max(1, (int) Math.ceil(Math.abs(distance) / 4.0));
        double increment = distance / steps;
        for (int i = 0; i < steps; i++) {
            double nextX = horizontal ? x + increment : x;
            double nextY = horizontal ? y : y + increment;
            if (world.isBlocked(nextX, nextY)) break;
            x = nextX;
            y = nextY;
        }
    }

    public void draw(Graphics2D g) {
        if (hdAvailable) drawHd(g);
        else if (sprite != null) drawSprite(g);
        else drawChibi(g);
    }

    /** HD sheet animation, at one fixed size and anchor for every state. */
    private void drawHd(Graphics2D g) {
        SpriteSheet sheet = currentSheet();
        if (sheet == null || !sheet.isValid()) { drawSprite(g); return; }
        BufferedImage frame = sheet.getFrame(getFrameIndex());
        if (frame == null) { drawSprite(g); return; }

        AffineTransform old = g.getTransform();
        g.translate(x, y);

        // Small bob on top of the frames (the frames already move the legs,
        // so this is only 2-3 px — enough to feel the step, not enough to jump).
        // IDLE on a walk sheet has no breathing in its frames (it is one
        // frozen standing frame), so it gets a slow breathing bob instead;
        // IDLE on the idle sheet already breathes in the frames, so no bob.
        double bob = switch (anim) {
            case WALK -> Math.abs(Math.sin(animTime * 10)) * 2;
            case RUN -> Math.abs(Math.sin(animTime * 12)) * 3;
            case IDLE -> (sheet == idleSheet) ? 0 : 1 + Math.sin(animTime * 3) * 1;
            default -> 0;
        };

        // Soft shadow: shrinks a little as the character bobs up.
        double shadowW = 36 - bob;
        g.setColor(new Color(0, 0, 0, 70));
        g.fill(new Ellipse2D.Double(-shadowW / 2, -5, shadowW, 10));

        // The left walk sheet already faces left, so it must NOT be
        // flipped — flipping it would turn the character right again
        // and put the paddle in the wrong hand. Every other sheet
        // (right-facing run/walk, front idle/swing) flips for LEFT
        // exactly like before.
        if (dir == Dir.LEFT && sheet != walkLeftSheet) g.scale(-1, 1); // face left
        g.drawImage(frame, -FRAME_SIZE / 2, -FRAME_SIZE - (int) bob, FRAME_SIZE, FRAME_SIZE, null);

        g.setTransform(old);
    }

    /** First sheet if it loaded, otherwise the fallback. Keeps one
     *  missing directional sheet from breaking the whole character. */
    private static SpriteSheet validOr(SpriteSheet first, SpriteSheet fallback) {
        return (first != null && first.isValid()) ? first : fallback;
    }

    /** Which sheet is playing right now. WALK has a sheet per
     *  direction; RUN uses the run sheet for LEFT/RIGHT and the
     *  walk up/down sheet (at run speed) for UP/DOWN. IDLE uses
     *  the direction's sheet too, so stopping never turns the
     *  character to face the camera: DOWN gets the real idle
     *  sheet (front view, breathing + blink), the other
     *  directions hold their walk sheet's standing frame. */
    private SpriteSheet currentSheet() {
        return switch (anim) {
            case SWING -> swingSheet;
            case IDLE -> switch (dir) {
                case DOWN -> idleSheet;
                case RIGHT -> validOr(walkRightSheet, idleSheet);
                case LEFT -> validOr(walkLeftSheet, walkRightSheet);
                case UP -> validOr(walkUpSheet, idleSheet);
            };
            case WALK -> switch (dir) {
                case RIGHT -> validOr(walkRightSheet, idleSheet);
                case LEFT -> validOr(walkLeftSheet, walkRightSheet);
                case UP -> validOr(walkUpSheet, idleSheet);
                case DOWN -> validOr(walkDownSheet, idleSheet);
            };
            case RUN -> switch (dir) {
                case RIGHT, LEFT -> validOr(runSheet, walkRightSheet);
                case UP -> validOr(walkUpSheet, idleSheet);
                case DOWN -> validOr(walkDownSheet, idleSheet);
            };
        };
    }

    /** The selected avatar's single full-body sprite, flipped for LEFT, bobbing while walking. */
    private void drawSprite(Graphics2D g) {
        AffineTransform old = g.getTransform();
        g.translate(x, y);

        double bob = moving ? Math.abs(Math.sin(walkT * 13)) * 3 : 0;

        // shadow (drawn before the flip — it is symmetric, so it stays centred)
        g.setColor(new Color(0, 0, 0, 70));
        g.fill(new Ellipse2D.Double(-18, -5, 36, 10));

        int h = SPRITE_HEIGHT;
        int w = (int) Math.round(sprite.getWidth() * (h / (double) sprite.getHeight()));
        if (dir == Dir.LEFT) g.scale(-1, 1); // face left; RIGHT / DOWN / UP use the front view as-is
        g.drawImage(sprite, -w / 2, -h - (int) bob, w, h, null);

        g.setTransform(old);
    }

    /** The old code-drawn chibi (cap + blue shirt) — kept as the fallback when no sprite loads. */
    private void drawChibi(Graphics2D g) {
        AffineTransform old = g.getTransform();
        g.translate(x, y);

        double swing = moving ? Math.sin(walkT * 13) : 0;
        double bob = moving ? Math.abs(Math.sin(walkT * 13)) * 3 : 0;
        boolean side = dir == Dir.LEFT || dir == Dir.RIGHT;
        int s = dir == Dir.LEFT ? -1 : 1;

        // shadow
        g.setColor(new Color(0, 0, 0, 70));
        g.fill(new Ellipse2D.Double(-15, -5, 30, 10));

        // legs + shoes
        if (side) {
            leg(g, -3 + swing * 6, -bob * 0.3);
            leg(g, 3 - swing * 6, -bob * 0.3);
        } else {
            leg(g, -6, -Math.max(0, swing) * 4);
            leg(g, 6, -Math.max(0, -swing) * 4);
        }

        // arms (behind body for side view)
        g.setColor(SKIN);
        if (side) {
            g.fill(new Ellipse2D.Double(-4 + swing * -6, -30 - bob, 8, 10));
        }

        // torso
        double bw = side ? 18 : 26;
        g.setColor(SHIRT);
        g.fill(new RoundRectangle2D.Double(-bw / 2, -40 - bob, bw, 24, 8, 8));
        g.setColor(SHIRT_D);
        g.fill(new RoundRectangle2D.Double(-bw / 2, -22 - bob, bw, 6, 4, 4));
        if (dir == Dir.DOWN) { // white chevron on the chest
            Path2D p = new Path2D.Double();
            p.moveTo(-5, -33 - bob); p.lineTo(0, -26 - bob); p.lineTo(5, -33 - bob);
            p.lineTo(3, -33 - bob); p.lineTo(0, -29 - bob); p.lineTo(-3, -33 - bob); p.closePath();
            g.setColor(Color.WHITE);
            g.fill(p);
        }
        if (!side) { // arms
            g.setColor(SHIRT);
            g.fill(new RoundRectangle2D.Double(-bw / 2 - 5, -38 - bob + swing * -2, 6, 15, 5, 5));
            g.fill(new RoundRectangle2D.Double(bw / 2 - 1, -38 - bob + swing * 2, 6, 15, 5, 5));
            g.setColor(SKIN);
            g.fill(new Ellipse2D.Double(-bw / 2 - 5, -25 - bob + swing * -2, 6, 6));
            g.fill(new Ellipse2D.Double(bw / 2 - 1, -25 - bob + swing * 2, 6, 6));
        } else {
            g.setColor(SHIRT);
            g.fill(new RoundRectangle2D.Double(-3 + swing * 6, -38 - bob, 7, 15, 5, 5));
            g.setColor(SKIN);
            g.fill(new Ellipse2D.Double(-3 + swing * 6, -25 - bob, 7, 6));
        }

        // head
        double hy = -62 - bob;
        g.setColor(HAIR);
        g.fill(new Ellipse2D.Double(-13, hy + 2, 26, 26));
        g.setColor(SKIN);
        if (dir == Dir.DOWN) {
            g.fill(new Ellipse2D.Double(-11, hy + 5, 22, 21));
        } else if (side) {
            g.fill(new Ellipse2D.Double(s > 0 ? -9 : -11, hy + 5, 20, 21));
        }

        // face
        if (dir == Dir.DOWN) {
            eye(g, -5, hy + 15);
            eye(g, 5, hy + 15);
            g.setColor(new Color(0xC9704F));
            g.setStroke(new BasicStroke(1.4f));
            g.drawArc(-3, (int) (hy + 19), 6, 4, 200, 140);
        } else if (side) {
            eye(g, 5 * s, hy + 15);
        }

        // cap
        g.setColor(CAP);
        g.fill(new java.awt.geom.Arc2D.Double(-14, hy - 4, 28, 26, 0, 180, java.awt.geom.Arc2D.CHORD));
        g.setColor(SHIRT_D);
        g.fill(new java.awt.geom.Arc2D.Double(-14, hy - 4, 28, 26, 25, 40, java.awt.geom.Arc2D.PIE));
        if (dir == Dir.DOWN) {
            g.setColor(SHIRT_D);
            g.fill(new RoundRectangle2D.Double(-13, hy + 8, 26, 5, 5, 5)); // brim
            Path2D logo = new Path2D.Double(); // small "A" mark
            logo.moveTo(0, hy); logo.lineTo(-3.5, hy + 7); logo.lineTo(3.5, hy + 7); logo.closePath();
            g.setColor(new Color(0x3C8BFF));
            g.fill(logo);
        } else if (side) {
            g.setColor(SHIRT_D);
            g.fill(new RoundRectangle2D.Double(s > 0 ? 2 : -20, hy + 8, 18, 5, 5, 5));
        }

        g.setTransform(old);
    }

    private void leg(Graphics2D g, double ox, double lift) {
        g.setColor(PANTS);
        g.fill(new RoundRectangle2D.Double(ox - 4, -17 + lift, 8, 12, 4, 4));
        g.setColor(Color.WHITE);
        g.fill(new RoundRectangle2D.Double(ox - 5, -6 + lift, 10, 6, 4, 4));
    }

    private void eye(Graphics2D g, double ex, double ey) {
        g.setColor(new Color(0x1A1A2E));
        g.fill(new Ellipse2D.Double(ex - 2.5, ey - 3, 5, 7));
        g.setColor(Color.WHITE);
        g.fill(new Ellipse2D.Double(ex - 1.2, ey - 2.2, 2, 2));
    }

    /** Small rectangle at the feet used for collision tests. */
    public static Rectangle footBox(double fx, double fy) {
        return new Rectangle((int) fx - 10, (int) fy - 8, 20, 8);
    }
}
