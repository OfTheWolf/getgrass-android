package com.grass.android.ui.live

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.grass.android.ui.dasboard.DashboardScreen
import com.grass.android.ui.home.ConnectButton

@Composable
fun LiveScreen(
    modifier: Modifier = Modifier,
    viewModel: LiveViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier) {
        DashboardScreen(Modifier)

        ConnectButton(modifier.fillMaxWidth(), viewModel)

        uiState.status?.let {
            Text(text = "Status: $it")
        }
    }
}
