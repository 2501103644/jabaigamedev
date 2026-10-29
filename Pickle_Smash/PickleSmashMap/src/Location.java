import java.awt.Point;
import java.awt.Rectangle;

/** One place on the map (a city block with a building, a label and an entrance). */
public class Location {
    public enum Kind { HOME, TRAINING, STORE, TWO_PLAYER, LEVEL }

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

    public Location(String name, Kind kind, Rectangle block, String sprite,
                    String pinIcon, Point entrance, boolean locked) {
        this.name = name;
        this.kind = kind;
        this.block = block;
        this.sprite = sprite;
        this.pinIcon = pinIcon;
        this.entrance = entrance;
        this.locked = locked;
    }

    /** True if this location shows a padlock in its label. */
    public boolean hasLock() {
        return kind != Kind.HOME && kind != Kind.TWO_PLAYER;
    }
}
