package org.mancala.shared;

/**
 * @author yzibin@google.com (Yoav Zibin) adapted by Micha Guthmann
 */
public enum PlayerColor {
	  NORTH, SOUTH; 

	  public static final PlayerColor E = null; 
	  public static final PlayerColor N = NORTH;
	  public static final PlayerColor S = SOUTH;

	  public PlayerColor getOpposite() {
	    return this == NORTH ? SOUTH : NORTH;
	  }

	  public boolean isNorth() {
	    return this == NORTH;
	  }
	  
	  public boolean isSouth() {
	    return this == SOUTH;
	  }

	  @Override
	  public String toString() {
	    return isNorth() ? "N" : isSouth() ? "S" : "E";
	  }
	  
	  public static PlayerColor intToColor(int i) {
	    return PlayerColor.values()[i];
	  }
}
