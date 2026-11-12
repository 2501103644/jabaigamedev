import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Renders the static part of the map once: sky, skyline, streets, blocks, buildings and props. */
public final class CityBackground {
    static final Color ASPHALT = new Color(0x2B3544);
    static final Color LANE = new Color(0xC9CED6);
    static final Color SIDEWALK = new Color(0xBCC2CC);
    static final Color SIDEWALK_D = new Color(0x7C8594);
    static final Color GRASS = new Color(0x4FA03F);
    static final Color GRASS_L = new Color(0x5DB24A);

    /** Decorative park blocks (not locations): they fill the freed space in the
     *  middle band and the bottom-right corner, so the map has no empty holes.
     *  They are solid, like the location blocks. The middle one has fountains. */
    public static final Rectangle[] PARK_BLOCKS = {
            new Rectangle(48, 400, 440, 200),
            new Rectangle(558, 400, 548, 200),
            new Rectangle(1150, 400, 440, 200),
            new Rectangle(1128, 655, 460, 200),
    };

    private CityBackground() {}

    public static BufferedImage render(List<Location> locations) {
        int W = GamePanel.W, H = GamePanel.H;
        BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        drawSky(g, W);
        g.setColor(ASPHALT);
        g.fillRect(0, 150, W, H - 150);
        drawLanes(g, W);
        drawCrosswalks(g);
        drawSideStrips(g);

        for (Location loc : locations) drawBlock(g, loc);
        for (int i = 0; i < PARK_BLOCKS.length; i++) drawParkBlock(g, PARK_BLOCKS[i], i, i == 1);
        g.dispose();
        return img;
    }

    // ------------------------------------------------------------------ sky & skyline
    private static void drawSky(Graphics2D g, int W) {
        g.setPaint(new GradientPaint(0, 0, new Color(0x5BB0F4), 0, 150, new Color(0xCDE8FF)));
        g.fillRect(0, 0, W, 150);
        g.setColor(new Color(255, 255, 255, 190));
        int[][] clouds = {{150, 30, 120}, {430, 20, 90}, {760, 45, 130}, {1000, 18, 100}, {1480, 60, 110}};
        for (int[] c : clouds) {
            g.fill(new Ellipse2D.Double(c[0], c[1], c[2], 26));
            g.fill(new Ellipse2D.Double(c[0] + 20, c[1] - 12, c[2] * 0.55, 30));
        }
        Random r = new Random(7);
        Color[] pal = {new Color(0x3F6FB5), new Color(0x5B86C9), new Color(0x7FA6DD), new Color(0x2E5497), new Color(0x6A93D4)};
        int x = -10;
        while (x < W) {
            int w = 38 + r.nextInt(60), h = 50 + r.nextInt(95);
            Color c = pal[r.nextInt(pal.length)];
            g.setColor(c);
            g.fillRect(x, 150 - h, w, h);
            g.setColor(new Color(255, 255, 255, 70));
            for (int wy = 150 - h + 8; wy < 142; wy += 12)
                for (int wx = x + 6; wx < x + w - 8; wx += 12) g.fillRect(wx, wy, 5, 6);
            x += w + 2 + r.nextInt(8);
        }
    }

    private static void drawSideStrips(Graphics2D g) {
        Random r = new Random(21);
        Color[] pal = {new Color(0xE9A25B), new Color(0x4F7FCB), new Color(0xD98B6A), new Color(0x7FA6DD), new Color(0xE7C27D)};
        drawStrip(g, r, pal, 0, 150, 44, 855);
        drawStrip(g, r, pal, 1598, 150, 44, 855);
    }

    private static void drawStrip(Graphics2D g, Random r, Color[] pal, int x, int y0, int w, int y1) {
        int y = y0;
        while (y < y1) {
            int h = Math.min(60 + r.nextInt(60), y1 - y);
            Color c = pal[r.nextInt(pal.length)];
            g.setColor(c);
            g.fillRect(x, y, w, h);
            g.setColor(c.darker());
            g.fillRect(x, y, w, 4);
            g.setColor(new Color(255, 255, 255, 90));
            for (int wy = y + 12; wy < y + h - 10; wy += 14)
                for (int wx = x + 6; wx < x + w - 8; wx += 12) g.fillRect(wx, wy, 6, 7);
            y += h;
        }
    }

    // ------------------------------------------------------------------ streets
    private static void drawLanes(Graphics2D g, int W) {
        g.setColor(LANE);
        g.setStroke(new BasicStroke(3f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[]{22f, 18f}, 0f));
        g.drawLine(44, 378, 1598, 378);
        g.drawLine(44, 628, 1598, 628);
        g.drawLine(0, 905, 1598, 905);
        // vertical lane lines, one per gap between blocks in each band
        int[][] vertical = {{488, 150, 355}, {1048, 150, 355},
                {488, 400, 600}, {1106, 400, 600}, {508, 655, 860}, {1088, 655, 860}};
        for (int[] v : vertical) g.drawLine(v[0], v[1], v[0], v[2]);
        g.setStroke(new BasicStroke(1f));
    }

    private static void drawCrosswalks(Graphics2D g) {
        g.setColor(new Color(0xE9EDF3));
        int[][] overHorizontalRoad = {{470, 358, 34, 40}, {1030, 358, 34, 40}, {490, 606, 34, 44}, {1070, 606, 34, 44}};
        for (int[] c : overHorizontalRoad)
            for (int i = 0; i < c[2]; i += 9) g.fillRect(c[0] + i, c[1], 5, c[3]);
        int[][] overVerticalRoad = {{448, 170, 80, 26}, {1008, 170, 80, 26}, {488, 664, 80, 30}, {1068, 664, 80, 30}};
        for (int[] c : overVerticalRoad)
            for (int i = 0; i < c[3]; i += 9) g.fillRect(c[0], c[1] + i, c[2], 5);
    }

    // ------------------------------------------------------------------ blocks
    private static void drawBlock(Graphics2D g, Location loc) {
        Rectangle b = loc.block;
        RoundRectangle2D walk = new RoundRectangle2D.Double(b.x, b.y, b.width, b.height, 22, 22);
        g.setColor(SIDEWALK_D);
        g.fill(new RoundRectangle2D.Double(b.x - 2, b.y - 2, b.width + 4, b.height + 4, 24, 24));
        g.setColor(SIDEWALK);
        g.fill(walk);
        RoundRectangle2D lawn = new RoundRectangle2D.Double(b.x + 12, b.y + 12, b.width - 24, b.height - 24, 14, 14);
        g.setColor(GRASS);
        g.fill(lawn);
        Shape oldClip = g.getClip();
        g.clip(lawn);
        g.setColor(GRASS_L);
        for (int i = b.x + 12; i < b.x + b.width; i += 26) g.fillRect(i, b.y + 12, 13, b.height);
        g.setClip(oldClip);

        // building (every location in the 5-location map has a sprite)
        Rectangle bounds;
        int cx = b.x + b.width / 2, bottom = b.y + b.height - 14;
        BufferedImage s = Assets.get(loc.sprite);
        double sc = Math.min(1.0, Math.min((b.width - 50) / (double) s.getWidth(), (b.height - 38) / (double) s.getHeight()));
        int w = (int) (s.getWidth() * sc), h = (int) (s.getHeight() * sc);
        bounds = new Rectangle(cx - w / 2, bottom - h, w, h);

        scatterProps(g, loc, bounds);

        g.drawImage(Assets.get(loc.sprite), bounds.x, bounds.y, bounds.width, bounds.height, null);
    }

    /** A decorative park block: same sidewalk + lawn + props treatment as a location
     *  block, but with no building and no label. The middle park gets two fountains
     *  (sparkles still match their spots). */
    private static void drawParkBlock(Graphics2D g, Rectangle b, int seed, boolean fountains) {
        RoundRectangle2D walk = new RoundRectangle2D.Double(b.x, b.y, b.width, b.height, 22, 22);
        g.setColor(SIDEWALK_D);
        g.fill(new RoundRectangle2D.Double(b.x - 2, b.y - 2, b.width + 4, b.height + 4, 24, 24));
        g.setColor(SIDEWALK);
        g.fill(walk);
        RoundRectangle2D lawn = new RoundRectangle2D.Double(b.x + 12, b.y + 12, b.width - 24, b.height - 24, 14, 14);
        g.setColor(GRASS);
        g.fill(lawn);
        Shape oldClip = g.getClip();
        g.clip(lawn);
        g.setColor(GRASS_L);
        for (int i = b.x + 12; i < b.x + b.width; i += 26) g.fillRect(i, b.y + 12, 13, b.height);
        g.setClip(oldClip);

        Random r = new Random(1000 + seed);
        String[] trees = {"tree1", "tree2", "tree3", "tree_pine", "palm1", "palm2"};
        List<Rectangle> placed = new ArrayList<>();
        int target = Math.max(5, b.width * b.height / 9000);
        int tries = 0;
        while (placed.size() < target && tries++ < 200) {
            String name;
            double sc;
            int roll = r.nextInt(10);
            if (roll < 6) { name = trees[r.nextInt(trees.length)]; sc = 0.72 + r.nextDouble() * 0.2; }
            else if (roll < 8) { name = "bush_flower"; sc = 0.7; }
            else if (roll < 9) { name = r.nextBoolean() ? "bench1" : "bench2"; sc = 0.7; }
            else { name = "lamp"; sc = 0.62; }
            BufferedImage s = Assets.get(name);
            int w = (int) (s.getWidth() * sc), h = (int) (s.getHeight() * sc);
            int px = b.x + 20 + r.nextInt(Math.max(1, b.width - 40 - w));
            int py = b.y + 24 + h + r.nextInt(Math.max(1, b.height - 48 - h));
            Rectangle foot = new Rectangle(px, py - 14, w, 14);
            boolean clash = false;
            for (Rectangle p : placed) if (p.intersects(foot)) { clash = true; break; }
            if (clash) continue;
            placed.add(foot);
            g.drawImage(s, px, py - h, w, h, null);
        }
        if (fountains) {
            for (int fx : new int[]{b.x + 85, b.x + b.width - 125}) {
                BufferedImage f = Assets.get("fountain");
                g.drawImage(f, fx, b.y + 52, (int) (f.getWidth() * 0.7), (int) (f.getHeight() * 0.7), null);
            }
        }
    }

    /** Trees, benches, lamps and flower bushes around the building (stable random seed per block). */
    private static void scatterProps(Graphics2D g, Location loc, Rectangle building) {
        Rectangle b = loc.block;
        Random r = new Random(loc.name.hashCode());
        String[] trees = {"tree1", "tree2", "tree3", "tree_pine", "palm1", "palm2"};
        List<Rectangle> placed = new ArrayList<>();
        Rectangle keepOut = new Rectangle(building);
        keepOut.grow(10, 6);
        int target = Math.max(4, b.width * b.height / 9500);
        int tries = 0;
        List<Object[]> items = new ArrayList<>();
        while (items.size() < target && tries++ < 200) {
            String name;
            double sc;
            int roll = r.nextInt(10);
            if (roll < 6) { name = trees[r.nextInt(trees.length)]; sc = 0.72 + r.nextDouble() * 0.2; }
            else if (roll < 8) { name = "bush_flower"; sc = 0.7; }
            else if (roll < 9) { name = r.nextBoolean() ? "bench1" : "bench2"; sc = 0.7; }
            else { name = "lamp"; sc = 0.62; }
            java.awt.image.BufferedImage s = Assets.get(name);
            int w = (int) (s.getWidth() * sc), h = (int) (s.getHeight() * sc);
            int px = b.x + 20 + r.nextInt(Math.max(1, b.width - 40 - w));
            int py = b.y + 24 + h + r.nextInt(Math.max(1, b.height - 48 - h)); // py = feet
            Rectangle rect = new Rectangle(px, py - h, w, h);
            Rectangle foot = new Rectangle(px, py - 14, w, 14);
            if (foot.intersects(keepOut) || rect.intersects(new Rectangle(building.x, building.y + building.height / 2, building.width, building.height / 2 + 6))) continue;
            boolean clash = false;
            for (Rectangle p : placed) if (p.intersects(foot)) { clash = true; break; }
            if (clash) continue;
            placed.add(foot);
            items.add(new Object[]{name, rect, py});
        }
        items.sort((a, c) -> Integer.compare((int) a[2], (int) c[2]));
        for (Object[] it : items) {
            Rectangle rc = (Rectangle) it[1];
            g.drawImage(Assets.get((String) it[0]), rc.x, rc.y, rc.width, rc.height, null);
        }
        if (loc.kind == Location.Kind.LEVEL && loc.name.startsWith("Barangay")) {
            BufferedImage f = Assets.get("flag_ph");
            g.drawImage(f, b.x + 24, b.y + 20, (int) (f.getWidth() * 0.9), (int) (f.getHeight() * 0.9), null);
        }
    }

    // ------------------------------------------------------------------ animated overlays (new)
    // These are drawn every frame in GamePanel, on top of the static
    // background image — the background itself never changes, so the
    // map layout, roads, buildings and locations are untouched.
    // Every speed here is in px/second, driven by the game clock `t`,
    // so the movement is the same at any frame rate.

    /** Subtle clouds drifting across the sky band (y 0-150), wrapping around. */
    public static void drawMovingClouds(Graphics2D g, double t) {
        // base x, y, width, speed (px/s)
        double[][] clouds = {{120, 22, 150, 11}, {520, 48, 110, 14}, {900, 18, 170, 9}, {1300, 55, 120, 13}};
        for (double[] c : clouds) {
            double x = ((c[0] + t * c[3]) % (GamePanel.W + 320)) - 160;
            double y = c[1];
            g.setColor(new Color(255, 255, 255, 150));
            g.fill(new Ellipse2D.Double(x, y, c[2], 24));
            g.fill(new Ellipse2D.Double(x + c[2] * 0.22, y - 10, c[2] * 0.5, 26));
            g.fill(new Ellipse2D.Double(x + c[2] * 0.55, y + 4, c[2] * 0.35, 18));
        }
    }

    /** Two small birds crossing the sky, flapping gently. */
    public static void drawBirds(Graphics2D g, double t) {
        for (int i = 0; i < 2; i++) {
            double speed = 26 + i * 8;
            double x = ((200 + i * 700 + t * speed) % (GamePanel.W + 200)) - 100;
            double y = 38 + i * 28 + Math.sin(t * 1.2 + i * 2) * 6;
            double flap = Math.sin(t * 9 + i * 3) * 4;
            g.setColor(new Color(40, 60, 90, 180));
            g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(new Line2D.Double(x - 9, y - flap, x, y));
            g.draw(new Line2D.Double(x, y, x + 9, y - flap));
            g.setStroke(new BasicStroke(1f));
        }
    }

    /** Sparkles rising from the two fountains in the middle park (block 558,400,548,200). */
    public static void drawFountainSparkles(Graphics2D g, double t) {
        for (int fx : new int[]{674, 1012}) {
            int fy = 478;
            for (int i = 0; i < 6; i++) {
                double phase = (t * 0.9 + i * 0.17) % 1.0; // 0 = bottom, 1 = top, then restarts
                double sx = fx + Math.sin(t * 2 + i * 1.3) * 14 * (1 - phase);
                double sy = fy - phase * 34;
                int alpha = (int) (200 * (1 - phase));
                g.setColor(new Color(255, 255, 255, alpha));
                g.fill(new Ellipse2D.Double(sx - 2, sy - 2, 4, 4));
                g.setColor(new Color(170, 220, 255, alpha / 2));
                g.fill(new Ellipse2D.Double(sx - 3.5, sy - 3.5, 7, 7));
            }
        }
    }

    /** A few leaves drifting down across the map, swaying as they fall. */
    public static void drawLeaves(Graphics2D g, double t) {
        for (int i = 0; i < 12; i++) {
            double seed = i * 137.5;
            double x = (seed * 3.1 + t * (8 + (i % 3) * 3) + Math.sin(t * 1.5 + i) * 18) % GamePanel.W;
            double y = (seed * 1.7 + t * (11 + (i % 4) * 2)) % GamePanel.H;
            if (y < 150) continue; // keep the sky clear — leaves fall over the city, not the sky
            g.setColor(new Color(i % 2 == 0 ? 0x8BC34A : 0xFFD54F, true));
            Composite old = g.getComposite();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.55f));
            g.fill(new Ellipse2D.Double(x, y, 5, 3.5));
            g.setComposite(old);
        }
    }

    /** The warm glow around the player, rendered once and reused.
     *  The old code built a new RadialGradientPaint and filled a
     *  520x520 gradient ellipse EVERY frame — per-pixel gradient
     *  math, 60 times a second, on top of everything else. A
     *  gradient is just an image, so it is painted once into this
     *  small off-screen image and then blitted at the player's
     *  position, which is all the game loop ever needs. */
    private static BufferedImage glowImg;

    /** Gentle light: a warm glow that follows the player.
     *  (The slow daylight pulse used to be a translucent fillRect
     *  over the whole screen every frame — ~6 ms per paint for an
     *  effect nobody can see change. It is now baked into the
     *  background once, in GamePanel, at its average alpha 7.) */
    public static void drawLight(Graphics2D g, double t, double px, double py) {
        // Warm glow around the player (cached image, drawn at the player's feet).
        if (glowImg == null) {
            glowImg = new BufferedImage(520, 520, BufferedImage.TYPE_INT_ARGB);
            Graphics2D gg = glowImg.createGraphics();
            float[] dist = {0f, 1f};
            Color[] cols = {new Color(255, 240, 180, 26), new Color(255, 240, 180, 0)};
            gg.setPaint(new RadialGradientPaint(260f, 260f, 260f, dist, cols));
            gg.fillRect(0, 0, 520, 520);
            gg.dispose();
        }
        g.drawImage(glowImg, (int) (px - 260), (int) (py - 300), 520, 520, null);
    }
}
