package org.mancala.shared;

public class MancalaDemo {

	/**
	 * @param args
	 */
	public static void main(String[] args) {		

		int[] initNorthPits = {4,4,4,4,4,4,0};
		int[] initSouthPits = {4,4,4,4,4,4,0};
			
		State init = new State(initNorthPits, initSouthPits, PlayerColor.SOUTH);
		init.makeMove(2);
		System.out.println(init.toString());
		System.out.println(init.score());
		System.out.println(init.winner());
	}

}
