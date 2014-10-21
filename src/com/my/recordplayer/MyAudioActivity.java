package com.my.recordplayer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.my.recordplayer.b.b;
import com.my.recordplayer.b.c;
import com.my.recordplayer.b.d;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import paul.arian.fileselector.FileSelectionActivity;

public class MyAudioActivity extends Activity implements MyAudioActivityInt {
	private boolean mIsExist = false;

	private SharedPreferences mSharedpreferences;
	public static final String MyPREFERENCES = "MyPrefs";
	private static final String PREF_PATH = "path";
	private static final String PREF_HISTORY_FILE = "history";

	private MediaPlayer mMediaPlayer = new MediaPlayer();
	private LinearLayout mLayoutSeekBars;
	private List<SeekBar> mListSeekBars = new ArrayList<SeekBar>();

	private TextView mTextHistory;

	public static final long STATUS_NORMAL = 0x1;
	public static final long STATUS_ZOOM_OUT = (0x1 << 1);
	public static final long STATUS_USER_PROGRESSBAR = (0x1 << 2);
	public static final long STATUS_ALREADY_OPEN_FILE = (0x1 << 3);

	private long mStatus = STATUS_NORMAL;

	public List<d> mListZoom = new ArrayList<d>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setContentView(R.layout.activity_main);
		setContentView(R.layout.fragment_main);

		// if (savedInstanceState == null) {
		// getFragmentManager().beginTransaction()
		// .add(R.id.container, new PlaceholderFragment()).commit();
		// }

		mSharedpreferences = getSharedPreferences(MyPREFERENCES,
				Context.MODE_PRIVATE);
		mLayoutSeekBars = (LinearLayout) findViewById(R.id.layout_seekbars);
		mTextHistory = (TextView) (findViewById(R.id.text_history));
		mTextHistory.setText(mSharedpreferences
				.getString(PREF_HISTORY_FILE, ""));
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mIsExist = true;
		if (mMediaPlayer.isPlaying())
			mMediaPlayer.stop();
		mMediaPlayer.release();
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
		if (id == R.id.action_next_sing) {
			try {
				File files = new File(mSharedpreferences.getString(PREF_PATH,
						""));
				for (int i = 0; i < files.listFiles().length - 1; i++) {
					File file = files.listFiles()[i];
					if (file.getName()
							.equals(mSharedpreferences.getString(
									PREF_HISTORY_FILE, ""))) {
						playFile(files.listFiles()[i + 1]);
						break;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return true;
		}
		if (id == R.id.action_open) {
			showFileChooser();
			return true;
		}
		if (id == R.id.action_play_cur_sing) {
			try {
				String path = mSharedpreferences.getString(PREF_PATH, "");
				if (path == null || path.length() == 0) {
					return true;
				}
				File file = new File(path + "/"
						+ mSharedpreferences.getString(PREF_HISTORY_FILE, ""));
				playFile(file);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return true;
		}
		if (id == R.id.action_zoom_in) {
			if ((mStatus & STATUS_ALREADY_OPEN_FILE) == 0) {
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

	private void playFile(File file) {
		if (file == null) {
			return;
		}

		mSharedpreferences.edit().putString(PREF_PATH, file.getParent())
				.commit();
		mSharedpreferences.edit().putString(PREF_HISTORY_FILE, file.getName())
				.commit();

		mTextHistory.setVisibility(View.GONE);
		try {
			if (mMediaPlayer != null) {
				if (mMediaPlayer.isPlaying()) {
					mMediaPlayer.stop();
				}
				mMediaPlayer.release();
			}

			mMediaPlayer = new MediaPlayer();
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

				Button btn = (Button) findViewById(R.id.btn_control);
				btn.setOnClickListener(new b(this));
				setStatus(STATUS_ALREADY_OPEN_FILE);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		int onePer = 100 / count;

		if ((mStatus & STATUS_ZOOM_OUT) > 0) {
			if (mListZoom.size() == 2) {
				if (zoomStart == 0 && zoomDur == 100) {
					int val0 = mListZoom.get(0).calcuLatePer(onePer);
					int val1 = mListZoom.get(1).calcuLatePer(onePer);
					d smallD = val0 > val1 ? mListZoom.get(1) : mListZoom
							.get(0);
					zoomStart = val0 > val1 ? val1 : val0;
					zoomDur = Math.abs(val1 - val0);
					num = smallD.getNum();
					numProgress = smallD.getProgress();
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
			if (perDouble > 99.0) {
				perDouble = 99.0;
			}
//			a.b("perDouble:" + perDouble);
			mMediaPlayer
					.seekTo((int) (mMediaPlayer.getDuration() * perDouble / 100));
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

	@Override
	public MediaPlayer getMediaPlayer() {
		return mMediaPlayer;
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
			if (mMediaPlayer == null) {
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
		if (bit1Count > 1) {
			mStatus &= status;
		} else {
			mStatus |= status;
		}
	}

	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if ((mStatus & STATUS_ZOOM_OUT) > 0) {
			setStatus(~STATUS_ZOOM_OUT);
			Toast.makeText(this, R.string.already_to_normal_model,
					Toast.LENGTH_LONG).show();
			return false;
		}
		return super.onKeyUp(keyCode, event);
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

}