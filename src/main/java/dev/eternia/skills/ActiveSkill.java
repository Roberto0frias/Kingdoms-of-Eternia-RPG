package dev.eternia.skills;

import dev.eternia.classes.PlayerClass;

public enum ActiveSkill {
    WARRIOR("Golpe poderoso", PlayerClass.WARRIOR, 8, 5.0, 8),
    MAGE("Llama arcana", PlayerClass.MAGE, 7, 16.0, 16),
    ARCHER("Disparo certero", PlayerClass.ARCHER, 6, 24.0, 12),
    ASSASSIN("Golpe sombrío", PlayerClass.ASSASSIN, 10, 5.0, 10),
    PRIEST("Sanación menor", PlayerClass.PRIEST, 12, 0.0, 14);

    private final String displayName;
    private final PlayerClass playerClass;
    private final int cooldownSeconds;
    private final double range;
    private final int manaCost;

    ActiveSkill(String displayName, PlayerClass playerClass, int cooldownSeconds, double range, int manaCost) {
        this.displayName = displayName;
        this.playerClass = playerClass;
        this.cooldownSeconds = cooldownSeconds;
        this.range = range;
        this.manaCost = manaCost;
    }

    public String displayName() { return displayName; }
    public PlayerClass playerClass() { return playerClass; }
    public int cooldownTicks() { return cooldownSeconds * 20; }
    public double range() { return range; }
    public int manaCost() { return manaCost; }

    public static ActiveSkill forClass(PlayerClass playerClass) {
        for (ActiveSkill skill : values()) {
            if (skill.playerClass == playerClass) return skill;
        }
        return null;
    }
}
