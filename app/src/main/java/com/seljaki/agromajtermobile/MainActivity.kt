package com.seljaki.agromajtermobile


import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.seljaki.agromajtermobile.databinding.ActivityMainBinding
import org.pytorch.IValue
import org.pytorch.LiteModuleLoader
import org.pytorch.Module
import org.pytorch.torchvision.TensorImageUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var model: Module;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        model = LiteModuleLoader.load(assetFilePath(this, "model_lite.pt"));

        binding.openImageBtn.setOnClickListener{openImage()}
    }

    @Throws(IOException::class)
    fun assetFilePath(context: Context, assetName: String?): String {
        val file = File(context.filesDir, assetName)
        if (file.exists() && file.length() > 0) {
            return file.absolutePath
        }

        context.assets.open(assetName!!).use { `is` ->
            FileOutputStream(file).use { os ->
                val buffer = ByteArray(4 * 1024)
                var read: Int
                while ((`is`.read(buffer).also { read = it }) != -1) {
                    os.write(buffer, 0, read)
                }
                os.flush()
            }
            return file.absolutePath
        }
    }

    fun openImage() {
        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        // Callback is invoked after the user selects a media item or closes the
        // photo picker.
        if (uri != null) {
            Log.d("PhotoPicker", "Selected URI: $uri")
            try {
                val bitmap = uriToBitmap(uri)
                if (bitmap != null) {
                    // Use the bitmap (e.g., display it in an ImageView)
                    Log.d("PhotoPicker", "Bitmap created successfully")
                    val scores = recognizeWeather(bitmap)
                    var str: String = ""
                    for(score in scores) {
                        str += "$score, "
                    }
                    binding.predictionTV.text = str
                } else {
                    Log.e("PhotoPicker", "Failed to create Bitmap")
                }
            } catch (e: Exception) {
                Log.e("PhotoPicker", "Error converting URI to Bitmap", e)
            }
        } else {
            Log.d("PhotoPicker", "No media selected")
        }
    }

    fun uriToBitmap(uri: Uri): Bitmap? {
        return try {
            val contentResolver = this.contentResolver
            val inputStream = contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream).also {
                inputStream?.close()
            }
        } catch (e: Exception) {
            Log.e("PhotoPicker", "Error decoding Bitmap", e)
            null
        }
    }

    fun recognizeWeather(inputBitmap: Bitmap): FloatArray {
        val resizedBitmap = Bitmap.createScaledBitmap(inputBitmap, 64, 64, true)
        binding.imageView.setImageBitmap(resizedBitmap)

        val inputTensor = TensorImageUtils.bitmapToFloat32Tensor(
            resizedBitmap,
            TensorImageUtils.TORCHVISION_NORM_MEAN_RGB,
            TensorImageUtils.TORCHVISION_NORM_STD_RGB,
        )

        val outputTensor = model.forward(IValue.from(inputTensor)).toTensor()
        val rawScores = outputTensor.dataAsFloatArray
        println(rawScores.joinToString(", "))

        // Normalize the scores using softmax
        val normalizedScores = softmax(rawScores)

        // Print and return scores
        println(normalizedScores.joinToString(", "))
        return normalizedScores
    }

    fun softmax(scores: FloatArray): FloatArray {
        val expScores = scores.map { Math.exp(it.toDouble()).toFloat() }
        val sumExpScores = expScores.sum()
        return expScores.map { it / sumExpScores }.toFloatArray()
    }
}