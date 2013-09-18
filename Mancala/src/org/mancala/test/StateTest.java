package org.mancala.test;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mancala.GameOverException;
import org.mancala.IllegalMoveException;
import org.mancala.Pit;
import org.mancala.State;

public class StateTest {
	
	public final Pit[] STANDARD_PITS = pitsGenerator(7, 4, 6);

	@Test
	public void testConstructorWithNormalValuesIsWorking() {
		new State(STANDARD_PITS, STANDARD_PITS, true);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorWithNullSouthPits() {
		new State(null, STANDARD_PITS, true);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorWithNullNorthPits() {
		new State(STANDARD_PITS, null, true);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorWithNotEnoughSouthPits() {
		//There should be exactly 7 Pits		
		Pit[] testPits = pitsGenerator(6, 4, 5);
		new State(testPits, STANDARD_PITS, true);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorWithTooManySouthPits() {
		//There should be exactly 7 Pits
		Pit[] testPits = pitsGenerator(8, 4, 7);
		new State(testPits, STANDARD_PITS, true);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorWithNotEnoughNorthPits() {
		//There should be exactly 7 Pits
		Pit[] testPits = pitsGenerator(6, 4, 5);
		new State(STANDARD_PITS, testPits, true);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorWithTooManyNorthPits() {
		//There should be exactly 7 Pits
		Pit[] testPits = pitsGenerator(8, 4, 7);
		new State(STANDARD_PITS, testPits, true);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorWithNotEnoughTreasureChestsInSouthPits() {
		//There should be exactly 1 Pit that has the treasureChestFlag set to true
		Pit[] testPits = pitsGenerator(7, 4, 6);
		testPits[6] = new Pit(0,false);
		new State(testPits, STANDARD_PITS, true);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorWithTooManyTreasureChestsInSouthPits() {
		//There should be exactly 1 Pit that has the treasureChestFlag set to true
		Pit[] testPits = pitsGenerator(7, 4, 6);
		testPits[5] = new Pit(0,true);
		new State(testPits, STANDARD_PITS, true);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorWithNotEnoughTreasureChestsInNorthPits() {
		//There should be exactly 1 Pit that has the treasureChestFlag set to true
		Pit[] testPits = pitsGenerator(7, 4, 6);
		testPits[6] = new Pit(0,false);
		new State(STANDARD_PITS, testPits, true);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorWithTooManyTreasureChestsInNorthPits() {
		//There should be exactly 1 Pit that has the treasureChestFlag set to true
		Pit[] testPits = pitsGenerator(7, 4, 6);
		testPits[5] = new Pit(0,true);
		new State(STANDARD_PITS, testPits, true);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorWithSouthTreasureChestAtWrongPlace() {
		//the treasure chest pit should be the last pit of the array
		Pit[] testPits = pitsGenerator(7, 4, 6);
		testPits[0] = new Pit(0,true);
		testPits[6] = new Pit(4,false);
		new State(testPits, STANDARD_PITS, true);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorWithNorthTreasureChestAtWrongPlace() {
		//the treasure chest pit should be the last pit of the array
		Pit[] testPits = pitsGenerator(7, 4, 6);
		testPits[0] = new Pit(0,true);
		testPits[6] = new Pit(4,false);
		new State(STANDARD_PITS, testPits, true);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorWithNegativeSeedsInTheSouthPits() {
		//there is never a negative amount of seeds in one of the pits
		Pit[] testPits = pitsGenerator(7, -1, 6);
		new State(testPits, STANDARD_PITS, true);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorWithNegativeSeedsInTheNorthPits() {
		//there is never a negative amount of seeds in one of the pits
		Pit[] testPits = pitsGenerator(7, -1, 6);
		new State(STANDARD_PITS, testPits, true);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorWithDifferentAmountOfPitsForSouthAndNorth() {
		//there should always be the same number of pits in both south and north
		Pit[] testPits = pitsGenerator(6, 4, 5);
		Pit[] testPits2 = pitsGenerator(8, 4, 6);
		new State(testPits, testPits2, true);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorWithWrongNumberOfSeedsTotal() {
		//there should always be a total of 48 seeds in the game
		Pit[] testPits = pitsGenerator(7, 5, 6);
		Pit[] testPits2 = pitsGenerator(7, 5, 6);
		new State(testPits, testPits2, true);
	}
	
	@Test
	public void testMoveOnlyOnAvtiveSide() {
		Pit[] southPits = STANDARD_PITS;
		Pit[] northPits = STANDARD_PITS;
		boolean southsTurn = true;
		State initialState = new State(southPits, northPits, southsTurn);
		State afterMoveState = initialState.makeMove(0);
		
		Pit[] expectedSouthPits = pitsGeneratorBySpecifyingSeeds(0, 5, 5, 5, 5, 4, 0);
		Pit[] expectedNorthPits = pitsGenerator(7, 4, 6);
		boolean expectedSouthsTurn = false;
		State expectedState = new State(expectedSouthPits, expectedNorthPits, expectedSouthsTurn);
		
		assertTrue("From init south takes index 0 seeds: ", compareStates(afterMoveState, expectedState));
	}
	
	@Test
	public void testMoveAcrossOneSide() {
		Pit[] southPits = STANDARD_PITS;
		Pit[] northPits = STANDARD_PITS;
		boolean southsTurn = true;
		State initialState = new State(southPits, northPits, southsTurn);
		State afterMoveState = initialState.makeMove(5);
		
		Pit[] expectedSouthPits = pitsGeneratorBySpecifyingSeeds(4, 4, 4, 4, 4, 0, 1);
		Pit[] expectedNorthPits = pitsGeneratorBySpecifyingSeeds(5, 5, 5, 4, 4, 4, 0);
		boolean expectedSouthsTurn = false;
		State expectedState = new State(expectedSouthPits, expectedNorthPits, expectedSouthsTurn);
		
		assertTrue("From init south takes index 5 seeds: ", compareStates(afterMoveState, expectedState));
	}
	
	@Test
	public void testMoveAcrossTwoSidesSouth() {
		Pit[] southPits = pitsGeneratorBySpecifyingSeeds(1, 1, 1, 1, 1, 17, 10);
		Pit[] northPits = pitsGeneratorBySpecifyingSeeds(1, 1, 1, 1, 1, 1, 10);
		boolean southsTurn = true;
		State initialState = new State(southPits, northPits, southsTurn);
		State afterMoveState = initialState.makeMove(5);
		
		Pit[] expectedSouthPits = pitsGeneratorBySpecifyingSeeds(2, 2, 2, 2, 2, 1, 12);
		Pit[] expectedNorthPits = pitsGeneratorBySpecifyingSeeds(3, 3, 3, 2, 2, 2, 10);
		boolean expectedSouthsTurn = false;
		State expectedState = new State(expectedSouthPits, expectedNorthPits, expectedSouthsTurn);
		
		assertTrue("From custom south takes index 5 seeds: ", compareStates(afterMoveState, expectedState));
	}
	
	@Test
	public void testMoveAcrossTwoSidesNorth() {
		Pit[] southPits = pitsGeneratorBySpecifyingSeeds(1, 1, 1, 1, 1, 1, 10);
		Pit[] northPits = pitsGeneratorBySpecifyingSeeds(1, 1, 1, 1, 1, 17, 10);
		boolean southsTurn = false;
		State initialState = new State(southPits, northPits, southsTurn);
		State afterMoveState = initialState.makeMove(5);
		
		Pit[] expectedSouthPits = pitsGeneratorBySpecifyingSeeds(3, 3, 3, 2, 2, 2, 10);
		Pit[] expectedNorthPits = pitsGeneratorBySpecifyingSeeds(2, 2, 2, 2, 2, 1, 12);
		boolean expectedSouthsTurn = true;
		State expectedState = new State(expectedSouthPits, expectedNorthPits, expectedSouthsTurn);
		
		assertTrue("From custom south takes index 5 seeds: ", compareStates(afterMoveState, expectedState));
	}
	
	@Test
	public void testMoveLastOnTreasureChestSouth() {
		//If the last seed of a move landed on your treasure chest, then you can move again
		Pit[] southPits = STANDARD_PITS;
		Pit[] northPits = STANDARD_PITS;
		boolean southsTurn = true;
		State initialState = new State(southPits, northPits, southsTurn);
		State afterMoveState = initialState.makeMove(2);
		
		Pit[] expectedSouthPits = pitsGeneratorBySpecifyingSeeds(4, 4, 0, 5, 5, 5, 1);
		Pit[] expectedNorthPits = pitsGeneratorBySpecifyingSeeds(4, 4, 4, 4, 4, 4, 0);
		boolean expectedSouthsTurn = true;
		State expectedState = new State(expectedSouthPits, expectedNorthPits, expectedSouthsTurn);
		
		assertTrue("From custom south takes index 2 seeds: ", compareStates(afterMoveState, expectedState));
	}
	
	@Test
	public void testMoveLastOnTreasureChestNorth() {
		//If the last seed of a move landed on your treasure chest, then you can move again
		Pit[] southPits = STANDARD_PITS;
		Pit[] northPits = STANDARD_PITS;
		boolean southsTurn = false;
		State initialState = new State(southPits, northPits, southsTurn);
		State afterMoveState = initialState.makeMove(2);
		
		Pit[] expectedSouthPits = pitsGeneratorBySpecifyingSeeds(4, 4, 4, 4, 4, 4, 0);
		Pit[] expectedNorthPits = pitsGeneratorBySpecifyingSeeds(4, 4, 0, 5, 5, 5, 1);
		boolean expectedSouthsTurn = false;
		State expectedState = new State(expectedSouthPits, expectedNorthPits, expectedSouthsTurn);
		
		assertTrue("From custom south takes index 2 seeds: ", compareStates(afterMoveState, expectedState));
	}
	
	@Test
	public void testMoveCaptureOpposingSeedsSouth() {
		//If the last seed of a move landed on an empty pit on your side and there are some seeds in the opposite pit, 
		//then the seeds in the two cups will be captured in your treasure chest
		Pit[] southPits = pitsGeneratorBySpecifyingSeeds(2, 2, 2, 0, 2, 2, 12);
		Pit[] northPits = pitsGeneratorBySpecifyingSeeds(2, 2, 4, 2, 2, 2, 12);
		boolean southsTurn = true;
		State initialState = new State(southPits, northPits, southsTurn);
		State afterMoveState = initialState.makeMove(1);
		
		Pit[] expectedSouthPits = pitsGeneratorBySpecifyingSeeds(2, 0, 3, 0, 2, 2, 17);
		Pit[] expectedNorthPits = pitsGeneratorBySpecifyingSeeds(2, 2, 0, 2, 2, 2, 12);
		boolean expectedSouthsTurn = false;
		State expectedState = new State(expectedSouthPits, expectedNorthPits, expectedSouthsTurn);
		
		assertTrue("From custom south takes index 1 seeds: ", compareStates(afterMoveState, expectedState));
	}
	
	@Test
	public void testMoveCaptureOpposingSeedsNorth() {
		//If the last seed of a move landed on an empty pit on your side and there are some seeds in the opposite pit, 
		//then the seeds in the two cups will be captured in your treasure chest
		Pit[] southPits = pitsGeneratorBySpecifyingSeeds(2, 2, 4, 2, 2, 2, 12);
		Pit[] northPits = pitsGeneratorBySpecifyingSeeds(2, 2, 2, 0, 2, 2, 12);
		boolean southsTurn = false;
		State initialState = new State(southPits, northPits, southsTurn);
		State afterMoveState = initialState.makeMove(1);
		
		Pit[] expectedSouthPits = pitsGeneratorBySpecifyingSeeds(2, 2, 0, 2, 2, 2, 12);
		Pit[] expectedNorthPits = pitsGeneratorBySpecifyingSeeds(2, 0, 3, 0, 2, 2, 17);
		boolean expectedSouthsTurn = true;
		State expectedState = new State(expectedSouthPits, expectedNorthPits, expectedSouthsTurn);
		
		assertTrue("From custom south takes index 1 seeds: ", compareStates(afterMoveState, expectedState));
	}
	
	@Test
	public void testMoveFinishSouth() {
		//The game ends when all the pits on one players side are empty. 
		//All remaining seeds that are still in the pits of the opposing player go into his treasure chest.
		Pit[] southPits = pitsGeneratorBySpecifyingSeeds(0, 0, 0, 0, 0, 2, 30);
		Pit[] northPits = pitsGeneratorBySpecifyingSeeds(0, 0, 0, 6, 0, 0, 10);
		boolean southsTurn = true;
		State initialState = new State(southPits, northPits, southsTurn);
		State afterMoveState = initialState.makeMove(5);
		
		Pit[] expectedSouthPits = pitsGeneratorBySpecifyingSeeds(0, 0, 0, 0, 0, 0, 31);
		Pit[] expectedNorthPits = pitsGeneratorBySpecifyingSeeds(0, 0, 0, 0, 0, 0, 17);
		boolean expectedSouthsTurn = false;
		boolean expectedGameOver = true;
		State expectedState = new State(expectedSouthPits, expectedNorthPits, expectedSouthsTurn, expectedGameOver);
		
		assertTrue("South provokes GameOver: ", compareStates(afterMoveState, expectedState));
	}
	
	@Test
	public void testMoveFinishNorth() {
		//The game ends when all the pits on one players side are empty. 
		//All remaining seeds that are still in the pits of the opposing player go into his treasure chest.
		Pit[] southPits = pitsGeneratorBySpecifyingSeeds(0, 0, 0, 6, 0, 0, 10);
		Pit[] northPits = pitsGeneratorBySpecifyingSeeds(0, 0, 0, 0, 0, 2, 30);
		boolean southsTurn = false;
		State initialState = new State(southPits, northPits, southsTurn);
		State afterMoveState = initialState.makeMove(5);
		
		Pit[] expectedSouthPits = pitsGeneratorBySpecifyingSeeds(0, 0, 0, 0, 0, 0, 17);
		Pit[] expectedNorthPits = pitsGeneratorBySpecifyingSeeds(0, 0, 0, 0, 0, 0, 31);
		boolean expectedSouthsTurn = true;
		boolean expectedGameOver = true;
		State expectedState = new State(expectedSouthPits, expectedNorthPits, expectedSouthsTurn, expectedGameOver);
		
		assertTrue("South provokes GameOver: ", compareStates(afterMoveState, expectedState));
	}
	
	@Test
	public void testMoveTie() {
		//The game ends when all the pits on one players side are empty. 
		//All remaining seeds that are still in the pits of the opposing player go into his treasure chest.
		Pit[] southPits = pitsGeneratorBySpecifyingSeeds(0, 0, 0, 0, 0, 2, 23);
		Pit[] northPits = pitsGeneratorBySpecifyingSeeds(0, 0, 0, 0, 0, 0, 23);
		boolean southsTurn = true;
		State initialState = new State(southPits, northPits, southsTurn);
		State afterMoveState = initialState.makeMove(5);
		
		Pit[] expectedSouthPits = pitsGeneratorBySpecifyingSeeds(0, 0, 0, 0, 0, 0, 24);
		Pit[] expectedNorthPits = pitsGeneratorBySpecifyingSeeds(0, 0, 0, 0, 0, 0, 24);
		boolean expectedSouthsTurn = false;
		boolean expectedGameOver = true;
		State expectedState = new State(expectedSouthPits, expectedNorthPits, expectedSouthsTurn, expectedGameOver);
		
		assertTrue("South provokes GameOver: ", compareStates(afterMoveState, expectedState));
	}
	
	@Test(expected = GameOverException.class)
	public void testMoveIllegalMoveAfterGameOver() {
		Pit[] southPits = pitsGeneratorBySpecifyingSeeds(0, 0, 0, 0, 0, 0, 20);
		Pit[] northPits = pitsGeneratorBySpecifyingSeeds(0, 0, 0, 0, 0, 0, 28);
		boolean southsTurn = true;
		boolean gameOver = true;
		State initialState = new State(southPits, northPits, southsTurn, gameOver);
		State afterMoveState = initialState.makeMove(5);
	}
	
	@Test(expected = IllegalMoveException.class)
	public void testMoveIllegalMove() {
		//You can't take the seeds out of an empty pit
		Pit[] southPits = pitsGeneratorBySpecifyingSeeds(4, 0, 5, 5, 5, 5, 0);
		Pit[] northPits = pitsGeneratorBySpecifyingSeeds(0, 5, 5, 5, 5, 4, 0);
		boolean southsTurn = true;
		State initialState = new State(southPits, northPits, southsTurn);
		State afterMoveState = initialState.makeMove(1);
	}
	
	private boolean compareStates(State state1, State state2){
		//todo: maybe it would be better to overwrite the equals (and hashcode) methods of State and Pit
		
		if(!(state1.southPits.length == state2.southPits.length) || !(state1.northPits.length == state2.northPits.length))
			return false;
		
		for(int i = 0; i < state1.southPits.length; i++){
			if(!(state1.southPits[i].getSeeds() == state2.southPits[i].getSeeds()) || !(state1.northPits[i].getSeeds() == state2.northPits[i].getSeeds()))
				return false;
			if(!(state1.southPits[i].isTreasureChest() == state2.southPits[i].isTreasureChest()) || !(state1.northPits[i].isTreasureChest() == state2.northPits[i].isTreasureChest()))
				return false;
		}
		
		if(state1.southsTurn != state2.southsTurn)
			return false;
		
		return true;
	}

	private Pit[] pitsGeneratorBySpecifyingSeeds(int pit0, int pit1, int pit2, int pit3, int pit4, int pit5, int pit6){
		Pit [] testPits = new Pit[7];
		testPits[0] = new Pit(pit0, false);
		testPits[1] = new Pit(pit1, false);
		testPits[2] = new Pit(pit2, false);
		testPits[3] = new Pit(pit3, false);
		testPits[4] = new Pit(pit4, false);
		testPits[5] = new Pit(pit5, false);
		testPits[6] = new Pit(pit6, true);
		return testPits;			
	}
	
	private Pit[] pitsGenerator(int numberOfPits, int seeds, int treasureChestIndex){
		Pit [] testPits = new Pit[numberOfPits];
		for(int i = 0; i < numberOfPits; i++)
			testPits[i] = new Pit(seeds, false);
		
		if(treasureChestIndex < testPits.length)
			testPits[treasureChestIndex] = new Pit(0, true);	
		
		return testPits;			
	}

}
