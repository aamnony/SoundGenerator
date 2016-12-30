package com.gmail.aamnony.soundgenerator;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

/**
 * Wrapper class for {@link AudioTrack}
 *
 * @see android.media.AudioTrack
 */
class Tone extends AudioTrack
{
    private static final int DEFAULT_SAMPLE_RATE_IN_HZ = 44100;
    private static final int SHORT_BYTES = Short.SIZE / Byte.SIZE;

    private int mCount;
    private double mFreqInHz = -1;
    private Type mType = null;

    private OnPlaybackChangedListener mListener;

    Tone (Type type, double freqInHz)
    {
        this(type, freqInHz, null);
    }

    Tone (Type type, double freqInHz, OnPlaybackChangedListener playbackChangedListener)
    {
        this(AudioManager.STREAM_MUSIC,
                DEFAULT_SAMPLE_RATE_IN_HZ,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                DEFAULT_SAMPLE_RATE_IN_HZ * SHORT_BYTES,
                MODE_STATIC);

        changeTo(type, freqInHz);
        mListener = playbackChangedListener;
    }

    private Tone (int streamType, int sampleRateInHz, int channelConfig,
                  int audioFormat, int bufferSizeInBytes, int mode) throws IllegalArgumentException
    {
        super(streamType, sampleRateInHz, channelConfig, audioFormat, bufferSizeInBytes, mode);
        mCount = bufferSizeInBytes / SHORT_BYTES;
    }

    void changeTo (Type type, double freqInHz)
    {
        if (mFreqInHz == freqInHz && mType == type)
        {
            return;
        }
        mFreqInHz = freqInHz;
        mType = type;

        flush();
        short[] samples = new short[mCount];
        double f = freqInHz / getSampleRate();
        for (int n = 0; n < mCount; n++)
        {
            samples[n] = (short) (Short.MAX_VALUE * mType.normalizedSample(f, n));
        }
        write(samples, 0, mCount);
        int code = setLoopPoints(0, mCount, -1);

        switch (code)
        {
            case ERROR_BAD_VALUE:
                throw new RuntimeException("Bad value, count = " + mCount);
            case ERROR_INVALID_OPERATION:
                throw new RuntimeException("Invalid operation");
        }
    }

    @Override
    public void play () throws IllegalStateException
    {
        super.play();
        if (mListener != null)
        {
            mListener.onPlaybackChanged(true);
        }
    }

    @Override
    public void pause () throws IllegalStateException
    {
        super.pause();
        if (mListener != null)
        {
            mListener.onPlaybackChanged(false);
        }
    }

    @Override
    public void stop () throws IllegalStateException
    {
        super.stop();
        if (mListener != null)
        {
            mListener.onPlaybackChanged(false);
        }
    }

    public OnPlaybackChangedListener getListener ()
    {
        return mListener;
    }

    public void setListener (OnPlaybackChangedListener listener)
    {
        mListener = listener;
    }

    interface OnPlaybackChangedListener
    {
        void onPlaybackChanged (boolean isPlaying);
    }

    enum Type
    {
        SINE
                {
                    @Override
                    public double normalizedSample (double f, int n)
                    {
                        return Math.sin(2 * Math.PI * f * n);
                    }
                },
        SQUARE
                {
                    @Override
                    public double normalizedSample (double f, int n)
                    {
                        return Math.signum(Math.sin(2 * Math.PI * f * n));
                    }
                },
        TRIANGLE
                {
                    @Override
                    public double normalizedSample (double f, int n)
                    {
                        return 2 * Math.asin(Math.sin(2 * Math.PI * f * n)) / Math.PI;
                    }
                },
        SAWTOOTH
                {
                    @Override
                    public double normalizedSample (double f, int n)
                    {
                        return 2 * (f * n - Math.floor(f * n + 0.5));
                    }
                },;

        public static final Type values[] = values();

        @Override
        public String toString ()
        {
            return capitalize(name()).replace("_", " ");
        }

        public abstract double normalizedSample (double f, int n);

        public static String[] names ()
        {
            String[] names = new String[values.length];
            for (int i = 0; i < names.length; i++)
            {
                names[i] = values[i].toString();
            }
            return names;
        }

        private static String capitalize (String s)
        {
            return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
        }
    }
}
