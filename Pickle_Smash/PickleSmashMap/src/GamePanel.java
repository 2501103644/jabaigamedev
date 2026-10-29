import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The city level-select map. Walk the player with WASD / arrow keys, press E / Enter / Space at a
 * glowing entrance (or click a label to fast-travel), P or the Play button starts the current location.
 */
public class GamePanel extends JPanel implements ActionListener, KeyListener, MouseListener, MouseMotionListener, Player.World {
    public static final int W = 1642, H = 958;

    /** Implement this to start your real game screens. */
    public interface LocationListener {
        void onEnter(Location loc, GameState state);
    }

    private final javax.swing.Timer timer = new javax.swing.Timer(16, this);
    private long lastNanos = System.nanoTime();
    private double time;

    private final GameState state = new GameState();
    private final List<Location> locations = new ArrayList<>();
    private final List<Car> cars = new ArrayList<>();
    private final List<Rectangle> solids = new ArrayList<>();
    private final Set<Integer> keys = new HashSet<>();
    private final Player player;
    private BufferedImage background;

    private Location current;
    private Location near;
    private Location hoverLoc;

    private String toast;
    private double toastTime;

    // modal dialog state
    private boolean modalOpen;
    private String modalTitle;
    private List<String> modalLines = new ArrayList<>();
    private Location modalLoc;
    private boolean modalConfirmable;
    private final Rectangle confirmRect = new Rectangle(), backRect = new Rectangle();
    private int activeTab;
    private boolean hoverPlay;

    private double scale = 1, offX, offY;
    private boolean unlockAll;
    private LocationListener listener = this::defaultEnter;

    public GamePanel() {
        setPreferredSize(new Dimension(1280, 748));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);

        buildLocations();
        buildCars();
        for (Location l : locations) solids.add(l.block);
        solids.add(CityBackground.WATER);

        current = find("Home");
        player = new Player(current.entrance.x, current.entrance.y + 6);
        background = CityBackground.render(locations);
        timer.start();
    }

    public void setLocationListener(LocationListener l) {
        this.listener = l;
    }

    // ------------------------------------------------------------------ setup
    private void buildLocations() {
        // name, kind, block (x,y,w,h), sprite, pin icon, entrance point on the street, locked?
        locations.add(new Location("City Level", Location.Kind.LEVEL, new Rectangle(48, 150, 338, 200),
                "city_hall", "pin_blue", new Point(217, 376), true));
        locations.add(new Location("Provincial Level", Location.Kind.LEVEL, new Rectangle(466, 150, 350, 200),
                "provincial_hall", "pin_blue", new Point(641, 376), true));
        locations.add(new Location("Regional Level", Location.Kind.LEVEL, new Rectangle(880, 150, 282, 200),
                "regional_hq", "pin_blue", new Point(1021, 376), true));
        locations.add(new Location("National Level", Location.Kind.LEVEL, new Rectangle(1235, 150, 360, 200),
                "national_hall", "pin_blue", new Point(1415, 376), true));
        locations.add(new Location("Barangay Level", Location.Kind.LEVEL, new Rectangle(50, 400, 440, 200),
                "barangay_hall", "pin_blue", new Point(270, 626), true));
        locations.add(new Location("Two Player Mode", Location.Kind.TWO_PLAYER, new Rectangle(558, 400, 548, 205),
                null, "pin_game", new Point(832, 628), false));
        locations.add(new Location("International Level", Location.Kind.LEVEL, new Rectangle(1235, 400, 360, 200),
                "sports_gym", "pin_blue", new Point(1415, 626), true));
        locations.add(new Location("Training", Location.Kind.TRAINING, new Rectangle(50, 655, 365, 200),
                "training", "pin_blue", new Point(232, 630), false));
        locations.add(new Location("Home", Location.Kind.HOME, new Rectangle(478, 655, 312, 200),
                "home", "pin_home", new Point(634, 630), false));
        locations.add(new Location("Store", Location.Kind.STORE, new Rectangle(845, 655, 350, 200),
                "store", "pin_store", new Point(1020, 630), true));
    }

    private void buildCars() {
        cars.add(new Car("car_blue", 400, 366, 110, false, 44, 1598));
        cars.add(new Car("car_green", 1000, 616, 100, false, 44, 1598));
        cars.add(new Car("car_taxi", 900, 640, -95, false, 44, 1598));
        cars.add(new Car("car_red", 1198, 200, 85, true, 170, 640));
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

        if (!modalOpen) {
            player.update(dt, keyDown(KeyEvent.VK_UP, KeyEvent.VK_W), keyDown(KeyEvent.VK_DOWN, KeyEvent.VK_S),
                    keyDown(KeyEvent.VK_LEFT, KeyEvent.VK_A), keyDown(KeyEvent.VK_RIGHT, KeyEvent.VK_D), this);
        } else {
            player.moving = false;
        }
        for (Car c : cars) c.update(dt, player.x, player.y);

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

    /** Walkable test for the player's feet: inside the map, not inside a block or the water. */
    @Override
    public boolean isBlocked(double fx, double fy) {
        if (fx < 42 || fx > 1600 || fy < 172 || fy > 848) return true;
        Rectangle foot = Player.footBox(fx, fy);
        for (Rectangle r : solids) if (r.intersects(foot)) return true;
        return false;
    }

    // ------------------------------------------------------------------ actions
    private void interact(Location loc) {
        if (loc == null) {
            showToast("Walk to a glowing entrance to enter");
            return;
        }
        boolean locked = loc.locked && !unlockAll;
        if (locked) {
            openModal(loc.name, Arrays.asList("This place is locked.", "Win the earlier levels to unlock it."), null, loc);
            return;
        }
        String[] desc = switch (loc.kind) {
            case HOME -> new String[]{"Rest at home to restore your HP."};
            case TRAINING -> new String[]{"Practice your serves and earn XP."};
            case STORE -> new String[]{"Spend coins on paddles and outfits."};
            case TWO_PLAYER -> new String[]{"Play a local match with a friend."};
            default -> new String[]{"Beat 3 opponents to clear this level."};
        };
        openModal(loc.name, Arrays.asList(desc), "Enter  (Enter)", loc);
    }

    private void openModal(String title, List<String> lines, String confirm, Location loc) {
        modalOpen = true;
        modalTitle = title;
        modalLines = lines;
        modalLoc = loc;
        modalConfirmable = confirm != null;
        keys.clear();
    }

    private void closeModal() {
        modalOpen = false;
        activeTab = 0;
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
            case HOME -> { st.hp = st.maxHp; showToast("You rested at home. HP restored!"); }
            case TRAINING -> { st.addXp(25); showToast("Training complete! +25 XP"); }
            case STORE -> showToast("Welcome to the Store!");
            case TWO_PLAYER -> showToast("Starting Two Player Mode...");
            default -> showToast("Starting " + loc.name + "...");
        }
    }

    private void play() {
        interact(current);
    }

    private void showToast(String msg) {
        toast = msg;
        toastTime = 2.4;
    }

    private void fastTravel(Location loc) {
        player.x = loc.entrance.x;
        player.y = loc.entrance.y + 6;
        player.dir = Player.Dir.DOWN;
        current = loc;
        showToast("Traveled to " + loc.name);
    }

    // ------------------------------------------------------------------ painting
    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        scale = Math.min(getWidth() / (double) W, getHeight() / (double) H);
        offX = (getWidth() - W * scale) / 2;
        offY = (getHeight() - H * scale) / 2;
        g.translate(offX, offY);
        g.scale(scale, scale);
        g.setClip(0, 0, W, H);

        g.drawImage(background, 0, 0, null);
        CityBackground.drawWater(g, time);

        drawEntrances(g);
        for (Car c : cars) c.draw(g);
        player.draw(g);

        for (Location l : locations) Hud.drawLabel(g, l, l == current, l == hoverLoc);
        Hud.drawTop(g, state, current);
        Hud.drawBottom(g, state, activeTab, time);

        if (near != null && !modalOpen) drawPrompt(g);
        if (toastTime > 0) Hud.drawToast(g, toast, Math.min(1, toastTime * 2));
        if (modalOpen) Hud.drawModal(g, modalTitle, modalLines, modalConfirmable ? "Enter  (Enter)" : null, confirmRect, backRect);
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
        String s = (near.locked && !unlockAll ? "Locked: " : "E: Enter ") + near.name;
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
        if (Hud.PLAY.contains(p)) { play(); return; }
        if (Hud.GEAR.contains(p)) { activeTab = 4; openModal("Settings", Arrays.asList("Controls: WASD / arrows to walk", "E / Enter / Space to enter, P to play", "L toggles unlock-all (debug)"), null, null); return; }
        String[] tabs = {"Map", "Inventory", "Quests", "Achievements", "Settings"};
        for (int i = 0; i < 5; i++) {
            if (Hud.NAV_RECTS[i].contains(p)) {
                if (i == 0) { activeTab = 0; return; }
                activeTab = i;
                openModal(tabs[i], Arrays.asList("Coming soon!", "Hook this screen up in GamePanel."), null, null);
                return;
            }
        }
        for (Location l : locations) {
            if (l.labelRect.contains(p) || l.block.contains(p)) { fastTravel(l); return; }
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        Point2D.Double p = toMap(e);
        hoverLoc = null;
        for (Location l : locations) if (l.labelRect.contains(p) || l.block.contains(p)) hoverLoc = l;
        boolean clickable = hoverLoc != null || Hud.PLAY.contains(p) || Hud.GEAR.contains(p);
        for (Rectangle r : Hud.NAV_RECTS) clickable |= r.contains(p);
        if (modalOpen) clickable = confirmRect.contains(p) || backRect.contains(p);
        setCursor(clickable ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
    }

    @Override public void mouseDragged(MouseEvent e) { mouseMoved(e); }
    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}

    // small alias so the file only needs java.awt.geom for one type
    private static class Point2D {
        static class Double extends java.awt.geom.Point2D.Double {
            Double(double x, double y) { super(x, y); }
        }
    }
}
