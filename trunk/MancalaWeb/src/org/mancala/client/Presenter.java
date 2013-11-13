package org.mancala.client;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.mancala.client.gwtfb.sdk.FBCore;
import org.mancala.client.i18n.MancalaMessages;
import org.mancala.client.services.MancalaService;
import org.mancala.client.services.MancalaServiceAsync;
import org.mancala.shared.MatchInfo;
import org.mancala.shared.PlayerColor;
import org.mancala.shared.State;
import org.mancala.shared.ai.AlphaBetaPruning;
import org.mancala.shared.ai.DateTimer;
import org.mancala.shared.ai.Heuristic;
import org.mancala.shared.exception.GameOverException;
import org.mancala.shared.exception.IllegalMoveException;

import com.google.common.collect.Lists;
import com.google.gwt.appengine.channel.client.Channel;
import com.google.gwt.appengine.channel.client.ChannelError;
import com.google.gwt.appengine.channel.client.ChannelFactoryImpl;
import com.google.gwt.appengine.channel.client.SocketListener;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.HasRpcToken;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.rpc.XsrfToken;
import com.google.gwt.user.client.rpc.XsrfTokenService;
import com.google.gwt.user.client.rpc.XsrfTokenServiceAsync;

/**
 * The MVP-Presenter of the Mancala game
 * 
 * @author Micha Guthmann
 */
public class Presenter {

	private static MancalaMessages messages = GWT.create(MancalaMessages.class);
	private static MancalaServiceAsync mancalaService = GWT.create(MancalaService.class);

	/**
	 * The view of the MVP pattern the presenter will use
	 */
	final View graphics;

	/**
	 * The model of the MVP pattern the presenter will use
	 */
	State state;

	FBCore fbCore;

	/**
	 * keeps track of which side the player is. He is either South or North
	 */
	private PlayerColor usersSide;
	State stateToSetAfterAnimation;
	int lastMove;
	private Long matchId;
	private String userId;
	private String opponentId;
	private String opponentName;
	private boolean aiMatch;

	/**
	 * 1. Sets the view 2. Checks if there is already a state in the url fragment and if so initializes this 3. Initializes the
	 * History 4. Updates the gaming board accordingly
	 */
	public Presenter(View graphics, FBCore fbCore, String userId, String userToken) {
		this.graphics = graphics;
		state = new State();
		// updateBoard();
		this.userId = userId;
		createChannel(userToken);
		this.fbCore = fbCore;
	}

	/**
	 * @return the state
	 */
	public State getState() {
		return state;
	}

	/**
	 * @param stateToSetAfterAnimation
	 *          the stateToSetAfterAnimation to set
	 */
	public void setStateToSetAfterAnimation(State stateToSetAfterAnimation) {
		this.stateToSetAfterAnimation = stateToSetAfterAnimation;
	}

	void restartGame() {
		startGame(userId, opponentId, opponentName);
	}

	private void startGame(String sendPlayerId, String sendOpponentId, String sendOpponentName) {
		final String fSendPlayerId = sendPlayerId;
		final String fSendOpponentId = sendOpponentId;
		final String fSendOpponentName = sendOpponentName;

		// secured against xsrf attacks
		// see http://www.gwtproject.org/doc/latest/DevGuideSecurityRpcXsrf.html
		XsrfTokenServiceAsync xsrf = (XsrfTokenServiceAsync) GWT.create(XsrfTokenService.class);
		((ServiceDefTarget) xsrf).setServiceEntryPoint(GWT.getModuleBaseURL() + "gwt/xsrf");
		xsrf.getNewXsrfToken(new AsyncCallback<XsrfToken>() {
			public void onSuccess(XsrfToken token) {
				((HasRpcToken) mancalaService).setRpcToken(token);
				// rematch can be treated like a new emailgame
				mancalaService.startGame(fSendPlayerId, fSendOpponentId, fSendOpponentName, new AsyncCallback<Boolean>() {

					@Override
					public void onFailure(Throwable caught) {
						Window.alert(messages.serverError());
					}

					@Override
					public void onSuccess(Boolean result) {
						if (result == false) {
							Window.alert(messages.opponent404(fSendOpponentName));
						}
					}

				});
			}

			public void onFailure(Throwable caught) {
				Window.alert("Error retrieving xsrf token! Please try again later.");
			}
		});
	}

	private void createChannel(String userToken) {
		Channel channel = new ChannelFactoryImpl().createChannel(userToken);
		channel.open(new SocketListener() {
			@Override
			public void onOpen() {
				updateMatchList();

				clearBoard();
			}

			@Override
			public void onMessage(String message) {
				if (message.equals("") || message == null)
					return;

				MatchInfo mI = MatchInfo.deserialize(message);
				updateMatchList();

				if (mI.getAction().equals("newgame")) {
					graphics.setWarnLabelText(messages.newGameAdded());

					if (matchId == null) {

						matchId = Long.valueOf(mI.getMatchId());
						if (mI.getNorthPlayerId().equals(userId)) {
							graphics.setOpponentNameLabelText(messages.opponent() + mI.getSouthPlayerName() + " (" + mI.getSouthPlayerRating()
									+ "|" + mI.getSouthPlayerRD() + ")");
							opponentId = mI.getSouthPlayerId();
							opponentName = mI.getSouthPlayerName();
						}
						else {
							graphics.setOpponentNameLabelText(messages.opponent() + mI.getNorthPlayerName() + " (" + mI.getNorthPlayerRating()
									+ "|" + mI.getNorthPlayerRD() + ")");
							opponentId = mI.getNorthPlayerId();
							opponentName = mI.getNorthPlayerName();
						}

						graphics.setStartDateLabelText("Start: " + getCustomLocalDate(Long.valueOf(mI.getStartDate())));

						if (mI.getUserIdOfWhoseTurnItIs().equals(userId)) {
							graphics.setTurnLabelText(messages.itsYourTurn());
							setUsersSide(PlayerColor.S);
							graphics.setSideLabelText(messages.playOnSouthSide());
						}
						else {
							graphics.setTurnLabelText(messages.opponentsTurn());
							setUsersSide(PlayerColor.N);
							graphics.setSideLabelText(messages.playOnNorthSide());
						}

						setState(new State());
					}
				}
				else if (mI.getAction().equals("move")) {

					if (matchId.equals(Long.valueOf(mI.getMatchId()))) {
						graphics.setStartDateLabelText("Start: " + getCustomLocalDate(Long.valueOf(mI.getStartDate())));

						if (mI.getUserIdOfWhoseTurnItIs().equals(userId))
							graphics.setTurnLabelText(messages.itsYourTurn());
						else
							graphics.setTurnLabelText(messages.opponentsTurn());

						State currentState = State.deserialize(mI.getState());

						// replaced by animation
						// setState(currentState);
						setStateToSetAfterAnimation(currentState);
						makeAnimatedMove(Integer.parseInt(mI.getMoveIndex()), getState().copyState());

						if (currentState.isGameOver()) {
							deleteGame();
							if (mI.getNorthPlayerId().equals(userId)) {
								graphics.setUserNameLabelText("Name: " + mI.getNorthPlayerName() + " (" + mI.getNorthPlayerRating() + "|"
										+ mI.getNorthPlayerRD() + ")");
								graphics.setOpponentNameLabelText(messages.opponent() + mI.getSouthPlayerName() + " ("
										+ mI.getSouthPlayerRating() + "|" + mI.getSouthPlayerRD() + ")");
							}
							else {
								graphics.setUserNameLabelText("Name: " + mI.getSouthPlayerName() + " (" + mI.getSouthPlayerRating() + "|"
										+ mI.getSouthPlayerRD() + ")");
								graphics.setOpponentNameLabelText(messages.opponent() + mI.getNorthPlayerName() + " ("
										+ mI.getNorthPlayerRating() + "|" + mI.getNorthPlayerRD() + ")");
							}
						}

					}
					else {
						if (mI.getNorthPlayerId().equals(userId))
							graphics.setWarnLabelText(messages.opponentMadeMove(mI.getSouthPlayerName(), mI.getMatchId()));
						else
							graphics.setWarnLabelText(messages.opponentMadeMove(mI.getNorthPlayerName(), mI.getMatchId()));

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

	void startAiMatchAsNorth() {
		aiMatch = true;
		// secured against xsrf attacks
		// see http://www.gwtproject.org/doc/latest/DevGuideSecurityRpcXsrf.html
		XsrfTokenServiceAsync xsrf = (XsrfTokenServiceAsync) GWT.create(XsrfTokenService.class);
		((ServiceDefTarget) xsrf).setServiceEntryPoint(GWT.getModuleBaseURL() + "gwt/xsrf");
		xsrf.getNewXsrfToken(new AsyncCallback<XsrfToken>() {
			public void onSuccess(XsrfToken token) {
				((HasRpcToken) mancalaService).setRpcToken(token);

				mancalaService.registerAiMatch(userId, false, new AsyncCallback<String>() {
					@Override
					public void onFailure(Throwable caught) {
						Window.alert(messages.serverError());
					}

					@Override
					public void onSuccess(String result) {
						String[] tokens = result.split("#");

						graphics.setTurnLabelText(messages.opponentsTurn());
						graphics.setOpponentNameLabelText(messages.opponent() + "AI");
						matchId = Long.valueOf(tokens[1]);
						graphics.setStartDateLabelText(getCustomLocalDate(Long.valueOf(tokens[3])));
						graphics.setSideLabelText(messages.playOnNorthSide());

						setUsersSide(PlayerColor.N);
						setState(new State());
						disableBoard();
						makeAiMove();
					}
				});
			}

			public void onFailure(Throwable caught) {
				Window.alert("Error retrieving xsrf token! Please try again later.");
			}
		});
	}

	void startAiMatchAsSouth() {
		aiMatch = true;

		// secured against xsrf attacks
		// see http://www.gwtproject.org/doc/latest/DevGuideSecurityRpcXsrf.html
		XsrfTokenServiceAsync xsrf = (XsrfTokenServiceAsync) GWT.create(XsrfTokenService.class);
		((ServiceDefTarget) xsrf).setServiceEntryPoint(GWT.getModuleBaseURL() + "gwt/xsrf");
		xsrf.getNewXsrfToken(new AsyncCallback<XsrfToken>() {
			public void onSuccess(XsrfToken token) {
				((HasRpcToken) mancalaService).setRpcToken(token);
				mancalaService.registerAiMatch(userId, true, new AsyncCallback<String>() {
					@Override
					public void onFailure(Throwable caught) {
						Window.alert(messages.serverError());
					}

					@Override
					public void onSuccess(String result) {
						String[] tokens = result.split("#");
						updateMatchList();

						graphics.setTurnLabelText(messages.itsYourTurn());
						graphics.setOpponentNameLabelText(messages.opponent() + "AI");
						matchId = Long.valueOf(tokens[1]);
						graphics.setStartDateLabelText(getCustomLocalDate(Long.valueOf(tokens[3])));
						graphics.setSideLabelText(messages.playOnSouthSide());

						setUsersSide(PlayerColor.S);
						setState(new State());
					}
				});
			}

			public void onFailure(Throwable caught) {
				Window.alert("Error retrieving xsrf token! Please try again later.");
			}
		});
	}

	// void restartMatch(String id) {
	// aiMatch = false;
	//
	// final String fId = id;
	// // secured against xsrf attacks
	// // see http://www.gwtproject.org/doc/latest/DevGuideSecurityRpcXsrf.html
	// XsrfTokenServiceAsync xsrf = (XsrfTokenServiceAsync) GWT.create(XsrfTokenService.class);
	// ((ServiceDefTarget) xsrf).setServiceEntryPoint(GWT.getModuleBaseURL() + "gwt/xsrf");
	// xsrf.getNewXsrfToken(new AsyncCallback<XsrfToken>() {
	// public void onSuccess(XsrfToken token) {
	// ((HasRpcToken) mancalaService).setRpcToken(token);
	// mancalaService.startGame(userId, fId, opponentName, new AsyncCallback<Boolean>() {
	//
	// @Override
	// public void onFailure(Throwable caught) {
	// Window.alert(messages.serverError());
	// }
	//
	// @Override
	// public void onSuccess(Boolean result) {
	// if (result == false) {
	// Window.alert(messages.opponentNotRegistered(fId));
	// }
	//
	// }
	//
	// });
	// }
	//
	// public void onFailure(Throwable caught) {
	// Window.alert("Error retrieving xsrf token! Please try again later.");
	// }
	// });
	// }

	void changeGame(Long changeToThisMatchId) {
		aiMatch = false;

		final Long matchIDFromList = changeToThisMatchId;
		// secured against xsrf attacks
		// see http://www.gwtproject.org/doc/latest/DevGuideSecurityRpcXsrf.html
		XsrfTokenServiceAsync xsrf = (XsrfTokenServiceAsync) GWT.create(XsrfTokenService.class);
		((ServiceDefTarget) xsrf).setServiceEntryPoint(GWT.getModuleBaseURL() + "gwt/xsrf");
		xsrf.getNewXsrfToken(new AsyncCallback<XsrfToken>() {
			public void onSuccess(XsrfToken token) {
				((HasRpcToken) mancalaService).setRpcToken(token);
				mancalaService.changeMatch(userId, matchIDFromList, new AsyncCallback<String>() {
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

							String opponentNameFromServer, opponentIdFromServer, opponentRating, opponentRD;
							if (mI.getNorthPlayerId().equals(userId)) {
								opponentNameFromServer = mI.getSouthPlayerName();
								opponentIdFromServer = mI.getSouthPlayerId();
								opponentRating = mI.getSouthPlayerRating();
								opponentRD = mI.getSouthPlayerRD();
							}
							else {
								opponentNameFromServer = mI.getNorthPlayerName();
								opponentIdFromServer = mI.getNorthPlayerId();
								opponentRating = mI.getNorthPlayerRating();
								opponentRD = mI.getNorthPlayerRD();
							}

							if (opponentNameFromServer.equals("AI")) {
								aiMatch = true;

								graphics.setOpponentNameLabelText("AI");
							}
							else {
								graphics.setOpponentNameLabelText(messages.opponent() + opponentNameFromServer + " (" + opponentRating + "|"
										+ opponentRD + ")");
								opponentId = opponentIdFromServer;
								opponentName = opponentNameFromServer;

							}

							graphics.setStartDateLabelText("Start: " + getCustomLocalDate(Long.valueOf(mI.getStartDate())));
							PlayerColor usersSide;
							if (mI.getUserIdOfWhoseTurnItIs().equals(userId)) {
								graphics.setTurnLabelText(messages.itsYourTurn());
								usersSide = newMatchState.getWhoseTurn();
							}
							else {
								graphics.setTurnLabelText(messages.turnOfOpponent(opponentNameFromServer));
								usersSide = newMatchState.getWhoseTurn().getOpposite();
							}
							setUsersSide(usersSide);
							if (usersSide.isNorth())
								graphics.setSideLabelText(messages.playOnNorthSide());
							else
								graphics.setSideLabelText(messages.playOnSouthSide());
							setState(newMatchState);

							if (mI.getUserIdOfWhoseTurnItIs().equals("AI")) // Can be caused due to un-updated AI turn
								makeAiMove();
						}
					}
				});
			}

			public void onFailure(Throwable caught) {
				Window.alert("Error retrieving xsrf token! Please try again later.");
			}
		});
	}

	void deleteGame() {

		// secured against xsrf attacks
		// see http://www.gwtproject.org/doc/latest/DevGuideSecurityRpcXsrf.html
		XsrfTokenServiceAsync xsrf = (XsrfTokenServiceAsync) GWT.create(XsrfTokenService.class);
		((ServiceDefTarget) xsrf).setServiceEntryPoint(GWT.getModuleBaseURL() + "gwt/xsrf");
		xsrf.getNewXsrfToken(new AsyncCallback<XsrfToken>() {
			public void onSuccess(XsrfToken token) {
				((HasRpcToken) mancalaService).setRpcToken(token);
				mancalaService.deleteMatch(userId, matchId, new AsyncCallback<Void>() {
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
								mancalaService.loadMatches(userId, loadMatchesCallback);
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
		//
		// graphics.setTurnLabelText(messages.startNewGame());
		// graphics.setSideLabelText("");
		// graphics.setOpponentNameLabelText("");
		// matchId = null;
		// graphics.setStartDateLabelText("");
		// clearBoard();
	}

	String getCustomLocalDate(Long lDate) {
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
			try {

				// graphics.addItemToMatchList("", "");

				final String[] fResult = result;

				// graphics.clearMatchDisplay();

				String method = "fql.query";
				String fql = "SELECT uid, name, pic_square FROM user WHERE uid IN (SELECT uid2 FROM friend WHERE uid1 = me())";

				JSONObject query = new JSONObject();
				query.put("method", new JSONString(method));
				query.put("query", new JSONString(fql));

				// Execute query
				fbCore.api(query.getJavaScriptObject(), new Callback<JavaScriptObject>() {
					public void onSuccess(JavaScriptObject response) {
						List<ContactInfo> contacts = Lists.newArrayList();
						try {
							JSONObject friends = new JSONObject(response);
							for (int i = 0; i < friends.size(); i++) {
								JSONObject jso = (JSONObject) new JSONObject(response).get(i + "");
								String id = jso.get("uid").toString().substring(1, jso.get("uid").toString().length() - 1);
								String name = jso.get("name").toString().substring(1, jso.get("name").toString().length() - 1);
								String picUrl = jso.get("pic_square").toString().substring(1, jso.get("pic_square").toString().length() - 1);

								contacts.add(new ContactInfo(id, name, picUrl));
							}
							Collections.sort(contacts);

							List<ContactInfo> contactsWithOngoingMatch = Lists.newArrayList();
							if (fResult != null) {
								for (String matchInfoString : fResult) {
									if (matchInfoString == null) {
										continue;
									}
									MatchInfo mI = MatchInfo.deserialize(matchInfoString);

									String opponent;
									if (mI.getNorthPlayerId().equals(userId))
										opponent = mI.getSouthPlayerId();
									else
										opponent = mI.getNorthPlayerId();
									ContactInfo matchOngoingContact;
									if (opponent.equals("AI")) {
										matchOngoingContact = new ContactInfo("AI", "Computer",
												"https://dl.dropboxusercontent.com/u/6568643/MancalaFb/robot.gif", "", null);
									}
									else {
										ContactInfo hCI = new ContactInfo(opponent, "", "", "", null);
										matchOngoingContact = contacts.get(contacts.indexOf(hCI)).copy();
									}

									String turnText;
									if (mI.getUserIdOfWhoseTurnItIs().equals(userId))
										turnText = messages.yourTurn();
									else
										turnText = messages.theirTurn();
									matchOngoingContact.setTurn(turnText);

									matchOngoingContact.setMatchId(Long.valueOf(mI.getMatchId()));

									State state = State.deserialize(mI.getState());
									if (state.isGameOver()) {
										matchOngoingContact.setTurn("");
										matchOngoingContact.setMatchId(null);
									}

									matchOngoingContact.setMatchId(Long.valueOf(mI.getMatchId()));
									contactsWithOngoingMatch.add(matchOngoingContact);

									// PlayerColor usersSide = mI.getNorthPlayerId().equals(userId) ? PlayerColor.N : PlayerColor.S;
									//
									// State state = State.deserialize(mI.getState());
									// if (state.isGameOver()) {
									// if (state.winner() == null) {
									// turnText = messages.tie();
									// }
									// else {
									// if (state.winner().equals(usersSide))
									// turnText = messages.youWon(state.score() + "", (48 - state.score()) + "");
									// else
									// turnText = messages.youLost((48 - state.score()) + "", state.score() + "");
									// }
									// }

								}

								// make them alphabetical
								Collections.sort(contactsWithOngoingMatch);
								// shift the contacts with a match to the top
								int i = 0;
								for (ContactInfo cI : contactsWithOngoingMatch) {
									contacts.remove(cI);
									contacts.add(i, cI);
									i++;
								}
							}

							graphics.setContactsInList(contacts);
						}
						catch (Exception e) {
							graphics.windowAlert(e.toString());
						}
					}
				});

			}
			catch (Exception e) {
				graphics.windowAlert(e.toString());
			}
		}
	};

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
				mancalaService.makeMove(userId, matchId, fChosenIndex, fStateString, new AsyncCallback<Void>() {
					@Override
					public void onFailure(Throwable caught) {
						Window.alert("Server error!");
					}

					@Override
					public void onSuccess(Void result) {
						// updateMatchList();
					}
				});
			}

			public void onFailure(Throwable caught) {
				Window.alert("Error retrieving xsrf token! Please try again later.");
			}
		});

	}

	public void sendMoveToServerAI(Integer chosenIndex, State state) {
		final Integer fChosenIndex = chosenIndex;
		final State fState = state;
		final Graphics graphicsForAnim = (Graphics) graphics;

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
						if (fState.getWhoseTurn().equals(getUsersSide().getOpposite())) {
							graphics.setAiMovesLabelTextTriggerAiMove(messages.aiMakesMove(), graphicsForAnim);
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

	public void updateMatchList() {
		// secured against xsrf attacks
		// see http://www.gwtproject.org/doc/latest/DevGuideSecurityRpcXsrf.html
		XsrfTokenServiceAsync xsrf = (XsrfTokenServiceAsync) GWT.create(XsrfTokenService.class);
		((ServiceDefTarget) xsrf).setServiceEntryPoint(GWT.getModuleBaseURL() + "gwt/xsrf");
		xsrf.getNewXsrfToken(new AsyncCallback<XsrfToken>() {
			public void onSuccess(XsrfToken token) {
				((HasRpcToken) mancalaService).setRpcToken(token);
				mancalaService.loadMatches(userId, loadMatchesCallback);
			}

			public void onFailure(Throwable caught) {
				Window.alert("Error retrieving xsrf token! Please try again later.");
			}
		});

	}

	/**
	 * makes a move on the model, adds the new state to the history and updates the board
	 */
	void makeMove(int index) {
		try {
			lastMove = index;
			if (aiMatch) {
				stateToSetAfterAnimation = state.copyState();
				stateToSetAfterAnimation.makeMove(index);
				makeAnimatedMove(index, state.copyState());
			}
			else {
				State stateCopy = state.copyState();
				stateCopy.makeMove(index);
				sendMoveToServer(index, State.serialize(stateCopy));
			}
			// State oldState = state.copyState();
			//
			// stateToSetAfterAnimation = state.copyState();
			// stateToSetAfterAnimation.makeMove(index);

			// state.makeMove(index);

			// sendMoveToServer() is called by the graphics class after the animation is done
			// makeAnimatedMove(index, oldState);

		}
		catch (IllegalMoveException e) {
			graphics.setMessage(messages.newGameBecauseError() + e);
			setState(new State());
		}
		catch (GameOverException e) {
			graphics.setMessage(messages.newGameBecauseError() + e);
			setState(new State());
		}
	}

	/**
	 * Updates all elements that are necessary after the state changed 1. Update all the seedAmounts in the pits after a move was
	 * made 2. It enables only the pits from the player whose turn it is 3. When there are zero seeds in a pit it can't be chosen
	 * either so disable them 4. Set a message in the case of game over
	 */
	void updateBoard() {

		if (usersSide == null)
			graphics.setTurnLabelText(messages.startNewGame());
		else {
			graphics.setTurnLabelText(state.getWhoseTurn().equals(usersSide) ? messages.itsYourTurn() : messages.opponentsTurn());
		}
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

		if (usersSide == null) {// sideTheUserIs is not yet initialized
			enableNorth = false;
			enableSouth = false;
		}
		else if (state.getWhoseTurn().isNorth() && usersSide.isNorth()) {
			enableNorth = true;
			enableSouth = false;
		}
		else if (state.getWhoseTurn().isNorth() && usersSide.isSouth()) {
			enableNorth = false;
			enableSouth = false;
		}
		else if (state.getWhoseTurn().isSouth() && usersSide.isSouth()) {
			enableNorth = false;
			enableSouth = true;
		}
		else {
			enableNorth = false;
			enableSouth = false;
		}

		for (int i = 0; i < state.getNorthPits().length - 1; i++) {
			graphics.setPitEnabled(PlayerColor.N, i, enableNorth);
			graphics.setPitEnabled(PlayerColor.S, i, enableSouth);
		}
	}

	/**
	 * When there are zero seeds in a pit it can't be chosen either so disable them
	 */
	void disableZeroSeedPits() {
		int[] activePits = new int[7];
		if (state.getWhoseTurn().isNorth())
			activePits = state.getNorthPits();
		else
			activePits = state.getSouthPits();

		// state.getNorthPits().length-1 because the last array field is the
		// treasure chest
		for (int i = 0; i < state.getNorthPits().length - 1; i++) {
			if (activePits[i] == 0)
				graphics.setPitEnabled(state.getWhoseTurn(), i, false);
		}
	}

	/**
	 * Set a message in the case of game over
	 */
	void message() {
		if (state.isGameOver()) {

			graphics.gameOverSound();
			String msg = "";
			if (state.winner() == null) {
				msg = messages.tie();
			}
			else {
				String winner = state.winner().isNorth() ? "North" : "South";
				msg = messages.winnerIs(winner, state.score() + "");
			}
			graphics.setMessage(msg);
			publish(msg);
		}
	}

	/**
	 * Show publish dialog.
	 */
	public void publish(String msg) {
		JSONObject data = new JSONObject();
		data.put("method", new JSONString("stream.publish"));
		data.put("message", new JSONString(msg));

		JSONObject attachment = new JSONObject();
		attachment.put("name", new JSONString("Mancala"));
		attachment.put("caption", new JSONString("The Board Game"));
		attachment.put("description", new JSONString(msg));
		attachment.put("href", new JSONString("http://10.mymancala.appspot.com"));
		data.put("attachment", attachment);

		JSONObject actionLink = new JSONObject();
		actionLink.put("text", new JSONString("Mancala"));
		actionLink.put("href", new JSONString("http://10.mymancala.appspot.com"));

		JSONArray actionLinks = new JSONArray();
		actionLinks.set(0, actionLink);
		data.put("action_links", actionLinks);

		data.put("user_message_prompt", new JSONString("Share your thoughts about Mancala"));

		/*
		 * Execute facebook method
		 */
		fbCore.ui(data.getJavaScriptObject(), new Callback());

	}

	/**
	 * Update all the seedAmounts in the pits after a move was made
	 */
	private void updatePits() {
		for (int i = 0; i < state.getNorthPits().length; i++) {
			graphics.setSeeds(PlayerColor.N, i, state.getNorthPits()[i]);
			graphics.setSeeds(PlayerColor.S, i, state.getSouthPits()[i]);
		}
	}

	/**
	 * After the user clicked on a pit the seeds should be distributed in an animated fashion
	 * 
	 * @param chosenPitIndex
	 *          the index the user chose to distribute the seeds from
	 * @param oldState
	 *          the state before the user chose his pit
	 */
	void makeAnimatedMove(int chosenPitIndex, State oldState) {

		// disable board until the animation is over
		disableBoard();
		state.makeMove(chosenPitIndex);

		PlayerColor whoseTurn = oldState.getWhoseTurn();
		PlayerColor sideToPlaceSeedOn = whoseTurn;
		int seedAmount = oldState.getPitsOfWhoseTurn()[chosenPitIndex];
		boolean lastAnimation = false;
		int indexToPlaceSeedIn = chosenPitIndex;
		int maxIndex = 6;
		for (int i = 1; i <= seedAmount; i++) {
			indexToPlaceSeedIn++;
			maxIndex = whoseTurn.equals(sideToPlaceSeedOn) ? 6 : 5;
			if ((indexToPlaceSeedIn) > maxIndex) {
				sideToPlaceSeedOn = sideToPlaceSeedOn.getOpposite();
				indexToPlaceSeedIn = 0;
			}
			if (i == seedAmount)
				lastAnimation = true;
			graphics
					.animateFromPitToPit(whoseTurn, chosenPitIndex, sideToPlaceSeedOn, indexToPlaceSeedIn, 400 * (i - 1), lastAnimation);

		}

		if (this.state.getLastMoveWasOppositeCapture()) {
			// graphics.oppositeCaptureSound();

			// TODO: give this it's own animation

			// //int[] opposingPits = whoseTurn.isNorth() ?
			// this.state.getSouthPits() : state.getNorthPits();
			// int seedAmountInOpposingPit = this.state.getOppositeSeeds();
			//
			// graphics.animateFromPitToPit(whoseTurn, indexToPlaceSeedIn,
			// whoseTurn, 6, seedAmount * 400 + 1400);
			// for(int i = 0; i < seedAmountInOpposingPit; i++)
			// graphics.animateFromPitToPit(whoseTurn.getOpposite(),
			// State.getMirrorIndex(indexToPlaceSeedIn, 5), whoseTurn, 6,
			// seedAmount * 400 + 1000 + 400 * i);
		}
	}

	public void afterAnimation() {
		if (stateToSetAfterAnimation.getLastMoveWasOppositeCapture())
			graphics.oppositeCaptureSound();

		state = stateToSetAfterAnimation.copyState();
		updateBoard();

		if (aiMatch)
			sendMoveToServerAI(lastMove, state);
	}

	void setState(State state) {
		this.state = state;

		updateBoard();
	}

	public void newGame() {
		setState(new State());
	}

	public void clearBoard() {
		// update pits
		for (int i = 0; i < state.getNorthPits().length; i++) {
			graphics.setSeeds(PlayerColor.N, i, 0);
			graphics.setSeeds(PlayerColor.S, i, 0);
		}

		// disable pits
		disableBoard();
	}

	public void disableBoard() {
		for (int i = 0; i < state.getNorthPits().length - 1; i++) {
			graphics.setPitEnabled(PlayerColor.N, i, false);
			graphics.setPitEnabled(PlayerColor.S, i, false);
		}
	}

	public void setUsersSide(PlayerColor side) {
		usersSide = side;
	}

	public PlayerColor getUsersSide() {
		return usersSide;
	}

	public void makeAiMove() {
		// System.out.println("Presenter makeAiMove");
		if (state.isGameOver()) {
			return;
		}
		AlphaBetaPruning ai = new AlphaBetaPruning(new Heuristic());
		Integer aiMove = ai.findBestMove(state, 5, new DateTimer(3000));

		System.out.println("P MAIM state: " + state);
		System.out.println("P MAIM move: " + aiMove);

		State oldState = state.copyState();

		stateToSetAfterAnimation = state.copyState();
		stateToSetAfterAnimation.makeMove(aiMove);

		makeAnimatedMove(aiMove, oldState);

		// state.makeMove(aiMove);
		System.out.println("State after Move: " + state);
		// setState(state);

		// graphics.sendMoveToServerAI(aiMove, state);

		// this will be made in the graphics after the move was saved
		// if (state.getWhoseTurn().equals(usersSide.getOpposite()))
		// makeAiMove();

	}

	public void startGame(ContactInfo contact) {
		if (contact.getTurn().equals(""))
			startGame(userId, contact.getId(), contact.getName());
		else
			changeGame(contact.getMatchId());
	}

}
