package com.gmail.aamnony.soundgenerator;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

public class MainActivity extends Activity implements SeekBar.OnSeekBarChangeListener
{
    private TextView txtFrequency;
    private SeekBar seekFrequency;
    private TextView txtVolume;
    private SeekBar seekVolume;
    private Spinner spinnerWaveTypes;
    private ChronometerEx chronometer;
    private Button btnBuzz;

    private Tone mTone;

    @Override
    protected void onCreate (Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();

        seekFrequency.setOnSeekBarChangeListener(this);
        seekVolume.setOnSeekBarChangeListener(this);

        btnBuzz.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch (View v, MotionEvent event)
            {
                switch (event.getAction())
                {
                    //case MotionEvent.ACTION_BUTTON_PRESS:
                    case MotionEvent.ACTION_DOWN:
                        Log.d("buzz", "press");
                        mTone.changeTo(Tone.Type.values[spinnerWaveTypes.getSelectedItemPosition()], seekFrequency.getProgress());
                        mTone.setVolume(seekVolume.getProgress() / 100.0f);
                        mTone.play();
                        chronometer.start();
                        seekFrequency.setEnabled(false);
                        spinnerWaveTypes.setEnabled(false);
                        return true;
                    default:
                        //case MotionEvent.ACTION_BUTTON_RELEASE:
                    case MotionEvent.ACTION_UP:
                        Log.d("buzz", "release");
                        mTone.pause();
                        chronometer.stop();
                        seekFrequency.setEnabled(true);
                        spinnerWaveTypes.setEnabled(true);
                        return true;
                }
            }
        });

        spinnerWaveTypes.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, Tone.Type.names()));

        seekFrequency.setProgress(1000);
        seekVolume.setProgress(100);
        mTone = new Tone(Tone.Type.SINE, seekFrequency.getProgress());
    }

    @Override
    protected void onPause ()
    {
        super.onPause();
        mTone.pause();
        seekFrequency.setEnabled(true);
        spinnerWaveTypes.setEnabled(true);
        chronometer.stop();
    }

    @Override
    protected void onDestroy ()
    {
        super.onDestroy();
        mTone.flush();
    }

    @Override
    public void onProgressChanged (SeekBar seekBar, int progress, boolean fromUser)
    {
        switch (seekBar.getId())
        {
            case R.id.seekFrequency:
                txtFrequency.setText(progress + " Hz");
                break;
            case R.id.seekVolume:
                txtVolume.setText(progress + "%");
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
        txtVolume = (TextView) findViewById(R.id.txtVolume);
        seekVolume = (SeekBar) findViewById(R.id.seekVolume);
        spinnerWaveTypes = (Spinner) findViewById(R.id.spinnerWaveTypes);
        chronometer = (ChronometerEx) findViewById(R.id.chronometer);
        btnBuzz = (Button) findViewById(R.id.btnBuzz);
    }
}
