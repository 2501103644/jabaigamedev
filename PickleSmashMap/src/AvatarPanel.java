import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AVATAR screen — the layout from the mockup, now real and working.
 *
 * Left: big animated preview of the selected character. Right: a row of
 * the 4 character variants, then a row of 4 paddles and a row of 4 balls.
 * Top: player name + coins. Bottom: BACK (from BaseMenuPanel) + SELECT.
 *
 * UPLOAD-ONLY NOTE: the uploaded zip contains only one character sprite
 * (assets/player_classic.png) and no paddle or ball pictures at all.
 * So the 4 character variants all use that one sprite, tinted a
 * different colour per rarity (see tintedAvatarImage()), and the
 * paddles and balls are drawn in code (see drawPaddleIcon /
 * drawBallIcon()). No picture from outside the uploaded zip is used.
 * Add more sprite files to assets/ and list them in AvatarCatalog,
 * and this screen will show them with no other change.
 *
 * DSA in this screen:
 *  - SORTING: the SORT button cycles Name / Cost / Rarity. Each sort makes
 *    a COPY of the catalog list and sorts the copy (Comparator), so the
 *    catalog's own order is never destroyed.
 *  - BINARY SEARCH: type a name and press Enter. findByName() searches
 *    the name-sorted list in O(log n) and jumps straight to that character.
 *  - HASHMAP: selecting uses getById() — O(1), see GameState / Catalog.
 */
public class AvatarPanel extends BaseMenuPanel implements KeyListener {
    private enum SortMode { NAME, COST, RARITY }

    private static final Rectangle PREVIEW_PANEL = new Rectangle(24, 100, 300, 528);
    private static final Rectangle GRID_PANEL = new Rectangle(340, 100, 916, 352);
    private static final Rectangle PADDLE_ROW = new Rectangle(340, 478, 916, 68);
    private static final Rectangle BALL_ROW = new Rectangle(340, 566, 916, 68);
    private static final Rectangle SELECT_RECT = new Rectangle(1046, 644, 210, 58);
    private static final Rectangle SORT_RECT = new Rectangle(1066, 108, 176, 36);

    private SortMode sortMode = SortMode.NAME;
    private List<Avatar> view = new ArrayList<>();
    private String searchText = "";
    private boolean hoverSelect;
    /** Tinted copies of player_classic.png, one per rarity — made once, reused. */
    private final Map<String, BufferedImage> tintedCache = new HashMap<>();

    public AvatarPanel(GameState state, ScreenNavigator navigator) {
        super(state, navigator);
        addKeyListener(this);
        rebuildView();
    }

    private void rebuildView() {
        view = switch (sortMode) {
            case NAME -> state.getAvatarCatalog().sortedByName();
            case COST -> state.getAvatarCatalog().sortedByCost();
            case RARITY -> {
                List<Avatar> copy = new ArrayList<>(state.getAvatarCatalog().getAll());
                copy.sort(Comparator.comparing(Avatar::getRarity).thenComparing(Avatar::getName));
                yield copy;
            }
        };
    }

    /**
     * The portrait for an avatar. Mockup portraits (assets/mockup_*.png)
     * are already four different people, so they are used as-is —
     * NO tint. The old tint is kept only for the single
     * player_classic.png sprite, in case a future avatar uses it.
     */
    private BufferedImage tintedAvatarImage(Avatar a) {
        if (a.getImageFile().startsWith("mockup_")) return Assets.get(a.getImageFile());
        return tintedCache.computeIfAbsent(a.getId(), id -> {
            BufferedImage src = Assets.get(a.getImageFile());
            float[] scales = switch (a.getRarity()) {
                case CLASSIC -> new float[]{1.0f, 1.0f, 1.0f, 1.0f};
                case ELITE -> new float[]{1.25f, 1.05f, 0.65f, 1.0f}; // golden
                case SPECIAL -> new float[]{1.05f, 0.8f, 1.35f, 1.0f}; // purple
                case EPIC -> new float[]{0.65f, 0.7f, 1.1f, 1.0f}; // dark blue
            };
            return new RescaleOp(scales, new float[]{0, 0, 0, 0}, null).filter(src, null);
        });
    }

    /** Rarity colour for a character's NAME in the grid — gold for Elite, purple for Special, blue for Epic. */
    private static Color rarityTextColor(Rarity r) {
        return switch (r) {
            case CLASSIC -> Color.WHITE;
            case ELITE -> new Color(0xFFD65A);
            case SPECIAL -> new Color(0xD08CFF);
            case EPIC -> new Color(0x7FB2FF);
        };
    }

    /** Rarity colour used for the code-drawn paddles and balls. */
    private static Color rarityColor(Rarity r) {
        return switch (r) {
            case CLASSIC -> new Color(0x2F6FDE);
            case ELITE -> new Color(0xE8A800);
            case SPECIAL -> new Color(0x9B4DFF);
            case EPIC -> new Color(0x1B2A4A);
        };
    }

    /** A paddle drawn in code — no paddle picture exists in the uploaded zip. */
    private void drawPaddleIcon(Graphics2D g, Rectangle box, Color color) {
        int w = box.width / 2, h = box.height;
        int x = box.x + (box.width - w) / 2, y = box.y;
        g.setColor(color);
        g.fillRoundRect(x, y, w, (int) (h * 0.68), 14, 14);
        g.setColor(new Color(0xC8E63A));
        g.fillOval(x + 6, y + 8, 7, 7);
        g.fillOval(x + w - 13, y + 8, 7, 7);
        g.fillOval(x + 6, y + (int) (h * 0.68) - 16, 7, 7);
        g.fillOval(x + w - 13, y + (int) (h * 0.68) - 16, 7, 7);
        g.setColor(new Color(0x5A3A1A));
        g.fillRoundRect(x + w / 2 - 4, y + (int) (h * 0.68), 8, (int) (h * 0.32), 4, 4);
    }

    /** A pickleball drawn in code — no ball picture exists in the uploaded zip. */
    private void drawBallIcon(Graphics2D g, Rectangle box, Color color) {
        int d = Math.min(box.width, box.height);
        int x = box.x + (box.width - d) / 2, y = box.y + (box.height - d) / 2;
        g.setColor(color);
        g.fillOval(x, y, d, d);
        g.setColor(new Color(0, 0, 0, 90));
        int r = d / 7;
        g.fillOval(x + d / 4, y + d / 4, r, r);
        g.fillOval(x + d / 2, y + d / 5, r, r);
        g.fillOval(x + d / 5, y + d / 2, r, r);
        g.fillOval(x + d / 2, y + d / 2, r, r);
        g.setColor(new Color(255, 255, 255, 120));
        g.fillOval(x + d / 4, y + d / 5, r, r / 2);
    }

    // ---- layout helpers: where is card i?
    private Rectangle avatarCardRect(int i) {
        int w = 214, h = 240, gapX = 10;
        int x = GRID_PANEL.x + 14 + i * (w + gapX);
        int y = GRID_PANEL.y + 56;
        return new Rectangle(x, y, w, h);
    }

    private Rectangle paddleCardRect(int i) {
        int w = 214, h = 64, gap = 10;
        return new Rectangle(PADDLE_ROW.x + 14 + i * (w + gap), PADDLE_ROW.y + 6, w, h);
    }

    private Rectangle ballCardRect(int i) {
        int w = 214, h = 64, gap = 10;
        return new Rectangle(BALL_ROW.x + 14 + i * (w + gap), BALL_ROW.y + 6, w, h);
    }

    @Override
    protected void drawContent(Graphics2D g) {
        drawTitle(g, "AVATAR");
        drawCoins(g, 1046, 28);
        g.setColor(new Color(6, 32, 86, 235));
        g.fillRoundRect(24, 28, 220, 52, 16, 16);
        g.setColor(GOLD);
        g.setStroke(new BasicStroke(2f));
        g.drawRoundRect(24, 28, 220, 52, 16, 16);
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 26));
        g.drawString("★  " + state.getPlayerName(), 44, 62);

        drawPreview(g);
        drawGrid(g);
        drawPaddleRow(g);
        drawBallRow(g);
        drawWoodenButton(g, SELECT_RECT, "✓  SELECT", hoverSelect);
        // Search hint, in the free strip at the bottom centre (typing + Enter searches by name).
        // It used to sit under the title, where it overlapped the AVATAR banner.
        g.setFont(new Font("SansSerif", Font.ITALIC, 16));
        g.setColor(new Color(255, 255, 255, 220));
        String hint = searchText.isEmpty()
                ? "Type a name + Enter to search  •  SORT: " + sortMode
                : "Search: " + searchText + "_";
        g.drawString(hint, (W - g.getFontMetrics().stringWidth(hint)) / 2, 692);
    }

    private void drawPreview(Graphics2D g) {
        drawPanel(g, PREVIEW_PANEL);
        // Gold YOUR PICK pill at the top of the preview card.
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        String pick = "★  YOUR PICK";
        int pw = g.getFontMetrics().stringWidth(pick) + 28;
        int px = PREVIEW_PANEL.x + (PREVIEW_PANEL.width - pw) / 2;
        g.setColor(GOLD);
        g.fillRoundRect(px, PREVIEW_PANEL.y + 10, pw, 26, 13, 13);
        g.setColor(new Color(0x8A5A00));
        g.drawString(pick, px + 14, PREVIEW_PANEL.y + 28);

        Avatar selected = state.getSelectedAvatar();
        BufferedImage img = tintedAvatarImage(selected);
        // Big portrait, fitted into the panel keeping its aspect ratio,
        // bobbing gently. The box is wide and tall so a head-and-shoulders
        // portrait fills the panel instead of floating in it.
        double bob = Math.sin(time * 2.2) * 5.0;
        int boxX = PREVIEW_PANEL.x + 10, boxY = PREVIEW_PANEL.y + 44;
        int boxW = PREVIEW_PANEL.width - 20, boxH = 380;
        double s = Math.min(boxW / (double) img.getWidth(), boxH / (double) img.getHeight());
        int fw = (int) (img.getWidth() * s), fh = (int) (img.getHeight() * s);
        int ix = boxX + (boxW - fw) / 2;
        int iy = boxY + (boxH - fh) / 2 + (int) bob;
        int cx = PREVIEW_PANEL.x + PREVIEW_PANEL.width / 2;
        g.setColor(new Color(0, 30, 80, 80));
        g.fillOval(cx - 80, iy + fh - 6, 160, 18);
        g.drawImage(img, ix, iy, fw, fh, null);

        g.setFont(new Font("SansSerif", Font.BOLD, 24));
        g.setColor(Color.WHITE);
        String name = selected.getName();
        g.drawString(name, cx - g.getFontMetrics().stringWidth(name) / 2, PREVIEW_PANEL.y + 470);
        g.setFont(new Font("SansSerif", Font.PLAIN, 18));
        g.setColor(GOLD);
        String sub = selected.getRarity().label() + "  •  " + selected.getGender();
        g.drawString(sub, cx - g.getFontMetrics().stringWidth(sub) / 2, PREVIEW_PANEL.y + 496);
    }

    private void drawGrid(Graphics2D g) {
        drawPanel(g, GRID_PANEL);
        g.setFont(new Font("SansSerif", Font.BOLD, 22));
        g.setColor(GOLD);
        g.drawString("CHOOSE CHARACTER", GRID_PANEL.x + 16, GRID_PANEL.y + 32);
        g.setColor(new Color(255, 214, 90, 220));
        g.fillRoundRect(SORT_RECT.x, SORT_RECT.y, SORT_RECT.width, SORT_RECT.height, 10, 10);
        g.setColor(new Color(0x8A5A00));
        g.setFont(new Font("SansSerif", Font.BOLD, 16));
        g.drawString("SORT: " + sortMode, SORT_RECT.x + 14, SORT_RECT.y + 24);

        for (int i = 0; i < view.size(); i++) {
            Avatar a = view.get(i);
            Rectangle r = avatarCardRect(i);
            boolean selected = a.getId().equals(state.getSelectedAvatarId());
            g.setColor(selected ? new Color(255, 214, 90, 230) : new Color(255, 255, 255, 40));
            g.fillRoundRect(r.x, r.y, r.width, r.height, 12, 12);
            if (selected) {
                g.setColor(GOLD);
                g.setStroke(new BasicStroke(3f));
                g.drawRoundRect(r.x, r.y, r.width, r.height, 12, 12);
            }
            drawFitted(g, tintedAvatarImage(a), new Rectangle(r.x + 8, r.y + 6, r.width - 16, 190));
            // Name in the rarity colour, so Elite / Special / Epic read at a glance.
            g.setFont(new Font("SansSerif", Font.BOLD, 15));
            // Selected card is gold, so its name is dark — gold-on-gold was unreadable.
            g.setColor(selected ? new Color(0x8A5A00) : rarityTextColor(a.getRarity()));
            String label = a.getName();
            g.drawString(label, r.x + (r.width - g.getFontMetrics().stringWidth(label)) / 2, r.y + 222);
            if (selected) {
                // Gold SELECTED tag, top-left corner of the card.
                g.setFont(new Font("SansSerif", Font.BOLD, 12));
                String tag = "★ SELECTED";
                int tw = g.getFontMetrics().stringWidth(tag) + 16;
                g.setColor(GOLD);
                g.fillRoundRect(r.x + 8, r.y + 8, tw, 22, 11, 11);
                g.setColor(new Color(0x8A5A00));
                g.drawString(tag, r.x + 16, r.y + 23);
            }
            if (!a.isUnlocked()) {
                g.setColor(new Color(0, 0, 0, 110));
                g.fillRoundRect(r.x, r.y, r.width, r.height, 12, 12);
                // Gold coin price pill, top-right corner of the card.
                g.setFont(new Font("SansSerif", Font.BOLD, 14));
                String price = "🪙 " + a.getCost();
                int pw = g.getFontMetrics().stringWidth(price) + 18;
                g.setColor(GOLD);
                g.fillRoundRect(r.x + r.width - pw - 8, r.y + 8, pw, 24, 12, 12);
                g.setColor(new Color(0x8A5A00));
                g.drawString(price, r.x + r.width - pw + 1, r.y + 25);
                g.setColor(Color.WHITE);
                g.setFont(new Font("SansSerif", Font.BOLD, 16));
                g.drawString("🔒  Locked", r.x + (r.width - g.getFontMetrics().stringWidth("🔒  Locked")) / 2, r.y + 120);
            }
        }
        g.setFont(new Font("SansSerif", Font.ITALIC, 14));
        g.setColor(new Color(255, 255, 255, 170));
        g.drawString("Portraits cut from the final Avatar mockup — stand-ins until Jeruld uploads the final assets.",
                GRID_PANEL.x + 16, GRID_PANEL.y + 332);
    }

    /** Gold circle with a mini paddle / ball inside — the section header's visual anchor. */
    private void drawSectionBadge(Graphics2D g, int cx, int cy, String kind) {
        g.setColor(GOLD);
        g.fillOval(cx - 13, cy - 13, 26, 26);
        if (kind.equals("PADDLE")) {
            drawPaddleIcon(g, new Rectangle(cx - 7, cy - 10, 14, 20), new Color(0x2F6FDE));
        } else {
            drawBallIcon(g, new Rectangle(cx - 9, cy - 9, 18, 18), new Color(0x2F6FDE));
        }
    }

    private void drawPaddleRow(Graphics2D g) {
        drawSectionBadge(g, PADDLE_ROW.x + 12, PADDLE_ROW.y - 12, "PADDLE");
        g.setFont(new Font("SansSerif", Font.BOLD, 20));
        g.setColor(GOLD);
        g.drawString("PADDLES", PADDLE_ROW.x + 32, PADDLE_ROW.y - 2);
        List<Paddle> paddles = state.getPaddleCatalog().getAll();
        for (int i = 0; i < paddles.size(); i++) {
            Paddle p = paddles.get(i);
            Rectangle r = paddleCardRect(i);
            boolean selected = p.getId().equals(state.getSelectedPaddleId());
            g.setColor(selected ? new Color(255, 214, 90, 230) : new Color(8, 38, 96, 200));
            g.fillRoundRect(r.x, r.y, r.width, r.height, 12, 12);
            drawPaddleIcon(g, new Rectangle(r.x + 8, r.y + 6, 46, 52), rarityColor(p.getRarity()));
            g.setFont(new Font("SansSerif", Font.BOLD, 15));
            g.setColor(Color.WHITE);
            g.drawString(p.getName(), r.x + 62, r.y + 28);
            g.setFont(new Font("SansSerif", Font.PLAIN, 13));
            g.setColor(p.isUnlocked() ? new Color(180, 255, 180) : GOLD);
            g.drawString(p.isUnlocked() ? p.getRarity().label() : "🔒 " + p.getCost() + " coins", r.x + 62, r.y + 48);
        }
    }

    private void drawBallRow(Graphics2D g) {
        drawSectionBadge(g, BALL_ROW.x + 12, BALL_ROW.y - 12, "BALL");
        g.setFont(new Font("SansSerif", Font.BOLD, 20));
        g.setColor(GOLD);
        g.drawString("BALLS", BALL_ROW.x + 32, BALL_ROW.y - 2);
        List<Ball> balls = state.getBallCatalog().getAll();
        for (int i = 0; i < balls.size(); i++) {
            Ball b = balls.get(i);
            Rectangle r = ballCardRect(i);
            boolean selected = b.getId().equals(state.getSelectedBallId());
            g.setColor(selected ? new Color(255, 214, 90, 230) : new Color(8, 38, 96, 200));
            g.fillRoundRect(r.x, r.y, r.width, r.height, 12, 12);
            drawBallIcon(g, new Rectangle(r.x + 8, r.y + 6, 46, 52), rarityColor(b.getRarity()));
            g.setFont(new Font("SansSerif", Font.BOLD, 15));
            g.setColor(Color.WHITE);
            g.drawString(b.getName(), r.x + 62, r.y + 28);
            g.setFont(new Font("SansSerif", Font.PLAIN, 13));
            g.setColor(b.isUnlocked() ? new Color(180, 255, 180) : GOLD);
            g.drawString(b.isUnlocked() ? b.getRarity().label() : "🔒 " + b.getCost() + " coins", r.x + 62, r.y + 48);
        }
    }

    /** One method for every kind of item — POLYMORPHISM: Avatar, Paddle and Ball all work here. */
    private void pickItem(CosmeticItem item) {
        if (item == null) return;
        boolean wasLocked = !item.isUnlocked();
        if (state.unlockAndSelect(item)) {
            showToast(wasLocked
                    ? "Unlocked " + item.getName() + " for " + item.getCost() + " coins!"
                    : "Selected " + item.getName());
            state.save();
        } else {
            showToast("Not enough coins — " + item.getName() + " costs " + item.getCost());
        }
    }

    @Override
    protected boolean onContentClick(Point p) {
        if (SORT_RECT.contains(p)) {
            sortMode = SortMode.values()[(sortMode.ordinal() + 1) % SortMode.values().length];
            rebuildView();
            showToast("Sorted by " + sortMode);
            return true;
        }
        if (SELECT_RECT.contains(p)) {
            state.save();
            showToast("Avatar saved!");
            if (navigator != null) navigator.goBack();
            return true;
        }
        for (int i = 0; i < view.size(); i++) {
            if (avatarCardRect(i).contains(p)) { pickItem(view.get(i)); return true; }
        }
        List<Paddle> paddles = state.getPaddleCatalog().getAll();
        for (int i = 0; i < paddles.size(); i++) {
            if (paddleCardRect(i).contains(p)) { pickItem(paddles.get(i)); return true; }
        }
        List<Ball> balls = state.getBallCatalog().getAll();
        for (int i = 0; i < balls.size(); i++) {
            if (ballCardRect(i).contains(p)) { pickItem(balls.get(i)); return true; }
        }
        return false;
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        super.mouseMoved(e);
        hoverSelect = SELECT_RECT.contains(toDesignPoint(e));
    }

    // ---- search: type a name, press Enter. Binary search does the finding.
    @Override
    public void keyTyped(KeyEvent e) {
        char c = e.getKeyChar();
        if (c == '\n' || c == '\r') {
            if (!searchText.isBlank()) {
                Avatar found = state.getAvatarCatalog().findByName(searchText.trim());
                if (found != null) {
                    pickItem(found);
                } else {
                    showToast("No character named \"" + searchText.trim() + "\"");
                }
                searchText = "";
            }
        } else if (c == '\b') {
            if (!searchText.isEmpty()) searchText = searchText.substring(0, searchText.length() - 1);
        } else if (Character.isLetterOrDigit(c) || c == ' ') {
            searchText += c;
        }
        repaint();
    }
    @Override public void keyPressed(KeyEvent e) {}
    @Override public void keyReleased(KeyEvent e) {}
}
