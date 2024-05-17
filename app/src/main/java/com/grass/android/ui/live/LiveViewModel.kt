package com.grass.android.ui.live

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grass.android.GrassService
import com.grass.android.Logger
import com.grass.android.network.Status
import com.grass.android.network.WebSocketFlow
import com.grass.android.network.WebSocketState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class LiveViewModel @Inject constructor(
    private val webSocketFlow: WebSocketFlow,
    private val logger: Logger
) : ViewModel() {
    private var isConnected: Boolean = false

    private val _uiState = MutableStateFlow(LiveUiState(isConnected = isConnected))
    val uiState: StateFlow<LiveUiState> = _uiState.asStateFlow()

    private var job: Job? = null

    init {
        isConnected = GrassService.isConnected
        refresh()
    }

    fun toggleConnection() {
        isConnected = !isConnected
        val value = uiState.value
        val isConnected = value.isConnected
        _uiState.value = LiveUiState(value.status, isConnected)
    }

    private fun refresh() {
        job?.cancel()
        job = webSocketFlow.state.onEach { state ->
            _uiState.value = when (state) {
                is WebSocketState.State -> {
                    val isConnected = state.status == Status.CONNECTED
                    LiveUiState(state.status.name, isConnected)
                }

                is WebSocketState.Message -> {
                    val isConnected =
                        (uiState.value.status ?: Status.DEAD) == Status.CONNECTED
                    LiveUiState(uiState.value.status, isConnected)
                }
            }
            logger.log("viewModel", state.toString())
        }.launchIn(viewModelScope)
    }
}