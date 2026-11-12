/**
 * OOP lesson — ABSTRACTION + INHERITANCE.
 * Avatar, Paddle and Ball are different things, but they share the same
 * idea: an id, a name, a picture, a price, and locked / unlocked.
 * That shared idea lives here once, in the abstract parent class.
 * The children only add what makes them different (gender, rarity...).
 * No child can be created directly from this class — it is abstract.
 */
public abstract class CosmeticItem {
    // ENCAPSULATION: fields are private. Other classes must use the
    // getters / unlock() below, so nobody can set coins or unlocks
    // by reaching straight into the object.
    private final String id;          // e.g. "male_classic" — also the file name
    private final String name;        // e.g. "Male Classic"
    private final String imageFile;   // e.g. "avatars/male_classic"
    private final int cost;           // coins to unlock, 0 = free
    private boolean unlocked;

    protected CosmeticItem(String id, String name, String imageFile, int cost, boolean unlocked) {
        this.id = id;
        this.name = name;
        this.imageFile = imageFile;
        this.cost = cost;
        this.unlocked = unlocked;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getImageFile() { return imageFile; }
    public int getCost() { return cost; }
    public boolean isUnlocked() { return unlocked; }

    /** Only the game logic may unlock an item, and only through here. */
    public void unlock() { unlocked = true; }
    public void lock() { unlocked = false; }

    /** Each child says what kind of item it is — POLYMORPHISM. */
    public abstract String getKind();
}
