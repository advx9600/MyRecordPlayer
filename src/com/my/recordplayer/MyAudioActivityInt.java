package com.my.recordplayer;

import android.media.MediaPlayer;

public interface MyAudioActivityInt {
	public void setPercent(int num, int per);

	public MediaPlayer getMediaPlayer();
	
	public void setStatus(long status);
}
