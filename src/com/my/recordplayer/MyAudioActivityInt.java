package com.my.recordplayer;

import android.media.MediaPlayer;

public interface MyAudioActivityInt {
	public static final long STATUS_NORMAL = 0x1;
	public static final long STATUS_ZOOM_OUT = (0x1 << 1);
	public static final long STATUS_USER_PROGRESSBAR = (0x1 << 2);
	public static final long STATUS_ALREADY_OPEN_FILE = (0x1 << 3);

	public void setPercent(int num, int per);

	public MediaPlayer getMediaPlayer();

	public void setStatus(long status);
}
