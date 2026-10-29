/** Player progress shown in the HUD. Hook your real game data in here. */
public class GameState {
    public String playerName = "Player 1";
    public int hp = 100, maxHp = 100;
    public int coins = 1200;
    public int stars = 6, maxStars = 6;
    public int level = 1, xp = 0, xpToNext = 100;

    public void addXp(int amount) {
        xp += amount;
        while (xp >= xpToNext) {
            xp -= xpToNext;
            level++;
        }
    }
}
