package org.mancala.client.services;

import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.google.gwt.user.client.rpc.XsrfProtectedService;

@RemoteServiceRelativePath("MancalaService")
public interface MancalaService extends XsrfProtectedService {

	public String connectPlayer(String playerId, String name);

	public String[] loadMatches(String playerId);

	public Boolean startGame(String playerId, String opponentId, String opponentName);

	public void deleteMatch(String playerId, Long matchId);

	public String changeMatch(String playerId, Long matchId);

	public void makeMove(String playerId, Long matchId, Integer chosenIndex, String stateString);

	public String registerAiMatch(String playerId, boolean aiIsNorth);

	public void saveAiMove(Long matchId, String moveString, String stateString);

}