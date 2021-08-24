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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import it.unibo.sistemidigitali.ml.SavedModel2
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer

class MainActivity : AppCompatActivity() {

    lateinit var bitmap : Bitmap
    lateinit var imgView : ImageView
    private val REQUEST_CODE = 23

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //gestione del file delle label
        val filename = "label.txt"
        val inputString = application.assets.open(filename).bufferedReader().use { it.readText() }
        var townlist = inputString.split("\n")
        var finalClass : String = townlist[0]


        //creazione dell'imageView per mostrare l'immagine e della textView per il risultato della predizione
        imgView = findViewById(R.id.imageView)
        var tv2:TextView = findViewById(R.id.textView4)
        tv2.visibility = View.INVISIBLE
        var simulate : Button = findViewById(R.id.simulateButton)
        simulate.visibility = View.INVISIBLE

        //bottone per scegliere la foto dalla galleria
        var select : Button = findViewById(R.id.selectButton)
        //scelta dell'immagine dalla galleria
        select.setOnClickListener(View.OnClickListener {
            var intent: Intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, 100)
        })

        //bottone per scattare una foto
        var camera : Button = findViewById(R.id.cameraButton)
        camera.setOnClickListener{
            var takePictureIntent : Intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if(takePictureIntent.resolveActivity(this.packageManager) != null) {
                startActivityForResult(takePictureIntent, REQUEST_CODE)
            } else{
                Toast.makeText(this, "Unable to open camera", Toast.LENGTH_SHORT).show()
            }
        }

        //codice per predire la classe dell'immagine
        var predict: Button = findViewById(R.id.predictButton)
        predict.setOnClickListener(View.OnClickListener {
            val model = SavedModel2.newInstance(this)
            val input = TensorBuffer.createFixedSize(intArrayOf(1, 384, 384, 3), DataType.FLOAT32)
            val resizedImage = resizeBitmap(bitmap, 384, 384)
            val tensorImage = TensorImage(DataType.FLOAT32)
            tensorImage.load(resizedImage)
            val modelOutput = tensorImage.buffer
            input.loadBuffer(modelOutput)
            val outputs = model.process(input)
            val outputFeature0 = outputs.outputFeature0AsTensorBuffer.floatArray
            var max = getMax(outputFeature0)
            finalClass = townlist[max]
            //scrittura risultato
            var result : String = printResult(townlist[max].trim())
            tv2.visibility = View.VISIBLE
            simulate.visibility = View.VISIBLE
            tv2.text = result
            // Releases model resources if no longer used.
            val TAG = "tensor"
            Log.i(TAG, "this is ${townlist[max]}")
            model.close()
        })
        //scelta dell'immagine dalla galleria
        simulate.setOnClickListener{
            val intent = Intent(this, SimulationActivity::class.java)
            intent.putExtra("classified", finalClass)
            startActivity(intent)
        }
    }

    //mostrare l'immagine selezionata
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == REQUEST_CODE && resultCode == RESULT_OK){
            bitmap = data?.extras?.get("data") as Bitmap
            imgView.setImageBitmap(bitmap)
        }else {
            super.onActivityResult(requestCode, resultCode, data)
            imgView.setImageURI(data?.data)
            var uri: Uri? = data?.data
            bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
        }
    }

    /*Funzioni di supporto per gestire bitmap ed imageview*/
    private fun resizeBitmap(bitmap: Bitmap, width: Int, height: Int):Bitmap{
        return Bitmap.createScaledBitmap(
                bitmap,
                width,
                height,
                false
        )
    }

    //scegliere il massimo tra i valori restituiti dal tensore
    fun getMax(arr: FloatArray) : Int{
        var index = 0
        var max = 0.0f
        var len = arr.size-1
        for(i in 0..len){
            if(arr[i] > max) {
                index = i
                max = arr[i]
            }
            val TAG = "value"
            Log.i(TAG, "this is ${i}, and here is its value ${arr[i]}")
        }
        return index
    }

    private fun printResult(result : String) : String{
        var returnResult : String = ""
       when (result) {
            "Arch_bridges" -> returnResult = "The picture shows an arch bridge"
            "Castles" -> returnResult ="The picture shows a castle"
            "Church" -> returnResult ="The picture shows a church"
            "Towers" -> returnResult ="The picture shows a tower"
            "Triumphal_arches" -> returnResult ="The picture shows a triumphal arch"
        }
        return returnResult
    }

}