package org.mancala.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("MancalaService")
public interface MancalaService extends RemoteService {
	/**
	 * Submit a move to the server
	 * 
	 * @param state the state that the game is in after the move
	 * @param id the user id
	 * @return the state in String format
	 */
	public String SubMove(String state, String id);
	
	/**
	 * Register a player at the server
	 * 
	 * @param id user id
	 * @return a string[2] array - the first entry is to give the user some information, 
	 * the second contains as which player (North or South) the player registered 
	 */
	public String[] AddPlayer(String id);
}