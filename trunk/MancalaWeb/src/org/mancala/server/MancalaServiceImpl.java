package org.mancala.server;

import static com.googlecode.objectify.ObjectifyService.ofy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mancala.client.MancalaService;
import org.mancala.shared.Match;
import org.mancala.shared.MatchInfo;
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
			String channelId = user.getEmail().toLowerCase();
			Key<Player> playerKey = Key.create(Player.class, user.getEmail().toLowerCase());
			Player player = ofy().load().key(playerKey).now();
			if (player == null) { // New player
				player = new Player(user.getEmail().toLowerCase(), user.getNickname());
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

			Key<Player> playerKey = Key.create(Player.class, user.getEmail().toLowerCase());
			Player player = ofy().load().key(playerKey).now();

			String[] matches = new String[player.getMatchesList().size()];
			Set<Key<Match>> matchesList = player.getMatchesList();

			int index = 0;
			for (Key<Match> matchKey : matchesList) {
				Match match = ofy().load().key(matchKey).now();
				if (match == null) {
					continue;
				}
				Key<Player> opponentKey = match.getOpponent(playerKey);
				Player opponent = ofy().load().key(opponentKey).now();

				MatchInfo matchInfo = new MatchInfo();
				matchInfo.setMatchId(match.getMatchId() + "");
				if (match.isNorthPlayer(playerKey)) {
					matchInfo.setNorthPlayerId(player.getEmail());
					matchInfo.setNorthPlayerName(player.getPlayerName());
					matchInfo.setSouthPlayerId(opponent.getEmail());
					matchInfo.setSouthPlayerName(opponent.getPlayerName());
				}
				else {
					matchInfo.setNorthPlayerId(opponent.getEmail());
					matchInfo.setNorthPlayerName(opponent.getPlayerName());
					matchInfo.setSouthPlayerId(player.getEmail());
					matchInfo.setSouthPlayerName(player.getPlayerName());
				}
				matchInfo.setState(match.getState());
				matchInfo.setMoveIndex(-1 + "");
				if ((match.isNorthPlayer(playerKey) && match.isNorthsTurn()) || (match.isSouthPlayer(playerKey) && !match.isNorthsTurn())) {
					matchInfo.setUserIdOfWhoseTurnItIs(player.getEmail());
				}
				else {
					matchInfo.setUserIdOfWhoseTurnItIs(opponent.getEmail());
				}
				matchInfo.setAction("loadMatches");
				String matchInfoString = MatchInfo.serialize(matchInfo);

				matches[index++] = matchInfoString;

				ofy().save().entities(match, opponent).now();
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

			final Key<Player> playerKey = Key.create(Player.class, user.getEmail().toLowerCase());
			final Player player = ofy().load().key(playerKey).now();
			// Get list of players waiting to be automatched
			List<Player> otherPlayers = ofy().load().type(Player.class).filter("automatchPooled", true).list();

			// If there are no other players add him to the automatch pool
			if (otherPlayers.isEmpty()) {
				player.setAutomatchEligible(true);
				ofy().save().entity(player).now();
			}
			else {
				final Player opponent;
				// Extract a random waiting player
				if (otherPlayers.get(0).equals(player)) {
					if (otherPlayers.size() > 1 && !otherPlayers.get(1).equals(player))
						opponent = otherPlayers.remove(1);
					else
						return; // Can't play against the same player
				}
				else
					opponent = otherPlayers.remove(0);

				opponent.setAutomatchEligible(false); // Remove this player from the automatch pool

				final Key<Player> opponentKey = Key.create(Player.class, opponent.getEmail());
				Match match = new Match(opponentKey, playerKey, "4,4,4,4,4,4,0_4,4,4,4,4,4,0_S_F_F_0");
				Key<Match> matchKey = ofy().save().entity(match).now();
				player.addMatch(matchKey);
				opponent.addMatch(matchKey);
				ofy().save().entities(player, opponent, match).now();

				// Send both players the matchInfo message
				MatchInfo matchInfo = new MatchInfo();

				matchInfo.setMatchId(match.getMatchId() + "");
				matchInfo.setNorthPlayerId(opponent.getEmail());
				matchInfo.setNorthPlayerName(opponent.getPlayerName());
				matchInfo.setSouthPlayerId(player.getEmail());
				matchInfo.setSouthPlayerName(player.getPlayerName());
				matchInfo.setState("4,4,4,4,4,4,0_4,4,4,4,4,4,0_S_F_F_0");
				matchInfo.setMoveIndex(-1 + "");
				matchInfo.setUserIdOfWhoseTurnItIs(player.getEmail());
				matchInfo.setAction("newgame");

				String message = MatchInfo.serialize(matchInfo);

				Set<String> tokens1 = opponent.getConnectedTokens();
				Set<String> tokens2 = player.getConnectedTokens();
				for (String connection : tokens1) {
					channelService.sendMessage(new ChannelMessage(connection, message));
				}
				for (String connection : tokens2) {
					channelService.sendMessage(new ChannelMessage(connection, message));
				}
			}
		}
	}

	@Override
	public Boolean newEmailGame(String email) {
		if (userService.isUserLoggedIn()) {
			User user = userService.getCurrentUser();
			Key<Player> playerKey = Key.create(Player.class, user.getEmail().toLowerCase());
			Player player = ofy().load().key(playerKey).now();
			if (player.getEmail().equals(email.toLowerCase()))
				return null;

			Key<Player> opponentKey = Key.create(Player.class, email.toLowerCase());
			Player opponent = ofy().load().key(opponentKey).now();
			if (opponent == null) {
				// No such player exists in the data store
				return new Boolean(false);
			}
			Match match = new Match(opponentKey, playerKey, "4,4,4,4,4,4,0_4,4,4,4,4,4,0_S_F_F_0");
			Key<Match> matchKey = ofy().save().entity(match).now();
			player.addMatch(matchKey);
			opponent.addMatch(matchKey);

			// Send both players matchInfo message
			MatchInfo matchInfo = new MatchInfo();
			matchInfo.setMatchId(match.getMatchId() + "");
			matchInfo.setNorthPlayerId(opponent.getEmail());
			matchInfo.setNorthPlayerName(opponent.getPlayerName());
			matchInfo.setSouthPlayerId(player.getEmail());
			matchInfo.setSouthPlayerName(player.getPlayerName());
			matchInfo.setState("4,4,4,4,4,4,0_4,4,4,4,4,4,0_S_F_F_0");
			matchInfo.setMoveIndex(-1 + "");
			matchInfo.setUserIdOfWhoseTurnItIs(player.getEmail());
			matchInfo.setAction("newgame");
			String message = MatchInfo.serialize(matchInfo);

			Set<String> tokens1 = player.getConnectedTokens();
			Set<String> tokens2 = opponent.getConnectedTokens();
			ofy().save().entities(player, opponent, match).now();
			for (String connection : tokens1) {
				channelService.sendMessage(new ChannelMessage(connection, message));
			}
			for (String connection : tokens2) {
				channelService.sendMessage(new ChannelMessage(connection, message));
			}
			return new Boolean(true);
		}
		return new Boolean(false);
	}

	@Override
	public void deleteMatch(Long matchId) {
		if (userService.isUserLoggedIn()) {
			User user = userService.getCurrentUser();

			Key<Player> playerKey = Key.create(Player.class, user.getEmail().toLowerCase());
			Player player = ofy().load().key(playerKey).now();

			Key<Match> matchKey = Key.create(Match.class, matchId);

			// Remove match from this player
			player.removeMatch(matchKey);

			ofy().save().entity(player).now();

			Match match = ofy().load().key(matchKey).now();

			Player opponent = ofy().load().key(match.getOpponent(playerKey)).now();

			if (!opponent.containsMatchKey(matchKey)) {
				// Delete the match from the datastore, if opponent has also deleted it
				ofy().delete().entity(match);
			}
			ofy().save().entities(match, opponent).now();
		}
	}

	@Override
	public String changeMatch(Long matchId) {
		if (userService.isUserLoggedIn()) {
			if (matchId == null) {
				return "noMatch";
			}

			User user = userService.getCurrentUser();

			Key<Player> playerKey = Key.create(Player.class, user.getEmail().toLowerCase());
			Player player = ofy().load().key(playerKey).now();

			Key<Match> matchKey = Key.create(Match.class, matchId);
			Match match = ofy().load().key(matchKey).now();

			Key<Player> opponentKey = match.getOpponent(playerKey);
			Player opponent = ofy().load().key(opponentKey).now();

			MatchInfo matchInfo = new MatchInfo();
			matchInfo.setMatchId(matchId + "");
			if (match.isNorthPlayer(playerKey)) {
				matchInfo.setNorthPlayerId(player.getEmail());
				matchInfo.setNorthPlayerName(player.getPlayerName());
				matchInfo.setSouthPlayerId(opponent.getEmail());
				matchInfo.setSouthPlayerName(opponent.getPlayerName());
			}
			else {
				matchInfo.setNorthPlayerId(opponent.getEmail());
				matchInfo.setNorthPlayerName(opponent.getPlayerName());
				matchInfo.setSouthPlayerId(player.getEmail());
				matchInfo.setSouthPlayerName(player.getPlayerName());
			}
			matchInfo.setState(match.getState());
			matchInfo.setMoveIndex(-1 + "");

			if ((match.isNorthPlayer(playerKey) && match.isNorthsTurn()) || (match.isSouthPlayer(playerKey) && !match.isNorthsTurn())) {
				matchInfo.setUserIdOfWhoseTurnItIs(player.getEmail());
			}
			else {
				matchInfo.setUserIdOfWhoseTurnItIs(opponent.getEmail());
			}
			matchInfo.setAction("changeMatch");

			String message = MatchInfo.serialize(matchInfo);

			ofy().save().entities(match, player, opponent).now();
			return message;
		}
		return null;
	}

	@Override
	public void makeMove(Long matchId, Integer chosenIndex, String state) {
		if (userService.isUserLoggedIn()) {
			User user = userService.getCurrentUser();

			Key<Match> matchKey = Key.create(Match.class, matchId);
			Match match = ofy().load().key(matchKey).now();
			match.setState(state);

			Key<Player> playerKey = Key.create(Player.class, user.getEmail().toLowerCase());
			Player player = ofy().load().key(playerKey).now();

			Key<Player> opponentKey = match.getOpponent(playerKey);
			Player opponent = ofy().load().key(opponentKey).now();

			ofy().save().entities(match, opponent, player).now();

			MatchInfo matchInfo = new MatchInfo();
			matchInfo.setMatchId(matchId + "");
			if (match.isNorthPlayer(playerKey)) {
				matchInfo.setNorthPlayerId(player.getEmail());
				matchInfo.setNorthPlayerName(player.getPlayerName());
				matchInfo.setSouthPlayerId(opponent.getEmail());
				matchInfo.setSouthPlayerName(opponent.getPlayerName());
			}
			else {
				matchInfo.setNorthPlayerId(opponent.getEmail());
				matchInfo.setNorthPlayerName(opponent.getPlayerName());
				matchInfo.setSouthPlayerId(player.getEmail());
				matchInfo.setSouthPlayerName(player.getPlayerName());
			}
			matchInfo.setState(state);
			matchInfo.setMoveIndex(chosenIndex + "");
			if ((match.isNorthPlayer(playerKey) && match.isNorthsTurn()) || (match.isSouthPlayer(playerKey) && !match.isNorthsTurn())) {
				matchInfo.setUserIdOfWhoseTurnItIs(player.getEmail());
			}
			else {
				matchInfo.setUserIdOfWhoseTurnItIs(opponent.getEmail());
			}
			matchInfo.setAction("move");
			String message = MatchInfo.serialize(matchInfo);

			Set<String> tokens1 = player.getConnectedTokens();
			for (String connection : tokens1) {
				System.out.println("token :" + connection);
				channelService.sendMessage(new ChannelMessage(connection, message));
			}
			Set<String> tokens2 = opponent.getConnectedTokens();
			for (String connection : tokens2) {
				System.out.println("token :" + connection);
				channelService.sendMessage(new ChannelMessage(connection, message));
			}
		}
	}

}