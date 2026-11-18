package dev.eternia.talents;

import dev.eternia.classes.PlayerClass;

public enum TalentBranch {
    WARRIOR_DEFENSE("Defensa", PlayerClass.WARRIOR), WARRIOR_COMBAT("Combate", PlayerClass.WARRIOR), WARRIOR_WEAPONS("Armas", PlayerClass.WARRIOR),
    MAGE_ELEMENTAL("Elemental", PlayerClass.MAGE), MAGE_NECROMANCY("Nigromancia", PlayerClass.MAGE), MAGE_SUMMONING("Invocación", PlayerClass.MAGE),
    ARCHER_SNIPER("Francotirador", PlayerClass.ARCHER), ARCHER_EXPLORATION("Exploración", PlayerClass.ARCHER), ARCHER_BEAST_HUNT("Caza de Bestias", PlayerClass.ARCHER),
    ASSASSIN_SHADOW("Sombras", PlayerClass.ASSASSIN), ASSASSIN_POISON("Venenos", PlayerClass.ASSASSIN), ASSASSIN_NIGHT_HUNT("Caza Nocturna", PlayerClass.ASSASSIN),
    PRIEST_HEALING("Sanación", PlayerClass.PRIEST), PRIEST_TEMPLAR("Templario", PlayerClass.PRIEST), PRIEST_EXORCISM("Exorcismo", PlayerClass.PRIEST);

    private final String displayName;
    private final PlayerClass playerClass;

    TalentBranch(String displayName, PlayerClass playerClass) {
        this.displayName = displayName;
        this.playerClass = playerClass;
    }

    public String displayName() { return displayName; }
    public PlayerClass playerClass() { return playerClass; }

    public static TalentBranch[] forClass(PlayerClass playerClass) {
        return java.util.Arrays.stream(values()).filter(branch -> branch.playerClass == playerClass).toArray(TalentBranch[]::new);
    }
}
