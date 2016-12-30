package com.gmail.aamnony.soundgenerator;

import android.content.Context;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.widget.TextView;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Custom {@link android.widget.Chronometer} for showing seconds and milliseconds only.
 *
 * @see android.widget.Chronometer
 */
public class ChronometerEx extends TextView
{
    private static final int TICK_PERIOD = 10;

    private long mBase;
    private long mNow; // the currently displayed time
    private boolean mVisible;
    private boolean mStarted;
    private boolean mRunning;

    public ChronometerEx (Context context)
    {
        super(context, null, 0);
    }

    public ChronometerEx (Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

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

    public void setBase (long base)
    {
        mBase = base;
        updateText(SystemClock.elapsedRealtime());
    }

    public long getBase ()
    {
        return mBase;
    }

    public void start ()
    {
        setBase(SystemClock.elapsedRealtime());
        mStarted = true;
        updateRunning();
    }

    public void stop ()
    {
        mStarted = false;
        updateRunning();
    }

    public void setStarted (boolean started)
    {
        if (started)
        {
            start();
        }
        else
        {
            stop();
        }
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
