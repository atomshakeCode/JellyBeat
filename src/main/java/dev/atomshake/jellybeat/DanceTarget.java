package dev.atomshake.jellybeat;
public enum DanceTarget {
 BLOCKS("Блоки"), BOATS("Лодки / плоты"), ENTITIES("Мобы / сущности"), PLAYERS("Игроки");
 public final String label;
 DanceTarget(String label){this.label=label;}
}
