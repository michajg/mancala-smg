package org.mancala.client;

import java.util.List;

import org.mancala.client.View;
import org.mancala.client.animation.AIAdditionalMoveAnimation;
import org.mancala.client.animation.FadeAnimation;
import org.mancala.client.animation.SeedMovingAnimation;
import org.mancala.client.audio.GameSounds;
import org.mancala.client.gwtfb.sdk.FBCore;
import org.mancala.client.i18n.MancalaMessages;
import org.mancala.client.img.GameImages;
import org.mancala.shared.PlayerColor;
import org.mancala.shared.State;
import org.mancala.shared.exception.IllegalMoveException;

import com.google.common.collect.Lists;
import com.google.gwt.core.client.Duration;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AudioElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.media.client.Audio;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.cellview.client.HasKeyboardPagingPolicy.KeyboardPagingPolicy;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

/**
 * The MVP-View of the Mancala game
 * 
 * @author Micha Guthmann
 */
public class Graphics extends Composite implements View {
	final int TREASURE_CHEST_WIDTH = 66;
	final int TREASURE_CHEST_HEIGHT = 172;
	final int PIT_WIDTH = 66;
	final int PIT_HEIGHT = 66;
	final int PADDING = 20;

	private static GameImages gameImages = GWT.create(GameImages.class);
	private static GameSounds gameSounds = GWT.create(GameSounds.class);
	private static GraphicsUiBinder uiBinder = GWT.create(GraphicsUiBinder.class);
	private static MancalaMessages messages = GWT.create(MancalaMessages.class);

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
	private FadeAnimation fadeAnimation;
	private AIAdditionalMoveAnimation aiAdditionalMoveAnimation;

	/**
	 * The audio sounds that are used in the game
	 */
	Audio dotSound;
	Audio gameOverSound;
	Audio oppositeCaptureSound;

	/**
	 * Note: UI is still proof of concept. I will add better images and better layout in the next homeworks The basis is an absolute
	 * panel
	 */
	@UiField
	AbsolutePanel gameAbsolutePanel;

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
	@UiField
	Label turnLabel;

	/**
	 * Warns the user for example if he clicked on a pit he is not allowed to choose
	 */
	@UiField
	Label warnLabel;

	@UiField
	Label aiMovesLabel;

	/**
	 * displays which side the user plays on
	 */
	@UiField
	Label sideLabel;

	/**
	 * displays user name of player
	 */
	@UiField
	Label userNameLabel;

	/**
	 * displays the start date of the Match
	 */
	@UiField
	Label startDateLabel;

	@UiField
	Label opponentNameLabel;
	@UiField
	Button aiIsNorthButton;
	@UiField
	Button aiIsSouthButton;
	/**
	 * The pager used to change the range of data.
	 */
	@UiField
	ShowMorePagerPanel contactsPanel;

	/**
	 * The CellList.
	 */
	private CellList<ContactInfo> cellList;

	@UiField
	Label startGameLabel;

	@UiField
	Button startGameButton;

	@UiField
	Button cancelStartGameButton;

	// /**
	// * The pager used to display the current range.
	// */
	// @UiField
	// RangeLabelPager rangeLabelPager;

	/**
	 * Initializes the Graphics
	 */
	public Graphics(FBCore fbCore, String userToken, String userEmail, String nickName, String playerRating, String playerRD) {
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
		for (int row = 0; row < 2; row++) {
			for (int col = 0; col < 6; col++) {
				AbsolutePanel pitPanel = new AbsolutePanel();
				pitPanel.setSize(PIT_WIDTH + "px", PIT_HEIGHT + "px");

				addSeeds(pitPanel, 0);

				if (row == 0) {
					handlerRegs[row][col] = pitPanel.addDomHandler(new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							// presenter.makeMove(colB);
						}
					}, ClickEvent.getType());

					gameGrid.setWidget(row, 5 - col, pitPanel);
					// setPitEnabled(PlayerColor.NORTH, col, false);
				}
				else {
					handlerRegs[row][col] = pitPanel.addDomHandler(new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							// presenter.makeMove(colB);
						}
					}, ClickEvent.getType());

					gameGrid.setWidget(row, col, pitPanel);
					// setPitEnabled(PlayerColor.SOUTH, col, true);
				}

			}
		}

		gameGrid.setCellPadding(20);

		gameAbsolutePanel.add(treasureGridN);
		gameAbsolutePanel.add(gameGrid, TREASURE_CHEST_WIDTH + 2 * PADDING, 0);
		gameAbsolutePanel.add(treasureGridS, TREASURE_CHEST_WIDTH + 14 * PADDING + PIT_WIDTH * 6 + 6 * 2, 0);
		gameAbsolutePanel.setSize(12 + TREASURE_CHEST_WIDTH * 2 + 16 * PADDING + PIT_WIDTH * 6 + "px", 4 + 4 * PADDING + 2
				* PIT_HEIGHT + "px");
		gameAbsolutePanel.getElement().getStyle().setProperty("margin", "auto");

		DOM.setStyleAttribute(gameAbsolutePanel.getElement(), "backgroundColor", "#b0c4de");
		Image bgImg = new Image();
		bgImg.setResource(gameImages.board());

		DOM.setStyleAttribute(gameAbsolutePanel.getElement(), "backgroundImage", "url(" + bgImg.getUrl() + ")");

		// Create a CellList.
		ContactCell contactCell = new ContactCell();

		// Set a key provider that provides a unique key for each contact. If key is used to identify contacts when fields (such as
		// the name and address) change.
		cellList = new CellList<ContactInfo>(contactCell, ContactInfo.KEY_PROVIDER);

		cellList.setPageSize(30);
		cellList.setKeyboardPagingPolicy(KeyboardPagingPolicy.INCREASE_RANGE);
		cellList.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.BOUND_TO_SELECTION);

		// Add a selection model so we can select cells.
		final SingleSelectionModel<ContactInfo> selectionModel = new SingleSelectionModel<ContactInfo>(ContactInfo.KEY_PROVIDER);
		cellList.setSelectionModel(selectionModel);
		selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
			public void onSelectionChange(SelectionChangeEvent event) {
				showPlayGameElements(selectionModel.getSelectedObject());
			}
		});

		// Set the cellList as the display of the pagers. This example has two pagers. pagerPanel is a scrollable pager that extends
		// the range when the user scrolls to the bottom. rangeLabelPager is a pager that displays the current range, but does not
		// have any controls to change the range.
		contactsPanel.setDisplay(cellList);
		// rangeLabelPager.setDisplay(cellList);

		presenter = new Presenter(this, fbCore, userEmail.toLowerCase(), userToken);

		turnLabel.setHorizontalAlignment(Label.ALIGN_CENTER);
		warnLabel.setHorizontalAlignment(Label.ALIGN_CENTER);
		aiMovesLabel.setHorizontalAlignment(Label.ALIGN_CENTER);
		hideStartGameElements();

		initializeAudios();
		initializeHandlers();

		// presenter.setState(Presenter.deserializeState(stateStr));

		setUserNameLabelText("Name: " + nickName + " (" + playerRating + "|" + playerRD + ")");
		initializeUILanguage();

	}

	private void showPlayGameElements(ContactInfo contact) {
		startGameLabel.setVisible(true);
		startGameButton.setVisible(true);
		cancelStartGameButton.setVisible(true);
		if (contact.getTurn().equals(""))
			startGameLabel.setText(messages.startGameQuestion(contact.getName()));
		else
			startGameLabel.setText(messages.continueGameQuestion(contact.getName()));
	}

	private void hideStartGameElements() {
		startGameLabel.setVisible(false);
		startGameButton.setVisible(false);
		cancelStartGameButton.setVisible(false);
	}

	private void initializeUILanguage() {
		aiIsNorthButton.setText(messages.GameAiSouth());
		aiIsSouthButton.setText(messages.GameAiNorth());
		startGameButton.setText(messages.play());
		cancelStartGameButton.setText(messages.cancel());
	}

	private void initializeHandlers() {
		cancelStartGameButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				hideStartGameElements();
			}
		});

		startGameButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				hideStartGameElements();
				presenter.startGame(((SingleSelectionModel<ContactInfo>) cellList.getSelectionModel()).getSelectedObject());
			}
		});

		aiIsSouthButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.startAiMatchAsNorth();
			}
		});

		aiIsNorthButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.startAiMatchAsSouth();
			}
		});

	}

	private void initializeAudios() {
		if (Audio.isSupported()) {
			dotSound = Audio.createIfSupported();
			dotSound.addSource(gameSounds.dotMp3().getSafeUri().asString(), AudioElement.TYPE_MP3);
			dotSound.addSource(gameSounds.dotWav().getSafeUri().asString(), AudioElement.TYPE_WAV);

			gameOverSound = Audio.createIfSupported();
			gameOverSound.addSource(gameSounds.gameOverMp3().getSafeUri().asString(), AudioElement.TYPE_MP3);
			gameOverSound.addSource(gameSounds.gameOverWav().getSafeUri().asString(), AudioElement.TYPE_WAV);

			oppositeCaptureSound = Audio.createIfSupported();
			oppositeCaptureSound.addSource(gameSounds.oppositeCaptureMp3().getSafeUri().asString(), AudioElement.TYPE_MP3);
			oppositeCaptureSound.addSource(gameSounds.oppositeCaptureWav().getSafeUri().asString(), AudioElement.TYPE_WAV);
		}
	}

	/**
	 * This calculates the x and the y value where in a pit a seed should be placed
	 * 
	 * @param index
	 *          in which place should the seed be placed
	 * @return an int[] array - [0] will contain x value, [1] will contain y value
	 */
	int[] getTargetPoint(int index) {
		int[] point = new int[2];

		// the middle is supposed to be left out
		if (index > 3) {
			index++;
		}

		if (index < 9) {

			// left
			point[0] = (index % 3) * 22;
			// top
			point[1] = (index / 3) * 22;
		}
		else {
			// if more than 8 seeds are in a pit it's only indicated by the
			// number
			// left
			point[0] = (8 % 3) * 22;
			// top
			point[1] = (8 / 3) * 22;
		}

		return point;
	}

	/**
	 * The treasure chests have their own method to calculate the x and y values
	 * 
	 * @param index
	 *          in which place should the seed be placed
	 * @return an int[] array - [0] will contain x value, [1] will contain y value
	 */
	int[] getTargetPointTreasureChest(int index) {
		int[] point = new int[2];

		// the middle is supposed to be left out
		if (index > 9) {
			index++;
		}

		if (index < 21) {
			// left
			point[0] = (index % 3) * 22;
			// top
			point[1] = (index / 3) * 22;
		}
		else {
			// left
			point[0] = (20 % 3) * 22;
			// top
			point[1] = (20 / 3) * 22;
		}

		return point;
	}

	private int[] getCirclePoint(int index) {
		int points = 7;
		int radius = 25;
		double slice = 2 * Math.PI / points;

		double angle = slice * index;
		double newX = 25 + radius * Math.cos(angle);
		double newY = 20 + radius * Math.sin(angle);

		return new int[] { (int) newX, (int) newY };

	}

	/**
	 * Add the seed images to a pit absolutePanel
	 * 
	 * @param pitPanel
	 *          where the seeds are being placed in
	 * @param seedAmount
	 *          how many seeds are being placed into the pit
	 */
	private void addSeeds(AbsolutePanel pitPanel, int seedAmount) {
		pitPanel.clear();

		for (int i = 0; i < seedAmount; i++) {
			final Image seed = new Image();
			seed.setResource(gameImages.redSeed());
			DOM.setStyleAttribute(seed.getElement(), "backgroundSize", "20px 20px");

			int[] point = getTargetPoint(i);
			pitPanel.add(seed, point[0], point[1]);
		}
		Label countLabel = new Label(seedAmount + "");
		countLabel.setWidth(PIT_WIDTH + "px");
		countLabel.setHorizontalAlignment(Label.ALIGN_CENTER);
		pitPanel.add(countLabel, 0, 22);

	}

	/**
	 * The treasureChests have their own method to place seeds in
	 * 
	 * @param treasurePanel
	 *          where the seeds are being placed in
	 * @param seedAmount
	 *          how many seeds are being placed into the pit
	 */
	private void addSeedsToTreasureChest(AbsolutePanel treasurePanel, int seedAmount) {
		treasurePanel.clear();

		for (int i = 0; i < seedAmount; i++) {
			final Image seed = new Image();
			seed.setResource(gameImages.redSeed());
			DOM.setStyleAttribute(seed.getElement(), "backgroundSize", "20px 20px");

			int[] point = getTargetPointTreasureChest(i);
			treasurePanel.add(seed, point[0], point[1]);
		}
		Label countLabel = new Label(seedAmount + "");
		countLabel.setWidth(TREASURE_CHEST_WIDTH + "px");
		countLabel.setHorizontalAlignment(Label.ALIGN_CENTER);
		treasurePanel.add(countLabel, 0, 22 * 3);
	}

	/**
	 * The seedAmount will be placed on the right side at the correct index
	 */
	@Override
	public void setSeeds(PlayerColor side, int col, int seedAmount) {
		if (col < 6) {
			AbsolutePanel pnl;
			if (side.isNorth()) {
				pnl = (AbsolutePanel) gameGrid.getWidget(0, State.getMirrorIndex(col, 5));
			}
			else if (side.isSouth()) {
				pnl = (AbsolutePanel) gameGrid.getWidget(1, col);
			}
			else
				throw new IllegalMoveException();

			addSeeds(pnl, seedAmount);
		}
		else if (col == 6) {
			AbsolutePanel pnl;
			if (side.isNorth()) {
				pnl = (AbsolutePanel) treasureGridN.getWidget(0, 0);
			}
			else if (side.isSouth()) {
				pnl = (AbsolutePanel) treasureGridS.getWidget(0, 0);
			}
			else
				throw new IllegalMoveException();
			addSeedsToTreasureChest(pnl, seedAmount);
		}
		else
			throw new IllegalMoveException();

	}

	/**
	 * To animate a seed from one pit to another
	 */
	@Override
	public void animateFromPitToPit(PlayerColor startSide, int startCol, PlayerColor endSide, int endCol, double delay,
			boolean finalAnimation) {
		int startRow = startSide.isNorth() ? 0 : 1;
		int actualStartCol = (startRow == 0) ? 5 - startCol : startCol;
		int endRow = endSide.isNorth() ? 0 : 1;
		int actualEndCol = (endRow == 0) ? 5 - endCol : endCol;

		AbsolutePanel startPanel = (AbsolutePanel) gameGrid.getWidget(startRow, actualStartCol);
		final Image seed;
		seed = (Image) startPanel.getWidget(startPanel.getWidgetCount() - 2);
		int startXStartPanel = startPanel.getWidgetLeft(seed);
		int startYStartPanel = startPanel.getWidgetTop(seed);
		int startX = 2 + actualStartCol * 2 + TREASURE_CHEST_WIDTH + PIT_WIDTH * actualStartCol + PADDING * (actualStartCol * 2 + 3)
				+ startXStartPanel;
		int startY = 2 + PADDING + startRow * (PIT_HEIGHT + 2 * PADDING + 2) + startYStartPanel;

		AbsolutePanel endPanel;
		int[] endPointEndPanel;
		int endXEndPanel;
		int endYEndPanel;
		int endX;
		int endY;
		if (endCol < 6) {
			endPanel = (AbsolutePanel) gameGrid.getWidget(endRow, actualEndCol);
			endPointEndPanel = getTargetPoint(endPanel.getWidgetCount() - 1);
			endXEndPanel = endPointEndPanel[0];
			endYEndPanel = endPointEndPanel[1];
			endX = 2 + actualEndCol * 2 + TREASURE_CHEST_WIDTH + PIT_WIDTH * actualEndCol + PADDING * (actualEndCol * 2 + 3)
					+ endXEndPanel;
			endY = 2 + PADDING + endRow * (PIT_HEIGHT + 2 * PADDING + 2) + endYEndPanel;
		}
		else {
			Grid hGrid = endRow == 0 ? treasureGridN : treasureGridS;
			endPanel = (AbsolutePanel) hGrid.getWidget(0, 0);
			endPointEndPanel = getTargetPointTreasureChest(endPanel.getWidgetCount() - 1);
			endXEndPanel = endPointEndPanel[0];
			endYEndPanel = endPointEndPanel[1];
			endX = 2 + PADDING + (TREASURE_CHEST_WIDTH + PIT_WIDTH * 6 + PADDING * 14 + 6 * 2) * endRow + endXEndPanel;
			endY = 2 + PADDING + endYEndPanel;
		}

		animation = new SeedMovingAnimation(seed, gameImages.redSeed(), startPanel, endPanel, startXStartPanel, startYStartPanel,
				endXEndPanel, endYEndPanel, startX, startY, endX, endY, finalAnimation, this, dotSound);
		animation.run(1000, Duration.currentTimeMillis() + delay);
	}

	/**
	 * A player can only select certain pits for their move. That's why some have to be enabled and some have to be disabled before
	 * a player's turn.
	 */
	@Override
	public void setPitEnabled(PlayerColor side, int col, boolean enabled) {
		AbsolutePanel pnl;
		if (col < 6) {
			if (side.isNorth()) {
				pnl = (AbsolutePanel) gameGrid.getWidget(0, State.getMirrorIndex(col, 5));
			}
			else if (side.isSouth()) {
				pnl = (AbsolutePanel) gameGrid.getWidget(1, col);
			}
			else
				throw new IllegalMoveException();

			final int colB = col;
			int row = side.isNorth() ? 0 : 1;

			if (enabled) {
				// the handerRegs keeps track of all click handlers of the pits
				// by removing it from this array the handler is removed from
				// the pit (absolutePanel) as well
				handlerRegs[row][col].removeHandler();
				handlerRegs[row][col] = pnl.addDomHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						presenter.makeMove(colB);
					}
				}, ClickEvent.getType());
			}
			else {
				handlerRegs[row][col].removeHandler();
				handlerRegs[row][col] = pnl.addDomHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						setWarnLabelText(messages.warnMessage());
					}
				}, ClickEvent.getType());
			}

		}
	}

	@Override
	public void setWarnLabelText(String text) {
		warnLabel.setText(text);
		warnLabel.getElement().getStyle().setOpacity(1);
		fadeAnimation = new FadeAnimation(warnLabel.getElement());
		fadeAnimation.fade(6000, 0, 3000);
	}

	/**
	 * Informs the user of certain events.
	 */
	@Override
	public void setMessage(String labelMsg) {
		turnLabel.setText(labelMsg);
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

	/**
	 * Every time the last seed is placed in a pit update the game board This is a safety precaution that the UI state does not
	 * solely rely on the correct animation, so the model is questioned again for the correct amount of seeds everywhere
	 * 
	 */
	public void afterAnimation() {
		presenter.afterAnimation();
	}

	/**
	 * make the user input a little bit more secure found this here:
	 * http://www.coderanch.com/t/361152/Servlets/java/Sanitization-routines-HTML-input
	 */
	static String sanitize(String dirtyString) {
		if (null == dirtyString)
			return null;

		String tmp = dirtyString;
		// clean up line format chars
		tmp = tmp.replaceAll("\n", " ").replace("\r", " ").replace("\t", " ");
		// remove SGML markup
		tmp = tmp.replaceAll("<[^>]*>", " ");
		// remove any remaining metacharacters &;`'\|"*?~<>^()[]{}$ and null (00h)
		tmp = tmp.replaceAll("[\\&;`'\\\\\\|\"*?~<>^\\(\\)\\[\\]\\{\\}\\$\\x00]", "");

		// clean up whitespace
		tmp = tmp.replaceAll("\\s+", " ").trim();
		return tmp;
	}

	@Override
	public void windowAlert(String alertMessage) {
		Window.alert(alertMessage);
	}

	public void afterAIAdditionalMoveAnimation() {
		presenter.makeAiMove();
	}

	@Override
	public void setUserNameLabelText(String text) {
		userNameLabel.setText(text);
	}

	@Override
	public void setOpponentNameLabelText(String text) {
		opponentNameLabel.setText(text);
	}

	@Override
	public void setStartDateLabelText(String text) {
		startDateLabel.setText(text);
	}

	@Override
	public void setTurnLabelText(String text) {
		turnLabel.setText(text);
	}

	@Override
	public void setSideLabelText(String text) {
		sideLabel.setText(text);
	}

	@Override
	public void setAiMovesLabelTextTriggerAiMove(String text, Graphics graphicsForAnim) {
		aiMovesLabel.setText(messages.aiMakesMove());
		aiMovesLabel.getElement().getStyle().setOpacity(1);
		aiAdditionalMoveAnimation = new AIAdditionalMoveAnimation(aiMovesLabel.getElement(), graphicsForAnim);
		aiAdditionalMoveAnimation.fade(2000, 0, 1000);
	}

	@Override
	public void setContactsInList(List<ContactInfo> contacts) {
		// List<ContactInfo> contacts = Lists.newArrayList();
		// contacts.add(new ContactInfo("100001739510188", "Nadine Mueller",
		// "http://profile.ak.fbcdn.net/hprofile-ak-ash1/273602_100001739510188_1126790243_q.jpg"));
		// contacts.add(new ContactInfo("100001739510189", "Micha-Jamie Guthmann",
		// "http://profile.ak.fbcdn.net/hprofile-ak-ash1/273602_100001739510188_1126790243_q.jpg"));
		cellList.setRowData(contacts);
	}

	@Override
	public void clearMatchDisplay() {
		List<ContactInfo> hList = Lists.newArrayList();
		cellList.setRowData(hList);
	}

}
