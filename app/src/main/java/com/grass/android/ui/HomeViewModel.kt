package com.grass.android.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grass.android.Conductor
import com.grass.android.PreferencesRepository
import com.grass.android.data.Login
import com.grass.android.network.GrassApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository, private val conductor: Conductor
) : ViewModel() {
    private val TAG = "HomeViewModel"

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            preferencesRepository.readData().collect {
                it?.let { data ->
                    _uiState.value = UiState.Success(data)
                }
            }
        }

        conductor.state.onEach {
            Log.d(TAG, it.toString())
        }.launchIn(viewModelScope)
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            _uiState.value = try {
                val loginResult =
                    GrassApi.retrofitService.login(Login.Request(username, password)).result.data
                preferencesRepository.saveLoginData(loginResult)
                val devices = GrassApi.retrofitService.devices().result.data.data
                preferencesRepository.saveDevicesData(devices)
                UiState.Success(loginResult)
            } catch (e: Exception) {
                UiState.Error
            }
        }
    }
}