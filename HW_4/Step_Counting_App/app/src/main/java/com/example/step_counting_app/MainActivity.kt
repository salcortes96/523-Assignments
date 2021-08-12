package com.example.step_counting_app

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import android.widget.*
import android.media.MediaPlayer



class MainActivity : AppCompatActivity(), SensorEventListener {
    // Added SensorEventListener the MainActivity class
    // Implement all the members in the class MainActivity
    // after adding SensorEventListener

    // we have assigned sensorManger to nullable
    private var sensorManager: SensorManager? = null

    // var assigned sensorMode to false
    private var Mode = false
    private var UserInpute: Int = 0
    private lateinit var SoundPlay: MediaPlayer

    // Creating a variable which will give the running status
    // and initially given the boolean value as false
    private var running = false

    // Creating a variable which will counts total steps
    // and it has been given the value of 0 float
    private var totalSteps = 0f

    // Creating a variable  which counts previous total
    // steps and it has also been given the value of 0 float
    private var previousTotalSteps = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loadData()
        resetSteps()
        StopCount()

        // Adding a context of SENSOR_SERVICE aas Sensor Manager
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        // Adding context to MediaPlayer
        SoundPlay = MediaPlayer.create(this, R.raw.pristine)

        val sw = findViewById<Switch>(R.id.switch1)

        sw?.setOnCheckedChangeListener { _, isChecked ->
            Mode = isChecked
        }

        val InputeSteps = findViewById<EditText>(R.id.editTextNumber)
        val ButtonGo = findViewById<Button>(R.id.button)

        ButtonGo.setOnClickListener {
            UserInpute = InputeSteps.text.toString().toInt();
        }


    }

    override fun onResume() {
        super.onResume()
        running = true

        // Returns the number of steps taken by the user since the last reboot while activated
        // This sensor requires permission android.permission.ACTIVITY_RECOGNITION.
        // So don't forget to add the following permission in AndroidManifest.xml present in manifest folder of the app.
        val stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)


        if (stepSensor == null) {
            // This will give a toast message to the user if there is no sensor in the device
            Toast.makeText(this, "No sensor detected on this device", Toast.LENGTH_SHORT).show()
        } else {
            // Rate suitable for the user interface
            sensorManager?.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {

        // Calling the TextView that we made in activity_main.xml
        // by the id given to that TextView
        var tv_stepsTaken = findViewById<TextView>(R.id.tv_stepsTaken)

        if (running) {
            totalSteps = event!!.values[0]

            // Current steps are calculated by taking the difference of total steps
            // and previous steps
            val currentSteps = totalSteps.toInt() - previousTotalSteps.toInt()

            if(Mode && currentSteps >= UserInpute){
                SoundPlay.start()
                running = false

            }
            // It will show the current steps to the user
            tv_stepsTaken.text = ("$currentSteps")
        }
    }

    fun resetSteps() {
        var tv_stepsTaken = findViewById<TextView>(R.id.tv_stepsTaken)
        var button2 = findViewById<TextView>(R.id.button2)

        button2.setOnClickListener {
            previousTotalSteps = totalSteps
            tv_stepsTaken.text = 0.toString()
            saveData()
            previousTotalSteps = totalSteps
            tv_stepsTaken.text = 0.toString()
            saveData()
            running = true

        }

    }

    fun StopCount(){
        var button3 = findViewById<TextView>(R.id.button3)
        button3.setOnClickListener {
            running = false
        }
    }

/*        //tv_stepsTaken.setOnClickListener {
            // This will give a toast message if the user want to reset the steps
            Toast.makeText(this, "Long tap to reset steps", Toast.LENGTH_SHORT).show()
        }

        tv_stepsTaken.setOnLongClickListener {

            previousTotalSteps = totalSteps

            // When the user will click long tap on the screen,
            // the steps will be reset to 0
            tv_stepsTaken.text = 0.toString()

            // This will save the data
            saveData()

            true
        }
    }*/

    private fun saveData() {

        // Shared Preferences will allow us to save
        // and retrieve data in the form of key,value pair.
        // In this function we will save data
        val sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)

        val editor = sharedPreferences.edit()
        editor.putFloat("key1", previousTotalSteps)
        editor.apply()
    }

    private fun loadData() {

        // In this function we will retrieve data
        val sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val savedNumber = sharedPreferences.getFloat("key1", 0f)

        // Log.d is used for debugging purposes
        Log.d("MainActivity", "$savedNumber")

        previousTotalSteps = savedNumber
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // We do not have to write anything in this function for this app
    }
}