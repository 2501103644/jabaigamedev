import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.*;

/**
 * SETTINGS screen — LAYOUT 1, improved UI (2026-11-07).
 *
 * Tall centred panel, Music Volume on top, Sound Effects Volume below
 * it, both with big percentage numbers, then two toggles:
 * Mute All and Fullscreen. Nothing else — no Vibration, Language,
 * Graphics Quality, Controls or Reset Progress.
 *
 * UI IMPROVEMENTS in this version (all in this file + BaseMenuPanel):
 *  1. Clearer hierarchy: a "GAME OPTIONS" header with a gold divider,
 *     a thin divider line between every row, and a small description
 *     under Mute All / Fullscreen, so each row reads as one thing.
 *  2. Icon badges: every row starts with a gold circle holding its
 *     icon (♪ 🔊 🔇 ⛶), so you can scan the screen by icon, not text.
 *  3. Better sliders: thicker track, gold gradient fill, a bigger
 *     knob with a highlight and a soft shadow. The knob grows a
 *     little when the mouse is over it, so it is obvious it can
 *     be dragged. The percentage is bigger and has a soft shadow.
 *  4. Better toggles: a wider pill, a knob with a shadow, ON in gold
 *     and OFF in grey — the state is readable from across the room.
 *  5. Muted state is unmistakable: dimmed sliders, a MUTED pill
 *     instead of a percentage, and a note saying the volume was kept.
 *  6. Keyboard shortcuts: M = Mute All, F = Fullscreen, Esc = Back.
 *     They are printed at the bottom of the panel, so players find them.
 *  7. The shared panel, title and wooden buttons in BaseMenuPanel
 *     were polished too (drop shadow, gradient panel, brighter hover),
 *     so Avatar and Credits got the same upgrade for free.
 *
 * How the two options work:
 * - Mute All: one switch silences everything. The two volumes are
 *   NOT set to 0 — they are kept, so un-muting brings the sound back
 *   exactly as it was (see Settings.effectiveMusicVolume()). While
 *   muted the sliders are drawn dimmed; clicking one un-mutes first.
 * - Fullscreen: flips the real JFrame to maximised (and back to a
 *   1280 x 720 window when turned off). See applyFullscreen().
 */
public class SettingsPanel extends BaseMenuPanel implements KeyListener {
    /** Tall centred panel, like Layout 1 in the mockup. */
    private static final Rectangle PANEL = new Rectangle(340, 100, 600, 500);
    private static final Rectangle SAVE_RECT = new Rectangle(1046, 644, 210, 58);

    private static final int MUSIC_ROW_Y = 150;
    private static final int SFX_ROW_Y = 270;
    private static final int MUTE_ROW_Y = 390;
    private static final int FULLSCREEN_ROW_Y = 480;

    /** Which slider (0 = music, 1 = sfx) is being dragged, or -1. */
    private int draggingSlider = -1;
    /** Which slider / toggle the mouse is over, for the hover grow / glow. */
    private int hoverSlider = -1;
    private int hoverToggle = -1;
    private boolean hoverSave;

    public SettingsPanel(GameState state, ScreenNavigator navigator) {
        super(state, navigator);
        addKeyListener(this);
        // Apply the saved fullscreen choice once this panel is on a frame.
        SwingUtilities.invokeLater(this::applyFullscreen);
    }

    private Rectangle sliderTrack(int rowY) {
        return new Rectangle(PANEL.x + 40, rowY + 52, 360, 14);
    }

    private Rectangle toggleRect(int rowY) {
        return new Rectangle(PANEL.x + 420, rowY + 8, 100, 40);
    }

    private int sliderValueFromX(Rectangle track, int x) {
        double frac = (x - track.x) / (double) track.width;
        return (int) Math.round(Math.max(0, Math.min(1, frac)) * 100);
    }

    /** Fullscreen ON = maximise the real window; OFF = back to 1280 x 720. */
    public void applyFullscreen() {
        java.awt.Window window = SwingUtilities.getWindowAncestor(this);
        if (!(window instanceof JFrame frame)) return; // headless test: no frame, just save the setting
        if (state.getSettings().isFullscreen()) {
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        } else {
            frame.setExtendedState(JFrame.NORMAL);
            frame.setSize(1280, 720);
            frame.setLocationRelativeTo(null);
        }
    }

    @Override
    protected void drawContent(Graphics2D g) {
        drawTitle(g, "SETTINGS");
        drawPanel(g, PANEL);

        // Header + gold divider, so the panel reads as one titled card.
        g.setFont(new Font("SansSerif", Font.BOLD, 18));
        g.setColor(GOLD);
        g.drawString("GAME OPTIONS", PANEL.x + 40, PANEL.y + 30);
        g.setColor(new Color(255, 214, 90, 180));
        g.setStroke(new BasicStroke(2f));
        g.drawLine(PANEL.x + 40, PANEL.y + 40, PANEL.x + PANEL.width - 40, PANEL.y + 40);

        Settings s = state.getSettings();
        drawSliderRow(g, MUSIC_ROW_Y, 0, "♪", "Music Volume", s.getMusicVolume(), s.isMuteAll());
        drawDivider(g, MUSIC_ROW_Y + 108);
        drawSliderRow(g, SFX_ROW_Y, 1, "🔊", "Sound Effects Volume", s.getSfxVolume(), s.isMuteAll());
        drawDivider(g, SFX_ROW_Y + 108);
        drawToggleRow(g, MUTE_ROW_Y, 0, "🔇", "Mute All", "Silence music and sound effects", s.isMuteAll());
        drawDivider(g, MUTE_ROW_Y + 78);
        drawToggleRow(g, FULLSCREEN_ROW_Y, 1, "⛶", "Fullscreen", "Fill the whole screen", s.isFullscreen());

        // Keyboard hints, printed where players can find them.
        g.setFont(new Font("SansSerif", Font.PLAIN, 14));
        g.setColor(new Color(255, 255, 255, 170));
        String hints = "M — Mute    •    F — Fullscreen    •    Esc — Back";
        g.drawString(hints, PANEL.x + (PANEL.width - g.getFontMetrics().stringWidth(hints)) / 2, PANEL.y + PANEL.height - 14);

        drawWoodenButton(g, SAVE_RECT, "✓  SAVE", hoverSave);
    }

    private void drawDivider(Graphics2D g, int y) {
        g.setColor(new Color(255, 255, 255, 45));
        g.setStroke(new BasicStroke(1f));
        g.drawLine(PANEL.x + 40, y, PANEL.x + PANEL.width - 40, y);
    }

    /** Gold circle with a dark icon inside — the row's visual anchor. */
    private void drawIconBadge(Graphics2D g, int cx, int cy, String icon) {
        g.setColor(GOLD);
        g.fillOval(cx - 19, cy - 19, 38, 38);
        g.setColor(new Color(0x8A5A00));
        g.setFont(new Font("SansSerif", Font.BOLD, 20));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(icon, cx - fm.stringWidth(icon) / 2, cy + 7);
    }

    private void drawSliderRow(Graphics2D g, int rowY, int index, String icon, String label, int value, boolean muted) {
        drawIconBadge(g, PANEL.x + 59, rowY + 16, icon);
        g.setFont(new Font("SansSerif", Font.BOLD, 24));
        g.setColor(muted ? new Color(255, 255, 255, 130) : Color.WHITE);
        // Soft shadow under the label, so white text stays readable on the panel.
        g.setColor(new Color(0, 0, 0, 90));
        g.drawString(label, PANEL.x + 83, rowY + 25);
        g.setColor(muted ? new Color(255, 255, 255, 130) : Color.WHITE);
        g.drawString(label, PANEL.x + 82, rowY + 24);

        Rectangle t = sliderTrack(rowY);
        // Track: dark groove, then the gold gradient fill up to the knob.
        g.setColor(new Color(0, 0, 0, 110));
        g.fillRoundRect(t.x, t.y, t.width, t.height, 7, 7);
        g.setColor(new Color(255, 255, 255, muted ? 25 : 55));
        g.fillRoundRect(t.x, t.y, t.width, t.height, 7, 7);
        if (value > 0) {
            g.setPaint(new GradientPaint(t.x, t.y, new Color(0xFFE082),
                    t.x + t.width, t.y, muted ? new Color(0x9A9A9A) : new Color(0xE8A800)));
            g.fillRoundRect(t.x, t.y, (int) (t.width * value / 100.0), t.height, 7, 7);
        }
        // Knob: bigger on hover, with a shadow and a white highlight.
        int knobD = hoverSlider == index ? 40 : 36;
        int cx = t.x + (int) (t.width * value / 100.0);
        int cy = t.y + t.height / 2;
        g.setColor(new Color(0, 0, 0, 90));
        g.fillOval(cx - knobD / 2, cy - knobD / 2 + 3, knobD, knobD);
        g.setPaint(new GradientPaint(cx - knobD / 2f, cy - knobD / 2f, new Color(0xFFF3C4),
                cx + knobD / 2f, cy + knobD / 2f, muted ? new Color(0xB0B0B0) : new Color(0xE8A800)));
        g.fillOval(cx - knobD / 2, cy - knobD / 2, knobD, knobD);
        g.setColor(new Color(255, 255, 255, 180));
        g.fillOval(cx - knobD / 4, cy - knobD / 4 - 2, knobD / 3, knobD / 4);

        if (muted) {
            // MUTED pill instead of a percentage — unmistakable at a glance.
            String pct = "MUTED";
            g.setFont(new Font("SansSerif", Font.BOLD, 22));
            int w = g.getFontMetrics().stringWidth(pct) + 28;
            g.setColor(new Color(0, 0, 0, 120));
            g.fillRoundRect(PANEL.x + 440, rowY + 38, w, 36, 18, 18);
            g.setColor(GOLD);
            g.setStroke(new BasicStroke(2f));
            g.drawRoundRect(PANEL.x + 440, rowY + 38, w, 36, 18, 18);
            g.setColor(Color.WHITE);
            g.drawString(pct, PANEL.x + 454, rowY + 63);
            g.setFont(new Font("SansSerif", Font.PLAIN, 14));
            g.setColor(new Color(255, 255, 255, 170));
            g.drawString("Volume kept: " + value + "% — un-mute to hear it again", PANEL.x + 40, rowY + 94);
        } else {
            // Big percentage with a soft shadow, like Layout 1 in the mockup.
            g.setFont(new Font("SansSerif", Font.BOLD, 40));
            g.setColor(new Color(0, 0, 0, 90));
            g.drawString(value + "%", PANEL.x + 442, rowY + 64);
            g.setColor(GOLD);
            g.drawString(value + "%", PANEL.x + 440, rowY + 62);
        }
    }

    private void drawToggleRow(Graphics2D g, int rowY, int index, String icon, String label, String description, boolean on) {
        drawIconBadge(g, PANEL.x + 59, rowY + 24, icon);
        g.setFont(new Font("SansSerif", Font.BOLD, 24));
        g.setColor(Color.WHITE);
        g.drawString(label, PANEL.x + 82, rowY + 32);
        g.setFont(new Font("SansSerif", Font.PLAIN, 14));
        g.setColor(new Color(255, 255, 255, 170));
        g.drawString(description, PANEL.x + 82, rowY + 54);

        Rectangle t = toggleRect(rowY);
        if (hoverToggle == index) {
            g.setColor(new Color(255, 200, 60, 70));
            g.setStroke(new BasicStroke(6f));
            g.drawRoundRect(t.x - 3, t.y - 3, t.width + 6, t.height + 6, 22, 22);
        }
        // Pill: gold when ON, grey when OFF, with a soft shadow under the knob.
        g.setColor(on ? new Color(0xFFD65A) : new Color(255, 255, 255, 55));
        g.fillRoundRect(t.x, t.y, t.width, t.height, 20, 20);
        g.setColor(on ? new Color(0x8A5A00) : new Color(255, 255, 255, 90));
        g.setStroke(new BasicStroke(1.5f));
        g.drawRoundRect(t.x, t.y, t.width, t.height, 20, 20);
        int knobD = 32;
        int knobX = on ? t.x + t.width - knobD - 4 : t.x + 4;
        g.setColor(new Color(0, 0, 0, 90));
        g.fillOval(knobX, t.y + 6, knobD, knobD);
        g.setColor(Color.WHITE);
        g.fillOval(knobX, t.y + 4, knobD, knobD);
        g.setColor(new Color(255, 255, 255, 200));
        g.fillOval(knobX + 6, t.y + 8, 10, 8);
        g.setFont(new Font("SansSerif", Font.BOLD, 22));
        g.setColor(on ? GOLD : new Color(255, 255, 255, 160));
        g.drawString(on ? "ON" : "OFF", t.x + t.width + 14, rowY + 34);
    }

    @Override
    protected boolean onContentClick(Point p) {
        Settings s = state.getSettings();
        if (SAVE_RECT.contains(p)) {
            state.save();
            showToast("Settings saved!");
            if (navigator != null) navigator.goBack();
            return true;
        }
        // Sliders: a click jumps straight to that value; dragging is handled in onContentDrag.
        // Clicking a slider while muted un-mutes first, so the volume you pick is heard.
        Rectangle musicTrack = sliderTrack(MUSIC_ROW_Y);
        Rectangle sfxTrack = sliderTrack(SFX_ROW_Y);
        if (new Rectangle(musicTrack.x - 16, musicTrack.y - 16, musicTrack.width + 32, musicTrack.height + 32).contains(p)) {
            if (s.isMuteAll()) s.setMuteAll(false);
            s.setMusicVolume(sliderValueFromX(musicTrack, p.x));
            draggingSlider = 0;
            return true;
        }
        if (new Rectangle(sfxTrack.x - 16, sfxTrack.y - 16, sfxTrack.width + 32, sfxTrack.height + 32).contains(p)) {
            if (s.isMuteAll()) s.setMuteAll(false);
            s.setSfxVolume(sliderValueFromX(sfxTrack, p.x));
            draggingSlider = 1;
            return true;
        }
        if (toggleRect(MUTE_ROW_Y).contains(p)) {
            s.toggleMuteAll();
            showToast(s.isMuteAll() ? "Muted — volumes kept, just silenced" : "Sound back on");
            return true;
        }
        if (toggleRect(FULLSCREEN_ROW_Y).contains(p)) {
            s.toggleFullscreen();
            applyFullscreen();
            showToast(s.isFullscreen() ? "Fullscreen on" : "Windowed — 1280 x 720");
            return true;
        }
        return false;
    }

    @Override
    protected void onContentDrag(Point p) {
        if (draggingSlider < 0) return;
        Settings s = state.getSettings();
        if (s.isMuteAll()) s.setMuteAll(false);
        Rectangle track = draggingSlider == 0 ? sliderTrack(MUSIC_ROW_Y) : sliderTrack(SFX_ROW_Y);
        int v = sliderValueFromX(track, p.x);
        if (draggingSlider == 0) s.setMusicVolume(v);
        else s.setSfxVolume(v);
    }

    @Override
    public void mouseReleased(java.awt.event.MouseEvent e) {
        super.mouseReleased(e);
        draggingSlider = -1;
    }

    @Override
    public void mouseMoved(java.awt.event.MouseEvent e) {
        super.mouseMoved(e);
        Point p = toDesignPoint(e);
        hoverSave = SAVE_RECT.contains(p);
        hoverSlider = -1;
        if (new Rectangle(sliderTrack(MUSIC_ROW_Y).x - 16, sliderTrack(MUSIC_ROW_Y).y - 16,
                sliderTrack(MUSIC_ROW_Y).width + 32, sliderTrack(MUSIC_ROW_Y).height + 32).contains(p)) hoverSlider = 0;
        else if (new Rectangle(sliderTrack(SFX_ROW_Y).x - 16, sliderTrack(SFX_ROW_Y).y - 16,
                sliderTrack(SFX_ROW_Y).width + 32, sliderTrack(SFX_ROW_Y).height + 32).contains(p)) hoverSlider = 1;
        hoverToggle = -1;
        if (toggleRect(MUTE_ROW_Y).contains(p)) hoverToggle = 0;
        else if (toggleRect(FULLSCREEN_ROW_Y).contains(p)) hoverToggle = 1;
        boolean hand = hoverSave || hoverSlider >= 0 || hoverToggle >= 0;
        setCursor(hand ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
    }

    // ---- keyboard shortcuts: M = Mute All, F = Fullscreen, Esc = Back.
    @Override
    public void keyTyped(KeyEvent e) {
        char c = Character.toLowerCase(e.getKeyChar());
        Settings s = state.getSettings();
        if (c == 'm') {
            s.toggleMuteAll();
            showToast(s.isMuteAll() ? "Muted — volumes kept, just silenced" : "Sound back on");
            repaint();
        } else if (c == 'f') {
            s.toggleFullscreen();
            applyFullscreen();
            showToast(s.isFullscreen() ? "Fullscreen on" : "Windowed — 1280 x 720");
            repaint();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE && navigator != null) navigator.goBack();
    }

    @Override public void keyReleased(KeyEvent e) {}
}
