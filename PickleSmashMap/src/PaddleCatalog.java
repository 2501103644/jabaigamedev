/** Factory for the 4 paddles. Drawn in code — the uploaded zip has no paddle images.
 * Names are from the final Avatar mockup: Classic, Pro Strike, Power+, Control. */
public final class PaddleCatalog {
    private PaddleCatalog() {}

    public static Catalog<Paddle> create() {
        Catalog<Paddle> c = new Catalog<>();
        c.add(new Paddle("paddle_classic", "Classic", Rarity.CLASSIC, true));
        c.add(new Paddle("paddle_elite", "Pro Strike", Rarity.ELITE, false));
        c.add(new Paddle("paddle_special", "Power+", Rarity.SPECIAL, false));
        c.add(new Paddle("paddle_epic", "Control", Rarity.EPIC, false));
        return c;
    }
}
