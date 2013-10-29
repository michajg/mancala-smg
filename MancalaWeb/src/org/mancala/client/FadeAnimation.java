package org.mancala.client;

import java.math.BigDecimal;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.user.client.Element;

/**
 * Saw it originally here: http://map-notes.blogspot.com/2012/11/fade-animation.html I added the delay
 */
public class FadeAnimation extends Animation {

	private Element element;
	private double opacityIncrement;
	private double targetOpacity;
	private double baseOpacity;
	private double delayQuotient;

	public FadeAnimation(Element element) {
		this.element = element;
	}

	@Override
	protected void onUpdate(double progress) {
		if (progress > delayQuotient)
			element.getStyle().setOpacity(baseOpacity + (progress - delayQuotient) * (opacityIncrement) * (1 / (1 - delayQuotient)));
	}

	@Override
	protected void onComplete() {
		super.onComplete();
		element.getStyle().setOpacity(targetOpacity);
	}

	public void fade(int duration, double targetOpacity, double delay) {
		double durationDouble = duration;
		this.delayQuotient = delay / durationDouble;
		if (targetOpacity > 1.0) {
			targetOpacity = 1.0;
		}
		if (targetOpacity < 0.0) {
			targetOpacity = 0.0;
		}
		this.targetOpacity = targetOpacity;
		String opacityStr = element.getStyle().getOpacity();
		try {
			baseOpacity = new BigDecimal(opacityStr).doubleValue();
			opacityIncrement = targetOpacity - baseOpacity;
			run(duration);
		} catch (NumberFormatException e) {
			// set opacity directly
			onComplete();
		}
	}

}