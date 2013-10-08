package org.mancala.shared;

import static org.junit.Assert.*;

import org.junit.Test;
import org.mancala.shared.GameOverException;
import org.mancala.shared.IllegalMoveException;
import org.mancala.shared.PlayerColor;
import org.mancala.shared.State;
public class StateTest {
	
	public final int[] STANDARD_PITS = {4,4,4,4,4,4,0};

	@Test
	public void testConstructorWithNormalValuesIsWorking() {
		new State(STANDARD_PITS.clone(), STANDARD_PITS.clone(), PlayerColor.SOUTH);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorWithNullNorthPits() {
		new State(null, STANDARD_PITS.clone(), PlayerColor.SOUTH);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorWithNullSouthPits() {
		new State(STANDARD_PITS.clone(), null, PlayerColor.SOUTH);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorWithNotEnoughNorthPits() {
		//There should be exactly 7 Pits		
		int[] testPits = {4,4,4,4,4,0};
		new State(testPits, STANDARD_PITS.clone(), PlayerColor.SOUTH);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorWithTooManyNorthPits() {
		//There should be exactly 7 Pits
		int[] testPits = {4,4,4,4,4,4,4,0};
		new State(testPits, STANDARD_PITS.clone(), PlayerColor.SOUTH);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorWithNotEnoughSouthPits() {
		//There should be exactly 7 Pits
		int[] testPits = {4,4,4,4,4,0};
		new State(STANDARD_PITS.clone(), testPits, PlayerColor.SOUTH);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorWithTooManySouthPits() {
		//There should be exactly 7 Pits
		int[] testPits = {4,4,4,4,4,4,4,0};
		new State(STANDARD_PITS.clone(), testPits, PlayerColor.SOUTH);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorWithNegativeSeedsInTheNorthPits() {
		//there is never a negative amount of seeds in one of the pits
		int[] testPits = {-1,4,4,4,4,4,0};
		new State(testPits, STANDARD_PITS.clone(), PlayerColor.SOUTH);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorWithNegativeSeedsInTheSouthPits() {
		//there is never a negative amount of seeds in one of the pits
		int[] testPits = {-1,4,4,4,4,4,0};
		new State(STANDARD_PITS.clone(), testPits, PlayerColor.SOUTH);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorWithDifferentAmountOfPitsForSouthAndNorth() {
		//there should always be the same number of pits in both south and north
		int[] testPits = {4,4,4,4,4,0};
		int[] testPits2 = {4,4,4,4,4,4,4,0};
		new State(testPits, testPits2, PlayerColor.SOUTH);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorWithWrongNumberOfSeedsTotal() {
		//there should always be a total of 48 seeds in the game
		int[] testPits = {4,4,4,4,4,4,0};
		int[] testPits2 = {5,4,4,4,4,4,0};
		new State(testPits, testPits2, PlayerColor.SOUTH);
	}
	
	@Test(expected = GameOverException.class)
	public void testMoveIllegalMoveAfterGameOver() {
		int[] northPits = {0, 0, 0, 0, 0, 0, 28};
		int[] southPits = {0, 0, 0, 0, 0, 0, 20};
		boolean gameOver = true;
		State testState = new State(northPits, southPits, PlayerColor.SOUTH, gameOver);
		testState.makeMove(5);
	}
	
	@Test(expected = IllegalMoveException.class)
	public void testMoveIllegalMove() {
		//You can't take the seeds out of an empty pit
		int[] northPits = {0, 5, 5, 5, 5, 4, 0};
		int[] southPits = {4, 0, 5, 5, 5, 5, 0};
		State testState = new State(northPits, southPits, PlayerColor.SOUTH);
		testState.makeMove(1);
	}
	
	@Test
	public void testMoveOnlyOnActiveSide() {
		State testState = new State(STANDARD_PITS.clone(), STANDARD_PITS.clone(), PlayerColor.SOUTH);
		testState.makeMove(0);
		
		int[] expectedSouthPits = {0, 5, 5, 5, 5, 4, 0};
		State expectedState = new State(STANDARD_PITS.clone(), expectedSouthPits, PlayerColor.NORTH);
		
		assertEquals("From init south takes index 0 seeds: ", expectedState, testState);
	}
	
	@Test
	public void testMoveAcrossOneSide() {
		State testState = new State(STANDARD_PITS.clone(), STANDARD_PITS.clone(), PlayerColor.SOUTH);
		testState.makeMove(5);
		
		int[] expectedNorthPits = {5, 5, 5, 4, 4, 4, 0};
		int[] expectedSouthPits = {4, 4, 4, 4, 4, 0, 1};
		State expectedState = new State(expectedNorthPits, expectedSouthPits, PlayerColor.NORTH);
		
		assertEquals("From init south takes index 5 seeds: ", expectedState, testState);
	}
	
	@Test
	public void testMoveAcrossTwoSidesSouth() {
		int[] northPits = {1, 1, 1, 1, 1, 1, 10};
		int[] southPits = {1, 1, 1, 1, 1, 17, 10};
		State testState = new State(northPits, southPits, PlayerColor.SOUTH);
		testState.makeMove(5);
		
		int[] expectedNorthPits = {3, 3, 3, 2, 2, 2, 10};
		int[] expectedSouthPits = {2, 2, 2, 2, 2, 1, 12};
		State expectedState = new State(expectedNorthPits, expectedSouthPits, PlayerColor.NORTH);
		
		assertEquals("From custom south takes index 5 seeds: ", expectedState, testState);
	}
	
	@Test
	public void testMoveAcrossTwoSidesNorth() {
		int[] northPits = {1, 1, 1, 1, 1, 17, 10};
		int[] southPits = {1, 1, 1, 1, 1, 1, 10};
		State testState = new State(northPits, southPits, PlayerColor.NORTH);
		testState.makeMove(5);
		
		int[] expectedNorthPits = {2, 2, 2, 2, 2, 1, 12};
		int[] expectedSouthPits = {3, 3, 3, 2, 2, 2, 10};
		State expectedState = new State(expectedNorthPits, expectedSouthPits, PlayerColor.SOUTH);
		
		assertEquals("From custom south takes index 5 seeds: ", expectedState, testState);
	}
	
	@Test
	public void testMoveLastOnTreasureChestSouth() {
		//If the last seed of a move landed on your treasure chest, then you can move again
		State testState = new State(STANDARD_PITS.clone(), STANDARD_PITS.clone(), PlayerColor.SOUTH);
		testState.makeMove(2);
		
		int[] expectedNorthPits = {4, 4, 4, 4, 4, 4, 0};
		int[] expectedSouthPits = {4, 4, 0, 5, 5, 5, 1};
		State expectedState = new State(expectedNorthPits, expectedSouthPits, PlayerColor.SOUTH);
		
		assertEquals("From custom south takes index 2 seeds: ", expectedState, testState);
	}
	
	@Test
	public void testMoveLastOnTreasureChestNorth() {
		//If the last seed of a move landed on your treasure chest, then you can move again
		State testState = new State(STANDARD_PITS.clone(), STANDARD_PITS.clone(), PlayerColor.NORTH);
		testState.makeMove(2);
		
		int[] expectedNorthPits = {4, 4, 0, 5, 5, 5, 1};
		int[] expectedSouthPits = {4, 4, 4, 4, 4, 4, 0};
		State expectedState = new State(expectedNorthPits, expectedSouthPits, PlayerColor.NORTH);
		
		assertEquals("From custom south takes index 2 seeds: ", expectedState, testState);
	}
	
	@Test
	public void testMoveCaptureOpposingSeedsSouth() {
		//If the last seed of a move landed on an empty pit on your side and there are some seeds in the opposite pit, 
		//then the seeds in the two cups will be captured in your treasure chest
		int[] northPits = {2, 2, 4, 2, 2, 2, 12};
		int[] southPits = {2, 2, 2, 0, 2, 2, 12};
		State testState = new State(northPits, southPits, PlayerColor.SOUTH);
		testState.makeMove(1);
		
		int[] expectedNorthPits = {2, 2, 0, 2, 2, 2, 12};
		int[] expectedSouthPits = {2, 0, 3, 0, 2, 2, 17};
		State expectedState = new State(expectedNorthPits, expectedSouthPits, PlayerColor.NORTH);
		
		assertEquals("From custom south takes index 1 seeds: ", expectedState, testState);
	}
	
	@Test
	public void testMoveCaptureOpposingSeedsNorth() {
		//If the last seed of a move landed on an empty pit on your side and there are some seeds in the opposite pit, 
		//then the seeds in the two cups will be captured in your treasure chest
		int[] northPits = {2, 2, 2, 0, 2, 2, 12};
		int[] southPits = {2, 2, 4, 2, 2, 2, 12};
		State testState = new State(northPits, southPits, PlayerColor.NORTH);
		testState.makeMove(1);
		
		int[] expectedNorthPits = {2, 0, 3, 0, 2, 2, 17};
		int[] expectedSouthPits = {2, 2, 0, 2, 2, 2, 12};
		State expectedState = new State(expectedNorthPits, expectedSouthPits, PlayerColor.SOUTH);
		
		assertEquals("From custom south takes index 1 seeds: ", expectedState, testState);
	}
	
	@Test
	public void testMoveFinishSouth() {
		//The game ends when all the pits on one players side are empty. 
		//All remaining seeds that are still in the pits of the opposing player go into his treasure chest.
		int[] northPits = {0, 0, 0, 6, 0, 0, 10};
		int[] southPits = {0, 0, 0, 0, 0, 2, 30};
		State testState = new State(northPits, southPits, PlayerColor.SOUTH);
		testState.makeMove(5);
		
		int[] expectedNorthPits = {0, 0, 0, 0, 0, 0, 17};
		int[] expectedSouthPits = {0, 0, 0, 0, 0, 0, 31};
		boolean expectedGameOver = true;
		State expectedState = new State(expectedNorthPits, expectedSouthPits, PlayerColor.NORTH, expectedGameOver);
		
		assertEquals("South provokes GameOver: ", expectedState, testState);
	}
	
	@Test
	public void testMoveFinishNorth() {
		//The game ends when all the pits on one players side are empty. 
		//All remaining seeds that are still in the pits of the opposing player go into his treasure chest.
		int[] northPits = {0, 0, 0, 0, 0, 2, 30};
		int[] southPits = {0, 0, 0, 6, 0, 0, 10};
		State testState = new State(northPits, southPits, PlayerColor.NORTH);
		testState.makeMove(5);
		
		int[] expectedNorthPits = {0, 0, 0, 0, 0, 0, 31};
		int[] expectedSouthPits = {0, 0, 0, 0, 0, 0, 17};
		boolean expectedGameOver = true;
		State expectedState = new State(expectedNorthPits, expectedSouthPits, PlayerColor.SOUTH, expectedGameOver);
		
		assertEquals("South provokes GameOver: ", expectedState, testState);
	}
	
	@Test
	public void testMoveTie() {
		//The game ends when all the pits on one players side are empty. 
		//All remaining seeds that are still in the pits of the opposing player go into his treasure chest.
		int[] northPits = {0, 0, 0, 0, 0, 0, 23};
		int[] southPits = {0, 0, 0, 0, 0, 2, 23};
		State testState = new State(northPits, southPits, PlayerColor.SOUTH);
		testState.makeMove(5);
		
		int[] expectedNorthPits = {0, 0, 0, 0, 0, 0, 24};
		int[] expectedSouthPits = {0, 0, 0, 0, 0, 0, 24};
		boolean expectedGameOver = true;
		State expectedState = new State(expectedNorthPits, expectedSouthPits, PlayerColor.NORTH, expectedGameOver);
		
		assertEquals("South provokes GameOver: ", expectedState, testState);
	}

}
