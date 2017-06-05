package com.mancj.slideup;

import android.animation.Animator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.mancj.slideup.SlideUp.State.HIDDEN;
import static com.mancj.slideup.SlideUp.State.SHOWED;

public class SlideUp implements View.OnTouchListener, ValueAnimator.AnimatorUpdateListener,
        Animator.AnimatorListener {

    private final static String TAG = "SlideUp";

    private final static String KEY_DEBUG = TAG + "_debug";
    private final static String KEY_STATE = TAG + "_state";
    private final static String KEY_AUTO_SLIDE_DURATION = TAG + "_auto_slide_duration";
    private final static String KEY_HIDE_SOFT_INPUT = TAG + "_hide_soft_input";

    /**
     * <p>Available start states</p>
     */
    public enum State implements Parcelable, Serializable {

        /**
         * State hidden is equal {@link View#GONE}
         */
        HIDDEN,
        STOP,

        /**
         * State showed is equal {@link View#VISIBLE}
         */
        SHOWED;

        private float position = 0f;

        void setPosition(float position) {
            if (this == STOP) {
                this.position = position;
            } else {
                throw new IllegalStateException();
            }

        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(ordinal());
            dest.writeFloat(position);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<State> CREATOR = new Creator<State>() {
            @Override
            public State createFromParcel(Parcel in) {
                State state = State.values()[in.readInt()];
                if (state == STOP) {
                    state.setPosition(in.readFloat());
                }
                return state;
            }

            @Override
            public State[] newArray(int size) {
                return new State[size];
            }
        };
    }

    private State startState;
    private State currentState;
    private SlidingContentContainer slidingContentContainer;
    private int autoSlideDuration;
    private List<Listener> listeners;

    private ValueAnimator valueAnimator;
    private float slideAnimationTo;

    private float startPositionY;
    private float viewStartPositionY;
    private float maxSlidePosition;
    private float viewHeight;
    private boolean hideKeyboard;
    private TimeInterpolator interpolator;
    private boolean debug;

    /**
     * <p>Interface to listen to all handled events taking place in the slider</p>
     */
    public interface Listener {

        /**
         * @param percent percents of complete slide <b color="#EF6C00">(100 = HIDDEN, 0 =
         *                SHOWED)</b>
         */
        void onSlide(float percent);

        /**
         * @param visibility (<b>GONE</b> or <b>VISIBLE</b>)
         */
        void onVisibilityChanged(int visibility);
    }

    /**
     * <p>Adapter for {@link Listener}. With it you can use all, some single, or none method from
     * Listener</p>
     */
    public static class ListenerAdapter implements Listener {

        public void onSlide(float percent) {
        }

        public void onVisibilityChanged(int visibility) {
        }
    }

    /**
     * <p>Default constructor for SlideUp</p>
     */
    public final static class Builder {

        private SlidingContentContainer slidingContentContainer;
        private State startState = HIDDEN;
        private List<Listener> listeners = new ArrayList<>();
        private boolean debug = false;
        private int autoSlideDuration = 300;
        private boolean hideKeyboard = false;
        private TimeInterpolator interpolator = new DecelerateInterpolator();

        /**
         * <p>Construct a SlideUp by passing the view or his child to use for the generation</p>
         */
        public Builder(@NonNull SlidingContentContainer sliderView) {
            this.slidingContentContainer = sliderView;
        }

        /**
         * <p>Define a start state on screen</p>
         *
         * @param startState <b>(default - <b color="#EF6C00">{@link State#HIDDEN}</b>)</b>
         */
        public Builder withStartState(@NonNull State startState) {
            this.startState = startState;
            return this;
        }

        /**
         * <p>Define a {@link Listener} for this SlideUp</p>
         *
         * @param listeners {@link List} of listeners
         */
        public Builder withListeners(@NonNull List<Listener> listeners) {
            this.listeners = listeners;
            return this;
        }

        /**
         * <p>Define a {@link Listener} for this SlideUp</p>
         *
         * @param listeners array of listeners
         */
        public Builder withListeners(@NonNull Listener... listeners) {
            List<Listener> listeners_list = new ArrayList<>();
            Collections.addAll(listeners_list, listeners);
            return withListeners(listeners_list);
        }

        /**
         * <p>Turning on/off debug logging for all handled events</p>
         *
         * @param enabled <b>(default - <b color="#EF6C00">false</b>)</b>
         */
        public Builder withLoggingEnabled(boolean enabled) {
            debug = enabled;
            return this;
        }

        /**
         * <p>Define duration of animation (whenever you use {@link #hide()} or {@link #show()}
         * methods)</p>
         *
         * @param duration <b>(default - <b color="#EF6C00">300</b>)</b>
         */
        public Builder withAutoSlideDuration(int duration) {
            autoSlideDuration = duration;
            return this;
        }

        /**
         * <p>Define behavior of soft input</p>
         *
         * @param hide <b>(default - <b color="#EF6C00">false</b>)</b>
         */
        public Builder withHideSoftInputWhenDisplayed(boolean hide) {
            hideKeyboard = hide;
            return this;
        }

        /**
         * <p>Define interpolator for animation (whenever you use {@link #hide()} or {@link #show()}
         * methods)</p>
         *
         * @param interpolator <b>(default - <b color="#EF6C00">Decelerate interpolator</b>)</b>
         */
        public Builder withInterpolator(TimeInterpolator interpolator) {
            this.interpolator = interpolator;
            return this;
        }

        /**
         * <p>
         * <b color="#EF6C00">IMPORTANT:</b>
         * If you want to restore saved parameters, place this method at the end of builder
         * </p>
         *
         * @param savedState parameters will be restored from this bundle, if it contains them
         */
        public Builder withSavedState(@Nullable Bundle savedState) {
            restoreParams(savedState);
            return this;
        }

        /**
         * <p>Build the SlideUp and add behavior to view</p>
         */
        public SlideUp build() {
            return new SlideUp(this);
        }

        /**
         * <p>Trying restore saved state</p>
         */
        private void restoreParams(@Nullable Bundle savedState) {
            if (savedState == null) {
                return;
            }
            if (savedState.getParcelable(KEY_STATE) != null) {
                startState = savedState.getParcelable(KEY_STATE);
            }
            debug = savedState.getBoolean(KEY_DEBUG, debug);
            autoSlideDuration = savedState.getInt(KEY_AUTO_SLIDE_DURATION, autoSlideDuration);
            hideKeyboard = savedState.getBoolean(KEY_HIDE_SOFT_INPUT, hideKeyboard);
        }
    }

    /**
     * <p>Trying hide soft input from window</p>
     *
     * @see InputMethodManager#hideSoftInputFromWindow(IBinder, int)
     */
    public void hideSoftInput() {
        ((InputMethodManager) slidingContentContainer.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(slidingContentContainer.getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
    }

    /**
     * <p>Trying show soft input to window</p>
     *
     * @see InputMethodManager#showSoftInput(View, int)
     */
    public void showSoftInput() {
        ((InputMethodManager) slidingContentContainer.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE))
                .showSoftInput(slidingContentContainer, 0);
    }

    private SlideUp(Builder builder) {
        listeners = builder.listeners;
        slidingContentContainer = builder.slidingContentContainer;
        startState = builder.startState;
        autoSlideDuration = builder.autoSlideDuration;
        debug = builder.debug;
        hideKeyboard = builder.hideKeyboard;
        interpolator = builder.interpolator;
        init();
    }

    private void init() {
        slidingContentContainer.setOnTouchListener(this);
        createAnimation();
        slidingContentContainer.getViewTreeObserver()
                .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        viewHeight = slidingContentContainer.getHeight();
                        slidingContentContainer.setPivotY(0);
                        updateToCurrentState();
                        ViewTreeObserver observer = slidingContentContainer.getViewTreeObserver();
                        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN) {
                            observer.removeGlobalOnLayoutListener(this);
                        } else {
                            observer.removeOnGlobalLayoutListener(this);
                        }
                    }
                });
        updateToCurrentState();
    }

    private void updateToCurrentState() {
        switch (startState) {
            case HIDDEN:
                hideImmediately();
                break;
            case SHOWED:
                showImmediately();
                break;
            case STOP:
                show();
                break;
        }
    }

    /**
     * <p>Returns the visibility status for this view.</p>
     *
     * @return true if view have status {@link View#VISIBLE}
     */
    public boolean isVisible() {
        return slidingContentContainer.getVisibility() == VISIBLE;
    }

    /**
     * <p>Add Listener which will be used in combination with this SlideUp</p>
     */
    public void addSlideListener(@NonNull Listener listener) {
        listeners.add(listener);
    }

    /**
     * <p>Remove Listener which was used in combination with this SlideUp</p>
     */
    public void removeSlideListener(@NonNull Listener listener) {
        listeners.remove(listener);
    }

    /**
     * <p>Returns typed view which was used as slider</p>
     */
    public <T extends View> T getSlidingContentContainer() {
        return (T) slidingContentContainer;
    }

    /**
     * <p>Set duration of animation (whenever you use {@link #hide()} or {@link #show()}
     * methods)</p>
     *
     * @param autoSlideDuration <b>(default - <b color="#EF6C00">300</b>)</b>
     */
    public void setAutoSlideDuration(int autoSlideDuration) {
        this.autoSlideDuration = autoSlideDuration;
    }

    /**
     * <p>Returns duration of animation (whenever you use {@link #hide()} or {@link #show()}
     * methods)</p>
     */
    public float getAutoSlideDuration() {
        return this.autoSlideDuration;
    }

    /**
     * <p>Returns running status of animation</p>
     *
     * @return true if animation is running
     */
    public boolean isAnimationRunning() {
        return valueAnimator != null && valueAnimator.isRunning();
    }

    /**
     * <p>Show view with animation</p>
     */
    public void show() {
        show(false);
    }

    /**
     * <p>Hide view with animation</p>
     */
    public void hide() {
        hide(false);
    }

    /**
     * <p>Hide view without animation</p>
     */
    public void hideImmediately() {
        hide(true);
    }

    /**
     * <p>Show view without animation</p>
     */
    public void showImmediately() {
        show(true);
    }

    /**
     * <p>Turning on/off debug logging</p>
     *
     * @param enabled <b>(default - <b color="#EF6C00">false</b>)</b>
     */
    public void setLoggingEnabled(boolean enabled) {
        debug = enabled;
    }

    /**
     * <p>Returns current status of debug logging</p>
     */
    public boolean isLoggingEnabled() {
        return debug;
    }

    /**
     * <p>Returns current interpolator</p>
     */
    public TimeInterpolator getInterpolator() {
        return interpolator;
    }

    /**
     * <p>Sets interpolator for animation (whenever you use {@link #hide()} or {@link #show()}
     * methods)</p>
     *
     * @param interpolator <b>(default - <b color="#EF6C00">Decelerate interpolator</b>)</b>
     */
    public void setInterpolator(TimeInterpolator interpolator) {
        valueAnimator.setInterpolator(this.interpolator = interpolator);
    }

    /**
     * <p>Returns current behavior of soft input</p>
     */
    public boolean isHideKeyboardWhenDisplayed() {
        return hideKeyboard;
    }

    /**
     * <p>Sets behavior of soft input</p>
     *
     * @param hide <b>(default - <b color="#EF6C00">false</b>)</b>
     */
    public void setHideKeyboardWhenDisplayed(boolean hide) {
        hideKeyboard = hide;
    }

    /**
     * <p>Toggle current state with animation</p>
     */
    public void toggle() {
        if (isVisible()) {
            hide();
        } else {
            show();
        }
    }

    /**
     * <p>Toggle current state without animation</p>
     */
    public void toggleImmediately() {
        if (isVisible()) {
            hideImmediately();
        } else {
            showImmediately();
        }
    }

    /**
     * <p>Saving current parameters of SlideUp</p>
     *
     * @return {@link Bundle} with saved parameters of SlideUp
     */
    public Bundle onSaveInstanceState(@Nullable Bundle savedState) {
        if (savedState == null) {
            savedState = Bundle.EMPTY;
        }
        savedState.putBoolean(KEY_DEBUG, debug);
        savedState.putParcelable(KEY_STATE, currentState);
        savedState.putInt(KEY_AUTO_SLIDE_DURATION, autoSlideDuration);
        savedState.putBoolean(KEY_HIDE_SOFT_INPUT, hideKeyboard);
        return savedState;
    }

    private void endAnimation() {
        if (valueAnimator.getValues() != null) {
            valueAnimator.end();
        }
    }

    private void hide(boolean immediately) {
        endAnimation();
        if (immediately) {
            if (slidingContentContainer.getHeight() > 0) {
                slidingContentContainer.setTranslationY(viewHeight);
                slidingContentContainer.setVisibility(GONE);
                notifyVisibilityChanged(GONE);
            } else {
                startState = HIDDEN;
            }
        } else {
            this.slideAnimationTo = slidingContentContainer.getHeight();
            valueAnimator
                    .setFloatValues(slidingContentContainer.getTranslationY(), slideAnimationTo);
            valueAnimator.start();
        }
    }

    private void show(boolean immediately) {
        endAnimation();
        if (immediately) {
            if (slidingContentContainer.getHeight() > 0) {
                slidingContentContainer.setTranslationY(0);
                slidingContentContainer.setVisibility(VISIBLE);
                notifyVisibilityChanged(VISIBLE);
            } else {
                startState = SHOWED;
            }
        } else {
            this.slideAnimationTo = 750;
            valueAnimator
                    .setFloatValues(slidingContentContainer.getTranslationY(), slideAnimationTo);
            valueAnimator.start();
        }
    }

    private void createAnimation() {
        valueAnimator = ValueAnimator.ofFloat();
        valueAnimator.setDuration(autoSlideDuration);
        valueAnimator.setInterpolator(interpolator);
        valueAnimator.addUpdateListener(this);
        valueAnimator.addListener(this);
    }

    @Override
    public final boolean onTouch(View v, MotionEvent event) {
        if (isAnimationRunning()) {
            return false;
        }
        return onTouchDownToUp(event);
    }

    private boolean onTouchDownToUp(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                viewHeight = slidingContentContainer.getHeight();
                startPositionY = event.getRawY();
                viewStartPositionY = slidingContentContainer.getTranslationY();
                break;
            case MotionEvent.ACTION_MOVE:
                float difference = event.getRawY() - startPositionY;
                float moveTo = viewStartPositionY + difference;
                float percents = moveTo * 100 / slidingContentContainer.getHeight();

                if (moveTo > 0) {
                    notifyPercentChanged(percents);
                    slidingContentContainer.setTranslationY(moveTo);
                }
                if (event.getRawY() > maxSlidePosition) {
                    maxSlidePosition = event.getRawY();
                }
                break;
            case MotionEvent.ACTION_UP:
                float slideAnimationFrom = slidingContentContainer.getTranslationY();
                if (slideAnimationFrom == viewStartPositionY) {
                    return false;
                }
                float stop = 0;
                if (slideAnimationFrom > 300) {
                    stop = 750;
                }
                slideAnimationTo = stop;
                valueAnimator.setFloatValues(slideAnimationFrom, stop);
                valueAnimator.start();
                maxSlidePosition = 0;
                break;
        }
        return true;
    }

    @Override
    public final void onAnimationUpdate(ValueAnimator animation) {
        float value = (float) animation.getAnimatedValue();
        onAnimationUpdateDownToUp(value);
    }

    private void onAnimationUpdateDownToUp(float value) {
        slidingContentContainer.setTranslationY(value);
        float visibleDistance = slidingContentContainer.getY() - slidingContentContainer.getTop();
        float percents = (visibleDistance) * 100 / viewHeight;
        notifyPercentChanged(percents);
    }

    private void notifyPercentChanged(float percent) {
        percent = percent > 100 ? 100 : percent;
        percent = percent < 0 ? 0 : percent;
        if (slideAnimationTo == 0 && hideKeyboard) {
            hideSoftInput();
        }
        if (listeners != null && !listeners.isEmpty()) {
            for (int i = 0; i < listeners.size(); i++) {
                Listener l = listeners.get(i);
                if (l != null) {
                    l.onSlide(percent);
                    d("Listener(" + i + ")", "(onSlide)", "value = " + percent);
                } else {
                    e("Listener(" + i + ")", "(onSlide)", "Listener is null, skip notification...");
                }
            }
        }
    }

    private void notifyVisibilityChanged(int visibility) {
        if (listeners != null && !listeners.isEmpty()) {
            for (int i = 0; i < listeners.size(); i++) {
                Listener l = listeners.get(i);
                if (l != null) {
                    l.onVisibilityChanged(visibility);
                    d("Listener(" + i + ")", "(onVisibilityChanged)",
                            "value = " + (visibility == VISIBLE ? "VISIBLE"
                                    : visibility == GONE ? "GONE" : visibility));
                } else {
                    e("Listener(" + i + ")", "(onVisibilityChanged)",
                            "Listener is null, skip  notify for him...");
                }
            }
        }
        switch (visibility) {
            case VISIBLE:
                currentState = SHOWED;
                break;
            case GONE:
                currentState = HIDDEN;
                break;
        }
    }

    @Override
    public final void onAnimationStart(Animator animator) {
        if (slidingContentContainer.getVisibility() != VISIBLE) {
            slidingContentContainer.setVisibility(VISIBLE);
            notifyVisibilityChanged(VISIBLE);
        }
    }

    @Override
    public final void onAnimationEnd(Animator animator) {
        if (slideAnimationTo != 0) {
            if (slidingContentContainer.getVisibility() != GONE) {
//                slidingContentContainer.setVisibility(GONE);
                notifyVisibilityChanged(GONE);
            }
        }
    }

    @Override
    public final void onAnimationCancel(Animator animator) {
    }

    @Override
    public final void onAnimationRepeat(Animator animator) {
    }

    private void e(String listener, String method, String message) {
        if (debug) {
            Log.e(TAG, String.format("%1$-15s %2$-23s %3$s", listener, method, message));
        }
    }

    private void d(String listener, String method, String value) {
        if (debug) {
            Log.d(TAG, String.format("%1$-15s %2$-23s %3$s", listener, method, value));
        }
    }
}
