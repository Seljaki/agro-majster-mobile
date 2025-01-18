package com.seljaki.agromajtermobile.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.navigation.fragment.findNavController
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.seljaki.agromajtermobile.MyApplication
import com.seljaki.agromajtermobile.R
import com.seljaki.agromajtermobile.databinding.FragmentProcessImageBinding
import com.seljaki.agromajtermobile.weather.RetrofitClient
import com.seljaki.lib.Blockchain
import com.seljaki.lib.WeatherPrediction
import com.seljaki.lib.recognizeWeather
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import com.seljaki.lib.BlockchainClient
import com.seljaki.lib.Data
import com.seljaki.lib.client

//data class Location(
//    val latitude: Double,
//    val longitude: Double
//)
class ProcessImageFragment : Fragment() {
    private lateinit var binding: FragmentProcessImageBinding
    private lateinit var image: Bitmap
    private lateinit var app: MyApplication
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var prediction: WeatherPrediction

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        app = requireActivity().application as MyApplication
        image = app.imageToPredict!!
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProcessImageBinding.inflate(layoutInflater, container, false)

        return binding.main
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.imagePreview.setImageBitmap(image)
        binding.backgroundView.visibility = View.VISIBLE
        binding.progressBar.visibility = View.VISIBLE
        binding.buttonBack.setOnClickListener {
            findNavController().popBackStack()
        }
        predictWeather()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        binding.buttonBlockchain.setOnClickListener {
            getUserLocation { location ->
                if (location == null) {
                    Log.d("Seznor info", "Ni bilo mogoče pridobiti trenutne lokacije.")
                }
                else {
                    Log.d("Seznor info","lat: " + location.latitude + ", long: " + location.longitude)
                    getTemperature(location.latitude, location.longitude) { temperature ->
                        if (temperature != null) {
                            Log.d("Seznor info", "Temperatura: $temperature °C")

                            val dataToMine = Data(
                                temperature = temperature,
                                longitude = location.longitude,
                                latitude = location.latitude,
                                prediction = prediction
                            )
                            app.blockchainClient.sendDataToMine(dataToMine)
                            findNavController().popBackStack()
                        } else {
                            Log.d("Seznor info", "Ni bilo mogoče pridobiti temperature.")
                        }
                    }
                }
            }
        }
    }

    private fun getTemperature(latitude: Double, longitude: Double, callback: (Double?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.instance.getWeather(latitude, longitude)
                withContext(Dispatchers.Main) {
                    callback(response.main.temp)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("WeatherError", e.message.toString())
                    callback(null)
                }
            }
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    @SuppressLint("MissingPermission")
    private fun getUserLocation(callback: (Location?) -> Unit) {
        if (!isLocationEnabled()) {
            Toast.makeText(requireContext(), "Please enable location services", Toast.LENGTH_SHORT).show()
            callback(null)
            return
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            callback(null)
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                callback(location)
            } else {
                Toast.makeText(requireContext(), "Could not fetch location", Toast.LENGTH_SHORT).show()
                callback(null)
            }
        }.addOnFailureListener { exception ->
            Log.e("LocationError", "Error fetching location: ${exception.message}")
            callback(null)
        }
    }

    private fun predictWeather() {
        // Convert Bitmap to ByteArray in JPG format
        val outputStream = ByteArrayOutputStream()
        val isCompressed = image.compress(
            Bitmap.CompressFormat.JPEG,
            100,
            outputStream
        ) // Change to PNG if needed
        if (isCompressed) {
            val imageBytes = outputStream.toByteArray()
            outputStream.close()
            // Use the proper content type for JPEG
            val contentType = "image/jpeg"

            // Call the server API
            CoroutineScope(Dispatchers.IO).launch {
                prediction = recognizeWeather(imageBytes, contentType)!!
                withContext(Dispatchers.Main) {
                    if (prediction != null) {
                        binding.predictionTextView.text =
                            getString(R.string.predicted_weather, prediction.getPredicted())
                        setChart(prediction)
                        binding.backgroundView.visibility = View.GONE
                        binding.progressBar.visibility = View.GONE
                        Log.d("WeatherPrediction", "Prediction result: $prediction")
                    } else {
                        Log.d("WeatherPrediction", "Failed to get prediction.")
                        binding.progressBar.visibility = View.GONE
                    }
                }
            }
        } else {
            Log.e("WeatherPrediction", "Failed to compress Bitmap.")
        }
    }

    fun setChart(prediction: WeatherPrediction) {
        val chart: BarChart = binding.barChart

        val entries = listOf(
            BarEntry(0f, prediction.rainy.toFloat()),
            BarEntry(1f, prediction.cloudy.toFloat()),
            BarEntry(2f, prediction.clear.toFloat())
        )

        val dataSet = BarDataSet(entries, "Weather Chances")
        dataSet.setColors(
            requireContext().getColor(R.color.blue_graph),
            requireContext().getColor(R.color.green_graph),
            requireContext().getColor(R.color.orange_graph)
        )

        dataSet.valueFormatter = PercentFormatter()
        val barData = BarData(dataSet)
        barData.barWidth = 0.9f

        chart.data = barData
        chart.description.isEnabled = false

        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.valueFormatter = XAxisValueFormatter(
            listOf(
                getString(R.string.rainy),
                getString(R.string.cloudy),
                getString(R.string.clear)
            )
        )
        xAxis.granularity = 1f


        chart.axisLeft.setDrawGridLines(false)
        chart.axisRight.isEnabled = false

        chart.setFitBars(true)

        val textColor = if (isDarkMode()) {
            requireContext().getColor(R.color.white)
        } else {
            requireContext().getColor(R.color.black)
        }

        xAxis.textColor = textColor
        chart.axisLeft.textColor = textColor
        chart.axisRight.textColor = textColor
        dataSet.valueTextColor = textColor
        chart.legend.isEnabled = false
        chart.axisLeft.valueFormatter = PercentFormatter()

        chart.invalidate()
    }

    fun isDarkMode(): Boolean {
        val nightModeFlags = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES
    }

    class PercentFormatter : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            return String.format("%.0f%%", value * 100)
        }
    }

    class XAxisValueFormatter(private val labels: List<String>) : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            return labels.getOrNull(value.toInt()) ?: value.toString()
        }
    }
}