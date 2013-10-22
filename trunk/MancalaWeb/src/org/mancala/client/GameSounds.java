package org.mancala.client;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.DataResource;

public interface GameSounds extends ClientBundle {
    
    @Source("audio/click_x.mp3")
    DataResource dotMp3();

    @Source("audio/click_x.wav")
    DataResource dotWav();
    
    @Source("audio/fanfare_x.mp3")
    DataResource gameOverMp3();

    @Source("audio/fanfare_x.wav")
    DataResource gameOverWav();
    
    @Source("audio/applause_y.mp3")
    DataResource applauseMp3();

    @Source("audio/applause_y.wav")
    DataResource applauseWav();
    
    @Source("audio/thunk.mp3")
    DataResource oppositeCaptureMp3();

    @Source("audio/thunk.wav")
    DataResource oppositeCaptureWav();
        
}