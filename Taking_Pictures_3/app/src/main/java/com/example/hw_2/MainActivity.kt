package edu.uw.eep523.summer2021.takepictures

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.IOException
import java.util.*

//import kotlinx.android.synthetic.main.activity_main.*



class MainActivity : AppCompatActivity() {

    private var isLandScape: Boolean = false
    private var imageUri: Uri? = null
    private var imageUri2: Uri? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        getRuntimePermissions()
        if (!allPermissionsGranted()) {
            getRuntimePermissions()
        }
        isLandScape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        savedInstanceState?.let {
            imageUri = it.getParcelable(KEY_IMAGE_URI)
            imageUri2 = it.getParcelable(KEY_IMAGE_URI2)
        }
    }

    private fun getRequiredPermissions(): Array<String?> {
        return try {
            val info = this.packageManager
                .getPackageInfo(this.packageName, PackageManager.GET_PERMISSIONS)
            val ps = info.requestedPermissions
            if (ps != null && ps.isNotEmpty()) {
                ps
            } else {
                arrayOfNulls(0)
            }
        } catch (e: Exception) {
            arrayOfNulls(0)
        }
    }

    private fun allPermissionsGranted(): Boolean {
        for (permission in getRequiredPermissions()) {
            permission?.let {
                if (!isPermissionGranted(this, it)) {
                    return false
                }
            }
        }
        return true
    }

    private fun getRuntimePermissions() {
        val allNeededPermissions = ArrayList<String>()
        for (permission in getRequiredPermissions()) {
            permission?.let {
                if (!isPermissionGranted(this, it)) {
                    allNeededPermissions.add(permission)
                }
            }
        }

        if (allNeededPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this, allNeededPermissions.toTypedArray(), PERMISSION_REQUESTS)
        }
    }

    private fun isPermissionGranted(context: Context, permission: String): Boolean {
        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
            return true
        }
        return false
    }


    public override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        with(outState) {
            putParcelable(KEY_IMAGE_URI, imageUri)
            putParcelable(KEY_IMAGE_URI2, imageUri2)
        }
    }

    fun startCameraIntentForResult(view:View) {
        var previewPane = findViewById<ImageView>(R.id.previewPane)
        // Clean up last time's image
        imageUri = null
        previewPane?.setImageBitmap(null)

        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        takePictureIntent.resolveActivity(packageManager)?.let {
            val values = ContentValues()
            values.put(MediaStore.Images.Media.TITLE, "New Picture")
            values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera")
            imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        }
    }

    fun startCameraIntentForResult2(view:View) {
        var previewPane = findViewById<ImageView>(R.id.imageView5)
        // Clean up last time's image
        imageUri2 = null
        previewPane?.setImageBitmap(null)

        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        takePictureIntent.resolveActivity(packageManager)?.let {
            val values = ContentValues()
            values.put(MediaStore.Images.Media.TITLE, "New Picture2")
            values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera2")
            imageUri2 = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri2)
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE2)
        }
    }

    fun startChooseImageIntentForResult(view:View) {
        val intent1 = Intent()
        intent1.type = "image/*"
        intent1.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent1, "Select Picture"), REQUEST_CHOOSE_IMAGE)
    }

    fun startChooseImageIntentForResult2(view:View) {
        val intent2 = Intent()
        intent2.type = "image/*"
        intent2.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent2, "Select Picture2"), REQUEST_CHOOSE_IMAGE2)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            tryReloadAndDetectInImage()
        } else if (requestCode == REQUEST_IMAGE_CAPTURE2 && resultCode == RESULT_OK){
            tryReloadAndDetectInImage2()
        } else if (requestCode == REQUEST_CHOOSE_IMAGE && resultCode == RESULT_OK) {
            // In this case, imageUri is returned by the chooser, save it.
            imageUri = data!!.data
            tryReloadAndDetectInImage()
        } else if (requestCode == REQUEST_CHOOSE_IMAGE2 && resultCode == RESULT_OK){
            imageUri2 = data!!.data
            tryReloadAndDetectInImage2()
        }
    }

    private fun tryReloadAndDetectInImage() {
        var previewPane = findViewById<ImageView>(R.id.previewPane)
        try {
            if (imageUri == null) {
                return
            }
            val imageBitmap = if (Build.VERSION.SDK_INT < 29) {
                MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
            } else {
                val source = ImageDecoder.createSource(contentResolver, imageUri!!)
                ImageDecoder.decodeBitmap(source)
            }
            previewPane?.setImageBitmap(imageBitmap) //previewPane is the ImageView from the layout
        } catch (e: IOException) {
        }
    }

    private fun tryReloadAndDetectInImage2() {
        var previewPane2 = findViewById<ImageView>(R.id.imageView5)
        try {
            if (imageUri2 == null) {
                return
            }
            val imageBitmap = if (Build.VERSION.SDK_INT < 29) {
                MediaStore.Images.Media.getBitmap(contentResolver, imageUri2)
            } else {
                val source2 = ImageDecoder.createSource(contentResolver, imageUri2!!)
                ImageDecoder.decodeBitmap(source2)
            }
            previewPane2?.setImageBitmap(imageBitmap) //previewPane is the ImageView from the layout
        } catch (e: IOException) {
        }
    }
    companion object {
        private const val KEY_IMAGE_URI = "edu.uw.eep523.takepicture.KEY_IMAGE_URI"
        private const val KEY_IMAGE_URI2 = "edu.uw.eep523.takepicture.KEY_IMAGE_URI2"
        private const val REQUEST_IMAGE_CAPTURE = 1001
        private const val REQUEST_IMAGE_CAPTURE2 = 1002
        private const val REQUEST_CHOOSE_IMAGE = 1003
        private const val REQUEST_CHOOSE_IMAGE2 = 1004
        private const val PERMISSION_REQUESTS = 1
    }
}
