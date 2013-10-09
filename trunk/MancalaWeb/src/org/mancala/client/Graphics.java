package org.mancala.client;

import org.mancala.client.Presenter.View;
import org.mancala.shared.IllegalMoveException;
import org.mancala.shared.PlayerColor;
import org.mancala.shared.State;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * The MVP-View of the Mancala game 
 * @author Micha Guthmann
 */
public class Graphics extends Composite implements View{
	//private static GameImages gameImages = GWT.create(GameImages.class);
	private static GraphicsUiBinder uiBinder = GWT.create(GraphicsUiBinder.class);
	
	interface GraphicsUiBinder extends UiBinder<Widget, Graphics> {
	}
	
	/**
	 * The MVP-Presenter 
	 */
	private final Presenter presenter;
	
	/**
	 * Note: UI is proof of concept. I will add images and better layout in the next homeworks
	 * The basis is a horizontal panel
	 */
	@UiField HorizontalPanel gameHorizontalPanel;
	
	/**
	 * To the left is one treasure chest
	 */
	@UiField Grid treasureGridN;
	
	/**
	 * In the middle are the 12 pits the user can take action on
	 */
	@UiField Grid gameGrid;
	
	/**
	 * To the right is the other treasure chest
	 */
	@UiField Grid treasureGridS;
	
	/**
	 * To inform the user of certain events a PopupPabel will be used
	 */
	@UiField PopupPanel gamePopupPanel;
	
	/**
	 * Initializes the Graphics
	 * To display the pits and seeds Buttons and their text is used. This is only a proof of concept.
	 */
	public Graphics() {
	    initWidget(uiBinder.createAndBindUi(this));
	    
//	    Image image1 = new Image();
//	    image1.setWidth("100%");
//	    image1.setResource(gameImages.longTile());	  
//
//	    Image image2 = new Image();
//	    image2.setWidth("100%");
//	    image2.setResource(gameImages.longTile());
	    
	    treasureGridN = new Grid(1, 1);
	    treasureGridN.setText(0, 0, "0");
	    treasureGridN.setBorderWidth(20);
	    treasureGridN.setCellPadding(50);
	    treasureGridS = new Grid(1, 1);
	    treasureGridS.setText(0, 0, "0.0");
	    treasureGridS.setBorderWidth(20);
	    treasureGridS.setCellPadding(50);
	    
	    gameGrid = new Grid(2, 6);
	    gameGrid.resize(2, 6);
	    for(int row = 0; row < 2; row++){
	    	for(int col = 0; col < 6; col++){
//	    		final Image image = new Image();
//		        image.setWidth("100%");
//		        image.setResource(gameImages.shortTile());
	    		Button button = new Button("4");
	    		
	            final int colB = col;
	            if(row == 0){
	            	button.addClickHandler(new ClickHandler() {
	    	          @Override
	    	          public void onClick(ClickEvent event) {
	    	            presenter.makeMove(colB);
	    	          }
	    	        });
		    		button.setEnabled(false);
		    		gameGrid.setWidget(row, 5-col, button);
	            }
	            else{
	            	button.addClickHandler(new ClickHandler() {
	    	          @Override
	    	          public void onClick(ClickEvent event) {
	    	            presenter.makeMove(colB);
	    	          }
	    	        });
		    		gameGrid.setWidget(row, col, button);
	            }
	    	}
	    }
	    
	    gameGrid.setCellPadding(50);

	    gameHorizontalPanel.add(treasureGridN);
	    gameHorizontalPanel.add(gameGrid);
	    gameHorizontalPanel.add(treasureGridS);

		presenter = new Presenter(this);
	}
	
	/**
	 * The seedAmount will be placed on the right side at the correct index 
	 */
	@Override
	public void setSeeds(int index, PlayerColor side, int seedAmount){
		if(index < 6){
			Button btn;
			if(side.isNorth()){
				btn = (Button) gameGrid.getWidget(0, State.getMirrorIndex(index, 5));
			}
			else if(side.isSouth()){
				btn = (Button) gameGrid.getWidget(1, index);
			}
			else
				throw new IllegalMoveException();
			
			btn.setText(""+seedAmount);
		}
		else if(index == 6){
			if(side.isNorth()){
				treasureGridN.setText(0, 0, ""+seedAmount);
			}
			else if(side.isSouth()){
				treasureGridS.setText(0, 0, ""+seedAmount);
			}
			else
				throw new IllegalMoveException();
		} else 
			throw new IllegalMoveException();
		
	}
	
	/**
	 * A player can only select certain pits for their move. 
	 * That's why some have to be enabled and some have to be disabled before a player's turn. 
	 */
	@Override
	public void setPitEnabled(int index, PlayerColor side, boolean enabled) {
		if(index < 6){
			Button btn;
			if(side.isNorth()){
				btn = (Button) gameGrid.getWidget(0, State.getMirrorIndex(index, 5));
			}
			else if(side.isSouth()){
				btn = (Button) gameGrid.getWidget(1, index);
			}
			else
				throw new IllegalMoveException();
			
			btn.setEnabled(enabled);
		}
	}

	/**
	 * Informs the user of certain events. 
	 * If the parameter for the buttons is null no button will be displayed.
	 * The first button makes the information disappear, the second starts a new game
	 */
	@Override
	public void setMessage(String labelMsg, String HideBtnText, String restartBtnText) {
		VerticalPanel vPanel = new VerticalPanel();
		vPanel.add(new Label(labelMsg));
		if(HideBtnText != null){
			Button okayButton = new Button(HideBtnText);
			okayButton.addClickHandler(new ClickHandler() {
	          @Override
	          public void onClick(ClickEvent event) {
	        	  gamePopupPanel.hide();
	          }
	        });
			vPanel.add(okayButton);
		}
		if(restartBtnText != null){
			Button playAgainButton = new Button(restartBtnText);
			playAgainButton.addClickHandler(new ClickHandler() {
	          @Override
	          public void onClick(ClickEvent event) {
	        	  gamePopupPanel.hide();
	        	  presenter.newGame();
	          }
	        });
			vPanel.add(playAgainButton);
		}
		
		gamePopupPanel = new PopupPanel();
		gamePopupPanel.setAutoHideEnabled(true);
		gamePopupPanel.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
	          public void setPosition(int offsetWidth, int offsetHeight) {
	              int left = (Window.getClientWidth() - offsetWidth) / 3;
	              int top = (Window.getClientHeight() - offsetHeight) / 3;
	              gamePopupPanel.setPopupPosition(left, top);
	            }
	          });
		gamePopupPanel.add(vPanel);		
	}
	
	/**
	 * Check if there was a previous game - if not return a new game
	 * Look at the url fragment and try to deserialize a state
	 */
	@Override
	public State getPreviousGame() {
		String urlFragment = Window.Location.getHash();
		State newState = new State();
		if(urlFragment != "" && urlFragment != null && urlFragment.length() > 1){
			try {
				newState = presenter.deserializeState(urlFragment.substring(1, urlFragment.length()));
			}
			catch (Exception e) {
				setMessage("New game because of error: " + e, "Okay", null);
				newState = new State();
			}
		}
		return newState;
	}

	/**
	 * Add a serialized State to the history so a user can undo and redo actions
	 */
	@Override
	public void setHistoryNewItem(String serializedState) {
		History.newItem(serializedState);
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
                            presenter.setState(presenter.deserializeState(historyToken));
                    	}
                        catch(Exception e) {
                        	setMessage("New game because of error: " + e, "Okay", null);
                        	presenter.addHistoryTriggerBoardUpdate(new State());
                        }
                    }
            });
    }
    
}
