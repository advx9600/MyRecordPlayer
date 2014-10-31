package com.my.recordplayer.b;


import com.my.recordplayer.MyAudioActivity;
import com.my.recordplayer.MyAudioActivityInt;

import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class c implements OnSeekBarChangeListener {
	private int mNum;
	private MyAudioActivityInt mInt;

	public c(int i, MyAudioActivityInt mainActivity) {
		// TODO Auto-generated constructor stub
		mNum = i;
		mInt = mainActivity;
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		mInt.setStatus(MyAudioActivity.STATUS_USER_PROGRESSBAR);
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		mInt.setStatus(~MyAudioActivity.STATUS_USER_PROGRESSBAR);
		mInt.setPercent(mNum, seekBar.getProgress());
	}

}
