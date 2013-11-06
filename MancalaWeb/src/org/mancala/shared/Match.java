package org.mancala.shared;

import java.util.Date;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

/**
 * Match Entity which stores information about the match
 * 
 * @author Harsh - adapted by Micha Guthmann
 * 
 */
@Entity
public class Match {
	@Id
	private Long matchId;
	private Key<Player> northPlayer;
	private Key<Player> southPlayer;
	private String state;
	Long startDate;
	boolean singleGame;

	public Match() {
	}

	public Match(Key<Player> north, Key<Player> south, String state) {
		this.northPlayer = north;
		this.southPlayer = south;
		this.state = state;
		startDate = new Date().getTime();
	}

	/**
	 * @return the isSingleGame
	 */
	public boolean getSingleGame() {
		return singleGame;
	}

	/**
	 * @param isSingleGame
	 *          the isSingleGame to set
	 */
	public void setSingleGame(boolean singleGame) {
		this.singleGame = singleGame;
	}

	public Long getMatchId() {
		return matchId;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public Key<Player> getOpponent(Key<Player> player) {
		if (player.equals(northPlayer)) {
			return southPlayer;
		}
		else if (player.equals(southPlayer)) {
			return northPlayer;
		}
		else {
			return null;
		}
	}

	// South starts the game
	public boolean isNorthsTurn() {
		if (state.equals("")) {
			return false;
		}
		if (state.split("_")[2].equals("N")) {
			return true;
		}
		return false;
	}

	public boolean isNorthPlayer(Key<Player> player) {
		return northPlayer.equals(player);
	}

	public boolean isSouthPlayer(Key<Player> player) {
		return southPlayer.equals(player);
	}

	public boolean isMatchOver() {
		if (state.split("_")[3].equals("T")) {
			return true;
		}
		return false;
	}

	public void removePlayer(Key<Player> player) {
		if (northPlayer.equals(player)) {
			northPlayer = null;
		}
		else if (southPlayer.equals(player)) {
			southPlayer = null;
		}
	}

	public Long getStartDate() {
		return startDate;
	}

	public void setStartDate(Long startDate) {
		this.startDate = startDate;
	}

}
