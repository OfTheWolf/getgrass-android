package com.grass.android.ui.home

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.grass.android.GrassService
import com.grass.android.R
import com.grass.android.ui.LoadingScreen
import com.grass.android.ui.live.LiveScreen
import com.grass.android.ui.live.LiveViewModel
import com.grass.android.ui.theme.LightBackround

@Composable
fun HomeScreen(modifier: Modifier = Modifier, homeViewModel: HomeViewModel = viewModel()) {
    val uiState by homeViewModel.uiState.collectAsState<HomeUiState>()
//    val messages by Conductor.messages.observeAsState(emptyList())

//    val status = Conductor.status.observeAsState()

    Column(
        modifier
            .background(LightBackround)
            .padding(Dp(16f)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LazyColumn(modifier = Modifier.weight(1f), contentPadding = PaddingValues(Dp(20f))) {
//            items(messages) { message ->
//                Text(message, fontSize = TextUnit(12f, TextUnitType.Sp))
//                Divider()
//            }
        }

        when (uiState) {
            is HomeUiState.Loading -> {
                Column(
                    modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LoadingScreen(modifier)
                }
            }

            is HomeUiState.Success -> {
                val data = (uiState as HomeUiState.Success).data
                if (!data.isLoggedIn) {
                    LoginScreen(modifier, homeViewModel)
                }
                Column(modifier = modifier.fillMaxSize()) {
                    ResultScreen(
                        modifier = modifier,
                        data = data
                    )
                    LiveScreen(
                        modifier = modifier.weight(1f),
                    )
                }
            }

            is HomeUiState.Error -> {
                Toast.makeText(LocalContext.current, R.string.loading_failed, Toast.LENGTH_SHORT)
                    .show()
            }

            else -> {

            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(modifier: Modifier = Modifier, homeViewModel: HomeViewModel) {
    var username by remember { mutableStateOf(TextFieldValue("")) }
    var password by remember { mutableStateOf(TextFieldValue("")) }

    val focusManager = LocalFocusManager.current

    Column(
        modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = username,
            onValueChange = { newText ->
                username = newText
            },
            placeholder = { Text(text = "Username") },
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done,
                keyboardType = KeyboardType.Password
            ),
        )

        Spacer(modifier.height(Dp(8f)))

        OutlinedTextField(
            value = password,
            onValueChange = { newText ->
                password = newText
            },
            visualTransformation = PasswordVisualTransformation(),
            placeholder = { Text(text = "Password") },
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done,
                keyboardType = KeyboardType.Password
            ),
        )

        Button(modifier = Modifier.padding(Dp(20f)), onClick = {
            homeViewModel.login(username.text, password.text)
        }) {
            Text(
                text = "Login", modifier = modifier
            )
        }
    }
}

@Composable
fun ConnectButton(modifier: Modifier = Modifier, viewModel: LiveViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    val context = LocalContext.current

    Button(modifier = Modifier.padding(vertical = Dp(16f)), onClick = {
        if (uiState.isConnected) {
            GrassService.stopService(context)
        } else {
            GrassService.startService(context)
        }
        viewModel.toggleConnection()
    }) {
        Text(
            text = if (uiState.isConnected) "Disconnect" else "Connect",
            modifier = modifier,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun InfoItemView(modifier: Modifier = Modifier, tag: String, text: String) {
    Column(
        modifier
            .fillMaxWidth()
            .background(Color(0, 0, 0, 0x0F))
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(modifier = modifier.alpha(0.5f), text = tag, fontSize = 13.sp)
        Text(text = text)
    }
    Divider()
}


@Composable
fun ResultScreen(modifier: Modifier = Modifier, data: HomeUiData) {
    LazyColumn(
        modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        userScrollEnabled = false
    ) {
        item {
            InfoItemView(tag = "UserId", text = data.userId ?: "")
        }

        item {
            InfoItemView(tag = "DeviceId", text = data.deviceId ?: "")
        }

        item {
            InfoItemView(tag = "Email", text = data.email ?: "")
        }
    }
}
