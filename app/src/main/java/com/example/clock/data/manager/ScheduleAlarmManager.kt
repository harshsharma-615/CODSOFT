package com.example.clock.data.manager

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.clock.data.model.Alarm
import com.example.clock.data.receiver.AlarmBroadcastReceiver
import com.example.clock.data.receiver.DAYS_SELECTED
import com.example.clock.data.receiver.HOUR
import com.example.clock.data.receiver.IS_RECURRING
import com.example.clock.data.receiver.MINUTE
import com.example.clock.data.receiver.TITLE
import com.example.clock.data.repository.AlarmRepository
import com.example.clock.util.GlobalProperties.pendingIntentFlags
import com.example.clock.util.helper.AlarmNotificationHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Random
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScheduleAlarmManager @Inject constructor(
    @ApplicationContext private val applicationContext: Context,
    private val alarmRepository: AlarmRepository,
    private val alarmNotificationHelper: AlarmNotificationHelper,
) {

    suspend fun schedule(alarm: Alarm) {
        val alarmManager = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(applicationContext, AlarmBroadcastReceiver::class.java).apply {
            putExtra(IS_RECURRING, alarm.isRecurring)
            putExtra(MINUTE, alarm.minute)
            putExtra(TITLE, alarm.title)
            putExtra(HOUR, alarm.hour)
            putExtra(DAYS_SELECTED, HashMap(alarm.daysSelected))
        }
        val alarmPendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            alarm.id,
            alarmIntent,
            pendingIntentFlags,
        )
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, alarm.hour.toInt())
            set(Calendar.MINUTE, alarm.minute.toInt())
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }
        val toastText = if (alarm.isRecurring) {
            "Recurring Alarm ${alarm.title} scheduled for ${alarm.description} at ${alarm.hour}:${alarm.minute}"
        } else {
            "One Time Alarm ${alarm.title} scheduled for ${alarm.description.substringAfter('-')} at ${alarm.hour}:${alarm.minute}"
        }
        withContext(Dispatchers.Main) {
            Toast.makeText(applicationContext, toastText, Toast.LENGTH_SHORT).show()
        }

        if (alarm.isRecurring) {
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                alarmPendingIntent,
            )
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                alarmPendingIntent,
            )
        }
    }

    suspend fun cancel(alarm: Alarm) {
        val alarmManager = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alamIntent = Intent(applicationContext, AlarmBroadcastReceiver::class.java)
        val alarmPendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            alarm.id,
            alamIntent,
            pendingIntentFlags,
        )
        val toastText = "Alarm canceled for ${alarm.hour}:${alarm.minute}"

        withContext(Dispatchers.Main) {
            Toast.makeText(applicationContext, toastText, Toast.LENGTH_SHORT).show()
        }

        alarmManager.cancel(alarmPendingIntent)
    }

    suspend fun snooze() {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.add(Calendar.MINUTE, 10)

        val alarm = Alarm(
            id = Random().nextInt(Integer.MAX_VALUE),
            hour = String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)),
            minute = String.format("%02d", calendar.get(Calendar.MINUTE)),
            title = "Snooze",
            isScheduled = true,
        )

        schedule(alarm)
    }

    suspend fun clearScheduledAlarms(alarmsList: List<Alarm>) {
        alarmsList.asFlow().buffer().collect {
            cancel(it)
        }
    }
}
