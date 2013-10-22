package org.mancala.client;

import org.mancala.shared.PlayerColor;
import org.mancala.shared.State;
import org.mancala.shared.IllegalMoveException;
import org.mancala.shared.GameOverException;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The MVP-Presenter of the Mancala game
 * @author Micha Guthmann
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
	 * userId is represented by his email address
	 */
	private String userId="";
	
	/**
	 * keeps track of which side the player is. He is either South or North
	 */
	private PlayerColor iAm;
	
	
	
	/**
	 * The view has to support these methods to communicate with the presenter 
	 * @author Micha Guthmann
	 */
	public interface View {
	    
	    /**
	     * The seedAmount will be placed on the right side at the correct index 
	     */
		void setSeeds(PlayerColor side, int col, int seedAmount);
		
		/**
		 * A player can only select certain pits for their move. 
		 * That's why some have to be enabled and some have to be disabled before a player's turn. 
		 */
		void setPitEnabled(PlayerColor side, int col, boolean enabled);/**
		
		/**
		 * Informs the user of certain events. 
		 * If the parameter for the buttons is null no button will be displayed.
		 * The first button makes the information disappear, the second starts a new game
		 */
	    void setMessage(String labelMsg, String HideBtnText, String restartBtnText);
	    
	    /**
	     * Animate seeds moving from one pit to an other
	     */
	    void animateFromPitToPit(PlayerColor startSide, int startCol, PlayerColor endSide, int endCol, double delay, boolean finalAnimation);

        /**
         *  Cancel an animation (for if a game is loaded in the middle of an animation)
         */
        void cancelAnimation();
        
        /**
         *  Plays a sound for the event of a game finishing
         */
        void gameOverSound();
        
        /**
         *  Plays a sound for the event of catching seeds from the opposite pit
         */
        void oppositeCaptureSound();
        
        /**
         * Show the user who's turn it is somehow
         */
        void setWhoseTurn(PlayerColor side);
        
        /**
         * Make the user pairing system a little bit more transparent to the user.
         */
        void showUserPairUp(String message);
	}
	
	/**
	 * 1. Sets the view
	 * 2. Checks if there is already a state in the url fragment and if so initializes this
	 * 3. Initializes the History
	 * 4. Updates the gaming board accordingly
	 */
	public Presenter(View graphics) {
		this.graphics = graphics;
		state = new State();
		updateBoard();
	}
	
	/**
	 * Contact the server to register.
	 * The server will return a string to make the pair up a little bit more transparent. 
	 * The first player will see his name, the second player will see his name and who he plays against.
	 * The server will also return which player the user is (North or South).
	 */
	void initialServerContact() {
		AsyncCallback<String[]> callback = new AsyncCallback<String[]>() {
			public void onSuccess(String[] result) {
                 //Window.alert("initial server contact :" + result);
                 graphics.showUserPairUp(result[0]);
                 iAm = result[1].equals("N") ? PlayerColor.N : PlayerColor.S;
                 enableActiveSide();
            }
			public void onFailure(Throwable caught) {
                 Window.alert("initial server contact fail");
            }
		};
		
		MancalaServiceAsync ac = (MancalaServiceAsync) GWT.create(MancalaService.class);
		ac.AddPlayer(userId, callback);
	}

	/**
	 * makes a move on the model, adds the new state to the history and updates the board
	 */
	void makeMove(int index) {
		try {
			State oldState = state.copyState(); 
			state.makeMove(index);
			
			//contactServer() is called by the graphics class after the animation is done  
			enableActiveSide();
			disableZeroSeedPits();
			animateMove(index, oldState);			
			message();
		} 
		catch (IllegalMoveException e) {
			graphics.setMessage("New game because of error: " + e, "Okay", null);
			setState(new State());
		} 
		catch (GameOverException e) {
			graphics.setMessage("New game because of error: " + e, "Okay", null);
			setState(new State());
		}
	}

	/**
	 * Contact the server to submit the move the user made
	 */
	void contactServer() {
		AsyncCallback<String> callback = new AsyncCallback<String>() {
			public void onSuccess(String result) {
                 //Window.alert("testmicha :" + result);
            }
			public void onFailure(Throwable caught) {
                 Window.alert("contacting the server failed.");
            }
		};
		
		MancalaServiceAsync ac = (MancalaServiceAsync) GWT.create(MancalaService.class);
		ac.SubMove(Presenter.serializeState(state), userId, callback);
	}

	/**
	 * Updates all elements that are necessary after the state changed
	 * 1. Update all the seedAmounts in the pits after a move was made 
	 * 2. It enables only the pits from the player whose turn it is
	 * 3. When there are zero seeds in a pit it can't be chosen either so disable them
	 * 4. Set a message in the case of game over
	 */
	void updateBoard() {
		updatePits();
		enableActiveSide();
		disableZeroSeedPits();
		message();
	}

	/**
	 * It enables only the pits from the player whose turn it is 
	 */
	void enableActiveSide() {
		boolean enableNorth;
		boolean enableSouth;
		
		if(state.getWhoseTurn().isNorth() && iAm.isNorth()){
			graphics.setWhoseTurn(PlayerColor.N);
			enableNorth = true;
			enableSouth = false;
		}
		else if(state.getWhoseTurn().isNorth() && iAm.isSouth()){
			graphics.setWhoseTurn(PlayerColor.N);
			enableNorth = false;
			enableSouth = false;
		}
		else if(state.getWhoseTurn().isSouth() && iAm.isSouth()){
			graphics.setWhoseTurn(PlayerColor.S);
			enableNorth = false;
			enableSouth = true;
		}
		else if(state.getWhoseTurn().isSouth() && iAm.isNorth()){
			graphics.setWhoseTurn(PlayerColor.S);
			enableNorth = false;
			enableSouth = false;
		}	
		else { //iAm is not yet initialized 
			graphics.setWhoseTurn(PlayerColor.S);
			enableNorth = false;
			enableSouth = false;
		}
		
		for(int i = 0; i < state.getNorthPits().length-1; i++){
			graphics.setPitEnabled(PlayerColor.N, i, enableNorth);
			graphics.setPitEnabled(PlayerColor.S, i, enableSouth);
		}	
	}

	/**
	 * When there are zero seeds in a pit it can't be chosen either so disable them
	 */
	void disableZeroSeedPits() {
		int[] activePits = new int [7];
		if(state.getWhoseTurn().equals(PlayerColor.N))
			activePits = state.getNorthPits();
		else
			activePits = state.getSouthPits();
		
		//state.getNorthPits().length-1 because the last array field is the treasure chest
		for(int i = 0; i < state.getNorthPits().length-1; i++){
			if(activePits[i] == 0)
				graphics.setPitEnabled(state.getWhoseTurn(), i, false);			
		}
	}
	
	/**
	 * Set a message in the case of game over
	 */
	void message(){
		if (state.isGameOver() == true) {
			graphics.gameOverSound();
			
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
			graphics.setSeeds(PlayerColor.N, i, state.getNorthPits()[i]);
			graphics.setSeeds(PlayerColor.S, i, state.getSouthPits()[i]);
		}		
	}

	/**
	 * After the user clicked on a pit the seeds should be distributed in an animated fashion
	 * 
	 * @param chosenPitIndex the index the user chose to distribute the seeds from
	 * @param oldState the state before the user chose his pit
	 */
	private void animateMove(int chosenPitIndex, State oldState) {
		
		PlayerColor whoseTurn = oldState.getWhoseTurn();
		PlayerColor sideToPlaceSeedOn = whoseTurn;
		int seedAmount = oldState.getPitsOfWhoseTurn()[chosenPitIndex];
		boolean lastAnimation = false;
		int indexToPlaceSeedIn = chosenPitIndex;
		int maxIndex = 6;
		for(int i = 1; i <= seedAmount; i++) {
			indexToPlaceSeedIn++;
			maxIndex = whoseTurn.equals(sideToPlaceSeedOn) ? 6 : 5;
			if((indexToPlaceSeedIn) > maxIndex) {
				sideToPlaceSeedOn = sideToPlaceSeedOn.getOpposite();
				indexToPlaceSeedIn = 0;
			}
			if(i == seedAmount)
				lastAnimation = true;
			graphics.animateFromPitToPit(whoseTurn, chosenPitIndex, sideToPlaceSeedOn, indexToPlaceSeedIn, 400 * (i-1), lastAnimation);
			
		}
		
		if(this.state.getLastMoveWasOppositeCapture()) {
			graphics.oppositeCaptureSound();
			//I have to give this it's own animation down the line
			
//			//int[] opposingPits = whoseTurn.isNorth() ? this.state.getSouthPits() : state.getNorthPits();
//			int seedAmountInOpposingPit = this.state.getOppositeSeeds();
//
//			graphics.animateFromPitToPit(whoseTurn, indexToPlaceSeedIn, whoseTurn, 6, seedAmount * 400 + 1400);
//			for(int i = 0; i < seedAmountInOpposingPit; i++)
//				graphics.animateFromPitToPit(whoseTurn.getOpposite(), State.getMirrorIndex(indexToPlaceSeedIn, 5), whoseTurn, 6, seedAmount * 400 + 1000 + 400 * i);
		}
	}
	
	void setState(State state) {
		this.state = state;

		updateBoard();
	}

	public void newGame() {		
		setState(new State());
	}

	void setId(String id) {
		this.userId=id;
	}
	
    /**
     * gets a String serialized state and deserializes it into a State object
     */
    static State deserializeState(String serialized) {
 	
    	int[] northPits = new int[7];
    	int[] southPits = new int[7];
    	PlayerColor whoseTurn = PlayerColor.S;
    	boolean gameOver = false;
    	boolean lastMoveWasOppositeCapture = false;
    	int oppositeSeeds = 0; 
    	
    	String[] serTokens = serialized.split("_");
    	
    	String[] nTokens = serTokens[0].split(",");
    	for(int i = 0; i < nTokens.length; i++)
    		northPits[i] = Integer.parseInt(nTokens[i]);

    	String[] sTokens = serTokens[1].split(",");
    	for(int i = 0; i < sTokens.length; i++)
    		southPits[i] = Integer.parseInt(sTokens[i]);
    	
		whoseTurn = serTokens[2].charAt(0) == 'N' ? PlayerColor.N : PlayerColor.S;
		
    	gameOver = serTokens[3].charAt(0) == 'F' ? false : true;
    	
    	lastMoveWasOppositeCapture = serTokens[4].charAt(0) == 'F' ? false : true;
    	
    	oppositeSeeds = Integer.parseInt(serTokens[5]);

		return new State(northPits, southPits, whoseTurn, gameOver, lastMoveWasOppositeCapture, oppositeSeeds);
		
	}
	
    /**
     * Gets a State state and serializes it into a String object
     * The pattern will be: north pits _ south pits _ whose turn _ game over _ lastMoveWasOppositeCapture _ oppositeSeeds
     * e.g. initial state would be 4,4,4,4,4,4,0_4,4,4,4,4,4,0_S_F_F_0
     */
    static public String serializeState(State state) {
    	String serialized = "";
    	for(int i = 0; i < state.getNorthPits().length; i++){
    		if(i < state.getNorthPits().length-1)
    			serialized += state.getNorthPits()[i]+",";
    		else
    			serialized += state.getNorthPits()[i]+"_";
    	}

    	for(int i = 0; i < state.getSouthPits().length; i++){
    		if(i < state.getSouthPits().length-1)
    			serialized += state.getSouthPits()[i]+",";
    		else
    			serialized += state.getSouthPits()[i]+"_";
    	}

    	serialized += state.getWhoseTurn().toString()+"_";

    	serialized += state.isGameOver() ? "T_":"F_";

    	serialized += state.getLastMoveWasOppositeCapture() ? "T_":"F_";
    	
    	serialized += state.getOppositeSeeds()+"";
    	
    	return serialized; 
    }
}
