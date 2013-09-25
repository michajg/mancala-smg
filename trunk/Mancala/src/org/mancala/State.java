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
		this(southPits, northPits, whoseTurn, false);
	}
	
	public State(int[] southPits, int[] northPits, PlayerColor whoseTurn, boolean gameOver) {	
		if(southPits == null || northPits == null || southPits.length != 7 || northPits.length != 7) 
			throw new IllegalArgumentException();
		int helpS = 0;
		int helpN = 0;
		for(int i = 0; i < southPits.length; i++){
			if(southPits[i] < 0 || northPits[i] < 0)
				throw new IllegalArgumentException();
			helpS += southPits[i];
			helpN += northPits[i]; 
		}
		if(helpS + helpN != 48)
			throw new IllegalArgumentException();
		
		this.southPits = southPits;
		this.northPits = northPits;
		this.whoseTurn = whoseTurn;
		this.gameOver = gameOver;
	}
	
	public void makeMove(int chosenPitIndex) {
		// give seeds beginning with the pit corresponding with the chosen index + 1
		// every time one side is completely dealt you continue with the other side
		// only the players own treasure chest receives seeds in the process
		
		// if last seed was in a treasure chest, the player moves again 
		// if last was in an empty pit opposing seeds all go into the players treasure chest
		// check if the game has ended
		if(this.gameOver == true)
			throw new GameOverException();
		
		int[] activePits = getPitsOfActivePlayer();
		int pitIndexWhereSeedsAreBeeingPlaced = chosenPitIndex + 1;
		int seeds = activePits[chosenPitIndex];
		if(seeds <= 0)
			throw new IllegalMoveException();
		boolean treasureIsDealt = true;
		int potentialLastIndex = seeds + pitIndexWhereSeedsAreBeeingPlaced;
		int dealSeedsUpToThisIndex = (potentialLastIndex < activePits.length) ? potentialLastIndex : activePits.length;
		activePits[chosenPitIndex] = 0;
		boolean changePlayer = true;
		
		while(seeds > 0) {
			for(int j = pitIndexWhereSeedsAreBeeingPlaced; j < dealSeedsUpToThisIndex; j++){
				activePits[j]++;
				seeds--;
				
				if(seeds == 0){ //last seed was placed
					changePlayer = falseIfEndedOnTreasureChest(j);
					testForAndManageOppositionCapture(j, activePits);					
				}
			}
			
			activePits = (activePits == this.southPits) ? this.northPits : this.southPits; //now seeds are placed on the opposite side
			treasureIsDealt = !treasureIsDealt; //every second time you leave out the treasure chest while dealing seeds
			if(treasureIsDealt)
				dealSeedsUpToThisIndex = seeds < activePits.length     ? seeds : activePits.length;
			else 
				dealSeedsUpToThisIndex = seeds < activePits.length - 1 ? seeds : activePits.length - 1;	
			pitIndexWhereSeedsAreBeeingPlaced = 0; 			
		}
		
		if(changePlayer)
			this.whoseTurn = this.whoseTurn.getOpposite();
		
		if(gameEnded()){
			this.gameOver = true;
			putLeftoverInTreasureChest(this.southPits);
			putLeftoverInTreasureChest(this.northPits);
		}
			
	}
	
	private boolean falseIfEndedOnTreasureChest(int j){
		return !(j == this.southPits.length-1); //last seed was placed in players treasure chest
	}
	
	private void testForAndManageOppositionCapture(int j, int[] activePits){
		//last seed was placed on an empty pit and it was not the treasure chest
		if((activePits[j] == 1) && (j != activePits.length-1) ){ 
			int[] otherPits = (activePits == this.southPits) ? this.northPits : this.southPits;
			if(otherPits[getMirrorIndex(j, this.southPits.length-2)] != 0){ //opposite pit was not empty
				activePits[this.southPits.length-1] += 1 + otherPits[getMirrorIndex(j, this.southPits.length-2)];
				activePits[j] = otherPits[getMirrorIndex(j, this.southPits.length-2)] = 0;						
			} 
		}	
	}
	
	private void putLeftoverInTreasureChest(int[] array){
		for(int i = 0; i < array.length-2; i++){
			array[array.length-1] += array[i];
			array[i] = 0; 
		}
	}
	
	private boolean gameEnded(){
		int[] zerosArray = new int[southPits.length-1];
		int[] helpArray = Arrays.copyOfRange(southPits, 0, southPits.length-1);
		if(Arrays.equals(helpArray, zerosArray))
			return true;
		helpArray = Arrays.copyOfRange(northPits, 0, northPits.length-1);
		if(Arrays.equals(helpArray, zerosArray))
			return true;
		
		return false;
	}
	
	private int getMirrorIndex(int index, int length){
		return length - index;
	}
	
	public int[] getPitsOfActivePlayer(){
		return this.whoseTurn.isSouth() ? this.southPits : this.northPits;
	}
	
	public int[] getPitsOfInactivePlayer(){
		return this.whoseTurn.isSouth() ? this.northPits : this.southPits;
	}
	
	@Override
	public String toString() {
	    return "southPits: " + Arrays.toString(southPits) + "\n" + 
	    	   "northPits: " + Arrays.toString(northPits) + "\n" +
	    	   "whoseTurn: "+ whoseTurn + "\n" +
	    	   "gameOver: " + gameOver;
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
