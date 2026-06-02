package com.lksnext.ParkingPRecaj.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.lksnext.ParkingPRecaj.R
import com.lksnext.ParkingPRecaj.databinding.ItemSpotBinding

class SpotAdapter(
    private var spots: List<String>,
    private val onSpotSelected: (String) -> Unit
) : RecyclerView.Adapter<SpotAdapter.SpotViewHolder>() {

    private var selectedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpotViewHolder {
        val binding = ItemSpotBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SpotViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SpotViewHolder, position: Int) {
        holder.bind(spots[position], position == selectedPosition)
    }

    override fun getItemCount(): Int = spots.size

    fun updateSpots(newSpots: List<String>) {
        spots = newSpots
        selectedPosition = -1
        notifyDataSetChanged()
    }

    fun getSelectedSpot(): String? {
        return if (selectedPosition != -1) spots[selectedPosition] else null
    }

    inner class SpotViewHolder(private val binding: ItemSpotBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(spotNumber: String, isSelected: Boolean) {
            binding.tvSpotNumber.text = spotNumber
            
            val context = binding.root.context
            if (isSelected) {
                binding.cardSpot.setCardBackgroundColor(ContextCompat.getColor(context, R.color.primary_orange))
                binding.tvSpotNumber.setTextColor(ContextCompat.getColor(context, android.R.color.white))
            } else {
                binding.cardSpot.setCardBackgroundColor(ContextCompat.getColor(context, android.R.color.white))
                binding.tvSpotNumber.setTextColor(ContextCompat.getColor(context, R.color.text_primary))
            }

            binding.root.setOnClickListener {
                val previousPosition = selectedPosition
                selectedPosition = adapterPosition
                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)
                onSpotSelected(spotNumber)
            }
        }
    }
}
