package com.grass.android.ui.live

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grass.android.GrassService
import com.grass.android.Logger
import com.grass.android.network.Status
import com.grass.android.network.WebSocketFlow
import com.grass.android.network.WebSocketState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class LiveViewModel @Inject constructor(
    webSocketFlow: WebSocketFlow,
    private val logger: Logger
) : ViewModel() {
    private var isConnected: Boolean = false

    private val _uiState = MutableStateFlow(LiveUiState(isConnected = isConnected))
    val uiState: StateFlow<LiveUiState> = _uiState.asStateFlow()

    init {
        isConnected = GrassService.isConnected
        refresh()
        webSocketFlow.state
            .onEach {
                _uiState.value = when (val state = it) {
                    is WebSocketState.State -> {
                        val isConnected = state.status == Status.CONNECTED
                        LiveUiState(uiState.value.message, state.status.name, isConnected)
                    }

                    is WebSocketState.Message -> {
                        val isConnected =
                            (uiState.value.status ?: Status.DEAD) == Status.CONNECTED
                        LiveUiState(state.message, uiState.value.status, isConnected)
                    }
                }
                logger.log("viewModel", it.toString())
            }
            .launchIn(viewModelScope)
    }

    fun toggleConnection() {
        isConnected = !isConnected
        refresh()
    }

    private fun refresh() {
        val value = uiState.value
        _uiState.value = LiveUiState(value.message, value.status, isConnected)
    }
}