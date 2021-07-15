package com.example.tipcalculator

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.AbsSeekBar
import kotlinx.android.synthetic.main.activity_main.*

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        seekBarTip.setOnSeekBarChangeListener(object: seekBar.OnSeekBarChangeListener {
            override fun onProgressChanged( seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                Log.i(TAG, msg: "onProgresChanged $progress")
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        } )
    }
}

