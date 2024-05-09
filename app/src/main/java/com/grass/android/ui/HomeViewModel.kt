package com.grass.android.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grass.android.PreferencesRepository
import com.grass.android.data.Login
import com.grass.android.network.GrassApi
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {
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