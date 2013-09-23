package org.mancala;

/**
 * @author yzibin@google.com (Yoav Zibin) adapted by Micha Guthmann
 */
public enum PlayerColor {
	  SOUTH, NORTH; 

	  public static final PlayerColor E = null; 
	  public static final PlayerColor S = SOUTH;
	  public static final PlayerColor N = NORTH;

	  public PlayerColor getOpposite() {
	    //checkArgument(this == SOUTH || this == NORTH);
	    return this == SOUTH ? NORTH : SOUTH;
	  }

	  public boolean isSouth() {
	    return this == SOUTH;
	  }

	  public boolean isNorth() {
	    return this == NORTH;
	  }

	  @Override
	  public String toString() {
	    return isSouth() ? "S" : isNorth() ? "N" : "" + ordinal();
	  }
	  
	  public static PlayerColor intToColor(int i) {
	    return PlayerColor.values()[i];
	  }
}
