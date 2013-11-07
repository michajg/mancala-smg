package org.mancala.client.services;

import org.mancala.client.LoginInfo;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface LoginServiceAsync {
	void login(String requestUri, AsyncCallback<LoginInfo> callback);

}