package org.mancala.client;

import com.google.gwt.appengine.channel.client.ChannelError;
import com.google.gwt.appengine.channel.client.ChannelFactoryImpl;
import com.google.gwt.appengine.channel.client.Socket;
import com.google.gwt.appengine.channel.client.SocketListener;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootPanel;

public class MancalaEntryPoint implements EntryPoint {
	private LoginInfo loginInfo = null;
	
	@Override
	public void onModuleLoad() {
		
		final Graphics graphics = new Graphics();
		final Presenter presenter = graphics.presenter;
		
		//Log user in 
		//Set id in presenter
		//Register user with initialServerContact
		//Set channel up
		LoginServiceAsync loginService = GWT.create(LoginService.class);
        loginService.login(GWT.getHostPageBaseURL()+"MancalaWeb.html", new AsyncCallback<LoginInfo>() {
        	public void onFailure(Throwable error) { }
        	
        	public void onSuccess(LoginInfo result) {
			    loginInfo = result;
			    if(loginInfo.isLoggedIn()) {
		            presenter.setId(loginInfo.getEmailAddress());
		            presenter.initialServerContact();
		            //Window.alert("email adress: " + loginInfo.getEmailAddress() +", channelToken: "+ loginInfo.getToken()); 
		         
		            Socket socket = new ChannelFactoryImpl().createChannel(loginInfo.getToken()).open(new SocketListener() {
		            	@Override
                        public void onOpen() {
                        	//Window.alert("Channel opened!");
                        }
                        @Override
                        public void onMessage(String message) {
                        	presenter.setState(Presenter.deserializeState(message));
                        }
                        @Override
                        public void onError(ChannelError error) {
                        	Window.alert("Channel error: " + error.getCode() + " : " + error.getDescription());
                        }
                        @Override
                        public void onClose() {
                        	Window.alert("Channel closed!");
                        }
                    });
			    } else {
			    	LoginPanel lg = new LoginPanel(loginInfo.getLoginUrl());
			    	lg.center();
			    }
        	}
        });
        

		RootPanel.get().add(graphics);
	}
}
