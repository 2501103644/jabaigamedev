import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/** A decorative car driving along a street. Horizontal cars wrap; vertical cars turn around. */
public class Car {
    private final BufferedImage img;
    private double x, y;
    private double speed;          // pixels/second, sign = direction
    private final boolean vertical;
    private final double min, max; // travel range along the axis
    private static final double SCALE = 0.8;

    public Car(String sprite, double x, double y, double speed, boolean vertical, double min, double max) {
        this.img = Assets.get(sprite);
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.vertical = vertical;
        this.min = min;
        this.max = max;
    }

    public void update(double dt, double playerX, double playerY) {
        // cars wait politely when the player is close
        if (Math.hypot(x - playerX, y - 14 - playerY) < 70) return;
        if (vertical) {
            y += speed * dt;
            if (y > max) { y = max; speed = -Math.abs(speed); }
            if (y < min) { y = min; speed = Math.abs(speed); }
        } else {
            x += speed * dt;
            if (speed > 0 && x > max) x = min;
            if (speed < 0 && x < min) x = max;
        }
    }

    public void draw(Graphics2D g) {
        int w = (int) (img.getWidth() * SCALE), h = (int) (img.getHeight() * SCALE);
        int dx = (int) x - w / 2, dy = (int) y - h / 2;
        // Soft shadow under the car, so it sits on the road instead of floating on it.
        g.setColor(new java.awt.Color(0, 0, 0, 60));
        g.fill(new java.awt.geom.Ellipse2D.Double(dx + 4, dy + h - 6, w - 8, 10));
        boolean flip = !vertical && speed > 0; // sprites face left
        if (flip) g.drawImage(img, dx + w, dy, -w, h, null);
        else g.drawImage(img, dx, dy, w, h, null);
    }
}
