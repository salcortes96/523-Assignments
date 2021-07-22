 package com.example.taking_pictures

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*


 // arb Request Code
 private const val REQUEST_CODE = 42
 class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // not entirely how to make sure tha " it: View! " is typed in like this...
        // I just kept trying and trying until it worked to get in this format.

        btnTakePicture.setOnClickListener {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            if (takePictureIntent.resolveActivity(this.packageManager) != null){
                // the strikethrough is a visual way to identify methods marked as @Deprecated
                // meaning that it exist but you should not use it anymore
                startActivityForResult(takePictureIntent, REQUEST_CODE)
            }
            else {
                Toast.makeText( this,"Unable to open camera", Toast.LENGTH_SHORT).show()
            }
        }
    }

     override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
         if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK){
             val takenImage = data?.extras?.get("data") as Bitmap
             imageView.setImageBitmap(takenImage)
         }
         else {
             super.onActivityResult(requestCode, resultCode, data)
         }

     }

}


