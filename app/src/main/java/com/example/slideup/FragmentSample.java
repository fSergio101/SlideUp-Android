package com.example.slideup;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mancj.slideup.SliderContent;

public class FragmentSample extends Fragment implements SliderContent {

    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_view, container, false);
        return view;
    }

    @Override
    public float getInitialVisibleHeight() {
        return 200;
    }

    @Override
    public int getStopCount() {
        return 4;
    }

    @Override
    public float getHeightForStop(int position) {
        return -1;
//        switch (position) {
//            case 0:
//                return getInitialVisibleHeight();
//            case 1:
//                return 400;
//            case 2:
//                return 600;
//            case 3:
//                return view.getHeight();
//            default:
//                return getInitialVisibleHeight();
//        }
    }
}
