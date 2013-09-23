package org.mancala;

import java.util.Arrays;

public class State { 
	public int[] southPits;
	public int[] northPits;
	public PlayerColor whoseTurn;
	public boolean gameOver;
	
	
	public static final PlayerColor INIT_SOUTHS_TURN = PlayerColor.SOUTH;
	public static final int[] INIT_SOUTH_PITS = {4,4,4,4,4,4,0};
	public static final int[] INIT_NORTH_PITS = {4,4,4,4,4,4,0}; //they are the same at the beginning
	public static final State INIT_STATE = new State(INIT_SOUTH_PITS, INIT_NORTH_PITS, INIT_SOUTHS_TURN);
	
	public State() {
		
	}
	
	public State(int[] southPits, int[] northPits, PlayerColor whoseTurn) {
		this.southPits = southPits;
		this.northPits = northPits;
		this.whoseTurn = whoseTurn;
		this.gameOver = false;
	}
	
	public State(int[] southPits, int[] northPits, PlayerColor whoseTurn, boolean gameOver) {
		this.southPits = southPits;
		this.northPits = northPits;
		this.whoseTurn = whoseTurn;
		this.gameOver = gameOver;
	}
	
	public void makeMove(int chosenPitIndex) {
	    //magic
	}
	
	public int[] getPitsOfActivePlayer(){
		return this.whoseTurn.isSouth() ? this.southPits : this.northPits;
	}
	
	@Override
	public String toString() {
	    return "southPits: " + Arrays.toString(southPits) + "\n" + 
	    	   "northPits: " + Arrays.toString(northPits) + "\n" +
	    	   "whoseTurn: "+ whoseTurn;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (gameOver ? 1231 : 1237);
		result = prime * result + Arrays.hashCode(northPits);
		result = prime * result + Arrays.hashCode(southPits);
		result = prime * result
				+ ((whoseTurn == null) ? 0 : whoseTurn.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		State other = (State) obj;
		if (!Arrays.equals(northPits, other.northPits))
			return false;
		if (!Arrays.equals(southPits, other.southPits))
			return false;
		if (whoseTurn != other.whoseTurn)
			return false;
		if (gameOver != other.gameOver)
			return false;
		return true;
	}
	
	
	
}
