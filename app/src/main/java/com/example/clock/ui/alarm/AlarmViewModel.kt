package com.example.clock.ui.alarm

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.clock.data.manager.ScheduleAlarmManager
import com.example.clock.data.model.Alarm
import com.example.clock.data.repository.AlarmRepository
import com.example.clock.util.Constants.alarmDefaultValue
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.system.measureTimeMillis


private const val TAG = "AlarmsListViewModel"

@HiltViewModel
class AlarmViewModel @Inject constructor(
    private val alarmRepository: AlarmRepository,
    private val scheduleAlarmManager: ScheduleAlarmManager
) : ViewModel() {

    val alarmsListState = alarmRepository.alarmsList.asLiveData()
    var createAlarmState by mutableStateOf(alarmDefaultValue)
    private set

    fun changeCreateAlarmState(alarm: Alarm) {
        createAlarmState = alarm
    }

    fun update(alarm: Alarm) = viewModelScope.launch {
        Log.e(TAG, "description: ${alarm.description}", )
        alarmRepository.update(alarm)
    }

    fun onScheduledChange(alarm: Alarm) {
        viewModelScope.launch {
            listOf(
                async { update(alarm) },
                async {
                    if (alarm.isScheduled) {
                        scheduleAlarmManager.schedule(alarm)
                    } else {
                        scheduleAlarmManager.cancel(alarm)
                    }
                }
            )
        }
    }

    fun remove(alarm: Alarm) {
        viewModelScope.launch {
            listOf(
                async { alarmRepository.delete(alarm) },
                async {
                    if (alarm.isScheduled) {
                        scheduleAlarmManager.cancel(alarm)
                    }
                }
            )
        }
    }

    fun saveAlarm() {
        viewModelScope.launch {
            val lastAutoGeneratedId = alarmRepository.getLastAutoId()
            val alarm = alarmRepository.getAlarmById(createAlarmState.id)

            if (!createAlarmState.isScheduled) {
                createAlarmState.isScheduled = true
            }

            listOf(
                async {
                    if (alarm?.id == createAlarmState.id) {
                        update(createAlarmState)
                    } else {
                        createAlarmState.id = lastAutoGeneratedId?.plus(1) ?: 1
                        alarmRepository.insert(createAlarmState)
                    }
                },
                async { scheduleAlarmManager.schedule(createAlarmState) }
            )

        }
    }

    fun clearAlarmsList() {
        viewModelScope.launch {
            alarmsListState.value?.let {
                listOf(
                    async { alarmRepository.clear() },
                    async { scheduleAlarmManager.cancelAlarms(alarms = it) }
                )
            }
        }
    }

}


