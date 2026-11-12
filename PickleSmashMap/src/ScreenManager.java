import javax.swing.*;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * DSA lesson — STACK for screen history.
 *
 * Every screen you open is PUSHED onto a stack (ArrayDeque used as a stack).
 * BACK is a POP: it removes the top screen and shows the one underneath.
 * That is exactly how a phone's back button works, and why BACK from
 * Avatar always returns to Home — Home is underneath it on the stack.
 * Push = O(1), pop = O(1).
 *
 * This class is the ONLY place that swaps the JFrame's content pane,
 * so navigation can never get out of sync between panels.
 */
public class ScreenManager implements ScreenNavigator {
    private final JFrame frame;
    private final GameState state;
    private final Deque<JPanel> history = new ArrayDeque<>();
    private JPanel current;

    public ScreenManager(JFrame frame, GameState state) {
        this.frame = frame;
        this.state = state;
    }

    private void show(JPanel panel, String title) {
        if (current != null) history.push(current); // push the screen we are leaving
        current = panel;
        frame.setContentPane(panel);
        frame.setTitle(title);
        frame.pack();
        frame.setLocationRelativeTo(null);
        panel.requestFocusInWindow();
    }

    @Override public void showHome() { show(new HomeMenuPanel(state, this), "Pickle Smash - The Ultimate Paddle Challenge"); }
    @Override public void showAvatar() { show(new AvatarPanel(state, this), "Pickle Smash - Avatar"); }
    @Override public void showSettings() { show(new SettingsPanel(state, this), "Pickle Smash - Settings"); }
    @Override public void showCredits() { show(new CreditsPanel(state, this), "Pickle Smash - Credits"); }
    @Override public void showMap() { show(new GamePanel(state), "Pickle Smash - City Map"); }

    @Override
    public void goBack() {
        if (history.isEmpty()) { showHome(); return; }
        current = history.pop(); // pop the screen underneath and show it
        frame.setContentPane(current);
        frame.setTitle("Pickle Smash - The Ultimate Paddle Challenge");
        frame.pack();
        frame.setLocationRelativeTo(null);
        current.requestFocusInWindow();
    }
}
