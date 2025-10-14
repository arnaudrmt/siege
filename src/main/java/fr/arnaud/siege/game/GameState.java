package fr.arnaud.siege.game;

/**
 * Represents a state in the Siege game lifecycle.
 */
public interface GameState {

    /**
     * Called when the state is entered.
     */
    void onEnter();

    /**
     * Called periodically while the state is active.
     * @param elapsedTime time elapsed in seconds since state started
     */
    void onUpdate(long elapsedTime);

    /**
     * Called when the state is exited.
     */
    void onExit();

    /**
     * Returns the name of the state.
     */
    String getName();
}