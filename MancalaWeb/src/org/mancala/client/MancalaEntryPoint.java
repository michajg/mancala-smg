package org.mancala.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.HasRpcToken;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.rpc.XsrfToken;
import com.google.gwt.user.client.rpc.XsrfTokenService;
import com.google.gwt.user.client.rpc.XsrfTokenServiceAsync;
import com.google.gwt.user.client.ui.RootPanel;

public class MancalaEntryPoint implements EntryPoint {
	private LoginInfo loginInfo = null;

	@Override
	public void onModuleLoad() {

		Cookies.setCookie("JSESSIONID", "JSESSIONID", null, null, "/", false);

		LoginServiceAsync loginService = GWT.create(LoginService.class);
		loginService.login(Window.Location.getHref(), new AsyncCallback<LoginInfo>() {
			public void onFailure(Throwable error) {
			}

			public void onSuccess(LoginInfo result) {
				loginInfo = result;
				if (loginInfo.isLoggedIn()) {
					// secured against xsrf attacks
					// see http://www.gwtproject.org/doc/latest/DevGuideSecurityRpcXsrf.html
					XsrfTokenServiceAsync xsrf = (XsrfTokenServiceAsync) GWT.create(XsrfTokenService.class);
					((ServiceDefTarget) xsrf).setServiceEntryPoint(GWT.getModuleBaseURL() + "gwt/xsrf");
					xsrf.getNewXsrfToken(new AsyncCallback<XsrfToken>() {
						public void onSuccess(XsrfToken token) {
							MancalaServiceAsync mancalaService = GWT.create(MancalaService.class);
							((HasRpcToken) mancalaService).setRpcToken(token);

							mancalaService.connectPlayer(new AsyncCallback<String>() {

								@Override
								public void onFailure(Throwable caught) {
									Window.alert("Error retrieving token! Please try again later.");
								}

								@Override
								public void onSuccess(String result) {
									loadGraphics(result);
								}

							});
						}

						public void onFailure(Throwable caught) {
							Window.alert("Error retrieving xsrf token! Please try again later.");
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

	private void loadGraphics(String result) {
		// result is channel token + "#" + rating + "#" + RD;
		String[] msg = result.split("#");
		String token = msg[0];
		String playerRating = msg[1];
		String playerRD = msg[2];

		final Graphics graphics = new Graphics(token, Graphics.sanitize(loginInfo.getEmailAddress()), Graphics.sanitize(loginInfo
				.getNickname()), playerRating, playerRD);

		RootPanel.get().add(graphics);
	}
}
