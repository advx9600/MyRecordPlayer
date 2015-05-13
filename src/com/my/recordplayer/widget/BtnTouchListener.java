package com.my.recordplayer.widget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import paul.arian.fileselector.a;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ListView;

public class BtnTouchListener implements View.OnTouchListener {
	// Cached ViewConfiguration and system-wide constant values
	private int mSlop;
	private int mMinFlingVelocity;
	private int mMaxFlingVelocity;
	private long mAnimationTime;

	// Fixed properties
	private Button mButton;
	private OnSwipeCallback mCallback;
	private int mViewWidth = 1; // 1 and not 0 to prevent dividing by zero
	private boolean dismissLeft = true;
	private boolean dismissRight = true;

	// Transient properties
	private List<PendingSwipeData> mPendingSwipes = new ArrayList<PendingSwipeData>();
	private int mDismissAnimationRefCount = 0;
	private float mDownX;
	private boolean mSwiping;
	private VelocityTracker mVelocityTracker;
	private int mDownPosition;
	private View mDownView;
	private boolean mPaused;
	private String mBtnText;
	private String mTextRight;
	private String mTextLeft;
	private boolean mWaitDouble = true;
	private final int DOUBLE_CLICK_TIME = 200;
	private final static int LONG_CLICK_1 = 200;

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				if (mWaitDouble == false) {
					mWaitDouble = true;
					mCallback.onClick(mButton, null);
				}
				break;
			}
			super.handleMessage(msg);
		}
	};

	private class ClickJudgeThread extends Thread {
		@Override
		public void run() {
			try {
				sleep(DOUBLE_CLICK_TIME);
				handler.sendEmptyMessage(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	};

	/**
	 * The callback interface used by {@link BtnTouchListener} to inform its
	 * client about a successful swipe of one or more list item positions.
	 */
	public interface OnSwipeCallback {
		/**
		 * Called when the user has swiped the list item to the left.
		 *
		 * @param listView
		 *            The originating {@link ListView}.
		 * @param reverseSortedPositions
		 *            An array of positions to dismiss, sorted in descending
		 *            order for convenience.
		 */
		void onSwipeLeft(Button btn, int[] reverseSortedPositions);

		void onSwipeRight(Button btn, int[] reverseSortedPositions);

		void onClick(Button btn, int[] reverseSortedPositions);

		void onDoubleClick(Button btn, int[] reverseSortedPositions);

		void onLongClick1(Button btn, int[] reverseSortedPositions);
	}

	/**
	 * Constructs a new swipe-to-action touch listener for the given list view.
	 *
	 * @param listView
	 *            The list view whose items should be dismissable.
	 * @param callback
	 *            The callback to trigger when the user has indicated that she
	 *            would like to dismiss one or more list items.
	 */
	public BtnTouchListener(Button btn, OnSwipeCallback callback) {
		ViewConfiguration vc = ViewConfiguration.get(btn.getContext());
		mSlop = vc.getScaledTouchSlop();
		mMinFlingVelocity = vc.getScaledMinimumFlingVelocity();
		mMaxFlingVelocity = vc.getScaledMaximumFlingVelocity();
		mAnimationTime = btn.getContext().getResources()
				.getInteger(android.R.integer.config_shortAnimTime);
		mButton = btn;
		mCallback = callback;
	}

	/**
	 * Constructs a new swipe-to-action touch listener for the given list view.
	 * 
	 * @param listView
	 *            The list view whose items should be dismissable.
	 * @param callback
	 *            The callback to trigger when the user has indicated that she
	 *            would like to dismiss one or more list items.
	 * @param dismissLeft
	 *            set if the dismiss animation is up when the user swipe to the
	 *            left
	 * @param dismissRight
	 *            set if the dismiss animation is up when the user swipe to the
	 *            right
	 * @see #SwipeListViewTouchListener(ListView, OnSwipeCallback, boolean,
	 *      boolean)
	 */
	public BtnTouchListener(Button btn, OnSwipeCallback callback,
			boolean dismissLeft, boolean dismissRight) {
		this(btn, callback);
		this.dismissLeft = dismissLeft;
		this.dismissRight = dismissRight;
	}

	public BtnTouchListener(Button btn, OnSwipeCallback callback,
			String textRight, String textLeft) {
		this(btn, callback);
		this.mTextRight = textRight;
		this.mTextLeft = textLeft;
	}

	/**
	 * Enables or disables (pauses or resumes) watching for swipe-to-dismiss
	 * gestures.
	 *
	 * @param enabled
	 *            Whether or not to watch for gestures.
	 */
	public void setEnabled(boolean enabled) {
		mPaused = !enabled;
	}

	/**
	 * Returns an {@link android.widget.AbsListView.OnScrollListener} to be
	 * added to the {@link ListView} using
	 * {@link ListView#setOnScrollListener(android.widget.AbsListView.OnScrollListener)}
	 * . If a scroll listener is already assigned, the caller should still pass
	 * scroll changes through to this listener. This will ensure that this
	 * {@link BtnTouchListener} is paused during list view scrolling.</p>
	 *
	 * @see {@link BtnTouchListener}
	 */
	public AbsListView.OnScrollListener makeScrollListener() {
		return new AbsListView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView absListView,
					int scrollState) {
				setEnabled(scrollState != AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL);
			}

			@Override
			public void onScroll(AbsListView absListView, int i, int i1, int i2) {
			}
		};
	}

	private long mPreDownTimeMill;

	@Override
	public boolean onTouch(View view, MotionEvent motionEvent) {
		if (mViewWidth < 2) {
			mViewWidth = mButton.getWidth();
		}

		switch (motionEvent.getActionMasked()) {
		case MotionEvent.ACTION_DOWN: {
			mBtnText = mButton.getText().toString();
			if (mPaused) {
				return false;
			}

			// TODO: ensure this is a finger, and set a flag

			// Find the child view that was touched (perform a hit test)
			mDownView = mButton;
			mPreDownTimeMill = System.currentTimeMillis();
			if (mDownView != null) {
				mDownX = motionEvent.getRawX();
				mVelocityTracker = VelocityTracker.obtain();
				mVelocityTracker.addMovement(motionEvent);
			}
			// view.onTouchEvent(motionEvent);
			return false;
		}

		case MotionEvent.ACTION_UP: {
			mButton.setText(mBtnText);
			if (mVelocityTracker == null) {
				break;
			}

			float deltaX = motionEvent.getRawX() - mDownX;
			mVelocityTracker.addMovement(motionEvent);
			mVelocityTracker.computeCurrentVelocity(500); // 1000 by defaut but
															// it was too much
			float velocityX = Math.abs(mVelocityTracker.getXVelocity());
			boolean swipe = false;
			boolean swipeRight = false;

			if (Math.abs(deltaX) > mViewWidth / 2) {
				swipe = true;
				swipeRight = deltaX > 0;
			} /*
			 * else if (mMinFlingVelocity <= velocityX && velocityX <=
			 * mMaxFlingVelocity) { swipe = true; swipeRight =
			 * (motionEvent.getRawX() - mDownX) > 0 ? true : false; }
			 */
			if (swipe) {
				// sufficent swipe value
				final View downView = mDownView; // mDownView gets null'd before
													// animation ends
				final int downPosition = mDownPosition;
				final boolean toTheRight = swipeRight;
				++mDismissAnimationRefCount;
				mDownView.animate()
						.translationX(swipeRight ? mViewWidth : -mViewWidth)
						.alpha(0).setDuration(mAnimationTime)
						.setListener(new AnimatorListenerAdapter() {
							@Override
							public void onAnimationEnd(Animator animation) {
								performSwipeAction(downView, downPosition,
										toTheRight, toTheRight ? dismissRight
												: dismissLeft);
							}
						});
			} else {
				// cancel
				mDownView.animate().translationX(0).alpha(1)
						.setDuration(mAnimationTime).setListener(null);
			}
			if (!mSwiping) {
				// if (mWaitDouble == true) {
				// mWaitDouble = false;
				// new ClickJudgeThread().start();
				// } else {
				// mWaitDouble = true;
				// mCallback.onDoubleClick(mButton, null);
				// }
				if (System.currentTimeMillis() - this.mPreDownTimeMill > LONG_CLICK_1) {
					mCallback.onLongClick1(mButton, null);
				} else {
					mCallback.onClick(mButton, null);
				}
			}
			mVelocityTracker = null;
			mDownX = 0;
			mDownView = null;
			mDownPosition = ListView.INVALID_POSITION;
			mSwiping = false;

			break;
		}

		case MotionEvent.ACTION_MOVE: {
			if (mVelocityTracker == null || mPaused) {
				break;
			}

			mVelocityTracker.addMovement(motionEvent);
			float deltaX = motionEvent.getRawX() - mDownX;
			if (Math.abs(deltaX) > mSlop) {
				mSwiping = true;
				// mButton.requestDisallowInterceptTouchEvent(true);

				// Cancel ListView's touch (un-highlighting the item)
				// MotionEvent cancelEvent = MotionEvent.obtain(motionEvent);
				// cancelEvent
				// .setAction(MotionEvent.ACTION_CANCEL
				// | (motionEvent.getActionIndex() <<
				// MotionEvent.ACTION_POINTER_INDEX_SHIFT));
				// mButton.onTouchEvent(cancelEvent);
			}

			if (mSwiping) {
				mDownView.setTranslationX(deltaX);
				if (deltaX > 0) {
					mButton.setText(mTextRight);
				} else {
					mButton.setText(mTextLeft);
				}
				mDownView.setAlpha(Math.max(0f,
						Math.min(1f, 1f - 2f * Math.abs(deltaX) / mViewWidth)));
				return true;
			}
			break;
		}
		}
		return false;
	}

	class PendingSwipeData implements Comparable<PendingSwipeData> {
		public int position;
		public View view;

		public PendingSwipeData(int position, View view) {
			this.position = position;
			this.view = view;
		}

		@Override
		public int compareTo(PendingSwipeData other) {
			// Sort by descending position
			return other.position - position;
		}
	}

	private void performSwipeAction(final View swipeView,
			final int swipePosition, boolean toTheRight, boolean dismiss) {
		// Animate the dismissed list item to zero-height and fire the dismiss
		// callback when
		// all dismissed list item animations have completed. This triggers
		// layout on each animation
		// frame; in the future we may want to do something smarter and more
		// performant.

		final ViewGroup.LayoutParams lp = swipeView.getLayoutParams();
		final int originalHeight = swipeView.getHeight();
		final boolean swipeRight = toTheRight;

		ValueAnimator animator;
		if (dismiss)
			animator = ValueAnimator.ofInt(originalHeight, 1).setDuration(
					mAnimationTime);
		else
			animator = ValueAnimator.ofInt(originalHeight, originalHeight - 1)
					.setDuration(mAnimationTime);

		animator.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				--mDismissAnimationRefCount;
				if (mDismissAnimationRefCount == 0) {
					// No active animations, process all pending dismisses.
					// Sort by descending position
					Collections.sort(mPendingSwipes);

					int[] swipePositions = new int[mPendingSwipes.size()];
					for (int i = mPendingSwipes.size() - 1; i >= 0; i--) {
						swipePositions[i] = mPendingSwipes.get(i).position;
					}
					if (swipeRight)
						mCallback.onSwipeRight(mButton, swipePositions);
					else
						mCallback.onSwipeLeft(mButton, swipePositions);

					ViewGroup.LayoutParams lp;
					for (PendingSwipeData pendingDismiss : mPendingSwipes) {
						// Reset view presentation
						pendingDismiss.view.setAlpha(1f);
						pendingDismiss.view.setTranslationX(0);
						lp = pendingDismiss.view.getLayoutParams();
						lp.height = originalHeight;
						pendingDismiss.view.setLayoutParams(lp);
					}

					mPendingSwipes.clear();
				}
			}
		});

		animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator valueAnimator) {
				lp.height = (Integer) valueAnimator.getAnimatedValue();
				swipeView.setLayoutParams(lp);
			}
		});

		mPendingSwipes.add(new PendingSwipeData(swipePosition, swipeView));
		animator.start();
	}
}