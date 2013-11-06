package org.mancala.client;

import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.google.gwt.user.client.rpc.XsrfProtectedService;

@RemoteServiceRelativePath("MancalaService")
public interface MancalaService extends XsrfProtectedService {

	public String connectPlayer();

	public String[] loadMatches();

	public void automatch();

	public Boolean newEmailGame(String email);

	public void deleteMatch(Long matchId);

	public String changeMatch(Long matchId);

	public void makeMove(Long matchId, Integer chosenIndex, String stateString);

	public String registerAiMatch(boolean aiIsNorth);

	public void saveAiMove(Long matchId, String moveString, String stateString);

}