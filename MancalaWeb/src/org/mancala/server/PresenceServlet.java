package org.mancala.server;

import static com.googlecode.objectify.ObjectifyService.ofy;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mancala.shared.Player;

import com.google.appengine.api.channel.ChannelPresence;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.googlecode.objectify.Key;

/**
 * Uses Channel Presence API to maintain a user's tokens
 * 
 * @author Harsh
 * 
 */
public class PresenceServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	ChannelService channelService = ChannelServiceFactory.getChannelService();

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) {
		ChannelPresence channelPresence;
		try {
			channelPresence = channelService.parsePresence(req);
			String channelToken = channelPresence.clientId();
			// String userID = channelToken.substring(0,
			// channelToken.lastIndexOf('-'));
			String userID = channelToken;

			Key<Player> playerKey = Key.create(Player.class, userID);
			Player player = ofy().load().key(playerKey).now();

			if (channelPresence.isConnected()) {
				player.addToken(channelToken);
				ofy().save().entity(player).now();
			}
			else {
				player.removeToken(channelToken);
				ofy().save().entity(player).now();
			}

		} catch (IOException e) {
			System.err.println("Error in presence service: " + e.getMessage());
		}
	}
}
