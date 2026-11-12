/**
 * A paddle IS-A CosmeticItem, plus its rarity. See CosmeticItem.java.
 *
 * NOTE — upload-only build: the uploaded zip has no paddle image at all,
 * so imageFile is "none" and AvatarPanel draws the paddle in code
 * (a coloured paddle shape) instead of loading a picture.
 * Drop a paddle PNG into assets/ and set the file name here to use it.
 */
public class Paddle extends CosmeticItem {
    private final Rarity rarity;

    public Paddle(String id, String name, Rarity rarity, boolean unlocked) {
        super(id, name, "none", rarity.unlockCost(), unlocked);
        this.rarity = rarity;
    }

    public Rarity getRarity() { return rarity; }

    @Override
    public String getKind() { return "Paddle"; }
}
