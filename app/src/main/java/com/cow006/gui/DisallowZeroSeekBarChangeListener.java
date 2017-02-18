package com.cow006.gui;

import android.widget.SeekBar;

public class DisallowZeroSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
    }

    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    public void onStopTrackingTouch(SeekBar seekBar) {
        seekBar.setProgress(Math.max(seekBar.getProgress(), 1));
    }
}
