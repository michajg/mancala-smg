package org.mancala.client;

import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class LoginPanel extends PopupPanel {
	private VerticalPanel loginPanel = new VerticalPanel();
	private Label loginLabel = new Label("Please sign in to your Google Account to access Mancala.");
	private Anchor signInLink = new Anchor("Sign In");
	private Anchor signOutLink = new Anchor("Sign Out");
	
	public LoginPanel(String url){
		signInLink.setHref(url);
		loginPanel.add(loginLabel);
		loginPanel.add(signInLink);
		this.setGlassEnabled(true);
		this.setAnimationEnabled(true);
		this.setWidget(loginPanel);
	}
}