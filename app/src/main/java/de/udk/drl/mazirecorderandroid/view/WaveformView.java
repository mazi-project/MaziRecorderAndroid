/*
 * Copyright (C) 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package de.udk.drl.mazirecorderandroid.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.SurfaceView;


import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;

import de.udk.drl.mazirecorderandroid.R;

/**
 * A view that displays audio data on the screen as a waveform.
 */
public class WaveformView extends SurfaceView {

    // The number of buffer frames to keep around (for a nice fade-out visualization).
    private static final int HISTORY_SIZE = 8;

    // To make quieter sounds still show up well on the display, we use +/- 8192 as the amplitude
    // that reaches the top/bottom of the view instead of +/- 32767. Any samples that have
    // magnitude higher than this limit will simply be clipped during drawing.
    private static final float MAX_AMPLITUDE_TO_DRAW = 20000.0F;

    // The queue that will hold historical audio data.
    private final LinkedList<Integer> mAudioData;

    private final Paint mPaint;

    private ColorStateList backgroundColor;
    private ColorStateList strokeColor;

    private ReentrantLock lock;

    public WaveformView(Context context) {
        this(context, null, 0);
    }

    public WaveformView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WaveformView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray attrArray = context.obtainStyledAttributes(attrs, R.styleable.WaveformView, defStyle, 0);

        backgroundColor = attrArray.getColorStateList(R.styleable.WaveformView_canvasBgColor);
        strokeColor = attrArray.getColorStateList(R.styleable.WaveformView_strokeColor);

        mAudioData = new LinkedList<Integer>();

        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(strokeColor.getDefaultColor());
        mPaint.setStrokeWidth(1);
        mPaint.setAntiAlias(true);

        lock = new ReentrantLock();

        //call onDraw
        setWillNotDraw(false);
        this.invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(backgroundColor.getDefaultColor());

        float width = getWidth();
        float height = getHeight();
        float centerY = height / 2;
        float centerX = width / 2;

        // We draw the history from oldest to newest so that the older audio data is further back
        // and darker than the most recent data.
        int colorDelta = 255 / (HISTORY_SIZE + 1);
        int brightness = colorDelta;

        lock.lock();
        LinkedList<Integer> maxima = (LinkedList<Integer>) mAudioData.clone();
        lock.unlock();

        for (int max : maxima) {
            mPaint.setColor(strokeColor.getDefaultColor());
            mPaint.setAlpha(brightness);

            float radius = Math.min(width/2, (max / MAX_AMPLITUDE_TO_DRAW) * width/2);

            canvas.drawCircle(centerX,centerY, radius, mPaint);

            brightness += colorDelta;
        }
    }

    /**
     * Updates the waveform view with a new "frame" of samples and renders it. The new frame gets
     * added to the front of the rendering queue, pushing the previous frames back, causing them to
     * be faded out visually.
     */
    public synchronized void updateAudioData(int max) {

        lock.lock();
        if (mAudioData.size() >= HISTORY_SIZE)
            mAudioData.removeFirst();
        mAudioData.add(max);
        lock.unlock();

        postInvalidate();
    }

    public synchronized void clearAudioData() {
        lock.lock();
        mAudioData.clear();
        lock.unlock();

        postInvalidate();
    }

}