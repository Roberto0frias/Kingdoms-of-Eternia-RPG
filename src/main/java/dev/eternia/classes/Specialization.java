package dev.eternia.classes;

import java.util.Arrays;
import java.util.List;

public enum Specialization {
    KNIGHT("knight", "Caballero", PlayerClass.WARRIOR, "Defensa, escudos y protección"),
    BERSERKER("berserker", "Berserker", PlayerClass.WARRIOR, "Daño y presión ofensiva"),
    PALADIN("paladin", "Paladín", PlayerClass.WARRIOR, "Combate y apoyo sagrado"),
    ELEMENTAL("elemental", "Elemental", PlayerClass.MAGE, "Magia de elementos"),
    NECROMANCER("necromancer", "Nigromante", PlayerClass.MAGE, "Magia de sombras y drenaje"),
    SUMMONER("summoner", "Invocador", PlayerClass.MAGE, "Criaturas y control del campo"),
    SNIPER("sniper", "Francotirador", PlayerClass.ARCHER, "Precisión y alcance"),
    EXPLORER("explorer", "Explorador", PlayerClass.ARCHER, "Movilidad y supervivencia"),
    BEAST_HUNTER("beast_hunter", "Cazador de Bestias", PlayerClass.ARCHER, "Daño contra criaturas"),
    SHADOWS("shadows", "Sombras", PlayerClass.ASSASSIN, "Sigilo y golpes rápidos"),
    POISONS("poisons", "Venenos", PlayerClass.ASSASSIN, "Daño persistente y debilitación"),
    NIGHT_HUNTER("night_hunter", "Cazador Nocturno", PlayerClass.ASSASSIN, "Caza y eliminación de objetivos"),
    HEALER("healer", "Sanador", PlayerClass.PRIEST, "Curación y soporte"),
    TEMPLAR("templar", "Templario", PlayerClass.PRIEST, "Defensa y combate sagrado"),
    EXORCIST("exorcist", "Exorcista", PlayerClass.PRIEST, "Daño contra criaturas oscuras");

    private final String id;
    private final String displayName;
    private final PlayerClass playerClass;
    private final String description;

    Specialization(String id, String displayName, PlayerClass playerClass, String description) {
        this.id = id;
        this.displayName = displayName;
        this.playerClass = playerClass;
        this.description = description;
    }

    public String id() { return id; }
    public String displayName() { return displayName; }
    public PlayerClass playerClass() { return playerClass; }
    public String description() { return description; }

    public static Specialization fromId(String id) {
        return Arrays.stream(values()).filter(spec -> spec.id.equals(id)).findFirst().orElse(null);
    }

    public static List<Specialization> forClass(PlayerClass playerClass) {
        return Arrays.stream(values()).filter(spec -> spec.playerClass == playerClass).toList();
    }
}
