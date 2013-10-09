package org.mancala.client;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.mancala.client.Presenter;
import org.mancala.client.Presenter.View;
import org.mancala.shared.PlayerColor;
import org.mancala.shared.State;
import org.mockito.Mockito;

public class PresenterTest {
	Presenter presenter;
	View graphics;

	public final int[] STANDARD_PITS = {4,4,4,4,4,4,0};
	
	@Before
    public void setup(){
		graphics = Mockito.mock(Presenter.View.class);
        Mockito.when(graphics.getPreviousGame()).thenReturn(new State());
        presenter = new Presenter(graphics);
//      HasClickHandlers dummyResetButton = Mockito.mock(HasClickHandlers.class);
    }
	
	/**
	 * Because of the setUp() method there should be calls to setSeeds to set the standard pits
	 */
	@Test
	public void testInitialSetPits(){
		Mockito.verify(graphics).setSeeds(0, PlayerColor.S, 4);
		Mockito.verify(graphics).setSeeds(1, PlayerColor.S, 4);
		Mockito.verify(graphics).setSeeds(2, PlayerColor.S, 4);
		Mockito.verify(graphics).setSeeds(3, PlayerColor.S, 4);
		Mockito.verify(graphics).setSeeds(4, PlayerColor.S, 4);
		Mockito.verify(graphics).setSeeds(5, PlayerColor.S, 4);
		Mockito.verify(graphics).setSeeds(6, PlayerColor.S, 0);
		
		Mockito.verify(graphics).setSeeds(0, PlayerColor.N, 4);
		Mockito.verify(graphics).setSeeds(1, PlayerColor.N, 4);
		Mockito.verify(graphics).setSeeds(2, PlayerColor.N, 4);
		Mockito.verify(graphics).setSeeds(3, PlayerColor.N, 4);
		Mockito.verify(graphics).setSeeds(4, PlayerColor.N, 4);
		Mockito.verify(graphics).setSeeds(5, PlayerColor.N, 4);
		Mockito.verify(graphics).setSeeds(6, PlayerColor.N, 0);
	}
	
	/**
	 * Test another setSeeds arrangement
	 */
	@Test
	public void testSetPits(){
		int[] northPits = new int[]{3,3,3,3,3,3,6};
		int[] southPits = new int[]{3,3,3,3,3,3,6};
		PlayerColor whoseTurn = PlayerColor.S;
		presenter.state = new State(northPits, southPits, whoseTurn);
		
		presenter.setState(presenter.state);
		
		Mockito.verify(graphics).setSeeds(0, PlayerColor.S, 3);
		Mockito.verify(graphics).setSeeds(1, PlayerColor.S, 3);
		Mockito.verify(graphics).setSeeds(2, PlayerColor.S, 3);
		Mockito.verify(graphics).setSeeds(3, PlayerColor.S, 3);
		Mockito.verify(graphics).setSeeds(4, PlayerColor.S, 3);
		Mockito.verify(graphics).setSeeds(5, PlayerColor.S, 3);
		Mockito.verify(graphics).setSeeds(6, PlayerColor.S, 6);
		
		Mockito.verify(graphics).setSeeds(0, PlayerColor.N, 3);
		Mockito.verify(graphics).setSeeds(1, PlayerColor.N, 3);
		Mockito.verify(graphics).setSeeds(2, PlayerColor.N, 3);
		Mockito.verify(graphics).setSeeds(3, PlayerColor.N, 3);
		Mockito.verify(graphics).setSeeds(4, PlayerColor.N, 3);
		Mockito.verify(graphics).setSeeds(5, PlayerColor.N, 3);
		Mockito.verify(graphics).setSeeds(6, PlayerColor.N, 6);
	}

	/**
	 * Because of the setUp() method there should be calls to set the initial History
	 */
	@Test
	public void testInitialAddHistoryTriggerBoardUpdate(){
		Mockito.verify(graphics).setHistoryNewItem(presenter.serializeState(presenter.state));
	}
	
	/**
	 * Because of the setUp() method there should be calls to enable the south and disable the north pits
	 */
	@Test
	public void testEnableInitialSouthSide(){
		for(int i = 0; i < 6; i++){
			Mockito.verify(graphics).setPitEnabled(i, PlayerColor.N, false);
			Mockito.verify(graphics).setPitEnabled(i, PlayerColor.S, true);
		}
	}
	
	/**
	 * Every time the presenter wants to add to the history the setHistoryNewItem method in graphics should be called
	 */
	@Test
	public void testAnotherAddHistoryTriggerBoardUpdate(){
		int[] northPits = new int[]{3,3,3,3,3,3,6};
		int[] southPits = new int[]{3,3,3,3,3,3,6};
		PlayerColor whoseTurn = PlayerColor.S;
		presenter.state = new State(northPits, southPits, whoseTurn);
		
		presenter.addHistoryTriggerBoardUpdate(presenter.state);
		
		Mockito.verify(graphics).setHistoryNewItem(presenter.serializeState(presenter.state));
	}
	
	/**
	 * If whose turn is set to North their pits should be enabled and the south pits should be disabled
	 */
	@Test
	public void testEnableNorthSide(){
		int[] northPits = STANDARD_PITS.clone();
		int[] southPits = STANDARD_PITS.clone();
		PlayerColor whoseTurn = PlayerColor.N;
		presenter.state = new State(northPits, southPits, whoseTurn);
		
		presenter.enableActiveSide();

		for(int i = 0; i < 6; i++){
			Mockito.verify(graphics).setPitEnabled(i, PlayerColor.N, true);
			Mockito.verify(graphics).setPitEnabled(i, PlayerColor.S, false);
		}
	}
	
	/**
	 * When there are zero seeds in the pits, they should be disabled since the user can't choose them
	 */
	@Test
	public void testDisableZeroPitsNorth(){
		int[] northPits = new int[]{3,0,3,3,0,3,12};
		int[] southPits = new int[]{3,0,3,3,0,3,12};
		PlayerColor whoseTurn = PlayerColor.N;
		presenter.state = new State(northPits, southPits, whoseTurn);
		
		presenter.disableZeroSeedPits();

		//Because of initial call in presenter constructor this should be the second time
		//that these north pits are disabled
		Mockito.verify(graphics, Mockito.times(1)).setPitEnabled(0, PlayerColor.N, false);
		Mockito.verify(graphics, Mockito.times(2)).setPitEnabled(1, PlayerColor.N, false);
		Mockito.verify(graphics, Mockito.times(1)).setPitEnabled(2, PlayerColor.N, false);
		Mockito.verify(graphics, Mockito.times(1)).setPitEnabled(3, PlayerColor.N, false);
		Mockito.verify(graphics, Mockito.times(2)).setPitEnabled(4, PlayerColor.N, false);
		Mockito.verify(graphics, Mockito.times(1)).setPitEnabled(5, PlayerColor.N, false);
		Mockito.verify(graphics, Mockito.times(0)).setPitEnabled(6, PlayerColor.N, false);
		
		//because of active player = north all south pits should be set to false once
		Mockito.verify(graphics, Mockito.times(0)).setPitEnabled(0, PlayerColor.S, false);
		Mockito.verify(graphics, Mockito.times(0)).setPitEnabled(1, PlayerColor.S, false);
		Mockito.verify(graphics, Mockito.times(0)).setPitEnabled(2, PlayerColor.S, false);
		Mockito.verify(graphics, Mockito.times(0)).setPitEnabled(3, PlayerColor.S, false);
		Mockito.verify(graphics, Mockito.times(0)).setPitEnabled(4, PlayerColor.S, false);
		Mockito.verify(graphics, Mockito.times(0)).setPitEnabled(5, PlayerColor.S, false);
		Mockito.verify(graphics, Mockito.times(0)).setPitEnabled(6, PlayerColor.S, false);
	}
	
	/**
	 * When there are zero seeds in the pits, they should be disabled since the user can't choose them
	 */
	@Test
	public void testDisableZeroPitsSouth(){
		int[] northPits = new int[]{3,0,3,3,0,3,12};
		int[] southPits = new int[]{3,0,3,3,0,3,12};
		PlayerColor whoseTurn = PlayerColor.S;
		presenter.state = new State(northPits, southPits, whoseTurn);
		
		presenter.disableZeroSeedPits();

		//Because of initial call in presenter constructor north pits were disabled once
		Mockito.verify(graphics, Mockito.times(1)).setPitEnabled(0, PlayerColor.N, false);
		Mockito.verify(graphics, Mockito.times(1)).setPitEnabled(1, PlayerColor.N, false);
		Mockito.verify(graphics, Mockito.times(1)).setPitEnabled(2, PlayerColor.N, false);
		Mockito.verify(graphics, Mockito.times(1)).setPitEnabled(3, PlayerColor.N, false);
		Mockito.verify(graphics, Mockito.times(1)).setPitEnabled(4, PlayerColor.N, false);
		Mockito.verify(graphics, Mockito.times(1)).setPitEnabled(5, PlayerColor.N, false);
		Mockito.verify(graphics, Mockito.times(0)).setPitEnabled(6, PlayerColor.N, false);
		
		//only the pits with 0 seeds should be disabled
		Mockito.verify(graphics, Mockito.times(0)).setPitEnabled(0, PlayerColor.S, false);
		Mockito.verify(graphics, Mockito.times(1)).setPitEnabled(1, PlayerColor.S, false);
		Mockito.verify(graphics, Mockito.times(0)).setPitEnabled(2, PlayerColor.S, false);
		Mockito.verify(graphics, Mockito.times(0)).setPitEnabled(3, PlayerColor.S, false);
		Mockito.verify(graphics, Mockito.times(1)).setPitEnabled(4, PlayerColor.S, false);
		Mockito.verify(graphics, Mockito.times(0)).setPitEnabled(5, PlayerColor.S, false);
		Mockito.verify(graphics, Mockito.times(0)).setPitEnabled(6, PlayerColor.S, false);
	}
	
	/**
	 * When the game is NOT over there should be no message displayed
	 */
	@Test
	public void testMessageWithNoGameOver(){
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
	public void testMessageWithNorthWinner(){
		int[] northPits = new int[]{0,0,0,0,0,0,28};
		int[] southPits = new int[]{0,0,0,0,0,0,20};
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
	public void testMessageWithSouthWinner(){
		int[] northPits = new int[]{0,0,0,0,0,0,20};
		int[] southPits = new int[]{0,0,0,0,0,0,28};
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
	public void testMessageWithTie(){
		int[] northPits = new int[]{0,0,0,0,0,0,24};
		int[] southPits = new int[]{0,0,0,0,0,0,24};
		PlayerColor whoseTurn = PlayerColor.S;
		boolean gameOver = true;
		presenter.state = new State(northPits, southPits, whoseTurn, gameOver);
		
		presenter.message();

		Mockito.verify(graphics, Mockito.times(1)).setMessage(Mockito.contains("tie"), Mockito.anyString(), Mockito.anyString());
	}
	
	/**
	 * If the new method is called there should be a new item set in the history 
	 * (that triggers the event handler were eventually the new state will be set)
	 */
	@Test
	public void testNewGame(){
		int[] northPits = new int[]{3,0,3,3,0,3,12};
		int[] southPits = new int[]{3,0,3,3,0,3,12};
		PlayerColor whoseTurn = PlayerColor.N;
		boolean gameOver = true;
		presenter.state = new State(northPits, southPits, whoseTurn, gameOver);
		
		presenter.newGame();
		
		//It should be called two times - 1 for the initial call and 1 for the newGame call
		Mockito.verify(graphics, Mockito.times(2)).setHistoryNewItem(presenter.serializeState(new State()));
	}
	
	/**
	 * Test if the serialization works correctly 
	 */
	@Test
	public void testSerialize(){
		int[] northPits = new int[]{3,0,3,3,0,3,12};
		int[] southPits = new int[]{3,0,3,3,0,3,12};
		PlayerColor whoseTurn = PlayerColor.N;
		boolean gameOver = true;
		presenter.state = new State(northPits, southPits, whoseTurn, gameOver);
		
		String serializedState = presenter.serializeState(presenter.state);
		
		assertEquals("3,0,3,3,0,3,12_3,0,3,3,0,3,12_N_T", serializedState);
	}
	
	/**
	 * Test if the deserialization works correctly 
	 */
	@Test
	public void testDeserialize(){
		State testState = presenter.deserializeState("3,0,3,3,0,3,12_3,0,3,3,0,3,12_N_T");
		
		int[] northPits = new int[]{3,0,3,3,0,3,12};
		int[] southPits = new int[]{3,0,3,3,0,3,12};
		PlayerColor whoseTurn = PlayerColor.N;
		boolean gameOver = true;
		State expectedState = new State(northPits, southPits, whoseTurn, gameOver);
		
		assertEquals(expectedState, testState);
	}
}
