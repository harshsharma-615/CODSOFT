package com.example.clock.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmDao {

    @Insert
    suspend fun insert(alarm: Alarm)

    @Delete
    suspend fun delete(alarm: Alarm)

    @Update
    suspend fun update(alarm: Alarm)

    @Query("DELETE FROM alarm_Item_table")
    suspend fun clear()

    @Query("SELECT * FROM ALARM_ITEM_TABLE ORDER BY alarmId DESC")
    fun getAlarmsItems(): Flow<List<Alarm>>

    @Query("SELECT * FROM ALARM_ITEM_TABLE WHERE alarmId=:alarmId ")
    suspend fun getAlarmItem(alarmId: Long): Alarm?
}