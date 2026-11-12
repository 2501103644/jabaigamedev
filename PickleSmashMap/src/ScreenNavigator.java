/**
 * OOP lesson — INTERFACE: a contract, not a class.
 * The menu panels do not know about JFrame, CardLayout or the ScreenManager.
 * They only know "someone can show these screens and go back". That keeps
 * the panels reusable and testable, and Main can swap the navigator later
 * without touching any panel.
 */
public interface ScreenNavigator {
    void showHome();
    void showAvatar();
    void showSettings();
    void showCredits();
    void showMap();
    void goBack();
}
