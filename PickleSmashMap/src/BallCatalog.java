/** Factory for the 4 balls. Drawn in code — the uploaded zip has no ball images.
 * Names are from the final Avatar mockup: Classic, Speed, Bounce+, Training. */
public final class BallCatalog {
    private BallCatalog() {}

    public static Catalog<Ball> create() {
        Catalog<Ball> c = new Catalog<>();
        c.add(new Ball("ball_classic", "Classic", Rarity.CLASSIC, true));
        c.add(new Ball("ball_elite", "Speed", Rarity.ELITE, false));
        c.add(new Ball("ball_special", "Bounce+", Rarity.SPECIAL, false));
        c.add(new Ball("ball_epic", "Training", Rarity.EPIC, false));
        return c;
    }
}
