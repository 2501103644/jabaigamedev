import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.List;

/** Draws the heads-up display: HP / coins / stars, location panel, player card, nav bar, Play button. */
public final class Hud {
    static final Color NAVY_T = new Color(0x14264F), NAVY_B = new Color(0x0A1630);
    static final Color BLUE = new Color(0x2E7BE8);
    static final Color GOLD = new Color(0xFFC83D);

    public static final Rectangle NAV_BAR = new Rectangle(485, 858, 670, 92);
    public static final Rectangle PLAY = new Rectangle(1335, 826, 282, 112);
    public static final Rectangle GEAR = new Rectangle(1548, 14, 64, 64);
    private static final String[] NAV = {"nav_map", "nav_inventory", "nav_quests", "nav_achievements", "nav_settings"};
    private static final int[] NAV_X = {498, 640, 752, 868, 1000};
    private static final int[] NAV_W = {138, 108, 110, 124, 128};
    public static final Rectangle[] NAV_RECTS = new Rectangle[5];
    private static BufferedImage dimMap;

    static {
        for (int i = 0; i < 5; i++) NAV_RECTS[i] = new Rectangle(NAV_X[i], 864, NAV_W[i], 80);
    }

    private Hud() {}

    // ------------------------------------------------------------------ helpers
    static void pill(Graphics2D g, int x, int y, int w, int h, double arc) {
        RoundRectangle2D r = new RoundRectangle2D.Double(x, y, w, h, arc, arc);
        g.setPaint(new GradientPaint(0, y, NAVY_T, 0, y + h, NAVY_B));
        g.fill(r);
        g.setColor(BLUE);
        g.setStroke(new BasicStroke(2.5f));
        g.draw(r);
        g.setColor(new Color(130, 195, 255, 80));
        g.setStroke(new BasicStroke(1.2f));
        g.draw(new RoundRectangle2D.Double(x + 3.5, y + 3.5, w - 7, h - 7, arc - 4, arc - 4));
        g.setStroke(new BasicStroke(1f));
    }

    static void text(Graphics2D g, String s, int x, int y, Font f, Color c) {
        g.setFont(f);
        g.setColor(new Color(0, 0, 0, 120));
        g.drawString(s, x + 2, y + 2);
        g.setColor(c);
        g.drawString(s, x, y);
    }

    static void centered(Graphics2D g, String s, int cx, int y, Font f, Color c) {
        g.setFont(f);
        int w = g.getFontMetrics().stringWidth(s);
        text(g, s, cx - w / 2, y, f, c);
    }

    static void heart(Graphics2D g, double cx, double cy, double r) {
        Path2D p = new Path2D.Double();
        p.moveTo(cx, cy + r);
        p.curveTo(cx - r * 1.7, cy + r * 0.05, cx - r * 1.1, cy - r * 1.2, cx, cy - r * 0.4);
        p.curveTo(cx + r * 1.1, cy - r * 1.2, cx + r * 1.7, cy + r * 0.05, cx, cy + r);
        g.setPaint(new GradientPaint(0, (float) (cy - r), new Color(0xFF5A6A), 0, (float) (cy + r), new Color(0xD81F35)));
        g.fill(p);
        g.setColor(new Color(255, 255, 255, 150));
        g.fill(new Ellipse2D.Double(cx - r * 0.75, cy - r * 0.55, r * 0.4, r * 0.3));
    }

    static void coin(Graphics2D g, double cx, double cy, double r) {
        g.setColor(new Color(0xD98B0B));
        g.fill(new Ellipse2D.Double(cx - r, cy - r, r * 2, r * 2));
        g.setPaint(new GradientPaint(0, (float) (cy - r), new Color(0xFFE27A), 0, (float) (cy + r), new Color(0xF9A825)));
        g.fill(new Ellipse2D.Double(cx - r + 3, cy - r + 3, r * 2 - 6, r * 2 - 6));
        centered(g, "$", (int) cx, (int) (cy + r * 0.45), new Font("SansSerif", Font.BOLD, (int) (r * 1.3)), new Color(0xC77700));
    }

    static void star(Graphics2D g, double cx, double cy, double r) {
        Path2D p = new Path2D.Double();
        for (int i = 0; i < 10; i++) {
            double a = -Math.PI / 2 + i * Math.PI / 5;
            double rr = i % 2 == 0 ? r : r * 0.45;
            double px = cx + Math.cos(a) * rr, py = cy + Math.sin(a) * rr;
            if (i == 0) p.moveTo(px, py); else p.lineTo(px, py);
        }
        p.closePath();
        g.setColor(new Color(0xD98B0B));
        g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(p);
        g.setColor(new Color(0xFFD83A));
        g.fill(p);
        g.setStroke(new BasicStroke(1f));
    }

    static void gear(Graphics2D g, double cx, double cy, double r, Color c) {
        g.setColor(c);
        AffineTransform old = g.getTransform();
        g.translate(cx, cy);
        for (int i = 0; i < 8; i++) {
            g.fill(new RoundRectangle2D.Double(-r * 0.18, -r, r * 0.36, r * 0.5, 3, 3));
            g.rotate(Math.PI / 4);
        }
        g.setTransform(old);
        g.fill(new Ellipse2D.Double(cx - r * 0.75, cy - r * 0.75, r * 1.5, r * 1.5));
        g.setColor(NAVY_B);
        g.fill(new Ellipse2D.Double(cx - r * 0.33, cy - r * 0.33, r * 0.66, r * 0.66));
    }

    static void lock(Graphics2D g, int x, int y, Color c) { // x,y = top-left of a 16x18 icon
        g.setColor(c);
        g.setStroke(new BasicStroke(2.4f));
        g.draw(new Arc2D.Double(x + 2, y, 12, 14, 0, 180, Arc2D.OPEN));
        g.fill(new RoundRectangle2D.Double(x, y + 7, 16, 11, 4, 4));
        g.setColor(NAVY_B);
        g.fill(new Ellipse2D.Double(x + 6, y + 10, 4, 4));
        g.setStroke(new BasicStroke(1f));
    }

    static void pickleball(Graphics2D g, double cx, double cy, double r) {
        g.setColor(new Color(0xC9A800));
        g.fill(new Ellipse2D.Double(cx - r, cy - r, r * 2, r * 2));
        g.setPaint(new GradientPaint(0, (float) (cy - r), new Color(0xFFEB3B), 0, (float) (cy + r), new Color(0xF2C500)));
        g.fill(new Ellipse2D.Double(cx - r + 2, cy - r + 2, r * 2 - 4, r * 2 - 4));
        g.setColor(new Color(0x3A3000));
        double[][] holes = {{0, 0}, {-0.5, -0.1}, {0.5, -0.1}, {-0.25, -0.55}, {0.25, -0.55}, {-0.25, 0.5}, {0.25, 0.5}, {-0.7, 0.35}, {0.7, 0.35}, {0, -0.85}, {0, 0.85}};
        for (double[] h : holes) g.fill(new Ellipse2D.Double(cx + h[0] * r - r * 0.09, cy + h[1] * r - r * 0.09, r * 0.18, r * 0.18));
    }

    static void drawSprite(Graphics2D g, String name, int x, int y, int h) {
        BufferedImage s = Assets.get(name);
        int w = (int) Math.round(s.getWidth() * (h / (double) s.getHeight()));
        g.drawImage(s, x, y, w, h, null);
    }

    // ------------------------------------------------------------------ top bar
    public static void drawTop(Graphics2D g, GameState st, Location current) {
        Font big = new Font("SansSerif", Font.BOLD, 30);

        pill(g, 22, 14, 458, 64, 40);
        heart(g, 62, 46, 22);
        text(g, "HP", 100, 56, new Font("SansSerif", Font.BOLD, 26), Color.WHITE);
        g.setColor(new Color(0x07101F));
        g.fill(new RoundRectangle2D.Double(146, 34, 208, 22, 22, 22));
        double frac = Math.max(0, Math.min(1, st.hp / (double) st.maxHp));
        if (frac > 0) {
            g.setPaint(new GradientPaint(0, 34, new Color(0x7BEA5A), 0, 56, new Color(0x2FA82A)));
            g.fill(new RoundRectangle2D.Double(148, 36, 204 * frac, 18, 18, 18));
        }
        text(g, st.hp + "/" + st.maxHp, 366, 56, new Font("SansSerif", Font.BOLD, 24), Color.WHITE);

        pill(g, 1065, 14, 208, 64, 40);
        coin(g, 1108, 46, 23);
        text(g, String.valueOf(st.coins), 1150, 57, big, Color.WHITE);

        pill(g, 1288, 14, 235, 64, 40);
        star(g, 1330, 47, 24);
        text(g, st.stars + "/" + st.maxStars, 1378, 57, big, Color.WHITE);

        pill(g, GEAR.x, GEAR.y, GEAR.width, GEAR.height, 20);
        gear(g, GEAR.getCenterX(), GEAR.getCenterY(), 20, new Color(0xE8EEF9));

        // current location panel
        pill(g, 1366, 88, 258, 70, 20);
        BufferedImage flag = Assets.get("flag_ph");
        g.drawImage(flag, 1380, 98, (int) (flag.getWidth() * 0.5), (int) (flag.getHeight() * 0.5), null);
        text(g, "Current Location", 1440, 117, new Font("SansSerif", Font.BOLD, 18), Color.WHITE);
        drawSprite(g, current.pinIcon, 1440, 126, 26);
        text(g, current.name, 1474, 148, new Font("SansSerif", Font.BOLD, 22), GOLD);
    }

    // ------------------------------------------------------------------ bottom bar
    public static void drawBottom(Graphics2D g, GameState st, int activeTab, double t) {
        // player card
        pill(g, 60, 852, 336, 92, 44);
        int ax = 22, ay = 822, ad = 124;
        g.setColor(BLUE);
        g.fill(new Ellipse2D.Double(ax - 4, ay - 4, ad + 8, ad + 8));
        Shape oldClip = g.getClip();
        g.clip(new Ellipse2D.Double(ax, ay, ad, ad));
        g.drawImage(Assets.get("portrait"), ax, ay, ad, ad, null);
        g.setClip(oldClip);
        text(g, st.playerName, 160, 880, new Font("SansSerif", Font.BOLD, 24), Color.WHITE);
        text(g, "Lv. " + st.level, 160, 906, new Font("SansSerif", Font.BOLD, 20), Color.WHITE);
        g.setColor(new Color(0x07101F));
        g.fill(new RoundRectangle2D.Double(160, 915, 150, 14, 14, 14));
        double xf = st.xp / (double) st.xpToNext;
        if (xf > 0) {
            g.setPaint(new GradientPaint(0, 915, new Color(0x6FD0FF), 0, 929, new Color(0x2B86E8)));
            g.fill(new RoundRectangle2D.Double(161, 916, 148 * xf, 12, 12, 12));
        }
        text(g, st.xp + "/" + st.xpToNext, 326, 928, new Font("SansSerif", Font.BOLD, 20), Color.WHITE);

        // nav bar
        pill(g, NAV_BAR.x, NAV_BAR.y, NAV_BAR.width, NAV_BAR.height, 36);
        for (int i = 0; i < 5; i++) {
            BufferedImage img = Assets.get(NAV[i]);
            if (i == 0 && activeTab != 0) img = dimmedMap(img);
            Rectangle r = NAV_RECTS[i];
            int w = r.width - 6, h = (int) (img.getHeight() * (w / (double) img.getWidth()));
            h = Math.min(h, 74);
            w = (int) (img.getWidth() * (h / (double) img.getHeight()));
            g.drawImage(img, r.x + (r.width - w) / 2, r.y + (r.height - h) / 2, w, h, null);
            if (i == activeTab && i != 0) {
                g.setColor(GOLD);
                g.setStroke(new BasicStroke(3f));
                g.draw(new RoundRectangle2D.Double(r.x + 2, r.y + 2, r.width - 4, r.height - 4, 14, 14));
                g.setStroke(new BasicStroke(1f));
            }
        }

        // play button
        double pulse = 0.5 + 0.5 * Math.sin(t * 3);
        pill(g, PLAY.x, PLAY.y, PLAY.width, PLAY.height, 70);
        RoundRectangle2D inner = new RoundRectangle2D.Double(PLAY.x + 8, PLAY.y + 8, PLAY.width - 16, PLAY.height - 16, 56, 56);
        g.setPaint(new GradientPaint(0, PLAY.y + 8, new Color(0x2F8BFF), 0, PLAY.y + PLAY.height - 8, new Color(0x1347B8)));
        g.fill(inner);
        g.setColor(new Color(255, 255, 255, (int) (50 + 60 * pulse)));
        g.setStroke(new BasicStroke(2f));
        g.draw(inner);
        g.setStroke(new BasicStroke(1f));
        pickleball(g, PLAY.x + 62, PLAY.getCenterY(), 40);
        text(g, "Play", PLAY.x + 118, PLAY.y + 76, new Font("SansSerif", Font.BOLD | Font.ITALIC, 56), Color.WHITE);
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int cx = PLAY.x + PLAY.width - 38, cy = (int) PLAY.getCenterY();
        g.drawLine(cx - 8, cy - 16, cx + 8, cy);
        g.drawLine(cx + 8, cy, cx - 8, cy + 16);
        g.setStroke(new BasicStroke(1f));
    }

    private static BufferedImage dimmedMap(BufferedImage src) {
        if (dimMap == null) {
            BufferedImage gray = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D gg = gray.createGraphics();
            gg.drawImage(src, 0, 0, null);
            gg.setColor(new Color(10, 20, 45, 150));
            gg.fillRect(0, 0, gray.getWidth(), gray.getHeight());
            gg.dispose();
            dimMap = gray;
        }
        return dimMap;
    }

    // ------------------------------------------------------------------ labels
    public static void drawLabel(Graphics2D g, Location loc, boolean isCurrent, boolean hover) {
        Font f = new Font("SansSerif", Font.BOLD, 19);
        g.setFont(f);
        FontMetrics fm = g.getFontMetrics();
        int iconH = 26;
        int tw = fm.stringWidth(loc.name);
        int w = 14 + 22 + 8 + tw + (loc.hasLock() ? 12 + 16 : 0) + 16;
        int h = 38;
        int x = loc.block.x + loc.block.width / 2 - w / 2, y = loc.block.y + 4;
        loc.labelRect.setBounds(x, y, w, h);

        RoundRectangle2D r = new RoundRectangle2D.Double(x, y, w, h, h, h);
        g.setPaint(new GradientPaint(0, y, NAVY_T, 0, y + h, NAVY_B));
        g.fill(r);
        g.setColor(isCurrent ? GOLD : (hover ? new Color(0x8CC8FF) : BLUE));
        g.setStroke(new BasicStroke(isCurrent ? 3f : 2.2f));
        g.draw(r);
        g.setStroke(new BasicStroke(1f));
        drawSprite(g, loc.pinIcon, x + 12, y + (h - iconH) / 2, iconH);
        text(g, loc.name, x + 12 + 22 + 8, y + 26, f, Color.WHITE);
        if (loc.hasLock()) lock(g, x + w - 16 - 14, y + 10, loc.locked ? new Color(0xE3E8F2) : new Color(0x3DDB6A));
    }

    // ------------------------------------------------------------------ overlays
    public static void drawToast(Graphics2D g, String msg, double alpha) {
        Font f = new Font("SansSerif", Font.BOLD, 22);
        g.setFont(f);
        int w = g.getFontMetrics().stringWidth(msg) + 48, h = 50;
        int x = GamePanel.W / 2 - w / 2, y = 100;
        Composite old = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) Math.max(0, Math.min(1, alpha))));
        pill(g, x, y, w, h, h);
        text(g, msg, x + 24, y + 33, f, Color.WHITE);
        g.setComposite(old);
    }

    /** Draws a centered dialog; returns the confirm/back button rectangles via the arrays. */
    public static void drawModal(Graphics2D g, String title, List<String> lines, String confirm, Rectangle confirmRect, Rectangle backRect) {
        g.setColor(new Color(4, 9, 22, 170));
        g.fillRect(0, 0, GamePanel.W, GamePanel.H);
        int w = 640, h = 130 + lines.size() * 34 + 90;
        int x = GamePanel.W / 2 - w / 2, y = GamePanel.H / 2 - h / 2;
        pill(g, x, y, w, h, 36);
        centered(g, title, x + w / 2, y + 62, new Font("SansSerif", Font.BOLD, 38), GOLD);
        int ly = y + 112;
        for (String s : lines) {
            centered(g, s, x + w / 2, ly, new Font("SansSerif", Font.PLAIN, 22), Color.WHITE);
            ly += 34;
        }
        int by = y + h - 76;
        if (confirm != null) {
            confirmRect.setBounds(x + w / 2 - 250, by, 240, 54);
            backRect.setBounds(x + w / 2 + 10, by, 240, 54);
        } else {
            confirmRect.setBounds(0, 0, 0, 0);
            backRect.setBounds(x + w / 2 - 120, by, 240, 54);
        }
        if (confirm != null) button(g, confirmRect, confirm, true);
        button(g, backRect, confirm != null ? "Back  (Esc)" : "OK  (Enter)", false);
    }

    private static void button(Graphics2D g, Rectangle r, String label, boolean primary) {
        RoundRectangle2D rr = new RoundRectangle2D.Double(r.x, r.y, r.width, r.height, 30, 30);
        g.setPaint(primary
                ? new GradientPaint(0, r.y, new Color(0xFFD64D), 0, r.y + r.height, new Color(0xF2A512))
                : new GradientPaint(0, r.y, new Color(0x2F8BFF), 0, r.y + r.height, new Color(0x1347B8)));
        g.fill(rr);
        g.setColor(new Color(255, 255, 255, 120));
        g.draw(rr);
        centered(g, label, (int) r.getCenterX(), r.y + 35, new Font("SansSerif", Font.BOLD, 24), primary ? new Color(0x4A2C00) : Color.WHITE);
    }
}
