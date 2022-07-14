package com.example.clock.timer

import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.internal.isLiveLiteralsEnabled
import com.example.clock.stopwatch.StopwatchState
import com.zhuinden.flowcombinetuplekt.combineTuple
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

private const val TIME_FORMAT = "%02d:%02d:%02d"

data class TimerState(
    val timeInLong: Long,
    val time: String,
    val hours: Long,
    val minutes: Long,
    val seconds: Long,
    val progress: Float,
    val isPlaying: Boolean,
    val isDone: Boolean
)

@Singleton
class TimerManager @Inject constructor(
    private val timerServiceManager: TimerServiceManager,
    private val notificationHelper: NotificationHelper
) {

    private val timeInLongFlow = MutableStateFlow(0L)
    private val timeFlow = MutableStateFlow("00:00:00")
    private val hourFlow = MutableStateFlow(0L)
    private val minuteFlow = MutableStateFlow(0L)
    private val secondFlow = MutableStateFlow(0L)
    private val progressFlow = MutableStateFlow(1.00F)
    private val isPlayingFlow = MutableStateFlow(false)
    private val isDoneFlow = MutableStateFlow(false)

    val timerState = combineTuple(
        timeInLongFlow,
        timeFlow,
        hourFlow,
        minuteFlow,
        secondFlow,
        progressFlow,
        isPlayingFlow,
        isDoneFlow
    ).map { (timeInLong, time, hours, minutes,  seconds, progress, isPlaying, isDone) ->
        TimerState(
            timeInLong = timeInLong, time = time, hours = hours, minutes = minutes,
            seconds = seconds, progress = progress, isPlaying = isPlaying, isDone = isDone
        )
    }

    private var countDownTimer: CountDownTimerExt? = null

    fun setTHours(hours: Long) {
        hourFlow.value = hours.times(3600000L)
    }

    fun setMinutes(minutes: Long) {
        minuteFlow.value = minutes.times(60000L)
    }

    fun setSeconds(seconds: Long) {
        secondFlow.value = seconds.times(1000L)
    }

     fun setTime() {
        timeInLongFlow.value = hourFlow.value + minuteFlow.value + secondFlow.value
        countDownTimer = object : CountDownTimerExt(timeInLongFlow.value, 1000) {
            override fun onTimerTick(millisUntilFinished: Long) {
                val progressValue = millisUntilFinished.toFloat() / timeInLongFlow.value
                handleTimerValues(true, millisUntilFinished.formatTime(), progressValue)
            }

            override fun onTimerFinish() {
                onChangeDone()
                stopService()
                notificationHelper.showTimerCompletedNotification(timeFlow.value)
                handleTimerValues(false, timeInLongFlow.value.formatTime(), 1.0F)
            }
        }
    }

     fun handleCountDownTimer() {
        notificationHelper.removeTimerCompletedNotification()
        if (isPlayingFlow.value) {
            pauseTimer()
        } else {
            startTimer()
        }
    }

    private fun pauseTimer() {
        countDownTimer?.pause()
        isPlayingFlow.value = false
    }

    fun onChangeDone() {
        isDoneFlow.value = true
    }

    fun resetTimer() {
        countDownTimer?.restart()
        timeFlow.value = timeInLongFlow.value.formatTime()
        isPlayingFlow.value = false
        isDoneFlow.value = false
    }

    fun stopService() {
     timerServiceManager.stopTimerService()
    }

    private fun startTimer() {
        countDownTimer?.start()
        isPlayingFlow.value = true
        timerServiceManager.startTimerService()
    }

     private fun handleTimerValues(
        isPlaying: Boolean,
        text: String,
        progress: Float
    ) {
        isPlayingFlow.value = isPlaying
        timeFlow.value = text
        progressFlow.value = progress
    }

     fun Long.formatTime(): String = String.format(
        TIME_FORMAT,
        TimeUnit.MILLISECONDS.toHours(this),
        TimeUnit.MILLISECONDS.toMinutes(this) % 60,
        TimeUnit.MILLISECONDS.toSeconds(this) % 60
    )

}

private const val TAG = "TimerManager"
