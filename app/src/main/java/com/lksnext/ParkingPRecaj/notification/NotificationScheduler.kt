package com.lksnext.ParkingPRecaj.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.lksnext.ParkingPRecaj.data.model.Reservation
import java.util.Calendar

object NotificationScheduler {

    private const val TAG = "NotificationScheduler"
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
        val now = System.currentTimeMillis()

        Log.d(TAG, "Scheduling notifications for reservation: ${reservation.id} at ${reservation.startTime}")

        // Obtener fecha en UTC para evitar desfases de zona horaria al extraer D/M/Y
        val dateUtc = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = reservation.date
        }

        // Notificación 30 minutos antes del inicio
        val (startHour, startMinute) = reservation.startTime.split(":").map { it.toInt() }
        val startDateTime = Calendar.getInstance().apply {
            set(Calendar.YEAR, dateUtc.get(Calendar.YEAR))
            set(Calendar.MONTH, dateUtc.get(Calendar.MONTH))
            set(Calendar.DAY_OF_MONTH, dateUtc.get(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, startHour)
            set(Calendar.MINUTE, startMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        val triggerStartTime = (startDateTime.clone() as Calendar).apply {
            add(Calendar.MINUTE, -30)
        }.timeInMillis

        if (triggerStartTime > now) {
            scheduleNotification(
                context,
                alarmManager,
                reservation.id + "_start",
                triggerStartTime,
                "Tu reserva comienza pronto",
                "Tu reserva en la plaza ${reservation.spotNumber} comienza en 30 minutos"
            )
        } else if (startDateTime.timeInMillis > now) {
            // Si falta menos de 30 min pero aún no ha empezado, avisar pronto para feedback
            Log.d(TAG, "Reservation starts in less than 30 mins, scheduling immediate notification")
            scheduleNotification(
                context,
                alarmManager,
                reservation.id + "_start_soon",
                now + 2000, // En 2 segundos
                "Tu reserva comienza pronto",
                "Tu reserva en la plaza ${reservation.spotNumber} comenzará a las ${reservation.startTime}"
            )
        }

        // Notificación 15 minutos antes del fin
        val (endHour, endMinute) = reservation.endTime.split(":").map { it.toInt() }
        val endDateTime = Calendar.getInstance().apply {
            set(Calendar.YEAR, dateUtc.get(Calendar.YEAR))
            set(Calendar.MONTH, dateUtc.get(Calendar.MONTH))
            set(Calendar.DAY_OF_MONTH, dateUtc.get(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, endHour)
            set(Calendar.MINUTE, endMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Caso especial: si la hora de fin es menor que la de inicio, es el día siguiente
        if (endDateTime.timeInMillis <= startDateTime.timeInMillis) {
            endDateTime.add(Calendar.DAY_OF_YEAR, 1)
        }

        val triggerEndTime = (endDateTime.clone() as Calendar).apply {
            add(Calendar.MINUTE, -15)
        }.timeInMillis

        if (triggerEndTime > now) {
            scheduleNotification(
                context,
                alarmManager,
                reservation.id + "_end",
                triggerEndTime,
                "Tu reserva finaliza pronto",
                "Tu reserva en la plaza ${reservation.spotNumber} finaliza en 15 minutos"
            )
        }
    }

    private fun scheduleNotification(
        context: Context,
        alarmManager: AlarmManager,
        notificationId: String,
        triggerTime: Long,
        title: String,
        message: String
    ) {
        Log.d(TAG, "Scheduling alarm for $notificationId at ${java.util.Date(triggerTime)}")
        
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

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                Log.w(TAG, "Cannot schedule exact alarms, falling back to set()")
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                }
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException scheduling alarm: ${e.message}")
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
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
