package dev.eternia.races;

import dev.eternia.attributes.AttributeStat;
import java.util.Arrays;

public enum Race {
    HUMAN("human", "Humano", "+5% experiencia, +5 Suerte, +5 Comercio", "Negociador: descuento con aldeanos"),
    ELF("elf", "Elfo", "+10 Agilidad, +10 Inteligencia", "Visión Natural: mayor alcance con arcos"),
    DWARF("dwarf", "Enano", "+15 Resistencia, +10 Fuerza", "Maestro Herrero: calidad de fabricación"),
    ORC("orc", "Orco", "+20 Fuerza, +10 Vitalidad", "Furia de Guerra: más daño con poca vida"),
    TIEFLING("tiefling", "Tiefling", "+15 Inteligencia, resistencia al fuego", "Llama Infernal: ataque mágico de fuego");

    private final String id;
    private final String displayName;
    private final String bonuses;
    private final String racialSkill;

    Race(String id, String displayName, String bonuses, String racialSkill) {
        this.id = id;
        this.displayName = displayName;
        this.bonuses = bonuses;
        this.racialSkill = racialSkill;
    }

    public String id() { return id; }
    public String displayName() { return displayName; }
    public String bonuses() { return bonuses; }
    public String racialSkill() { return racialSkill; }
    public int experiencePercent() { return this == HUMAN ? 105 : 100; }

    public int attributeBonus(AttributeStat stat) {
        return switch (this) {
            case HUMAN -> stat == AttributeStat.LUCK ? 5 : 0;
            case ELF -> stat == AttributeStat.AGILITY || stat == AttributeStat.INTELLIGENCE ? 10 : 0;
            case DWARF -> stat == AttributeStat.RESISTANCE ? 15 : stat == AttributeStat.STRENGTH ? 10 : 0;
            case ORC -> stat == AttributeStat.STRENGTH ? 20 : stat == AttributeStat.VITALITY ? 10 : 0;
            case TIEFLING -> stat == AttributeStat.INTELLIGENCE ? 15 : 0;
        };
    }

    public static Race fromId(String id) {
        return Arrays.stream(values()).filter(race -> race.id.equals(id)).findFirst().orElse(null);
    }
}
