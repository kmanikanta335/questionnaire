package com.example.questionnaire

import android.Manifest
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import androidx.compose.ui.geometry.Size as composeSize

@Composable
fun DropMenu(
    viewModel: MainViewModel = viewModel(),
    context: Context = LocalContext.current
){
    val q1 = "how are you ?"
    // Declaring a boolean value to store
    // the expanded state of the Text Field
    var mExpanded by remember { mutableStateOf(false) }
    val translatedQuestions by viewModel.translatedQuestions.collectAsState()



    val questions by remember { mutableStateOf(listOf("How are you?", "What is your name?","What is your monthly income ?","where do you stay ?","what is your age ?")) }
    // Create a list of cities
    val mCities = listOf<Pair<String,String>>(Pair("ENGLISH","en"), Pair("KANNADA","kn"), Pair("HINDI","hi"), Pair("TAMIL","ta"),Pair("TELUGU","te"), Pair("BENGALI","bn"), Pair("GUJURATHI","gu"))

    // Create a string value to store the selected city
    var mSelectedText by remember { mutableStateOf("") }
    val languageCode by viewModel.languageCode.observeAsState("en")

    var mTextFieldSize by remember { mutableStateOf(composeSize.Zero)}

    // Up Icon when expanded and down icon when collapsed
    val icon = if (mExpanded)
        Icons.Filled.KeyboardArrowUp
    else
        Icons.Filled.KeyboardArrowDown

    Column(Modifier.padding(20.dp)) {

        // Create an Outlined Text Field
        // with icon and not expanded
        OutlinedTextField(
            value = mSelectedText,
            onValueChange = { mSelectedText = it },
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    // This value is used to assign to
                    // the DropDown the same width
                    mTextFieldSize = coordinates.size.toSize()
                },
            label = {Text("Select Language")},
            trailingIcon = {
                Icon(icon,"contentDescription",
                    Modifier.clickable { mExpanded = !mExpanded })
            }
        )

        // Create a drop-down menu with list of cities,
        // when clicked, set the Text Field text as the city selected
        DropdownMenu(
            expanded = mExpanded,
            onDismissRequest = { mExpanded = false },
            modifier = Modifier
                .width(with(LocalDensity.current){mTextFieldSize.width.toDp()})
        ) {
            mCities.forEach { label ->
                DropdownMenuItem(
                    text={
                        Text(label.first)
                    },
                    onClick = {
                            for (question in questions) {
                                viewModel.translateQuestion(question, "en", label.second,context){
                                }
                            }

                        mSelectedText = label.first
                        viewModel.setLanguageCode(label.second)
                        viewModel.setTranslatedQuestions(emptyList())
                        mExpanded = false
                    })
            }
        }
    }
}
@Preview
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel= viewModel()
) {
    val state = viewModel.state.value
    val context = LocalContext.current


    val permissionState = rememberPermissionState(
        permission = Manifest.permission.RECORD_AUDIO
    )
    val languageCode by viewModel.languageCode.observeAsState("en")


    val translatedQuestions by viewModel.translatedQuestions.collectAsState()


    // Use a coroutine to perform translations

    SideEffect {
        permissionState.launchPermissionRequest()
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
          DropMenu()
        LazyColumn {
            items(translatedQuestions.size) {index ->
                val speechRecognizerLauncher = rememberLauncherForActivityResult(
                    contract = SpeechRecognizerContract(languageCode),
                    onResult = {
                        viewModel.changeTextValue(it.toString(),index)
                    }
                )
                val translatedQuestion = translatedQuestions.getOrNull(index)

                if (translatedQuestion != null) {
                    Text(
                        text = translatedQuestion,
                        modifier = Modifier.padding(bottom = 7.dp)
                    )

                    Button(
                        onClick = {
                            viewModel.textToSpeech(context, translatedQuestion, languageCode)
                        },
                        enabled = state.isButtonSpeakEnabled,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Text(text = "Speak")
                    }


                    TextField(
                        value = state.textFields[index] ?: "",
                        onValueChange = {
                            viewModel.updateTextField(it, index)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 7.dp)
                    )

                    Button(onClick = {
                        speechRecognizerLauncher.launch(Unit)
//                        Toast.makeText(
//                            context,
//                            textFields["What is your name?"] ?: "",
//                            Toast.LENGTH_SHORT
//                        ).show()
                    }) {
                        Text(text = "mic")
                    }
                }
            }
        }
    }
}

