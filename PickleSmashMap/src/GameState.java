import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

/**
 * OOP lesson — ENCAPSULATION + COMPOSITION.
 *
 * Before, every field here was public, so any class could do
 * {@code state.coins = 999999} or {@code state.hp = -50} and break the game.
 * Now the fields are private and the ONLY way in is through methods that
 * guard the rules: spendCoins() refuses when there are not enough coins,
 * addXp() handles levelling up, setHp() can never go below 0.
 *
 * COMPOSITION: a GameState HAS-A Settings and HAS-A catalog for each kind
 * of cosmetic. It does not inherit from them — it owns them. The Avatar
 * screen asks the state for its catalog, it never builds its own copy,
 * so the home screen, the map and the avatar screen all see the same coins
 * and the same unlocked characters.
 *
 * Save file: picklesmash-save.properties, next to the game. Plain text,
 * so you can open it and see what was saved.
 *
 * Upload-only build: this file is the same design as the earlier build,
 * but the starting values (name "Player 1", 1200 coins) are exactly
 * the ones from the uploaded zip, and the catalogs hold only what
 * that zip contains (see AvatarCatalog / PaddleCatalog / BallCatalog).
 */
public class GameState {
    public static final String SAVE_FILE = "picklesmash-save.properties";

    private String playerName = "Player 1"; // as in the uploaded zip
    private int hp = 100;
    private int maxHp = 100;
    private int coins = 1200;
    private int stars = 6;
    private int maxStars = 6;
    private int level = 1;
    private int xp = 0;
    private int xpToNext = 100;

    private final Settings settings = new Settings();
    private final Catalog<Avatar> avatarCatalog = AvatarCatalog.create();
    private final Catalog<Paddle> paddleCatalog = PaddleCatalog.create();
    private final Catalog<Ball> ballCatalog = BallCatalog.create();

    private String selectedAvatarId = "classic";
    private String selectedPaddleId = "paddle_classic";
    private String selectedBallId = "ball_classic";

    public GameState() {
        load();
    }

    // ---- getters: read-only access, no direct writes from outside.
    public String getPlayerName() { return playerName; }
    public int getHp() { return hp; }
    public int getMaxHp() { return maxHp; }
    public int getCoins() { return coins; }
    public int getStars() { return stars; }
    public int getMaxStars() { return maxStars; }
    public int getLevel() { return level; }
    public int getXp() { return xp; }
    public int getXpToNext() { return xpToNext; }
    public Settings getSettings() { return settings; }
    public Catalog<Avatar> getAvatarCatalog() { return avatarCatalog; }
    public Catalog<Paddle> getPaddleCatalog() { return paddleCatalog; }
    public Catalog<Ball> getBallCatalog() { return ballCatalog; }
    public String getSelectedAvatarId() { return selectedAvatarId; }
    public String getSelectedPaddleId() { return selectedPaddleId; }
    public String getSelectedBallId() { return selectedBallId; }

    public Avatar getSelectedAvatar() {
        Avatar a = avatarCatalog.getById(selectedAvatarId); // O(1) HashMap lookup
        return a != null ? a : avatarCatalog.getById("classic");
    }
    public Paddle getSelectedPaddle() {
        Paddle p = paddleCatalog.getById(selectedPaddleId);
        return p != null ? p : paddleCatalog.getById("paddle_classic");
    }
    public Ball getSelectedBall() {
        Ball b = ballCatalog.getById(selectedBallId);
        return b != null ? b : ballCatalog.getById("ball_classic");
    }

    // ---- guarded writes.
    public void setPlayerName(String name) {
        if (name != null && !name.isBlank()) playerName = name.trim();
    }
    public void setHp(int value) { hp = Math.max(0, Math.min(maxHp, value)); }
    public void restoreHp() { hp = maxHp; }
    public void addCoins(int amount) { if (amount > 0) coins += amount; }

    /** Returns false (and changes nothing) when the player cannot afford it. */
    public boolean spendCoins(int amount) {
        if (amount < 0 || coins < amount) return false;
        coins -= amount;
        return true;
    }

    public void addXp(int amount) {
        if (amount <= 0) return;
        xp += amount;
        while (xp >= xpToNext) {
            xp -= xpToNext;
            level++;
        }
    }

    public void selectAvatar(String id) {
        Avatar a = avatarCatalog.getById(id);
        if (a != null && a.isUnlocked()) selectedAvatarId = id;
    }
    public void selectPaddle(String id) {
        Paddle p = paddleCatalog.getById(id);
        if (p != null && p.isUnlocked()) selectedPaddleId = id;
    }
    public void selectBall(String id) {
        Ball b = ballCatalog.getById(id);
        if (b != null && b.isUnlocked()) selectedBallId = id;
    }

    /**
     * Unlock + select in one guarded step: free items just select,
     * paid items only unlock when spendCoins() succeeds.
     * Returns true when the item is now selected.
     */
    public boolean unlockAndSelect(CosmeticItem item) {
        if (item == null) return false;
        if (!item.isUnlocked()) {
            if (!spendCoins(item.getCost())) return false;
            item.unlock();
        }
        if (item instanceof Avatar a) selectedAvatarId = a.getId();
        else if (item instanceof Paddle p) selectedPaddleId = p.getId();
        else if (item instanceof Ball b) selectedBallId = b.getId();
        return true;
    }

    /** Reset progress (Settings screen). Locks everything except the free items. */
    public void resetProgress() {
        coins = 1200;
        level = 1;
        xp = 0;
        hp = maxHp;
        stars = 6;
        selectedAvatarId = "classic";
        selectedPaddleId = "paddle_classic";
        selectedBallId = "ball_classic";
        for (Catalog<? extends CosmeticItem> c : new Catalog[]{avatarCatalog, paddleCatalog, ballCatalog}) {
            for (CosmeticItem item : c.getAll()) {
                if (item.getCost() == 0) item.unlock(); else item.lock();
            }
        }
    }

    // ---- save / load.
    public void save() {
        Properties p = new Properties();
        p.setProperty("playerName", playerName);
        p.setProperty("coins", String.valueOf(coins));
        p.setProperty("level", String.valueOf(level));
        p.setProperty("xp", String.valueOf(xp));
        p.setProperty("hp", String.valueOf(hp));
        p.setProperty("stars", String.valueOf(stars));
        p.setProperty("avatar", selectedAvatarId);
        p.setProperty("paddle", selectedPaddleId);
        p.setProperty("ball", selectedBallId);
        settings.saveTo(p);
        for (Catalog<? extends CosmeticItem> c : new Catalog[]{avatarCatalog, paddleCatalog, ballCatalog}) {
            for (CosmeticItem item : c.getAll()) {
                if (item.isUnlocked()) p.setProperty("unlocked." + item.getId(), "true");
            }
        }
        try (FileOutputStream out = new FileOutputStream(SAVE_FILE)) {
            p.store(out, "Pickle Smash save — edit coins / unlocks here if you need to test");
        } catch (Exception e) {
            System.err.println("Could not save: " + e.getMessage());
        }
    }

    public void load() {
        File f = new File(SAVE_FILE);
        if (!f.isFile()) return;
        Properties p = new Properties();
        try (FileInputStream in = new FileInputStream(f)) {
            p.load(in);
        } catch (Exception e) {
            System.err.println("Could not load save: " + e.getMessage());
            return;
        }
        setPlayerName(p.getProperty("playerName", playerName));
        coins = Math.max(0, parse(p, "coins", coins));
        level = Math.max(1, parse(p, "level", level));
        xp = Math.max(0, parse(p, "xp", xp));
        hp = Math.max(0, parse(p, "hp", hp));
        stars = Math.max(0, parse(p, "stars", stars));
        settings.loadFrom(p);
        selectedAvatarId = p.getProperty("avatar", selectedAvatarId);
        selectedPaddleId = p.getProperty("paddle", selectedPaddleId);
        selectedBallId = p.getProperty("ball", selectedBallId);
        for (Catalog<? extends CosmeticItem> c : new Catalog[]{avatarCatalog, paddleCatalog, ballCatalog}) {
            for (CosmeticItem item : c.getAll()) {
                if ("true".equals(p.getProperty("unlocked." + item.getId()))) item.unlock();
            }
        }
    }

    private static int parse(Properties p, String key, int fallback) {
        try { return Integer.parseInt(p.getProperty(key, String.valueOf(fallback))); }
        catch (NumberFormatException e) { return fallback; }
    }
}
