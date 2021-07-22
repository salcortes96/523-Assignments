package com.example.taking_pictures_2

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts

class MainActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView

    private var our_request_code : Int = 123 //given

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        imageView = findViewById(R.id.image)
    }

    // get a variable

    private val getAction = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        // get bitmap data for image
        val bitmap = it?.data?.extras?.get("data") as Bitmap
        imageView.setImageBitmap(bitmap)
    }

    fun takePhoto(view: View){
        //start an intent to capture image

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        //start the result
        //check if the task can be performed or not
        if(intent.resolveActivity(packageManager) != null){
            getAction.launch(intent)
        }
    }




}