package fr.arnaud.siege.game;

public interface GameState {

    void onEnter();
    void onUpdate(long elapsedTime);
    void onExit();
    String getName();

    boolean canBreakBlocks();
    boolean canPlaceBlocks();
    boolean canReceiveDamage();
}