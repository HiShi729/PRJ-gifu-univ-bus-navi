package com.example.prj_gifu_univ_bus_navi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.prj_gifu_univ_bus_navi.data.LocalBusScheduleData
import com.example.prj_gifu_univ_bus_navi.data.UserSettingsRepository
import com.example.prj_gifu_univ_bus_navi.ui.GifuBusNaviApp
import com.example.prj_gifu_univ_bus_navi.ui.MainViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        viewModel.loadInitialData(
            busTrips = LocalBusScheduleData.loadBusTrips(this),
            repository = UserSettingsRepository(applicationContext),
        )
        setContent {
            GifuBusNaviApp(viewModel)
        }
    }
}
