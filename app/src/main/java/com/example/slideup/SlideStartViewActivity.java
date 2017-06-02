package com.example.slideup;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.mancj.slideup.SlideUp;

public class SlideStartViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slide_start_view);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentSample fragment = new FragmentSample();
        fragmentManager.beginTransaction().replace(R.id.fragmentContainer, fragment).commitNow();
    }

    public void setView(View view) {
        new SlideUp.Builder(view)
                .withLoggingEnabled(true)
                .withTouchableArea(1000)
                .withStartState(SlideUp.State.MIDDLE)
                .build();
    }

}
