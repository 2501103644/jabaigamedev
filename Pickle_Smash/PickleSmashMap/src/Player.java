import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;

/** The controllable chibi character (cap + blue shirt), drawn in code with a walk cycle. */
public class Player {
    public enum Dir { DOWN, UP, LEFT, RIGHT }

    /** Lets the player ask the map whether a spot is walkable. */
    public interface World {
        boolean isBlocked(double footX, double footY);
    }

    public double x, y;            // position of the feet
    public Dir dir = Dir.DOWN;
    public boolean moving;
    private double walkT;
    public static final double SPEED = 230;

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

    public void update(double dt, boolean up, boolean down, boolean left, boolean right, World world) {
        double mx = (right ? 1 : 0) - (left ? 1 : 0);
        double my = (down ? 1 : 0) - (up ? 1 : 0);
        moving = mx != 0 || my != 0;
        if (!moving) {
            walkT = 0;
            return;
        }
        double len = Math.hypot(mx, my);
        double stepX = mx / len * SPEED * dt, stepY = my / len * SPEED * dt;
        if (!world.isBlocked(x + stepX, y)) x += stepX;
        if (!world.isBlocked(x, y + stepY)) y += stepY;
        if (Math.abs(mx) > Math.abs(my) || (my == 0)) dir = mx > 0 ? Dir.RIGHT : Dir.LEFT;
        else dir = my > 0 ? Dir.DOWN : Dir.UP;
        walkT += dt;
    }

    public void draw(Graphics2D g) {
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
