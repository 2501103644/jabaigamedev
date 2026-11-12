import javax.swing.*;

/**
 * Starts Pickle Smash. One GameState is created here and shared with
 * every screen (COMPOSITION), and one ScreenManager owns navigation
 * (STACK — see ScreenManager.java). No screen creates its own state,
 * so coins / unlocks / settings never disagree between screens.
 *
 * Built ONLY from the zip Jeruld uploaded (PickleSmash_HomeScreen__1.zip):
 * no files from the earlier workspace build were used.
 */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Pickle Smash - The Ultimate Paddle Challenge");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            GameState state = new GameState(); // loads picklesmash-save.properties if it exists
            ScreenManager navigator = new ScreenManager(frame, state);
            navigator.showHome();
            frame.setMinimumSize(new java.awt.Dimension(960, 560));
            frame.setVisible(true);
        });
    }
}
