package com.my.recordplayer.b;

import com.my.recordplayer.MainActivity;
import com.my.recordplayer.MainActivityInt;
import com.my.recordplayer.R;

import android.media.MediaPlayer;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class b implements OnClickListener {
	private MainActivityInt mMainActivity;

	public b(MainActivityInt mainActivity) {
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
