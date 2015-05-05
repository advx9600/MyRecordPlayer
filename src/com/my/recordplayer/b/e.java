package com.my.recordplayer.b;

import com.my.recordplayer.MyAudioActivityInt;

import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

public class e extends PhoneStateListener {
	private MyAudioActivityInt mAudio;
	public e(MyAudioActivityInt activity){
		mAudio = activity;
	}
	@Override
	public void onCallStateChanged(int state, String incomingNumber) {
		switch (state) {
		case TelephonyManager.CALL_STATE_RINGING:
			mAudio.doClick(true, false);
			break;
		}
	}
}
