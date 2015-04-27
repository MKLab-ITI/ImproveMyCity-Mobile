// ImageView_Zoom
package com.mk4droid.IMC_Core;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;

import com.mk4droid.IMC_Activities.Fragment_Issue_Details;

/**
 * This is a custom ImageView that also incorporates zoom gestures.
 * 
 * @copyright   Copyright (C) 2012 - 2015 Information Technology Institute ITI-CERTH. All rights reserved.
 * @license     GNU Affero General Public License version 3 or later; see LICENSE.txt
 * @author      Dimitrios Ververidis for the Multimedia Group (http://mklab.iti.gr). 
 */
public class ImageView_Zoom extends ImageView {

	private static final int INVALID_POINTER_ID = -1;

	private float mPosX;
	private float mPosY;

	private float mLastTouchX;
	private float mLastTouchY;

	private float mLastGestureX;
	private float mLastGestureY;

	private int mActivePointerId = INVALID_POINTER_ID;

	private ScaleGestureDetector mScaleDetector;
	private float mScaleFactor = 1.f;

	//    PointF mid = new PointF();  

	boolean resetFlag = false;

	/* (non-Javadoc)
	 * @see android.view.View#onConfigurationChanged(android.content.res.Configuration)
	 */
	@Override
	protected void onConfigurationChanged(Configuration newConfig) {
		resetFlag = true;
		invalidate();
		super.onConfigurationChanged(newConfig);

	}

	/** Constructor with 2 arguments of this class */
	public ImageView_Zoom(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		mScaleDetector = new ScaleGestureDetector(getContext(), new ScaleListener());

		getViewTreeObserver().addOnGlobalLayoutListener( new OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				setImageBitmap(Fragment_Issue_Details.bmI); 
			}
		});
	}

	/** Constructor with 3 arguments of this class */
	public ImageView_Zoom(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
	}

	/** Main functionality */
	@Override
	public boolean onTouchEvent(MotionEvent ev) {

		mScaleDetector.onTouchEvent(ev);

		final int action = ev.getAction();
		switch (action & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN: {
			if (!mScaleDetector.isInProgress()) {

				final float x = ev.getX();
				final float y = ev.getY();

				mLastTouchX = x;
				mLastTouchY = y;
				mActivePointerId = ev.getPointerId(0);
			}
			break;
		}
		case MotionEvent.ACTION_POINTER_1_DOWN: {
			if (mScaleDetector.isInProgress()) {
				final float gx = mScaleDetector.getFocusX();
				final float gy = mScaleDetector.getFocusY();
				mLastGestureX = gx;
				mLastGestureY = gy;
			}
			break;
		}
		case MotionEvent.ACTION_MOVE: {
			// Only move if the ScaleGestureDetector isn't processing a gesture.
			if (!mScaleDetector.isInProgress()) {
				final int pointerIndex = ev.findPointerIndex(mActivePointerId);
				final float x = ev.getX(pointerIndex);
				final float y = ev.getY(pointerIndex);

				final float dx = x - mLastTouchX;
				final float dy = y - mLastTouchY;

				mPosX += dx;
				mPosY += dy;

				invalidate();

				mLastTouchX = x;
				mLastTouchY = y;
			}
			else{
				final float gx = mScaleDetector.getFocusX();
				final float gy = mScaleDetector.getFocusY();

				final float gdx = gx - mLastGestureX;
				final float gdy = gy - mLastGestureY;

				mPosX += gdx;
				mPosY += gdy;

				invalidate();

				mLastGestureX = gx;
				mLastGestureY = gy;
			}
			break;
		}
		case MotionEvent.ACTION_UP: {
			mActivePointerId = INVALID_POINTER_ID;
			break;
		}
		case MotionEvent.ACTION_CANCEL: {
			mActivePointerId = INVALID_POINTER_ID;
			break;
		}
		case MotionEvent.ACTION_POINTER_UP: {
			final int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) 
					>> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
					final int pointerId = ev.getPointerId(pointerIndex);

					if (pointerId == mActivePointerId) {
						// This was our active pointer going up. Choose a new
						// active pointer and adjust accordingly.
						final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
						mLastTouchX = ev.getX(newPointerIndex);
						mLastTouchY = ev.getY(newPointerIndex);
						mActivePointerId = ev.getPointerId(newPointerIndex);
					}
					else{
						final int tempPointerIndex = ev.findPointerIndex(mActivePointerId);
						mLastTouchX = ev.getX(tempPointerIndex);
						mLastTouchY = ev.getY(tempPointerIndex);
					}

					break;
		}
		}

		return true;
	}

	/** Draw the scaled image */
	@Override
	public void onDraw(Canvas canvas) {

		if (resetFlag){
			canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

			mPosX = mPosY = mLastTouchX = mLastTouchY = mLastGestureX = mLastGestureY = 0;
			mScaleFactor = 1;

			setScaleType(ScaleType.FIT_CENTER);
			setImageBitmap(Fragment_Issue_Details.bmI);

			resetFlag = false;
			canvas.restore();
		} else {

			if (mScaleDetector.isInProgress()) {
				canvas.scale(mScaleFactor, mScaleFactor, mScaleDetector.getFocusX(), mScaleDetector.getFocusY());
			} else{
				canvas.translate(mPosX/2, mPosY/2);
				canvas.scale(mScaleFactor, mScaleFactor, mLastGestureX, mLastGestureY);
			}

		}

		super.onDraw(canvas);
	}

	
	private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			mScaleFactor *= detector.getScaleFactor();
			mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 10.0f)); 			// Don't let the object get too small or too large.
			invalidate();
			return true;
		}
	}
}