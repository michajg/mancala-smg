package org.mancala.server;

import static com.googlecode.objectify.ObjectifyService.ofy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mancala.client.MancalaService;
import org.mancala.shared.Match;
import org.mancala.shared.Player;

import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;

/**
 * Contains remote server methods that provide services for multiplayer Mancala gameplay
 * 
 * @author Harsh - adapted by Micha Guthmann
 */
public class MancalaServiceImpl extends RemoteServiceServlet implements MancalaService {
	private static final long serialVersionUID = 1L;

	private String wait = "";
	private Map<String, String> hash = new HashMap<String, String>();
	ChannelService channelService = ChannelServiceFactory.getChannelService();

	static {
		ObjectifyService.register(Player.class);
		ObjectifyService.register(Match.class);
	}

	UserService userService = UserServiceFactory.getUserService();

	@Override
	public String connectPlayer() {
		if (userService.isUserLoggedIn()) {
			User user = userService.getCurrentUser();
			String channelId = user.getEmail();
			Key<Player> playerKey = Key.create(Player.class, user.getEmail());
			Player player = ofy().load().key(playerKey).now();
			if (player == null) { // New player
				player = new Player(user.getEmail(), user.getNickname());
			}
			ofy().save().entity(player).now();
			// channelId += "-"+System.currentTimeMillis();
			String token = channelService.createChannel(channelId);
			return token;
		}
		return null;
	}

	@Override
	public String[] loadMatches() {
		if (userService.isUserLoggedIn()) {
			User user = userService.getCurrentUser();
			Key<Player> playerKey = Key.create(Player.class, user.getEmail());
			Player player = ofy().load().key(playerKey).now();
			String[] matches = new String[player.getMatchesList().size()];
			Set<Key<Match>> matchesList = player.getMatchesList();
			int index = 0;
			for (Key<Match> matchKey : matchesList) {
				Match match = ofy().load().key(matchKey).now();
				if (match == null) {
					continue;
				}
				Key<Player> otherPlayerKey = match.getOpponent(playerKey);
				Player otherPlayer = ofy().load().key(otherPlayerKey).now();
				String userIdWhosTurn;
				if ((match.isNorthPlayer(playerKey) && match.isNorthsTurn()) || (match.isSouthPlayer(playerKey) && !match.isNorthsTurn())) {
					userIdWhosTurn = player.getPlayerName();
				}
				else {
					userIdWhosTurn = otherPlayer.getPlayerName();
				}
				String northPlayer = (match.isNorthPlayer(playerKey) ? player.getEmail() : otherPlayer.getEmail());
				matches[index++] = match.getMatchId() + "#" + otherPlayer.getEmail() + "#" + otherPlayer.getPlayerName() + "#"
						+ userIdWhosTurn + "#" + northPlayer + "#" + match.getState();
				ofy().save().entities(match, otherPlayer).now();
			}
			ofy().save().entity(player).now();
			return matches;
		}
		return null;
	}

	@Override
	public void automatch() {
		if (userService.isUserLoggedIn()) {
			User user = userService.getCurrentUser();
			final Key<Player> playerKey = Key.create(Player.class, user.getEmail());
			final Player player = ofy().load().key(playerKey).now();
			// Get list of players waiting to be automatched
			List<Player> otherPlayers = ofy().load().type(Player.class).filter("automatchPooled", true).list();
			if (otherPlayers.isEmpty()) {
				player.setAutomatchEligible(true); // Put this player in the
				// automatch pool
				ofy().save().entity(player).now();
			}
			else {
				// Extract a random waiting player
				if (otherPlayers.get(0).equals(player)) {
					ofy().save().entity(player).now();
					return; // Can't play against the same player
				}
				final Player randomPlayer = otherPlayers.remove(0);
				randomPlayer.setAutomatchEligible(false); // Remove this player
				// from the
				// automatch pool
				final Key<Player> randomPlayerKey = Key.create(Player.class, randomPlayer.getEmail());
				/*
				 * Match match = ofy().transact(new Work<Match>() {
				 * 
				 * @Override public Match run() { Match match = new Match(playerKey, randomPlayerKey, "");
				 * System.out.println(match.getMatchId()); Key<Match> matchKey = ofy().save().entity(match).now(); if
				 * (!player.containsMatchKey(matchKey)) player.addMatch(matchKey); if (!randomPlayer.containsMatchKey(matchKey))
				 * randomPlayer.addMatch(matchKey); ofy().save().entities(player, randomPlayer, match).now(); return match; } });
				 */
				Match match = new Match(randomPlayerKey, playerKey, "4,4,4,4,4,4,0_4,4,4,4,4,4,0_S_F_F_0");
				Key<Match> matchKey = ofy().save().entity(match).now();
				player.addMatch(matchKey);
				randomPlayer.addMatch(matchKey);

				// Send both players messages
				// Here, player is the first player (South)
				// The message is game status # match ID # which side is the user # opponent name
				String message1 = "newgame#" + match.getMatchId() + "#N#" + player.getPlayerName();
				String message2 = "newgame#" + match.getMatchId() + "#S#" + randomPlayer.getPlayerName();
				Set<String> tokens1 = randomPlayer.getConnectedTokens();
				Set<String> tokens2 = player.getConnectedTokens();
				ofy().save().entities(player, randomPlayer, match).now();
				for (String connection : tokens1) {
					channelService.sendMessage(new ChannelMessage(connection, message1));
				}
				for (String connection : tokens2) {
					channelService.sendMessage(new ChannelMessage(connection, message2));
				}
			}
		}
	}

	@Override
	public Boolean newEmailGame(String email) {
		if (userService.isUserLoggedIn()) {
			User user = userService.getCurrentUser();
			Key<Player> playerKey = Key.create(Player.class, user.getEmail());
			Player player = ofy().load().key(playerKey).now();
			Key<Player> opponentKey = Key.create(Player.class, email);
			Player opponent = ofy().load().key(opponentKey).now();
			if (opponent == null) {
				// No such player exists in the data store
				return new Boolean(false);
			}
			Match match = new Match(playerKey, opponentKey, "4,4,4,4,4,4,0_4,4,4,4,4,4,0_S_F_F_0");
			Key<Match> matchKey = ofy().save().entity(match).now();
			player.addMatch(matchKey);
			opponent.addMatch(matchKey);
			// Send both players messages
			String message1 = "newgame#" + match.getMatchId() + "#S#" + opponent.getPlayerName();
			String message2 = "newgame#" + match.getMatchId() + "#N#" + player.getPlayerName();
			Set<String> tokens1 = player.getConnectedTokens();
			Set<String> tokens2 = opponent.getConnectedTokens();
			ofy().save().entities(player, opponent, match).now();
			for (String connection : tokens1) {
				channelService.sendMessage(new ChannelMessage(connection, message1));
			}
			for (String connection : tokens2) {
				channelService.sendMessage(new ChannelMessage(connection, message2));
			}
			return new Boolean(true);
		}
		return new Boolean(false);
	}

	@Override
	public void deleteMatch(Long matchId) {
		if (userService.isUserLoggedIn()) {
			User user = userService.getCurrentUser();
			Key<Player> playerKey = Key.create(Player.class, user.getEmail());
			Player player = ofy().load().key(playerKey).now();
			Key<Match> matchKey = Key.create(Match.class, matchId);
			player.removeMatch(matchKey); // Remove match from this player
			ofy().save().entity(player).now();

			Match match = ofy().load().key(matchKey).now();
			Player opponent = ofy().load().key(match.getOpponent(playerKey)).now();
			/*
			 * // Remove player from match if (match.isBlackPlayer(playerKey)) { match.removePlayer(playerKey); } else if
			 * (match.isWhitePlayer(playerKey)) { match.removePlayer(playerKey); }
			 */
			if (!opponent.containsMatchKey(matchKey)) {
				// Delete the match from the datastore, if opponent has also
				// deleted it
				ofy().delete().entity(match);
			}
			ofy().save().entities(match, opponent).now();
		}
	}

	@Override
	public String changeMatch(Long matchId) {
		if (userService.isUserLoggedIn()) {
			if (matchId == null) {
				return "no match";
			}
			User user = userService.getCurrentUser();
			Key<Player> playerKey = Key.create(Player.class, user.getEmail());
			Player player = ofy().load().key(playerKey).now();
			Key<Match> matchKey = Key.create(Match.class, matchId);
			Match match = ofy().load().key(matchKey).now();
			String stateStr = match.getState();
			Key<Player> otherPlayerKey = match.getOpponent(playerKey);
			Player otherPlayer = ofy().load().key(otherPlayerKey).now();
			String userIdWhosTurn;
			if ((match.isNorthPlayer(playerKey) && match.isNorthsTurn()) || (match.isSouthPlayer(playerKey) && !match.isNorthsTurn())) {
				userIdWhosTurn = player.getEmail();
			}
			else {
				userIdWhosTurn = otherPlayer.getEmail();
			}
			String msg = matchId + "#" + userIdWhosTurn + "#" + otherPlayer.getPlayerName() + "#" + stateStr;
			ofy().save().entities(match, player, otherPlayer).now();
			return msg;
		}
		return null;
	}

	@Override
	public void makeMove(Long matchId, Integer chosenIndex, String stateString) {
		if (userService.isUserLoggedIn()) {
			User user = userService.getCurrentUser();

			Key<Match> matchKey = Key.create(Match.class, matchId);
			Match match = ofy().load().key(matchKey).now();

			Key<Player> playerKey = Key.create(Player.class, user.getEmail());
			Player player = ofy().load().key(playerKey).now();

			Key<Player> otherPlayerKey = match.getOpponent(playerKey);
			Player otherPlayer = ofy().load().key(otherPlayerKey).now();

			match.setState(stateString);
			// ofy().save().entity(match).now();
			ofy().save().entities(match, otherPlayer, player).now();

			String userIdWhosTurn;
			if ((match.isNorthPlayer(playerKey) && match.isNorthsTurn()) || (match.isSouthPlayer(playerKey) && !match.isNorthsTurn())) {
				userIdWhosTurn = player.getEmail();
			}
			else {
				userIdWhosTurn = otherPlayer.getEmail();
			}

			String msg = "move#" + matchId + "#" + userIdWhosTurn + "#" + stateString + "#" + chosenIndex.toString();
			Set<String> opponentTokens = otherPlayer.getConnectedTokens();
			for (String connection : opponentTokens) {
				System.out.println("token :" + connection);
				channelService.sendMessage(new ChannelMessage(connection, msg));
			}
			Set<String> userTokens = player.getConnectedTokens();
			for (String connection : userTokens) {
				System.out.println("token :" + connection);
				channelService.sendMessage(new ChannelMessage(connection, msg));
			}
		}
	}

}