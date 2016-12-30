package com.gmail.aamnony.soundgenerator;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Locale;

public class MainActivity extends Activity implements SeekBar.OnSeekBarChangeListener, Tone.OnPlaybackChangedListener
{
    private TextView txtFrequency;
    private SeekBar seekFrequency;
    private SeekBar seekVolume;
    private Spinner spinnerWaveTypes;
    private CheckBox chkHighFrequencies;
    private ChronometerEx chronometer;
    private Button btnBuzz;

    private Tone mTone;
    private AudioManager mAudioManager;

    @Override
    protected void onCreate (Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        seekFrequency.setOnSeekBarChangeListener(this);
        seekVolume.setOnSeekBarChangeListener(this);
        seekVolume.setMax(mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        spinnerWaveTypes.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, Tone.Type.names()));
        chkHighFrequencies.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged (CompoundButton buttonView, boolean isChecked)
            {
                seekFrequency.setMax(isChecked ? 20000 : 3500);
            }
        });
        btnBuzz.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch (View v, MotionEvent event)
            {
                switch (event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        mTone.changeTo(Tone.Type.values[spinnerWaveTypes.getSelectedItemPosition()], seekFrequency.getProgress());
                        mTone.play();
                        return false;
                    case MotionEvent.ACTION_UP:
                        mTone.pause();
                        return false;
                    default:
                        return true;
                }
            }
        });

        seekFrequency.setProgress(1000);
        seekVolume.setProgress(seekVolume.getMax() / 2);
        mTone = new Tone(Tone.Type.SINE, seekFrequency.getProgress(), this);
    }

    @Override
    protected void onPause ()
    {
        super.onPause();
        mTone.pause();
    }

    @Override
    protected void onDestroy ()
    {
        super.onDestroy();
        mTone.flush();
    }

    @Override
    public void onPlaybackChanged (boolean isPlaying)
    {
        chronometer.setStarted(isPlaying);
        seekFrequency.setEnabled(!isPlaying);
        spinnerWaveTypes.setEnabled(!isPlaying);
        chkHighFrequencies.setEnabled(!isPlaying);
    }

    @Override
    public void onProgressChanged (SeekBar seekBar, int progress, boolean fromUser)
    {
        switch (seekBar.getId())
        {
            case R.id.seekFrequency:
                txtFrequency.setText(String.format(Locale.ENGLISH, "%d Hz", progress));
                break;
            case R.id.seekVolume:
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
                break;
        }
    }

    @Override
    public void onStartTrackingTouch (SeekBar seekBar)
    {

    }

    @Override
    public void onStopTrackingTouch (SeekBar seekBar)
    {

    }

    private void initViews ()
    {
        txtFrequency = (TextView) findViewById(R.id.txtFrequency);
        seekFrequency = (SeekBar) findViewById(R.id.seekFrequency);
        seekVolume = (SeekBar) findViewById(R.id.seekVolume);
        spinnerWaveTypes = (Spinner) findViewById(R.id.spinnerWaveTypes);
        chkHighFrequencies = (CheckBox) findViewById(R.id.chkHighFrequencies);
        chronometer = (ChronometerEx) findViewById(R.id.chronometer);
        btnBuzz = (Button) findViewById(R.id.btnBuzz);
    }
}
