/**
 * OOP lesson — ENCAPSULATION, again: one team member is one object,
 * with a name, a role, and two things they did on the game.
 * CreditsPanel only walks a List<TeamMember> and draws each card
 * the same way — that is why all three cards are exactly equal.
 *
 * To add a 4th member, add one line in CreditsPanel:
 *   team.add(new TeamMember("Name", "Role", "🎮", "Did this", "🎨", "Did that", Rarity.CLASSIC));
 * To put a real name in, change the name and role strings only —
 * the drawing code never needs to change.
 *
 * The portrait is cut from the final Credits mockup
 * (assets/mockup_jeruld.png etc.) — a STAND-IN until Jeruld
 * uploads the final assets. Member 2 and Member 3 are also
 * placeholders: their real names, roles and photos are not known yet.
 * To swap in a final portrait, put its PNG in assets/ and change
 * the image file here — nothing else needs to change.
 */
public record TeamMember(
        String imageFile,
        String name,
        String role,
        String detailIcon1, String detail1,
        String detailIcon2, String detail2) {
}
