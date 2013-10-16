package org.mancala.client;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

public interface GameImages extends ClientBundle {
	@Source("dot.png")
	  ImageResource redSeed();

	@Source("graydot.png")
	  ImageResource graySeed();

	@Source("diagonalhalfemptydot.png")
	  ImageResource seed();

	@Source("Board.png")
	  ImageResource board();
}