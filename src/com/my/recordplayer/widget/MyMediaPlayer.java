package com.my.recordplayer.widget;

import android.media.MediaPlayer;
import android.widget.TextView;

public class MyMediaPlayer extends MediaPlayer {
	private TextView mTextView = null;
	private int mPauseStr;
	private int mStartStr;

	@Override
	public void pause() {
		if (mTextView != null) {
			mTextView.setText(mStartStr);
		}
		super.pause();
	}

	@Override
	public void start() {
		if (mTextView != null) {
			mTextView.setText(mPauseStr);
		}
		super.start();
	}

	public TextView getmTextView() {
		return mTextView;
	}

	public void setmTextView(TextView mTextView, int startString,
			int pauseString) {
		this.mTextView = mTextView;
		mPauseStr = pauseString;
		mStartStr = startString;
	}
}
