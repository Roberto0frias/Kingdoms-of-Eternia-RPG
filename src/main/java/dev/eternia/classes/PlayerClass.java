package dev.eternia.classes;

import java.util.Arrays;

public enum PlayerClass {
    WARRIOR("warrior", "Guerrero", "Caballero · Berserker · Paladín"),
    MAGE("mage", "Mago", "Elemental · Nigromante · Invocador"),
    ARCHER("archer", "Arquero", "Francotirador · Explorador · Cazador de Bestias"),
    ASSASSIN("assassin", "Asesino", "Sombras · Venenos · Cazador Nocturno"),
    PRIEST("priest", "Sacerdote", "Sanador · Templario · Exorcista");

    private final String id;
    private final String displayName;
    private final String specializations;

    PlayerClass(String id, String displayName, String specializations) {
        this.id = id;
        this.displayName = displayName;
        this.specializations = specializations;
    }

    public String id() { return id; }
    public String displayName() { return displayName; }
    public String specializations() { return specializations; }

    public static PlayerClass fromId(String id) {
        return Arrays.stream(values()).filter(playerClass -> playerClass.id.equals(id)).findFirst().orElse(null);
    }
}
