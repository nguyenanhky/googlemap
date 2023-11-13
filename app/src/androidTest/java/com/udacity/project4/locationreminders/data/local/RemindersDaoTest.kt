package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

//    TODO: Add testing implementation to the RemindersDao.kt

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase

    @Before
    fun initDb() {
        // Using an in-memory database so that the information stored here disappears when the
        // process is killed.
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()
    }


    @After
    fun closeDb() = database.close()


    @Test
    fun saveAndGetReminderById() = runBlockingTest {
        val reminder = ReminderDTO("Ha Noi", "Get to the Shop", "Ha Noi", 21.0285, 105.8542)

        database.reminderDao().saveReminder(reminder)

        val result = database.reminderDao().getReminderById(reminder.id)

        assertEquals(reminder, result)
    }

    @Test
    fun getAllRemindersFromDb() = runBlockingTest {
        val reminder = ReminderDTO("Ho Chi Minh ", "Get to the Shop", "Ho Chi Minh", 10.7769, 106.7009)
        val reminder2 = ReminderDTO("Phu Tho", "Get to the office", "Phu tho", 21.4225, 104.8796)
        val reminder3 = ReminderDTO("Da Nang", "Get to the Gym", "Da Nang", 16.0544, 108.2022)

        database.reminderDao().saveReminder(reminder)
        database.reminderDao().saveReminder(reminder2)
        database.reminderDao().saveReminder(reminder3)

        val remindersList = database.reminderDao().getReminders()

        assertEquals(listOf(reminder, reminder2, reminder3), remindersList)
    }

    @Test
    fun insertReminders_deleteAllReminders() = runBlockingTest {
        val reminder = ReminderDTO("Ho Chi Minh ", "Get to the Shop", "Ho Chi Minh", 10.7769, 106.7009)
        val reminder2 = ReminderDTO("Phu Tho", "Get to the office", "Phu tho", 21.4225, 104.8796)
        val reminder3 = ReminderDTO("Da Nang", "Get to the Gym", "Da Nang", 16.0544, 108.2022)


        database.reminderDao().saveReminder(reminder)
        database.reminderDao().saveReminder(reminder2)
        database.reminderDao().saveReminder(reminder3)

        database.reminderDao().deleteAllReminders()

        val remindersList = database.reminderDao().getReminders()

        assertTrue(remindersList.isEmpty())
    }


}