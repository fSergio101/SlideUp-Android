package com.mancj.slideup;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class SlidingContentContainer extends FrameLayout {

    private ViewDragHelper dragHelper;
    private DragViewHelperCallback dragHelperCallback;
    private ViewGroup rootView;

    public SlidingContentContainer(@NonNull Context context) {
        super(context);
        init();
    }

    public SlidingContentContainer(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        layoutInflater.inflate(R.layout.slider_content_wrapper, this, true);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        dragHelperCallback = new DragViewHelperCallback();
        dragHelper = ViewDragHelper.create(this, 1.0f, dragHelperCallback);
        rootView = (ViewGroup) this.getRootView();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean shouldInterceptTouchEvent = dragHelper.shouldInterceptTouchEvent(ev);
        return shouldInterceptTouchEvent;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        dragHelper.processTouchEvent(event);
        return true;
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (dragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    private class DragViewHelperCallback extends ViewDragHelper.Callback {

        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            //TODO
            return false;
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            //TODO
            return top;
        }

        @Override
        public int getViewVerticalDragRange(View child) {
            //TODO
            return rootView.getMeasuredHeight() - child.getMeasuredHeight();
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            //TODO
            super.onViewReleased(releasedChild, xvel, yvel);
            if (yvel > 0) {
                dragHelper.settleCapturedViewAt(releasedChild.getLeft(),
                        rootView.getMeasuredHeight() - releasedChild.getMeasuredHeight());
            } else {
                dragHelper.settleCapturedViewAt(releasedChild.getLeft(), 0);
            }
            invalidate();
        }

    }
}
