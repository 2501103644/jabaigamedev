import java.awt.Point;
import java.awt.Rectangle;

/** One place on the map (a city block with a building, a label and an entrance). */
public class Location {
    public enum Kind { HOME, TRAINING, LEVEL }

    public final String name;
    public final Kind kind;
    /** The city block this location sits on (map coordinates). */
    public final Rectangle block;
    /** Sprite name in assets/ (null = building is drawn in code). */
    public final String sprite;
    /** Icon shown in the label pill. */
    public final String pinIcon;
    /** Spot on the street where the player can enter. */
    public final Point entrance;
    public boolean locked;
    /** Filled in by GamePanel: where the label was drawn (for mouse clicks). */
    public final Rectangle labelRect = new Rectangle();

    /** Collision rect (usually the same as block). For Home it is the block minus
     *  the front walkway, so the player can stand in front of the house as in the
     *  exact-image map. */
    public final Rectangle solid;
    /** Label pill y in world coords (-1 = default: just inside the top of the block). */
    public final int labelY;
    /** Label pill minimum width in world coords (0 = auto width from the text). */
    public final int labelW;
    /** Label pill x in world coords (-1 = centred on the block). Needed because the
     *  mockup's baked pills are not centred on their blocks. */
    public final int labelX;

    public Location(String name, Kind kind, Rectangle block, String sprite,
                    String pinIcon, Point entrance, boolean locked) {
        this(name, kind, block, sprite, pinIcon, entrance, locked, block, -1, 0, -1);
    }

    public Location(String name, Kind kind, Rectangle block, String sprite,
                    String pinIcon, Point entrance, boolean locked,
                    Rectangle solid, int labelY, int labelW) {
        this(name, kind, block, sprite, pinIcon, entrance, locked, solid, labelY, labelW, -1);
    }

    public Location(String name, Kind kind, Rectangle block, String sprite,
                    String pinIcon, Point entrance, boolean locked,
                    Rectangle solid, int labelY, int labelW, int labelX) {
        this.name = name;
        this.kind = kind;
        this.block = block;
        this.sprite = sprite;
        this.pinIcon = pinIcon;
        this.entrance = entrance;
        this.locked = locked;
        this.solid = solid;
        this.labelY = labelY;
        this.labelW = labelW;
        this.labelX = labelX;
    }

    /** True if this location shows a padlock in its label. */
    public boolean hasLock() {
        return kind != Kind.HOME;
    }
}
