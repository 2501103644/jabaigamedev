import java.util.Properties;

/**
 * OOP lesson — ENCAPSULATION: every setting is private, and the setters
 * guard the value (a volume can never be 150 or -20).
 *
 * Layout 1 (the one Jeruld picked) has only four settings:
 *   Music Volume, Sound Effects Volume, Mute All, Fullscreen.
 * Vibration, Language, Graphics Quality, Controls and Reset Progress
 * were removed — they were inside the red circle in Jeruld's screenshot.
 *
 * Mute All is a boolean, not a volume of 0: the two volumes are kept
 * as they were, so un-muting brings the sound back exactly as it was.
 * effectiveMusicVolume() / effectiveSfxVolume() are the values the
 * game's audio code should actually use — 0 when muted, the slider
 * value otherwise. That is a DERIVED value: it is computed, never stored.
 */
public class Settings {
    private int musicVolume = 75;
    private int sfxVolume = 60;
    private boolean muteAll = false;
    private boolean fullscreen = false;

    public int getMusicVolume() { return musicVolume; }
    public int getSfxVolume() { return sfxVolume; }
    public boolean isMuteAll() { return muteAll; }
    public boolean isFullscreen() { return fullscreen; }

    /** What the audio code should use: 0 when Mute All is on. */
    public int effectiveMusicVolume() { return muteAll ? 0 : musicVolume; }
    public int effectiveSfxVolume() { return muteAll ? 0 : sfxVolume; }

    public void setMusicVolume(int v) { musicVolume = clamp(v); }
    public void setSfxVolume(int v) { sfxVolume = clamp(v); }
    public void setMuteAll(boolean on) { muteAll = on; }
    public void toggleMuteAll() { muteAll = !muteAll; }
    public void setFullscreen(boolean on) { fullscreen = on; }
    public void toggleFullscreen() { fullscreen = !fullscreen; }

    public void resetToDefaults() {
        musicVolume = 75;
        sfxVolume = 60;
        muteAll = false;
        fullscreen = false;
    }

    private static int clamp(int v) { return Math.max(0, Math.min(100, v)); }

    // ---- save / load: plain key=value text, so Jeruld can open the file and read it.
    void saveTo(Properties p) {
        p.setProperty("music", String.valueOf(musicVolume));
        p.setProperty("sfx", String.valueOf(sfxVolume));
        p.setProperty("muteAll", String.valueOf(muteAll));
        p.setProperty("fullscreen", String.valueOf(fullscreen));
    }

    void loadFrom(Properties p) {
        try { musicVolume = clamp(Integer.parseInt(p.getProperty("music", "75"))); } catch (NumberFormatException ignored) {}
        try { sfxVolume = clamp(Integer.parseInt(p.getProperty("sfx", "60"))); } catch (NumberFormatException ignored) {}
        muteAll = Boolean.parseBoolean(p.getProperty("muteAll", "false"));
        fullscreen = Boolean.parseBoolean(p.getProperty("fullscreen", "false"));
    }
}
