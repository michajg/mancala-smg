//Copyright 2012 Google Inc.
//
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at
//
//http://www.apache.org/licenses/LICENSE-2.0
//
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.
////////////////////////////////////////////////////////////////////////////////
package org.mancala.shared.ai;

import java.util.Collections;
import java.util.List;

//import org.gaming.shared.games.GameOver;
//import org.gaming.shared.games.MatchMove;
//import org.mancala.shared.PlayerColor;
//import org.gaming.shared.games.turn_based.TurnBasedState;


import org.mancala.shared.PlayerColor;
import org.mancala.shared.State;

import com.google.common.collect.Lists;

/**
 * http://en.wikipedia.org/wiki/Alpha-beta_pruning<br>
 * This algorithm performs both A* and alpha-beta pruning.<br>
 * The set of possible moves is maintained ordered by the current heuristic value of each move. We first use depth=1, and update
 * the heuristic value of each move, then use depth=2, and so on until we get a timeout or reach maximum depth. <br>
 * 
 * @author yzibin@google.com (Yoav Zibin) adapted by mijagu@gmail.com (Micha Guthmann)
 */
public class AlphaBetaPruning {
	static class TimeoutException extends RuntimeException {
		private static final long serialVersionUID = 1L;
	}

	static class MoveScore<T> implements Comparable<MoveScore<T>> {
		T move;
		int score;

		@Override
		public int compareTo(MoveScore<T> o) {
			return o.score - score; // sort DESC (best score first)
		}
	}

	private Heuristic heuristic;

	public AlphaBetaPruning(Heuristic heuristic) {
		this.heuristic = heuristic;
	}

	public Integer findBestMove(State state, int depth, Timer timer) {
		// System.out.println("alopha beta findbestmove: " + state);
		// System.out.println("ab FBM move: " + depth);
		boolean isNorth = state.getWhoseTurn().isNorth();
		// Do iterative deepening (A*), and slow get better heuristic values for the states.
		List<MoveScore<Integer>> scores = Lists.newArrayList();
		{
			Iterable<Integer> possibleMoves = heuristic.getOrderedMoves(state);
			for (Integer move : possibleMoves) {
				MoveScore<Integer> score = new MoveScore<Integer>();
				score.move = move;
				score.score = Integer.MIN_VALUE;
				scores.add(score);
			}
		}

		try {
			for (int i = 0; i < depth; i++) {
				for (MoveScore<Integer> moveScore : scores) {
					Integer move = moveScore.move;
					State copyState = state.copyState();
					// System.out.println("ab FBM state: " + state);
					// System.out.println("ab FBM move: " + move);
					copyState.makeMove(move);
					int score = findMoveScore(copyState, i, Integer.MIN_VALUE, Integer.MAX_VALUE, timer);
					if (!isNorth) {
						// the scores are from the point of view of north, so for south we need to switch.
						score = -score;
					}
					moveScore.score = score;
				}
				Collections.sort(scores); // This will give better pruning on the next iteration.
			}
		} catch (TimeoutException e) {
			// Ok should happen
		}

		Collections.sort(scores);
		return scores.get(0).move;
	}

	/**
	 * If we get a timeout, then the score is invalid.
	 */
	private int findMoveScore(State state, int depth, int alpha, int beta, Timer timer) throws TimeoutException {
		// System.out.println("alphabeta findMoveScore");
		// System.out.println("ab FMS depth: " + depth);

		if (timer.didTimeout()) {
			throw new TimeoutException();
		}
		// GameOver over = state.getGameOver();
		// if (depth == 0 || over != null) {
		// return heuristic.getStateValue(state);
		// }
		if (depth == 0 || state.isGameOver()) {
			return heuristic.getStateValue(state);
		}
		PlayerColor side = state.getWhoseTurn();

		Iterable<Integer> possibleMoves = heuristic.getOrderedMoves(state);

		for (Integer move : possibleMoves) {
			// System.out.println("ab FMS possibleMoves: " + possibleMoves);
			// System.out.println("ab FMS state: " + state);
			// System.out.println("ab FMS move: " + move);
			// System.out.println("ab FMS depth: " + depth);
			State copyState = state.copyState();
			copyState.makeMove(move);
			int childScore = findMoveScore(copyState, depth - 1, alpha, beta, timer);
			if (side.isNorth()) {
				alpha = Math.max(alpha, childScore);
				// System.out.println("alpha: " + alpha);
				if (beta <= alpha) {
					break;
				}
			}
			else {
				beta = Math.min(beta, childScore);
				if (beta <= alpha) {
					break;
				}
			}
		}
		return side.isNorth() ? alpha : beta;
	}

	// public static void main(String args[]) {
	// AlphaBetaPruning abp = new AlphaBetaPruning(new Heuristic());
	// State state = State.deserialize("4,4,4,4,4,4,0_4,4,4,4,4,4,0_S_F_F_0");
	// Integer move = abp.findBestMove(state, 4, new DateTimer(3000));
	// System.out.println("#############################################################");
	// System.out.println("#############################################################");
	// System.out.println(move);
	// System.out.println("#############################################################");
	// System.out.println("#############################################################");
	// state.makeMove(move);
	// System.out.println(abp.findBestMove(state, 2, new DateTimer(5000)));
	// }
}
