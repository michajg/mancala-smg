package org.mancala.client;

import java.util.List;

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
	 * Informs the user of certain events.
	 */
	void setMessage(String labelMsg);

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

	void windowAlert(String serverError);

	void setUserNameLabelText(String text);

	void setWarnLabelText(String text);

	void setOpponentNameLabelText(String text);

	void setStartDateLabelText(String text);

	void setTurnLabelText(String text);

	void setSideLabelText(String text);

	void setAiMovesLabelTextTriggerAiMove(String text, Graphics graphics);

	void setContactsInList(List<ContactInfo> contacts);

	void clearMatchDisplay();

}
