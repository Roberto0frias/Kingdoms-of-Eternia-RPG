package dev.eternia.professions;

public enum Profession {
    BLACKSMITH("blacksmith", "Herrero", "Fabricación y mejora de equipo"),
    MINER("miner", "Minero", "Minerales y excavación"),
    LUMBERJACK("lumberjack", "Leñador", "Tala de troncos"),
    FARMER("farmer", "Agricultor", "Cultivos maduros"),
    FISHER("fisher", "Pescador", "Pesca y tesoros acuáticos"),
    HUNTER("hunter", "Cazador", "Caza de criaturas hostiles"),
    ALCHEMIST("alchemist", "Alquimista", "Pociones y preparados");

    private final String id;
    private final String displayName;
    private final String activity;

    Profession(String id, String displayName, String activity) {
        this.id = id;
        this.displayName = displayName;
        this.activity = activity;
    }

    public String id() { return id; }
    public String displayName() { return displayName; }
    public String activity() { return activity; }

    public static Profession fromId(String id) {
        for (Profession profession : values()) if (profession.id.equals(id)) return profession;
        return null;
    }
}
