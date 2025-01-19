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
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlockViewHolder {
        context = parent.context
        val binding = BlockItemBinding.inflate(LayoutInflater.from(context), parent, false)
        return BlockViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BlockViewHolder, position: Int) {
        val block = blocks[position]
        val binding = holder.binding

        binding.indexTextView.text = context.getString(R.string.block, block.index.toString())
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
            showDataBottomSheet(block, context)
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

        binding.indexTextView.text = context.getString(R.string.index, block.index.toString())
        binding.timestampTextView.text =
            context.getString(R.string.date, convertTimestampToReadableDate(block.timestamp))
        binding.difficultyTextViewBD.text = context.getString(R.string.difficulty, block.difficulty.toString())
        binding.minerTextView.text = context.getString(R.string.miner, block.miner ?: "No miner")
        binding.nonceTextView.text = context.getString(R.string.nonce, block.nonce.toString())
        binding.hashTextViewBD.text = context.getString(R.string.hash, block.hash)
        binding.prevHashTextViewBD.text = context.getString(R.string.prev_hash, block.previousHash)

        bottomSheetDialog.show()
    }
}










