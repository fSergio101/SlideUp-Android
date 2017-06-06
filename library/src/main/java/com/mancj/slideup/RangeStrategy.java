package com.mancj.slideup;

import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

class RangeStrategy extends SliderFuturePositionStrategy {

    List<Pair<Float, Float>> ranges = new ArrayList();

    public RangeStrategy(SliderContent sliderContent, float maxSize) {
        super(sliderContent, maxSize);
        initRanges();
    }

    private void initRanges() {
        ranges.add(new Pair(0.0f, sliderContent.getInitialVisibleHeight()));
        for (int i = 1; i < sliderContent.getStopCount(); i++) {
            ranges.add(new Pair(ranges.get(i - 1).second, calculateRangeForPosition(i)));
        }
    }

    private Float calculateRangeForPosition(int position) {
        if (sliderContent.getHeightForStop(position) != -1) {
            return sliderContent.getHeightForStop(position);
        }

        float stoppableHeight = maxSize - sliderContent
                .getInitialVisibleHeight();
        float stopHeight = stoppableHeight / (sliderContent.getStopCount() - 1);
        return sliderContent.getInitialVisibleHeight() + (stopHeight * position);
    }

    @Override
    public SliderFuturePosition calculateStopPosition(
            float viewStartPositionY,
            float endDragPosition,
            SlideUp.State currentState) {
        Pair<Float, Float> positionRange = getEndDragPositionRange(endDragPosition);
        float position = getClosestValueToEndOfDrag(positionRange, endDragPosition);
        return new SliderFuturePosition(
                maxSize,
                sliderContent.getInitialVisibleHeight(),
                position);
    }

    private float getClosestValueToEndOfDrag(Pair<Float, Float> positionRange,
            float endDragPosition) {
        float rangeTopValue = normalize(positionRange.second);
        float rangeBottomValue = normalize(positionRange.first);
        float topDifference = Math.abs(endDragPosition - rangeTopValue);
        float bottomDifference = Math.abs(endDragPosition - rangeBottomValue);
        if (topDifference <= bottomDifference) {
            return rangeTopValue;
        } else {
            return rangeBottomValue;
        }
    }

    private Pair<Float, Float> getEndDragPositionRange(float endDragPosition) {
        for (Pair<Float, Float> range : ranges) {
            if (endDragPosition >= normalize(range.second)
                    && endDragPosition < normalize(range.first)) {
                return range;
            }
        }
        return ranges.get(0);

    }

}
