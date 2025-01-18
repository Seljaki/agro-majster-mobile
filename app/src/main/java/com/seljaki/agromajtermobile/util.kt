package com.seljaki.agromajtermobile

import android.content.Context
import android.content.res.Configuration
import android.view.LayoutInflater
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.seljaki.agromajtermobile.databinding.BottomSheetBlockDataBinding
import com.seljaki.agromajtermobile.fragments.ProcessImageFragment.PercentFormatter
import com.seljaki.agromajtermobile.fragments.ProcessImageFragment.XAxisValueFormatter
import com.seljaki.lib.Block
import com.seljaki.lib.WeatherPrediction
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun showDataBottomSheet(block: Block, context: Context){
    val bottomSheetDialog = BottomSheetDialog(context)
    val binding = BottomSheetBlockDataBinding.inflate(LayoutInflater.from(context))
    bottomSheetDialog.setContentView(binding.root)

    binding.locationButton.setOnClickListener{
        val action = NavGraphDirections.actionGlobalMapsFragment(block.index - 1)
        val navController = (context as? MainActivity)?.navController
        navController?.navigate(action)
        bottomSheetDialog.dismiss()
    }

    binding.indexTextView.text = "Index: ${block.index}"
    binding.timestampTextView.text = "Date: ${convertTimestampToReadableDate(block.timestamp)}"
    setChart(block.data.prediction, binding, context)
    binding.predictionTextView.text = context.getString(R.string.predicted_weather, block.data.prediction.getPredicted())
    binding.temperatureTextView.text = "Temperature: ${block.data.temperature}"
    binding.locationTextView.text = "Longtitude: ${block.data.longitude} Latitude: ${block.data.latitude}"

    bottomSheetDialog.show()
}

fun convertTimestampToReadableDate(timestamp: Long): String {
    val date = Date(timestamp * 1000)
    val format = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
    return format.format(date)
}

fun setChart(prediction: WeatherPrediction, binding: BottomSheetBlockDataBinding, context: Context) {
    val chart: BarChart = binding.barChart

    val entries = listOf(
        BarEntry(0f, prediction.rainy.toFloat()),
        BarEntry(1f, prediction.cloudy.toFloat()),
        BarEntry(2f, prediction.clear.toFloat())
    )

    val dataSet = BarDataSet(entries, "Weather Chances")
    dataSet.setColors(
        context.getColor(R.color.blue_graph),
        context.getColor(R.color.green_graph),
        context.getColor(R.color.orange_graph)
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
            context.getString(R.string.rainy),
            context.getString(R.string.cloudy),
            context.getString(R.string.clear)
        )
    )
    xAxis.granularity = 1f


    chart.axisLeft.setDrawGridLines(false)
    chart.axisRight.isEnabled = false

    chart.setFitBars(true)

    val textColor = if (isDarkMode(context)) {
        context.getColor(R.color.white)
    } else {
        context.getColor(R.color.black)
    }

    xAxis.textColor = textColor
    chart.axisLeft.textColor = textColor
    chart.axisRight.textColor = textColor
    dataSet.valueTextColor = textColor
    chart.legend.isEnabled = false
    chart.axisLeft.valueFormatter = PercentFormatter()

    chart.invalidate()
}

fun isDarkMode(context: Context): Boolean {
    val nightModeFlags = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
    return nightModeFlags == Configuration.UI_MODE_NIGHT_YES
}