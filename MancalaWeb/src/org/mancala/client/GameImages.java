package org.mancala.client;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

public interface GameImages extends ClientBundle {
	@Source("img/dot.png")
	  ImageResource redSeed();

	@Source("img/graydot.png")
	  ImageResource graySeed();

	@Source("img/diagonalhalfemptydot.png")
	  ImageResource seed();

	@Source("img/Board1.png")
	  ImageResource board();
}