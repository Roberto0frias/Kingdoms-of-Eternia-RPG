package dev.eternia.talents;

import dev.eternia.classes.PlayerClass;

import java.util.Arrays;
import java.util.List;

public enum TalentNode {
    // Warrior
    WARRIOR_IRON_WILL("warrior_iron_will", "Voluntad de Hierro", TalentBranch.WARRIOR_DEFENSE, 5, "+1 salud máxima por rango", TalentEffect.HEALTH, 1.0),
    WARRIOR_SHIELD_TRAINING("warrior_shield_training", "Guardia Firme", TalentBranch.WARRIOR_DEFENSE, 15, "+0.5 armadura por rango", TalentEffect.ARMOR, 0.5),
    WARRIOR_LAST_STAND("warrior_last_stand", "Última Defensa", TalentBranch.WARRIOR_DEFENSE, 25, "+3% daño de habilidad por rango", TalentEffect.SKILL_DAMAGE, 0.03),
    WARRIOR_HEAVY_BLOWS("warrior_heavy_blows", "Golpes Pesados", TalentBranch.WARRIOR_COMBAT, 5, "+4% daño de habilidad por rango", TalentEffect.SKILL_DAMAGE, 0.04),
    WARRIOR_BATTLE_RHYTHM("warrior_battle_rhythm", "Ritmo de Batalla", TalentBranch.WARRIOR_COMBAT, 15, "-3% enfriamiento por rango", TalentEffect.SKILL_COOLDOWN, 0.03),
    WARRIOR_EXECUTION("warrior_execution", "Ejecución", TalentBranch.WARRIOR_COMBAT, 25, "+0.15 daño de ataque por rango", TalentEffect.ATTACK_DAMAGE, 0.15),
    WARRIOR_BLADE_MASTERY("warrior_blade_mastery", "Maestro de Espadas", TalentBranch.WARRIOR_WEAPONS, 5, "+0.15 daño de ataque por rango", TalentEffect.ATTACK_DAMAGE, 0.15),
    WARRIOR_AXE_MASTERY("warrior_axe_mastery", "Maestro de Hachas", TalentBranch.WARRIOR_WEAPONS, 15, "+0.5 armadura por rango", TalentEffect.ARMOR, 0.5),
    WARRIOR_HAMMER_MASTERY("warrior_hammer_mastery", "Maestro de Martillos", TalentBranch.WARRIOR_WEAPONS, 25, "+3% daño de habilidad por rango", TalentEffect.SKILL_DAMAGE, 0.03),
    // Mage
    MAGE_EMBER("mage_ember", "Ascua Arcana", TalentBranch.MAGE_ELEMENTAL, 5, "+4% daño de habilidad por rango", TalentEffect.SKILL_DAMAGE, 0.04),
    MAGE_FOCUS("mage_focus", "Enfoque Elemental", TalentBranch.MAGE_ELEMENTAL, 15, "-3% enfriamiento por rango", TalentEffect.SKILL_COOLDOWN, 0.03),
    MAGE_OVERLOAD("mage_overload", "Sobrecarga", TalentBranch.MAGE_ELEMENTAL, 25, "+0.2 daño de ataque por rango", TalentEffect.ATTACK_DAMAGE, 0.2),
    MAGE_SOUL_DRAIN("mage_soul_drain", "Drenaje de Alma", TalentBranch.MAGE_NECROMANCY, 5, "+1 salud máxima por rango", TalentEffect.HEALTH, 1.0),
    MAGE_DARK_PACT("mage_dark_pact", "Pacto Oscuro", TalentBranch.MAGE_NECROMANCY, 15, "+4% daño de habilidad por rango", TalentEffect.SKILL_DAMAGE, 0.04),
    MAGE_GRAVE_WARD("mage_grave_ward", "Resguardo Funerario", TalentBranch.MAGE_NECROMANCY, 25, "+0.5 armadura por rango", TalentEffect.ARMOR, 0.5),
    MAGE_ARCANE_COMPANION("mage_arcane_companion", "Compañero Arcano", TalentBranch.MAGE_SUMMONING, 5, "-3% enfriamiento por rango", TalentEffect.SKILL_COOLDOWN, 0.03),
    MAGE_SUMMONERS_WARD("mage_summoners_ward", "Barrera del Invocador", TalentBranch.MAGE_SUMMONING, 15, "+1 salud máxima por rango", TalentEffect.HEALTH, 1.0),
    MAGE_COMMANDING_WILL("mage_commanding_will", "Voluntad Dominante", TalentBranch.MAGE_SUMMONING, 25, "+3% daño de habilidad por rango", TalentEffect.SKILL_DAMAGE, 0.03),
    // Archer
    ARCHER_TRUE_AIM("archer_true_aim", "Mira Certera", TalentBranch.ARCHER_SNIPER, 5, "+0.2 daño de ataque por rango", TalentEffect.ATTACK_DAMAGE, 0.2),
    ARCHER_STEADY_HAND("archer_steady_hand", "Pulso Estable", TalentBranch.ARCHER_SNIPER, 15, "+4% daño de habilidad por rango", TalentEffect.SKILL_DAMAGE, 0.04),
    ARCHER_LONG_SHOT("archer_long_shot", "Tiro Largo", TalentBranch.ARCHER_SNIPER, 25, "-3% enfriamiento por rango", TalentEffect.SKILL_COOLDOWN, 0.03),
    ARCHER_TRAIL_SENSE("archer_trail_sense", "Sentido del Rastro", TalentBranch.ARCHER_EXPLORATION, 5, "+0.5 armadura por rango", TalentEffect.ARMOR, 0.5),
    ARCHER_LIGHT_STEP("archer_light_step", "Paso Ligero", TalentBranch.ARCHER_EXPLORATION, 15, "+1 salud máxima por rango", TalentEffect.HEALTH, 1.0),
    ARCHER_QUICK_DRAW("archer_quick_draw", "Desenfunde Rápido", TalentBranch.ARCHER_EXPLORATION, 25, "-3% enfriamiento por rango", TalentEffect.SKILL_COOLDOWN, 0.03),
    ARCHER_BEAST_LORE("archer_beast_lore", "Conocimiento de Bestias", TalentBranch.ARCHER_BEAST_HUNT, 5, "+4% daño de habilidad por rango", TalentEffect.SKILL_DAMAGE, 0.04),
    ARCHER_HUNTERS_MARK("archer_hunters_mark", "Marca del Cazador", TalentBranch.ARCHER_BEAST_HUNT, 15, "+0.2 daño de ataque por rango", TalentEffect.ATTACK_DAMAGE, 0.2),
    ARCHER_TROPHY_HUNTER("archer_trophy_hunter", "Cazatrofeos", TalentBranch.ARCHER_BEAST_HUNT, 25, "+1 salud máxima por rango", TalentEffect.HEALTH, 1.0),
    // Assassin
    ASSASSIN_SHADOW_ADEPT("assassin_shadow_adept", "Adepto de las Sombras", TalentBranch.ASSASSIN_SHADOW, 5, "+4% daño de habilidad por rango", TalentEffect.SKILL_DAMAGE, 0.04),
    ASSASSIN_VANISHING_STEP("assassin_vanishing_step", "Paso Fugaz", TalentBranch.ASSASSIN_SHADOW, 15, "-3% enfriamiento por rango", TalentEffect.SKILL_COOLDOWN, 0.03),
    ASSASSIN_DARK_VEIL("assassin_dark_veil", "Velo Oscuro", TalentBranch.ASSASSIN_SHADOW, 25, "+1 salud máxima por rango", TalentEffect.HEALTH, 1.0),
    ASSASSIN_TOXIN_CRAFT("assassin_toxin_craft", "Toxinas", TalentBranch.ASSASSIN_POISON, 5, "+0.2 daño de ataque por rango", TalentEffect.ATTACK_DAMAGE, 0.2),
    ASSASSIN_DEEP_WOUND("assassin_deep_wound", "Herida Profunda", TalentBranch.ASSASSIN_POISON, 15, "+4% daño de habilidad por rango", TalentEffect.SKILL_DAMAGE, 0.04),
    ASSASSIN_VENOM_GUARD("assassin_venom_guard", "Resistencia al Veneno", TalentBranch.ASSASSIN_POISON, 25, "+0.5 armadura por rango", TalentEffect.ARMOR, 0.5),
    ASSASSIN_NIGHT_VISION("assassin_night_vision", "Visión Nocturna", TalentBranch.ASSASSIN_NIGHT_HUNT, 5, "+1 salud máxima por rango", TalentEffect.HEALTH, 1.0),
    ASSASSIN_MARKED_PREY("assassin_marked_prey", "Presa Marcada", TalentBranch.ASSASSIN_NIGHT_HUNT, 15, "+4% daño de habilidad por rango", TalentEffect.SKILL_DAMAGE, 0.04),
    ASSASSIN_FATAL_WINDOW("assassin_fatal_window", "Ventana Letal", TalentBranch.ASSASSIN_NIGHT_HUNT, 25, "-3% enfriamiento por rango", TalentEffect.SKILL_COOLDOWN, 0.03),
    // Priest
    PRIEST_MERCY("priest_mercy", "Misericordia", TalentBranch.PRIEST_HEALING, 5, "+5% sanación por rango", TalentEffect.SKILL_HEALING, 0.05),
    PRIEST_RESTORATION("priest_restoration", "Restauración", TalentBranch.PRIEST_HEALING, 15, "+1 salud máxima por rango", TalentEffect.HEALTH, 1.0),
    PRIEST_DIVINE_CYCLE("priest_divine_cycle", "Ciclo Divino", TalentBranch.PRIEST_HEALING, 25, "-3% enfriamiento por rango", TalentEffect.SKILL_COOLDOWN, 0.03),
    PRIEST_TEMPLAR_ARMOR("priest_templar_armor", "Armadura Templaria", TalentBranch.PRIEST_TEMPLAR, 5, "+0.5 armadura por rango", TalentEffect.ARMOR, 0.5),
    PRIEST_HOLY_STRIKE("priest_holy_strike", "Golpe Sagrado", TalentBranch.PRIEST_TEMPLAR, 15, "+0.2 daño de ataque por rango", TalentEffect.ATTACK_DAMAGE, 0.2),
    PRIEST_BULWARK("priest_bulwark", "Baluarte", TalentBranch.PRIEST_TEMPLAR, 25, "+1 salud máxima por rango", TalentEffect.HEALTH, 1.0),
    PRIEST_PURGE("priest_purge", "Purga", TalentBranch.PRIEST_EXORCISM, 5, "+4% daño de habilidad por rango", TalentEffect.SKILL_DAMAGE, 0.04),
    PRIEST_UNDEAD_WARD("priest_undead_ward", "Amparo", TalentBranch.PRIEST_EXORCISM, 15, "+0.5 armadura por rango", TalentEffect.ARMOR, 0.5),
    PRIEST_JUDGMENT("priest_judgment", "Juicio", TalentBranch.PRIEST_EXORCISM, 25, "+4% daño de habilidad por rango", TalentEffect.SKILL_DAMAGE, 0.04),
    // Deep tiers turn each of the class's three tracks into a five-node progression path.
    WARRIOR_DEFENDER("warrior_defender", "Muro Viviente", TalentBranch.WARRIOR_DEFENSE, 35, "+1.5 salud máxima por rango", TalentEffect.HEALTH, 1.5),
    WARRIOR_UNBROKEN("warrior_unbroken", "Inquebrantable", TalentBranch.WARRIOR_DEFENSE, 45, "+0.5 armadura por rango", TalentEffect.ARMOR, 0.5),
    WARRIOR_WARCRY("warrior_warcry", "Grito de Guerra", TalentBranch.WARRIOR_COMBAT, 35, "−3% enfriamiento por rango", TalentEffect.SKILL_COOLDOWN, 0.03),
    WARRIOR_EXECUTIONER("warrior_executioner", "Verdugo", TalentBranch.WARRIOR_COMBAT, 45, "+5% daño de habilidad por rango", TalentEffect.SKILL_DAMAGE, 0.05),
    WARRIOR_EDGE("warrior_edge", "Filo Implacable", TalentBranch.WARRIOR_WEAPONS, 35, "+0.2 daño de ataque por rango", TalentEffect.ATTACK_DAMAGE, 0.2),
    WARRIOR_ANVIL("warrior_anvil", "Espíritu del Yunque", TalentBranch.WARRIOR_WEAPONS, 45, "+4% daño de habilidad por rango", TalentEffect.SKILL_DAMAGE, 0.04),
    MAGE_STORM("mage_storm", "Dominio de la Tormenta", TalentBranch.MAGE_ELEMENTAL, 35, "+5% daño de habilidad por rango", TalentEffect.SKILL_DAMAGE, 0.05),
    MAGE_ECHO("mage_echo", "Eco Arcano", TalentBranch.MAGE_ELEMENTAL, 45, "−3% enfriamiento por rango", TalentEffect.SKILL_COOLDOWN, 0.03),
    MAGE_RESERVOIR("mage_reservoir", "Reserva de Almas", TalentBranch.MAGE_NECROMANCY, 35, "+1.5 salud máxima por rango", TalentEffect.HEALTH, 1.5),
    MAGE_DEATH_SURGE("mage_death_surge", "Oleada Fúnebre", TalentBranch.MAGE_NECROMANCY, 45, "+5% daño de habilidad por rango", TalentEffect.SKILL_DAMAGE, 0.05),
    MAGE_FAMILIAR("mage_familiar", "Guardián Familiar", TalentBranch.MAGE_SUMMONING, 35, "+0.5 armadura por rango", TalentEffect.ARMOR, 0.5),
    MAGE_COMMAND("mage_command", "Vínculo Dominante", TalentBranch.MAGE_SUMMONING, 45, "+5% daño de habilidad por rango", TalentEffect.SKILL_DAMAGE, 0.05),
    ARCHER_PINPOINT("archer_pinpoint", "Punto Vital", TalentBranch.ARCHER_SNIPER, 35, "+0.2 daño de ataque por rango", TalentEffect.ATTACK_DAMAGE, 0.2),
    ARCHER_HORIZON("archer_horizon", "Horizonte Lejano", TalentBranch.ARCHER_SNIPER, 45, "+5% daño de habilidad por rango", TalentEffect.SKILL_DAMAGE, 0.05),
    ARCHER_TRAIL("archer_trail", "Paso del Rastreador", TalentBranch.ARCHER_EXPLORATION, 35, "+0.5 armadura por rango", TalentEffect.ARMOR, 0.5),
    ARCHER_QUICKSTEP("archer_quickstep", "Desplazamiento", TalentBranch.ARCHER_EXPLORATION, 45, "−3% enfriamiento por rango", TalentEffect.SKILL_COOLDOWN, 0.03),
    ARCHER_PREDATOR("archer_predator", "Depredador", TalentBranch.ARCHER_BEAST_HUNT, 35, "+4% daño de habilidad por rango", TalentEffect.SKILL_DAMAGE, 0.04),
    ARCHER_SLAYER("archer_slayer", "Cazador de Monstruos", TalentBranch.ARCHER_BEAST_HUNT, 45, "+5% daño de habilidad por rango", TalentEffect.SKILL_DAMAGE, 0.05),
    ASSASSIN_VANISH("assassin_vanish", "Desaparición", TalentBranch.ASSASSIN_SHADOW, 35, "−3% enfriamiento por rango", TalentEffect.SKILL_COOLDOWN, 0.03),
    ASSASSIN_EXECUTION("assassin_execution", "Ejecución Silenciosa", TalentBranch.ASSASSIN_SHADOW, 45, "+5% daño de habilidad por rango", TalentEffect.SKILL_DAMAGE, 0.05),
    ASSASSIN_BLACK_VENOM("assassin_black_venom", "Veneno Negro", TalentBranch.ASSASSIN_POISON, 35, "+0.2 daño de ataque por rango", TalentEffect.ATTACK_DAMAGE, 0.2),
    ASSASSIN_TOXIN_MASTERY("assassin_toxin_mastery", "Maestría Tóxica", TalentBranch.ASSASSIN_POISON, 45, "+5% daño de habilidad por rango", TalentEffect.SKILL_DAMAGE, 0.05),
    ASSASSIN_SILENT_KILL("assassin_silent_kill", "Muerte Silenciosa", TalentBranch.ASSASSIN_NIGHT_HUNT, 35, "+5% daño de habilidad por rango", TalentEffect.SKILL_DAMAGE, 0.05),
    ASSASSIN_MOONLIT("assassin_moonlit", "Evasión Lunar", TalentBranch.ASSASSIN_NIGHT_HUNT, 45, "+0.5 armadura por rango", TalentEffect.ARMOR, 0.5),
    PRIEST_GRACE("priest_grace", "Gracia Superior", TalentBranch.PRIEST_HEALING, 35, "+5% sanación por rango", TalentEffect.SKILL_HEALING, 0.05),
    PRIEST_AURA("priest_aura", "Aura Serena", TalentBranch.PRIEST_HEALING, 45, "−3% enfriamiento por rango", TalentEffect.SKILL_COOLDOWN, 0.03),
    PRIEST_ARMS("priest_arms", "Armas Sagradas", TalentBranch.PRIEST_TEMPLAR, 35, "+0.2 daño de ataque por rango", TalentEffect.ATTACK_DAMAGE, 0.2),
    PRIEST_RESOLVE("priest_resolve", "Temple Sagrado", TalentBranch.PRIEST_TEMPLAR, 45, "+0.5 armadura por rango", TalentEffect.ARMOR, 0.5),
    PRIEST_BANISHER("priest_banisher", "Desterrador", TalentBranch.PRIEST_EXORCISM, 35, "+5% daño de habilidad por rango", TalentEffect.SKILL_DAMAGE, 0.05),
    PRIEST_WARDING_LIGHT("priest_warding_light", "Luz Protectora", TalentBranch.PRIEST_EXORCISM, 45, "+1.5 salud máxima por rango", TalentEffect.HEALTH, 1.5);

    public static final int MAX_RANK = 3;

    private final String id;
    private final String displayName;
    private final TalentBranch branch;
    private final int requiredLevel;
    private final String description;
    private final TalentEffect effect;
    private final double amountPerRank;

    TalentNode(String id, String displayName, TalentBranch branch, int requiredLevel,
               String description, TalentEffect effect, double amountPerRank) {
        this.id = id;
        this.displayName = displayName;
        this.branch = branch;
        this.requiredLevel = requiredLevel;
        this.description = description;
        this.effect = effect;
        this.amountPerRank = amountPerRank;
    }

    public String id() { return id; }
    public String displayName() { return displayName; }
    public TalentBranch branch() { return branch; }
    public int requiredLevel() { return requiredLevel; }
    public String description() { return description; }
    public TalentEffect effect() { return effect; }
    public double amountPerRank() { return amountPerRank; }
    public PlayerClass playerClass() { return branch.playerClass(); }

    public static TalentNode fromId(String id) {
        return Arrays.stream(values()).filter(node -> node.id.equals(id)).findFirst().orElse(null);
    }

    public static List<TalentNode> forBranch(TalentBranch branch) {
        return Arrays.stream(values()).filter(node -> node.branch == branch).toList();
    }
}
