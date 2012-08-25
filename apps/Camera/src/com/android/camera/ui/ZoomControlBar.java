/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.camera.ui;

import com.android.camera.R;
import com.android.camera.Util;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.util.Log;
/**
 * A view that contains camera zoom control and its layout.
 */
public class ZoomControlBar extends ZoomControl {
    private static final String TAG = "ZoomControlBar";
    private static final int THRESHOLD_FIRST_MOVE = Util.dpToPixel(10); // pixels
    // Space between indicator icon and the zoom-in/out icon.
    private static final int ICON_SPACING = Util.dpToPixel(12);

    private View mBar;
    private boolean mStartChanging;
    private int mSliderPosition = 0;
    private int mSliderLength;
    private int mLength;
    private int mIconWidth;
    private int mTotalIconWidth;
	private boolean mLandscapeInLayout; //when it is true, the width is the length of the bar.
		
    public ZoomControlBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        mBar = new View(context);
        mBar.setBackgroundResource(R.drawable.zoom_slider_bar);
        addView(mBar);
		if(mContext.getResources().getBoolean(R.bool.TabletUsePhoneUI)==true) {
			mLandscapeInLayout = false;
		 } else {
			mLandscapeInLayout = true;
		 }
    }

    @Override
    public void setActivated(boolean activated) {
        super.setActivated(activated);
        mBar.setActivated(activated);
    }

    private int getSliderPosition(int x) {
        // Calculate the absolute offset of the slider in the zoom control bar.
        // For left-hand users, as the device is rotated for 180 degree for
        // landscape mode, the zoom-in bottom should be on the top, so the
        // position should be reversed.
        int pos; // the relative position in the zoom slider bar
        if(mLandscapeInLayout == true) {
	        if (mOrientation == 90) {
				pos = mLength - mTotalIconWidth - x;
	        } else {
				pos = x - mTotalIconWidth;
	        }
        } else {
			if (mOrientation == 180) {
				pos = x - mTotalIconWidth;
	        } else {
				pos = mLength - mTotalIconWidth - x;
	        }
        }
        if (pos < 0) pos = 0;
        if (pos > mSliderLength) pos = mSliderLength;
        return pos;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    	if(mLandscapeInLayout == true) {
	        mLength = w;
			mIconWidth = mZoomIn.getMeasuredWidth();
    	} else {
			mLength = h;
			mIconWidth = mZoomIn.getMeasuredHeight();
    	}
		mTotalIconWidth = mIconWidth + ICON_SPACING;
		mSliderLength = mLength  - (2 * mTotalIconWidth);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (!isEnabled() || (mLength == 0)) return false;
        int action = event.getAction();

        switch (action) {
            case MotionEvent.ACTION_OUTSIDE:
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                setActivated(false);
                closeZoomControl();
                break;

            case MotionEvent.ACTION_DOWN:
                setActivated(true);
                mStartChanging = false;
            case MotionEvent.ACTION_MOVE:
				int pos = 0;
				if(mLandscapeInLayout)
                	pos = getSliderPosition((int) event.getX());
				else
					pos = getSliderPosition((int) event.getY());
                if (!mStartChanging) {
                    // Make sure the movement is large enough before we start
                    // changing the zoom.
                    int delta = mSliderPosition - pos;
                    if ((delta > THRESHOLD_FIRST_MOVE) ||
                            (delta < -THRESHOLD_FIRST_MOVE)) {
                        mStartChanging = true;
                    }
                }
                if (mStartChanging) {
                    performZoom(1.0d * pos / mSliderLength);
                    mSliderPosition = pos;
                }
                requestLayout();
        }
        return true;
    }

    @Override
    public void setOrientation(int orientation) {
        // layout for the left-hand camera control
        if(mLandscapeInLayout == true) {
        	if ((orientation == 90) || (mOrientation == 90)) requestLayout();
        } else {
			if ((orientation == 180) || (mOrientation == 180)) requestLayout();
        }
        super.setOrientation(orientation);
    }

    @Override
    protected void onLayout(
            boolean changed, int left, int top, int right, int bottom) {
        if (mZoomMax == 0) return;
		if(mLandscapeInLayout == true) {
	        int height = bottom - top;
	        mBar.layout(mTotalIconWidth, 0, mLength - mTotalIconWidth, height);
	        // For left-hand users, as the device is rotated for 180 degree,
	        // the zoom-in button should be on the top.
	        int pos; // slider position
	        int sliderPosition;
	        if (mSliderPosition != -1) { // -1 means invalid
	            sliderPosition = mSliderPosition;
	        } else {
	            sliderPosition = (int) ((double) mSliderLength * mZoomIndex / mZoomMax);
	        }
	        if (mOrientation == 90) {
	            mZoomIn.layout(0, 0, mIconWidth, height);
	            mZoomOut.layout(mLength - mIconWidth, 0, mLength, height);
	            pos = mBar.getRight() - sliderPosition;
	        } else {
	            mZoomOut.layout(0, 0, mIconWidth, height);
	            mZoomIn.layout(mLength - mIconWidth, 0, mLength, height);
	            pos = mBar.getLeft() + sliderPosition;
	        }
	        int sliderWidth = mZoomSlider.getMeasuredWidth();
	        mZoomSlider.layout((pos - sliderWidth / 2), 0,
	                (pos + sliderWidth / 2), height);
		} else {
            int width = right - left;
	        mBar.layout(0, mTotalIconWidth, width ,mLength - mTotalIconWidth );
			int temp = mLength - mTotalIconWidth;
			Log.d(TAG,"bar layout "+String.valueOf(width)+","+String.valueOf(temp));
	        // For left-hand users, as the device is rotated for 180 degree,
	        // the zoom-in button should be on the top.
	        int pos; // slider position
	        int sliderPosition;
	        if (mSliderPosition != -1) { // -1 means invalid
	            sliderPosition = mSliderPosition;
	        } else {
	            sliderPosition = (int) ((double) mSliderLength * mZoomIndex / mZoomMax);
	        }
	        if (mOrientation == 180) {
				mZoomOut.layout(0, 0,  width,mIconWidth );
	            mZoomIn.layout( 0, mLength - mIconWidth, width, mLength);
	            pos = mBar.getTop() + sliderPosition;
	        } else {
				mZoomIn.layout(0, 0, width, mIconWidth);
	            mZoomOut.layout( 0, mLength - mIconWidth, width, mLength);
	            pos = mBar.getBottom() - sliderPosition;
	        }
	        int sliderWidth = mZoomSlider.getMeasuredWidth();
	        mZoomSlider.layout( 0,(pos - sliderWidth / 2),
	                width, (pos + sliderWidth / 2));
		}
    }

    @Override
    public void setZoomIndex(int index) {
        super.setZoomIndex(index);
        mSliderPosition = -1; // -1 means invalid
        requestLayout();
    }
}
