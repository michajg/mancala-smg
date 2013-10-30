package org.mancala.client;

import org.mancala.shared.GameOverException;
import org.mancala.shared.IllegalMoveException;
import org.mancala.shared.PlayerColor;
import org.mancala.shared.State;

import com.google.gwt.core.client.GWT;

/**
 * The MVP-Presenter of the Mancala game
 * 
 * @author Micha Guthmann
 */
public class Presenter {

	/**
	 * The view of the MVP pattern the presenter will use
	 */
	final View graphics;

	/**
	 * The model of the MVP pattern the presenter will use
	 */
	State state;

	/**
	 * keeps track of which side the player is. He is either South or North
	 */
	private PlayerColor usersSide;

	private static MancalaMessages messages = GWT.create(MancalaMessages.class);

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
		 * A player can only select certain pits for their move. That's why some have to be enabled and some have to be disabled
		 * before a player's turn.
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
	}

	/**
	 * 1. Sets the view 2. Checks if there is already a state in the url fragment and if so initializes this 3. Initializes the
	 * History 4. Updates the gaming board accordingly
	 */
	public Presenter(View graphics) {
		this.graphics = graphics;
		state = new State();
		// updateBoard();
	}

	/**
	 * makes a move on the model, adds the new state to the history and updates the board
	 */
	void makeMove(int index) {
		try {
			State oldState = state.copyState();
			state.makeMove(index);

			// graphics.sendMoveToServer() is called by the graphics class after the animation is done
			enableActiveSide();
			disableZeroSeedPits();
			animateMove(index, oldState);
			message();

		} catch (IllegalMoveException e) {
			graphics.setMessage(messages.newGameBecauseError() + e, "Okay", null);
			setState(new State());
		} catch (GameOverException e) {
			graphics.setMessage(messages.newGameBecauseError() + e, "Okay", null);
			setState(new State());
		}
	}

	/**
	 * Updates all elements that are necessary after the state changed 1. Update all the seedAmounts in the pits after a move was
	 * made 2. It enables only the pits from the player whose turn it is 3. When there are zero seeds in a pit it can't be chosen
	 * either so disable them 4. Set a message in the case of game over
	 */
	void updateBoard() {

		if (usersSide == null)
			graphics.setStatus(messages.startNewGame());
		else {
			graphics.setStatus(state.getWhoseTurn().equals(usersSide) ? messages.itsYourTurn() : messages.opponentsTurn());
		}
		updatePits();
		enableActiveSide();
		disableZeroSeedPits();
		message();
	}

	/**
	 * It enables only the pits from the player whose turn it is
	 */
	void enableActiveSide() {
		boolean enableNorth;
		boolean enableSouth;

		if (usersSide == null) {// sideTheUserIs is not yet initialized
			enableNorth = false;
			enableSouth = false;
		}
		else if (state.getWhoseTurn().isNorth() && usersSide.isNorth()) {
			enableNorth = true;
			enableSouth = false;
		}
		else if (state.getWhoseTurn().isNorth() && usersSide.isSouth()) {
			enableNorth = false;
			enableSouth = false;
		}
		else if (state.getWhoseTurn().isSouth() && usersSide.isSouth()) {
			enableNorth = false;
			enableSouth = true;
		}
		else {
			enableNorth = false;
			enableSouth = false;
		}

		for (int i = 0; i < state.getNorthPits().length - 1; i++) {
			graphics.setPitEnabled(PlayerColor.N, i, enableNorth);
			graphics.setPitEnabled(PlayerColor.S, i, enableSouth);
		}
	}

	/**
	 * When there are zero seeds in a pit it can't be chosen either so disable them
	 */
	void disableZeroSeedPits() {
		int[] activePits = new int[7];
		if (state.getWhoseTurn().equals(PlayerColor.N))
			activePits = state.getNorthPits();
		else
			activePits = state.getSouthPits();

		// state.getNorthPits().length-1 because the last array field is the
		// treasure chest
		for (int i = 0; i < state.getNorthPits().length - 1; i++) {
			if (activePits[i] == 0)
				graphics.setPitEnabled(state.getWhoseTurn(), i, false);
		}
	}

	/**
	 * Set a message in the case of game over
	 */
	void message() {
		if (state.isGameOver()) {
			graphics.gameOverSound();

			if (state.winner() == null) {
				graphics.setMessage(messages.tie(), "okay", messages.playAgain());
			}
			else {
				String winner = state.winner().equals(PlayerColor.N) ? "North" : "South";
				graphics.setMessage(messages.winnerIs(winner, state.score() + ""), "Okay", messages.playAgain());
			}
		}
	}

	/**
	 * Update all the seedAmounts in the pits after a move was made
	 */
	private void updatePits() {
		for (int i = 0; i < state.getNorthPits().length; i++) {
			graphics.setSeeds(PlayerColor.N, i, state.getNorthPits()[i]);
			graphics.setSeeds(PlayerColor.S, i, state.getSouthPits()[i]);
		}
	}

	/**
	 * After the user clicked on a pit the seeds should be distributed in an animated fashion
	 * 
	 * @param chosenPitIndex
	 *          the index the user chose to distribute the seeds from
	 * @param oldState
	 *          the state before the user chose his pit
	 */
	private void animateMove(int chosenPitIndex, State oldState) {
		// disable board until the animation is over
		disableBoard();

		PlayerColor whoseTurn = oldState.getWhoseTurn();
		PlayerColor sideToPlaceSeedOn = whoseTurn;
		int seedAmount = oldState.getPitsOfWhoseTurn()[chosenPitIndex];
		boolean lastAnimation = false;
		int indexToPlaceSeedIn = chosenPitIndex;
		int maxIndex = 6;
		for (int i = 1; i <= seedAmount; i++) {
			indexToPlaceSeedIn++;
			maxIndex = whoseTurn.equals(sideToPlaceSeedOn) ? 6 : 5;
			if ((indexToPlaceSeedIn) > maxIndex) {
				sideToPlaceSeedOn = sideToPlaceSeedOn.getOpposite();
				indexToPlaceSeedIn = 0;
			}
			if (i == seedAmount)
				lastAnimation = true;
			graphics
					.animateFromPitToPit(whoseTurn, chosenPitIndex, sideToPlaceSeedOn, indexToPlaceSeedIn, 400 * (i - 1), lastAnimation);

		}

		if (this.state.getLastMoveWasOppositeCapture()) {
			// graphics.oppositeCaptureSound();

			// TODO: give this it's own animation

			// //int[] opposingPits = whoseTurn.isNorth() ?
			// this.state.getSouthPits() : state.getNorthPits();
			// int seedAmountInOpposingPit = this.state.getOppositeSeeds();
			//
			// graphics.animateFromPitToPit(whoseTurn, indexToPlaceSeedIn,
			// whoseTurn, 6, seedAmount * 400 + 1400);
			// for(int i = 0; i < seedAmountInOpposingPit; i++)
			// graphics.animateFromPitToPit(whoseTurn.getOpposite(),
			// State.getMirrorIndex(indexToPlaceSeedIn, 5), whoseTurn, 6,
			// seedAmount * 400 + 1000 + 400 * i);
		}
	}

	public void afterAnimation() {
		if (state.getLastMoveWasOppositeCapture())
			graphics.oppositeCaptureSound();

		updateBoard();
		// TODO: insert correct move to make animation happen everywhere later
		graphics.sendMoveToServer(new Integer(0), State.serialize(state));
		// graphics.updateMatchList();
	}

	void setState(State state) {
		this.state = state;

		updateBoard();
	}

	public void newGame() {
		setState(new State());
	}

	public void clearBoard() {
		// update pits
		for (int i = 0; i < state.getNorthPits().length; i++) {
			graphics.setSeeds(PlayerColor.N, i, 0);
			graphics.setSeeds(PlayerColor.S, i, 0);
		}

		// disable pits
		disableBoard();
	}

	public void disableBoard() {
		for (int i = 0; i < state.getNorthPits().length - 1; i++) {
			graphics.setPitEnabled(PlayerColor.N, i, false);
			graphics.setPitEnabled(PlayerColor.S, i, false);
		}
	}

	public void setUsersSide(PlayerColor side) {
		usersSide = side;
	}
}
