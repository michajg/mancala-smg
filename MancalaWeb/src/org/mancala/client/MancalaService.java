package org.mancala.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("MancalaService")
public interface MancalaService extends RemoteService {

	public String connectPlayer();

	public String[] loadMatches();

	public void automatch();

	public Boolean newEmailGame(String email);

	public void deleteMatch(Long matchId);

	public String changeMatch(Long matchId);

	public void makeMove(Long matchId, Integer chosenIndex, String stateString);

}