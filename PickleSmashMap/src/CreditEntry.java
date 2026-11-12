/**
 * One line of the Credits screen. A record is Java's shortest class:
 * it automatically gives you role(), name(), equals() and toString().
 * The credits list is a List<CreditEntry> — an ArrayList in CreditsPanel.
 */
public record CreditEntry(String role, String name) {}
