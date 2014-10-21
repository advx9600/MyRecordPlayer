package com.my.recordplayer.b;

import com.my.recordplayer.MyAudioActivity;
import com.my.recordplayer.MyAudioActivityInt;
import com.my.recordplayer.R;

import android.media.MediaPlayer;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class b implements OnClickListener {
	private MyAudioActivityInt mMainActivity;

	public b(MyAudioActivityInt mainActivity) {
		// TODO Auto-generated constructor stub
		mMainActivity = mainActivity;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (mMainActivity.getMediaPlayer().isPlaying()) {
			mMainActivity.getMediaPlayer().pause();
			((TextView) v).setText(R.string.play);
		} else {
			mMainActivity.getMediaPlayer().start();
			((TextView) v).setText(R.string.pause);
		}
	}

}
