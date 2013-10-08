package org.mancala.client;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

public interface GameImages extends ClientBundle {
  @Source("tile_short.png")
  ImageResource shortTile();
  
  @Source("tile_long.png")
  ImageResource longTile();
}