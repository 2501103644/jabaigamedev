import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * One HD animation sheet, split ONCE when it is loaded into its
 * frame images. Player asks for a frame by index, so the game loop
 * never does any cropping or resizing per frame — that is what
 * keeps the animation smooth and the sprites sharp.
 *
 * SMOOTHNESS (why the frames are small): the source cells are
 * 512 px, but the character is drawn at 96 px on the map. Scaling
 * a 512 px frame down to 96 px with bicubic interpolation EVERY
 * frame (60 times a second) is the most expensive thing the map
 * used to do, and it is what made the animation lag. So each
 * frame is scaled down ONCE, here, when the sheet loads (bicubic,
 * so the edges stay crisp), and the game loop only ever draws
 * the 96 px copy — a plain blit, no resampling per frame.
 * The 512 px PNGs are still on disk, so a bigger character later
 * only needs this one number changed.
 *
 * GRIDS: the classic sheets (idle, paddle-swing) are 4 cols x 2
 * rows = 8 frames. The directional sheets use their own grids,
 * all with 512 px cells:
 *   walk right / left: 6 cols x 1 row = 6 frames
 *   walk up:           5 cols x 1 row = 5 frames
 *   walk down:         4 cols x 1 row = 4 frames
 *   run:               3 cols x 2 rows = 6 frames
 * Pass the grid to the constructor; the no-arg form stays 4x2 so
 * the old sheets keep working unchanged.
 *
 * A sheet is "valid" only when it really loaded: Assets.get()
 * returns a 32x32 magenta square for a missing file, and a sheet
 * must be at least 1000x500. Anything else is treated as missing,
 * and Player falls back instead of showing magenta.
 */
public final class SpriteSheet {
    /** The classic 4x2 grid (idle, swing): 8 frames. */
    public static final int COLS = 4;
    public static final int ROWS = 2;
    public static final int FRAMES = COLS * ROWS; // 8
    public static final int CELL = 512;

    /** Size of a frame once it is scaled for the map (Player draws at 96 px). */
    public static final int DRAW_SIZE = 96;

    private final List<BufferedImage> frames = new ArrayList<>();
    private final String name;
    private final int cols, rows;

    /** Classic 4 cols x 2 rows sheet (idle, paddle-swing). */
    public SpriteSheet(String assetName) {
        this(assetName, COLS, ROWS);
    }

    /** Any grid: cols x rows frames, left-to-right then top-to-bottom. */
    public SpriteSheet(String assetName, int cols, int rows) {
        this.name = assetName;
        this.cols = cols;
        this.rows = rows;
        BufferedImage sheet = Assets.get(assetName);
        if (sheet == null || sheet.getWidth() < 1000 || sheet.getHeight() < 500) return;
        int cw = sheet.getWidth() / cols;
        int ch = sheet.getHeight() / rows;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                BufferedImage cell = sheet.getSubimage(c * cw, r * ch, cw, ch);
                // Scale once, now, with bicubic quality — never per frame.
                BufferedImage small = new BufferedImage(DRAW_SIZE, DRAW_SIZE, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = small.createGraphics();
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g.drawImage(cell, 0, 0, DRAW_SIZE, DRAW_SIZE, null);
                g.dispose();
                frames.add(small);
            }
        }
    }

    public boolean isValid() { return frames.size() == cols * rows; }
    public int getFrameCount() { return frames.size(); }
    public String getName() { return name; }

    /** Frame 0..(frameCount-1), left-to-right then top-to-bottom, already 96x96. Out of range wraps, so a bad index can never crash the game. */
    public BufferedImage getFrame(int i) {
        if (frames.isEmpty()) return null;
        return frames.get(Math.floorMod(i, frames.size()));
    }
}
