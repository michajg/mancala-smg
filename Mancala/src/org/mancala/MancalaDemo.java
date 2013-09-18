package org.mancala;

public class MancalaDemo {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Pit pit = new Pit();
		System.out.println(pit.toString());
		
		boolean initSouthsTurn = true;
		Pit[] initSouthPits = pitsGenerator(7,4,6);
		Pit[] initNorth = pitsGenerator(7,4,6);
			
		State INIT_STATE = new State(initSouthPits, initNorth, initSouthsTurn);
		System.out.println(INIT_STATE.toString());
	}
	
	private static Pit[] pitsGenerator(int numberOfPits, int seeds, int treasureChestIndex){
		Pit [] testPits = new Pit[numberOfPits];
		for(int i = 0; i < numberOfPits; i++)
			testPits[i] = new Pit(seeds, false);
		
		if(treasureChestIndex < testPits.length)
			testPits[treasureChestIndex] = new Pit(0, true);	
		
		return testPits;			
	}

}
