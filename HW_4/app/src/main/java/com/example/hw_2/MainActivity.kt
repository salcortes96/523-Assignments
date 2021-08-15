package edu.uw.eep523.summer2021.takepictures

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import edu.uw.eep523.summer2021.takepictures.R

class MainActivity : AppCompatActivity(), SensorEventListener {

    private var sensorManager: SensorManager? = null

    private var Mode = false

    private var UserInpute: Int = 0
    private lateinit var SoundPlay: MediaPlayer






    private var running = false


    private var totalSteps = 0f


    private var previousTotalSteps = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loadData()
        resetSteps()
        StopCount()



        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

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


        val stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)


        if (stepSensor == null) {

            Toast.makeText(this, "No sensor detected on this device", Toast.LENGTH_SHORT).show()
        } else {
            sensorManager?.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {

        var tv_stepsTaken = findViewById<TextView>(R.id.tv_stepsTaken)

        if (running) {
            totalSteps = event!!.values[0]

            var currentSteps = totalSteps.toInt() - previousTotalSteps.toInt()

            if(Mode && currentSteps >= UserInpute){
                SoundPlay.start()
                running = false

            }
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





    private fun saveData() {

        val sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)

        val editor = sharedPreferences.edit()
        editor.putFloat("key1", previousTotalSteps)
        editor.apply()
    }

    private fun loadData() {


        val sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val savedNumber = sharedPreferences.getFloat("key1", 0f)


        Log.d("MainActivity", "$savedNumber")

        previousTotalSteps = savedNumber
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {


    }
}