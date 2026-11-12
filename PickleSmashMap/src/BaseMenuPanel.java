import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

/**
 * OOP lesson — INHERITANCE, the other way round.
 * AvatarPanel, SettingsPanel and CreditsPanel all need the same things:
 * the court background, a title banner, a wooden BACK button, a toast
 * message, a timer, and mouse-to-design-coordinate conversion.
 * Instead of copying that code into three files, it lives here once.
 * Each child only implements drawContent() and onContentClick().
 * This is the TEMPLATE METHOD pattern: the parent controls the flow
 * (background -> content -> toast), the child fills in the middle.
 */
public abstract class BaseMenuPanel extends JPanel implements ActionListener, MouseListener, MouseMotionListener {
    public static final int W = 1280;
    public static final int H = 720;

    protected static final Color NAVY = new Color(0x0A2A6B);
    protected static final Color NAVY_PANEL = new Color(0x0A2A6B, true);
    protected static final Color GOLD = new Color(0xFFD65A);
    protected static final Color WOOD_TOP = new Color(0xE89A45);
    protected static final Color WOOD_BOTTOM = new Color(0xB96625);

    /** BACK, bottom-left on every menu screen — same place, same size, every time. */
    protected static final Rectangle BACK_RECT = new Rectangle(24, 644, 210, 58);

    protected final GameState state;
    protected final ScreenNavigator navigator;
    protected final BufferedImage background;
    protected final Timer timer = new Timer(33, this);
    protected double time;
    protected String toast;
    protected double toastTime;
    protected boolean hoverBack;

    protected BaseMenuPanel(GameState state, ScreenNavigator navigator) {
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

    /** The child draws its own screen here. The parent has already drawn the background. */
    protected abstract void drawContent(Graphics2D g);

    /** The child handles clicks that are NOT the BACK button. Return true if it handled the click. */
    protected abstract boolean onContentClick(Point p);

    /** The child handles a mouse drag (sliders use this). Default: do nothing. */
    protected void onContentDrag(Point p) {}

    @Override
    public void actionPerformed(ActionEvent e) {
        time += 0.033;
        if (toastTime > 0) toastTime -= 0.033;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        double scale = Math.min(getWidth() / (double) W, getHeight() / (double) H);
        double ox = (getWidth() - W * scale) / 2.0;
        double oy = (getHeight() - H * scale) / 2.0;
        g.translate(ox, oy);
        g.scale(scale, scale);
        g.setClip(0, 0, W, H);

        if (background != null && background.getWidth() > 100) {
            g.drawImage(background, 0, 0, W, H, null);
            // Dim the busy artwork a little so the panel text stays readable.
            g.setColor(new Color(6, 30, 80, 70));
            g.fillRect(0, 0, W, H);
        } else {
            g.setPaint(new GradientPaint(0, 0, new Color(0x1687E8), 0, H, new Color(0x073A86)));
            g.fillRect(0, 0, W, H);
        }

        drawContent(g);
        drawWoodenButton(g, BACK_RECT, "◀  BACK", hoverBack);
        drawToast(g);
        g.dispose();
    }

    protected void showToast(String msg) {
        toast = msg;
        toastTime = 2.4;
    }

    protected void drawToast(Graphics2D g) {
        if (toast == null || toastTime <= 0) return;
        Font f = new Font("SansSerif", Font.BOLD, 20);
        g.setFont(f);
        int w = g.getFontMetrics().stringWidth(toast) + 36;
        int x = (W - w) / 2;
        int y = H - 88;
        g.setColor(new Color(0, 0, 0, (int) (200 * Math.min(1, toastTime))));
        g.fillRoundRect(x, y, w, 40, 20, 20);
        g.setColor(GOLD);
        g.setStroke(new BasicStroke(2f));
        g.drawRoundRect(x, y, w, 40, 20, 20);
        g.setColor(Color.WHITE);
        g.drawString(toast, x + 18, y + 27);
    }

    /** Title banner, like the AVATAR / SETTINGS / CREDITS banner in the layouts. */
    protected void drawTitle(Graphics2D g, String title) {
        int w = 560, x = (W - w) / 2, y = 18;
        // Drop shadow so the banner lifts off the background.
        g.setColor(new Color(0, 0, 0, 90));
        g.fillRoundRect(x + 4, y + 5, w, 62, 18, 18);
        g.setColor(new Color(6, 32, 86, 235));
        g.fillRoundRect(x, y, w, 62, 18, 18);
        g.setColor(GOLD);
        g.setStroke(new BasicStroke(3f));
        g.drawRoundRect(x, y, w, 62, 18, 18);
        g.setFont(new Font("SansSerif", Font.BOLD, 42));
        FontMetrics fm = g.getFontMetrics();
        int tx = x + (w - fm.stringWidth(title)) / 2;
        // Text shadow, then the white title, then a gold pickleball dot on each side.
        g.setColor(new Color(0, 0, 0, 110));
        g.drawString(title, tx + 2, y + 48);
        g.setColor(Color.WHITE);
        g.drawString(title, tx, y + 46);
        int cy = y + 31;
        g.setColor(GOLD);
        g.fillOval(x + 28, cy - 9, 18, 18);
        g.fillOval(x + w - 46, cy - 9, 18, 18);
        g.setColor(new Color(0x8A5A00));
        g.fillOval(x + 33, cy - 4, 3, 3);
        g.fillOval(x + 37, cy + 2, 3, 3);
        g.fillOval(x + w - 41, cy - 4, 3, 3);
        g.fillOval(x + w - 37, cy + 2, 3, 3);
    }

    protected void drawPanel(Graphics2D g, Rectangle r) {
        g.setColor(new Color(0, 0, 0, 90));
        g.fillRoundRect(r.x + 6, r.y + 8, r.width, r.height, 22, 22);
        g.setPaint(new GradientPaint(r.x, r.y, new Color(12, 52, 120, 245),
                r.x, r.y + r.height, new Color(6, 28, 78, 245)));
        g.fillRoundRect(r.x, r.y, r.width, r.height, 22, 22);
        g.setColor(new Color(255, 214, 90, 200));
        g.setStroke(new BasicStroke(2.5f));
        g.drawRoundRect(r.x, r.y, r.width, r.height, 22, 22);
    }

    protected void drawWoodenButton(Graphics2D g, Rectangle r, String label, boolean hover) {
        // Hover lifts the button 2px and glows; the shadow stays on the ground.
        int lift = hover ? -2 : 0;
        g.setColor(new Color(0, 0, 0, 100));
        g.fillRoundRect(r.x + 3, r.y + 5, r.width, r.height, 16, 16);
        if (hover) {
            g.setColor(new Color(255, 200, 60, 110));
            g.setStroke(new BasicStroke(10f));
            g.drawRoundRect(r.x - 3, r.y - 5, r.width + 6, r.height + 6, 18, 18);
        }
        g.setPaint(new GradientPaint(r.x, r.y + lift, hover ? new Color(0xF2B45E) : WOOD_TOP,
                r.x, r.y + r.height + lift, WOOD_BOTTOM));
        g.fillRoundRect(r.x, r.y + lift, r.width, r.height, 16, 16);
        g.setColor(hover ? new Color(0xFFE082) : GOLD);
        g.setStroke(new BasicStroke(hover ? 3.5f : 2.5f));
        g.drawRoundRect(r.x, r.y + lift, r.width, r.height, 16, 16);
        g.setFont(new Font("SansSerif", Font.BOLD, 26));
        FontMetrics fm = g.getFontMetrics();
        g.setColor(new Color(0, 0, 0, 90));
        g.drawString(label, r.x + (r.width - fm.stringWidth(label)) / 2 + 1, r.y + lift + (r.height + fm.getAscent() - fm.getDescent()) / 2 + 1);
        g.setColor(Color.WHITE);
        g.drawString(label, r.x + (r.width - fm.stringWidth(label)) / 2, r.y + lift + (r.height + fm.getAscent() - fm.getDescent()) / 2);
    }

    protected void drawCoins(Graphics2D g, int x, int y) {
        g.setColor(new Color(6, 32, 86, 235));
        g.fillRoundRect(x, y, 210, 52, 16, 16);
        g.setColor(GOLD);
        g.setStroke(new BasicStroke(2f));
        g.drawRoundRect(x, y, 210, 52, 16, 16);
        g.setColor(new Color(0xFFD65A));
        g.fillOval(x + 14, y + 11, 30, 30);
        g.setColor(new Color(0x8A5A00));
        g.setFont(new Font("SansSerif", Font.BOLD, 20));
        g.drawString("C", x + 23, y + 33);
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 24));
        g.drawString("Coins  " + state.getCoins(), x + 54, y + 34);
    }

    /** Draw an image fitted inside a box, keeping its aspect ratio, centred. */
    protected void drawFitted(Graphics2D g, BufferedImage img, Rectangle box) {
        if (img == null) return;
        double s = Math.min(box.width / (double) img.getWidth(), box.height / (double) img.getHeight());
        int w = (int) (img.getWidth() * s);
        int h = (int) (img.getHeight() * s);
        g.drawImage(img, box.x + (box.width - w) / 2, box.y + (box.height - h) / 2, w, h, null);
    }

    protected Point toDesignPoint(MouseEvent e) {
        double scale = Math.min(getWidth() / (double) W, getHeight() / (double) H);
        double ox = (getWidth() - W * scale) / 2.0;
        double oy = (getHeight() - H * scale) / 2.0;
        return new Point((int) ((e.getX() - ox) / scale), (int) ((e.getY() - oy) / scale));
    }

    @Override
    public void mousePressed(MouseEvent e) {
        Point p = toDesignPoint(e);
        if (BACK_RECT.contains(p)) {
            if (navigator != null) navigator.goBack();
            return;
        }
        onContentClick(p);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        Point p = toDesignPoint(e);
        hoverBack = BACK_RECT.contains(p);
        setCursor(hoverBack ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
        repaint();
    }

    @Override public void mouseDragged(MouseEvent e) { onContentDrag(toDesignPoint(e)); }
    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) { hoverBack = false; repaint(); }
}
