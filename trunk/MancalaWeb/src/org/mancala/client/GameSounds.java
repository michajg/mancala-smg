package org.mancala.client;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.DataResource;

public interface GameSounds extends ClientBundle {

//	@Source("boing_spring.mp3")
//    DataResource dotMp3();
//
//    @Source("boing_spring.wav")
//    DataResource dotWav();
    
    @Source("click_x.mp3")
    DataResource dotMp3();

    @Source("click_x.wav")
    DataResource dotWav();
    
    @Source("fanfare_x.mp3")
    DataResource gameOverMp3();

    @Source("fanfare_x.wav")
    DataResource gameOverWav();
    
    @Source("applause_y.mp3")
    DataResource applauseMp3();

    @Source("applause_y.wav")
    DataResource applauseWav();
    
    @Source("thunk.mp3")
    DataResource oppositeCaptureMp3();

    @Source("thunk.wav")
    DataResource oppositeCaptureWav();
        
}