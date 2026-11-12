import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/** Loads and caches PNG sprites from the assets/ folder. */
public final class Assets {
    private static final Map<String, BufferedImage> CACHE = new HashMap<>();
    private static File dir;

    static {
        for (String p : new String[]{"assets", "../assets", "PickleSmashMap/assets"}) {
            File f = new File(p);
            if (f.isDirectory()) {
                dir = f;
                break;
            }
        }
    }

    private Assets() {}

    public static BufferedImage get(String name) {
        return CACHE.computeIfAbsent(name, n -> {
            try {
                return ImageIO.read(new File(dir, n + ".png"));
            } catch (Exception e) {
                System.err.println("Missing asset: " + n + ".png");
                BufferedImage img = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = img.createGraphics();
                g.setColor(Color.MAGENTA);
                g.fillRect(0, 0, 32, 32);
                g.dispose();
                return img;
            }
        });
    }
}
