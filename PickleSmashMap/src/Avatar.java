/**
 * OOP lesson — INHERITANCE: an Avatar IS-A CosmeticItem, plus its own
 * gender and rarity. Everything shared (id, name, cost, unlocked)
 * comes from the parent; this class only adds what is avatar-only.
 *
 * NOTE — the portraits are cut from the final Avatar mockup
 * (assets/mockup_classic.png etc.) as STAND-INS, because the zip
 * Jeruld uploaded contains only ONE character sprite
 * (assets/player_classic.png). When Jeruld uploads the final
 * assets, put their PNGs in assets/ and change the image file in
 * AvatarCatalog to the new name — nothing else needs to change.
 *
 * MAP SPRITE: the portrait (imageFile) is head-and-shoulders, so it
 * cannot walk around the map. mapSpriteFile is the FULL-BODY sprite
 * used on the city map instead — cut from the avatar sheet Jeruld
 * sent (assets/avatars/male_classic.png etc.). The map player
 * (Player.java) draws this sprite; if it is ever missing, Player
 * falls back to the old code-drawn chibi, so the map never breaks.
 */
public class Avatar extends CosmeticItem {
    public enum Gender { MALE, FEMALE }

    private final Gender gender;
    private final Rarity rarity;
    private final String mapSpriteFile;

    public Avatar(String id, String name, Gender gender, Rarity rarity, boolean unlocked, String imageFile) {
        this(id, name, gender, rarity, unlocked, imageFile, "player_classic");
    }

    public Avatar(String id, String name, Gender gender, Rarity rarity, boolean unlocked,
                  String imageFile, String mapSpriteFile) {
        // The price comes from the rarity, so price and rarity can never disagree.
        super(id, name, imageFile, rarity.unlockCost(), unlocked);
        this.gender = gender;
        this.rarity = rarity;
        this.mapSpriteFile = mapSpriteFile;
    }

    public Gender getGender() { return gender; }
    public Rarity getRarity() { return rarity; }

    /** Full-body sprite for the map, e.g. "avatars/male_classic". */
    public String getMapSpriteFile() { return mapSpriteFile; }

    @Override
    public String getKind() { return "Avatar"; }
}
