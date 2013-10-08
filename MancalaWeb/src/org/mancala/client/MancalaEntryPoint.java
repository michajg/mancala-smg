package org.mancala.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;

public class MancalaEntryPoint implements EntryPoint {
	@Override
	public void onModuleLoad() {
		Graphics graphics = new Graphics();
		RootPanel.get().add(graphics);
	}
}
