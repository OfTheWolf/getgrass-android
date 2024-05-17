package com.grass.android.ui.home

data class HomeUiData(
    val userId: String?,
    val deviceId: String?,
    val email: String?,
    val isLoggedIn: Boolean
)

sealed class HomeUiState {
    data class Success(val data: HomeUiData) : HomeUiState()
    object Error : HomeUiState()
    object Loading : HomeUiState()
    object Idle : HomeUiState()
}