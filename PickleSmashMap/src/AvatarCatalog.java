/**
 * Factory for the avatars in THIS build.
 *
 * The portraits are cut from the final Avatar mockup
 * (assets/mockup_*.png) — stand-ins until Jeruld uploads the final
 * assets. Classic (free), Elite (400), Special (700), Epic (1000),
 * in the mockup's order. Elite is female in the mockup.
 * This is the ONLY file that needs to change to use the final
 * assets: change the image file name per character here.
 *
 * MAP SPRITES: each avatar also carries a full-body sprite for the
 * city map (assets/avatars/*.png), cut from the avatar sheet Jeruld
 * sent — the same sheet he re-sent in this chat. The portrait alone
 * could not walk on the map (it is head-and-shoulders), so the map
 * uses this file instead. Classic = Male Classic, Elite = Female
 * Elite (female in the mockup), Special = Male Special, Epic =
 * Male Epic. Change one string per character here to swap in a
 * final sprite; Player.java, GamePanel.java and AvatarPanel.java
 * need no change.
 */
public final class AvatarCatalog {
    private AvatarCatalog() {}

    public static Catalog<Avatar> create() {
        Catalog<Avatar> c = new Catalog<>();
        c.add(new Avatar("classic", "Classic", Avatar.Gender.MALE, Rarity.CLASSIC, true, "mockup_classic", "avatars/male_classic"));
        c.add(new Avatar("elite", "Elite", Avatar.Gender.FEMALE, Rarity.ELITE, false, "mockup_elite", "avatars/female_elite"));
        c.add(new Avatar("special", "Special", Avatar.Gender.MALE, Rarity.SPECIAL, false, "mockup_special", "avatars/male_special"));
        c.add(new Avatar("epic", "Epic", Avatar.Gender.MALE, Rarity.EPIC, false, "mockup_epic", "avatars/male_epic"));
        return c;
    }
}
