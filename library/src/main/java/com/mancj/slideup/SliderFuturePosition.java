package com.mancj.slideup;

class SliderFuturePosition {

    private float maxPosition;
    private float minPosition;
    private float position;

    public SliderFuturePosition(float maxPosition, float minPosition, float position) {
        this.position = position;
        this.maxPosition = maxPosition;
        this.minPosition = minPosition;
    }

    public boolean isMax() {
        return position == maxPosition;
    }

    public boolean isMin() {
        return position == minPosition;
    }

    public float getPosition() {
        return position;
    }
}
