package org.mancala.client;

import org.mancala.client.gwtfb.sdk.FBCore;
import org.mancala.client.gwtfb.sdk.FBEvent;
import org.mancala.client.gwtfb.sdk.FBXfbml;
import org.mancala.client.services.MancalaService;
import org.mancala.client.services.MancalaServiceAsync;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.HasRpcToken;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.rpc.XsrfToken;
import com.google.gwt.user.client.rpc.XsrfTokenService;
import com.google.gwt.user.client.rpc.XsrfTokenServiceAsync;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class MancalaEntryPoint implements EntryPoint {
	private static final String ApiKey = "539265309500529";

	private FBCore fbCore = GWT.create(FBCore.class);
	private FBEvent fbEvent = GWT.create(FBEvent.class);

	private boolean status = true;
	private boolean xfbml = true;
	private boolean cookie = true;

	@Override
	public void onModuleLoad() {
		fbCore.init(ApiKey, status, cookie, xfbml);

		//
		// Callback used when session status is changed
		//
		class SessionChangeCallback extends Callback<JavaScriptObject> {
			public void onSuccess(JavaScriptObject response) {
				// Make sure cookie is set so we can use the non async method
				renderHomeView();
			}

			@Override
			public void onFailure(Throwable caught) {
				Window.alert(caught + "");
			}
		}

		//
		// Get notified when user session is changed
		//
		fbEvent.subscribe("auth.statusChange", new SessionChangeCallback());

		// Callback used when checking login status
		class LoginStatusCallback extends Callback<JavaScriptObject> {
			public void onSuccess(JavaScriptObject response) {
				// renderApp(Window.Location.getHash());
				renderHomeView();
			}

			@Override
			public void onFailure(Throwable caught) {
				Window.alert(caught + "");
			}

		}

		// Get login status
		fbCore.getLoginStatus(new LoginStatusCallback());

		Cookies.setCookie("JSESSIONID", "JSESSIONID", null, null, "/", false);

	}

	/**
	 * Display User info
	 */
	class MeCallback extends Callback<JavaScriptObject> {
		public void onSuccess(JavaScriptObject response) {
			renderMe(response);
		}
	}

	/**
	 * Render information about logged in user
	 */
	private void renderMe(JavaScriptObject response) {
		JSOModel jso = response.cast();
		// if (RootPanel.get().getWidget(0) != null)
		// RootPanel.get().remove(0);
		// RootPanel.get().add(new HTML("<h3> Hi,  " + jso.get("name") + "</h3>"));

		final String id = jso.get("id");
		final String name = jso.get("name");
		// secured against xsrf attacks
		// see http://www.gwtproject.org/doc/latest/DevGuideSecurityRpcXsrf.html
		XsrfTokenServiceAsync xsrf = (XsrfTokenServiceAsync) GWT.create(XsrfTokenService.class);
		((ServiceDefTarget) xsrf).setServiceEntryPoint(GWT.getModuleBaseURL() + "gwt/xsrf");
		xsrf.getNewXsrfToken(new AsyncCallback<XsrfToken>() {
			public void onSuccess(XsrfToken token) {
				MancalaServiceAsync mancalaService = GWT.create(MancalaService.class);
				((HasRpcToken) mancalaService).setRpcToken(token);

				mancalaService.connectPlayer(id, name, new AsyncCallback<String>() {

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

	private void loadGraphics(String result) {
		// result is channel token + "#" + fbid + "#" + name + "#" + rating + "#" + RD;
		String[] msg = result.split("#");
		String token = msg[0];
		String id = msg[1];
		String name = msg[2];
		String playerRating = msg[3];
		String playerRD = msg[4];

		final Graphics graphics = new Graphics(fbCore, token, Graphics.sanitize(id), Graphics.sanitize(name), playerRating, playerRD);

		if (RootPanel.get().getWidgetCount() > 0)
			RootPanel.get().remove(0);
		RootPanel.get().add(graphics);
	}

	/**
	 * Render home view. If user is logged in display welcome message, otherwise display login dialog.
	 */
	private void renderHomeView() {
		// RootPanel.get().clear();

		if (fbCore.getAuthResponse() == null) {
			renderWhenNotLoggedIn();
		}
		else {
			renderWhenLoggedIn();
		}
	}

	/**
	 * Render GUI when logged in
	 */
	private void renderWhenLoggedIn() {
		fbCore.api("/me", new MeCallback());

		FBXfbml.parse();
	}

	/**
	 * Render GUI when not logged in
	 */
	private void renderWhenNotLoggedIn() {
		// mainView.setWidget(new FrontpageViewController());
		FBXfbml.parse();
	}
}
