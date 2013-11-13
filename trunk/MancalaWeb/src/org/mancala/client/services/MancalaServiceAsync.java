package org.mancala.client.services;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface MancalaServiceAsync {

	void connectPlayer(String playerId, String name, AsyncCallback<String> callback);

	void loadMatches(String playerId, AsyncCallback<String[]> callback);

	void startGame(String playerId, String opponentId, String opponentName, AsyncCallback<Boolean> callback);

	void deleteMatch(String playerId, Long matchId, AsyncCallback<Void> callback);

	void changeMatch(String playerId, Long matchId, AsyncCallback<String> callback);

	void makeMove(String playerId, Long matchId, Integer chosenIndex, String stateString, AsyncCallback<Void> callback);

	void registerAiMatch(String playerId, boolean aiIsNorth, AsyncCallback<String> asyncCallback);

	void saveAiMove(Long matchId, String moveString, String stateString, AsyncCallback<Void> asyncCallback);

}