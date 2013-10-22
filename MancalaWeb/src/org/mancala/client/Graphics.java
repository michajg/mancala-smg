package org.mancala.client;

import org.mancala.client.Presenter.View;
import org.mancala.shared.IllegalMoveException;
import org.mancala.shared.PlayerColor;
import org.mancala.shared.State;

import com.google.gwt.core.client.Duration;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AudioElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.media.client.Audio;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * The MVP-View of the Mancala game 
 * @author Micha Guthmann
 */
public class Graphics extends Composite implements View{
	final int TREASURE_CHEST_WIDTH = 66;
	final int TREASURE_CHEST_HEIGHT = 172;
	final int PIT_WIDTH = 66;
	final int PIT_HEIGHT = 66;
	final int PADDING = 20;

	
	private static GameImages gameImages = GWT.create(GameImages.class);
	private static GameSounds gameSounds = GWT.create(GameSounds.class);
	private static GraphicsUiBinder uiBinder = GWT.create(GraphicsUiBinder.class);
	
	interface GraphicsUiBinder extends UiBinder<Widget, Graphics> {
	}
	
	/**
	 * The MVP-Presenter 
	 */
	final Presenter presenter;
	
	/**
	 * This is so that if it's not a users turn the click handlers can be taken away from the pits
	 */
	HandlerRegistration[][] handlerRegs = new HandlerRegistration[2][6];
	
	/**
	 * An animation to distribute the seeds
	 */
	private SeedMovingAnimation animation;
	
	/**
	 * The audio sounds that are used in the game
	 */
	Audio dotSound;	
	Audio gameOverSound;
	Audio oppositeCaptureSound;
	
	/**
	 * Note: UI is still proof of concept. I will add better images and better layout in the next homeworks
	 * The basis is an absolute panel
	 */
	@UiField AbsolutePanel gameAbsolutePanel;
	
	/**
	 * To the left is one treasure chest
	 */
	Grid treasureGridN;
	
	/**
	 * In the middle are 12 pits the user can take action on,...
	 */
	Grid gameGrid;

	/**
	 * To the right is the other treasure chest
	 */
	Grid treasureGridS;
	
	/**
	 * Keeps track of who has to make the next turn
	 */
	@UiField Label turnLabel;
	
	/**
	 * Warns the user for example if he clicked on a pit he is not allowed to choose 
	 */
	@UiField Label warnLabel;
	
	/**
	 * displays who the user is, which side he plays on and sometimes how the opponent is called
	 */
	@UiField Label userLabel;
	
	/**
	 * To inform the user of certain events a PopupPabel will be used
	 */
	@UiField PopupPanel gamePopupPanel;
	
	/**
	 * Initializes the Graphics
	 */
	public Graphics() {
	    initWidget(uiBinder.createAndBindUi(this));
	    
	    treasureGridN = new Grid(1, 1);
	    treasureGridN.setHeight(TREASURE_CHEST_HEIGHT + PADDING * 2 + "px");
	    treasureGridN.setCellPadding(PADDING);
	    
	    treasureGridS = new Grid(1, 1);
	    treasureGridS.setHeight(TREASURE_CHEST_HEIGHT + PADDING * 2 + "px");
	    treasureGridS.setCellPadding(PADDING);
	    
	    AbsolutePanel treasurePanelN = new AbsolutePanel();
	    treasurePanelN.setSize(TREASURE_CHEST_WIDTH + "px", TREASURE_CHEST_HEIGHT + "px");
	    treasureGridN.setWidget(0, 0, treasurePanelN);
	    
	    AbsolutePanel treasurePanelS = new AbsolutePanel();
	    treasurePanelS.setSize(TREASURE_CHEST_WIDTH + "px", TREASURE_CHEST_HEIGHT + "px");
	    treasureGridS.setWidget(0, 0, treasurePanelS);
	    
	    gameGrid = new Grid(2, 6);
	    gameGrid.resize(2, 6);
	    for(int row = 0; row < 2; row++){
	    	for(int col = 0; col < 6; col++){
	    		AbsolutePanel pitPanel = new AbsolutePanel();
        		pitPanel.setSize(PIT_WIDTH + "px", PIT_HEIGHT + "px");	
        		
        		addSeeds(pitPanel, 4);
        		
	            if(row == 0){
	            	final int colB = col;
                	handlerRegs[row][col] = pitPanel.addDomHandler(new ClickHandler() {
            	          @Override
            	          public void onClick(ClickEvent event) {
            	            presenter.makeMove(colB);
            	          }
            	        }, ClickEvent.getType());
                	
	            	gameGrid.setWidget(row, 5-col, pitPanel);
	            	setPitEnabled(PlayerColor.NORTH, col, false);
	            }
	            else{     		
		            final int colB = col;
                	handlerRegs[row][col] = pitPanel.addDomHandler(new ClickHandler() {
            	          @Override
            	          public void onClick(ClickEvent event) {
            	            presenter.makeMove(colB);
            	          }
            	        }, ClickEvent.getType());

            		gameGrid.setWidget(row, col, pitPanel);
	            	setPitEnabled(PlayerColor.SOUTH, col, true);
	            }
	            
	    	}
	    }
	    
	    gameGrid.setCellPadding(20);

	    gameAbsolutePanel.add(treasureGridN);
	    gameAbsolutePanel.add(gameGrid, TREASURE_CHEST_WIDTH + 2 * PADDING, 0);
	    gameAbsolutePanel.add(treasureGridS, TREASURE_CHEST_WIDTH + 14 * PADDING + PIT_WIDTH * 6 + 6 * 2, 0);
	    gameAbsolutePanel.setSize(12 + TREASURE_CHEST_WIDTH * 2 + 16 * PADDING + PIT_WIDTH * 6 + "px", 
	    		4 + 4 * PADDING + 2 * PIT_HEIGHT + "px");
	    gameAbsolutePanel.getElement().getStyle().setProperty("margin", "auto");
	   
	    DOM.setStyleAttribute(gameAbsolutePanel.getElement(), "backgroundColor", "#b0c4de");
	    Image bgImg = new Image();
	    bgImg.setResource(gameImages.board());
	    
	    DOM.setStyleAttribute(gameAbsolutePanel.getElement(), "backgroundImage", "url("+bgImg.getUrl() +")");
	    
		presenter = new Presenter(this);
		
		turnLabel.setHorizontalAlignment(Label.ALIGN_CENTER);
		warnLabel.setHorizontalAlignment(Label.ALIGN_CENTER);
		userLabel.setHorizontalAlignment(Label.ALIGN_CENTER);
		
		initializeAudios();
	}
	
	private void initializeAudios() {
		if (Audio.isSupported()) {
            dotSound = Audio.createIfSupported();
            dotSound.addSource(gameSounds.dotMp3().getSafeUri()
                            .asString(), AudioElement.TYPE_MP3);
            dotSound.addSource(gameSounds.dotWav().getSafeUri()
                            .asString(), AudioElement.TYPE_WAV);
            
            gameOverSound = Audio.createIfSupported();
            gameOverSound.addSource(gameSounds.gameOverMp3().getSafeUri()
                            .asString(), AudioElement.TYPE_MP3);
            gameOverSound.addSource(gameSounds.gameOverWav().getSafeUri()
                            .asString(), AudioElement.TYPE_WAV);
            
            oppositeCaptureSound = Audio.createIfSupported();
            oppositeCaptureSound.addSource(gameSounds.oppositeCaptureMp3().getSafeUri()
                            .asString(), AudioElement.TYPE_MP3);
            oppositeCaptureSound.addSource(gameSounds.oppositeCaptureWav().getSafeUri()
                            .asString(), AudioElement.TYPE_WAV);
		}
	}
	
	/**
	 * This calculates the x and the y value where in a pit a seed should be placed
	 * 
	 * @param index in which place should the seed be placed 
	 * @return an int[] array - [0] will contain x value, [1] will contain y value
	 */
	int[] getTargetPoint(int index) {
		int[] point = new int[2];
		
		//the middle is supposed to be left out
		if(index > 3) {
			index++;
		}
		
		if(index < 9) {
			
			//left
			point[0] = (index % 3) * 22;
			//top
			point[1] = (index / 3) * 22;
		}
		else {
			//if more than 8 seeds are in a pit it's only indicated by the number
			//left
			point[0] = (8 % 3) * 22;
			//top
			point[1] = (8 / 3) * 22;
		}
		
		return point;
	}
	
	/**
	 * The treasure chests have their own method to calculate the x and y values 
	 * 
	 * @param index in which place should the seed be placed 
	 * @return an int[] array - [0] will contain x value, [1] will contain y value
	 */
	int[] getTargetPointTreasureChest(int index) {
		int[] point = new int[2];
		
		//the middle is supposed to be left out
		if(index > 9) {
			index++;
		}
		
		if(index < 21) {
			//left
			point[0] = (index % 3) * 22;
			//top
			point[1] = (index / 3) * 22;
		}
		else {
			//left
			point[0] = (20 % 3) * 22;
			//top
			point[1] = (20 / 3) * 22;
		}
		
		return point;
	}
	
	/**
	 * Add the seed images to a pit absolutePanel
	 * 
	 * @param pitPanel where the seeds are being placed in
	 * @param seedAmount how many seeds are being placed into the pit
	 */
	private void addSeeds(AbsolutePanel pitPanel, int seedAmount) {
		pitPanel.clear();
		
		for(int i = 0; i < seedAmount; i++){
			final Image seed = new Image();
	        seed.setResource(gameImages.redSeed());
	        DOM.setStyleAttribute(seed.getElement(), "backgroundSize", "20px 20px");

	        int[] point = getTargetPoint(i);
			pitPanel.add(seed, point[0], point[1]);	
		}	
		Label countLabel = new Label(seedAmount + "");
		countLabel.setWidth(PIT_WIDTH+"px");
		countLabel.setHorizontalAlignment(Label.ALIGN_CENTER);
		pitPanel.add(countLabel, 0, 22);
		
	}
	
	/**
	 * The treasureChests have their own method to place seeds in
	 * 
	 * @param treasurePanel where the seeds are being placed in
	 * @param seedAmount how many seeds are being placed into the pit
	 */
	private void addSeedsToTreasureChest(AbsolutePanel treasurePanel, int seedAmount) {
		treasurePanel.clear();
		
		for(int i = 0; i < seedAmount; i++){
			final Image seed = new Image();
	        seed.setResource(gameImages.redSeed());
	        DOM.setStyleAttribute(seed.getElement(), "backgroundSize", "20px 20px");

	        int[] point = getTargetPointTreasureChest(i);
			treasurePanel.add(seed, point[0], point[1]);	
		}	
		Label countLabel = new Label(seedAmount + "");
		countLabel.setWidth(TREASURE_CHEST_WIDTH+"px");
		countLabel.setHorizontalAlignment(Label.ALIGN_CENTER);
		treasurePanel.add(countLabel, 0, 22*3);
	}


	/**
	 * The seedAmount will be placed on the right side at the correct index 
	 */
	@Override
	public void setSeeds(PlayerColor side, int col, int seedAmount){
		if(col < 6){
			AbsolutePanel pnl;
			if(side.isNorth()){
				pnl = (AbsolutePanel) gameGrid.getWidget(0, State.getMirrorIndex(col, 5));
			}
			else if(side.isSouth()){
				pnl = (AbsolutePanel) gameGrid.getWidget(1, col);
			}
			else
				throw new IllegalMoveException();
			
			addSeeds(pnl, seedAmount);
		}
		else if(col == 6){
			AbsolutePanel pnl;
			if(side.isNorth()){
				pnl = (AbsolutePanel) treasureGridN.getWidget(0, 0);
			}
			else if(side.isSouth()){
				pnl = (AbsolutePanel) treasureGridS.getWidget(0, 0);
			}
			else
				throw new IllegalMoveException();
			addSeedsToTreasureChest(pnl, seedAmount);
		} else 
			throw new IllegalMoveException();
		
	}
	

	/**
	 * To animate a seed from one pit to another
	 */
	@Override
	public void animateFromPitToPit(PlayerColor startSide, int startCol, PlayerColor endSide, int endCol, double delay, boolean finalAnimation) {
		int startRow = startSide.isNorth() ? 0 : 1;
		int actualStartCol = (startRow == 0) ? 5 - startCol : startCol;
		int endRow = endSide.isNorth() ? 0 : 1;
		int actualEndCol = (endRow == 0) ? 5 - endCol : endCol;
		
		AbsolutePanel startPanel = (AbsolutePanel) gameGrid.getWidget(startRow, actualStartCol);
		final Image seed;
		seed = (Image) startPanel.getWidget(startPanel.getWidgetCount()-2);
		int startXStartPanel = startPanel.getWidgetLeft(seed);
		int startYStartPanel = startPanel.getWidgetTop(seed);
        int startX = 2 + actualStartCol * 2 + TREASURE_CHEST_WIDTH + PIT_WIDTH * actualStartCol + PADDING * (actualStartCol * 2 + 3) + startXStartPanel;
        int startY = 2 + PADDING + startRow * (PIT_HEIGHT + 2 * PADDING + 2) + startYStartPanel; 
        
		AbsolutePanel endPanel;
        int[] endPointEndPanel;
        int endXEndPanel;
        int endYEndPanel;
        int endX;
        int endY;
		if(endCol < 6) {
			endPanel = (AbsolutePanel) gameGrid.getWidget(endRow, actualEndCol);
	        endPointEndPanel = getTargetPoint(endPanel.getWidgetCount()-1);
	        endXEndPanel = endPointEndPanel[0];
	        endYEndPanel = endPointEndPanel[1];
	        endX = 2 + actualEndCol * 2 + TREASURE_CHEST_WIDTH + PIT_WIDTH * actualEndCol + PADDING * (actualEndCol * 2 + 3) + endXEndPanel;
	        endY = 2 + PADDING + endRow * (PIT_HEIGHT + 2 * PADDING + 2) + endYEndPanel;
		}
		else {
			Grid hGrid = endRow == 0 ? treasureGridN : treasureGridS; 
			endPanel = (AbsolutePanel) hGrid.getWidget(0, 0);
	        endPointEndPanel = getTargetPointTreasureChest(endPanel.getWidgetCount()-1);
	        endXEndPanel = endPointEndPanel[0];
	        endYEndPanel = endPointEndPanel[1];
	        endX = 2 + PADDING + (TREASURE_CHEST_WIDTH +  PIT_WIDTH * 6 + PADDING * 14 + 6 * 2) * endRow + endXEndPanel;
	        endY = 2 + PADDING + endYEndPanel;
		}

        animation = new SeedMovingAnimation(seed, gameImages.redSeed(), startPanel, endPanel, startXStartPanel, startYStartPanel, endXEndPanel, endYEndPanel, startX, startY, endX, endY, finalAnimation, this, dotSound);
        animation.run(1000, Duration.currentTimeMillis() + delay);
	}
	
	
	/**
	 * A player can only select certain pits for their move. 
	 * That's why some have to be enabled and some have to be disabled before a player's turn. 
	 */
	@Override
	public void setPitEnabled(PlayerColor side, int col, boolean enabled) {
		AbsolutePanel pnl;		
		if(col < 6){
			if(side.isNorth()){
				pnl = (AbsolutePanel) gameGrid.getWidget(0, State.getMirrorIndex(col, 5));
			}
			else if(side.isSouth()){
				pnl = (AbsolutePanel) gameGrid.getWidget(1, col);
			}
			else
				throw new IllegalMoveException();
			
			final int colB = col;
			int row = side.isNorth() ? 0 : 1;
			
			if(enabled) {
				//the handerRegs keeps track of all click handlers of the pits
				//by removing it from this array the handler is removed from the pit (absolutePanel) as well
				handlerRegs[row][col].removeHandler();
				handlerRegs[row][col] = pnl.addDomHandler(new ClickHandler() {
	    	          @Override
	    	          public void onClick(ClickEvent event) {
	    	            presenter.makeMove(colB);
	    	            warnLabel.setText("");
	    	          }
	    	        }, ClickEvent.getType());
			}
			else {		
				handlerRegs[row][col].removeHandler();
				handlerRegs[row][col] = pnl.addDomHandler(new ClickHandler() {
	    	          @Override
	    	          public void onClick(ClickEvent event) {
	    	            warnLabel.setText("You can only choose one of your own pits with at least one seed when it's your turn!");
	    	          }
	    	        }, ClickEvent.getType());
			}	
			
//			
//			AbsolutePanel pnl;
//			if(side.isNorth()){
//				pnl = (AbsolutePanel) gameGrid.getWidget(0, State.getMirrorIndex(index, 5));
//			}
//			else if(side.isSouth()){
//				pnl = (AbsolutePanel) gameGrid.getWidget(1, index);
//			}
//			else
//				throw new IllegalMoveException();
			
			//setPitPanelEnabled(pnl, enabled, );
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
		Label lbl = new Label(labelMsg);
		lbl.setHorizontalAlignment(Label.ALIGN_CENTER);
		vPanel.add(lbl);
		if(HideBtnText != null){
			Button okayButton = new Button(HideBtnText);
			okayButton.addClickHandler(new ClickHandler() {
	          @Override
	          public void onClick(ClickEvent event) {
	        	  gamePopupPanel.hide();
	          }
	        });
			okayButton.getElement().getStyle().setProperty("marginLeft", "auto");
			okayButton.getElement().getStyle().setProperty("marginRight", "auto");
			vPanel.add(okayButton);
		}
		if(restartBtnText != null){
			Button playAgainButton = new Button(restartBtnText);
			playAgainButton.addClickHandler(new ClickHandler() {
	          @Override
	          public void onClick(ClickEvent event) {
	        	  presenter.newGame();
	        	  gamePopupPanel.hide();
	          }
	        });
			playAgainButton.getElement().getStyle().setProperty("marginLeft", "auto");
			playAgainButton.getElement().getStyle().setProperty("marginRight", "auto");
			vPanel.add(playAgainButton);
		}
		
		gamePopupPanel.clear();
		gamePopupPanel.setAutoHideEnabled(true);
		gamePopupPanel.add(vPanel);	
//		gamePopupPanel.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
//	          public void setPosition(int offsetWidth, int offsetHeight) {
//	              int left = (Window.getClientWidth() - offsetWidth) / 3;
//	              int top = (Window.getClientHeight() - offsetHeight) / 3;
//	              gamePopupPanel.setPopupPosition(left, top);
//	            }
//	          });	
		gamePopupPanel.center();
	}

	@Override
	public void cancelAnimation() {
	    if (animation != null) {
	            animation.cancel();
	            animation = null;
	    }
    }
	
	@Override
	public void gameOverSound() {
		if (gameOverSound != null)
			gameOverSound.play();
	}
	
	@Override
	public void oppositeCaptureSound() {
		if (oppositeCaptureSound != null)
			oppositeCaptureSound.play();
	}

	@Override
	public void setWhoseTurn(PlayerColor side) {
		if(side.isNorth())
			turnLabel.setText("It is Norths turn");
		else if(side.isSouth())
			turnLabel.setText("It is Souths turn");
		else
			turnLabel.setText("The game is over");
	}

	/**
	 * Every time the last seed is placed in a pit update the game board
	 * This is a safety precaution that the UI state does not solely rely on the correct animation,
	 * so the model is questioned again for the correct amount of seeds everywhere
	 * 
	 * After that contact the server and 
	 */
	public void afterAnimation() {
		updateBoard();
		presenter.contactServer();
	}
	
	private void updateBoard() {
		presenter.updateBoard();
	}

	@Override
	public void showUserPairUp(String message) {
		userLabel.setText(message);
	}


}
