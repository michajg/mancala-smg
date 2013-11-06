package org.mancala.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface MancalaServiceAsync {

	void connectPlayer(AsyncCallback<String> callback);

	void loadMatches(AsyncCallback<String[]> callback);

	void automatch(AsyncCallback<Void> callback);

	void newEmailGame(String email, AsyncCallback<Boolean> callback);

	void deleteMatch(Long matchId, AsyncCallback<Void> callback);

	void changeMatch(Long matchId, AsyncCallback<String> callback);

	void makeMove(Long matchId, Integer chosenIndex, String stateString, AsyncCallback<Void> callback);

	void registerAiMatch(boolean aiIsNorth, AsyncCallback<String> asyncCallback);

	void saveAiMove(Long matchId, String moveString, String stateString, AsyncCallback<Void> asyncCallback);

}