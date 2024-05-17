package com.grass.android.ui.dasboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grass.android.Logger
import com.grass.android.repository.EarningsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val earningsRepository: EarningsRepository,
    private val decimalFormat: DecimalFormat,
    private val logger: Logger
) : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardUiState("-", "-"))
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private var job: Job? = null

    init {
        fetchData()
    }

    private fun fetchData() {
        job?.cancel()
        job = viewModelScope.launch {
            earningsRepository.earnings()
                .map {
                    DashboardUiState(
                        decimalFormat.format(it.totalEpoch),
                        decimalFormat.format(it.totalToday)
                    )
                }
                .onEach {
                    logger.log("dashboard", it.toString())
                }
                .collect {
                    _uiState.value = it
                }
        }
    }
}