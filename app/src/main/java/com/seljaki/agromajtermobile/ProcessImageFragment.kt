package com.seljaki.agromajtermobile

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.seljaki.agromajtermobile.databinding.FragmentProcessImageBinding
import com.seljaki.lib.recognizeWeather
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class ProcessImageFragment : Fragment() {
    private lateinit var binding: FragmentProcessImageBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProcessImageBinding.inflate(layoutInflater, container, false)

        val imageUriString = arguments?.let {
            ProcessImageFragmentArgs.fromBundle(it).imageUri
        }

        // Use the URI string (convert to Uri if needed)
        val imageUri: Uri? = imageUriString?.let { Uri.parse(it) }
        if(imageUri != null)
            getImagePrediction(imageUri)

        return binding.main
    }

    fun getImagePrediction(uri: Uri) {
        try {
            // Retrieve content resolver and decode URI to Bitmap
            val contentResolver = requireContext().contentResolver
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (bitmap != null) {
                // Convert Bitmap to ByteArray in JPG format
                val outputStream = ByteArrayOutputStream()
                val isCompressed = bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream) // Change to PNG if needed
                if (isCompressed) {
                    val imageBytes = outputStream.toByteArray()
                    outputStream.close()

                    // Use the proper content type for JPEG
                    val contentType = "image/jpeg"

                    // Call the server API
                    CoroutineScope(Dispatchers.IO).launch {
                        val prediction = recognizeWeather(imageBytes, contentType)
                        withContext(Dispatchers.Main) {
                            if (prediction != null) {
                                Log.d("WeatherPrediction", "Prediction result: $prediction")
                            } else {
                                Log.d("WeatherPrediction", "Failed to get prediction.")
                            }
                        }
                    }
                } else {
                    Log.e("PhotoPicker", "Failed to compress Bitmap.")
                }
            } else {
                Log.e("PhotoPicker", "Failed to decode Bitmap from URI.")
            }
        } catch (e: Exception) {
            Log.e("PhotoPicker", "Error processing image", e)
        }
    }
}