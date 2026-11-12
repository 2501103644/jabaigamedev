import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * CREDITS screen — EQUAL TEAM CARDS, the layout Jeruld picked.
 *
 * Three IDENTICAL cards, side by side, same size, same gold frame,
 * same layout: avatar on top, big name, gold role badge, a divider,
 * and two detail rows with gold icon badges. Nobody is featured
 * bigger than anyone else — the team is equal.
 *
 * The team is a List<TeamMember> (ArrayList). Adding a person is
 * one line in the constructor, and the drawing loop draws every
 * card the same way, so a 4th member gets an equal card for free.
 * No names are hard-coded in the drawing code — the list IS the data.
 *
 * The portraits are cut from the final Credits mockup
 * (assets/mockup_jeruld.png etc.) — STAND-INS until Jeruld
 * uploads the final assets. Member 2 and Member 3 are placeholders:
 * Jeruld has not given their real names, roles or photos yet.
 * Send them and change the strings in the constructor; the cards
 * and the layout stay exactly as they are.
 */
public class CreditsPanel extends BaseMenuPanel {
    private static final int CARD_W = 360;
    private static final int CARD_H = 470;
    private static final int CARD_Y = 100;
    private static final int CARD_GAP = 40;

    private final List<TeamMember> team = new ArrayList<>();
    public CreditsPanel(GameState state, ScreenNavigator navigator) {
        super(state, navigator);
        // --- the team. Change a name / role / portrait here, nothing else. ---
        team.add(new TeamMember("mockup_jeruld", "Jeruld", "Game Developer",
                "🎮", "Game Design", "⌨", "Programming"));
        team.add(new TeamMember("mockup_member2", "Member 2", "Game Artist",
                "✎", "Characters", "▦", "UI Design"));
        team.add(new TeamMember("mockup_member3", "Member 3", "Sound Designer",
                "♪", "Music", "🔊", "Sound Effects"));
    }

    /** For tests / future screens: the team, in order. */
    public List<TeamMember> getTeam() { return List.copyOf(team); }

    /** Three equal card rectangles, centred as a row. */
    private Rectangle cardRect(int i) {
        int totalW = team.size() * CARD_W + (team.size() - 1) * CARD_GAP;
        int x0 = (W - totalW) / 2;
        return new Rectangle(x0 + i * (CARD_W + CARD_GAP), CARD_Y, CARD_W, CARD_H);
    }

    /** The member's portrait, cut from the Credits mockup — used as-is, no tint. */
    private BufferedImage portraitImage(TeamMember m) {
        return Assets.get(m.imageFile());
    }

    @Override
    protected void drawContent(Graphics2D g) {
        drawTitle(g, "CREDITS");
        for (int i = 0; i < team.size(); i++) {
            drawMemberCard(g, cardRect(i), team.get(i));
        }
        // Footer pill: thanks + engine + version, centred under the cards.
        String footer = "✦  Special Thanks to the Pickleball Community  •  Powered by Java  •  Pickle Smash v1.0";
        g.setFont(new Font("SansSerif", Font.BOLD, 16));
        int fw = g.getFontMetrics().stringWidth(footer) + 44;
        int fx = (W - fw) / 2;
        int fy = CARD_Y + CARD_H + 22;
        g.setColor(new Color(6, 32, 86, 235));
        g.fillRoundRect(fx, fy, fw, 40, 20, 20);
        g.setColor(GOLD);
        g.setStroke(new BasicStroke(2f));
        g.drawRoundRect(fx, fy, fw, 40, 20, 20);
        g.setColor(Color.WHITE);
        g.drawString(footer, fx + 22, fy + 26);
    }

    private void drawMemberCard(Graphics2D g, Rectangle card, TeamMember m) {
        drawPanel(g, card);

        // Avatar, centred at the top of the card.
        BufferedImage img = portraitImage(m);
        if (img != null) {
            drawFitted(g, img, new Rectangle(card.x + 40, card.y + 12, card.width - 80, 230));
        } else {
            // No sprite at all: a gold circle with the member's initial.
            g.setColor(GOLD);
            g.fillOval(card.x + card.width / 2 - 60, card.y + 40, 120, 120);
            g.setColor(new Color(0x8A5A00));
            g.setFont(new Font("SansSerif", Font.BOLD, 64));
            String initial = m.name().substring(0, 1);
            g.drawString(initial, card.x + card.width / 2 - g.getFontMetrics().stringWidth(initial) / 2, card.y + 122);
        }

        // Big name, in gold — same size on every card.
        g.setFont(new Font("SansSerif", Font.BOLD, 34));
        g.setColor(GOLD);
        int nw = g.getFontMetrics().stringWidth(m.name());
        g.drawString(m.name(), card.x + (card.width - nw) / 2, card.y + 272);

        // Gold role badge, same pill on every card.
        g.setFont(new Font("SansSerif", Font.BOLD, 19));
        int bw = g.getFontMetrics().stringWidth("★  " + m.role()) + 44;
        int bx = card.x + (card.width - bw) / 2;
        int by = card.y + 288;
        g.setColor(GOLD);
        g.fillRoundRect(bx, by, bw, 38, 19, 19);
        g.setColor(new Color(0x8A5A00));
        g.drawString("★  " + m.role(), bx + 22, by + 26);

        // Divider, then two detail rows with gold icon badges.
        g.setColor(new Color(255, 255, 255, 60));
        g.setStroke(new BasicStroke(1f));
        g.drawLine(card.x + 40, card.y + 352, card.x + card.width - 40, card.y + 352);
        drawDetailRow(g, card, card.y + 372, m.detailIcon1(), m.detail1());
        drawDetailRow(g, card, card.y + 416, m.detailIcon2(), m.detail2());
    }

    private void drawDetailRow(Graphics2D g, Rectangle card, int y, String icon, String text) {
        int cx = card.x + 56, cy = y + 8;
        g.setColor(GOLD);
        g.fillOval(cx - 17, cy - 17, 34, 34);
        g.setColor(new Color(0x8A5A00));
        g.setFont(new Font("SansSerif", Font.BOLD, 18));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(icon, cx - fm.stringWidth(icon) / 2, cy + 6);
        g.setFont(new Font("SansSerif", Font.BOLD, 20));
        g.setColor(Color.WHITE);
        g.drawString(text, card.x + 84, y + 15);
    }

    @Override
    protected boolean onContentClick(Point p) {
        return false; // Credits has no clickable rows — only BACK (handled by the parent).
    }
}
