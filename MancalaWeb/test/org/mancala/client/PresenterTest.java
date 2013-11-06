package org.mancala.client;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mancala.client.View;
import org.mancala.shared.PlayerColor;
import org.mancala.shared.State;
import org.mockito.Mockito;

public class PresenterTest {
	Presenter presenter;
	View graphics;

	public final int[] STANDARD_PITS = { 4, 4, 4, 4, 4, 4, 0 };

	@Before
	public void setup() {
		graphics = Mockito.mock(View.class);
		presenter = new Presenter(graphics);
		// HasClickHandlers dummyResetButton = Mockito.mock(HasClickHandlers.class);
	}

	/**
	 * Because of the setUp() method there should be calls to setSeeds to set the standard pits
	 */
	@Test
	public void testInitialSetPits() {
		Mockito.verify(graphics).setSeeds(PlayerColor.S, 0, 4);
		Mockito.verify(graphics).setSeeds(PlayerColor.S, 1, 4);
		Mockito.verify(graphics).setSeeds(PlayerColor.S, 2, 4);
		Mockito.verify(graphics).setSeeds(PlayerColor.S, 3, 4);
		Mockito.verify(graphics).setSeeds(PlayerColor.S, 4, 4);
		Mockito.verify(graphics).setSeeds(PlayerColor.S, 5, 4);
		Mockito.verify(graphics).setSeeds(PlayerColor.S, 6, 0);

		Mockito.verify(graphics).setSeeds(PlayerColor.N, 0, 4);
		Mockito.verify(graphics).setSeeds(PlayerColor.N, 1, 4);
		Mockito.verify(graphics).setSeeds(PlayerColor.N, 2, 4);
		Mockito.verify(graphics).setSeeds(PlayerColor.N, 3, 4);
		Mockito.verify(graphics).setSeeds(PlayerColor.N, 4, 4);
		Mockito.verify(graphics).setSeeds(PlayerColor.N, 5, 4);
		Mockito.verify(graphics).setSeeds(PlayerColor.N, 6, 0);
	}

	/**
	 * Test another setSeeds arrangement
	 */
	@Test
	public void testSetPits() {
		int[] northPits = new int[] { 3, 3, 3, 3, 3, 3, 6 };
		int[] southPits = new int[] { 3, 3, 3, 3, 3, 3, 6 };
		PlayerColor whoseTurn = PlayerColor.S;
		presenter.state = new State(northPits, southPits, whoseTurn);

		presenter.setState(presenter.state);

		Mockito.verify(graphics).setSeeds(PlayerColor.S, 0, 3);
		Mockito.verify(graphics).setSeeds(PlayerColor.S, 1, 3);
		Mockito.verify(graphics).setSeeds(PlayerColor.S, 2, 3);
		Mockito.verify(graphics).setSeeds(PlayerColor.S, 3, 3);
		Mockito.verify(graphics).setSeeds(PlayerColor.S, 4, 3);
		Mockito.verify(graphics).setSeeds(PlayerColor.S, 5, 3);
		Mockito.verify(graphics).setSeeds(PlayerColor.S, 6, 6);

		Mockito.verify(graphics).setSeeds(PlayerColor.N, 0, 3);
		Mockito.verify(graphics).setSeeds(PlayerColor.N, 1, 3);
		Mockito.verify(graphics).setSeeds(PlayerColor.N, 2, 3);
		Mockito.verify(graphics).setSeeds(PlayerColor.N, 3, 3);
		Mockito.verify(graphics).setSeeds(PlayerColor.N, 4, 3);
		Mockito.verify(graphics).setSeeds(PlayerColor.N, 5, 3);
		Mockito.verify(graphics).setSeeds(PlayerColor.N, 6, 6);
	}

	/**
	 * Because of the setUp() method there should be calls to enable the south and disable the north pits
	 */
	@Test
	public void testEnableInitialSouthSide() {
		for (int i = 0; i < 6; i++) {
			Mockito.verify(graphics).setPitEnabled(PlayerColor.N, i, false);
			Mockito.verify(graphics).setPitEnabled(PlayerColor.S, i, true);
		}
	}

	/**
	 * If whose turn is set to North their pits should be enabled and the south pits should be disabled
	 */
	@Test
	public void testEnableNorthSide() {
		int[] northPits = STANDARD_PITS.clone();
		int[] southPits = STANDARD_PITS.clone();
		PlayerColor whoseTurn = PlayerColor.N;
		presenter.state = new State(northPits, southPits, whoseTurn);

		presenter.enableActiveSide();

		for (int i = 0; i < 6; i++) {
			Mockito.verify(graphics).setPitEnabled(PlayerColor.N, i, true);
			Mockito.verify(graphics).setPitEnabled(PlayerColor.S, i, false);
		}
	}

	/**
	 * When there are zero seeds in the pits, they should be disabled since the user can't choose them
	 */
	@Test
	public void testDisableZeroPitsNorth() {
		int[] northPits = new int[] { 3, 0, 3, 3, 0, 3, 12 };
		int[] southPits = new int[] { 3, 0, 3, 3, 0, 3, 12 };
		PlayerColor whoseTurn = PlayerColor.N;
		presenter.state = new State(northPits, southPits, whoseTurn);

		presenter.disableZeroSeedPits();

		// Because of initial call in presenter constructor this should be the second time
		// that these north pits are disabled
		Mockito.verify(graphics, Mockito.times(1)).setPitEnabled(PlayerColor.N, 0, false);
		Mockito.verify(graphics, Mockito.times(2)).setPitEnabled(PlayerColor.N, 1, false);
		Mockito.verify(graphics, Mockito.times(1)).setPitEnabled(PlayerColor.N, 2, false);
		Mockito.verify(graphics, Mockito.times(1)).setPitEnabled(PlayerColor.N, 3, false);
		Mockito.verify(graphics, Mockito.times(2)).setPitEnabled(PlayerColor.N, 4, false);
		Mockito.verify(graphics, Mockito.times(1)).setPitEnabled(PlayerColor.N, 5, false);
		Mockito.verify(graphics, Mockito.times(0)).setPitEnabled(PlayerColor.N, 6, false);

		// because of active player = north all south pits should never be set to false
		Mockito.verify(graphics, Mockito.times(0)).setPitEnabled(PlayerColor.S, 0, false);
		Mockito.verify(graphics, Mockito.times(0)).setPitEnabled(PlayerColor.S, 1, false);
		Mockito.verify(graphics, Mockito.times(0)).setPitEnabled(PlayerColor.S, 2, false);
		Mockito.verify(graphics, Mockito.times(0)).setPitEnabled(PlayerColor.S, 3, false);
		Mockito.verify(graphics, Mockito.times(0)).setPitEnabled(PlayerColor.S, 4, false);
		Mockito.verify(graphics, Mockito.times(0)).setPitEnabled(PlayerColor.S, 5, false);
		Mockito.verify(graphics, Mockito.times(0)).setPitEnabled(PlayerColor.S, 6, false);
	}

	/**
	 * When there are zero seeds in the pits, they should be disabled since the user can't choose them
	 */
	@Test
	public void testDisableZeroPitsSouth() {
		int[] northPits = new int[] { 3, 0, 3, 3, 0, 3, 12 };
		int[] southPits = new int[] { 3, 0, 3, 3, 0, 3, 12 };
		PlayerColor whoseTurn = PlayerColor.S;
		presenter.state = new State(northPits, southPits, whoseTurn);

		presenter.disableZeroSeedPits();

		// Because of initial call in presenter constructor north pits were disabled once
		Mockito.verify(graphics, Mockito.times(1)).setPitEnabled(PlayerColor.N, 0, false);
		Mockito.verify(graphics, Mockito.times(1)).setPitEnabled(PlayerColor.N, 1, false);
		Mockito.verify(graphics, Mockito.times(1)).setPitEnabled(PlayerColor.N, 2, false);
		Mockito.verify(graphics, Mockito.times(1)).setPitEnabled(PlayerColor.N, 3, false);
		Mockito.verify(graphics, Mockito.times(1)).setPitEnabled(PlayerColor.N, 4, false);
		Mockito.verify(graphics, Mockito.times(1)).setPitEnabled(PlayerColor.N, 5, false);
		Mockito.verify(graphics, Mockito.times(0)).setPitEnabled(PlayerColor.N, 6, false);

		// only the pits with 0 seeds should be disabled
		Mockito.verify(graphics, Mockito.times(0)).setPitEnabled(PlayerColor.S, 0, false);
		Mockito.verify(graphics, Mockito.times(1)).setPitEnabled(PlayerColor.S, 1, false);
		Mockito.verify(graphics, Mockito.times(0)).setPitEnabled(PlayerColor.S, 2, false);
		Mockito.verify(graphics, Mockito.times(0)).setPitEnabled(PlayerColor.S, 3, false);
		Mockito.verify(graphics, Mockito.times(1)).setPitEnabled(PlayerColor.S, 4, false);
		Mockito.verify(graphics, Mockito.times(0)).setPitEnabled(PlayerColor.S, 5, false);
		Mockito.verify(graphics, Mockito.times(0)).setPitEnabled(PlayerColor.S, 6, false);
	}

	/**
	 * When the game is NOT over there should be no message displayed
	 */
	@Test
	public void testMessageWithNoGameOver() {
		int[] northPits = STANDARD_PITS.clone();
		int[] southPits = STANDARD_PITS.clone();
		PlayerColor whoseTurn = PlayerColor.S;
		boolean gameOver = false;
		presenter.state = new State(northPits, southPits, whoseTurn, gameOver);

		presenter.message();

		Mockito.verify(graphics, Mockito.times(0)).setMessage(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
	}

	/**
	 * When the game IS over and North won there should be a message saying so.
	 */
	@Test
	public void testMessageWithNorthWinner() {
		int[] northPits = new int[] { 0, 0, 0, 0, 0, 0, 28 };
		int[] southPits = new int[] { 0, 0, 0, 0, 0, 0, 20 };
		PlayerColor whoseTurn = PlayerColor.S;
		boolean gameOver = true;
		presenter.state = new State(northPits, southPits, whoseTurn, gameOver);

		presenter.message();

		Mockito.verify(graphics, Mockito.times(1)).setMessage(Mockito.contains("North"), Mockito.anyString(), Mockito.anyString());
	}

	/**
	 * When the game IS over and South won there should be a message saying so.
	 */
	@Test
	public void testMessageWithSouthWinner() {
		int[] northPits = new int[] { 0, 0, 0, 0, 0, 0, 20 };
		int[] southPits = new int[] { 0, 0, 0, 0, 0, 0, 28 };
		PlayerColor whoseTurn = PlayerColor.S;
		boolean gameOver = true;
		presenter.state = new State(northPits, southPits, whoseTurn, gameOver);

		presenter.message();

		Mockito.verify(graphics, Mockito.times(1)).setMessage(Mockito.contains("South"), Mockito.anyString(), Mockito.anyString());
	}

	/**
	 * When the game IS over and it's a tie there should be a message saying so.
	 */
	@Test
	public void testMessageWithTie() {
		int[] northPits = new int[] { 0, 0, 0, 0, 0, 0, 24 };
		int[] southPits = new int[] { 0, 0, 0, 0, 0, 0, 24 };
		PlayerColor whoseTurn = PlayerColor.S;
		boolean gameOver = true;
		presenter.state = new State(northPits, southPits, whoseTurn, gameOver);

		presenter.message();

		Mockito.verify(graphics, Mockito.times(1)).setMessage(Mockito.contains("tie"), Mockito.anyString(), Mockito.anyString());
	}

	/**
	 * If the new method is called there should be a new item set in the history (that triggers the event handler were eventually
	 * the new state will be set)
	 */
	@Test
	public void testNewGame() {
		int[] northPits = new int[] { 3, 0, 3, 3, 0, 3, 12 };
		int[] southPits = new int[] { 3, 0, 3, 3, 0, 3, 12 };
		PlayerColor whoseTurn = PlayerColor.N;
		boolean gameOver = true;
		presenter.state = new State(northPits, southPits, whoseTurn, gameOver);

		presenter.newGame();

		// It should be called two times - 1 for the initial call and 1 for the newGame call
		Mockito.verify(graphics, Mockito.times(2)).setSeeds(PlayerColor.S, 0, 4);
		Mockito.verify(graphics, Mockito.times(2)).setSeeds(PlayerColor.S, 1, 4);
		Mockito.verify(graphics, Mockito.times(2)).setSeeds(PlayerColor.S, 2, 4);
		Mockito.verify(graphics, Mockito.times(2)).setSeeds(PlayerColor.S, 3, 4);
		Mockito.verify(graphics, Mockito.times(2)).setSeeds(PlayerColor.S, 4, 4);
		Mockito.verify(graphics, Mockito.times(2)).setSeeds(PlayerColor.S, 5, 4);
		Mockito.verify(graphics, Mockito.times(2)).setSeeds(PlayerColor.S, 6, 0);

		Mockito.verify(graphics, Mockito.times(2)).setSeeds(PlayerColor.N, 0, 4);
		Mockito.verify(graphics, Mockito.times(2)).setSeeds(PlayerColor.N, 1, 4);
		Mockito.verify(graphics, Mockito.times(2)).setSeeds(PlayerColor.N, 2, 4);
		Mockito.verify(graphics, Mockito.times(2)).setSeeds(PlayerColor.N, 3, 4);
		Mockito.verify(graphics, Mockito.times(2)).setSeeds(PlayerColor.N, 4, 4);
		Mockito.verify(graphics, Mockito.times(2)).setSeeds(PlayerColor.N, 5, 4);
		Mockito.verify(graphics, Mockito.times(2)).setSeeds(PlayerColor.N, 6, 0);
	}

	/**
	 * Test if the serialization works correctly
	 */
	@Test
	public void testSerialize() {
		int[] northPits = new int[] { 3, 0, 3, 3, 0, 3, 12 };
		int[] southPits = new int[] { 3, 0, 3, 3, 0, 3, 12 };
		PlayerColor whoseTurn = PlayerColor.N;
		boolean gameOver = true;
		boolean lastMoveWasOppositeCapture = true;
		int oppositeSeeds = 5;
		presenter.state = new State(northPits, southPits, whoseTurn, gameOver, lastMoveWasOppositeCapture, oppositeSeeds);

		String serializedState = State.serialize(presenter.state);

		assertEquals("3,0,3,3,0,3,12_3,0,3,3,0,3,12_N_T_T_5", serializedState);
	}

	/**
	 * Test if the deserialization works correctly
	 */
	@Test
	public void testDeserialize() {
		State testState = State.deserialize("3,0,3,3,0,3,12_3,0,3,3,0,3,12_N_T_T_5");

		int[] northPits = new int[] { 3, 0, 3, 3, 0, 3, 12 };
		int[] southPits = new int[] { 3, 0, 3, 3, 0, 3, 12 };
		PlayerColor whoseTurn = PlayerColor.N;
		boolean gameOver = true;
		boolean lastMoveWasOppositeCapture = true;
		int oppositeSeeds = 5;
		State expectedState = new State(northPits, southPits, whoseTurn, gameOver, lastMoveWasOppositeCapture, oppositeSeeds);

		assertEquals(expectedState, testState);
	}
}
