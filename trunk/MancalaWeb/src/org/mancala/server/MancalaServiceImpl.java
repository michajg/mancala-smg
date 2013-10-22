package org.mancala.server;


import java.util.HashMap;
import java.util.Map;

import org.mancala.client.MancalaService;

import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class MancalaServiceImpl extends RemoteServiceServlet implements MancalaService{
    private String wait="";
    private Map<String,String> hash = new HashMap<String,String>();
    ChannelService channelService = ChannelServiceFactory.getChannelService();
    
    /**
	 * Submit a move to the server
	 * 
	 * @param state the state that the game is in after the move
	 * @param id the user id
	 * @return the state in String format
	 */
    @Override
    public String SubMove(String state, String id) {
        ChannelService channelService = ChannelServiceFactory.getChannelService();
        if(hash.containsKey(id)){
        	String op = hash.get(id);
        	channelService.sendMessage(new ChannelMessage(op, state));
        	channelService.sendMessage(new ChannelMessage(id, state));
        }
        else if(hash.values().contains(id)){
        	String op = "";
            for (String key: hash.keySet()){
            	if(hash.get(key).equals(id)) 
            		op = key;
            }
            channelService.sendMessage(new ChannelMessage(op, state));
        	channelService.sendMessage(new ChannelMessage(id, state));
        }
        else if (wait.equals("")){ 
	        wait = id;
	        channelService.sendMessage(new ChannelMessage(id, "4,4,4,4,4,4,0_4,4,4,4,4,4,0_S_F_F_0"));
        }
        else {
            if(!wait.equals(id)){
	            hash.put(wait, id);
	            wait="";
	            channelService.sendMessage(new ChannelMessage(wait, state));
			    channelService.sendMessage(new ChannelMessage(id, state));
		    }else{
		    	channelService.sendMessage(new ChannelMessage(id, "4,4,4,4,4,4,0_4,4,4,4,4,4,0_S_F_F_0"));
		    }
        }
        
        return state;
    }

    /**
	 * Register a player at the server
	 * 
	 * @param id user id
	 * @return a string[2] array - the first entry is to give the user some information, 
	 * the second contains as which player (North or South) the player registered 
	 */
	@Override
	public String[] AddPlayer(String id) {
		String[] result = new String[2];
		if(hash.containsKey(id))
			if(hash.get(id).equals("")) {
				result[0] = "You're " + id + ". You're playing on the South side";// + "\n for test: " + hash.toString();
				result[1] = "S";
				return result;
			}
			else {
				result[0] = "You're " + id + " and you're playing against " + hash.get(id) + ". You're playing on the South side";//"\n for test: " + hash.toString();
				result[1] = "S";
				return result;
			}
				
		if(hash.values().contains(id)){
			for (String key : hash.keySet()){
				if(hash.get(key).equals(id)) {
					result[0] = "You're " + id + " and you're playing against " + key + ". You're playing on the North side";// + "\n for test: " + hash.toString();
					result[1] = "N";
					return result;
				}
			}
			result[0] = "You're " + id + ". You're playing on the North side";// + "\n for test: " + hash.toString();
			result[1] = "N";
			return result;
		}
	
		if(wait.equals(id)){
			result[0] = "You're " + id + ". You're playing on the South side";// + "\n for test: " + hash.toString();
			result[1] = "S";
			return result;
		}
		
		if (wait.equals("")){ 
	        wait = id;
	        result[0] = "You're " + id + ". You're playing on the South side";// + "\n for test: " + hash.toString();
	        result[1] = "S";
	        return result;
	    }
	    else {
            hash.put(wait, id);
            String tempS = wait;
            wait="";
            result[0] = "You're " + id + " and you're playing against " + tempS + ". You're playing on the North side";// + "\n for test: " + hash.toString();
            result[1] = "N";
            return result;
	    }
	    
		
	}
}