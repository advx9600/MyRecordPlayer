package com.my.recordplayer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.my.recordplayer.b.b;
import com.my.recordplayer.b.c;
import com.my.recordplayer.b.d;
import com.my.recordplayer.b.e;
import com.my.recordplayer.widget.MyMediaPlayer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import paul.arian.fileselector.FileSelectionActivity;

public class MyAudioActivity extends Activity implements MyAudioActivityInt {
	private boolean mIsExist = false;

	private boolean mIsLockMediaPlayer = false;
	private SharedPreferences mSharedpreferences;
	public static final String MyPREFERENCES = "MyPrefs";
	private static final String PREF_PATH = "path";
	private static final String PREF_HISTORY_FILE = "history";

	private static final String TAG = "MyRecordPlayer";

	private MyMediaPlayer mMediaPlayer = null;
	private LinearLayout mLayoutSeekBars;
	private List<SeekBar> mListSeekBars = new ArrayList<SeekBar>();

	private TextView mTextHistory;

	private long mStatus = STATUS_NORMAL;

	public List<d> mListZoom = new ArrayList<d>();

	private WakeLock mWakelock;
	private boolean isWakeAcquire = false;

	private TelephonyManager mPhoneManager;

	private Button mBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setContentView(R.layout.activity_main);
		setContentView(R.layout.fragment_main);
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		// if (savedInstanceState == null) {
		// getFragmentManager().beginTransaction()
		// .add(R.id.container, new PlaceholderFragment()).commit();
		// }
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		mSharedpreferences = getSharedPreferences(MyPREFERENCES,
				Context.MODE_PRIVATE);
		mLayoutSeekBars = (LinearLayout) findViewById(R.id.layout_seekbars);
		mTextHistory = (TextView) (findViewById(R.id.text_history));
		mTextHistory.setText(mSharedpreferences
				.getString(PREF_HISTORY_FILE, ""));

		mBtn = (Button) findViewById(R.id.btn_control);
		new b(this, mBtn);

		mWakelock = ((PowerManager) getSystemService(POWER_SERVICE))
				.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK
						| PowerManager.ON_AFTER_RELEASE, TAG);

		mPhoneManager = (TelephonyManager) this
				.getSystemService(TELEPHONY_SERVICE);
		mPhoneManager.listen(new e(this), PhoneStateListener.LISTEN_CALL_STATE);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mIsExist = true;
		if (mMediaPlayer != null) {
			if (mMediaPlayer.isPlaying())
				mMediaPlayer.stop();
			mMediaPlayer.release();
		}
	}

	@Override
	public void onResume() {
		if (isWakeAcquire) {
			setWakelock(true, false);
		}
		super.onResume();
	}

	@Override
	public void onPause() {
		if (isWakeAcquire) {
			setWakelock(false, false);
		}
		super.onPause();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_open) {
			showFileChooser();
			return true;
		}
		if (id == R.id.action_zoom_in) {
			if ((mStatus & STATUS_ALREADY_OPEN_FILE) == 0) {
				a.b("yes here status:"+mStatus);
				return true;
			}
			if ((mStatus & STATUS_ZOOM_OUT) > 0) {
				setStatus(~STATUS_ZOOM_OUT);
				Toast.makeText(this, R.string.already_to_normal_model,
						Toast.LENGTH_SHORT).show();
			} else {
				setStatus(STATUS_ZOOM_OUT);
				mListZoom.clear();
				Toast.makeText(this,
						R.string.select_progress_bar_start_end_area,
						Toast.LENGTH_LONG).show();
				if (mMediaPlayer.isPlaying()) {
					mMediaPlayer.pause();
				}
			}
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	private static final int FILE_SELECT_CODE = 0;

	// private static final String FILES_TO_UPLOAD = "one";

	private void showFileChooser() {
		Intent intent = new Intent(getBaseContext(),
				FileSelectionActivity.class);
		intent.putExtra(FileSelectionActivity.EXTRA_SET_PATH,
				mSharedpreferences.getString(PREF_PATH, ""));
		intent.putExtra(FileSelectionActivity.EXTRA_SET_FILE,
				mSharedpreferences.getString(PREF_HISTORY_FILE, ""));
		startActivityForResult(intent, FILE_SELECT_CODE);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == FILE_SELECT_CODE && resultCode == RESULT_OK) {
			@SuppressWarnings("unchecked")
			ArrayList<File> Files = (ArrayList<File>) data
					.getSerializableExtra("upload");
			for (File file : Files) {
				playFile(file);
				break; // only radio checked
			}
		}

	}

	public void doClick(boolean isForceControl, boolean on) {
		if (mMediaPlayer == null) {
			if (!isForceControl)
				playCur();
			return;
		}
		if (!isForceControl)
			if (mMediaPlayer.isPlaying()) {
				mMediaPlayer.pause();
			} else {
				mMediaPlayer.start();
			}
		else {
			if (on != mMediaPlayer.isPlaying()) {
				doClick(false, false);
			}
		}
	}

	private void playFile(File file) {
		if (file == null) {
			return;
		}

		setStatus(STATUS_NORMAL);
		mSharedpreferences.edit().putString(PREF_PATH, file.getParent())
				.commit();
		mSharedpreferences.edit().putString(PREF_HISTORY_FILE, file.getName())
				.commit();

		setTitle(file.getName());
		mTextHistory.setVisibility(View.GONE);
		try {
			mIsLockMediaPlayer = true;
			if (mMediaPlayer != null) {
				if (mMediaPlayer.isPlaying()) {
					mMediaPlayer.stop();
				}
				mMediaPlayer.release();
			}

			mMediaPlayer = new MyMediaPlayer();
			mMediaPlayer.setmTextView(mBtn, R.string.play, R.string.pause);
			mMediaPlayer.setDataSource(file.getAbsolutePath());
			mMediaPlayer.prepare();
			mMediaPlayer.start();
			// a.b("duration:" + mMediaPlayer.getDuration());
			if (mListSeekBars.size() == 0) {
				for (int i = 0; i < 8; i++) {
					SeekBar sb = new SeekBar(this);
					sb.setMax(100);
					sb.setOnSeekBarChangeListener(new c(i, this));
					mListSeekBars.add(sb);
					LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
							LayoutParams.MATCH_PARENT,
							LayoutParams.WRAP_CONTENT);
					lp.setMargins(0, 20, 0, 0);
					mLayoutSeekBars.addView(sb, lp);
				}

				new SeekBarHandler().execute();				
			}
			setStatus(STATUS_ALREADY_OPEN_FILE);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			mIsLockMediaPlayer = false;
		}
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	@Override
	public void setPercent(int num, int progress) {
		if ((mStatus & STATUS_ZOOM_OUT) > 0) {
			if (mListZoom.size() < 2) {
				d d = new d();
				d.setNum(num);
				d.setProgress(progress);
				mListZoom.add(d);
			}
			if (mListZoom.size() < 2) {
				return;
			}
		}
		setPercent(0, true, num, progress);
	}

	private void setPercent(double percent, boolean isSetPlay) {
		setPercent(percent, false, 0, 0);
	}

	private int zoomStart = 0;
	private int zoomDur = 100;

	private void setPercent(double percent, boolean isSetPlay, int num,
			int numProgress) {
		int count = mListSeekBars.size();
		// int onePer = 100 / count + (100 % count == 0 ? 0 : 1);
		final double onePer = 100f / count;
		boolean isFirstZoomOut = false;

		if ((mStatus & STATUS_ZOOM_OUT) > 0) {
			if (mListZoom.size() == 2) {
				if (zoomStart == 0 && zoomDur == 100) {
					int val0 = mListZoom.get(0).calcuLatePer(onePer,true);
					int val1 = mListZoom.get(1).calcuLatePer(onePer,false);
					d smallD = val0 > val1 ? mListZoom.get(1) : mListZoom
							.get(0);
					zoomStart = val0 > val1 ? val1 : val0;
					zoomDur = Math.abs(val1 - val0);
					num = smallD.getNum();
					numProgress = smallD.getProgress();
					isFirstZoomOut = true;
				}
				if (percent < zoomStart) {
					percent = 0;
				} else if (percent > zoomStart + zoomDur) {
					percent = 100;
				} else {
					percent = (percent - zoomStart) * 100 / zoomDur;
				}
			} else {
				return;
			}
		} else {
			zoomStart = 0;
			zoomDur = 100;
		}

		if (isSetPlay) {
			double perDouble = zoomStart
					+ (num * onePer + (numProgress * onePer * 1.0 / 100))
					* zoomDur / 100;
			 // 因为播放时不可能设置为 100(即为停止)
			if (perDouble > 99.9999) {
				perDouble = 99.9999;
			}
			if (isFirstZoomOut) {
				if (mMediaPlayer.getCurrentPosition() * 100
						/ mMediaPlayer.getDuration() < zoomStart) {
					mMediaPlayer.seekTo((int) (mMediaPlayer.getDuration()
							* zoomStart / 100));
				}
			} else {
				mMediaPlayer.seekTo((int) (mMediaPlayer.getDuration()
						* perDouble / 100));
			}
			if (!mMediaPlayer.isPlaying()) {
				mMediaPlayer.start();
			}
			return;
		}

		if ((mStatus & STATUS_USER_PROGRESSBAR) > 0) {
			return;
		}

		for (int i = 0; i < count; i++) {
			SeekBar bar = mListSeekBars.get(i);
			int setPer = 100;
			if (percent < (i + 1) * onePer) {
				setPer = (int) ((percent - i * onePer) * 100 / onePer);
			}
			if (bar.getProgress() != setPer) {
				bar.setProgress(setPer);
			}
		}
	}

	public class SeekBarHandler extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			// seekBar.setProgress(mMediaPlayer.getCurrentPosition());
			// a.b("getCurrentPosition:" + mMediaPlayer.getCurrentPosition()
			// + ",getDuration:" + mMediaPlayer.getDuration());
			if (mMediaPlayer == null || mIsLockMediaPlayer) {
				return;
			}
			setPercent(
					mMediaPlayer.getCurrentPosition() * 100.0
							/ mMediaPlayer.getDuration(), false);
			// mMediaPlayer.set
			super.onProgressUpdate(values);
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			while (!mIsExist) {
				onProgressUpdate();
				// mMediaPlayer.isPlaying();
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return null;
		}

	}

	@Override
	public void setStatus(long status) {
		// TODO Auto-generated method stub
		int bit1Count = 0;
		for (int i = 0; i < 3; i++) {
			if ((status & (0x1 << i)) > 0) {
				bit1Count++;
			}
		}
		if (status == STATUS_NORMAL) {
			mStatus = status;
		} else if (bit1Count > 1) {
			mStatus &= status;
		} else {
			mStatus |= status;
		}
	}

	public boolean onKeyUp(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			if ((mStatus & STATUS_ZOOM_OUT) > 0) {
				setStatus(~STATUS_ZOOM_OUT);
				Toast.makeText(this, R.string.already_to_normal_model,
						Toast.LENGTH_LONG).show();
				return false;
			}
			break;
		}
		return super.onKeyUp(keyCode, event);
	}

	@Override
	public void playCur() {
		// TODO Auto-generated method stub
		playFileDirection(0);
	}

	@Override
	public void playPre() {
		// TODO Auto-generated method stub
		playFileDirection(-1);
	}

	@Override
	public void playNext() {
		// TODO Auto-generated method stub
		playFileDirection(1);
	}

	private void playFileDirection(int dir) {
		try {
			File files = new File(mSharedpreferences.getString(PREF_PATH, ""));
			for (int i = 0; i < files.listFiles().length; i++) {
				File file = files.listFiles()[i];
				if (file.getName().equals(
						mSharedpreferences.getString(PREF_HISTORY_FILE, ""))) {
					if (i + dir > -1 && i + dir < files.listFiles().length) {
						playFile(files.listFiles()[i + dir]);
						break;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// public static class PlaceholderFragment extends Fragment {
	//
	// public PlaceholderFragment() {
	// }
	//
	// @Override
	// public View onCreateView(LayoutInflater inflater, ViewGroup container,
	// Bundle savedInstanceState) {
	// View rootView = inflater.inflate(R.layout.fragment_main, container,
	// false);
	// return rootView;
	// }
	// }

	@Override
	public void setWakelock(boolean acquire, boolean isUserOps) {
		if (isUserOps) {
			isWakeAcquire = acquire;
		}
		if (acquire) {
			if (!mWakelock.isHeld()) {
				mWakelock.acquire();
				this.getActionBar().setIcon(R.drawable.ic_launcher_light);
			}
		} else {
			if (mWakelock.isHeld()) {
				mWakelock.release();
				this.getActionBar().setIcon(R.drawable.ic_launcher);
			}
		}
	}

	public boolean getWakLockAcquire() {
		return isWakeAcquire;
	}

}
