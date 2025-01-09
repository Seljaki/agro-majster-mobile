package com.seljaki.agromajtermobile

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.seljaki.agromajtermobile.R
import com.seljaki.lib.Block

class BlockAdapter(private val blocks: List<Block>) : RecyclerView.Adapter<BlockAdapter.BlockViewHolder>() {

    class BlockViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val hashTextView: TextView = view.findViewById(R.id.hashTextView)
        val timestampTextView: TextView = view.findViewById(R.id.timestampTextView)
        val dataTextView: TextView = view.findViewById(R.id.dataTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlockViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.block_item, parent, false)
        return BlockViewHolder(view)
    }

    override fun onBindViewHolder(holder: BlockViewHolder, position: Int) {
        val block = blocks[position]
        holder.hashTextView.text = "Hash: ${block.hash}"
        holder.timestampTextView.text = "Time mined: ${block.timestamp}"
        holder.dataTextView.text = "Temperature: ${block.data.temperature}, Coordinates: (${block.data.latitude}, ${block.data.longitude})"
    }

    override fun getItemCount(): Int {
        Log.d("Blocks Size", blocks.size.toString())
        return blocks.size
    }
}
