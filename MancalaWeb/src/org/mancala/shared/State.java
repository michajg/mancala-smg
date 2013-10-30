package org.mancala.shared;

import java.util.Arrays;

/**
 * The MVP-Model for the Mancala game
 * 
 * @author Micha Guthmann
 */
public class State {

	/*
	 * A Mancala board consists out of 14 pits - 7 for the north player and 7 for the south player. Every player has 6 "normal" pits
	 * and one treasure chest. The pits are mirrored to each other - the pits array indexes can be laid out like this north: 6 5 4 3
	 * 2 1 0 south: 0 1 2 3 4 5 6 where index 6 is the treasure chest of that player.
	 */
	private int[] northPits;
	private int[] southPits;
	private PlayerColor whoseTurn;
	private boolean gameOver;
	/**
	 * A flag needed for the animation
	 */
	private boolean lastMoveWasOppositeCapture;
	/**
	 * Also needed for the animation - how many seeds were in the opposite pit in the case of opposite capture
	 */
	private int oppositeSeeds;

	public State() {
		this.northPits = new int[] { 4, 4, 4, 4, 4, 4, 0 };
		this.southPits = new int[] { 4, 4, 4, 4, 4, 4, 0 };
		this.whoseTurn = PlayerColor.S;
		this.gameOver = false;
	}

	public State(int[] northPits, int[] southPits, PlayerColor whoseTurn) {
		this(northPits, southPits, whoseTurn, false);
	}

	public State(int[] northPits, int[] southPits, PlayerColor whoseTurn, boolean gameOver) {
		this(northPits, southPits, whoseTurn, gameOver, false);
	}

	public State(int[] northPits, int[] southPits, PlayerColor whoseTurn, boolean gameOver, boolean lastMoveWasOppositeCapture) {
		this(northPits, southPits, whoseTurn, gameOver, lastMoveWasOppositeCapture, 0);
	}

	public State(int[] northPits, int[] southPits, PlayerColor whoseTurn, boolean gameOver, boolean lastMoveWasOppositeCapture,
			int oppositeSeeds) {
		if (northPits == null || southPits == null || northPits.length != 7 || southPits.length != 7)
			throw new IllegalArgumentException();
		int helpS = 0;
		int helpN = 0;
		for (int i = 0; i < southPits.length; i++) {
			if (northPits[i] < 0 || southPits[i] < 0)
				throw new IllegalArgumentException();
			helpS += northPits[i];
			helpN += southPits[i];
		}
		if (helpS + helpN != 48)
			throw new IllegalArgumentException();

		this.northPits = northPits;
		this.southPits = southPits;
		this.whoseTurn = whoseTurn;
		this.gameOver = gameOver;
		this.lastMoveWasOppositeCapture = lastMoveWasOppositeCapture;
		this.oppositeSeeds = oppositeSeeds;
	}

	/**
	 * @return whose turn it is
	 */
	public PlayerColor getWhoseTurn() {
		return whoseTurn;
	}

	/**
	 * @return the pits of the north side of the game board
	 */
	public int[] getNorthPits() {
		return this.northPits;
	}

	/**
	 * @return the pits of the south side of the game board
	 */
	public int[] getSouthPits() {
		return this.southPits;
	}

	/**
	 * @return if game is over
	 */
	public boolean isGameOver() {
		return gameOver;
	}

	/**
	 * 
	 */
	public boolean getLastMoveWasOppositeCapture() {
		return lastMoveWasOppositeCapture;
	}

	/**
	 * 
	 */
	public int getOppositeSeeds() {
		return oppositeSeeds;
	}

	/**
	 * A user can make a move by specifying from which pit he wants to distribute the seeds from
	 * <p>
	 * Distribute seeds beginning with the pit corresponding with the chosen index + 1
	 * <p>
	 * Every time one side is completely dealt you continue with the other side
	 * <p>
	 * Only the players own treasure chest receives seeds in the process
	 * <p>
	 * If last seed is placed in a treasure chest, the player moves again
	 * <p>
	 * If last seed is placed in an empty pit, this seed and the opposing seeds all go into the players treasure chest
	 * <p>
	 * If all pits are empty on at least on side the game ends
	 * 
	 * @param chosenPitIndex
	 *          the index corresponding with the user selected pit
	 */
	public void makeMove(int chosenPitIndex) {
		if (this.gameOver == true)
			throw new GameOverException();

		int[] activePits = getPitsOfWhoseTurn();
		int startPlacingSeedsAtThisIndex = chosenPitIndex + 1;
		int seeds = activePits[chosenPitIndex];
		if (seeds <= 0)
			throw new IllegalMoveException();
		boolean dealTreasureChest = true;
		int potentialLastIndex = seeds + startPlacingSeedsAtThisIndex;
		int stopPlacingSeedsAtThisIndex = (potentialLastIndex < activePits.length) ? potentialLastIndex : activePits.length;
		activePits[chosenPitIndex] = 0;
		boolean changePlayer = true;

		while (seeds > 0) {
			for (int j = startPlacingSeedsAtThisIndex; j < stopPlacingSeedsAtThisIndex; j++) {
				activePits[j]++;
				seeds--;

				// check if last seed was placed
				if (seeds == 0) {
					changePlayer = !endedOnTreasureChest(j, activePits);
					manageOppositionCapture(j, activePits);
				}
			}

			// next seeds are placed on the opposite side
			activePits = getOppositePits(activePits);
			// every second time you leave out the treasure chest while dealing seeds
			dealTreasureChest = !dealTreasureChest;
			if (dealTreasureChest)
				stopPlacingSeedsAtThisIndex = seeds < activePits.length ? seeds : activePits.length;
			else
				stopPlacingSeedsAtThisIndex = seeds < activePits.length - 1 ? seeds : activePits.length - 1;
			startPlacingSeedsAtThisIndex = 0;
		}

		if (changePlayer)
			this.whoseTurn = this.whoseTurn.getOpposite();

		if (gameEnded(this.northPits, this.southPits)) {
			this.gameOver = true;
			putLeftoverInTreasureChest(this.northPits);
			putLeftoverInTreasureChest(this.southPits);
		}

	}

	/**
	 * True if the last seed was placed on a treasure chest. Since only the treasure chest of the use whose turn it is receiving
	 * seeds this means the user gets another turn
	 * 
	 * @param indexLastSeedPlaced
	 *          index where the last seed was placed
	 * @param activePits
	 *          the pits (north or south) where the last seed was placed
	 * @return true if the last seed ended on the treasure chest
	 */
	private boolean endedOnTreasureChest(int indexLastSeedPlaced, int[] activePits) {
		return indexLastSeedPlaced == activePits.length - 1; // last seed was placed in players treasure chest
	}

	/**
	 * If the active pits are the south pits return north pits and vice versa
	 * 
	 * @param activePits
	 *          the pits (north or south) where the seeds are being placed
	 * @return the other pits
	 */
	private int[] getOppositePits(int[] activePits) {
		return (activePits == this.southPits) ? this.northPits : this.southPits;
	}

	/**
	 * If last seed was placed on an empty pit and it was not the treasure chest check if the opposing pit is not empty If that's
	 * the case both the last seed and the seeds in the opposing pit are removed from their pits and added to the treasure chest
	 * 
	 * @param indexLastSeedPlaced
	 *          index where the last seed was placed
	 * @param activePits
	 *          the pits (north or south) where the last seed was placed
	 */
	private void manageOppositionCapture(int indexLastSeedPlaced, int[] activePits) {
		this.lastMoveWasOppositeCapture = false;

		if (activePits != getPitsOfWhoseTurn())
			return;
		if ((activePits[indexLastSeedPlaced] == 1) && !endedOnTreasureChest(indexLastSeedPlaced, activePits)) {
			int[] otherPits = getOppositePits(activePits);
			int seedAmountInOppositePit = otherPits[getMirrorIndex(indexLastSeedPlaced, activePits.length - 2)];
			if (seedAmountInOppositePit != 0) {

				this.lastMoveWasOppositeCapture = true;
				this.oppositeSeeds = otherPits[getMirrorIndex(indexLastSeedPlaced, this.southPits.length - 2)];
				// fill treasure chest
				activePits[activePits.length - 1] += activePits[indexLastSeedPlaced] + seedAmountInOppositePit;
				// empty the pits where you took the seeds from
				activePits[indexLastSeedPlaced] = otherPits[getMirrorIndex(indexLastSeedPlaced, this.southPits.length - 2)] = 0;

			}
		}
	}

	/**
	 * All the seeds of one side of the pits are being shifted to the corresponding treasure chest
	 * 
	 * @param pits
	 *          the pits array where all seeds should be in the treasure chest
	 */
	private void putLeftoverInTreasureChest(int[] pits) {
		for (int i = 0; i < pits.length - 1; i++) {
			pits[pits.length - 1] += pits[i];
			pits[i] = 0;
		}
	}

	/**
	 * Checks for the game over condition - the game ends if all of the pits of at least one side is empty
	 * 
	 * @return true if game has ended, false otherwise
	 */
	private boolean gameEnded(int[] northPits, int[] southPits) {
		boolean nZero = true;
		boolean sZero = true;
		for (int i = 0; i < northPits.length - 1; i++) {
			if (northPits[i] != 0)
				nZero = false;
		}
		for (int i = 0; i < southPits.length - 1; i++) {
			if (southPits[i] != 0)
				sZero = false;
		}

		return (nZero || sZero);
	}

	/**
	 * The pits are mirrored to each other the pits arrays are basically like this 6 5 4 3 2 1 0 0 1 2 3 4 5 6 where index 6 is the
	 * treasure chest. This method returns the index of the opposing pit given an index
	 * 
	 * @param index
	 *          the pit to which you want the opposing pit index to
	 * @param maxIndex
	 *          the length of the pits array
	 * @return opposing pit index
	 */
	public static int getMirrorIndex(int index, int maxIndex) {
		return maxIndex - index;
	}

	/**
	 * @return the pits which correspondence to the player whose turn it is
	 */
	public int[] getPitsOfWhoseTurn() {
		return this.whoseTurn.isSouth() ? this.southPits : this.northPits;
	}

	/**
	 * Returns leading player (SOUTH or NORTH). If it's a tie E is getting returned. A player leads if he has more seeds in his
	 * treasure chest than the other
	 * 
	 * @return SOUTH or NORTH if they are leading, E if it's a tie
	 */
	public PlayerColor winner() {
		if (southPits[southPits.length - 1] > northPits[northPits.length - 1])
			return PlayerColor.SOUTH;
		else if (northPits[northPits.length - 1] > southPits[southPits.length - 1])
			return PlayerColor.NORTH;

		return PlayerColor.E;
	}

	/**
	 * Returns leading players score
	 * 
	 * @return the score of the leading player
	 */
	public int score() {
		if (southPits[southPits.length - 1] >= northPits[northPits.length - 1])
			return southPits[southPits.length - 1];
		else
			return northPits[northPits.length - 1];
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (gameOver ? 1231 : 1237);
		result = prime * result + Arrays.hashCode(northPits);
		result = prime * result + Arrays.hashCode(southPits);
		result = prime * result + ((whoseTurn == null) ? 0 : whoseTurn.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		State other = (State) obj;
		if (obj == null || getClass() != obj.getClass() || !Arrays.equals(northPits, other.northPits)
				|| !Arrays.equals(southPits, other.southPits) || whoseTurn != other.whoseTurn || gameOver != other.gameOver)
			return false;

		return true;
	}

	@Override
	public String toString() {
		return "State [northPits=" + Arrays.toString(northPits) + ", southPits=" + Arrays.toString(southPits) + ", whoseTurn="
				+ whoseTurn + ", gameOver=" + gameOver + "]";
	}

	public State copyState() {
		int[] cloneNorth = new int[this.northPits.length];
		int[] cloneSouth = new int[this.northPits.length];
		for (int i = 0; i < this.northPits.length; i++) {
			cloneNorth[i] = this.northPits[i];
			cloneSouth[i] = this.southPits[i];
		}
		return new State(cloneNorth, cloneSouth, this.whoseTurn, this.gameOver, this.lastMoveWasOppositeCapture, this.oppositeSeeds);
	}

	/**
	 * gets a String serialized state and deserializes it into a State object
	 */
	static public State deserialize(String serialized) {

		int[] northPits = new int[7];
		int[] southPits = new int[7];
		PlayerColor whoseTurn = PlayerColor.S;
		boolean gameOver = false;
		boolean lastMoveWasOppositeCapture = false;
		int oppositeSeeds = 0;

		String[] serTokens = serialized.split("_");

		String[] nTokens = serTokens[0].split(",");
		for (int i = 0; i < nTokens.length; i++)
			northPits[i] = Integer.parseInt(nTokens[i]);

		String[] sTokens = serTokens[1].split(",");
		for (int i = 0; i < sTokens.length; i++)
			southPits[i] = Integer.parseInt(sTokens[i]);

		whoseTurn = serTokens[2].charAt(0) == 'N' ? PlayerColor.N : PlayerColor.S;

		gameOver = serTokens[3].charAt(0) == 'F' ? false : true;

		lastMoveWasOppositeCapture = serTokens[4].charAt(0) == 'F' ? false : true;

		oppositeSeeds = Integer.parseInt(serTokens[5]);

		return new State(northPits, southPits, whoseTurn, gameOver, lastMoveWasOppositeCapture, oppositeSeeds);

	}

	/**
	 * Gets a State state and serializes it into a String object The pattern will be: north pits _ south pits _ whose turn _ game
	 * over _ lastMoveWasOppositeCapture _ oppositeSeeds e.g. initial state would be 4,4,4,4,4,4,0_4,4,4,4,4,4,0_S_F_F_0
	 */
	static public String serialize(State state) {
		String serialized = "";
		for (int i = 0; i < state.getNorthPits().length; i++) {
			if (i < state.getNorthPits().length - 1)
				serialized += state.getNorthPits()[i] + ",";
			else
				serialized += state.getNorthPits()[i] + "_";
		}

		for (int i = 0; i < state.getSouthPits().length; i++) {
			if (i < state.getSouthPits().length - 1)
				serialized += state.getSouthPits()[i] + ",";
			else
				serialized += state.getSouthPits()[i] + "_";
		}

		serialized += state.getWhoseTurn().toString() + "_";

		serialized += state.isGameOver() ? "T_" : "F_";

		serialized += state.getLastMoveWasOppositeCapture() ? "T_" : "F_";

		serialized += state.getOppositeSeeds() + "";

		return serialized;
	}
}
