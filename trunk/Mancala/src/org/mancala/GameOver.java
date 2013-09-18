package org.mancala;

import java.util.Arrays;

/**
 * @author yzibin@google.com (Yoav Zibin) - adapted by Micha Guthmann
 */
public class GameOver {
	  
	  public static final GameOver SOUTH_WIN = new GameOver(1, -1);
	  public static final GameOver NORTH_WIN = new GameOver(-1, 1);
	  public static final GameOver TIE = new GameOver(0, 0);

	  public final int[] scores;
	  /**
	   * Describes why the game ended.
	   * E.g., in chess the game can end because of:
	   * - checkmate
	   * - 50 moves rule
	   * - threefold-repetition rule
	   */
	  public String reason;
	  
	  
	  public GameOver() {
	    scores = new int[0];
	  }
	  
	  public GameOver(int southScore, int northScore) {
	    scores = new int[2];
	    this.scores[0] = southScore;
	    this.scores[1] = northScore;
	  }

	  public GameOver(int[] scores) {
	    this.scores = scores;
	  }

	  public int getSouthScore() {
	    return scores[0];
	  }

	  public int getNorthScore() {
	    return scores[1];
	  }

	  @Override
	  public String toString() {
	    return "scores: " + Arrays.toString(scores);
	  }
}
