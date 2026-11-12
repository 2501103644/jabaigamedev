/**
 * OOP lesson — ENUM: a fixed set of values, each carrying its own data.
 * Every tier knows its own unlock price, so no screen has to remember
 * "Elite costs 400" in a magic number. Change the price here, once,
 * and every screen that uses it changes too.
 */
public enum Rarity {
    CLASSIC("Classic", 0),
    ELITE("Elite", 400),
    SPECIAL("Special", 700),
    EPIC("Epic", 1000);

    private final String label;
    private final int unlockCost;

    Rarity(String label, int unlockCost) {
        this.label = label;
        this.unlockCost = unlockCost;
    }

    public String label() { return label; }
    public int unlockCost() { return unlockCost; }
}
