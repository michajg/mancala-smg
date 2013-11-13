package org.mancala.server;

import static com.googlecode.objectify.ObjectifyService.ofy;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.mancala.client.services.MancalaService;
import org.mancala.shared.Match;
import org.mancala.shared.MatchInfo;
import org.mancala.shared.Player;
import org.mancala.shared.PlayerColor;
import org.mancala.shared.State;
import org.mancala.shared.rating.GlickoRating;

import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gwt.user.server.rpc.XsrfProtectedServiceServlet;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;

/**
 * Contains remote server methods that provide services for multiplayer Mancala gameplay
 * 
 * @author Micha Guthmann, special thanks to Harsh from whom I took a lot of inspiration from
 */
public class MancalaServiceImpl extends XsrfProtectedServiceServlet implements MancalaService {
	private static final long serialVersionUID = 1L;

	static {
		ObjectifyService.register(Player.class);
		ObjectifyService.register(Match.class);
	}

	ChannelService channelService = ChannelServiceFactory.getChannelService();
	private DecimalFormat decimal = new DecimalFormat("#.##");
	private final String INITIAL_STATE_STRING = "4,4,4,4,4,4,0_4,4,4,4,4,4,0_S_F_F_0";
	private final String TEST_STATE_STRING = "0,0,0,0,0,0,25_0,0,0,0,0,1,22_S_F_F_0";

	@Override
	public String connectPlayer(String playerId, String name) {
		String channelId = playerId;

		Key<Player> playerKey = Key.create(Player.class, playerId);
		Player player = ofy().load().key(playerKey).now();

		if (player == null) { // New player
			player = new Player(playerId, name);
		}
		double rating = player.getRating();
		double RD = player.getRD();
		ofy().save().entity(player).now();

		String token = channelService.createChannel(channelId) + "#" + playerId + "#" + name + "#" + decimal.format(rating) + "#"
				+ decimal.format(RD);
		return token;
	}

	@Override
	public String[] loadMatches(String playerId) {

		Key<Player> playerKey = Key.create(Player.class, playerId);
		Player player = ofy().load().key(playerKey).now();

		String[] matches = new String[player.getMatchesList().size()];
		Set<Key<Match>> matchesList = player.getMatchesList();

		int index = 0;
		if (matchesList.size() > 0) {
			for (Key<Match> matchKey : matchesList) {
				Match match = ofy().load().key(matchKey).now();
				if (match == null) {
					continue;
				}
				Key<Player> opponentKey = match.getOpponent(playerKey);
				Player opponent = ofy().load().key(opponentKey).now();

				MatchInfo matchInfo = new MatchInfo();
				matchInfo.setMatchId(match.getMatchId() + "");
				Player northPlayer, southPlayer;
				if (match.isNorthPlayer(playerKey)) {
					northPlayer = player;
					southPlayer = opponent;
				}
				else {
					northPlayer = opponent;
					southPlayer = player;
				}
				matchInfo.setNorthPlayerId(northPlayer.getId());
				matchInfo.setNorthPlayerName(northPlayer.getPlayerName());
				matchInfo.setNorthPlayerRating(decimal.format(northPlayer.getRating()));
				matchInfo.setNorthPlayerRD(decimal.format(northPlayer.getRD()));
				matchInfo.setSouthPlayerId(southPlayer.getId());
				matchInfo.setSouthPlayerName(southPlayer.getPlayerName());
				matchInfo.setSouthPlayerRating(decimal.format(southPlayer.getRating()));
				matchInfo.setSouthPlayerRD(decimal.format(southPlayer.getRD()));

				matchInfo.setState(match.getState());
				matchInfo.setMoveIndex(-1 + "");
				if ((match.isNorthPlayer(playerKey) && match.isNorthsTurn()) || (match.isSouthPlayer(playerKey) && !match.isNorthsTurn())) {
					matchInfo.setUserIdOfWhoseTurnItIs(player.getId());
				}
				else {
					matchInfo.setUserIdOfWhoseTurnItIs(opponent.getId());
				}
				matchInfo.setStartDate(match.getStartDate() + "");
				matchInfo.setAction("loadMatches");
				String matchInfoString = MatchInfo.serialize(matchInfo);

				matches[index++] = matchInfoString;

				ofy().save().entities(match, opponent).now();
			}
		}
		else
			return null;
		ofy().save().entity(player).now();
		return matches;

	}

	@Override
	public Boolean startGame(String playerId, String opponentId, String opponentName) {
		Key<Player> playerKey = Key.create(Player.class, playerId);
		Player player = ofy().load().key(playerKey).now();
		if (player.getId().equals(opponentId))
			return null;

		Key<Player> opponentKey = Key.create(Player.class, opponentId);
		Player opponent = ofy().load().key(opponentKey).now();
		if (opponent == null) {
			// No such player exists in the data store
			connectPlayer(opponentId, opponentName);
			opponent = ofy().load().key(opponentKey).now();
			// return new Boolean(false);
		}

		Match match = new Match(opponentKey, playerKey, INITIAL_STATE_STRING);
		Key<Match> matchKey = ofy().save().entity(match).now();
		player.addMatch(matchKey);
		opponent.addMatch(matchKey);

		// Send both players matchInfo message
		MatchInfo matchInfo = new MatchInfo();
		matchInfo.setMatchId(match.getMatchId() + "");
		matchInfo.setNorthPlayerId(opponent.getId());
		matchInfo.setNorthPlayerName(opponent.getPlayerName());
		matchInfo.setNorthPlayerRating(decimal.format(opponent.getRating()));
		matchInfo.setNorthPlayerRD(decimal.format(opponent.getRD()));
		matchInfo.setSouthPlayerId(player.getId());
		matchInfo.setSouthPlayerName(player.getPlayerName());
		matchInfo.setSouthPlayerRating(decimal.format(player.getRating()));
		matchInfo.setSouthPlayerRD(decimal.format(player.getRD()));
		matchInfo.setState(match.getState());
		matchInfo.setMoveIndex(-1 + "");
		matchInfo.setUserIdOfWhoseTurnItIs(player.getId());
		matchInfo.setStartDate(match.getStartDate() + "");
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

	@Override
	public void deleteMatch(String playerId, Long matchId) {
		Key<Player> playerKey = Key.create(Player.class, playerId);
		Player player = ofy().load().key(playerKey).now();

		Key<Match> matchKey = Key.create(Match.class, matchId);
		Match match = ofy().load().key(matchKey).now();
		if (match != null) {
			// Remove match from this player
			player.removeMatch(matchKey);

			Player opponent = ofy().load().key(match.getOpponent(playerKey)).now();
			opponent.removeMatch(matchKey);

			// Delete the match from the datastore, if opponent has also deleted it
			ofy().delete().entity(match);

			ofy().save().entities(player, opponent, match).now();
		}
	}

	@Override
	public String changeMatch(String playerId, Long matchId) {

		if (matchId == null) {
			return "noMatch";
		}

		Key<Player> playerKey = Key.create(Player.class, playerId);
		Player player = ofy().load().key(playerKey).now();

		Key<Match> matchKey = Key.create(Match.class, matchId);
		Match match = ofy().load().key(matchKey).now();

		Key<Player> opponentKey = match.getOpponent(playerKey);
		Player opponent = ofy().load().key(opponentKey).now();

		MatchInfo matchInfo = new MatchInfo();
		matchInfo.setMatchId(matchId + "");
		Player northPlayer, southPlayer;
		if (match.isNorthPlayer(playerKey)) {
			northPlayer = player;
			southPlayer = opponent;
		}
		else {
			northPlayer = opponent;
			southPlayer = player;
		}
		matchInfo.setNorthPlayerId(northPlayer.getId());
		matchInfo.setNorthPlayerName(northPlayer.getPlayerName());
		matchInfo.setNorthPlayerRating(decimal.format(northPlayer.getRating()));
		matchInfo.setNorthPlayerRD(decimal.format(northPlayer.getRD()));
		matchInfo.setSouthPlayerId(southPlayer.getId());
		matchInfo.setSouthPlayerName(southPlayer.getPlayerName());
		matchInfo.setSouthPlayerRating(decimal.format(southPlayer.getRating()));
		matchInfo.setSouthPlayerRD(decimal.format(southPlayer.getRD()));
		matchInfo.setState(match.getState());
		matchInfo.setMoveIndex(-1 + "");

		if ((match.isNorthPlayer(playerKey) && match.isNorthsTurn()) || (match.isSouthPlayer(playerKey) && !match.isNorthsTurn())) {
			matchInfo.setUserIdOfWhoseTurnItIs(player.getId());
		}
		else {
			matchInfo.setUserIdOfWhoseTurnItIs(opponent.getId());
		}
		matchInfo.setStartDate(match.getStartDate() + "");
		matchInfo.setAction("changeMatch");

		String message = MatchInfo.serialize(matchInfo);

		ofy().save().entities(match, player, opponent).now();
		return message;

	}

	@Override
	public void makeMove(String playerId, Long matchId, Integer chosenIndex, String state) {

		Key<Match> matchKey = Key.create(Match.class, matchId);
		Match match = ofy().load().key(matchKey).now();
		match.setState(state);

		Key<Player> playerKey = Key.create(Player.class, playerId);
		Player player = ofy().load().key(playerKey).now();

		Key<Player> opponentKey = match.getOpponent(playerKey);
		Player opponent = ofy().load().key(opponentKey).now();

		ofy().save().entities(match, opponent, player).now();

		State currentState = State.deserialize(match.getState());
		if (currentState.isGameOver())
			changeGeckoRatings(currentState, match, playerKey, opponentKey, player, opponent);

		MatchInfo matchInfo = new MatchInfo();
		matchInfo.setMatchId(matchId + "");
		Player northPlayer, southPlayer;
		if (match.isNorthPlayer(playerKey)) {
			northPlayer = player;
			southPlayer = opponent;
		}
		else {
			northPlayer = opponent;
			southPlayer = player;
		}
		matchInfo.setNorthPlayerId(northPlayer.getId());
		matchInfo.setNorthPlayerName(northPlayer.getPlayerName());
		matchInfo.setNorthPlayerRating(decimal.format(northPlayer.getRating()));
		matchInfo.setNorthPlayerRD(decimal.format(northPlayer.getRD()));
		matchInfo.setSouthPlayerId(southPlayer.getId());
		matchInfo.setSouthPlayerName(southPlayer.getPlayerName());
		matchInfo.setSouthPlayerRating(decimal.format(southPlayer.getRating()));
		matchInfo.setSouthPlayerRD(decimal.format(southPlayer.getRD()));
		matchInfo.setState(state);
		matchInfo.setMoveIndex(chosenIndex + "");
		if ((match.isNorthPlayer(playerKey) && match.isNorthsTurn()) || (match.isSouthPlayer(playerKey) && !match.isNorthsTurn())) {
			matchInfo.setUserIdOfWhoseTurnItIs(player.getId());
		}
		else {
			matchInfo.setUserIdOfWhoseTurnItIs(opponent.getId());
		}
		matchInfo.setStartDate(match.getStartDate() + "");
		matchInfo.setAction("move");
		String message = MatchInfo.serialize(matchInfo);

		Set<String> tokens1 = player.getConnectedTokens();
		for (String connection : tokens1) {
			channelService.sendMessage(new ChannelMessage(connection, message));
		}
		Set<String> tokens2 = opponent.getConnectedTokens();
		for (String connection : tokens2) {
			channelService.sendMessage(new ChannelMessage(connection, message));
		}

	}

	private void changeGeckoRatings(State state, Match match, Key<Player> playerKey, Key<Player> opponentKey, Player player,
			Player opponent) {

		double s = 0;
		if (state.winner() != null) {
			if (state.winner().isNorth()) {
				s = match.isNorthPlayer(playerKey) ? 1.0 : 0.0;
			}
			else if (state.winner().isSouth()) {
				s = match.isSouthPlayer(playerKey) ? 1.0 : 0.0;
			}
		}
		else { // Draw
			s = 0.5;
		}

		Date today = new Date();
		int t = GlickoRating.getNumDays(new Date(match.getStartDate()), today);
		double playerNewRD = GlickoRating.newRD(player.getRating(), player.getRD(), opponent.getRating(), opponent.getRD(), t);
		double opponentNewRD = GlickoRating.newRD(opponent.getRating(), opponent.getRD(), player.getRating(), player.getRD(), t);
		double playerNewRating = GlickoRating.newRating(player.getRating(), player.getRD(), opponent.getRating(), opponent.getRD(),
				s, t);
		double opponentNewRating = GlickoRating.newRating(opponent.getRating(), opponent.getRD(), player.getRating(), player.getRD(),
				1 - s, t);
		player.setRating(playerNewRating);
		player.setRD(playerNewRD);
		opponent.setRating(opponentNewRating);
		opponent.setRD(opponentNewRD);
	}

	@Override
	public String registerAiMatch(String playerId, boolean aiIsNorth) {

		Key<Player> playerKey = Key.create(Player.class, playerId);
		Player player = ofy().load().key(playerKey).now();

		Key<Player> aiKey = Key.create(Player.class, "AI");
		Player aiPlayer = ofy().load().key(aiKey).now();
		if (aiPlayer == null) { // register aiplayer in the server
			aiPlayer = new Player("AI", "AI");
		}
		ofy().save().entity(aiPlayer).now();

		Match match;
		if (aiIsNorth) {
			match = new Match(aiKey, playerKey, INITIAL_STATE_STRING);
		}
		else {
			match = new Match(playerKey, aiKey, INITIAL_STATE_STRING);
		}
		match.setSingleGame(true);
		Key<Match> matchKey = ofy().save().entity(match).now();

		Long matchDate = match.getStartDate();
		player.addMatch(matchKey);
		String message1 = "newAIgame#" + match.getMatchId() + "#S#" + matchDate;
		ofy().save().entities(player, match).now();
		return message1;

	}

	@Override
	public void saveAiMove(Long matchId, String moveString, String stateString) {

		Key<Match> matchKey = Key.create(Match.class, matchId);
		Match match = ofy().load().key(matchKey).now();

		match.setState(stateString);
		ofy().save().entity(match).now();

	}

	// @Override
	// public String connectPlayer() {
	// if (userService.isUserLoggedIn()) {
	// User user = userService.getCurrentUser();
	//
	// String channelId = user.getEmail().toLowerCase();
	//
	// Key<Player> playerKey = Key.create(Player.class, user.getEmail().toLowerCase());
	// Player player = ofy().load().key(playerKey).now();
	//
	// if (player == null) { // New player
	// player = new Player(user.getEmail().toLowerCase(), user.getNickname());
	// }
	// double rating = player.getRating();
	// double RD = player.getRD();
	// ofy().save().entity(player).now();
	//
	// String token = channelService.createChannel(channelId) + "#" + decimal.format(rating) + "#" + decimal.format(RD);
	// return token;
	// }
	// return null;
	// }

	// @Override
	// public String[] loadMatches() {
	// if (userService.isUserLoggedIn()) {
	// User user = userService.getCurrentUser();
	//
	// Key<Player> playerKey = Key.create(Player.class, user.getEmail().toLowerCase());
	// Player player = ofy().load().key(playerKey).now();
	//
	// String[] matches = new String[player.getMatchesList().size()];
	// Set<Key<Match>> matchesList = player.getMatchesList();
	//
	// int index = 0;
	// for (Key<Match> matchKey : matchesList) {
	// Match match = ofy().load().key(matchKey).now();
	// if (match == null) {
	// continue;
	// }
	// Key<Player> opponentKey = match.getOpponent(playerKey);
	// Player opponent = ofy().load().key(opponentKey).now();
	//
	// MatchInfo matchInfo = new MatchInfo();
	// matchInfo.setMatchId(match.getMatchId() + "");
	// Player northPlayer, southPlayer;
	// if (match.isNorthPlayer(playerKey)) {
	// northPlayer = player;
	// southPlayer = opponent;
	// }
	// else {
	// northPlayer = opponent;
	// southPlayer = player;
	// }
	// matchInfo.setNorthPlayerId(northPlayer.getEmail());
	// matchInfo.setNorthPlayerName(northPlayer.getPlayerName());
	// matchInfo.setNorthPlayerRating(decimal.format(northPlayer.getRating()));
	// matchInfo.setNorthPlayerRD(decimal.format(northPlayer.getRD()));
	// matchInfo.setSouthPlayerId(southPlayer.getEmail());
	// matchInfo.setSouthPlayerName(southPlayer.getPlayerName());
	// matchInfo.setSouthPlayerRating(decimal.format(southPlayer.getRating()));
	// matchInfo.setSouthPlayerRD(decimal.format(southPlayer.getRD()));
	//
	// matchInfo.setState(match.getState());
	// matchInfo.setMoveIndex(-1 + "");
	// if ((match.isNorthPlayer(playerKey) && match.isNorthsTurn()) || (match.isSouthPlayer(playerKey) && !match.isNorthsTurn())) {
	// matchInfo.setUserIdOfWhoseTurnItIs(player.getEmail());
	// }
	// else {
	// matchInfo.setUserIdOfWhoseTurnItIs(opponent.getEmail());
	// }
	// matchInfo.setStartDate(match.getStartDate() + "");
	// matchInfo.setAction("loadMatches");
	// String matchInfoString = MatchInfo.serialize(matchInfo);
	//
	// matches[index++] = matchInfoString;
	//
	// ofy().save().entities(match, opponent).now();
	// }
	// ofy().save().entity(player).now();
	// return matches;
	// }
	// return null;
	// }

	// @Override
	// public String changeMatch(Long matchId) {
	// if (userService.isUserLoggedIn()) {
	// if (matchId == null) {
	// return "noMatch";
	// }
	//
	// User user = userService.getCurrentUser();
	//
	// Key<Player> playerKey = Key.create(Player.class, user.getEmail().toLowerCase());
	// Player player = ofy().load().key(playerKey).now();
	//
	// Key<Match> matchKey = Key.create(Match.class, matchId);
	// Match match = ofy().load().key(matchKey).now();
	//
	// Key<Player> opponentKey = match.getOpponent(playerKey);
	// Player opponent = ofy().load().key(opponentKey).now();
	//
	// MatchInfo matchInfo = new MatchInfo();
	// matchInfo.setMatchId(matchId + "");
	// Player northPlayer, southPlayer;
	// if (match.isNorthPlayer(playerKey)) {
	// northPlayer = player;
	// southPlayer = opponent;
	// }
	// else {
	// northPlayer = opponent;
	// southPlayer = player;
	// }
	// matchInfo.setNorthPlayerId(northPlayer.getEmail());
	// matchInfo.setNorthPlayerName(northPlayer.getPlayerName());
	// matchInfo.setNorthPlayerRating(decimal.format(northPlayer.getRating()));
	// matchInfo.setNorthPlayerRD(decimal.format(northPlayer.getRD()));
	// matchInfo.setSouthPlayerId(southPlayer.getEmail());
	// matchInfo.setSouthPlayerName(southPlayer.getPlayerName());
	// matchInfo.setSouthPlayerRating(decimal.format(southPlayer.getRating()));
	// matchInfo.setSouthPlayerRD(decimal.format(southPlayer.getRD()));
	// matchInfo.setState(match.getState());
	// matchInfo.setMoveIndex(-1 + "");
	//
	// if ((match.isNorthPlayer(playerKey) && match.isNorthsTurn()) || (match.isSouthPlayer(playerKey) && !match.isNorthsTurn())) {
	// matchInfo.setUserIdOfWhoseTurnItIs(player.getEmail());
	// }
	// else {
	// matchInfo.setUserIdOfWhoseTurnItIs(opponent.getEmail());
	// }
	// matchInfo.setStartDate(match.getStartDate() + "");
	// matchInfo.setAction("changeMatch");
	//
	// String message = MatchInfo.serialize(matchInfo);
	//
	// ofy().save().entities(match, player, opponent).now();
	// return message;
	// }
	// return null;
	// }

	// @Override
	// public void deleteMatch(Long matchId) {
	// if (userService.isUserLoggedIn()) {
	// User user = userService.getCurrentUser();
	//
	// Key<Player> playerKey = Key.create(Player.class, user.getEmail().toLowerCase());
	// Player player = ofy().load().key(playerKey).now();
	//
	// Key<Match> matchKey = Key.create(Match.class, matchId);
	//
	// // Remove match from this player
	// player.removeMatch(matchKey);
	//
	// ofy().save().entity(player).now();
	//
	// Match match = ofy().load().key(matchKey).now();
	//
	// Player opponent = ofy().load().key(match.getOpponent(playerKey)).now();
	//
	// if (!opponent.containsMatchKey(matchKey)) {
	// // Delete the match from the datastore, if opponent has also deleted it
	// ofy().delete().entity(match);
	// }
	// ofy().save().entities(match, opponent).now();
	// }
	// }
}