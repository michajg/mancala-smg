package org.mancala.client;

import org.mancala.shared.PlayerColor;
import org.mancala.shared.State;

/**
 * The view has to support these methods to communicate with the presenter
 * 
 * @author Micha Guthmann
 */
public interface View {

	/**
	 * The seedAmount will be placed on the right side at the correct index
	 */
	void setSeeds(PlayerColor side, int col, int seedAmount);

	/**
	 * A player can only select certain pits for their move. That's why some have to be enabled and some have to be disabled before
	 * a player's turn.
	 */
	void setPitEnabled(PlayerColor side, int col, boolean enabled);

	/**
	 * /** Informs the user of certain events. If the parameter for the buttons is null no button will be displayed. The first
	 * button makes the information disappear, the second starts a new game
	 */
	void setMessage(String labelMsg, String HideBtnText, String restartBtnText);

	/**
	 * Animate seeds moving from one pit to an other
	 */
	void animateFromPitToPit(PlayerColor startSide, int startCol, PlayerColor endSide, int endCol, double delay,
			boolean finalAnimation);

	/**
	 * Cancel an animation (for if a game is loaded in the middle of an animation)
	 */
	void cancelAnimation();

	/**
	 * Plays a sound for the event of a game finishing
	 */
	void gameOverSound();

	/**
	 * Plays a sound for the event of catching seeds from the opposite pit
	 */
	void oppositeCaptureSound();

	/**
	 * Show the user name
	 */
	void setUserName(String userName);

	/**
	 * Show email address
	 */
	void setEmail(String email);

	/**
	 * Show status
	 */
	void setStatus(String status);

	/**
	 * Send the move just made to the server
	 * 
	 * @param serializedMove
	 *          The String representation of the move
	 * @param stateString
	 *          The String representation of the state
	 */
	void sendMoveToServer(Integer chosenIndex, String stateString);

	/**
	 * Updates the matches listbox
	 */
	void updateMatchList();

	void windowAlert(String serverError);

	// void saveMoveInServer(Integer aiMove, State state);
	void sendMoveToServerAI(Integer chosenIndex, State state);

	boolean getAiMatch();
}
