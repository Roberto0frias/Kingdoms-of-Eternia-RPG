package dev.eternia.relics;

import dev.eternia.classes.PlayerClass;

import java.util.List;

/** Original class relic forms. A talent and a compatible fused item are both required. */
public record RelicForm(String id, PlayerClass playerClass, String name, String lore,
                        String modelItemId, String fusionItemId, String fusionItemName,
                        String requiredTalentId, int requiredLevel, double skillPower,
                        String skillName) {
    public enum SkillStyle {
        STRIKE, CINDER_WAVE, BASTION_BASH, EMBER_BOLT, PRISM_BURST, SOUL_DRAIN,
        PINPOINT, TEMPEST_FAN, STARFALL_MARK, VEIL_STRIKE, VENOM_CUT, MOON_ECLIPSE,
        DAWN_HEAL, AEGIS_HEAL, SOLAR_JUDGMENT
    }
    private static final List<RelicForm> FORMS = List.of(
            form("warrior_oathblade", PlayerClass.WARRIOR, "Filo del Juramento", "Una hoja que recuerda cada promesa.", "minecraft:iron_sword", null, null, null, 1, 1.00, "Golpe Poderoso"),
            form("warrior_cinder", PlayerClass.WARRIOR, "Mandoble de Brasa", "Acero templado en el corazón de una forja antigua.", "minecraft:diamond_sword", "minecraft:iron_sword", "Espada de hierro", "warrior_heavy_blows", 5, 1.15, "Ruptura Ígnea"),
            form("warrior_bulwark", PlayerClass.WARRIOR, "Baluarte del Bastión", "La defensa también puede ser una forma de desafío.", "minecraft:shield", "minecraft:shield", "Escudo", "warrior_iron_will", 15, 1.10, "Contraataque del Bastión"),
            form("mage_ember", PlayerClass.MAGE, "Vara de la Primera Llama", "Un foco arcano que conserva una chispa primordial.", "minecraft:blaze_rod", null, null, null, 1, 1.00, "Llama Arcana"),
            form("mage_prism", PlayerClass.MAGE, "Prisma de Amatista", "La magia se curva; nunca obedece una sola dirección.", "minecraft:amethyst_shard", "minecraft:amethyst_shard", "Fragmento de amatista", "mage_ember", 5, 1.18, "Lanza Prismática"),
            form("mage_grimoire", PlayerClass.MAGE, "Grimorio del Umbral", "Sus páginas se escriben con tinta de sombra.", "minecraft:enchanted_book", "minecraft:book", "Libro", "mage_dark_pact", 15, 1.22, "Velo Umbrío"),
            form("archer_wayfinder", PlayerClass.ARCHER, "Arco del Trazasendas", "Su cuerda vibra al encontrar una ruta imposible.", "minecraft:bow", null, null, null, 1, 1.00, "Disparo Certero"),
            form("archer_tempest", PlayerClass.ARCHER, "Ballesta de la Tempestad", "Cada virote lleva el eco de una tormenta.", "minecraft:crossbow", "minecraft:crossbow", "Ballesta", "archer_steady_hand", 5, 1.16, "Virote Tormentoso"),
            form("archer_starfall", PlayerClass.ARCHER, "Arco de la Estrella Caída", "Su mira busca los puntos débiles del destino.", "minecraft:bow", "minecraft:bow", "Arco", "archer_long_shot", 15, 1.24, "Flecha Astral"),
            form("assassin_veil", PlayerClass.ASSASSIN, "Daga del Velo", "Su filo aparece donde la luz termina.", "minecraft:iron_sword", null, null, null, 1, 1.00, "Golpe Sombrío"),
            form("assassin_venom", PlayerClass.ASSASSIN, "Estilete de la Viuda", "Un veneno lento para una sentencia rápida.", "minecraft:diamond_sword", "minecraft:iron_sword", "Espada de hierro", "assassin_toxin_craft", 5, 1.18, "Herida Ponzoñosa"),
            form("assassin_moon", PlayerClass.ASSASSIN, "Hoja de Luna Nueva", "Solo refleja el cielo que aún no ha llegado.", "minecraft:golden_sword", "minecraft:golden_sword", "Espada de oro", "assassin_marked_prey", 15, 1.25, "Eclipse"),
            form("priest_dawn", PlayerClass.PRIEST, "Maza de la Aurora", "La primera luz no pregunta quién la merece.", "minecraft:mace", null, null, null, 1, 1.00, "Sanación Menor"),
            form("priest_aegis", PlayerClass.PRIEST, "Égida del Alba", "Su resplandor protege a quien sostiene la línea.", "minecraft:shield", "minecraft:shield", "Escudo", "priest_templar_armor", 5, 1.12, "Amparo Radiante"),
            form("priest_judgment", PlayerClass.PRIEST, "Martillo del Veredicto", "Un golpe para quebrar la oscuridad, no la voluntad.", "minecraft:golden_sword", "minecraft:golden_sword", "Espada de oro", "priest_holy_strike", 15, 1.23, "Juicio Solar")
    );

    private static RelicForm form(String id, PlayerClass playerClass, String name, String lore,
                                  String modelItemId, String fusionItemId, String fusionItemName,
                                  String requiredTalentId, int requiredLevel, double skillPower, String skillName) {
        return new RelicForm(id, playerClass, name, lore, modelItemId, fusionItemId, fusionItemName,
                requiredTalentId, requiredLevel, skillPower, skillName);
    }

    public boolean isBaseForm() { return fusionItemId == null; }
    public double attackDamage() { return 4.0 + Math.max(0.0, skillPower - 1.0) * 8.0; }
    public SkillStyle skillStyle() {
        return switch (id) {
            case "warrior_cinder" -> SkillStyle.CINDER_WAVE;
            case "warrior_bulwark" -> SkillStyle.BASTION_BASH;
            case "mage_ember" -> SkillStyle.EMBER_BOLT;
            case "mage_prism" -> SkillStyle.PRISM_BURST;
            case "mage_grimoire" -> SkillStyle.SOUL_DRAIN;
            case "archer_tempest" -> SkillStyle.TEMPEST_FAN;
            case "archer_starfall" -> SkillStyle.STARFALL_MARK;
            case "assassin_venom" -> SkillStyle.VENOM_CUT;
            case "assassin_moon" -> SkillStyle.MOON_ECLIPSE;
            case "priest_dawn" -> SkillStyle.DAWN_HEAL;
            case "priest_aegis" -> SkillStyle.AEGIS_HEAL;
            case "priest_judgment" -> SkillStyle.SOLAR_JUDGMENT;
            case "archer_wayfinder" -> SkillStyle.PINPOINT;
            case "assassin_veil" -> SkillStyle.VEIL_STRIKE;
            default -> SkillStyle.STRIKE;
        };
    }
    public static List<RelicForm> all() { return FORMS; }
    public static List<RelicForm> forClass(PlayerClass playerClass) {
        return FORMS.stream().filter(form -> form.playerClass == playerClass).toList();
    }
    public static RelicForm find(String id) { return FORMS.stream().filter(form -> form.id.equals(id)).findFirst().orElse(null); }
    public static RelicForm baseFor(PlayerClass playerClass) {
        return FORMS.stream().filter(form -> form.playerClass == playerClass && form.isBaseForm()).findFirst().orElse(null);
    }
}
