package com.example.clock.data.repository

import com.example.clock.data.model.Alarm
import com.example.clock.data.local.AlarmDao
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

class AlarmRepository @Inject constructor(
    private val alarmDao: AlarmDao
) {
    val alarmsList = alarmDao.getAlarmsList().distinctUntilChanged()

    suspend fun insert(alarm: Alarm) = alarmDao.insert(alarm)

    suspend fun update(alarm: Alarm) = alarmDao.update(alarm)

    suspend fun delete(alarm: Alarm) = alarmDao.delete(alarm)

    suspend fun getLastAutoId() = alarmDao.getLastAutoId()

    suspend fun clear() = alarmDao.clear()

    suspend fun getAlarmById(id: Int) = alarmDao.getAlarmById(id)

    fun getAlarmByTime(hour: String, minute: String, recurring: Boolean) = alarmDao.getAlarmByTime(hour, minute, recurring)

}