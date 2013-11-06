package org.mancala.shared;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.mancala.shared.AlphaBetaPruning.MoveScore;

import com.google.common.collect.Sets;
import com.google.common.collect.Lists;

/**
 * Contains methods to calculate the value of a particular game state
 * 
 * @author Micha Guthman - special thanks to Harsh from which I got a lot of inspiration from
 * 
 */
public class Heuristic {

	public int getStateValue(State state) {
		if (state.isGameOver()) {
			if (state.winner().isNorth()) {
				return Integer.MAX_VALUE - 1;
			}
			else if (state.winner().isSouth()) {
				return Integer.MIN_VALUE + 1;
			}
			else {
				return 0;
			}
		}

		// TODO: refine heuristic
		return state.getNorthPits()[6] - state.getSouthPits()[6];
	}

	/**
	 * Return the best possible moves from this state. Higher captures are ordered first.
	 * 
	 * @param state
	 *          The current state
	 * @return
	 */
	public Iterable<Integer> getOrderedMoves(State state) {
		// System.out.println("Heuristic getOrderedMove " + state);
		Set<Integer> possibleMoves = getPossibleMoves(state);
		List<MoveScore<Integer>> scores = Lists.newArrayList();
		for (Integer possibleMove : possibleMoves) {
			State copyState = state.copyState();
			try {
				// System.out.println("H GOM copystate: " + copyState);
				// System.out.println("H GOM move: " + possibleMove);
				copyState.makeMove(possibleMove);
				int score = (copyState.getWhoseTurn().isNorth()) ? getStateValue(copyState) : -getStateValue(copyState);
				MoveScore<Integer> moveScore = new MoveScore<Integer>();
				moveScore.move = possibleMove;
				moveScore.score = score;
				scores.add(moveScore);
			} catch (IllegalMoveException e) {
				// This is not possible but still included
			}
		}
		Collections.sort(scores);
		List<Integer> orderedMoves = new ArrayList<Integer>();
		for (MoveScore<Integer> moveScore : scores) {
			orderedMoves.add(moveScore.move);
		}
		// System.out.println("H GOM state: " + state);
		// System.out.println("H GOM ordered moves: " + orderedMoves);
		return orderedMoves;
	}

	private Set<Integer> getPossibleMoves(State state) {
		HashSet<Integer> possibleMoves = Sets.newHashSet();
		int[] sideInts = state.getWhoseTurn().isNorth() ? state.getNorthPits() : state.getSouthPits();
		for (int i = 0; i < sideInts.length - 1; i++) {
			if (sideInts[i] > 0)
				possibleMoves.add(new Integer(i));
		}
		// System.out.println("H GPM state: " + state);
		// System.out.println("H GPM possible moves: " + possibleMoves);
		return possibleMoves;
	}

}