package org.mancala;

import java.util.Arrays;

public class State { 
	public Pit[] southPits;
	public Pit[] northPits;
	public boolean southsTurn;
	public boolean gameOver;
	
	
	public static final boolean INIT_SOUTHS_TURN = true;
	public static final Pit[] INIT_SOUTH_PITS = {new Pit(4,false),new Pit(4,false),new Pit(4,false),new Pit(4,false),new Pit(4,false),new Pit(4,false),new Pit(0,true)};
	public static final Pit[] INIT_NORTH_PITS = INIT_SOUTH_PITS; //they are the same at the beginning
	public static final State INIT_STATE = new State(INIT_SOUTH_PITS, INIT_NORTH_PITS, INIT_SOUTHS_TURN);
	
	public State() {
		
	}
	
	public State(Pit[] southPits, Pit[] northPits, boolean southsTurn) {
		this.southPits = southPits;
		this.northPits = northPits;
		this.southsTurn = southsTurn;
		this.gameOver = false;
	}
	
	public State(Pit[] southPits, Pit[] northPits, boolean southsTurn, boolean gameOver) {
		this.southPits = southPits;
		this.northPits = northPits;
		this.southsTurn = southsTurn;
		this.gameOver = gameOver;
	}
	
	public State makeMove(int chosenPitIndex) {
	    //magic
		State nextState = this;
	    return nextState;
	}
	
	public Pit[] getPitsOfActivePlayer(){
		return this.southsTurn ? this.southPits : this.northPits;
	}
	
	@Override
	public String toString() {
	    return "southPits: " + Arrays.toString(southPits) + "\n" + 
	    	   "northPits: " + Arrays.toString(northPits) + "\n" +
	    	   "southsTurn: "+ southsTurn;
	}
	
}
