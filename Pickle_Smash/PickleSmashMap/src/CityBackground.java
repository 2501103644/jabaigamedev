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

    /** Water area in the bottom-right corner (also a solid obstacle). */
    public static final Rectangle WATER = new Rectangle(1215, 655, 427, 303);

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
        drawWaterLip(g);
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
        drawStrip(g, r, pal, 1598, 150, 44, 505);
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
        g.drawLine(0, 905, 1215, 905);
        int[][] vertical = {{426, 150, 355}, {848, 150, 355}, {1198, 150, 355},
                {524, 400, 600}, {1170, 400, 600}, {446, 655, 860}, {817, 655, 860}};
        for (int[] v : vertical) g.drawLine(v[0], v[1], v[0], v[2]);
        g.setStroke(new BasicStroke(1f));
    }

    private static void drawCrosswalks(Graphics2D g) {
        g.setColor(new Color(0xE9EDF3));
        int[][] overHorizontalRoad = {{535, 358, 34, 40}, {1135, 358, 34, 40}, {566, 606, 34, 44}, {930, 358, 34, 40}};
        for (int[] c : overHorizontalRoad)
            for (int i = 0; i < c[2]; i += 9) g.fillRect(c[0] + i, c[1], 5, c[3]);
        int[][] overVerticalRoad = {{418, 664, 58, 30}, {792, 664, 52, 30}, {1196, 664, 20, 30}, {386, 170, 80, 26}};
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

        // building
        Rectangle bounds;
        int cx = b.x + b.width / 2, bottom = b.y + b.height - 14;
        if (loc.sprite != null) {
            BufferedImage s = Assets.get(loc.sprite);
            double sc = Math.min(1.0, Math.min((b.width - 50) / (double) s.getWidth(), (b.height - 38) / (double) s.getHeight()));
            int w = (int) (s.getWidth() * sc), h = (int) (s.getHeight() * sc);
            bounds = new Rectangle(cx - w / 2, bottom - h, w, h);
        } else {
            bounds = new Rectangle(cx - 150, bottom - 150, 300, 150);
        }

        scatterProps(g, loc, bounds);

        if (loc.sprite != null) g.drawImage(Assets.get(loc.sprite), bounds.x, bounds.y, bounds.width, bounds.height, null);
        else drawTwoPlayerBuilding(g, cx, bottom);
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
        if (loc.kind == Location.Kind.TWO_PLAYER) { // fountains in the park like the mockup
            for (int fx : new int[]{b.x + 85, b.x + b.width - 125}) {
                BufferedImage f = Assets.get("fountain");
                g.drawImage(f, fx, b.y + 52, (int) (f.getWidth() * 0.7), (int) (f.getHeight() * 0.7), null);
            }
        }
        if (loc.kind == Location.Kind.LEVEL && loc.name.startsWith("Barangay")) {
            BufferedImage f = Assets.get("flag_ph");
            g.drawImage(f, b.x + 24, b.y + 20, (int) (f.getWidth() * 0.9), (int) (f.getHeight() * 0.9), null);
        }
    }

    /** "Two Player Mode" has no sprite in the atlas, so its stadium-style building is drawn here. */
    private static void drawTwoPlayerBuilding(Graphics2D g, int cx, int bottom) {
        // base slab
        g.setColor(new Color(0x9AA3B2));
        g.fill(new RoundRectangle2D.Double(cx - 160, bottom - 12, 320, 16, 8, 8));
        // facade
        g.setColor(new Color(0xE6D4AE));
        g.fillRect(cx - 135, bottom - 85, 270, 76);
        g.setColor(new Color(0xCBB88F));
        g.fillRect(cx - 135, bottom - 14, 270, 5);
        // roof (solar panels)
        Path2D roof = new Path2D.Double();
        roof.moveTo(cx - 150, bottom - 85);
        roof.lineTo(cx - 108, bottom - 140);
        roof.lineTo(cx + 108, bottom - 140);
        roof.lineTo(cx + 150, bottom - 85);
        roof.closePath();
        g.setColor(new Color(0x1F4FA6));
        g.fill(roof);
        g.setColor(new Color(0x5E97F0));
        g.setStroke(new BasicStroke(1.5f));
        for (int i = 1; i < 8; i++) {
            double t = i / 8.0;
            g.draw(new Line2D.Double(cx - 108 + t * 216, bottom - 140, cx - 150 + t * 300, bottom - 85));
        }
        for (int i = 1; i < 3; i++) g.draw(new Line2D.Double(cx - 108 - i * 14, bottom - 140 + i * 18, cx + 108 + i * 14, bottom - 140 + i * 18));
        g.setColor(new Color(0xF0E3C4));
        g.fillRect(cx - 152, bottom - 88, 304, 6);
        // doors
        g.setColor(new Color(0xF0E3C4));
        g.fillRect(cx - 34, bottom - 66, 68, 60);
        g.setColor(new Color(0x2C6BD6));
        g.fillRect(cx - 28, bottom - 60, 25, 54);
        g.fillRect(cx + 3, bottom - 60, 25, 54);
        g.setColor(new Color(0x9CC7FF));
        g.fillRect(cx - 24, bottom - 56, 6, 46);
        g.fillRect(cx + 7, bottom - 56, 6, 46);
        // paddle panels
        for (int sx : new int[]{-1, 1}) {
            int px = cx + sx * 92 - 24;
            g.setColor(new Color(0xF0E3C4));
            g.fillRect(px - 4, bottom - 78, 56, 60);
            g.setColor(new Color(0x2457C2));
            g.fillRect(px, bottom - 74, 48, 52);
            g.setColor(new Color(0xF2A12E));
            g.fill(new Ellipse2D.Double(px + 8, bottom - 70, 26, 28));
            g.setColor(new Color(0xB4631A));
            g.fill(new RoundRectangle2D.Double(px + 17, bottom - 44, 8, 18, 4, 4));
            g.setColor(new Color(0xFFF3B0));
            g.fill(new Ellipse2D.Double(px + 30, bottom - 36, 10, 10));
        }
        g.setStroke(new BasicStroke(1f));
    }

    private static void drawWaterLip(Graphics2D g) {
        Rectangle w = WATER;
        g.setColor(SIDEWALK_D);
        g.fillRect(w.x - 2, w.y - 2, w.width + 4, w.height + 4);
        g.setColor(SIDEWALK);
        g.fillRect(w.x, w.y, w.width, 30);
        g.fillRect(w.x, w.y, 26, w.height);
        g.setColor(new Color(0x9AA3B2));
        for (int x = w.x + 26; x < w.x + w.width; x += 34) g.drawLine(x, w.y, x, w.y + 29);
        // blue railing along the top edge
        g.setColor(new Color(0x3C7BD9));
        g.setStroke(new BasicStroke(3f));
        g.drawLine(w.x + 26, w.y + 8, w.x + w.width, w.y + 8);
        g.drawLine(w.x + 26, w.y + 18, w.x + w.width, w.y + 18);
        for (int x = w.x + 28; x < w.x + w.width; x += 22) g.drawLine(x, w.y + 4, x, w.y + 24);
        g.setStroke(new BasicStroke(1f));
        BufferedImage bush = Assets.get("bush_flower");
        g.drawImage(bush, w.x + 4, w.y - 16, (int) (bush.getWidth() * 0.8), (int) (bush.getHeight() * 0.8), null);
    }

    /** Animated water, drawn every frame on top of the background. */
    public static void drawWater(Graphics2D g, double t) {
        Rectangle w = new Rectangle(WATER.x + 26, WATER.y + 30, WATER.width - 26, WATER.height - 30);
        g.setPaint(new GradientPaint(0, w.y, new Color(0x2A9BEF), 0, w.y + w.height, new Color(0x0D62C4)));
        g.fill(w);
        Shape old = g.getClip();
        g.clip(w);
        g.setStroke(new BasicStroke(2f));
        for (int row = 0; row < 14; row++) {
            int y = w.y + 18 + row * 21;
            g.setColor(new Color(255, 255, 255, 70));
            Path2D p = new Path2D.Double();
            for (int x = w.x; x <= w.x + w.width; x += 8) {
                double yy = y + Math.sin(x * 0.05 + t * 1.6 + row) * 3;
                if (x == w.x) p.moveTo(x, yy); else p.lineTo(x, yy);
            }
            g.draw(p);
        }
        g.setClip(old);
        g.setStroke(new BasicStroke(1f));
    }
}
