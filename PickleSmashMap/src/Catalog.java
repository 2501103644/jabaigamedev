import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DSA lesson — GENERICS + two data structures working together.
 *
 * A Catalog<T> works for ANY CosmeticItem (Avatar, Paddle, Ball) because
 * of the generic type {@code <T extends CosmeticItem>}. One class, three uses.
 *
 * Inside it keeps the SAME items in two structures, for two different jobs:
 *   1. ArrayList<T> items  — keeps ORDER, so we can draw a grid, sort it,
 *      and walk through it one by one. Sorting / scanning is O(n log n) / O(n).
 *   2. HashMap<String, T> byId — finds ONE item by its id in O(1), no loop.
 *      The Avatar screen uses this every time the player picks a character:
 *      {@code getById("male_epic")} instead of looping through all 10.
 *
 * Keeping both in sync is this class's job — callers never touch them directly.
 */
public class Catalog<T extends CosmeticItem> {
    private final List<T> items = new ArrayList<>();
    private final Map<String, T> byId = new HashMap<>();

    public void add(T item) {
        items.add(item);
        byId.put(item.getId(), item);
    }

    /** O(1) lookup by id — the HashMap. Returns null when there is no such id. */
    public T getById(String id) {
        return byId.get(id);
    }

    /** The items in their current order. Read-only view, so callers cannot break the map. */
    public List<T> getAll() {
        return Collections.unmodifiableList(items);
    }

    public int size() { return items.size(); }

    /** DSA — SORTING with a Comparator. Returns a NEW list, the catalog itself stays untouched. */
    public List<T> sortedByCost() {
        List<T> copy = new ArrayList<>(items);
        copy.sort(Comparator.comparingInt(CosmeticItem::getCost));
        return copy;
    }

    /** Sorted alphabetically by name — the list binary search needs. */
    public List<T> sortedByName() {
        List<T> copy = new ArrayList<>(items);
        copy.sort(Comparator.comparing(CosmeticItem::getName, String.CASE_INSENSITIVE_ORDER));
        return copy;
    }

    /**
     * DSA — BINARY SEARCH, O(log n).
     * The list MUST be sorted by name first (sortedByName() does that).
     * Each step throws away half of what is left, so 10 items take at most 4 steps.
     * Returns the item, or null when no name matches.
     */
    public T findByName(String name) {
        List<T> sorted = sortedByName();
        int found = Collections.binarySearch(sorted,
                new SearchKey(name),
                (a, b) -> a.getName().compareToIgnoreCase(b.getName()));
        return found >= 0 ? sorted.get(found) : null;
    }

    /** Only the unlocked items, in order — a simple LINEAR SCAN, O(n). */
    public List<T> getUnlocked() {
        List<T> out = new ArrayList<>();
        for (T item : items) if (item.isUnlocked()) out.add(item);
        return out;
    }

    // Tiny helper so binarySearch can compare a plain name against an item.
    private static final class SearchKey extends CosmeticItem {
        SearchKey(String name) { super("", name, "", 0, false); }
        @Override public String getKind() { return "Search"; }
    }
}
