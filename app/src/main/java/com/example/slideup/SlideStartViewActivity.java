package com.example.slideup;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import com.mancj.slideup.SlideUp;

public class SlideStartViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slide_start_view);
        initSlider();
        loadFragment();
    }

    private void loadFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentSample fragment = new FragmentSample();
        fragmentManager.beginTransaction().replace(R.id.slidingContentContainer, fragment).commitNow();
    }

    private void initSlider() {
        new SlideUp.Builder(findViewById(R.id.slidingContentContainer))
                .withLoggingEnabled(true)
                .withTouchableArea(1000)
                .withStartState(SlideUp.State.STOP)
                .build();
    }

}
