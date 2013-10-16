package org.mancala.client;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.media.client.Audio;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Image;

public class SeedMovingAnimation extends Animation {

    final Image seed;
    AbsolutePanel panel;
    AbsolutePanel startPanel;
    AbsolutePanel endPanel;
    int startWidth; 
    int startHeight;
    int startXStartPanel;
    int startYStartPanel;
    int startX;
    int startY;
	int endX;
	int endY; 
	int endXEndPanel;
	int endYEndPanel;
	
	Audio soundAtEnd;
	boolean cancelled;
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
        graphics.updateBoard();
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
    			graphics.afterFinalAnimation();
    		}
    	}
    }
}