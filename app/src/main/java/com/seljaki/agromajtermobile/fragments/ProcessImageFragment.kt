package com.seljaki.agromajtermobile.fragments

import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.seljaki.agromajtermobile.MyApplication
import com.seljaki.agromajtermobile.R
import com.seljaki.agromajtermobile.databinding.FragmentProcessImageBinding
import com.seljaki.lib.WeatherPrediction
import com.seljaki.lib.recognizeWeather
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class ProcessImageFragment : Fragment() {
    private lateinit var binding: FragmentProcessImageBinding
    private lateinit var image: Bitmap
    private lateinit var app: MyApplication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        app = requireActivity().application as MyApplication
        image = app.imageToPredict!!
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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
                val prediction = recognizeWeather(imageBytes, contentType)
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