package com.example.questionnaire

import android.content.Context
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.launch
import java.util.*

class MainViewModel:ViewModel() {

    private val _state = mutableStateOf(MainScreenState())
    var state: State<MainScreenState> = _state
    private  var  textToSpeech: TextToSpeech? = null
    private val _dynamicText = MutableLiveData<String>()
    val dynamicText: LiveData<String> get() = _dynamicText
//    fun onTextToBeTranslatedChange(text: String) {
//        _state.value = state.value.copy(
//            textToBeTranslated = text
//        )
//    }



    fun changeTextValue(text:String){
        viewModelScope.launch {
            _state.value = state.value.copy(
                text = text
            )
        }
    }
    fun onTranslateButtonClick(
        text: String,
        context: Context
    ) {

        val options = TranslatorOptions
            .Builder()
            .setSourceLanguage(TranslateLanguage.ENGLISH)
            .setTargetLanguage(TranslateLanguage.KANNADA)
            .build()

        val languageTranslator = Translation
            .getClient(options)

        languageTranslator.translate(text)
            .addOnSuccessListener { translatedText ->
//                _state.value = state.value.copy(
//                    translatedText = translatedText
//                )
                _dynamicText.value = translatedText
//
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

    fun textToSpeech(context: Context){

        _state.value = state.value.copy(
            isButtonSpeakEnabled = false
        )
        textToSpeech = TextToSpeech(
            context
        ) {
            if (it == TextToSpeech.SUCCESS) {
                textToSpeech?.let { txtToSpeech ->
                    txtToSpeech.language = Locale("kn","IN")
                    txtToSpeech.setSpeechRate(1.0f)
                    txtToSpeech.speak(
                        dynamicText.value,
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