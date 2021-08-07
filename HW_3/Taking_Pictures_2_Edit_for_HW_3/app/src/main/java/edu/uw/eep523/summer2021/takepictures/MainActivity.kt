package edu.uw.eep523.summer2021.takepictures

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import java.util.*
import com.google.firebase.ml.vision.face.FirebaseVisionFace as FirebaseVisionFace1
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions as FirebaseVisionFaceDetectorOptions1


class MainActivity : AppCompatActivity() {

    companion object {
        private const val KEY_IMAGE_URI = "edu.uw.eep523.takepicture.KEY_IMAGE_URI"
        private const val REQUEST_IMAGE_CAPTURE = 1001
        private const val REQUEST_CHOOSE_IMAGE = 1003
        private const val KEY_IMAGE_URI2 = "edu.uw.eep523.takepicture.KEY_IMAGE_URI2"
        private const val REQUEST_IMAGE_CAPTURE2 = 1002
        private const val REQUEST_CHOOSE_IMAGE2 = 1004
        private const val PERMISSION_REQUESTS = 1

    }

    private var isLandScape: Boolean = false
    private var imageUri: Uri? = null
    private var imageUri2: Uri? = null


    private var cropActivityResultContract = object : ActivityResultContract<Any?, Uri?>(){
        override fun createIntent(context: Context, input: Any?): Intent {
            return CropImage.activity()
                .setAspectRatio(16,9)
                .getIntent(this@MainActivity)
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
            return CropImage.getActivityResult(intent)?.uri
        }
    }

    private lateinit var cropActivityResultLauncher: ActivityResultLauncher<Any?>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView((R.layout.activity_main))
    }


    // ************************  get all permissions here  ***********************

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
        if (ContextCompat.checkSelfPermission(
                context,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        ) {
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


    fun startChooseImageIntentForResult1(view: View) {
        val intent1 = Intent()
        intent1.type = "image/*"
        intent1.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(
            Intent.createChooser(intent1, "Select Picture 1 for Editing"),
            REQUEST_CHOOSE_IMAGE
        )
    }

    fun startChooseImageIntentForResult2(view: View) {
        val intent2 = Intent()
        intent2.type = "image/*"
        intent2.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(
            Intent.createChooser(intent2, "Select Picture 2 for Editing"),
            REQUEST_CHOOSE_IMAGE2
        )
    }


    // return home function
    fun goBack(view: View){
        var previewPane1 = findViewById<ImageView>(R.id.image1)
        var previewPane2 = findViewById<ImageView>(R.id.image2)
        var imageBitmap1: Bitmap
        var imageBitmap2: Bitmap

        try {
            if (imageUri == null) {
                return
            }
            if (Build.VERSION.SDK_INT < 29) {
                imageBitmap1 = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                imageBitmap2 = MediaStore.Images.Media.getBitmap(contentResolver, imageUri2)
//                 OriginImageBitmap1 = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
//                 OriginImageBitmap2 = MediaStore.Images.Media.getBitmap(contentResolver, imageUri2)
            } else {
                val source = ImageDecoder.createSource(contentResolver, imageUri!!)
                imageBitmap1 = ImageDecoder.decodeBitmap((source))
                val source2 = ImageDecoder.createSource(contentResolver, imageUri2!!)
                imageBitmap2 = ImageDecoder.decodeBitmap((source2))
            }
        } catch (e: IOException) {
            return
        }
        image1.background = BitmapDrawable(getResources(), imageBitmap1)
        image2.background = BitmapDrawable(getResources(), imageBitmap2)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            tryReloadAndDetectInImage()
        } else if (requestCode == REQUEST_IMAGE_CAPTURE2 && resultCode == Activity.RESULT_OK) {
            tryReloadAndDetectInImage2()
        } else if (requestCode == REQUEST_CHOOSE_IMAGE && resultCode == Activity.RESULT_OK) {
            // In this case, imageUri is returned by the chooser, save it.
            imageUri = data!!.data
            tryReloadAndDetectInImage()
        } else if (requestCode == REQUEST_CHOOSE_IMAGE2 && resultCode == Activity.RESULT_OK) {
            imageUri2 = data!!.data
            tryReloadAndDetectInImage2()
        }
    }

    private fun tryReloadAndDetectInImage() {
        var image = findViewById<ImageView>(R.id.image1)
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
            image.background = BitmapDrawable(getResources(), imageBitmap)
        } catch (e: IOException) {
        }
    }

    private fun tryReloadAndDetectInImage2() {
        var previewPane2 = findViewById<ImageView>(R.id.image2)
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
            image2.background = BitmapDrawable(getResources(), imageBitmap)
        } catch (e: IOException) {
        }
    }


    fun detectFace(image: FirebaseVisionImage): MutableList<FirebaseVisionFace1>? {
        //val imageBitmap: Bitmap? = null

        val highAccuracyOpts = FirebaseVisionFaceDetectorOptions1.Builder()
            .setPerformanceMode(FirebaseVisionFaceDetectorOptions1.ACCURATE)
            .setLandmarkMode(FirebaseVisionFaceDetectorOptions1.ALL_LANDMARKS)
            .setClassificationMode(FirebaseVisionFaceDetectorOptions1.ALL_CLASSIFICATIONS)
            .build()
//        val image =
//            imageBitmap?.let { FirebaseVisionImage.fromBitmap(it.copy(Bitmap.Config.ARGB_8888,true)) }
        val detector = FirebaseVision.getInstance()
            .getVisionFaceDetector(highAccuracyOpts)
        val result = detector.detectInImage(image)
            .addOnSuccessListener { faces ->
                // Task completed successfully
                // ...
                for (face in faces) {
                    val bounds = face.boundingBox
                }

            }
            .addOnFailureListener { e ->
                // Task failed with an exception
                // ...

            }
        while (!result.isComplete) {
        }
        return result.result
    }

    fun swapFace(view: View){
        var previewPane1 = findViewById<ImageView>(R.id.image1)
        var previewPane2 = findViewById<ImageView>(R.id.image2)

        val scaleFactor = 3;
        var imageBitmap1: Bitmap
        var imageBitmap2: Bitmap

        try {
            if (imageUri == null) {
                return
            }
            if (Build.VERSION.SDK_INT < 29) {
                imageBitmap1 = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                imageBitmap2 = MediaStore.Images.Media.getBitmap(contentResolver, imageUri2)
            } else {
                val source = ImageDecoder.createSource(contentResolver, imageUri!!)
                imageBitmap1 = ImageDecoder.decodeBitmap((source))
                val source2 = ImageDecoder.createSource(contentResolver, imageUri2!!)
                imageBitmap2 = ImageDecoder.decodeBitmap((source2))
            }
        } catch (e: IOException) {
            return
        }
        imageBitmap1 = Bitmap.createScaledBitmap(imageBitmap1, (imageBitmap1.width / scaleFactor),(imageBitmap1.height / scaleFactor),true)
        imageBitmap2 = Bitmap.createScaledBitmap(imageBitmap2, (imageBitmap2.width / scaleFactor),(imageBitmap2.height / scaleFactor),true)

        val image1 = FirebaseVisionImage.fromBitmap(imageBitmap1.copy(Bitmap.Config.ARGB_8888,true))
        val image2 = FirebaseVisionImage.fromBitmap(imageBitmap2.copy(Bitmap.Config.ARGB_8888,true))

        val imageBitmap1temp = imageBitmap1.copy(Bitmap.Config.ARGB_8888,true)
        val imageBitmap2temp = imageBitmap2.copy(Bitmap.Config.ARGB_8888,true)

        val faces1 = detectFace(image1)
        val faces2 = detectFace(image2)

        val bounds1 = faces1!![0].boundingBox
        val bounds2 = faces2!![0].boundingBox
        val width1 = bounds1.width()
        val width2 = bounds2.width()
        val height1 = bounds1.height()
        val height2 = bounds2.height()
        val height: Int
        val width: Int

        val x1: Int
        val x2: Int
        val y1: Int
        val y2: Int

        if (width1 > width2){
            x1 = bounds1.left + (width1 - width2)/2
            x2 = bounds2.left
            width = width2
        }else{
            x1 = bounds1.left
            x2 = bounds2.left + (width2 - width1)/2
            width = width1
        }

        if (height1 > height2){
            y1 = bounds1.top - (height1 - height2)/2
            y2 = bounds2.top
            height = height2
        }else{
            y1 = bounds1.top
            y2 = bounds2.top - (height2 - height1)/2
            height = height1
        }


        for (i in 0..height){
            for(j in 0..width) {
                val temp = imageBitmap1temp.getPixel(x1 + j, y1 + i)
                val new_x1 = x1 + j
                val new_y1 = y1 + i
                val new_x2 = x2 + j
                val new_y2 = y2 + i
                imageBitmap1temp.setPixel(new_x1, new_y1, imageBitmap2temp.getPixel(new_x2, new_y2))
                imageBitmap2temp.setPixel(new_x2, new_y2, temp)
            }
        }


        previewPane1.background = BitmapDrawable(getResources(), imageBitmap1temp)
        previewPane2.background = BitmapDrawable(getResources(), imageBitmap2temp)
    }

    fun blurFace(view: View){



        var previewPane1 = findViewById<ImageView>(R.id.image1)
        var previewPane2 = findViewById<ImageView>(R.id.image2)

        val scaleFactor = 3;
        var imageBitmap1: Bitmap
        var imageBitmap2: Bitmap

        try {
            if (imageUri == null) {
                return
            }
            if (Build.VERSION.SDK_INT < 29) {
                imageBitmap1 = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                imageBitmap2 = MediaStore.Images.Media.getBitmap(contentResolver, imageUri2)
            } else {
                val source = ImageDecoder.createSource(contentResolver, imageUri!!)
                imageBitmap1 = ImageDecoder.decodeBitmap((source))
                val source2 = ImageDecoder.createSource(contentResolver, imageUri2!!)
                imageBitmap2 = ImageDecoder.decodeBitmap((source2))
            }
        } catch (e: IOException) {
            return
        }
        imageBitmap1 = Bitmap.createScaledBitmap(imageBitmap1, (imageBitmap1.width / scaleFactor),(imageBitmap1.height / scaleFactor),true)
        imageBitmap2 = Bitmap.createScaledBitmap(imageBitmap2, (imageBitmap2.width / scaleFactor),(imageBitmap2.height / scaleFactor),true)

        val image1 = FirebaseVisionImage.fromBitmap(imageBitmap1.copy(Bitmap.Config.ARGB_8888,true))
        val image2 = FirebaseVisionImage.fromBitmap(imageBitmap2.copy(Bitmap.Config.ARGB_8888,true))

        val imageBitmap1temp = imageBitmap1.copy(Bitmap.Config.ARGB_8888,true)
        val imageBitmap2temp = imageBitmap2.copy(Bitmap.Config.ARGB_8888,true)

        val faces1 = detectFace(image1)
        val faces2 = detectFace(image2)

        val bounds1 = faces1!![0].boundingBox
        val bounds2 = faces2!![0].boundingBox
        val width1 = bounds1.width()
        val width2 = bounds2.width()
        val height1 = bounds1.height()
        val height2 = bounds2.height()
        val height: Int
        val width: Int

        val x1: Int
        val x2: Int
        val y1: Int
        val y2: Int

        if (width1 > width2){
            x1 = bounds1.left + (width1 - width2)/2
            x2 = bounds2.left
            width = width2
        }else{
            x1 = bounds1.left
            x2 = bounds2.left + (width2 - width1)/2
            width = width1
        }

        if (height1 > height2){
            y1 = bounds1.top - (height1 - height2)/2
            y2 = bounds2.top
            height = height2
        }else{
            y1 = bounds1.top
            y2 = bounds2.top - (height2 - height1)/2
            height = height1
        }


        val blur1 = bitmapBlur(imageBitmap1temp,1.toFloat(),20)!!
        val blur2 = bitmapBlur(imageBitmap2temp,1.toFloat(),20)!!

        for(i in 0..width1) {
            for(j in 0..height1) {
                imageBitmap1temp.setPixel(i + x1, j + y1, blur1!!.getPixel(i + x1, j + y1))
            }
        }

        for(i in 0..width2) {
            for(j in 0..height2) {
                imageBitmap2temp.setPixel(i + x2, j + y2, blur2!!.getPixel(i+ x2, j + y2))
            }
        }


        previewPane1.background = BitmapDrawable(getResources(), imageBitmap1temp)
        previewPane2.background = BitmapDrawable(getResources(), imageBitmap2temp)
    }



    fun bitmapBlur(sentBitmap: Bitmap, scale: Float, radius: Int): Bitmap? {
        var sentBitmap = sentBitmap
        val width = Math.round(sentBitmap.width * scale)
        val height = Math.round(sentBitmap.height * scale)
        sentBitmap = Bitmap.createScaledBitmap(sentBitmap, width, height, false)
        val bitmap = sentBitmap.copy(sentBitmap.config, true)
        if (radius < 1) {
            return null
        }

        val w = bitmap.width
        val h = bitmap.height

        val pix = IntArray(w * h)
        Log.e("pix", w.toString() + " " + h + " " + pix.size)
        bitmap.getPixels(pix, 0, w, 0, 0, w, h)

        val wm = w -1
        val hm = h -1
        val wh = w * h
        val div = radius + radius + 1

        val r = IntArray(wh)
        val g = IntArray(wh)
        val b = IntArray(wh)
        var rsum: Int
        var gsum: Int
        var bsum: Int
        var x: Int
        var y: Int
        var i: Int
        var p: Int
        var yp: Int
        var yi: Int
        var yw: Int
        val vmin = IntArray(Math.max(w, h))

        var divsum = div + 1 shr 1

        divsum *= divsum
        val dv = IntArray(256 * divsum)
        i = 0
        while (i < 256 * divsum) {
            dv[i] = i / divsum
            i++
        }

        yi = 0
        yw = yi

        val stack = Array(div) { IntArray(3) }
        var stackpointer: Int
        var stackstart: Int
        var sir: IntArray
        var rbs: Int
        val r1 = radius + 1
        var routsum: Int
        var goutsum: Int
        var boutsum: Int
        var rinsum: Int
        var ginsum: Int
        var binsum: Int

        y = 0
        while (y < h) {
            bsum = 0
            gsum = bsum
            rsum = gsum
            boutsum = rsum
            goutsum = boutsum
            routsum = goutsum
            binsum = routsum
            ginsum = binsum
            rinsum = ginsum
            i = -radius
            while (i <= radius) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))]
                sir = stack[i + radius]
                sir[0] = p and 0xff0000 shr 16
                sir[1] = p and 0x00ff00 shr 8
                sir[2] = p and 0x0000ff
                rbs = r1 -Math.abs(i)
                rsum += sir[0] * rbs
                gsum += sir[1] * rbs
                bsum += sir[2] * rbs
                if (i > 0) {
                    rinsum += sir[0]
                    ginsum += sir[1]
                    binsum += sir[2]
                } else {
                    routsum += sir[0]
                    goutsum += sir[1]
                    boutsum += sir[2]
                }
                i++
            }
            stackpointer = radius
            x = 0
            while (x < w) {
                r[yi] = dv[rsum]
                g[yi] = dv[gsum]
                b[yi] = dv[bsum]
                rsum -= routsum
                gsum -= goutsum
                bsum -= boutsum

                stackstart = stackpointer -radius + div
                sir = stack[stackstart % div]
                routsum -= sir[0]
                goutsum -= sir[1]
                boutsum -= sir[2]

                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm)
                }
                p = pix[yw + vmin[x]]
                sir[0] = p and 0xff0000 shr 16
                sir[1] = p and 0x00ff00 shr 8
                sir[2] = p and 0x0000ff

                rinsum += sir[0]
                ginsum += sir[1]
                binsum += sir[2]

                rsum += rinsum
                gsum += ginsum
                bsum += binsum

                stackpointer = (stackpointer + 1) % div
                sir = stack[stackpointer % div]

                routsum += sir[0]
                goutsum += sir[1]
                boutsum += sir[2]
                rinsum -= sir[0]
                ginsum -= sir[1]
                binsum -= sir[2]

                yi++
                x++
            }
            yw += w
            y++
        }
        x = 0
        while (x < w) {
            bsum = 0
            gsum = bsum
            rsum = gsum
            boutsum = rsum
            goutsum = boutsum
            routsum = goutsum
            binsum = routsum
            ginsum = binsum
            rinsum = ginsum
            yp = -radius * w
            i = -radius
            while (i <= radius) {
                yi = Math.max(0, yp) + x
                sir = stack[i + radius]
                sir[0] = r[yi]
                sir[1] = g[yi]
                sir[2] =b[yi]

                rbs = r1 -Math.abs(i)

                rsum += r[yi] * rbs
                gsum += g[yi] * rbs
                bsum += b[yi] * rbs

                if (i > 0) {
                    rinsum += sir[0]
                    ginsum += sir[1]
                    binsum += sir[2]
                } else {
                    routsum += sir[0]
                    goutsum += sir[1]
                    boutsum += sir[2]
                }

                if (i < hm) {
                    yp += w
                }
                i++
            }
            yi = x
            stackpointer = radius
            y = 0
            while (y < h) {
                // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                pix[yi] = -0x1000000 and pix[yi] or (dv[rsum] shl 16) or (dv[gsum] shl 8) or dv[bsum]

                rsum -= routsum
                gsum -= goutsum
                bsum -= boutsum
                stackstart = stackpointer -radius + div
                sir = stack[stackstart % div]
                routsum -= sir[0]
                goutsum -= sir[1]
                boutsum -= sir[2]

                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w
                }

                p = x + vmin[y]

                sir[0] = r[p]
                sir[1] = g[p]
                sir[2] = b[p]
                rinsum += sir[0]
                ginsum += sir[1]
                binsum += sir[2]
                rsum += rinsum
                gsum += ginsum
                bsum += binsum

                stackpointer = (stackpointer + 1) % div
                sir = stack[stackpointer]
                routsum += sir[0]
                goutsum += sir[1]
                boutsum += sir[2]
                rinsum -= sir[0]
                ginsum -= sir[1]
                binsum -= sir[2]
                yi += w
                y++
            }
            x++
        }
        Log.e("pix", w.toString() + " " + h + " " + pix.size)
        bitmap.setPixels(pix, 0, w, 0, 0, w, h)
        return bitmap
    }
}
