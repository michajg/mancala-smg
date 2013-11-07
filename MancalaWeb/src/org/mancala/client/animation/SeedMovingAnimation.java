package org.mancala.client.animation;

import org.mancala.client.Graphics;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.media.client.Audio;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Image;

/**
 * The animation to send one seed from one pit to another.
 * 
 * It works like this: 
 * 1. Remove the seed image from its parent pit panel
 * 2. Add it to the underlying absolute panel at the exact same position
 *    (the absolute panel that is more or less the root for every UI element in the game)
 * 3. Animate inside this absolute panel to the target point
 * 4. Remove Image from parent again
 * 5. Add seed image to the target pit panel
 * 
 * @author Micha Guthmann
 */
public class SeedMovingAnimation extends Animation {

	/**
	 * The seed being animated
	 */
    final Image seed;
    /**
     * the panel from where the seed is animatin in (the root panel)
     */
    AbsolutePanel panel;
    /**
     * Animate the seed from this panel
     */
    AbsolutePanel startPanel;
    /**
     * Animate the seed to this panel
     */
    AbsolutePanel endPanel;
    /**
     * If the animation is changing the dimension of the seed this is needed
     */
    int startWidth; 
    /**
     * If the animation is changing the dimension of the seed this is needed
     */
    int startHeight;
    /**
     * x value where seed lies in the start panel
     */
    int startXStartPanel;
    /**
     * y value where seed lies in the start panel
     */
    int startYStartPanel;
    /**
     * x value where seed will start in the root panel
     */
    int startX;
    /**
     * y value where seed will start in the root panel
     */
    int startY;
    /**
     * x value where seed will end in the root panel
     */
	int endX;
	/**
     * x value where seed will end in the root panel
     */
	int endY; 
	/**
     * x value where seed will lie in the end panel
     */
	int endXEndPanel;
	/**
     * y value where seed will lie in the end panel
     */
	int endYEndPanel;
	
	Audio soundAtEnd;
	boolean cancelled;
	/**
	 * keeps track is this is the last animation of a turn
	 */
	boolean finalAnimation;
	Graphics graphics;
        
    public SeedMovingAnimation(Image seed, ImageResource imgResource, AbsolutePanel startPanel, AbsolutePanel endPanel, int startXStartPanel, int startYStartPanel, int endXEndPanel, int endYEndPanel, int startX, int startY, int endX, int endY, boolean finalAnimation, Graphics graphics, Audio sfx) {
    	this.seed = seed;
        this.seed.setResource(imgResource);
        DOM.setStyleAttribute(seed.getElement(), "backgroundSize", 20 + "px " + 20 + "px");
        
        this.startPanel = startPanel;
        this.endPanel = endPanel;
        panel = (AbsolutePanel) startPanel.getParent().getParent();

        this.startWidth = 20;
        this.startHeight = 20;
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        this.startXStartPanel = startXStartPanel;
        this.startYStartPanel = startYStartPanel;
        this.endXEndPanel = endXEndPanel;
        this.endYEndPanel = endYEndPanel;
        this.finalAnimation = finalAnimation;
        this.graphics = graphics;
        this.soundAtEnd = sfx;
        
        cancelled = false;
        
        seed.removeFromParent();
        panel.add(seed, startX, startY);
        
    }

    /**
     * Play a sound at the start of the animation
     */
    @Override
    protected void onStart() {
    	 super.onStart();
    	 if (!cancelled) {
             if (soundAtEnd != null)
                     soundAtEnd.play();
         }
    }
    
    @Override
    protected void onUpdate(double progress) {

        double scale = 1 + 0.5 * Math.sin(progress * Math.PI); 
    	int x = (int) (startX + (endX - startX) * progress * scale);
        int y = (int) (startY + (endY - startY) * progress * scale);   	
        
//        int width = (int) (startWidth * scale);
//        int height = (int) (startHeight * scale);     
//        DOM.setStyleAttribute(seed.getElement(), "backgroundSize", width + "px " + height + "px");        
//        x -= (width - startWidth) / 2;
//        y -= (height - startHeight) / 2;
        
        panel.setWidgetPosition(seed, x, y);
    }

    @Override
    protected void onCancel() {
    	super.onCancel();
        cancelled = true;
        graphics.afterAnimation();
    }

    @Override
    protected void onComplete() {
    	super.onComplete();

//        if (!cancelled) {
//            if (soundAtEnd != null)
//                    soundAtEnd.play();
//        }
        
    	if(!cancelled) {
    		seed.removeFromParent();
    		
    		endPanel.add(seed, endXEndPanel, endYEndPanel);
    		
    		if(finalAnimation){
    			graphics.afterAnimation();
    		}
    	}
    }
}