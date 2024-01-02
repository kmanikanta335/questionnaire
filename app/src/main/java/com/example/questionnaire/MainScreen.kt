package com.example.questionnaire

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel= viewModel()
) {
    val state = viewModel.state.value
    val context = LocalContext.current
    val dynamicText by viewModel.dynamicText.observeAsState("what is your name ?")
    val permissionState = rememberPermissionState(
        permission = Manifest.permission.RECORD_AUDIO
    )


    SideEffect {
        permissionState.launchPermissionRequest()
    }

    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = SpeechRecognizerContract(),
        onResult = {
            viewModel.changeTextValue(it.toString())
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        Text(
            text = dynamicText,
            modifier = Modifier.padding(bottom = 7.dp)
        )
        Button(
            onClick = {
                viewModel.onTranslateButtonClick(
                    text = dynamicText,
                    context = context
                )

            },
            enabled = state.isButtonEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 7.dp)
        ) {
            Text(text = "Translate")
        }


        Button(
            onClick = {
                viewModel.textToSpeech(context)
            },
            enabled = state.isButtonSpeakEnabled,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(text = "Speak")
        }
        TextField(
            value = state.text,
            onValueChange = {

            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 7.dp)
        )
        Button(onClick = {
            if (permissionState.status.isGranted) {
                speechRecognizerLauncher.launch(Unit)
            } else
                permissionState.launchPermissionRequest()
        }) {
            Text(text = "Speak")
        }
    }


}
