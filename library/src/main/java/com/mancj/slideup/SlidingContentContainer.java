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
    private SliderContent sliderContent;
    private SliderFuturePositionStrategy sliderFuturePositionStrategy;
    private OnAttachedToWindowListener onAttachedToWindowListener;
    private float maxContainerSize;

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

    public SlideUp.State computeFollowingStop(
            final float viewStartPositionY,
            final float slideAnimationFrom,
            final SlideUp.State currentState) {

        SliderFuturePosition sliderFuturePosition = sliderFuturePositionStrategy
                .calculateStopPosition(
                        viewStartPositionY,
                        slideAnimationFrom,
                        currentState
                );

        return sliderFuturePositionToSlideUpState(sliderFuturePosition);
    }

    public SlideUp.State getInitialState() {

        SliderFuturePosition initialState = sliderFuturePositionStrategy.getInitialState();

        return sliderFuturePositionToSlideUpState(initialState);
    }

    private SlideUp.State sliderFuturePositionToSlideUpState(
            SliderFuturePosition sliderFuturePosition) {
        SlideUp.State state;
        if (sliderFuturePosition.isMax()) {
            state = SlideUp.State.SHOWED;
        } else {
            state = SlideUp.State.STOP;
            state.setPosition(sliderFuturePosition.getPosition());
        }
        return state;
    }

    // region touch intercepting

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

    public void setOnAttachedToWindowListener(
            OnAttachedToWindowListener onAttachedToWindowListener) {
        this.onAttachedToWindowListener = onAttachedToWindowListener;
    }

    public void setSliderContent(SliderContent sliderContent, float viewHeight) {
        this.sliderContent = sliderContent;
        this.maxContainerSize = viewHeight;
    }

    public void onAppearOnScreen() {
        sliderFuturePositionStrategy = new RangeStrategy(sliderContent, maxContainerSize);
    }

    //TODO this class stuff will be the one who intercept the grad events in order to avoid scroll
    // for cases in which our component won't be expanded
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
    // endregion touch intercepting

    //    private float normalize(float delta, float previous) {
//        if (delta < 0) {
//            //subir
//            if (delta < -50) {
//                return previous - 100;
//            } else {
//                return previous;
//            }
//        } else {
//            //subir
//            if (delta > 50) {
//                return previous + 100;
//            } else {
//                return previous;
//            }
//        }
//    }
}
