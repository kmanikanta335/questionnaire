package com.example.questionnaire

import android.content.Context
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.*
import java.util.Locale.ENGLISH

class MainViewModel:ViewModel() {

    private val _state = mutableStateOf(MainScreenState())
    var state: State<MainScreenState> = _state
    private  var  textToSpeech: TextToSpeech? = null
    private val _dynamicText = MutableLiveData<String>()
    val dynamicText: LiveData<String> get() = _dynamicText
    private val _languageCode = MutableLiveData<String>()
    val languageCode: LiveData<String> get() = _languageCode

    private var _translatedQuestions = MutableStateFlow<List<String>>(emptyList())
    val translatedQuestions: StateFlow<List<String>> get() = _translatedQuestions

    fun addTranslatedQuestion(question: String) {
        _translatedQuestions.value = _translatedQuestions.value + question
    }
    fun setTranslatedQuestions(questions: List<String>) {
        _translatedQuestions.value = questions
    }
    // Example function to update a text field
    fun setLanguageCode(code: String) {
        _languageCode.value = code
    }
//    fun onTextToBeTranslatedChange(text: String) {
//        _state.value = state.value.copy(
//            textToBeTranslated = text
//        )
//    }



    //val textFields: MutableList<String> = MutableList(translatedQuestions.value.size) { "" }

    // Function to change the text value for a specific index
    fun changeTextValue(text: String, index: Int) {
        _state.value = state.value.copy(
            textFields = state.value.textFields.toMutableList().apply {
                set(index, text)
            }
        )
    }
    // Function to update the text in a specific TextField
    fun updateTextField(text: String, index: Int) {
        _state.value = state.value.copy(
            textFields = state.value.textFields.toMutableList().apply {
                set(index, text)
            }
        )
    }
    fun translateQuestion(
        question: String,
        language: String,
        targetLanguage: String,
        context: Context,
        callback: (String) -> Unit
    ) {

        setLanguageCode(language)
        val options = TranslatorOptions
            .Builder()
            .setSourceLanguage(language)
            .setTargetLanguage(targetLanguage)
            .build()

        val languageTranslator = Translation
            .getClient(options)

        languageTranslator.translate(question)
            .addOnSuccessListener { translatedText ->
                addTranslatedQuestion(translatedText)
                callback(translatedText)

            }
            .addOnFailureListener {
                Toast.makeText(
                    context,
                    "Downloading started..",
                    Toast.LENGTH_SHORT
                ).show()
                downloadModelIfNotAvailable(languageTranslator, context)
            }
    }


    private fun downloadModelIfNotAvailable(
        languageTranslator: Translator,
        context: Context
    ) {
        _state.value = state.value.copy(
            isButtonEnabled = false
        )

        val conditions = DownloadConditions
            .Builder()
            .requireWifi()
            .build()


        languageTranslator
            .downloadModelIfNeeded(conditions)
            .addOnSuccessListener {
                Toast.makeText(
                    context,
                    "Downloaded model successfully..",
                    Toast.LENGTH_SHORT
                ).show()

                _state.value = state.value.copy(
                    isButtonEnabled = true
                )
            }
            .addOnFailureListener {
                Toast.makeText(
                    context,
                    "Some error occurred couldn't download language model..",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
//    fun onTextFieldValueChange(text:String){
//        _state.value = state.value.copy(
//            text = text
//        )
//    }

    fun textToSpeech(context: Context, question: String,languageCode:String){

        _state.value = state.value.copy(
            isButtonSpeakEnabled = false
        )
        textToSpeech = TextToSpeech(
            context
        ) {
            if (it == TextToSpeech.SUCCESS) {
                textToSpeech?.let { txtToSpeech ->
                    txtToSpeech.language = Locale(languageCode,"IN")
                    txtToSpeech.setSpeechRate(1.0f)
                    txtToSpeech.speak(
                        question,
                        TextToSpeech.QUEUE_ADD,
                        null,
                        null
                    )
                }
            }
            _state.value = state.value.copy(
                isButtonSpeakEnabled = true
            )
        }
    }
}