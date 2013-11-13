package org.mancala.shared;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.condition.IfTrue;

/**
 * Player Entity which stores information about the Player
 * 
 * @author Harsh
 * 
 */
@Entity
public class Player {
	@Id
	private String id;
	private String playerName;
	private Set<String> connectedTokens;
	private Set<Key<Match>> matches;
	@Index(IfTrue.class)
	private boolean automatchPooled;
	private double rating;
	private double RD;

	public Player() {
		this.id = "";
		this.playerName = "";
		connectedTokens = new HashSet<String>();
		matches = new HashSet<Key<Match>>();
		automatchPooled = false;
		rating = 1500;
		RD = 350;
	}

	public Player(String id, String name) {
		this();
		this.id = id;
		this.playerName = name;
	}

	public String getId() {
		return id;
	}

	public String getPlayerName() {
		return playerName;
	}

	public Set<String> getConnectedTokens() {
		return Collections.unmodifiableSet(connectedTokens);
	}

	public void addToken(String token) {
		connectedTokens.add(token);
	}

	public void removeToken(String token) {
		connectedTokens.remove(token);
	}

	public Set<Key<Match>> getMatchesList() {
		return matches;
	}

	public void addMatch(Key<Match> match) {
		matches.add(match);
	}

	public void removeMatch(Key<Match> match) {
		matches.remove(match);
	}

	public boolean containsMatchKey(Key<Match> match) {
		return matches.contains(match);
	}

	public void setAutomatchEligible(boolean condition) {
		automatchPooled = condition;
	}

	public double getRating() {
		return rating;
	}

	public void setRating(double rating) {
		this.rating = rating;
	}

	public double getRD() {
		return RD;
	}

	public void setRD(double rD) {
		RD = rD;
	}
}
