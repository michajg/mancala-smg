package org.mancala.shared;

import java.util.Date;

/**
 * Negative milliseconds means there will never be a timeout.
 * 
 * @author yzibin@google.com (Yoav Zibin)
 */
public class DateTimer implements Timer {
	private long start;
	private int milliseconds;

	public DateTimer(int milliseconds) {
		this.milliseconds = milliseconds;
		if (milliseconds > 0) {
			start = now();
		}
	}

	public long now() {
		return new Date().getTime();
	}

	@Override
	public boolean didTimeout() {
		return milliseconds <= 0 ? false : now() > start + milliseconds;
	}

}
