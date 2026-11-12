import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The city level-select map. Walk the player with WASD / arrow keys and talk to a level guide
 * with E / Enter / Space to enter. Clicking a location label shows directions; it does not move
 * the player.
 * Hold Shift to run, press J (or click empty road) to swing the paddle.
 * MAP EXACT IMAGE: the map background IS the approved mockup image
 * (assets/map_bg_exact.png, 2016x1152, stretched to the 1642x958 world, ~2% stretch) —
 * the code-drawn CityBackground is NOT rendered any more, so the map looks exactly like
 * the image. The live HUD (profile card, coin pill) and the 5 label pills are
 * drawn in code on top of the image, positioned to cover the baked-in ones.
 * Locations: only Barangay / City / Provincial levels, Training and Home.
 * Top row left to right: Barangay, City, Provincial. Bottom row: Training, Home.
 * The bottom-right water is part of the image and kept as-is for the exact look.
 * HUD: profile card top left and coin pill top right —
 * no HP, no stars, no gear, no nav bar.
 */
public class GamePanel extends JPanel implements ActionListener, KeyListener, MouseListener, MouseMotionListener, Player.World {
    public static final int W = 1642, H = 958;
    private static final Rectangle BOTTOM_RIGHT_BOUNDARY = new Rectangle(1150, 759, 466, 178);

    /** Implement this to start your real game screens. */
    public interface LocationListener {
        void onEnter(Location loc, GameState state);
    }

    private final javax.swing.Timer timer = new javax.swing.Timer(16, this);
    private long lastNanos = System.nanoTime();
    private double time;

    private final GameState state;
    private final List<Location> locations = new ArrayList<>();
    private final List<LevelNpc> levelNpcs = new ArrayList<>();
    private final List<Rectangle> solids = new ArrayList<>();
    private final Set<Integer> keys = new HashSet<>();
    private final Player player;
    private BufferedImage background;
    /** The background pre-scaled to the panel's pixels (rebuilt only
     *  when the panel size changes). Drawing the 1642x958 background
     *  through the world transform every frame meant a bicubic
     *  resample of 1.5M pixels 60 times a second — most of the lag.
     *  This is a plain 1:1 blit instead. No cars any more: the
     *  moving cars were removed (see the note in the README) —
     *  Car.java is still in the project, so they can come back. */
    private BufferedImage backgroundScaled;

    private Location current;
    private Location near;
    private Location hoverLoc;

    private String toast;
    private double toastTime;

    // modal dialog state
    private boolean modalOpen;
    private boolean npcDialogueOpen;
    private String modalTitle;
    private List<String> modalLines = new ArrayList<>();
    private Location modalLoc;
    private BufferedImage npcDialoguePortrait;
    private boolean modalConfirmable;
    private final Rectangle confirmRect = new Rectangle(), backRect = new Rectangle();

    private double scale = 1, offX, offY;
    private boolean unlockAll;
    private LocationListener listener = this::defaultEnter;

    public GamePanel() {
        this(new GameState());
    }

    /** Shared progress: the home / avatar / settings screens see the same coins and unlocks. */
    public GamePanel(GameState state) {
        this.state = state;
        setPreferredSize(new Dimension(1280, 748));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);

        buildLocations();
        buildLevelNpcs();
        for (Location l : locations) solids.add(l.solid);

        current = find("Home");
        player = new Player(current.entrance.x, current.entrance.y + 6);
        // MAP AVATAR: whoever is picked in the Avatar section walks here.
        // A new GamePanel is made every time PLAY opens the map, so this
        // reads the CURRENT selection; refreshAvatar() in the game loop
        // below also picks up a change while the map is open.
        player.setAvatar(state.getSelectedAvatar());
        // MAP EXACT IMAGE: the approved mockup IS the map background, scaled to
        // the world (2016x1152 -> 1642x958, x0.8145 y0.8316, ~2% stretch).
        // CityBackground.render() is NOT used any more, so nothing code-drawn
        // can double-draw over the image.
        BufferedImage src = Assets.get("map_bg_exact");
        background = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);
        Graphics2D bg = background.createGraphics();
        bg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        bg.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        bg.drawImage(src, 0, 0, W, H, null);
        bg.dispose();
        timer.start();
    }

    public void setLocationListener(LocationListener l) {
        this.listener = l;
    }

    // ------------------------------------------------------------------ setup
    private void buildLocations() {
        // MAP EXACT IMAGE — name, kind, block, sprite, pin icon, entrance, locked,
        // solid (collision), labelY, labelW. Block rects were measured from the mockup
        // image (2016x1152 -> world x0.8145, y0.8316): top row blocks sit at world
        // y 324-328, bottom row at y 632; label pills measured from the image too
        // (Barangay 77,274,362x68 / City 643,248,326x67 / Provincial 1238,274,387x68),
        // with a few px of margin so the live pills cover the baked ones.
        // Entrances sit on the roads next to each block: top-row entrances on the
        // middle road just below the blocks, Training on the middle road just above
        // its block, Home on the front walkway in front of the house, as in the image
        // (its solid stops 92px above the block bottom so that walkway is walkable).
        locations.add(new Location("Barangay Level", Location.Kind.LEVEL, new Rectangle(7, 328, 482, 254),
                "barangay_hall", "pin_blue", new Point(248, 610), true,
                new Rectangle(7, 328, 482, 254), 274, 370, 77));
        locations.add(new Location("City Level", Location.Kind.LEVEL, new Rectangle(570, 324, 485, 258),
                "city_hall", "pin_blue", new Point(812, 610), true,
                new Rectangle(570, 324, 485, 258), 248, 334, 643));
        locations.add(new Location("Provincial Level", Location.Kind.LEVEL, new Rectangle(1181, 324, 452, 258),
                "provincial_hall", "pin_blue", new Point(1407, 610), true,
                new Rectangle(1181, 324, 452, 258), 274, 395, 1238));
        locations.add(new Location("Training", Location.Kind.TRAINING, new Rectangle(7, 632, 474, 250),
                "training", "pin_blue", new Point(244, 610), false,
                new Rectangle(7, 632, 474, 250), 578, 0));
        locations.add(new Location("Home", Location.Kind.HOME, new Rectangle(570, 632, 485, 250),
                "home", "pin_home", new Point(802, 810), false,
                new Rectangle(570, 632, 485, 158), 578, 0));
    }

    private void buildLevelNpcs() {
        levelNpcs.add(new LevelNpc(find("Barangay Level"), "Barangay Guide",
                "The local court is ready. Are you ready to play?",
                new Color(0x38251E), new Color(0x4C8A54), new Color(0xF4C84A)));
        levelNpcs.add(new LevelNpc(find("City Level"), "City Guide",
                "Welcome to the city court. Shall we begin?",
                new Color(0x202633), new Color(0x3479C9), new Color(0xF0B44D)));
        levelNpcs.add(new LevelNpc(find("Provincial Level"), "Provincial Guide",
                "The provincial challenge awaits. Are you prepared?",
                new Color(0x29202D), new Color(0xA94C4C), new Color(0xE2C96E)));
        levelNpcs.add(new LevelNpc(find("Training"), "Training Coach",
                "Ready to practice? Let's work on your serves.",
                new Color(0x38251E), new Color(0x2F6FDE), new Color(0xF4C84A)));
    }

    private LevelNpc npcFor(Location location) {
        for (LevelNpc npc : levelNpcs) if (npc.location == location) return npc;
        return null;
    }

    private Location find(String name) {
        for (Location l : locations) if (l.name.equals(name)) return l;
        throw new IllegalArgumentException(name);
    }

    // ------------------------------------------------------------------ game loop
    @Override
    public void actionPerformed(ActionEvent e) {
        long now = System.nanoTime();
        double dt = Math.min(0.05, (now - lastNanos) / 1e9);
        lastNanos = now;
        time += dt;

        player.refreshAvatar(state); // live-switch if the Avatar pick changed
        if (!modalOpen) {
            boolean runHeld = keys.contains(KeyEvent.VK_SHIFT);
            player.update(dt, keyDown(KeyEvent.VK_UP, KeyEvent.VK_W), keyDown(KeyEvent.VK_DOWN, KeyEvent.VK_S),
                    keyDown(KeyEvent.VK_LEFT, KeyEvent.VK_A), keyDown(KeyEvent.VK_RIGHT, KeyEvent.VK_D), runHeld, this);
        } else {
            player.moving = false;
        }

        near = null;
        double best = 75;
        for (Location l : locations) {
            double d = Math.hypot(player.x - l.entrance.x, player.y - l.entrance.y);
            if (d < best) { best = d; near = l; }
            if (d < 130) current = l;
        }
        if (toastTime > 0) toastTime -= dt;
        repaint();
    }

    private boolean keyDown(int a, int b) {
        return keys.contains(a) || keys.contains(b);
    }

    /** Walkable test for the player's feet: inside the map, not inside a block.
     *  Bounds follow the image's roads: the skyline ends at world y ~245, the
     *  bottom road runs at y 871-925, side buildings start at x ~33 / 1608.
     *  The full collision box stays inside the walkable area. */
    @Override
    public boolean isBlocked(double fx, double fy) {
        Rectangle foot = Player.footBox(fx, fy);
        if (foot.x < 42 || foot.x + foot.width > 1600
                || foot.y < 252 || foot.y + foot.height > 920) return true;
        if (BOTTOM_RIGHT_BOUNDARY.intersects(foot)) return true;
        for (Rectangle r : solids) if (r.intersects(foot)) return true;
        return false;
    }

    // ------------------------------------------------------------------ actions
    private void interact(Location loc) {
        if (loc == null) {
            showToast("Walk to a glowing entrance to enter");
            return;
        }
        LevelNpc npc = npcFor(loc);
        if (npc != null && Math.hypot(player.x - npc.x, player.y - npc.y) > 75) {
            showToast("Talk to " + npc.name + " before entering");
            return;
        }
        boolean locked = loc.locked && !unlockAll;
        if (npc != null) {
            openNpcDialogue(npc, loc, locked);
            return;
        }
        if (locked) {
            openModal(loc.name, Arrays.asList("This place is locked.", "Win the earlier levels to unlock it."), null, loc);
            return;
        }
        String[] desc = switch (loc.kind) {
            case HOME -> new String[]{"Rest at home."};
            case TRAINING -> new String[]{"Practice your serves and earn XP."};
            default -> new String[]{"Beat 3 opponents to clear this level."};
        };
        openModal(loc.name, Arrays.asList(desc), "Enter  (Enter)", loc);
    }

    private void openNpcDialogue(LevelNpc npc, Location loc, boolean locked) {
        npcDialogueOpen = true;
        modalOpen = true;
        modalTitle = npc.name;
        modalLines = locked
                ? Arrays.asList("This level is locked.", "Win the earlier levels to unlock it.")
                : Arrays.asList(npc.message, "Would you like to enter?");
        modalLoc = loc;
        modalConfirmable = !locked;
        npcDialoguePortrait = npc.sprite;
        keys.clear();
    }

    private void openModal(String title, List<String> lines, String confirm, Location loc) {
        npcDialogueOpen = false;
        npcDialoguePortrait = null;
        modalOpen = true;
        modalTitle = title;
        modalLines = lines;
        modalLoc = loc;
        modalConfirmable = confirm != null;
        keys.clear();
    }

    private void closeModal() {
        modalOpen = false;
        npcDialogueOpen = false;
        npcDialoguePortrait = null;
    }

    private void confirmModal() {
        if (modalConfirmable && modalLoc != null) {
            Location loc = modalLoc;
            closeModal();
            listener.onEnter(loc, state);
        } else {
            closeModal();
        }
    }

    /** Placeholder behaviour — replace with your real screens via setLocationListener(). */
    private void defaultEnter(Location loc, GameState st) {
        switch (loc.kind) {
            case HOME -> { st.restoreHp(); showToast("You rested at home."); }
            case TRAINING -> { st.addXp(25); showToast("Training complete! +25 XP"); }
            default -> showToast("Starting " + loc.name + "...");
        }
    }

    private void play() {
        interact(near);
    }

    private void showToast(String msg) {
        toast = msg;
        toastTime = 2.4;
    }

    // ------------------------------------------------------------------ painting
    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        // BILINEAR, not bicubic: everything drawn per frame is already
        // at (or near) its final size — the sprite frames are
        // pre-scaled for the player sprite, the background is
        // pre-scaled below — so bicubic only cost time, and the
        // difference at these small scales is invisible.
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        scale = Math.min(getWidth() / (double) W, getHeight() / (double) H);
        offX = (getWidth() - W * scale) / 2;
        offY = (getHeight() - H * scale) / 2;

        // Background: one bicubic scale when the size changes, then a
        // 1:1 blit every frame. It covers exactly the world rect in
        // device pixels, so it is drawn before the world transform.
        // The slow daylight pulse (a uniform warm wash, alpha 7)
        // is baked in here once, instead of filling the whole
        // screen with a translucent colour every frame — that one
        // fillRect cost ~6 ms per paint, for an effect nobody can
        // see change. The pulse's slow variation (alpha 4-10) was
        // imperceptible, so the fixed average is identical on screen.
        int bw = (int) Math.round(W * scale), bh = (int) Math.round(H * scale);
        if (backgroundScaled == null || backgroundScaled.getWidth() != bw || backgroundScaled.getHeight() != bh) {
            backgroundScaled = new BufferedImage(Math.max(1, bw), Math.max(1, bh), BufferedImage.TYPE_INT_ARGB);
            Graphics2D bg2 = backgroundScaled.createGraphics();
            bg2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            bg2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            bg2.drawImage(background, 0, 0, bw, bh, null);
            bg2.setColor(new Color(255, 244, 200, 7));
            bg2.fillRect(0, 0, bw, bh);
            bg2.dispose();
        }
        g.drawImage(backgroundScaled, (int) offX, (int) offY, null);

        g.translate(offX, offY);
        g.scale(scale, scale);
        g.setClip(0, 0, W, H);

        CityBackground.drawMovingClouds(g, time);
        CityBackground.drawBirds(g, time);
        // No fountain sparkles: the image has no fountains — the old sparkle spots
        // (674,478) / (1012,478) would rise out of the City Hall building now.

        drawEntrances(g);
        drawLevelNpcs(g);
        player.draw(g);
        CityBackground.drawLeaves(g, time);
        CityBackground.drawLight(g, time, player.x, player.y);

        if (hoverLoc != null) drawLocationHover(g, hoverLoc);
        for (Location l : locations) Hud.drawLabel(g, l, l == current, l == hoverLoc);
        Hud.drawTop(g, state);
        if (near != null && !modalOpen) drawPrompt(g);
        if (toastTime > 0) Hud.drawToast(g, toast, Math.min(1, toastTime * 2));
        if (modalOpen) {
            if (npcDialogueOpen) {
                Hud.drawNpcDialogue(g, modalTitle, modalLines, npcDialoguePortrait,
                        modalConfirmable, confirmRect, backRect);
            } else {
                Hud.drawModal(g, modalTitle, modalLines, modalConfirmable ? "Enter  (Enter)" : null,
                        confirmRect, backRect);
            }
        }
        g.dispose();
    }

    private void drawEntrances(Graphics2D g) {
        for (Location l : locations) {
            boolean isNear = l == near;
            boolean locked = l.locked && !unlockAll;
            double pulse = 0.5 + 0.5 * Math.sin(time * 4);
            Color c = locked ? new Color(0xFF6B6B) : new Color(0xFFE066);
            g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), isNear ? 150 : (int) (40 + 40 * pulse)));
            g.fill(new Ellipse2D.Double(l.entrance.x - 26, l.entrance.y - 9, 52, 18));
            g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), isNear ? 255 : 150));
            g.setStroke(new BasicStroke(2f));
            g.draw(new Ellipse2D.Double(l.entrance.x - 26, l.entrance.y - 9, 52, 18));
            g.setStroke(new BasicStroke(1f));
        }
    }

    private void drawLocationHover(Graphics2D g, Location location) {
        Rectangle block = location.block;
        int alpha = 45 + (int) (25 * (0.5 + 0.5 * Math.sin(time * 5)));
        RoundRectangle2D outline = new RoundRectangle2D.Double(
                block.x - 5, block.y - 5, block.width + 10, block.height + 10, 30, 30);
        g.setColor(new Color(112, 205, 255, alpha / 3));
        g.setStroke(new BasicStroke(12f));
        g.draw(outline);
        g.setColor(new Color(160, 225, 255, alpha));
        g.setStroke(new BasicStroke(3f));
        g.draw(outline);
        g.setStroke(new BasicStroke(1f));
    }

    private void drawLevelNpcs(Graphics2D g) {
        for (LevelNpc npc : levelNpcs) {
            int height = 56;
            int width = 36;
            g.setColor(new Color(0, 0, 0, 90));
            g.fill(new Ellipse2D.Double(npc.x - 17, npc.y - 5, 34, 10));
            Object oldInterpolation = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g.drawImage(npc.sprite, (int) npc.x - width / 2, (int) npc.y - height, width, height, null);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, oldInterpolation);
        }
    }

    private void drawPrompt(Graphics2D g) {
        // bouncing arrow above the entrance + hint bubble
        int ax = near.entrance.x, ay = near.entrance.y - 92 + (int) (Math.sin(time * 6) * 4);
        Path2D arrow = new Path2D.Double();
        arrow.moveTo(ax, ay + 14);
        arrow.lineTo(ax - 10, ay);
        arrow.lineTo(ax + 10, ay);
        arrow.closePath();
        g.setColor(Hud.GOLD);
        g.fill(arrow);
        LevelNpc npc = npcFor(near);
        String s = npc != null
                ? (near.locked && !unlockAll ? "Locked: " : "E: Talk to ") + npc.name
                : (near.locked && !unlockAll ? "Locked: " : "E: Enter ") + near.name;
        Font f = new Font("SansSerif", Font.BOLD, 17);
        g.setFont(f);
        int w = g.getFontMetrics().stringWidth(s) + 26;
        Hud.pill(g, ax - w / 2, ay - 38, w, 32, 32);
        Hud.text(g, s, ax - w / 2 + 13, ay - 16, f, Color.WHITE);
    }

    // ------------------------------------------------------------------ input
    @Override
    public void keyPressed(KeyEvent e) {
        int k = e.getKeyCode();
        if (modalOpen) {
            if (k == KeyEvent.VK_ENTER || k == KeyEvent.VK_SPACE || k == KeyEvent.VK_E) confirmModal();
            else if (k == KeyEvent.VK_ESCAPE) closeModal();
            return;
        }
        keys.add(k);
        if (k == KeyEvent.VK_J) { player.startSwing(); return; }
        if (k == KeyEvent.VK_E || k == KeyEvent.VK_ENTER || k == KeyEvent.VK_SPACE) interact(near);
        else if (k == KeyEvent.VK_P) play();
        else if (k == KeyEvent.VK_L) {
            unlockAll = !unlockAll;
            showToast(unlockAll ? "All locations unlocked (debug)" : "Locations locked again");
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        keys.remove(e.getKeyCode());
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    private Point2D.Double toMap(MouseEvent e) {
        return new Point2D.Double((e.getX() - offX) / scale, (e.getY() - offY) / scale);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        requestFocusInWindow();
        Point2D.Double p = toMap(e);
        if (modalOpen) {
            if (modalConfirmable && confirmRect.contains(p)) confirmModal();
            else if (backRect.contains(p)) { if (modalConfirmable) closeModal(); else confirmModal(); }
            return;
        }
        for (Location l : locations) {
            if (l.labelRect.contains(p) || l.block.contains(p)) {
                showToast("Walk to the entrance to play " + l.name);
                return;
            }
        }
        // Clicked empty road / grass: swing the paddle, instead of doing nothing.
        player.startSwing();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        Point2D.Double p = toMap(e);
        hoverLoc = null;
        for (Location l : locations) if (l.labelRect.contains(p) || l.block.contains(p)) hoverLoc = l;
        boolean clickable = hoverLoc != null;
        if (modalOpen) clickable = confirmRect.contains(p) || backRect.contains(p);
        setCursor(clickable ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
    }

    @Override public void mouseDragged(MouseEvent e) { mouseMoved(e); }
    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {
        hoverLoc = null;
        setCursor(Cursor.getDefaultCursor());
        repaint();
    }

    private static final class LevelNpc {
        final Location location;
        final String name;
        final String message;
        final BufferedImage sprite;
        final double x;
        final double y;

        LevelNpc(Location location, String name, String message, Color hair, Color shirt, Color trim) {
            this.location = location;
            this.name = name;
            this.message = message;
            this.sprite = createPixelNpc(hair, shirt, trim);
            this.x = location.entrance.x - 34;
            this.y = location.entrance.y;
        }

        private static BufferedImage createPixelNpc(Color hair, Color shirt, Color trim) {
            BufferedImage image = new BufferedImage(20, 32, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = image.createGraphics();
            Color outline = new Color(0x25212A);
            Color skin = new Color(0xE8B68C);
            Color pants = new Color(0x33425B);
            Color shoes = new Color(0xE8E7DC);

            g.setColor(outline);
            g.fillRect(5, 1, 10, 2);
            g.fillRect(3, 3, 14, 7);
            g.setColor(skin);
            g.fillRect(6, 4, 8, 7);
            g.setColor(hair);
            g.fillRect(5, 3, 10, 2);
            g.fillRect(3, 4, 3, 5);
            g.fillRect(14, 4, 3, 5);
            g.setColor(outline);
            g.fillRect(7, 8, 2, 1);
            g.fillRect(12, 8, 2, 1);

            g.setColor(outline);
            g.fillRect(4, 11, 12, 10);
            g.fillRect(2, 12, 3, 8);
            g.fillRect(15, 12, 3, 8);
            g.setColor(shirt);
            g.fillRect(5, 12, 10, 8);
            g.setColor(trim);
            g.fillRect(9, 12, 2, 7);
            g.fillRect(3, 17, 2, 2);
            g.fillRect(15, 17, 2, 2);

            g.setColor(outline);
            g.fillRect(5, 20, 10, 8);
            g.setColor(pants);
            g.fillRect(6, 21, 3, 6);
            g.fillRect(11, 21, 3, 6);
            g.setColor(outline);
            g.fillRect(4, 27, 5, 3);
            g.fillRect(11, 27, 5, 3);
            g.setColor(shoes);
            g.fillRect(5, 27, 3, 2);
            g.fillRect(12, 27, 3, 2);
            g.dispose();
            return image;
        }
    }

    // small alias so the file only needs java.awt.geom for one type
    private static class Point2D {
        static class Double extends java.awt.geom.Point2D.Double {
            Double(double x, double y) { super(x, y); }
        }
    }
}
