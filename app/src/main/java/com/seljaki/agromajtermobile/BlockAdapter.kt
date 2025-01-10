package com.seljaki.agromajtermobile

import android.content.Context
import android.graphics.drawable.Drawable
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.seljaki.agromajtermobile.databinding.BlockItemBinding
import com.seljaki.agromajtermobile.databinding.BottomSheetBlockDetailsBinding
import com.seljaki.lib.Block
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

        binding.indexPlaceholderTextView.text = block.index.toString()
        binding.timestampPlaceHolderTextView.text = convertTimestampToReadableDate(block.timestamp)
        val municipality = getMunicipalityFromCoordinates(
            context, block.data.latitude, block.data.longitude) { municipality ->
            if (municipality != null) {
                println("Občina: $municipality")
            } else {
                println("Lokacije ni bilo mogoče najti")
            }
        }
        binding.difficultyPlaceholderTextView.text = municipality.toString()
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

    fun getMunicipalityFromCoordinates(context: Context, latitude: Double, longitude: Double, callback: (String?) -> Unit) {
        val geocoder = Geocoder(context, Locale.getDefault())

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geocoder.getFromLocation(latitude, longitude, 1, object : Geocoder.GeocodeListener {
                override fun onGeocode(addresses: MutableList<Address>) {
                    if (addresses.isNotEmpty()) {
                        val municipality = addresses[0].subAdminArea ?: addresses[0].locality
                        callback(municipality)
                    } else {
                        callback(null)
                    }
                }

                override fun onError(errorMessage: String?) {
                    callback(null)
                }
            })
        } else {
            // Backwards compatibility for older Android versions
            try {
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                if (addresses?.isNotEmpty() == true) {
                    callback(addresses[0].subAdminArea ?: addresses[0].locality)
                } else {
                    callback(null)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                callback(null)
            }
        }
    }

    private fun showDetailsBottomSheet(block: Block) {
        val bottomSheetDialog = BottomSheetDialog(context)
        val binding = BottomSheetBlockDetailsBinding.inflate(LayoutInflater.from(context))
        bottomSheetDialog.setContentView(binding.root)

        // Nastavimo podatke za prikaz
        binding.indexTextView.text = "Index: ${block.index}"
        binding.timestampTextView.text = "Date: ${convertTimestampToReadableDate(block.timestamp)}"
        binding.difficultyTextViewBD.text = "Difficulty: ${block.difficulty}"
        binding.minerTextView.text = "Miner: ${block.miner.toString()}"
        binding.nonceTextView.text = "Nonce: ${block.nonce}"
        binding.hashTextViewBD.text = "Hash: ${block.hash}"
        binding.prevHashTextViewBD.text = "Prev Hash: ${block.previousHash}"

        // Prikažemo kartico
        bottomSheetDialog.show()
    }

    private fun showDataBottomSheet(block: Block){

    }

    private fun convertTimestampToReadableDate(timestamp: Long): String {
        val date = Date(timestamp * 1000)
        val format = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
        return format.format(date)
    }
}










