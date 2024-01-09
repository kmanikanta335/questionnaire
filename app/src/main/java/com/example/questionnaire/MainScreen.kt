package com.example.questionnaire

import Question
import SubQuestion
import android.Manifest
import android.content.Context
import android.util.Size
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.InputStream
import androidx.compose.ui.geometry.Size as composeSize

data class text(var t:String,var optionIndex : Int,var SelectedTxt:String, var StedText : String)

val questionStateSaver = listSaver<text,Any>(
    save ={
        listOf(it.t,it.optionIndex,it.SelectedTxt,it.StedText)
    },
    restore = {
        text(it[0] as String, it[1] as Int,it[2] as String,it[3] as String)
    }
)
@Composable
fun DropMenu(questions: List<Question>,
    viewModel: MainViewModel = viewModel(),
    context: Context = LocalContext.current

){
    val q1 = "how are you ?"
    // Declaring a boolean value to store
    // the expanded state of the Text Field
    var mExpanded by remember { mutableStateOf(false) }
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
                        mSelectedText = label.first
                        viewModel.setLanguageCode(label.second)

                            for (question in questions) {
                                viewModel.translateQuestion(question, "en", label.second, context)
                            }
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
    val context = LocalContext.current
    val  sub1 = SubQuestion(0,"how much is your income ?","bool", listOf("YES","NO"))
    val data = Question(0,"Are you employeed ?","bool",sub1,listOf("YES","NO"))
    val string = Json.encodeToString(data)

    val myObject: Question = Json.decodeFromString<Question>(string)
    var questions: List<Question> = listOf(myObject)

    val fileName = "questionnnaire.json"
    fun assetFromAssets(context: Context, fileName: String): InputStream? {
        return context.assets.open(fileName)
    }
    // Obtain the InputStream for the file from the assets folder.
    try {
        // Attempt to obtain the InputStream for the file from the assets folder.
        val inputStream: InputStream? = assetFromAssets(context, fileName)

        // Check if the InputStream is not null before reading the content.
        if (inputStream != null) {
            val jsonString = inputStream.bufferedReader().use { it.readText() }

            // Decode the JSON string into a list of Question objects.
             questions= Json.decodeFromString(jsonString)

            // Now, you can use the 'questions' list as needed.
            questions.forEach { println(it) }
        } else {
            println("Error: InputStream is null. Make sure '$fileName' is in the correct location in the assets folder.")
        }

} catch (e: Exception) {
        println("Error: ${e.message}")
        e.printStackTrace()
    }

    val state = viewModel.state.value



    val permissionState = rememberPermissionState(
        permission = Manifest.permission.RECORD_AUDIO
    )
    val languageCode by viewModel.languageCode.observeAsState("")

//
//    LaunchedEffect(questions) {
//        for (question in questions) {
//            viewModel.translateQuestion(question, "en", languageCode, context)
//        }
//    }

    val translatedQuestions by viewModel.translatedQuestions.collectAsState()
    val translatedOptions by viewModel.translatedOptions.collectAsState()




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
          DropMenu(questions)
        Sumbit(questions)
        LazyColumn {
            itemsIndexed(questions) { index, question ->
                //                val speechRecognizerLauncher = rememberLauncherForActivityResult(
//                    contract = SpeechRecognizerContract(languageCode),
//                    onResult = {
//                        viewModel.changeTextValue(it.toString(),index)
//                    }
//                )
//                val translatedQuestion = translatedQuestions.getOrNull(index)
//
//                if (translatedQuestion != null) {
//                    Text(
//                        text = translatedQuestion,
//                        modifier = Modifier.padding(bottom = 7.dp)
//                    )
//
//                    Button(
//                        onClick = {
//                            viewModel.textToSpeech(context, translatedQuestion, languageCode)
//                        },
//                        enabled = state.isButtonSpeakEnabled,
//                        modifier = Modifier
//                            .fillMaxWidth()
//                    ) {
//                        Text(text = "Speak")
//                    }
//
//
//                    TextField(
//                        value = state.textFields[index] ?: "",
//                        onValueChange = {
//                            viewModel.updateTextField(it, index)
//                        },
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(bottom = 7.dp)
//                    )
//
//                    Button(onClick = {
//                        speechRecognizerLauncher.launch(Unit)
////                        Toast.makeText(
////                            context,
////                            textFields["What is your name?"] ?: "",
////                            Toast.LENGTH_SHORT
////                        ).show()
//                    }) {
//                        Text(text = "mic")
//                    }
//                }
                when (question.type) {
                    "bool" -> {
                        // Handle boolean type question
                        BooleanQuestion(question = question,question.id)
                    }
                    "num" -> {
                        // Handle numeric type question
                        NumericQuestion(question = question,question.id)
                    }
                    "obj" -> {
                        // Handle object type question
                        ObjectQuestion(question = question,question.id)
                    }
                }

            }

        }


    }

}
@Composable
fun Sumbit(questions: List<Question>,viewModel: MainViewModel = viewModel()){
    val languageCode by viewModel.languageCode.observeAsState("")

    val context = LocalContext.current
    Button(
        onClick = {
            viewModel.storeAnswersInEnglish(context,questions[0],languageCode)
        },
        enabled = viewModel.allQuestionsAnswered.value,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(text = "Submit")
    }
}

@Composable
fun BooleanQuestion(question: Question, index:Int, viewModel: MainViewModel = viewModel()) {
    val state = viewModel.state.value
    val context = LocalContext.current
    val languageCode by viewModel.languageCode.observeAsState("")

    val translatedQuestions by viewModel.translatedQuestions.collectAsState()
    val translatedOptions by viewModel.translatedOptions.collectAsState()
    val translatedSubQuestion by viewModel.translatedSubQuestions.collectAsState()
    val translatedSubQuestionOptions by viewModel.translatedSubOptions.collectAsState()
    val translatedQuestion = translatedQuestions[index] ?: ""
    val options = translatedOptions[index]
    val subQuestion = translatedSubQuestion[question.sub1?.id]
    val subOptions = translatedSubQuestionOptions[question.sub1?.id]
    fun androidx.compose.ui.geometry.Size.toAndroidSize(): android.util.Size {
        return android.util.Size(width.toInt(), height.toInt())
    }

    var optionIndex by rememberSaveable(stateSaver = questionStateSaver) { mutableStateOf(text("",2,"","")) }

    if(translatedQuestion!="") {


        var expanded by remember { mutableStateOf(false) }

        val selectedOptionIndex = remember { mutableStateOf(0) }
        val icon = if (expanded)
            Icons.Filled.KeyboardArrowUp
        else
            Icons.Filled.KeyboardArrowDown
        var TextFieldSize by remember { mutableStateOf(composeSize(100f, 50f)) }

        var SelectedText by rememberSaveable(stateSaver = questionStateSaver) {
            mutableStateOf(
                text(
                    "",
                    2,
                    "",
                    ""
                )
            )
        }
        OutlinedTextField(

            value = SelectedText.t,
            onValueChange = { SelectedText.t = it },
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    // This value is used to assign to
                    // the DropDown the same width
                    TextFieldSize = coordinates.size.toSize()
                },
            label = {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(translatedQuestion)
                    IconButton(
                        onClick = {
                            viewModel.textToSpeech(context, translatedQuestion, languageCode)
                        },
                        content = {
                            Icon(
                                painter = painterResource(R.drawable.speaker),
                                contentDescription = "Speaker Icon",
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        enabled = state.isButtonSpeakEnabled,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }
                    },
            trailingIcon = {
                Icon(icon, "contentDescription",
                    Modifier.clickable { expanded = !expanded })
            },

        )

        // Create a drop-down menu with list of cities,
        // when clicked, set the Text Field text as the city selected
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .width(with(LocalDensity.current) { TextFieldSize.width.toDp() })
        ) {
            if (options != null) {
                options.forEachIndexed { index, option ->
                    DropdownMenuItem({
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Display the option text
                            Text(text = option)

                            // Add an IconButton for text-to-speech
                            IconButton(
                                onClick = {
                                    viewModel.textToSpeech(context, option, languageCode)
                                },
                                content = {
                                    Icon(
                                        painter = painterResource(R.drawable.speaker),
                                        contentDescription = "Speaker Icon",
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                enabled = state.isButtonSpeakEnabled,
                            )
                        }
                    },
                        onClick = {
                            SelectedText.t = option
                            selectedOptionIndex.value = index
                            optionIndex = text(option, index, "", "")
                            viewModel.updateTextField(option, question.id)
                            expanded = false
                        }

                    )
                }
            }
        }
    }

    if (question.sub1 != null && optionIndex.optionIndex==0) {
        // Handle sub-question for boolean type
        when (question.sub1.type) {
            "num" -> {
                val state = viewModel.state.value

                if (subQuestion!= null) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = subQuestion,
                            modifier = Modifier.padding(bottom = 7.dp)
                        )
                        IconButton(
                            onClick = {
                                viewModel.textToSpeech(context, subQuestion, languageCode)
                            },
                            content = {
                                Icon(
                                    painter = painterResource(R.drawable.speaker),
                                    contentDescription = "Speaker Icon",
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            enabled = state.isButtonSpeakEnabled,
                            modifier = Modifier
                                .fillMaxWidth()
                        )
                    }


                    TextField(
                        value = state.textFields[question.sub1.id] ?: "",
                        onValueChange = {
                            viewModel.updateTextField(it, question.sub1.id)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 7.dp)
                    )
                }
            }
            "obj" -> {
                if(subQuestion!="") {
                    var Exp by  remember { mutableStateOf(false) }

                    val selOptionIndex = remember { mutableStateOf(0) }
                    val icon = if (Exp)
                        Icons.Filled.KeyboardArrowUp
                    else
                        Icons.Filled.KeyboardArrowDown
                    var TxtFieldSize by remember { mutableStateOf(composeSize.Zero)}

                    var SelectedTxt by rememberSaveable(stateSaver = questionStateSaver) { mutableStateOf(text("",2,"","")) }

                    OutlinedTextField(
                        value = SelectedTxt.SelectedTxt,
                        onValueChange = { SelectedTxt.SelectedTxt = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .onGloballyPositioned { coordinates ->
                                // This value is used to assign to
                                // the DropDown the same width
                                TxtFieldSize = coordinates.size.toSize()
                            },
                        label = {

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (subQuestion != null) {
                                    Text(subQuestion)

                                    IconButton(
                                        onClick = {
                                            viewModel.textToSpeech(
                                                context,
                                                subQuestion,
                                                languageCode
                                            )
                                        },
                                        content = {
                                            Icon(
                                                painter = painterResource(R.drawable.speaker),
                                                contentDescription = "Speaker Icon",
                                                modifier = Modifier.size(20.dp)
                                            )
                                        },
                                        enabled = state.isButtonSpeakEnabled,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                    )
                                }
                            }
                        },
                        trailingIcon = {
                            Icon(icon,"contentDescription",
                                Modifier.clickable { Exp = !Exp })
                        }
                    )

                    // Create a drop-down menu with list of cities,
                    // when clicked, set the Text Field text as the city selected

                        DropdownMenu(
                            expanded = Exp,
                            onDismissRequest = { Exp = false },
                            modifier = Modifier
                                .width(with(LocalDensity.current) { TxtFieldSize.width.toDp() })
                        ) {
                            if (subOptions != null) {
                                subOptions.forEachIndexed { index, option ->
                                    DropdownMenuItem({
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.Start,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Display the option text
                                            Text(text = option)

                                            // Add an IconButton for text-to-speech
                                            IconButton(
                                                onClick = {
                                                    viewModel.textToSpeech(
                                                        context,
                                                        option,
                                                        languageCode
                                                    )
                                                },
                                                content = {
                                                    Icon(
                                                        painter = painterResource(R.drawable.speaker),
                                                        contentDescription = "Speaker Icon",
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                },
                                                enabled = state.isButtonSpeakEnabled,
                                            )
                                        }
                                    },
                                        onClick = {
                                            SelectedTxt.SelectedTxt = option
                                            selOptionIndex.value = index
                                            viewModel.updateTextField(option, question.sub1.id)
                                            Exp = false
                                        }
                                    )
                                }
                            }
                        }
                }
            }
        }
    }
}


@Composable
fun NumericQuestion(question: Question,index:Int,viewModel: MainViewModel = viewModel()) {
    val state = viewModel.state.value
    val languageCode by viewModel.languageCode.observeAsState("en")
    val context = LocalContext.current
    val translatedQuestions by viewModel.translatedQuestions.collectAsState()
    val translatedOptions by viewModel.translatedOptions.collectAsState()
    val translatedQuestion = translatedQuestions[index]

    if (translatedQuestion != null) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = translatedQuestion,
                modifier = Modifier.padding(bottom = 7.dp)
            )
            IconButton(
                onClick = {
                    viewModel.textToSpeech(context, translatedQuestion, languageCode)
                },
                content = {
                    Icon(
                        painter = painterResource(R.drawable.speaker),
                        contentDescription = "Speaker Icon",
                        modifier = Modifier.size(20.dp)
                    )
                },
                enabled = state.isButtonSpeakEnabled,
                modifier = Modifier
                    .fillMaxWidth()
            )
        }


        TextField(
            value = state.textFields[question.id] ?: "",
            onValueChange = {
                viewModel.updateTextField(it, question.id)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 7.dp)
        )

    }
}

@Composable
fun ObjectQuestion(question: Question, index:Int, viewModel: MainViewModel = viewModel()) {
    val state = viewModel.state.value
    val languageCode by viewModel.languageCode.observeAsState("en")
    val context = LocalContext.current

    val translatedQuestions by viewModel.translatedQuestions.collectAsState()
    val translatedOptions by viewModel.translatedOptions.collectAsState()
    val translatedQuestion = translatedQuestions[index] ?: ""

    if(translatedQuestion!="") {
        var expand by  remember { mutableStateOf(false) }

        val selecOptionIndex = remember { mutableIntStateOf(0) }
        val icon = if (expand)
            Icons.Filled.KeyboardArrowUp
        else
            Icons.Filled.KeyboardArrowDown
        var TtFieldSize by remember { mutableStateOf(composeSize.Zero)}

        var StedText by rememberSaveable(stateSaver = questionStateSaver) { mutableStateOf(text("",2,"","")) }

        OutlinedTextField(
            value = StedText.StedText,
            onValueChange = { StedText.StedText = it },
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    // This value is used to assign to
                    // the DropDown the same width
                    TtFieldSize = coordinates.size.toSize()
                },
            label = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(translatedQuestion)
                    IconButton(
                        onClick = {
                            viewModel.textToSpeech(context, translatedQuestion, languageCode)
                        },
                        content = {
                            Icon(
                                painter = painterResource(R.drawable.speaker),
                                contentDescription = "Speaker Icon",
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        enabled = state.isButtonSpeakEnabled,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }
                    },
            trailingIcon = {
                Icon(icon,"contentDescription",
                    Modifier.clickable { expand = !expand })
            }
        )

        // Create a drop-down menu with list of cities,
        // when clicked, set the Text Field text as the city selected
        DropdownMenu(
            expanded = expand,
            onDismissRequest = { expand = false },
            modifier = Modifier
                .width(with(LocalDensity.current){TtFieldSize.width.toDp()})
        ) {
            translatedOptions[index]?.forEachIndexed { index, option ->
                DropdownMenuItem({
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Display the option text
                        Text(text = option)

                        // Add an IconButton for text-to-speech
                        IconButton(
                            onClick = {
                                viewModel.textToSpeech(context, option, languageCode)
                            },
                            content = {
                                Icon(
                                    painter = painterResource(R.drawable.speaker),
                                    contentDescription = "Speaker Icon",
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            enabled = state.isButtonSpeakEnabled,
                        )
                    }
                },
                    onClick = {
                        StedText.StedText = option
                        selecOptionIndex.value = index
                        viewModel.updateTextField(option, question.id)
                        expand = false
                    }
                )
            }
        }

    }

}


