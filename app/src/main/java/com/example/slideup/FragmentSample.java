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
    public int getStopCount() {
        return 0;
    }

    @Override
    public int getElementsCount() {
        return 0;
    }

    @Override
    public int getStopHeight() {
        return 0;
    }

    @Override
    public int getVisibleContentHeightAtStopIndex(int stop) {
        return 0;
    }

    @Override
    public int getStopIndexForVissibleHeight(int height) {
        return 0;
    }

    @Override
    public int getInitialStopPoint() {
        return 0;
    }

    @Override
    public int getInitialStopVisibleElements() {
        return 0;
    }

    @Override
    public int getInitialStopVisiblePixels() {
        return 0;
    }

    @Override
    public int calculateFollowingStop(int currentStop, int initialPosition, int displacement) {
        return 0;
    }
}
