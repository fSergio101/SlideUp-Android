package com.mancj.slideup;

abstract class SliderFuturePositionStrategy {

    protected final float maxSize;
    protected SliderContent sliderContent;

    protected SliderFuturePositionStrategy(SliderContent sliderContent, float maxSize) {
        this.sliderContent = sliderContent;
        this.maxSize = maxSize;
    }

    abstract SliderFuturePosition calculateStopPosition(
            float viewStartPositionY,
            float slideAnimationFrom,
            SlideUp.State currentState);

    public SliderFuturePosition getInitialState() {
        return new SliderFuturePosition(
                maxSize,
                sliderContent.getInitialVisibleHeight(),
                normalize(sliderContent.getInitialVisibleHeight())
        );
    }

    protected float normalize(float measureToBeNormalized) {
        return maxSize - measureToBeNormalized;
    }
}
