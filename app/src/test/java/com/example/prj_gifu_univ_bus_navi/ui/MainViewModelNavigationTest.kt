package com.example.prj_gifu_univ_bus_navi.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class MainViewModelNavigationTest {
    @Test
    fun settingsBackReturnsHome() {
        val viewModel = MainViewModel()

        viewModel.navigate(AppScreen.SETTINGS)
        viewModel.navigateBack()

        assertEquals(AppScreen.HOME, viewModel.currentScreen)
    }

    @Test
    fun resultBackReturnsHome() {
        val viewModel = MainViewModel()

        viewModel.navigate(AppScreen.RESULT)
        viewModel.navigateBack()

        assertEquals(AppScreen.HOME, viewModel.currentScreen)
    }

    @Test
    fun nestedSettingsScreensBackReturnsSettings() {
        val viewModel = MainViewModel()

        viewModel.navigate(AppScreen.ADD_NODE)
        viewModel.navigateBack()
        assertEquals(AppScreen.SETTINGS, viewModel.currentScreen)

        viewModel.navigate(AppScreen.TRAVEL_TIME_PROFILE)
        viewModel.navigateBack()
        assertEquals(AppScreen.SETTINGS, viewModel.currentScreen)
    }
}
