package org.mancala.client;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.mancala.client.Presenter;
import org.mancala.client.Presenter.View;
import org.mancala.shared.PlayerColor;
import org.mancala.shared.State;
import org.mockito.Mockito;

import com.google.gwt.junit.client.GWTTestCase;

public class PresenterTest extends GWTTestCase{
	Presenter presenter;
	View graphics;

	public final int[] STANDARD_PITS = {4,4,4,4,4,4,0};
	
	@Before
    public void setup(){
		graphics = Mockito.mock(Presenter.View.class);
        presenter = new Presenter(graphics);
    }

	@Test
	public void testSetPits(){
		State state = new State(STANDARD_PITS.clone(), STANDARD_PITS.clone(), PlayerColor.S, false);
		presenter.setState(state);
		Mockito.verify(graphics).setSeeds(0, PlayerColor.S, 4);
	}

	@Override
	public String getModuleName() {
		return "mancalaweb";
	}

}
