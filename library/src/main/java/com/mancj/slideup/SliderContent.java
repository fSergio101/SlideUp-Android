package com.mancj.slideup;

public interface SliderContent {

    int getStopCount();

    int getElementsCount();

    int getStopHeight();

    int getVisibleContentHeightAtStopIndex(int stop);

    int getStopIndexForVissibleHeight(int height);

    int getInitialStopPoint();

    int getInitialStopVisibleElements();

    int getInitialStopVisiblePixels();

    int calculateFollowingStop(int currentStop, int initialPosition, int displacement);

}
