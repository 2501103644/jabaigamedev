import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;

/**
 * Pickle Smash title screen, using the supplied artwork as the background.
 *
 * <p>The image (assets/home_menu_bg.png, 1672 x 941, ~16:9) already contains
 * the player, the PICKLE SMASH logo, the court and the five wooden buttons,
 * so this panel no longer draws any of those in code.</p>
 *
 * <p>Hover effect (fixed twice): the first version was only a thin, faint
 * gold outline plus a 26-alpha white fill, drawn as a straight rectangle.
 * The second version lifted and brightened the button, but still clipped
 * and outlined it with a rounded rectangle, so it looked like a square
 * box around the button. The painted buttons are not rectangles at all:
 * measured from the artwork, each one is a slanted wooden plank, rising
 * about 10-12 px to the right over its 304 px width (top ~297 px at the
 * left, ~290 px at the right for PLAY), with chamfered / pointed ends -
 * the left and right tips are only at mid-height. So the hover now uses
 * {@link #plankShape(Rectangle)}: a slanted hexagon that follows that
 * plank outline, for the clip, the glow and the gold border. No rectangle
 * is drawn at all.</p>
 * <ul>
 *   <li>it grows ~8% and lifts ~8 px, like the old code-drawn buttons did;</li>
 *   <li>the lifted copy is brightened ~30% and warmed, so the wood and
 *       the white label clearly pop against the rest of the screen;</li>
 *   <li>a soft golden glow and a bright gold border follow the plank
 *       outline itself, plus a white sheen across the top;</li>
 *   <li>the grow/shrink is animated (hoverAnim 0 -&gt; 1), so the button
 *       visibly pops in instead of just appearing outlined.</li>
 * </ul>
 *
 * <p>Button hit-areas were measured from the artwork itself (0-1000
 * coordinates, converted to the 1280 x 720 design space) and then inset a
 * few pixels so neighbouring buttons cannot overlap:</p>
 * <pre>
 *   PLAY     [668,385,922,512] -&gt; (866, 282, 304, 80)
 *   AVATAR   [668,505,922,620] -&gt; (866, 368, 304, 72)
 *   SETTINGS [668,605,922,722] -&gt; (866, 442, 304, 70)
 *   CREDITS  [668,708,922,820] -&gt; (866, 516, 304, 66)
 *   EXIT     [668,798,922,925] -&gt; (866, 586, 304, 72)
 * </pre>
 * <p>Mouse positions are converted back to design coordinates with
 * {@link #toDesignPoint(MouseEvent)}, so the hit-areas stay aligned with
 * the painted buttons at any window size.</p>
 */
public class HomeMenuPanel extends JPanel implements ActionListener, MouseListener, MouseMotionListener {
    private static final int W = 1280;
    private static final int H = 720;
    private static final String[] LABELS = {"PLAY", "AVATAR", "SETTINGS", "CREDITS", "EXIT"};

    // Invisible click areas over the wooden buttons painted in the artwork.
    private static final Rectangle[] BUTTONS = {
            new Rectangle(866, 282, 304, 80),  // PLAY
            new Rectangle(866, 368, 304, 72),  // AVATAR
            new Rectangle(866, 442, 304, 70),  // SETTINGS
            new Rectangle(866, 516, 304, 66),  // CREDITS
            new Rectangle(866, 586, 304, 72),  // EXIT
    };

    /** Painted buttons rise ~10 px to the right over 304 px, about 1.9 degrees.
     *  Rotating the hovered button clockwise by this much levels it. */
    private static final double TILT_RADIANS = Math.toRadians(1.9);

    private final GameState state;
    private final ScreenNavigator navigator;

    private final Timer timer = new Timer(33, this);
    private final BufferedImage background;
    private long previousNanos = System.nanoTime();
    private double time;
    private int hoverIndex = -1;
    private int pressedIndex = -1;
    /** 0 = no hover, 1 = fully popped. Animated in actionPerformed(). */
    private double hoverAnim;
    /** Brightened copy of the hovered button's artwork, cached per button. */
    private BufferedImage hoverImage;
    private int hoverImageIndex = -1;

    /** Kept for old callers / tests: a fresh state, no navigation. */
    public HomeMenuPanel() {
        this(new GameState(), null);
    }

    /** The real constructor: shared state + the navigator that opens the new screens. */
    public HomeMenuPanel(GameState state, ScreenNavigator navigator) {
        this.state = state;
        this.navigator = navigator;
        setPreferredSize(new Dimension(W, H));
        setMinimumSize(new Dimension(960, 540));
        setFocusable(true);
        addMouseListener(this);
        addMouseMotionListener(this);
        background = Assets.get("home_menu_bg");
        timer.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        long now = System.nanoTime();
        double dt = Math.min(0.05, (now - previousNanos) / 1_000_000_000.0);
        previousNanos = now;
        time += dt;
        // Animate the hover pop in / out smoothly (~0.15 s).
        double target = hoverIndex >= 0 ? 1.0 : 0.0;
        hoverAnim += (target - hoverAnim) * Math.min(1.0, dt * 12.0);
        if (Math.abs(target - hoverAnim) < 0.01) hoverAnim = target;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        double scale = Math.min(getWidth() / (double) W, getHeight() / (double) H);
        double offsetX = (getWidth() - W * scale) / 2.0;
        double offsetY = (getHeight() - H * scale) / 2.0;
        g.translate(offsetX, offsetY);
        g.scale(scale, scale);
        g.setClip(0, 0, W, H);

        if (background != null && background.getWidth() > 100) {
            g.drawImage(background, 0, 0, W, H, null);
        } else {
            // Fallback if home_menu_bg.png is missing from assets/.
            g.setPaint(new GradientPaint(0, 0, new Color(0x1687E8), 0, H, new Color(0x073A86)));
            g.fillRect(0, 0, W, H);
            g.setFont(new Font("SansSerif", Font.BOLD, 28));
            g.setColor(Color.WHITE);
            g.drawString("Missing asset: home_menu_bg.png (put it in PickleSmashMap/assets/)", 60, H / 2);
        }

        drawHoverEffect(g);
        drawSparkles(g);
        g.dispose();
    }

    /**
     * The outline of one painted wooden button, as a slanted hexagon.
     * Measured from the artwork: the plank rises ~10 px to the right,
     * its top/bottom edges are slanted, and its left/right ends are
     * chamfered to a point at mid-height - it is not a rectangle.
     * The same shape is used for the clip, the glow and the gold border,
     * so the hover follows the button instead of looking like a square.
     */
    private static Path2D plankShape(Rectangle r) {
        double x = r.x, y = r.y, w = r.width, h = r.height;
        Path2D p = new Path2D.Double();
        p.moveTo(x + 22, y + 13);          // top-left, inset + slanted top
        p.lineTo(x + w - 16, y + 2);       // top-right
        p.lineTo(x + w, y + h / 2 - 5);    // right tip
        p.lineTo(x + w - 16, y + h - 11);  // bottom-right
        p.lineTo(x + 22, y + h - 1);       // bottom-left
        p.lineTo(x, y + h / 2 + 4);        // left tip
        p.closePath();
        return p;
    }

    /**
     * Lifts, brightens, glows and straightens the painted button under
     * the mouse. The button is re-drawn from the background artwork
     * itself, so the whole plank - wood, icon and label - pops together.
     *
     * <p>Straightening: the painted buttons are slanted, rising to the
     * right. The hover is drawn in a copy of the graphics context that
     * is rotated clockwise by {@link #TILT_RADIANS} around the button's
     * centre (scaled by hoverAnim, so it straightens smoothly as it
     * pops in). The brightened button, its plank clip, its glow and its
     * gold border are all drawn inside that rotated context, using the
     * same plank shape and the same transform - so the outline and the
     * button straighten together and always match. The un-hovered
     * buttons stay slanted, exactly as painted.</p>
     */
    private void drawHoverEffect(Graphics2D g) {
        if (hoverIndex < 0 || hoverIndex >= BUTTONS.length || hoverAnim <= 0.01) return;
        if (background == null || background.getWidth() <= 100) return;
        Rectangle r = BUTTONS[hoverIndex];

        double pop = 1.0 + 0.14 * hoverAnim;
        if (pressedIndex == hoverIndex) pop -= 0.03; // pressed: sink back a little
        // Lift is kept smaller than the pop expansion, so the straightened
        // button stays big enough to fully cover the slanted original
        // underneath - otherwise the original peeks out at the bottom.
        double lift = 4.0 * hoverAnim;
        double cx = r.x + r.width / 2.0;
        double cy = r.y + r.height / 2.0;

        // Draw the whole hover in a rotated + scaled copy, centred on the
        // button and lifted up. Everything below uses the original rect.
        Graphics2D hg = (Graphics2D) g.create();
        hg.translate(cx, cy - lift);
        hg.rotate(TILT_RADIANS * hoverAnim);
        hg.scale(pop, pop);
        hg.translate(-cx, -cy);
        Path2D plank = plankShape(r);

        // 1) Soft golden glow that follows the plank outline (thick,
        // translucent strokes of the same shape, no filled rectangle).
        hg.setStroke(new BasicStroke(14f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        hg.setColor(new Color(255, 200, 60, (int) (55 * hoverAnim)));
        hg.draw(plank);
        hg.setStroke(new BasicStroke(8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        hg.setColor(new Color(255, 210, 80, (int) (95 * hoverAnim)));
        hg.draw(plank);

        // 2) The button itself, brightened, drawn enlarged over the original,
        // clipped to the plank shape so no sky/court shows in the corners.
        BufferedImage bright = getBrightButtonImage(hoverIndex);
        if (bright != null) {
            Shape clipBefore = hg.getClip();
            hg.setClip(plank);
            hg.drawImage(bright, r.x, r.y, r.width, r.height, null);
            hg.setClip(clipBefore);
        }

        // 3) Bright gold border that traces the plank outline.
        hg.setColor(new Color(255, 214, 90, (int) (240 * hoverAnim)));
        hg.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        hg.draw(plank);
        hg.setStroke(new BasicStroke(1f));

        // 4) White sheen across the top half of the lifted button.
        Shape oldClip = hg.getClip();
        hg.setClip(plank);
        hg.setPaint(new GradientPaint(r.x, r.y, new Color(255, 255, 255, (int) (95 * hoverAnim)),
                r.x, r.y + r.height / 2f, new Color(255, 255, 255, 0)));
        hg.fillRect(r.x, r.y, r.width, r.height / 2 + 4);
        hg.setClip(oldClip);
        hg.dispose();
    }

    /** Brightened copy of one painted button, cut out of the background art. */
    private BufferedImage getBrightButtonImage(int index) {
        if (hoverImage != null && hoverImageIndex == index) return hoverImage;
        Rectangle r = BUTTONS[index];
        int sx = (int) Math.round(r.x * (double) background.getWidth() / W);
        int sy = (int) Math.round(r.y * (double) background.getHeight() / H);
        int sw = (int) Math.round(r.width * (double) background.getWidth() / W);
        int sh = (int) Math.round(r.height * (double) background.getHeight() / H);
        sx = Math.max(0, Math.min(sx, background.getWidth() - 1));
        sy = Math.max(0, Math.min(sy, background.getHeight() - 1));
        sw = Math.max(1, Math.min(sw, background.getWidth() - sx));
        sh = Math.max(1, Math.min(sh, background.getHeight() - sy));
        BufferedImage cut = background.getSubimage(sx, sy, sw, sh);
        // Warm brighten: +~30% brightness, slightly more red/green than blue.
        RescaleOp brighten = new RescaleOp(
                new float[]{1.32f, 1.26f, 1.08f, 1.0f},
                new float[]{22f, 16f, 0f, 0f}, null);
        hoverImage = brighten.filter(cut, null);
        hoverImageIndex = index;
        return hoverImage;
    }

    /** A few tiny twinkling sparkles on top of the static artwork. */
    private void drawSparkles(Graphics2D g) {
        for (int i = 0; i < 16; i++) {
            double a = time * 0.55 + i * 0.82;
            int x = (int) (640 + Math.cos(a) * (280 + (i % 4) * 28));
            int y = (int) (345 + Math.sin(a * 1.35) * (120 + (i % 3) * 18));
            int alpha = 50 + (int) (60 * (0.5 + 0.5 * Math.sin(time * 2 + i)));
            g.setColor(new Color(255, 246, 190, alpha));
            g.fillOval(x, y, 4 + i % 3, 4 + i % 3);
        }
    }

    private void performAction(int index) {
        // No more JOptionPane placeholders: AVATAR / SETTINGS / CREDITS are
        // real screens now, opened through the navigator (see ScreenManager).
        // The hover effect above is untouched — it is exactly as uploaded.
        if (navigator == null) {
            if (index == 4) System.exit(0);
            if (index == 0) openMap();
            return;
        }
        switch (index) {
            case 0 -> openMap();
            case 1 -> navigator.showAvatar();
            case 2 -> navigator.showSettings();
            case 3 -> navigator.showCredits();
            case 4 -> System.exit(0);
            default -> { }
        }
    }

    private void openMap() {
        if (navigator != null) { navigator.showMap(); return; }
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window instanceof JFrame frame) {
            GamePanel map = new GamePanel(state);
            frame.setContentPane(map);
            frame.setTitle("Pickle Smash - City Map");
            frame.pack();
            frame.setLocationRelativeTo(null);
            map.requestFocusInWindow();
        }
    }

    private Point toDesignPoint(MouseEvent e) {
        double scale = Math.min(getWidth() / (double) W, getHeight() / (double) H);
        double ox = (getWidth() - W * scale) / 2.0;
        double oy = (getHeight() - H * scale) / 2.0;
        return new Point((int) ((e.getX() - ox) / scale), (int) ((e.getY() - oy) / scale));
    }

    @Override public void mouseMoved(MouseEvent e) {
        Point p = toDesignPoint(e);
        int next = -1;
        for (int i = 0; i < BUTTONS.length; i++) if (BUTTONS[i].contains(p)) next = i;
        if (next != hoverIndex) {
            hoverIndex = next;
            hoverImage = null;
            hoverImageIndex = -1;
        }
        setCursor(hoverIndex >= 0 ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
        repaint();
    }
    @Override public void mousePressed(MouseEvent e) {
        Point p = toDesignPoint(e);
        for (int i = 0; i < BUTTONS.length; i++) if (BUTTONS[i].contains(p)) {
            pressedIndex = i;
            repaint();
            performAction(i);
            return;
        }
    }
    @Override public void mouseExited(MouseEvent e) {
        hoverIndex = -1;
        pressedIndex = -1;
        hoverImage = null;
        hoverImageIndex = -1;
        repaint();
    }
    @Override public void mouseDragged(MouseEvent e) { mouseMoved(e); }
    @Override public void mouseClicked(MouseEvent e) { }
    @Override public void mouseReleased(MouseEvent e) { pressedIndex = -1; repaint(); }
    @Override public void mouseEntered(MouseEvent e) { }
}
