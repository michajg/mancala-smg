package org.mancala;

public class MancalaDemo {

	/**
	 * @param args
	 */
	public static void main(String[] args) {		
		
		int[] initSouthPits = {4,4,4,4,4,4,0};
		int[] initNorth = {4,4,4,4,4,4,0};
			
		State INIT_STATE = new State(initSouthPits, initNorth, PlayerColor.SOUTH);
		System.out.println(INIT_STATE.toString());
	}

}
