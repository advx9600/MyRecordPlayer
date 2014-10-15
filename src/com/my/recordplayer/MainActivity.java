package com.my.recordplayer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.my.recordplayer.b.b;
import com.my.recordplayer.b.c;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
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
import paul.arian.fileselector.FileSelectionActivity;

public class MainActivity extends Activity implements MainActivityInt {
	private boolean mIsExist = false;

	private SharedPreferences mSharedpreferences;
	public static final String MyPREFERENCES = "MyPrefs";
	private static final String PREF_PATH = "path";
	private static final String PREF_HISTORY = "history";

	private MediaPlayer mMediaPlayer = new MediaPlayer();
	private LinearLayout mLayoutSeekBars;
	private List<SeekBar> mListSeekBars = new ArrayList<SeekBar>();

	private TextView mTextHistory;

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
		mTextHistory.setText(mSharedpreferences.getString(PREF_HISTORY, ""));
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
		if (id == R.id.action_settings) {
			return true;
		}
		if (id == R.id.action_open) {
			showFileChooser();
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
				mSharedpreferences.edit()
						.putString(PREF_PATH, file.getParent()).commit();
				mSharedpreferences.edit()
						.putString(PREF_HISTORY, file.getName()).commit();
				// a.b(file.getAbsolutePath());
				mTextHistory.setVisibility(View.GONE);
				try {

					if (mMediaPlayer.isPlaying()) {
						mMediaPlayer.stop();
					}
					mMediaPlayer.release();

					mMediaPlayer = new MediaPlayer();
					mMediaPlayer.setDataSource(file.getAbsolutePath());
					mMediaPlayer.prepare();
					mMediaPlayer.start();
					a.b("duration:" + mMediaPlayer.getDuration());
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
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break; // only radio checked
			}
		}

	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	@Override
	public void setPercent(int num, int progress) {
		setPercent(0, true, num, progress);
	}

	private void setPercent(int percent, boolean isSetPlay) {
		setPercent(percent, false, 0, 0);
	}

	private void setPercent(int percent, boolean isSetPlay, int num,
			int numProgress) {
		int count = mListSeekBars.size();
		int onePer = 100 / count + (100 % count == 0 ? 1 : 0);

		if (isSetPlay) {
			percent = num * onePer + numProgress * onePer / 100;
			mMediaPlayer.seekTo(mMediaPlayer.getDuration() * percent / 100);
			if (!mMediaPlayer.isPlaying()) {
				mMediaPlayer.start();
			}
		}

		for (int i = 0; i < count; i++) {
			SeekBar bar = mListSeekBars.get(i);
			int setPer = 100;
			if (percent < (i + 1) * onePer) {
				setPer = (percent - i * onePer) * 100 / onePer;
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
			Log.d("##########Seek Bar Handler ################",
					"###################Destroyed##################");
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
					mMediaPlayer.getCurrentPosition() * 100
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

	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}
	}

}
