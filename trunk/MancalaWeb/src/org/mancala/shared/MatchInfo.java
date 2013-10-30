package org.mancala.shared;

public class MatchInfo {
	private String matchId;
	private String northPlayerId;
	private String northPlayerName;
	private String northPlayerRating;
	private String northPlayerRD;
	private String southPlayerId;
	private String southPlayerName;
	private String southPlayerRating;
	private String southPlayerRD;
	private String state;
	private String moveIndex;
	private String userIdOfWhoseTurnItIs;
	private String startDate;
	private String action;

	/**
	 * @return the northPlayerRating
	 */
	public String getNorthPlayerRating() {
		return northPlayerRating;
	}

	/**
	 * @param northPlayerRating
	 *          the northPlayerRating to set
	 */
	public void setNorthPlayerRating(String northPlayerRating) {
		this.northPlayerRating = northPlayerRating;
	}

	/**
	 * @return the northPlayerRD
	 */
	public String getNorthPlayerRD() {
		return northPlayerRD;
	}

	/**
	 * @param northPlayerRD
	 *          the northPlayerRD to set
	 */
	public void setNorthPlayerRD(String northPlayerRD) {
		this.northPlayerRD = northPlayerRD;
	}

	/**
	 * @return the southPlayerRating
	 */
	public String getSouthPlayerRating() {
		return southPlayerRating;
	}

	/**
	 * @param southPlayerRating
	 *          the southPlayerRating to set
	 */
	public void setSouthPlayerRating(String southPlayerRating) {
		this.southPlayerRating = southPlayerRating;
	}

	/**
	 * @return the southPlayerRD
	 */
	public String getSouthPlayerRD() {
		return southPlayerRD;
	}

	/**
	 * @param southPlayerRD
	 *          the southPlayerRD to set
	 */
	public void setSouthPlayerRD(String southPlayerRD) {
		this.southPlayerRD = southPlayerRD;
	}

	public MatchInfo() {
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
	 * @return the startDate
	 */
	public String getStartDate() {
		return startDate;
	}

	/**
	 * @param startDate
	 *          the startDate to set
	 */
	public void setStartDate(String startDate) {
		this.startDate = startDate;
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
		serialized += matchInfo.northPlayerRating + "#";
		serialized += matchInfo.northPlayerRD + "#";
		serialized += matchInfo.southPlayerId + "#";
		serialized += matchInfo.southPlayerName + "#";
		serialized += matchInfo.southPlayerRating + "#";
		serialized += matchInfo.southPlayerRD + "#";
		serialized += matchInfo.state + "#";
		serialized += matchInfo.moveIndex + "#";
		serialized += matchInfo.userIdOfWhoseTurnItIs + "#";
		serialized += matchInfo.startDate + "#";
		serialized += matchInfo.action;

		return serialized;
	}

	public static MatchInfo deserialize(String serialized) {
		String[] serTokens = serialized.trim().split("#");
		MatchInfo mI = new MatchInfo();

		mI.matchId = serTokens[0];
		mI.northPlayerId = serTokens[1];
		mI.northPlayerName = serTokens[2];
		mI.northPlayerRating = serTokens[3];
		mI.northPlayerRD = serTokens[4];
		mI.southPlayerId = serTokens[5];
		mI.southPlayerName = serTokens[6];
		mI.southPlayerRating = serTokens[7];
		mI.southPlayerRD = serTokens[8];
		mI.state = serTokens[9];
		mI.moveIndex = serTokens[10];
		mI.userIdOfWhoseTurnItIs = serTokens[11];
		mI.startDate = serTokens[12];
		mI.action = serTokens[13];

		return mI;
	}

}
