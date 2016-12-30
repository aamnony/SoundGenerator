package com.gmail.aamnony.soundgenerator;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Build;

/**
 * Created by Asaf on 30/12/2016.
 */

public class Tone extends AudioTrack
{
    private static final int DEFAULT_SAMPLE_RATE_IN_HZ = 44100;
    private static final int SHORT_BYTES = Short.SIZE / Byte.SIZE;
    private int mCount;
    private double mFreqInHz = -1;
    private Type mType = null;

    public Tone (Type type, double freqInHz)
    {
        this(AudioManager.STREAM_MUSIC,
                DEFAULT_SAMPLE_RATE_IN_HZ,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                DEFAULT_SAMPLE_RATE_IN_HZ * SHORT_BYTES,
                MODE_STATIC);

        changeTo(type, freqInHz);
    }

    private Tone (int streamType, int sampleRateInHz, int channelConfig,
                  int audioFormat, int bufferSizeInBytes, int mode) throws IllegalArgumentException
    {
        super(streamType, sampleRateInHz, channelConfig, audioFormat, bufferSizeInBytes, mode);
        mCount = bufferSizeInBytes / SHORT_BYTES;
    }

    public void changeTo (Type type, double freqInHz)
    {
        if (mFreqInHz == freqInHz && mType == type)
        {
            return;
        }

        mFreqInHz = freqInHz;
        mType = type;

        short[] samples = new short[mCount];
        for (int n = 0; n < mCount; n++)
        {
            double w = 2 * Math.PI / ((double) getSampleRate() / freqInHz);
            switch (type)
            {
                default:
                case SINE:
                    samples[n] = (short) (Math.sin(w * n) * Short.MAX_VALUE);
                    break;
                case SQUARE:
                    samples[n] = (short) (Math.signum(Math.sin(w * n)) * Short.MAX_VALUE);
                    break;
                case TRIANGLE:
                    samples[n] = (short) (Math.asin(Math.sin(w * n)) * Short.MAX_VALUE);
                    break;
                case SAWTOOTH:
                    samples[n] = (short) (2 * (w * n - Math.floor(0.5 + w * n)) * Short.MAX_VALUE);
                    break;
                case SINE_OF_SINE:
                    samples[n] = (short) (Math.sin(w * Math.sin(w * n)) * Short.MAX_VALUE);
                    break;
            }
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
    public int setVolume (float gain)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            return super.setVolume(gain);
        }
        else
        {
            return setStereoVolume(gain, gain);
        }
    }

    public enum Type
    {
        SINE, SQUARE, TRIANGLE, SAWTOOTH, SINE_OF_SINE;
        public static final Type values[] = values();

        @Override
        public String toString ()
        {
            return capitalize(name()).replace("_", " ");
        }

        public static String[] names ()
        {
            String[] names = new String[values.length];
            for (int i = 0; i < names.length; i++)
            {
                names[i] = values[i].toString();
            }
            return names;
        }

        private static String capitalize(String s)
        {
            return s.substring(0,1).toUpperCase() + s.substring(1).toLowerCase();
        }
    }
}
