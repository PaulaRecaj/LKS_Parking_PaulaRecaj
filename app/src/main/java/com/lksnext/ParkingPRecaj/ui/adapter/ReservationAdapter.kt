package com.lksnext.ParkingPRecaj.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lksnext.ParkingPRecaj.data.model.Reservation
import com.lksnext.ParkingPRecaj.databinding.ItemReservationBinding
import java.text.SimpleDateFormat
import java.util.*

class ReservationAdapter(
    private var reservations: List<Reservation>,
    private val onEditClick: ((Reservation) -> Unit)? = null,
    private val onCancelClick: ((Reservation) -> Unit)? = null,
    private val showActions: Boolean = false
) : RecyclerView.Adapter<ReservationAdapter.ReservationViewHolder>() {

    private val dateFormat = SimpleDateFormat("EEEE, d 'de' MMMM 'de' yyyy", Locale("es", "ES"))

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReservationViewHolder {
        val binding = ItemReservationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ReservationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReservationViewHolder, position: Int) {
        holder.bind(reservations[position])
    }

    override fun getItemCount(): Int = reservations.size

    fun updateReservations(newReservations: List<Reservation>) {
        reservations = newReservations
        notifyDataSetChanged()
    }

    inner class ReservationViewHolder(
        private val binding: ItemReservationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(reservation: Reservation) {
            with(binding) {
                // Fecha formateada
                tvDate.text = dateFormat.format(Date(reservation.date))

                // Horario
                tvTime.text = "${reservation.startTime} - ${reservation.endTime}"

                // Número de plaza
                tvSpotNumber.text = reservation.spotNumber

                // Tipo de plaza
                tvParkingType.text = reservation.parkingType.getDisplayName()

                // Mostrar/ocultar botones de acción
                if (showActions) {
                    layoutActions.visibility = View.VISIBLE

                    btnEdit.setOnClickListener {
                        onEditClick?.invoke(reservation)
                    }

                    btnCancel.setOnClickListener {
                        onCancelClick?.invoke(reservation)
                    }
                } else {
                    layoutActions.visibility = View.GONE
                }
            }
        }
    }
}
