package com.lksnext.ParkingPRecaj.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.lksnext.ParkingPRecaj.data.model.Reservation
import java.util.Calendar

object NotificationScheduler {

    private const val CHANNEL_ID = "parking_reservations"
    private const val CHANNEL_NAME = "Reservas de Parking"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones de reservas de parking"
                enableVibration(true)
                enableLights(true)
            }

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun scheduleReservationNotifications(context: Context, reservation: Reservation) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Notificación 30 minutos antes del inicio
        val (startHour, startMinute) = reservation.startTime.split(":").map { it.toInt() }
        val startDateTime = Calendar.getInstance().apply {
            timeInMillis = reservation.date
            set(Calendar.HOUR_OF_DAY, startHour)
            set(Calendar.MINUTE, startMinute)
            add(Calendar.MINUTE, -30) // 30 minutos antes
        }

        scheduleNotification(
            context,
            alarmManager,
            reservation.id + "_start",
            startDateTime.timeInMillis,
            "Tu reserva comienza pronto",
            "Tu reserva en la plaza ${reservation.spotNumber} comienza en 30 minutos"
        )

        // Notificación 15 minutos antes del fin
        val (endHour, endMinute) = reservation.endTime.split(":").map { it.toInt() }
        val endDateTime = Calendar.getInstance().apply {
            timeInMillis = reservation.date
            set(Calendar.HOUR_OF_DAY, endHour)
            set(Calendar.MINUTE, endMinute)
            add(Calendar.MINUTE, -15) // 15 minutos antes
        }

        scheduleNotification(
            context,
            alarmManager,
            reservation.id + "_end",
            endDateTime.timeInMillis,
            "Tu reserva finaliza pronto",
            "Tu reserva en la plaza ${reservation.spotNumber} finaliza en 15 minutos"
        )
    }

    private fun scheduleNotification(
        context: Context,
        alarmManager: AlarmManager,
        notificationId: String,
        triggerTime: Long,
        title: String,
        message: String
    ) {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("notification_id", notificationId.hashCode())
            putExtra("title", title)
            putExtra("message", message)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }

    fun cancelReservationNotifications(context: Context, reservationId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        listOf("${reservationId}_start", "${reservationId}_end").forEach { notificationId ->
            val intent = Intent(context, NotificationReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        }
    }
}
