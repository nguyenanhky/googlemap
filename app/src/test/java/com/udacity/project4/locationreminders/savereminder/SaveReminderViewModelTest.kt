package com.udacity.project4.locationreminders.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.udacity.project4.R
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.pauseDispatcher
import kotlinx.coroutines.test.resumeDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {


    //TODO: provide testing to the SaveReminderView and its live data objects
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var remindersRepository: FakeDataSource
    private lateinit var viewModel: SaveReminderViewModel

    @Before
    fun setupViewModel() {
        remindersRepository = FakeDataSource()
        viewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(), remindersRepository)
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun checkTitleEmptyAndShowSnackBarError() = runBlockingTest {
        val reminder = ReminderDataItem("", "Phu Tho", "My School", 21.4225, 104.8796)

        assertThat(viewModel.validateEnteredData(reminder)).isFalse()
        assertThat(viewModel.showSnackBarInt.value).isEqualTo(R.string.err_select_location)
    }

    @Test
    fun handleEmptyLocationAndShowSnackBarError() = runBlockingTest {
        val reminder = ReminderDataItem("Phu Tho", "Get to the office", "Phu tho", 21.4225, 104.8796)

        assertThat(viewModel.validateEnteredData(reminder)).isFalse()
        assertThat(viewModel.showSnackBarInt.value).isEqualTo(R.string.err_select_location)
    }

    @Test
    fun showLoadingWhileSavingReminder() = runBlockingTest {
        val reminder = ReminderDataItem("Phu Tho", "Get to the office", "Phu tho", 21.4225, 104.8796)


        mainCoroutineRule.pauseDispatcher()
        viewModel.saveReminder(reminder)

        assertThat(viewModel.showLoading.value).isTrue()

        mainCoroutineRule.resumeDispatcher()

        assertThat(viewModel.showLoading.value).isFalse()
    }


}