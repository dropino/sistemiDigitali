package it.unibo.sistemidigitali

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import it.unibo.sistemidigitali.Utilities.getMax
import it.unibo.sistemidigitali.Utilities.handleSamplingAndRotationBitmap
import it.unibo.sistemidigitali.ml.SavedModel384
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.File

private const val FILE_NAME = "photo.jpg"
private const val REQUEST_CODE = 23

class MainActivity : AppCompatActivity() {

    private lateinit var bitmap : Bitmap
    private lateinit var imgView : ImageView
    private lateinit var photoFile : File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //gestione del file delle label
        val filename = "label.txt"
        val inputString = application.assets.open(filename).bufferedReader().use { it.readText() }
        val townlist = inputString.split("\n")
        var finalClass : String = townlist[0]

        //creazione dell'imageView per mostrare l'immagine e della textView per il risultato della predizione
        imgView = findViewById(R.id.imageView)
        val tv2:TextView = findViewById(R.id.textView4)
        tv2.visibility = View.INVISIBLE
        val simulate : Button = findViewById(R.id.simulateButton)
        simulate.visibility = View.INVISIBLE

        //bottone per scegliere la foto dalla galleria
        val select : Button = findViewById(R.id.selectButton)
        //scelta dell'immagine dalla galleria
        select.setOnClickListener(View.OnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, 100)
        })

        //bottone per scattare una foto
        val camera : Button = findViewById(R.id.cameraButton)
        //gestione della fotocamera, con salvataggio su file per mantenere un'alta qualità dell'immagine
        camera.setOnClickListener{
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            photoFile = getPhotoFile(FILE_NAME)
            val fileProvider = FileProvider.getUriForFile(this, "it.unibo.sistemidigitali.fileprovider", photoFile)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider)
            if(takePictureIntent.resolveActivity(this.packageManager) != null) {
                startActivityForResult(takePictureIntent, REQUEST_CODE)
            } else{
                Toast.makeText(this, "Unable to open camera", Toast.LENGTH_SHORT).show()
            }
        }

        //codice per predire la classe dell'immagine
        val predict: Button = findViewById(R.id.predictButton)
        predict.setOnClickListener(View.OnClickListener {
            val model = SavedModel384.newInstance(this)
            val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 384, 384, 3), DataType.FLOAT32)
            //scala l'immagine nelle dimensioni dell'input della rete
            val resizedImage = Utilities.resizeBitmap(bitmap, 384, 384)
            val tensorImage = TensorImage(DataType.FLOAT32)
            tensorImage.load(resizedImage)
            //preprocessing: normalizzazione 0-255
            Utilities.preProcessImage(tensorImage)
            val modelOutput = tensorImage.buffer
            inputFeature0.loadBuffer(modelOutput)
            val outputs = model.process(inputFeature0)
            val outputFeature0 = outputs.outputFeature0AsTensorBuffer.floatArray
            //scelta del valore massimo di probabilità restituito
            val max = getMax(outputFeature0)
            finalClass = townlist[max]
            //stampa risultato
            val result : String = Utilities.printResult(townlist[max].trim())
            tv2.text = result
            tv2.visibility = View.VISIBLE
            simulate.visibility = View.VISIBLE
            //Rilascio delle risorse
            model.close()
        })

        //lancio della simulazione, passando come parametro la classe predetta
        simulate.setOnClickListener{
            tv2.visibility = View.GONE
            val intent = Intent(this, SimulationActivity::class.java)
            intent.putExtra("classified", finalClass)
            startActivity(intent)
        }
    }

    /**
     * Funzione per mostrare l'immagine selezionata.
     * La distinzione tra foto scattata dal dispositivo e foto ottenuta dalla galleria è specificata dal codice
     * @param requestCode;
     * @param resultCode riporta eventuali errori relativi al funzionamento della fotocamera
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //caso 1: fotocamera. La fotografia è stata scattata e non ci sono errori, carica la bitmap opportunamente ottimizzata nell'imageView
        if(requestCode == REQUEST_CODE && resultCode == RESULT_OK){
            val imageUri = Uri.fromFile(photoFile)
            bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
            imgView.setImageBitmap(handleSamplingAndRotationBitmap(this, imageUri))
        }else {
            //caso 2: galleria.
            super.onActivityResult(requestCode, resultCode, data)
            imgView.setImageURI(data?.data)
            val uri: Uri? = data?.data
            bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
        }
    }

    //Ottenere il file contenente la foto, salvato nella directory Pictures del dispositivo.
    private fun getPhotoFile(fileName: String): File {
        val storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(fileName, ".jpg", storageDirectory)
    }
}