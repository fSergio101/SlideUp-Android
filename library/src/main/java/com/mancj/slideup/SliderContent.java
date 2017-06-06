package com.mancj.slideup;

public interface SliderContent {

//    int getStopCount();
//
//    int getElementsCount();
//
//    float getStopHeight();
//
//    float getVisibleContentHeightAtStopIndex(int stop);
//
//    int getStopIndexForVissibleHeight(int height);
//
//    float getInitialStopPoint();
//
//    int getInitialStopVisibleElements();
//
//    float getInitialStopVisibleHeight();
//
//    int calculateFollowingStop(int currentStop, int initialPosition, int displacement);

    float getInitialVisibleHeight();

    int getStopCount();

    float getHeightForStop(int position);
}
