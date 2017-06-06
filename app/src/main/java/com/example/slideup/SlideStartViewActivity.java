package com.example.slideup;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

import com.mancj.slideup.SlideUp;
import com.mancj.slideup.SliderContent;
import com.mancj.slideup.SlidingContentContainer;

public class SlideStartViewActivity extends AppCompatActivity {

    private SlidingContentContainer slidingContentContainer;
    private RelativeLayout activityView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slide_start_view);

        slidingContentContainer = (SlidingContentContainer) findViewById(
                R.id.slidingContentContainer);
        activityView = (RelativeLayout) findViewById(R.id.activityView);

        activityView.getViewTreeObserver()
                .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        float viewHeight = activityView.getHeight();
                        loadFragment(viewHeight);
                        ViewTreeObserver observer = activityView.getViewTreeObserver();
                        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN) {
                            observer.removeGlobalOnLayoutListener(this);
                        } else {
                            observer.removeOnGlobalLayoutListener(this);
                        }
                    }
                });
    }

    private void loadFragment(float viewHeight) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentSample fragment = new FragmentSample();
        fragmentManager.beginTransaction().replace(R.id.slidingContentContainer, fragment)
                .commitNow();
        initSlider(fragment, viewHeight);
    }

    private void initSlider(SliderContent fragment, float viewHeight) {
        slidingContentContainer.setSliderContent(fragment, viewHeight);
        new SlideUp.Builder(slidingContentContainer)
                .withLoggingEnabled(true)
//                .withStartState(SlideUp.State.INITIAL)
                .build();
    }

}
