/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gmail.aamnony.soundgenerator;

import android.content.Context;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.widget.RemoteViews.RemoteView;
import android.widget.TextView;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

@RemoteView
public class ChronometerEx extends TextView
{
    public static final int TICK_PERIOD = 10;

    private long mBase;
    private long mNow; // the currently displayed time
    private boolean mVisible;
    private boolean mStarted;
    private boolean mRunning;

    /**
     * Initialize this Chronometer object.
     * Sets the base to the current time.
     */
    public ChronometerEx (Context context)
    {
        super(context, null, 0);
    }

    /**
     * Initialize with standard view layout information.
     * Sets the base to the current time.
     */
    public ChronometerEx (Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    /**
     * Initialize with standard view layout information and style.
     * Sets the base to the current time.
     */
    public ChronometerEx (Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        init();
    }


    private void init ()
    {
        mBase = SystemClock.elapsedRealtime();
        updateText(mBase);
    }

    /**
     * Set the time that the count-up timer is in reference to.
     *
     * @param base Use the {@link SystemClock#elapsedRealtime} time base.
     */
    public void setBase (long base)
    {
        mBase = base;
        updateText(SystemClock.elapsedRealtime());
    }

    /**
     * Return the base time as set through {@link #setBase}.
     */
    public long getBase ()
    {
        return mBase;
    }

    /**
     * Start counting up.  This does not affect the base as set from {@link #setBase}, just
     * the view display.
     * <p>
     * Chronometer works by regularly scheduling messages to the handler, even when the
     * Widget is not visible.  To make sure resource leaks do not occur, the user should
     * make sure that each start() call has a reciprocal call to {@link #stop}.
     */
    public void start ()
    {
        setBase(SystemClock.elapsedRealtime());
        mStarted = true;
        updateRunning();
    }

    /**
     * Stop counting up.  This does not affect the base as set from {@link #setBase}, just
     * the view display.
     * <p>
     * This stops the messages to the handler, effectively releasing resources that would
     * be held as the chronometer is running, via {@link #start}.
     */
    public void stop ()
    {
        mStarted = false;
        updateRunning();
    }

    @Override
    protected void onDetachedFromWindow ()
    {
        super.onDetachedFromWindow();
        mVisible = false;
        updateRunning();
    }

    @Override
    protected void onWindowVisibilityChanged (int visibility)
    {
        super.onWindowVisibilityChanged(visibility);
        mVisible = visibility == VISIBLE;
        updateRunning();
    }

    private synchronized void updateText (long now)
    {
        mNow = now;
        String text = formatDuration(now, mBase);
        setText(text);
    }

    private void updateRunning ()
    {
        boolean running = mVisible && mStarted;
        if (running != mRunning)
        {
            if (running)
            {
                updateText(SystemClock.elapsedRealtime());
                postDelayed(mTickRunnable, TICK_PERIOD);
            }
            else
            {
                removeCallbacks(mTickRunnable);
            }
            mRunning = running;
        }
    }

    private final Runnable mTickRunnable = new Runnable()
    {
        @Override
        public void run ()
        {
            if (mRunning)
            {
                updateText(SystemClock.elapsedRealtime());
                postDelayed(mTickRunnable, TICK_PERIOD);
            }
        }
    };

    private static String formatDuration (long now, long base)
    {
        long millis = now - base;

        long s = TimeUnit.MILLISECONDS.toSeconds(millis);
        long ms = millis - TimeUnit.SECONDS.toMillis(s);

        return String.format(Locale.ENGLISH, "%02d.%03d", s, ms);
    }

    @Override
    public CharSequence getContentDescription ()
    {
        return formatDuration(mNow, mBase);
    }

    @Override
    public CharSequence getAccessibilityClassName ()
    {
        return ChronometerEx.class.getName();
    }
}
