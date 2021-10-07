package it.unibo.sistemidigitali

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import java.io.IOException
import java.io.InputStream
import kotlin.math.roundToInt

/**Classe che contiene le funzioni di supporto necessarie al funzionamento dei metodi nella MainActivity.*/
object Utilities {
    /**
     * Funzione che applica il preprocessing adottato in fase di addestramento del modello alle immagini.
     * Normalizza l'immagine nel range 0-255
     * @param tensorImage : immagine di input, sotto forma di tensore
    */
    fun preProcessImage(tensorImage: TensorImage): Any {
        val imageProcessor : ImageProcessor.Builder = ImageProcessor.Builder().add(NormalizeOp(0.0f, 255.0f))
        val img = imageProcessor.build()
        img.process(tensorImage)
        return tensorImage
    }

    /**
     * Funzione di supporto per scalare la bitmap affinché abbia delle dimensioni consone all'imageView. Nel caso specifico, all'interno del codice
     * la funzione viene richiamata con dimensioni 384x384.
     * @param bitmap : immagine, sotto forma di bitmap, che deve essere ridimensionata
     * @param width : larghezza finale
     * @param height : altezza finale
     * */
    fun resizeBitmap(bitmap: Bitmap, width: Int, height: Int): Bitmap {
        return Bitmap.createScaledBitmap(
                bitmap,
                width,
                height,
                false
        )
    }

    /**
     * Funzione di supporto per individuare il valore massimo restituito dalla predizione.
     * */
    fun getMax(arr: FloatArray) : Int{
        var index = 0
        var max = 0.0f
        val len = arr.size-1
        for(i in 0..len){
            if(arr[i] > max) {
                index = i
                max = arr[i]
            }
        }
        return index
    }

    fun printResult(result: String) : String{
        var returnResult = ""
        when (result) {
            "Arch_bridges" -> returnResult = "The picture shows an arch bridge."
            "Castles" -> returnResult = "The picture shows a castle."
            "Churches" -> returnResult = "The picture shows a church."
            "Towers" -> returnResult = "The picture shows a tower."
            "Triumphal_Arches" -> returnResult = "The picture shows a triumphal arch."
        }
        return returnResult
    }

    /**
     * Funzioni di supporto per la gestione della bitmap e della rotazione delle immagini.
     * In alcuni dispositivi è stato rilevato come le immagini verticali catturate dalla fotocamera risultassero rotate di 90°.
     * Le funzioni che seguono gestiscono questo inconveniente, analizzando l'immagine in termini di dimensioni e orientamento ed eventualmente
     * ruotandola prima di utilizzarla all'interno della imageView.
     * @see handleSamplingAndRotationBitmap : gestione della bitmap per evitare un'occupazione in memoria eccessiva rispetto alle dimensioni effettive
     * dell'imageView
     * @see calculateInSampleSize : calcolo del parametro per l'eventuale sottocampionamento dell'immagine. Ad esempio, una risoluzione 1532x1532
     * rispetto al riferimento di 384x384 restituisce un'inSampleSize = 4
     * */

    @Throws(IOException::class)
    fun handleSamplingAndRotationBitmap(context: Context, selectedPicture: Uri): Bitmap? {
        //uso delle Options della BitmapFactory per gestire le dimensione dell'immagine (operazione di decoding)
        val options: BitmapFactory.Options = BitmapFactory.Options()
        //Decoding: lettura delle dimensioni dell'immagine prima della costruzione e dell'allocazione della bitmap. Risolve OutOfMemory
        options.inJustDecodeBounds = true
        var imageStream: InputStream = context.contentResolver.openInputStream(selectedPicture)!!
        BitmapFactory.decodeStream(imageStream, null, options)
        imageStream.close()
        //Valuto se l'immagine è eccessivamente grande o pesante rispetto alle dimensioni 384x384 che occorrono. Qualora così fosse, mediante
        //calculateInSampleSize ottengo un'immagine più piccola per ridurre l'occupazione di memoria
        options.inSampleSize = calculateInSampleSize(options, 384, 384)
        // Decodifica dell'immagine, gestita concordemente con le opzioni di riferimento
        options.inJustDecodeBounds = false
        imageStream = context.contentResolver.openInputStream(selectedPicture)!!
        var picture = BitmapFactory.decodeStream(imageStream, null, options)
        //Rotazione dell'immagine, qualora questa fosse necessaria
        picture = rotateIfRequired(context, picture!!, selectedPicture)
        return picture
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        //Recupero delle dimensioni dell'immagine
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        //Se l'immagine attuale ha una o entrambe le dimensioni superiori a quella di riferimento, calcolo i rapporti di cui sono scalate
        if (height > reqHeight || width > reqWidth) {
            val heightRatio = (height.toFloat() / reqHeight.toFloat()).roundToInt()
            val widthRatio = (width.toFloat() / reqWidth.toFloat()).roundToInt()
            //Scelgo il rapporto minore come valore di inSampleSize
            inSampleSize = if (heightRatio < widthRatio) heightRatio else widthRatio
        }
        return inSampleSize
    }

    /**
     * Funzione per analizzare l'orientazione dell'immagine e selezionare l'angolo di rotazione, qualora fosse stata automaticamente ruotata dopo lo
     * scatto.
     * @param picture : fotografia
     * @param pictureUri : Uri della fotografia
     * @return la bitmap modificata
     * */
    @Throws(IOException::class)
    private fun rotateIfRequired(context: Context, picture: Bitmap, pictureUri: Uri): Bitmap? {
        val input = context.contentResolver.openInputStream(pictureUri)
        val ei: ExifInterface = if (Build.VERSION.SDK_INT > 23) ExifInterface(input!!) else ExifInterface(pictureUri.path!!)
        return when (ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotate(picture, 90.toFloat())
            ExifInterface.ORIENTATION_ROTATE_180 -> rotate(picture, 180.toFloat())
            ExifInterface.ORIENTATION_ROTATE_270 -> rotate(picture, 270.toFloat())
            else -> picture
        }
    }

    /**
     * Funzione per creare una bitmap ruotata di uno specifico angolo, partendo dall'immagine originale.
     * */
    private fun rotate(picture: Bitmap, degree: Float): Bitmap? {
        val matrix = Matrix()
        matrix.postRotate(degree)
        val rotatedImg = Bitmap.createBitmap(picture, 0, 0, picture.width, picture.height, matrix, true)
        picture.recycle()
        return rotatedImg
    }
}