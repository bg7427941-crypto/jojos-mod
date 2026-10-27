package io.github.bg7427941.jojosmod.stands;

import io.github.bg7427941.jojosmod.registry.ModStands;
import java.util.Optional;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

/** The five new Part 3 identities. Combat values are Minecraft balance, not manga ratings. */
public enum PartThreeStand {
    SILVER_CHARIOT("silver_chariot", 14, 0xFFC6DBEE),
    MAGICIANS_RED("magicians_red", 50, 0xFFEF7540),
    HERMIT_PURPLE("hermit_purple", 24, 0xFFCA79DD),
    THE_FOOL("the_fool", 50, 0xFFD7BE77),
    THE_WORLD("the_world", 20, 0xFFE6CF57);

    private final String path;
    private final int cooldown;
    private final int color;
    PartThreeStand(String path, int cooldown, int color) { this.path = path; this.cooldown = cooldown; this.color = color; }
    public String path() { return path; }
    public int cooldown() { return cooldown; }
    public int color() { return color; }
    public Identifier id() { return Identifier.fromNamespaceAndPath("jojosmod", path); }
    public String translationKey() { return "stand.jojosmod." + path; }
    public StandDefinition definition() { return ModStands.find(id()).orElseThrow(); }
    public static Optional<PartThreeStand> of(Identifier id) {
        for (var kind : values()) if (kind.id().equals(id)) return Optional.of(kind);
        return Optional.empty();
    }
    public static Optional<PartThreeStand> of(Player player) { return PlayerStands.get(player).standId().flatMap(PartThreeStand::of); }
}
