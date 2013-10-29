package org.mancala.shared;

public class MatchInfo {
	private String matchId;
	private String northPlayerId;
	private String northPlayerName;
	private String southPlayerId;
	private String southPlayerName;
	private String state;
	private String moveIndex;
	private String userIdOfWhoseTurnItIs;
	private String action;

	public MatchInfo() {
	}

	public MatchInfo(String matchId, String playerId, String playerName, String opponentId, String opponentName, String state,
			String moveIndex, String userIdOfWhoseTurnItIs, String action) {
		this.matchId = matchId;
		this.northPlayerId = playerId;
		this.northPlayerName = playerName;
		this.southPlayerId = opponentId;
		this.southPlayerName = opponentName;
		this.state = state;
		this.moveIndex = moveIndex;
		this.userIdOfWhoseTurnItIs = userIdOfWhoseTurnItIs;
		this.action = action;
	}

	/**
	 * @return the matchId
	 */
	public String getMatchId() {
		return matchId;
	}

	/**
	 * @param matchId
	 *          the matchId to set
	 */
	public void setMatchId(String matchId) {
		this.matchId = matchId;
	}

	/**
	 * @return the northPlayerId
	 */
	public String getNorthPlayerId() {
		return northPlayerId;
	}

	/**
	 * @param northPlayerId
	 *          the northPlayerId to set
	 */
	public void setNorthPlayerId(String northPlayerId) {
		this.northPlayerId = northPlayerId;
	}

	/**
	 * @return the northPlayerName
	 */
	public String getNorthPlayerName() {
		return northPlayerName;
	}

	/**
	 * @param northPlayerName
	 *          the northPlayerName to set
	 */
	public void setNorthPlayerName(String northPlayerName) {
		this.northPlayerName = northPlayerName;
	}

	/**
	 * @return the southPlayerId
	 */
	public String getSouthPlayerId() {
		return southPlayerId;
	}

	/**
	 * @param southPlayerId
	 *          the southPlayerId to set
	 */
	public void setSouthPlayerId(String southPlayerId) {
		this.southPlayerId = southPlayerId;
	}

	/**
	 * @return the southPlayerName
	 */
	public String getSouthPlayerName() {
		return southPlayerName;
	}

	/**
	 * @param southPlayerName
	 *          the southPlayerName to set
	 */
	public void setSouthPlayerName(String southPlayerName) {
		this.southPlayerName = southPlayerName;
	}

	/**
	 * @return the state
	 */
	public String getState() {
		return state;
	}

	/**
	 * @param state
	 *          the state to set
	 */
	public void setState(String state) {
		this.state = state;
	}

	/**
	 * @return the moveIndex
	 */
	public String getMoveIndex() {
		return moveIndex;
	}

	/**
	 * @param moveIndex
	 *          the moveIndex to set
	 */
	public void setMoveIndex(String moveIndex) {
		this.moveIndex = moveIndex;
	}

	/**
	 * @return the userIdOfWhoseTurnItIs
	 */
	public String getUserIdOfWhoseTurnItIs() {
		return userIdOfWhoseTurnItIs;
	}

	/**
	 * @param userIdOfWhoseTurnItIs
	 *          the userIdOfWhoseTurnItIs to set
	 */
	public void setUserIdOfWhoseTurnItIs(String userIdOfWhoseTurnItIs) {
		this.userIdOfWhoseTurnItIs = userIdOfWhoseTurnItIs;
	}

	/**
	 * @return the action
	 */
	public String getAction() {
		return action;
	}

	/**
	 * @param action
	 *          the action to set
	 */
	public void setAction(String action) {
		this.action = action;
	}

	public static String serialize(MatchInfo matchInfo) {
		String serialized = "";

		serialized += matchInfo.matchId + "#";
		serialized += matchInfo.northPlayerId + "#";
		serialized += matchInfo.northPlayerName + "#";
		serialized += matchInfo.southPlayerId + "#";
		serialized += matchInfo.southPlayerName + "#";
		serialized += matchInfo.state + "#";
		serialized += matchInfo.moveIndex + "#";
		serialized += matchInfo.userIdOfWhoseTurnItIs + "#";
		serialized += matchInfo.action;

		return serialized;
	}

	public static MatchInfo deserialize(String serialized) {
		String[] serTokens = serialized.split("#");

		String matchId = serTokens[0];
		String northPlayerId = serTokens[1];
		String northPlayerName = serTokens[2];
		String southPlayerId = serTokens[3];
		String southPlayerName = serTokens[4];
		String state = serTokens[5];
		String moveIndex = serTokens[6];
		String userIdOfWhoseTurnItIs = serTokens[7];
		String action = serTokens[8].trim();

		return new MatchInfo(matchId, northPlayerId, northPlayerName, southPlayerId, southPlayerName, state, moveIndex,
				userIdOfWhoseTurnItIs, action);
	}

}
