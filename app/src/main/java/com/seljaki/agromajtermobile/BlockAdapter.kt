package com.seljaki.agromajtermobile

import android.content.Context
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.provider.Settings.Global.getString
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.seljaki.agromajtermobile.databinding.BlockItemBinding
import com.seljaki.agromajtermobile.databinding.BottomSheetBlockDataBinding
import com.seljaki.agromajtermobile.databinding.BottomSheetBlockDetailsBinding
import com.seljaki.agromajtermobile.fragments.ProcessImageFragment.PercentFormatter
import com.seljaki.agromajtermobile.fragments.ProcessImageFragment.XAxisValueFormatter
import com.seljaki.lib.Block
import com.seljaki.lib.WeatherPrediction
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BlockAdapter(private val blocks: List<Block>) : RecyclerView.Adapter<BlockAdapter.BlockViewHolder>() {

    private lateinit var context: Context

    class BlockViewHolder(val binding: BlockItemBinding) : RecyclerView.ViewHolder(binding.root)

//    class BlockViewHolder(view: View) : RecyclerView.ViewHolder(view) {
//
//        val indexTextView: TextView = view.findViewById(R.id.indexPlaceholderTextView)
//        val difficultyTextView: TextView = view.findViewById(R.id.difficultyPlaceholderTextView)
//        val timestampTextView: TextView = view.findViewById(R.id.timestampPlaceHolderTextView)
//        val weatherPrediction: TextView = view.findViewById(R.id.weatherPredictionPlaceHolderTextView)
//        val weatherImageView: ImageView = view.findViewById(R.id.weatherImageView)
//
//    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlockViewHolder {
        context = parent.context
        val binding = BlockItemBinding.inflate(LayoutInflater.from(context), parent, false)
        return BlockViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BlockViewHolder, position: Int) {
        val block = blocks[position]
        val binding = holder.binding

        binding.indexTextView.text = "Block: ${block.index}"
        //binding.timestampPlaceHolderTextView.text = convertTimestampToReadableDate(block.timestamp)
        //binding.difficultyPlaceholderTextView.text = municipality.toString()
        binding.weatherPredictionPlaceHolderTextView.text = block.data.prediction.getPredicted()

        val predictionImage = getPredictionImage(block.data.prediction.getPredicted())
        binding.weatherImageView.setImageDrawable(predictionImage)

        binding.root.setOnLongClickListener {
            showDetailsBottomSheet(block)
            true
        }
        binding.root.setOnClickListener {
            showDataBottomSheet(block)
            true
        }
    }

    override fun getItemCount(): Int {
        Log.d("Blocks Size", blocks.size.toString())
        return blocks.size
    }



    private fun getPredictionImage(prediction: String): Drawable{
        return when (prediction){
            "cloudy" -> ContextCompat.getDrawable(context, R.drawable.cloudy)!!
            "rainy" -> ContextCompat.getDrawable(context, R.drawable.rainy)!!
            else -> ContextCompat.getDrawable(context, R.drawable.sunny)!!
        }
    }


    private fun showDetailsBottomSheet(block: Block) {
        val bottomSheetDialog = BottomSheetDialog(context)
        val binding = BottomSheetBlockDetailsBinding.inflate(LayoutInflater.from(context))
        bottomSheetDialog.setContentView(binding.root)

        binding.indexTextView.text = "Index: ${block.index}"
        binding.timestampTextView.text = "Date: ${convertTimestampToReadableDate(block.timestamp)}"
        binding.difficultyTextViewBD.text = "Difficulty: ${block.difficulty}"
        binding.minerTextView.text = "Miner: ${block.miner ?: "No miner"}"
        binding.nonceTextView.text = "Nonce: ${block.nonce}"
        binding.hashTextViewBD.text = "Hash: ${block.hash}"
        binding.prevHashTextViewBD.text = "Prev Hash: ${block.previousHash}"

        bottomSheetDialog.show()
    }

    private fun showDataBottomSheet(block: Block){
        val bottomSheetDialog = BottomSheetDialog(context)
        val binding = BottomSheetBlockDataBinding.inflate(LayoutInflater.from(context))
        bottomSheetDialog.setContentView(binding.root)

        binding.locationButton.setOnClickListener{
            //viewLocation(block.data.longitude, block.data.latitude)
        }

        binding.indexTextView.text = "Index: ${block.index}"
        binding.timestampTextView.text = "Date: ${convertTimestampToReadableDate(block.timestamp)}"
        setChart(block.data.prediction,binding)
        binding.predictionTextView.text = context.getString(R.string.predicted_weather, block.data.prediction.getPredicted())
        binding.temperatureTextView.text = "Temperature: ${block.data.temperature}"
        binding.locationTextView.text = "Longtitude: ${block.data.longitude} Latitude: ${block.data.latitude}"

        bottomSheetDialog.show()
    }

    private fun convertTimestampToReadableDate(timestamp: Long): String {
        val date = Date(timestamp * 1000)
        val format = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
        return format.format(date)
    }

    fun setChart(prediction: WeatherPrediction, binding: BottomSheetBlockDataBinding) {
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

        val textColor = if (isDarkMode()) {
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
    fun isDarkMode(): Boolean {
        val nightModeFlags = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES
    }
}










