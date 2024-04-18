package com.grass.android.ui

import com.grass.android.data.Login

sealed class UiState {
    data class Success(val data: Login.Response) : UiState()
    object Error : UiState()
    object Loading : UiState()
    object Idle : UiState()
}