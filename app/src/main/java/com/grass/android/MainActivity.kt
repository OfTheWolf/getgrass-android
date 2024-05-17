package com.grass.android

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.grass.android.ui.home.HomeScreen
import com.grass.android.ui.home.HomeViewModel
import com.grass.android.ui.theme.GrassTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var requestPermissionLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                Log.d("POST_NOTIFICATION_PERMISSION", "USER DENIED PERMISSION")
            } else {
                Log.d("POST_NOTIFICATION_PERMISSION", "USER GRANTED PERMISSION")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GrassTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    HomeScreen()
                }
            }
        }

        requestNotificationPermission()
    }

    override fun onResume() {
        super.onResume()
//        Conductor.updateUI = true
    }

    override fun onPause() {
        super.onPause()
//        Conductor.updateUI = false
    }

    /**
     * Requests notification permission if it's not granted.
     * Shows a toast message indicating the permission status.
     */
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            when {
                ContextCompat.checkSelfPermission(
                    this, permission
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Action to take when permission is already granted
                    Toast.makeText(this, "Permission granted", Toast.LENGTH_LONG).show()
                }

                shouldShowRequestPermissionRationale(permission) -> {
                    // Action to take when permission was denied
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_LONG).show()
                }

                else -> {
                    // Request permission
                    requestPermissionLauncher.launch(permission)
                }
            }
        } else {
            // Device does not support required permission
            Toast.makeText(this, "No required permission", Toast.LENGTH_LONG).show()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    GrassTheme {
        val homeViewModel: HomeViewModel = viewModel()
        HomeScreen(homeViewModel = homeViewModel)
    }
}