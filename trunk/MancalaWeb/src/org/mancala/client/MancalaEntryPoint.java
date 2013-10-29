package org.mancala.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootPanel;

public class MancalaEntryPoint implements EntryPoint {
	private LoginInfo loginInfo = null;

	@Override
	public void onModuleLoad() {

		// final Graphics graphics = new Graphics();
		// final Presenter presenter = graphics.presenter;

		LoginServiceAsync loginService = GWT.create(LoginService.class);
		loginService.login(Window.Location.getHref(), new AsyncCallback<LoginInfo>() {
			public void onFailure(Throwable error) {
			}

			public void onSuccess(LoginInfo result) {
				loginInfo = result;
				if (loginInfo.isLoggedIn()) {
					MancalaServiceAsync mancalaService = GWT.create(MancalaService.class);
					mancalaService.connectPlayer(new AsyncCallback<String>() {

						@Override
						public void onFailure(Throwable caught) {
							Window.alert("Error retrieving token! Please try again later.");
						}

						@Override
						public void onSuccess(String result) {
							loadGame(result);
						}

					});
				}
				else {
					LoginPanel lg = new LoginPanel(loginInfo.getLoginUrl());
					lg.center();
				}
			}
		});

	}

	private void loadGame(String token) {

		final Graphics graphics = new Graphics(token, loginInfo.getEmailAddress(), loginInfo.getNickname());

		RootPanel.get().add(graphics);
	}
}
