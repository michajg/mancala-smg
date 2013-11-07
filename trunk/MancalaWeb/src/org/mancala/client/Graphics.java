package org.mancala.client;

import java.util.Date;

import org.mancala.client.View;
import org.mancala.client.animation.AIAdditionalMoveAnimation;
import org.mancala.client.animation.FadeAnimation;
import org.mancala.client.animation.SeedMovingAnimation;
import org.mancala.client.audio.GameSounds;
import org.mancala.client.i18n.MancalaMessages;
import org.mancala.client.img.GameImages;
import org.mancala.client.services.MancalaService;
import org.mancala.client.services.MancalaServiceAsync;
import org.mancala.shared.MatchInfo;
import org.mancala.shared.PlayerColor;
import org.mancala.shared.State;
import org.mancala.shared.exception.IllegalMoveException;

import com.google.gwt.appengine.channel.client.Channel;
import com.google.gwt.appengine.channel.client.ChannelError;
import com.google.gwt.appengine.channel.client.ChannelFactoryImpl;
import com.google.gwt.appengine.channel.client.SocketListener;
import com.google.gwt.core.client.Duration;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AudioElement;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.media.client.Audio;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.HasRpcToken;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.rpc.XsrfToken;
import com.google.gwt.user.client.rpc.XsrfTokenService;
import com.google.gwt.user.client.rpc.XsrfTokenServiceAsync;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

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
	private static MancalaServiceAsync mancalaService = GWT.create(MancalaService.class);

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

	private Long matchId;
	private String userId;
	private String opponentId;
	private boolean aiMatch;

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
	 * displays email address of player
	 */
	@UiField
	Label emailLabel;

	/**
	 * displays the start date of the Match
	 */
	@UiField
	Label startDateLabel;

	/**
	 * To inform the user of certain events a PopupPabel will be used
	 */
	@UiField
	PopupPanel gamePopupPanel;

	@UiField
	Label opponentNameLabel;
	@UiField
	Label matchIdLabel;
	@UiField
	Button aiIsNorthButton;
	@UiField
	Button aiIsSouthButton;
	@UiField
	Button automatchButton;
	@UiField
	TextBox emailBox;
	@UiField
	Button playButton;
	@UiField
	ListBox matchList;
	@UiField
	Button deleteButton;

	/**
	 * Initializes the Graphics
	 */
	public Graphics(String userToken, String userEmail, String nickName, String playerRating, String playerRD) {
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

		presenter = new Presenter(this);

		turnLabel.setHorizontalAlignment(Label.ALIGN_CENTER);
		warnLabel.setHorizontalAlignment(Label.ALIGN_CENTER);
		aiMovesLabel.setHorizontalAlignment(Label.ALIGN_CENTER);

		initializeAudios();
		initializeHandlers();
		createChannel(userToken);

		// presenter.setState(Presenter.deserializeState(stateStr));

		userId = userEmail.toLowerCase();

		setUserName("Nickname: " + nickName + " (" + playerRating + "|" + playerRD + ")");
		setEmail("eMail: " + userEmail);
		initializeUILanguage();

	}

	private void initializeUILanguage() {
		matchList.setItemText(0, messages.selectMatch());
		automatchButton.setText(messages.randomNewGame());
		deleteButton.setText(messages.deleteGame());
		playButton.setText(messages.play());
		emailBox.setText(messages.opponentsEmail());
		aiIsNorthButton.setText(messages.GameAiSouth());
		aiIsSouthButton.setText(messages.GameAiNorth());
	}

	private void createChannel(String userToken) {
		Channel channel = new ChannelFactoryImpl().createChannel(userToken);
		channel.open(new SocketListener() {
			@Override
			public void onOpen() {
				// secured against xsrf attacks
				// see http://www.gwtproject.org/doc/latest/DevGuideSecurityRpcXsrf.html
				XsrfTokenServiceAsync xsrf = (XsrfTokenServiceAsync) GWT.create(XsrfTokenService.class);
				((ServiceDefTarget) xsrf).setServiceEntryPoint(GWT.getModuleBaseURL() + "gwt/xsrf");
				xsrf.getNewXsrfToken(new AsyncCallback<XsrfToken>() {
					public void onSuccess(XsrfToken token) {
						((HasRpcToken) mancalaService).setRpcToken(token);
						mancalaService.loadMatches(loadMatchesCallback);
					}

					public void onFailure(Throwable caught) {
						Window.alert("Error retrieving xsrf token! Please try again later.");
					}
				});

				presenter.clearBoard();
			}

			@Override
			public void onMessage(String message) {
				if (message.equals("") || message == null)
					return;

				MatchInfo mI = MatchInfo.deserialize(message);
				updateMatchList();

				if (mI.getAction().equals("newgame")) {
					setWarnLabelText(messages.newGameAdded());

					if (matchId == null) {

						matchId = Long.valueOf(mI.getMatchId());
						if (mI.getNorthPlayerId().equals(userId)) {
							opponentNameLabel.setText(messages.opponent() + mI.getSouthPlayerName() + " (" + mI.getSouthPlayerRating() + "|"
									+ mI.getSouthPlayerRD() + ")");
							opponentId = mI.getSouthPlayerId().toLowerCase();
						}
						else {
							opponentNameLabel.setText(messages.opponent() + mI.getNorthPlayerName() + " (" + mI.getNorthPlayerRating() + "|"
									+ mI.getNorthPlayerRD() + ")");
							opponentId = mI.getNorthPlayerId().toLowerCase();
						}

						matchIdLabel.setText("MatchID: " + mI.getMatchId());
						startDateLabel.setText("Start: " + getCustomLocalDate(Long.valueOf(mI.getStartDate())));

						if (mI.getUserIdOfWhoseTurnItIs().equals(userId)) {
							turnLabel.setText(messages.itsYourTurn());
							presenter.setUsersSide(PlayerColor.S);
							sideLabel.setText(messages.playOnSouthSide());
						}
						else {
							turnLabel.setText(messages.opponentsTurn());
							presenter.setUsersSide(PlayerColor.N);
							sideLabel.setText(messages.playOnNorthSide());
						}

						presenter.setState(new State());
					}
				}
				else if (mI.getAction().equals("move")) {

					if (matchId.equals(Long.valueOf(mI.getMatchId()))) {
						matchIdLabel.setText("MatchID: " + mI.getMatchId());
						startDateLabel.setText("Start: " + getCustomLocalDate(Long.valueOf(mI.getStartDate())));

						if (mI.getUserIdOfWhoseTurnItIs().equals(userId))
							turnLabel.setText(messages.itsYourTurn());
						else
							turnLabel.setText(messages.opponentsTurn());

						State currentState = State.deserialize(mI.getState());

						// replaced by animation
						// presenter.setState(currentState);
						presenter.setStateToSetAfterAnimation(currentState);
						presenter.makeAnimatedMove(Integer.parseInt(mI.getMoveIndex()), presenter.getState().copyState());

						if (currentState.isGameOver()) {
							if (mI.getNorthPlayerId().equals(userId)) {
								userNameLabel.setText("Nickname: " + mI.getNorthPlayerName() + " (" + mI.getNorthPlayerRating() + "|"
										+ mI.getNorthPlayerRD() + ")");
								opponentNameLabel.setText(messages.opponent() + mI.getSouthPlayerName() + " (" + mI.getSouthPlayerRating() + "|"
										+ mI.getSouthPlayerRD() + ")");
							}
							else {
								userNameLabel.setText("Nickname: " + mI.getSouthPlayerName() + " (" + mI.getSouthPlayerRating() + "|"
										+ mI.getSouthPlayerRD() + ")");
								opponentNameLabel.setText(messages.opponent() + mI.getNorthPlayerName() + " (" + mI.getNorthPlayerRating() + "|"
										+ mI.getNorthPlayerRD() + ")");
							}
						}

					}
					else {
						if (mI.getNorthPlayerId().equals(userId))
							setWarnLabelText(messages.opponentMadeMove(mI.getSouthPlayerName(), mI.getMatchId()));
						else
							setWarnLabelText(messages.opponentMadeMove(mI.getNorthPlayerName(), mI.getMatchId()));

					}

				}

			}

			@Override
			public void onError(ChannelError error) {
				Window.alert("Channel error: " + error.getCode() + " : " + error.getDescription());
			}

			@Override
			public void onClose() {
				Window.alert("Channel closed!");
			}
		});
	}

	private void initializeHandlers() {
		aiIsSouthButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {

				aiMatch = true;
				// secured against xsrf attacks
				// see http://www.gwtproject.org/doc/latest/DevGuideSecurityRpcXsrf.html
				XsrfTokenServiceAsync xsrf = (XsrfTokenServiceAsync) GWT.create(XsrfTokenService.class);
				((ServiceDefTarget) xsrf).setServiceEntryPoint(GWT.getModuleBaseURL() + "gwt/xsrf");
				xsrf.getNewXsrfToken(new AsyncCallback<XsrfToken>() {
					public void onSuccess(XsrfToken token) {
						((HasRpcToken) mancalaService).setRpcToken(token);

						mancalaService.registerAiMatch(false, new AsyncCallback<String>() {
							@Override
							public void onFailure(Throwable caught) {
								Window.alert(messages.serverError());
							}

							@Override
							public void onSuccess(String result) {
								String[] tokens = result.split("#");

								turnLabel.setText(messages.opponentsTurn());
								opponentNameLabel.setText(messages.opponent() + "AI");
								matchId = Long.valueOf(tokens[1]);
								matchIdLabel.setText(tokens[1]);
								startDateLabel.setText(getCustomLocalDate(Long.valueOf(tokens[3])));
								sideLabel.setText(messages.playOnNorthSide());

								presenter.setUsersSide(PlayerColor.N);
								presenter.setState(new State());
								presenter.disableBoard();
								presenter.makeAiMove();
							}
						});
					}

					public void onFailure(Throwable caught) {
						Window.alert("Error retrieving xsrf token! Please try again later.");
					}
				});
			}
		});

		aiIsNorthButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				aiMatch = true;

				// secured against xsrf attacks
				// see http://www.gwtproject.org/doc/latest/DevGuideSecurityRpcXsrf.html
				XsrfTokenServiceAsync xsrf = (XsrfTokenServiceAsync) GWT.create(XsrfTokenService.class);
				((ServiceDefTarget) xsrf).setServiceEntryPoint(GWT.getModuleBaseURL() + "gwt/xsrf");
				xsrf.getNewXsrfToken(new AsyncCallback<XsrfToken>() {
					public void onSuccess(XsrfToken token) {
						((HasRpcToken) mancalaService).setRpcToken(token);
						mancalaService.registerAiMatch(true, new AsyncCallback<String>() {
							@Override
							public void onFailure(Throwable caught) {
								Window.alert(messages.serverError());
							}

							@Override
							public void onSuccess(String result) {
								String[] tokens = result.split("#");
								updateMatchList();

								turnLabel.setText(messages.itsYourTurn());
								opponentNameLabel.setText(messages.opponent() + "AI");
								matchId = Long.valueOf(tokens[1]);
								matchIdLabel.setText(tokens[1]);
								startDateLabel.setText(getCustomLocalDate(Long.valueOf(tokens[3])));
								sideLabel.setText(messages.playOnSouthSide());

								presenter.setUsersSide(PlayerColor.S);
								presenter.setState(new State());
							}
						});
					}

					public void onFailure(Throwable caught) {
						Window.alert("Error retrieving xsrf token! Please try again later.");
					}
				});
			}
		});

		automatchButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				aiMatch = false;

				turnLabel.setText(messages.waitForOpponent());
				opponentNameLabel.setText("");
				matchId = null;
				matchIdLabel.setText("");
				startDateLabel.setText("");
				presenter.clearBoard();
				// secured against xsrf attacks
				// see http://www.gwtproject.org/doc/latest/DevGuideSecurityRpcXsrf.html
				XsrfTokenServiceAsync xsrf = (XsrfTokenServiceAsync) GWT.create(XsrfTokenService.class);
				((ServiceDefTarget) xsrf).setServiceEntryPoint(GWT.getModuleBaseURL() + "gwt/xsrf");
				xsrf.getNewXsrfToken(new AsyncCallback<XsrfToken>() {
					public void onSuccess(XsrfToken token) {
						((HasRpcToken) mancalaService).setRpcToken(token);
						mancalaService.automatch(new AsyncCallback<Void>() {
							@Override
							public void onFailure(Throwable caught) {
								// Do nothing
							}

							@Override
							public void onSuccess(Void result) {
								// Do nothing
							}
						});
					}

					public void onFailure(Throwable caught) {
						Window.alert("Error retrieving xsrf token! Please try again later.");
					}
				});

			}
		});

		playButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				String emailAdress = Graphics.sanitize(emailBox.getText());
				if (!emailAdress.matches("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$")) {
					Window.alert(messages.invalidEmail());
					return;
				}
				aiMatch = false;

				// secured against xsrf attacks
				// see http://www.gwtproject.org/doc/latest/DevGuideSecurityRpcXsrf.html
				XsrfTokenServiceAsync xsrf = (XsrfTokenServiceAsync) GWT.create(XsrfTokenService.class);
				((ServiceDefTarget) xsrf).setServiceEntryPoint(GWT.getModuleBaseURL() + "gwt/xsrf");
				xsrf.getNewXsrfToken(new AsyncCallback<XsrfToken>() {
					public void onSuccess(XsrfToken token) {
						((HasRpcToken) mancalaService).setRpcToken(token);
						mancalaService.newEmailGame(emailBox.getText(), new AsyncCallback<Boolean>() {

							@Override
							public void onFailure(Throwable caught) {
								Window.alert(messages.serverError());
							}

							@Override
							public void onSuccess(Boolean result) {
								if (result == false) {
									Window.alert(messages.opponentNotRegistered(emailBox.getText()));
								}

							}

						});
					}

					public void onFailure(Throwable caught) {
						Window.alert("Error retrieving xsrf token! Please try again later.");
					}
				});

			}
		});

		matchList.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				aiMatch = false;

				final Long matchIDFromList = getSelectedMatch();
				// secured against xsrf attacks
				// see http://www.gwtproject.org/doc/latest/DevGuideSecurityRpcXsrf.html
				XsrfTokenServiceAsync xsrf = (XsrfTokenServiceAsync) GWT.create(XsrfTokenService.class);
				((ServiceDefTarget) xsrf).setServiceEntryPoint(GWT.getModuleBaseURL() + "gwt/xsrf");
				xsrf.getNewXsrfToken(new AsyncCallback<XsrfToken>() {
					public void onSuccess(XsrfToken token) {
						((HasRpcToken) mancalaService).setRpcToken(token);
						mancalaService.changeMatch(matchIDFromList, new AsyncCallback<String>() {
							@Override
							public void onFailure(Throwable caught) {
								Window.alert(messages.loadMatchError());
							}

							@Override
							public void onSuccess(String result) {
								if (!result.trim().equals("noMatch")) {

									MatchInfo mI = MatchInfo.deserialize(result);

									matchId = Long.valueOf(mI.getMatchId());
									State newMatchState = State.deserialize(mI.getState());

									String opponentName, opponentEmail, opponentRating, opponentRD;
									if (mI.getNorthPlayerId().equals(userId)) {
										opponentName = mI.getSouthPlayerName();
										opponentEmail = mI.getSouthPlayerId();
										opponentRating = mI.getSouthPlayerRating();
										opponentRD = mI.getSouthPlayerRD();
									}
									else {
										opponentName = mI.getNorthPlayerName();
										opponentEmail = mI.getNorthPlayerId();
										opponentRating = mI.getNorthPlayerRating();
										opponentRD = mI.getNorthPlayerRD();
									}

									if (opponentName.equals("AI")) {
										aiMatch = true;

										opponentNameLabel.setText("AI");
									}
									else {
										opponentNameLabel.setText(messages.opponent() + opponentName + " (" + opponentRating + "|" + opponentRD + ")");
										opponentId = opponentEmail.toLowerCase();
									}

									matchIdLabel.setText("MatchID: " + matchId);

									startDateLabel.setText("Start: " + getCustomLocalDate(Long.valueOf(mI.getStartDate())));
									PlayerColor usersSide;
									if (mI.getUserIdOfWhoseTurnItIs().equals(userId)) {
										turnLabel.setText(messages.itsYourTurn());
										usersSide = newMatchState.getWhoseTurn();
									}
									else {
										turnLabel.setText(messages.turnOfOpponent(opponentName));
										usersSide = newMatchState.getWhoseTurn().getOpposite();
									}
									presenter.setUsersSide(usersSide);
									if (usersSide.isNorth())
										sideLabel.setText(messages.playOnNorthSide());
									else
										sideLabel.setText(messages.playOnSouthSide());
									presenter.setState(newMatchState);

									if (mI.getUserIdOfWhoseTurnItIs().equals("AI")) // Can be caused due to un-updated AI turn
										presenter.makeAiMove();
								}
							}
						});
					}

					public void onFailure(Throwable caught) {
						Window.alert("Error retrieving xsrf token! Please try again later.");
					}
				});

			}
		});

		deleteButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				for (int i = 0; i < matchList.getItemCount(); i++) {
					if (matchList.getValue(i).equals(matchId)) {
						matchList.removeItem(i);
						break;
					}
				}
				// secured against xsrf attacks
				// see http://www.gwtproject.org/doc/latest/DevGuideSecurityRpcXsrf.html
				XsrfTokenServiceAsync xsrf = (XsrfTokenServiceAsync) GWT.create(XsrfTokenService.class);
				((ServiceDefTarget) xsrf).setServiceEntryPoint(GWT.getModuleBaseURL() + "gwt/xsrf");
				xsrf.getNewXsrfToken(new AsyncCallback<XsrfToken>() {
					public void onSuccess(XsrfToken token) {
						((HasRpcToken) mancalaService).setRpcToken(token);
						mancalaService.deleteMatch(matchId, new AsyncCallback<Void>() {
							@Override
							public void onFailure(Throwable caught) {
								Window.alert(messages.deleteMatchError());
							}

							@Override
							public void onSuccess(Void result) {
								// secured against xsrf attacks
								// see http://www.gwtproject.org/doc/latest/DevGuideSecurityRpcXsrf.html
								XsrfTokenServiceAsync xsrf = (XsrfTokenServiceAsync) GWT.create(XsrfTokenService.class);
								((ServiceDefTarget) xsrf).setServiceEntryPoint(GWT.getModuleBaseURL() + "gwt/xsrf");
								xsrf.getNewXsrfToken(new AsyncCallback<XsrfToken>() {
									public void onSuccess(XsrfToken token) {
										((HasRpcToken) mancalaService).setRpcToken(token);
										mancalaService.loadMatches(loadMatchesCallback);
									}

									public void onFailure(Throwable caught) {
										Window.alert("Error retrieving xsrf token! Please try again later.");
									}
								});
							}
						});
					}

					public void onFailure(Throwable caught) {
						Window.alert("Error retrieving xsrf token! Please try again later.");
					}
				});

				turnLabel.setText(messages.startNewGame());
				sideLabel.setText("");
				opponentNameLabel.setText("");
				matchId = null;
				matchIdLabel.setText("");
				startDateLabel.setText("");
				presenter.clearBoard();
			}
		});

	}

	private String getCustomLocalDate(Long lDate) {
		Date date = new Date(lDate);
		return DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_SHORT).format(date) + " "
				+ DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.TIME_SHORT).format(date);
	}

	AsyncCallback<String[]> loadMatchesCallback = new AsyncCallback<String[]>() {
		@Override
		public void onFailure(Throwable caught) {
			Window.alert(messages.loadMatchError());
		}

		@Override
		public void onSuccess(String[] result) {
			matchList.clear();
			matchList.addItem(messages.selectMatch(), "");

			for (String matchInfoString : result) {
				if (matchInfoString == null) {
					continue;
				}
				MatchInfo mI = MatchInfo.deserialize(matchInfoString);

				PlayerColor usersSide = mI.getNorthPlayerId().equals(userId) ? PlayerColor.N : PlayerColor.S;
				String turnText;
				if (mI.getUserIdOfWhoseTurnItIs().equals(userId))
					turnText = messages.yourTurn();
				else
					turnText = messages.theirTurn();

				State state = State.deserialize(mI.getState());
				if (state.isGameOver()) {
					if (state.winner() == null) {
						turnText = messages.tie();
					}
					else {
						if (state.winner().equals(usersSide))
							turnText = messages.youWon(state.score() + "", (48 - state.score()) + "");
						else
							turnText = messages.youLost((48 - state.score()) + "", state.score() + "");
					}
				}
				String opponent;
				if (mI.getNorthPlayerId().equals(userId))
					opponent = mI.getSouthPlayerId();
				else
					opponent = mI.getNorthPlayerId();

				matchList.addItem(messages.opponent() + opponent + " - " + turnText + " - MatchID: " + mI.getMatchId(), mI.getMatchId());
			}
		}
	};

	public Long getSelectedMatch() {
		if (matchList.getSelectedIndex() == 0)
			return null;
		return Long.valueOf(matchList.getValue(matchList.getSelectedIndex()));
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

	private void setWarnLabelText(String text) {
		warnLabel.setText(text);
		warnLabel.getElement().getStyle().setOpacity(1);
		fadeAnimation = new FadeAnimation(warnLabel.getElement());
		fadeAnimation.fade(6000, 0, 3000);
	}

	/**
	 * Informs the user of certain events. If the parameter for the buttons is null no button will be displayed. The first button
	 * makes the information disappear, the second starts a new game
	 */
	@Override
	public void setMessage(String labelMsg, String HideBtnText, String restartBtnText) {
		VerticalPanel vPanel = new VerticalPanel();
		Label lbl = new Label(labelMsg);
		lbl.setHorizontalAlignment(Label.ALIGN_CENTER);
		vPanel.add(lbl);
		if (HideBtnText != null) {
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
		if (restartBtnText != null) {
			Button playAgainButton = new Button(restartBtnText);
			playAgainButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					// secured against xsrf attacks
					// see http://www.gwtproject.org/doc/latest/DevGuideSecurityRpcXsrf.html
					XsrfTokenServiceAsync xsrf = (XsrfTokenServiceAsync) GWT.create(XsrfTokenService.class);
					((ServiceDefTarget) xsrf).setServiceEntryPoint(GWT.getModuleBaseURL() + "gwt/xsrf");
					xsrf.getNewXsrfToken(new AsyncCallback<XsrfToken>() {
						public void onSuccess(XsrfToken token) {
							((HasRpcToken) mancalaService).setRpcToken(token);
							// rematch can be treated like a new emailgame
							mancalaService.newEmailGame(opponentId, new AsyncCallback<Boolean>() {

								@Override
								public void onFailure(Throwable caught) {
									Window.alert(messages.serverError());
								}

								@Override
								public void onSuccess(Boolean result) {
									if (result == false) {
										Window.alert(messages.opponent404(opponentId));
									}
								}

							});
						}

						public void onFailure(Throwable caught) {
							Window.alert("Error retrieving xsrf token! Please try again later.");
						}
					});

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
		// gamePopupPanel.setPopupPositionAndShow(new
		// PopupPanel.PositionCallback() {
		// public void setPosition(int offsetWidth, int offsetHeight) {
		// int left = (Window.getClientWidth() - offsetWidth) / 3;
		// int top = (Window.getClientHeight() - offsetHeight) / 3;
		// gamePopupPanel.setPopupPosition(left, top);
		// }
		// });
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

	/**
	 * Every time the last seed is placed in a pit update the game board This is a safety precaution that the UI state does not
	 * solely rely on the correct animation, so the model is questioned again for the correct amount of seeds everywhere
	 * 
	 */
	public void afterAnimation() {
		presenter.afterAnimation();
	}

	@Override
	public void setUserName(String userName) {
		userNameLabel.setText(userName);
	}

	@Override
	public void setEmail(String email) {
		emailLabel.setText(email);
	}

	@Override
	public void sendMoveToServer(Integer chosenIndex, String stateString) {
		final Integer fChosenIndex = chosenIndex;
		final String fStateString = stateString;

		if (aiMatch) {
			sendMoveToServerAI(chosenIndex, State.deserialize(stateString));
			return;
		}

		// secured against xsrf attacks
		// see http://www.gwtproject.org/doc/latest/DevGuideSecurityRpcXsrf.html
		XsrfTokenServiceAsync xsrf = (XsrfTokenServiceAsync) GWT.create(XsrfTokenService.class);
		((ServiceDefTarget) xsrf).setServiceEntryPoint(GWT.getModuleBaseURL() + "gwt/xsrf");
		xsrf.getNewXsrfToken(new AsyncCallback<XsrfToken>() {
			public void onSuccess(XsrfToken token) {
				((HasRpcToken) mancalaService).setRpcToken(token);
				mancalaService.makeMove(matchId, fChosenIndex, fStateString, new AsyncCallback<Void>() {
					@Override
					public void onFailure(Throwable caught) {
						Window.alert("Server error!");
					}

					@Override
					public void onSuccess(Void result) {
						updateMatchList();
					}
				});
			}

			public void onFailure(Throwable caught) {
				Window.alert("Error retrieving xsrf token! Please try again later.");
			}
		});

	}

	@Override
	public boolean getAiMatch() {
		return aiMatch;
	}

	@Override
	public void sendMoveToServerAI(Integer chosenIndex, State state) {
		final Integer fChosenIndex = chosenIndex;
		final State fState = state;
		final Graphics graphicsForAnim = this;

		// secured against xsrf attacks
		// see http://www.gwtproject.org/doc/latest/DevGuideSecurityRpcXsrf.html
		XsrfTokenServiceAsync xsrf = (XsrfTokenServiceAsync) GWT.create(XsrfTokenService.class);
		((ServiceDefTarget) xsrf).setServiceEntryPoint(GWT.getModuleBaseURL() + "gwt/xsrf");
		xsrf.getNewXsrfToken(new AsyncCallback<XsrfToken>() {
			public void onSuccess(XsrfToken token) {
				((HasRpcToken) mancalaService).setRpcToken(token);
				mancalaService.saveAiMove(matchId, fChosenIndex + "", State.serialize(fState), new AsyncCallback<Void>() {
					@Override
					public void onFailure(Throwable caught) {
						Window.alert(messages.serverError());
					}

					@Override
					public void onSuccess(Void result) {
						if (fState.getWhoseTurn().equals(presenter.getUsersSide().getOpposite())) {
							aiMovesLabel.setText(messages.aiMakesMove());
							aiMovesLabel.getElement().getStyle().setOpacity(1);
							aiAdditionalMoveAnimation = new AIAdditionalMoveAnimation(aiMovesLabel.getElement(), graphicsForAnim);
							aiAdditionalMoveAnimation.fade(2000, 0, 1000);
						}

						updateMatchList();
					}
				});
			}

			public void onFailure(Throwable caught) {
				Window.alert("Error retrieving xsrf token! Please try again later.");
			}
		});
	}

	@Override
	public void updateMatchList() {
		// secured against xsrf attacks
		// see http://www.gwtproject.org/doc/latest/DevGuideSecurityRpcXsrf.html
		XsrfTokenServiceAsync xsrf = (XsrfTokenServiceAsync) GWT.create(XsrfTokenService.class);
		((ServiceDefTarget) xsrf).setServiceEntryPoint(GWT.getModuleBaseURL() + "gwt/xsrf");
		xsrf.getNewXsrfToken(new AsyncCallback<XsrfToken>() {
			public void onSuccess(XsrfToken token) {
				((HasRpcToken) mancalaService).setRpcToken(token);
				mancalaService.loadMatches(loadMatchesCallback);
			}

			public void onFailure(Throwable caught) {
				Window.alert("Error retrieving xsrf token! Please try again later.");
			}
		});

	}

	@Override
	public void setStatus(String status) {
		turnLabel.setText(status);
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

	// @Override
	// public void saveMoveInServer(Integer aiMove, State state) {
	// final Integer fAiMove = aiMove;
	// final State fState = state;
	// final Graphics graphicsForAnim = this;
	//
	// // secured against xsrf attacks
	// // see http://www.gwtproject.org/doc/latest/DevGuideSecurityRpcXsrf.html
	// XsrfTokenServiceAsync xsrf = (XsrfTokenServiceAsync) GWT.create(XsrfTokenService.class);
	// ((ServiceDefTarget) xsrf).setServiceEntryPoint(GWT.getModuleBaseURL() + "gwt/xsrf");
	// xsrf.getNewXsrfToken(new AsyncCallback<XsrfToken>() {
	// public void onSuccess(XsrfToken token) {
	// ((HasRpcToken) mancalaService).setRpcToken(token);
	//
	// mancalaService.saveAiMove(matchId, fAiMove + "", State.serialize(fState), new AsyncCallback<Void>() {
	// @Override
	// public void onFailure(Throwable caught) {
	// Window.alert(messages.serverError());
	// }
	//
	// @Override
	// public void onSuccess(Void result) {
	// updateMatchList();
	// if (fState.getWhoseTurn().equals(presenter.getUsersSide().getOpposite())) {
	// warnLabel.setText("AI makes another move");
	// warnLabel.getElement().getStyle().setOpacity(1);
	// aiAdditionalMoveAnimation = new AIAdditionalMoveAnimation(warnLabel.getElement(), graphicsForAnim);
	// aiAdditionalMoveAnimation.fade(6000, 0, 3000);
	// }
	//
	// }
	// });
	// }
	//
	// public void onFailure(Throwable caught) {
	// Window.alert("Error retrieving xsrf token! Please try again later.");
	// }
	// });
	// }

	public void afterAIAdditionalMoveAnimation() {
		presenter.makeAiMove();
	}

}
