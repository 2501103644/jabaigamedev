import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.List;

/** Draws the heads-up display for the exact-image map: profile card and coin pill.
 * No HP bar, no stars, no gear, no Current Location panel, no bottom nav bar.
 * MAP EXACT IMAGE: the rects below were measured from the mockup image
 * (2016x1152 -> world x0.8145, y0.8316), so the live HUD covers the baked-in
 * HUD in the image exactly. Settings stays reachable from the Home menu. */
public final class Hud {
    static final Color NAVY_T = new Color(0x14264F), NAVY_B = new Color(0x0A1630);
    static final Color BLUE = new Color(0x2E7BE8);
    static final Color GOLD = new Color(0xFFC83D);

    /** Profile card, top left — measured from the mockup: baked card spans
     *  world x 21-490, y 10-148 (avatar circle 16,8,140). */
    public static final Rectangle PROFILE_CARD = new Rectangle(21, 10, 469, 138);
    /** Coin pill, top right — measured from the mockup: baked pill spans
     *  world x 1290-1611, y 15-102 (coin icon centre 1346,59, r 35). */
    public static final Rectangle COIN_PILL = new Rectangle(1290, 15, 321, 87);

    private Hud() {}

    // ------------------------------------------------------------------ helpers
    static void pill(Graphics2D g, int x, int y, int w, int h, double arc) {
        RoundRectangle2D r = new RoundRectangle2D.Double(x, y, w, h, arc, arc);
        g.setPaint(new GradientPaint(0, y, new Color(0x17263B), 0, y + h, new Color(0x0C1728)));
        g.fill(r);
        g.setColor(new Color(255, 255, 255, 42));
        g.setStroke(new BasicStroke(1.5f));
        g.draw(r);
        g.setStroke(new BasicStroke(1f));
    }

    static void text(Graphics2D g, String s, int x, int y, Font f, Color c) {
        g.setFont(f);
        g.setColor(new Color(0, 0, 0, 90));
        g.drawString(s, x + 1, y + 1);
        g.setColor(c);
        g.drawString(s, x, y);
    }

    static void centered(Graphics2D g, String s, int cx, int y, Font f, Color c) {
        g.setFont(f);
        int w = g.getFontMetrics().stringWidth(s);
        text(g, s, cx - w / 2, y, f, c);
    }

    static void coin(Graphics2D g, double cx, double cy, double r) {
        g.setColor(new Color(0xD98B0B));
        g.fill(new Ellipse2D.Double(cx - r, cy - r, r * 2, r * 2));
        g.setPaint(new GradientPaint(0, (float) (cy - r), new Color(0xFFE27A), 0, (float) (cy + r), new Color(0xF9A825)));
        g.fill(new Ellipse2D.Double(cx - r + 3, cy - r + 3, r * 2 - 6, r * 2 - 6));
        centered(g, "$", (int) cx, (int) (cy + r * 0.45), new Font("SansSerif", Font.BOLD, (int) (r * 1.3)), new Color(0xC77700));
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
    /** Top left: profile card (selected avatar portrait, name, level, XP bar).
     *  Top right: coin pill only. No HP, no stars, no gear, no Current Location panel.
     *  Geometry measured from the mockup so the live card covers the baked one:
     *  avatar circle 16,8,140; "Player 1" baseline y 64; "Lv. 1" baseline y 108;
     *  XP bar 168,120,230x16; "0/100" at 412,137. */
    public static void drawTop(Graphics2D g, GameState st) {
        pill(g, PROFILE_CARD.x, PROFILE_CARD.y, PROFILE_CARD.width, PROFILE_CARD.height, 44);
        int ax = 34, ay = 23, ad = 108;
        g.setColor(new Color(255, 255, 255, 50));
        g.fill(new Ellipse2D.Double(ax - 3, ay - 3, ad + 6, ad + 6));
        Shape oldClip = g.getClip();
        g.clip(new Ellipse2D.Double(ax, ay, ad, ad));
        g.drawImage(Assets.get(st.getSelectedAvatar().getImageFile()), ax, ay, ad, ad, null);
        g.setClip(oldClip);
        text(g, "PLAYER", 170, 42, new Font("SansSerif", Font.BOLD, 14), new Color(0xA9B7C9));
        text(g, st.getPlayerName(), 170, 77, new Font("SansSerif", Font.BOLD, 32), Color.WHITE);
        text(g, "LEVEL " + st.getLevel(), 170, 108, new Font("SansSerif", Font.PLAIN, 18), new Color(0xD5DFEC));
        g.setColor(new Color(255, 255, 255, 38));
        g.fill(new RoundRectangle2D.Double(170, 119, 220, 10, 10, 10));
        double xf = Math.max(0, Math.min(1, st.getXp() / (double) st.getXpToNext()));
        if (xf > 0) {
            g.setColor(new Color(0x75C7FF));
            g.fill(new RoundRectangle2D.Double(170, 119, 220 * xf, 10, 10, 10));
        }
        text(g, st.getXp() + "/" + st.getXpToNext(), 400, 129, new Font("SansSerif", Font.PLAIN, 15), new Color(0xD5DFEC));

        pill(g, COIN_PILL.x, COIN_PILL.y, COIN_PILL.width, COIN_PILL.height, 40);
        coin(g, 1340, 58, 25);
        text(g, "COINS", 1385, 43, new Font("SansSerif", Font.BOLD, 14), new Color(0xA9B7C9));
        String coins = String.valueOf(st.getCoins());
        Font coinFont = new Font("SansSerif", Font.BOLD, 34);
        while (coinFont.getSize() > 22 && g.getFontMetrics(coinFont).stringWidth(coins) > 195) {
            coinFont = coinFont.deriveFont((float) coinFont.getSize() - 1);
        }
        text(g, coins, 1385, 78, coinFont, Color.WHITE);
    }

    // ------------------------------------------------------------------ labels
    /** Label pill above a location. MAP EXACT IMAGE: the pill is 68px tall, and the content
     *  (pin + name + lock) is centred in the pill. For the three level locations
     *  the pill position/width come from the location's measured labelY/labelW,
     *  so the live pill covers the baked-in pill in the image; Training and Home
     *  (no baked pill) use the same style, centred above their blocks. */
    public static void drawLabel(Graphics2D g, Location loc, boolean isCurrent, boolean hover) {
        Font f = new Font("SansSerif", Font.BOLD, 23);
        g.setFont(f);
        FontMetrics fm = g.getFontMetrics();
        int iconH = 32;
        int tw = fm.stringWidth(loc.name);
        int lockPart = loc.hasLock() ? 12 + 16 : 0;
        int contentW = 22 + 8 + tw + lockPart;
        int autoW = 14 + contentW + 16;
        int w = Math.max(autoW, loc.labelW);
        int h = 68;
        int x = loc.labelX >= 0 ? loc.labelX : loc.block.x + loc.block.width / 2 - w / 2;
        int y = loc.labelY >= 0 ? loc.labelY : loc.block.y + 4;
        loc.labelRect.setBounds(x, y, w, h);

        Graphics2D label = (Graphics2D) g.create();
        if (hover) {
            label.translate(x + w / 2.0, y + h / 2.0 - 3);
            label.scale(1.035, 1.035);
            label.translate(-(x + w / 2.0), -(y + h / 2.0));
        }
        RoundRectangle2D r = new RoundRectangle2D.Double(x, y, w, h, h, h);
        if (hover) {
            label.setColor(new Color(112, 205, 255, 55));
            label.setStroke(new BasicStroke(9f));
            label.draw(r);
        }
        label.setPaint(new GradientPaint(0, y, NAVY_T, 0, y + h, NAVY_B));
        label.fill(r);
        if (hover) {
            label.setColor(new Color(255, 255, 255, 18));
            label.fill(r);
        }
        label.setColor(isCurrent ? GOLD : (hover ? new Color(0x8CC8FF) : new Color(255, 255, 255, 48)));
        label.setStroke(new BasicStroke(isCurrent || hover ? 2.5f : 1.2f));
        label.draw(r);
        label.setStroke(new BasicStroke(1f));
        int cx = x + (w - contentW) / 2;
        drawSprite(label, loc.pinIcon, cx, y + (h - iconH) / 2, iconH);
        text(label, loc.name, cx + 22 + 8, y + h / 2 + 10, f, Color.WHITE);
        if (loc.hasLock()) lock(label, cx + 22 + 8 + tw + 12, y + (h - 18) / 2, loc.locked ? new Color(0xE3E8F2) : new Color(0x3DDB6A));
        label.dispose();
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

    public static void drawNpcDialogue(Graphics2D g, String speaker, List<String> lines,
                                       BufferedImage portrait, boolean canEnter,
                                       Rectangle confirmRect, Rectangle backRect) {
        g.setColor(new Color(5, 10, 8, 72));
        g.fillRect(0, 0, GamePanel.W, GamePanel.H);

        int x = 26, y = 622, w = GamePanel.W - 52, h = 302;
        g.setColor(new Color(13, 23, 18, 245));
        g.fillRect(x, y, w, h);
        g.setColor(new Color(174, 202, 151));
        g.setStroke(new BasicStroke(4f));
        g.drawRect(x + 2, y + 2, w - 4, h - 4);
        g.setColor(new Color(60, 83, 59));
        g.setStroke(new BasicStroke(2f));
        g.drawRect(x + 9, y + 9, w - 18, h - 18);
        g.setStroke(new BasicStroke(1f));

        int portraitX = x + 24, portraitY = y + 38, portraitW = 128, portraitH = 220;
        g.setColor(new Color(31, 45, 34));
        g.fillRect(portraitX, portraitY, portraitW, portraitH);
        g.setColor(new Color(174, 202, 151));
        g.drawRect(portraitX, portraitY, portraitW, portraitH);
        if (portrait != null) {
            Object oldInterpolation = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g.drawImage(portrait, portraitX + 13, portraitY + 12, 102, 192, null);
            if (oldInterpolation != null) g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, oldInterpolation);
        }

        int textX = x + 184;
        text(g, speaker, textX, y + 54, new Font("Monospaced", Font.BOLD, 24), new Color(0xD6E8C7));
        int lineY = y + 106;
        Font dialogueFont = new Font("Monospaced", Font.PLAIN, 22);
        for (String line : lines) {
            text(g, line, textX, lineY, dialogueFont, Color.WHITE);
            lineY += 34;
        }

        if (canEnter) {
            confirmRect.setBounds(textX, y + h - 66, 236, 46);
            backRect.setBounds(textX + 254, y + h - 66, 236, 46);
            dialogueButton(g, confirmRect, "Enter Level", true);
            dialogueButton(g, backRect, "Maybe Later", false);
        } else {
            confirmRect.setBounds(0, 0, 0, 0);
            backRect.setBounds(textX, y + h - 66, 236, 46);
            dialogueButton(g, backRect, "Close", false);
        }
        text(g, "E / ENTER: SELECT     ESC: CLOSE", GamePanel.W - 360, y + h - 24,
                new Font("Monospaced", Font.PLAIN, 13), new Color(0x9EAE98));
    }

    private static void dialogueButton(Graphics2D g, Rectangle r, String label, boolean primary) {
        g.setColor(new Color(0, 0, 0, 120));
        g.fillRect(r.x + 3, r.y + 3, r.width, r.height);
        g.setColor(primary ? new Color(0x405C3E) : new Color(0x26342B));
        g.fillRect(r.x, r.y, r.width, r.height);
        g.setColor(primary ? new Color(0xC4DBA8) : new Color(0x82947A));
        g.setStroke(new BasicStroke(2f));
        g.drawRect(r.x, r.y, r.width, r.height);
        g.setStroke(new BasicStroke(1f));
        centered(g, label, (int) r.getCenterX(), r.y + 30,
                new Font("Monospaced", Font.BOLD, 18), Color.WHITE);
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
