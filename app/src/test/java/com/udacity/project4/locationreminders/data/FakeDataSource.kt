package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(var remindersList: MutableList<ReminderDTO>? = mutableListOf()) : ReminderDataSource {

    //    TODO: Create a fake data source to act as a double to the real data source
    private var isError = false

    fun setReturnError(value: Boolean) {
        isError = value
    }
    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        TODO("Return the reminders")
        return if (isError) {
            Result.Error("Error getting reminders")
        } else {
            remindersList?.let { Result.Success(it) } ?: Result.Error("Reminders not found")
        }
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        TODO("save the reminder")
        remindersList?.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        TODO("return the reminder with the id")
        return when (val reminder = remindersList?.find { reminderDTO -> reminderDTO.id == id }) {
            null -> Result.Error("Reminder not found!")
            else -> if (isError) {
                Result.Error("Reminder not found!")
            } else {
                Result.Success(reminder)
            }
        }
    }

    override suspend fun deleteAllReminders() {
        TODO("delete all the reminders")
        remindersList?.clear()
    }


}