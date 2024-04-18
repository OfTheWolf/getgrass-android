package com.grass.android.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.lifecycle.viewmodel.compose.viewModel
import com.grass.android.Conductor
import com.grass.android.GrassService
import com.grass.android.R
import com.grass.android.data.Login

@Composable
fun HomeScreen(modifier: Modifier = Modifier, homeViewModel: HomeViewModel = viewModel()) {
    val uiState by homeViewModel.uiState.collectAsState<UiState>()
    val messages by Conductor.messages.observeAsState(emptyList())

//    val status = Conductor.status.observeAsState()

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        LazyColumn(modifier = Modifier.weight(1f), contentPadding = PaddingValues(Dp(20f))) {
            items(messages) { message ->
                Text(message, fontSize = TextUnit(12f, TextUnitType.Sp))
                Divider()
            }
        }

        when (uiState) {
            is UiState.Loading -> {
                Column(
                    modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LoadingScreen(modifier)
                }
            }

            is UiState.Success -> {
                ResultScreen(
                    modifier = modifier.fillMaxSize(),
                    data = (uiState as UiState.Success).data
                )
                ConnectButton(modifier)
            }

            is UiState.Error -> {
                Toast.makeText(LocalContext.current, R.string.loading_failed, Toast.LENGTH_SHORT)
                    .show()
            }

            else -> {

            }
        }

        if (!(uiState is UiState.Success))
            LoginScreen(modifier, homeViewModel)

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
fun ConnectButton(modifier: Modifier = Modifier) {
    var connected by remember { mutableStateOf(false) }

    val context = LocalContext.current

    Button(modifier = Modifier.padding(Dp(20f)), onClick = {
        if (connected) {
            GrassService.stopService(context)
        } else {
            GrassService.startService(context)
        }
        connected = !connected
    }) {
        Text(
            text = if (connected) "Disconnect" else "Connect", modifier = modifier
        )
    }
}


@Composable
fun ResultScreen(modifier: Modifier = Modifier, data: Login.Response) {
    Text(text = "UserId: ${data.userId}")
    Text(text = "Email: ${data.email}")
}
