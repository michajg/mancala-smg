package org.mancala.client;

import org.mancala.shared.PlayerColor;
import org.mancala.shared.State;
import org.mancala.shared.IllegalMoveException;
import org.mancala.shared.GameOverException;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;

/**
 * The MVP-Presenter of the Mancala game
 * @author Micha
 */
public class Presenter {
	
	/**
	 * The view of the MVP pattern the presenter will use
	 */
	final View graphics;
	
	/**
	 * The model of the MVP pattern the presenter will use
	 */
	State state;
	
	/**
	 * The view has to support these methods to communicate with the presenter 
	 * @author Micha Guthmann
	 */
	public interface View {
	    
	    /**
	     * The seedAmount will be placed on the right side at the correct index 
	     */
		void setSeeds(int index, PlayerColor side, int seedAmount);
		
		/**
		 * A player can only select certain pits for their move. 
		 * That's why some have to be enabled and some have to be disabled before a player's turn. 
		 */
		void setPitEnabled(int index, PlayerColor side, boolean enabled);/**
		
		 * Informs the user of certain events. 
		 * If the parameter for the buttons is null no button will be displayed.
		 * The first button makes the information disappear, the second starts a new game
		 */
	    void setMessage(String labelMsg, String HideBtnText, String restartBtnText);
	    
	}
	
	/**
	 * 1. Sets the view
	 * 2. Checks if there is already a state in the url fragment and if so initializes this
	 * 3. Initializes the History
	 * 4. Updates the gaming board accordingly
	 */
	public Presenter(View graphics) {
		this.graphics = graphics;
		String urlFragment = Window.Location.getHash();
		if(urlFragment != "" && urlFragment != null && urlFragment.length() > 1){
			try {
				state = deserializeState(urlFragment.substring(1, urlFragment.length()));
			}
			catch (Exception e) {
				graphics.setMessage("New game because of error: " + e, "Okay", null);
				state = new State();
			}
		}
		else
			state = new State();
		//the first newItem in History doesn't trigger the value change handler
		History.newItem(serializeState(state));
		addHistoryValueChangeHandler();
		updateBoard();
	}
	
	/**
	 * makes a move on the model, adds the new state to the history and updates the board
	 */
	void makeMove(int index) {
		
		try {
			state.makeMove(index);
			addToHistoryAndTriggerBoardUpdate(state);
		} 
		catch (IllegalMoveException e) {
			graphics.setMessage("New game because of error: " + e, "Okay", null);
			addToHistoryAndTriggerBoardUpdate(new State());
		} 
		catch (GameOverException e) {
			graphics.setMessage("New game because of error: " + e, "Okay", null);
			addToHistoryAndTriggerBoardUpdate(new State());
		}
	}
	
	/**
	 * When a new item is added to the history it triggers its value change handler in which the board will be updated
	 */
	private void addToHistoryAndTriggerBoardUpdate(State state){
		History.newItem(serializeState(state));		
	}

	/**
	 * Updates all elements that are necessary after the state changed
	 * 1. Update all the seedAmounts in the pits after a move was made 
	 * 2. It enables only the pits from the player whose turn it is
	 * 3. When there are zero seeds in a pit it can't be chosen either so disable them
	 * 4. Set a message in the case of game over
	 */
	private void updateBoard() {
		updatePits();
		enableActiveSide();
		disableZeroSeedPits();
		message();
	}
	
	/**
	 * When there are zero seeds in a pit it can't be chosen either so disable them
	 */
	private void disableZeroSeedPits() {
		int[] activePits = new int [7];
		if(state.getWhoseTurn().equals(PlayerColor.N))
			activePits = state.getNorthPits();
		else
			activePits = state.getSouthPits();
		
		for(int i = 0; i < state.getNorthPits().length; i++){
			if(activePits[i] == 0)
				graphics.setPitEnabled(i, state.getWhoseTurn(), false);			
		}
	}

	/**
	 * It enables only the pits from the player whose turn it is
	 */
	private void enableActiveSide() {
		boolean enableNorth;
		boolean enableSouth;
		
		if(state.getWhoseTurn().isNorth()){
			enableNorth = true;
			enableSouth = false;
		}
		else {
			enableNorth = false;
			enableSouth = true;
		}		
		
		for(int i = 0; i < state.getNorthPits().length-1; i++){
			graphics.setPitEnabled(i, PlayerColor.N, enableNorth);
			graphics.setPitEnabled(i, PlayerColor.S, enableSouth);
		}	
	}
	
	/**
	 * Set a message in the case of game over
	 */
	private void message(){
		if (state.isGameOver() == true) {
			if(state.winner() == null){
				graphics.setMessage("It's a tie!", "okay", "play again");
			}
			else{
				String winner = state.winner().equals(PlayerColor.N) ? "North" : "South";
				graphics.setMessage("The winner is " + winner
						  + " with a score of " + state.score(), "Okay", "Play again");
			}
		} 
	}

	/**
	 * Update all the seedAmounts in the pits after a move was made 
	 */
	private void updatePits() {
		for(int i = 0; i < state.getNorthPits().length; i++){
			graphics.setSeeds(i, PlayerColor.N, state.getNorthPits()[i]);
			graphics.setSeeds(i, PlayerColor.S, state.getSouthPits()[i]);
		}		
	}
	
	void setState(State state) {
		this.state = state;

		updateBoard();
	}

	public void newGame() {		
		addToHistoryAndTriggerBoardUpdate(new State());
	}
	
	/**
	 * Add the ValueChangeHandler responsible for traversing the browser history
	 */
    public void addHistoryValueChangeHandler() {
            History.addValueChangeHandler(new ValueChangeHandler<String> () {
                    @Override
                    public void onValueChange(ValueChangeEvent<String> event) {
                    	try{
                    		String historyToken = event.getValue();
                            setState(deserializeState(historyToken));
                    	}
                        catch(Exception e) {
                        	graphics.setMessage("New game because of error: " + e, "Okay", null);
                        	addToHistoryAndTriggerBoardUpdate(new State());
                        }
                    }
            });
    }

    /**
     * gets a String serialized state and deserializes it into a State object
     */
    private State deserializeState(String serialized) {
 	
    	int[] northPits = new int[7];
    	int[] southPits = new int[7];
    	PlayerColor whoseTurn = PlayerColor.S;
    	boolean gameOver = false;
    	
    	String[] serTokens = serialized.split("_");
    	
    	String[] nTokens = serTokens[0].split(",");
    	for(int i = 0; i < nTokens.length; i++)
    		northPits[i] = Integer.parseInt(nTokens[i]);

    	String[] sTokens = serTokens[1].split(",");
    	for(int i = 0; i < sTokens.length; i++)
    		southPits[i] = Integer.parseInt(sTokens[i]);
    	
		whoseTurn = serTokens[2].charAt(0) == 'N' ? PlayerColor.N : PlayerColor.S;
		
    	gameOver = serTokens[3].charAt(0) == 'F' ? false : true;

		return new State(northPits, southPits, whoseTurn, gameOver);
		
	}
	
    /**
     * gets a State state and serializes it into a String object
     * The pattern will be: north pits _ south pits _ whose turn _ game over
     * e.g. initial state would be 4,4,4,4,4,4,0_4,4,4,4,4,4,0_S_F
     */
    public String serializeState(State state) {
    	String serialized = "";
    	for(int i = 0; i < state.getNorthPits().length; i++){
    		if(i<state.getNorthPits().length-1)
    			serialized += state.getNorthPits()[i]+",";
    		else
    			serialized += state.getNorthPits()[i]+"_";
    	}

    	for(int i = 0; i < state.getSouthPits().length; i++){
    		if(i<state.getSouthPits().length-1)
    			serialized += state.getSouthPits()[i]+",";
    		else
    			serialized += state.getSouthPits()[i]+"_";
    	}

    	serialized += state.getWhoseTurn().toString()+"_";

    	serialized += state.isGameOver() ? "T":"F";
    	return serialized; 
    }
}
