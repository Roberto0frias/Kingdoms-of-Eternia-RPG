package dev.eternia.attributes;

import java.util.Arrays;

public enum AttributeStat {
    VITALITY("vitality", "Vitalidad"),
    RESISTANCE("resistance", "Resistencia"),
    STRENGTH("strength", "Fuerza"),
    AGILITY("agility", "Agilidad"),
    INTELLIGENCE("intelligence", "Inteligencia"),
    LUCK("luck", "Suerte");

    private final String id;
    private final String displayName;

    AttributeStat(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public String id() { return id; }
    public String displayName() { return displayName; }

    public static AttributeStat fromId(String id) {
        return Arrays.stream(values()).filter(stat -> stat.id.equals(id)).findFirst().orElse(null);
    }
}
