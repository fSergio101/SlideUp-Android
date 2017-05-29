package com.example.slideup;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.mancj.slideup.SlideUp;

public class SlideStartViewActivity extends AppCompatActivity {

    private SlideUp slideUp;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slide_start_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentSample fragment = new FragmentSample();
        fragmentManager.beginTransaction().replace(R.id.fragmentContainer, fragment).commitNow();

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                slideUp.show();
                fab.hide();
            }
        });

    }

    public void setView(View view) {
        slideUp = new SlideUp.Builder(view)
                .withListeners(new SlideUp.Listener() {
                    @Override
                    public void onSlide(float percent) {
                    }

                    @Override
                    public void onVisibilityChanged(int visibility) {
                        if (visibility == View.GONE) {
                            fab.show();
                        }
                    }
                })
                .withLoggingEnabled(true)
                .withStartState(SlideUp.State.HIDDEN)
                .build();
    }

}
