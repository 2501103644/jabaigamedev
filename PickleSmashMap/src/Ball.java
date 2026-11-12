/**
 * A ball IS-A CosmeticItem, plus its rarity. See CosmeticItem.java.
 *
 * NOTE — upload-only build: the uploaded zip has no ball image at all,
 * so imageFile is "none" and AvatarPanel draws the ball in code
 * (a coloured pickleball with holes) instead of loading a picture.
 */
public class Ball extends CosmeticItem {
    private final Rarity rarity;

    public Ball(String id, String name, Rarity rarity, boolean unlocked) {
        super(id, name, "none", rarity.unlockCost(), unlocked);
        this.rarity = rarity;
    }

    public Rarity getRarity() { return rarity; }

    @Override
    public String getKind() { return "Ball"; }
}
