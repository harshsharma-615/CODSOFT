package com.example.clock.data.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.example.clock.data.manager.ServiceManager
import com.example.clock.util.helper.MediaPlayerHelper
import com.example.clock.util.helper.TIMER_COMPLETED_NOTIFICATION_ID
import com.example.clock.util.helper.TimerNotificationHelper
import com.example.clock.util.isServiceRunning
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TimerCompletedService : Service() {

    @Inject
    lateinit var serviceManager: ServiceManager

    @Inject
    lateinit var timerNotificationHelper: TimerNotificationHelper

    @Inject
    lateinit var mediaPlayerHelper: MediaPlayerHelper
    private val serviceScope = CoroutineScope(SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        mediaPlayerHelper.prepare()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(
            TIMER_COMPLETED_NOTIFICATION_ID,
            timerNotificationHelper.showTimerCompletedNotification(),
        )

        serviceScope.launch {
            if (applicationContext.isServiceRunning(TimerRunningService::class.java)) {
                serviceManager.stopService(TimerRunningService::class.java)
            }
            mediaPlayerHelper.start()
        }
        return START_STICKY
    }

    override fun onBind(p0: Intent?): IBinder? = null

    override fun onDestroy() {
        serviceScope.cancel()
        mediaPlayerHelper.release()
        timerNotificationHelper.removeTimerCompletedNotification()
        super.onDestroy()
    }
}
