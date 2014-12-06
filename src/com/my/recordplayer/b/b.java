package com.my.recordplayer.b;

import com.my.recordplayer.MyAudioActivityInt;
import com.my.recordplayer.R;
import com.my.recordplayer.widget.BtnTouchListener;
import android.widget.Button;

public class b {
	private MyAudioActivityInt mMainActivity;

	public b(MyAudioActivityInt mainActivity, Button btn) {
		// TODO Auto-generated constructor stub
		mMainActivity = mainActivity;
		BtnTouchListener btnListener = new BtnTouchListener(btn,
				new BtnTouchListener.OnSwipeCallback() {

					@Override
					public void onSwipeRight(Button btn,
							int[] reverseSortedPositions) {
						// TODO Auto-generated method stub
						mMainActivity.playNext();
					}

					@Override
					public void onSwipeLeft(Button btn,
							int[] reverseSortedPositions) {
						// TODO Auto-generated method stub
						mMainActivity.playPre();
					}

					@Override
					public void onClick(Button btn, int[] reverseSortedPositions) {
						// TODO Auto-generated method stub
						if (mMainActivity.getMediaPlayer() == null) {
							mMainActivity.playCur();
							return;
						}
						if (mMainActivity.getMediaPlayer().isPlaying()) {
							mMainActivity.getMediaPlayer().pause();
							(btn).setText(R.string.play);
						} else {
							mMainActivity.getMediaPlayer().start();
							(btn).setText(R.string.pause);
						}
					}
				}, btn.getContext().getString(R.string.next_song), btn
						.getContext().getString(R.string.pre_song));
		btn.setOnTouchListener(btnListener);
	}

}