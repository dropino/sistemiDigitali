package it.unibo.sistemidigitali

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import it.unibo.sistemidigitali.ml.SavedModel1
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer


class MainActivity : AppCompatActivity() {

    lateinit var bitmap : Bitmap
    lateinit var imgView : ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        imgView = findViewById(R.id.imageView)
        var tv2:TextView = findViewById(R.id.textView4)
        tv2.visibility = View.INVISIBLE

        var select : Button = findViewById(R.id.cameraButton)
        select.setOnClickListener(View.OnClickListener {
            var intent : Intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, 100)
        })

        var predict: Button = findViewById(R.id.galleryButton)
        predict.setOnClickListener(View.OnClickListener {
            var resized : Bitmap = Bitmap.createScaledBitmap(bitmap, 384*4, 384, true)
            val model = SavedModel1.newInstance(this)
            // Creates inputs for reference.
            val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 384, 384, 3), DataType.FLOAT32)
            var tbuffer = TensorImage.fromBitmap(resized)
            var byteBuffer: ByteBuffer = tbuffer.buffer
            inputFeature0.loadBuffer(byteBuffer) //<- qui smadonna
            // Runs model inference and gets result.
            val outputs = model.process(inputFeature0)
            val outputFeature0 = outputs.outputFeature0AsTensorBuffer
            tv2.visibility = View.VISIBLE
            tv2.setText(outputFeature0.floatArray[4].toString())
            // Releases model resources if no longer used.
            val TAG = "tensor"
            Log.i(TAG, "this is ${outputFeature0.floatArray[0].toString()}")
            model.close()
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        imgView.setImageURI(data?.data)
        var uri:Uri ?= data?.data
        bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)

    }

}