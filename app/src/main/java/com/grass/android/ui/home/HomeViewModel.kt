package com.grass.android.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grass.android.Logger
import com.grass.android.PreferencesRepository
import com.grass.android.data.Login
import com.grass.android.network.GrassApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val logger: Logger
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Idle)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            preferencesRepository.readData().collect {
                it?.let { data ->
                    if (!data.userId.isNullOrEmpty()
                        || !data.deviceId.isNullOrEmpty()
                        || !data.email.isNullOrEmpty()) {
                        val uiData = HomeUiData(data.userId, data.deviceId, data.email)
                        _uiState.value = HomeUiState.Success(uiData)
                    }
                }
            }
        }
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            _uiState.value = try {
                val loginResult =
                    GrassApi.retrofitService.login(Login.Request(username, password)).result.data
                preferencesRepository.saveLoginData(loginResult)
                val devices = GrassApi.retrofitService.devices().result.data.data
                val device = devices.firstOrNull()
                device?.let { preferencesRepository.saveDevicesData(it) }
                val uiData = HomeUiData(device?.userId, device?.deviceId, loginResult.email)
                HomeUiState.Success(uiData)
            } catch (e: Exception) {
                HomeUiState.Error
            }
        }
    }
}