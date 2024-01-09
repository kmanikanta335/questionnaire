package com.example.questionnaire

import Question
import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
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
import com.google.android.gms.tasks.Task
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.*
import java.util.Locale.ENGLISH
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException



class MainViewModel:ViewModel() {

    private val _state = mutableStateOf(MainScreenState())
    var state: State<MainScreenState> = _state
    private  var  textToSpeech: TextToSpeech? = null
    private val _dynamicText = MutableLiveData<String>()
    val dynamicText: LiveData<String> get() = _dynamicText
    private val _languageCode = MutableLiveData<String>()
    val languageCode: LiveData<String> get() = _languageCode

    private var _translatedQuestions = MutableStateFlow<Map<Int, String>>(emptyMap())
    val translatedQuestions: StateFlow<Map<Int, String>> get() = _translatedQuestions

    private var _translatedSubQuestions = MutableStateFlow<Map<Int, String>>(emptyMap())
    val translatedSubQuestions: StateFlow<Map<Int, String>> get() = _translatedSubQuestions

    private val _allQuestionsAnswered = mutableStateOf(false)
    val allQuestionsAnswered: State<Boolean> = _allQuestionsAnswered

    // Existing code...

    fun addTranslatedQuestion(questionId: Int, translatedText: String) {
        _translatedQuestions.value = _translatedQuestions.value + (questionId to translatedText)
    }

    fun addTranslatedSubQuestion(subQuestionId: Int, translatedText: String) {
        _translatedSubQuestions.value = _translatedSubQuestions.value + (subQuestionId to translatedText)
    }

    private var _translatedOptions = MutableStateFlow<Map<Int, List<String>>>(emptyMap())
    val translatedOptions: StateFlow<Map<Int, List<String>>> get() = _translatedOptions
    fun addTranslatedOptions(questionId: Int, options: List<String>?) {
        val currentMap = _translatedOptions.value.toMutableMap()
        currentMap[questionId] = options ?: emptyList()
        _translatedOptions.value = currentMap
    }
    private var _translatedSubOptions = MutableStateFlow<Map<Int, List<String>>>(emptyMap())
    val translatedSubOptions: StateFlow<Map<Int, List<String>>> get() = _translatedSubOptions

    fun addTranslatedSubQuestionOptions(id:Int,options: List<String>?){
        val currentMap = _translatedSubOptions.value.toMutableMap()
        currentMap[id] = options ?: emptyList()
        _translatedSubOptions.value = currentMap
    }

    fun setTranslatedQuestions(questions: List<String>) {
        _translatedQuestions.value = emptyMap()
    }
    // Example function to update a text field
    fun setLanguageCode(code: String) {
        viewModelScope.launch(Dispatchers.Main) {
            _languageCode.value = code
        }
    }
//    fun onTextToBeTranslatedChange(text: String) {
//        _state.value = state.value.copy(
//            textToBeTranslated = text
//        )
//    }
var resultMap: Map<Int,String> = emptyMap()
    fun storeAnswersInEnglish(context: Context,question: Question,languageCode: String) {
        // Iterate through questions and translate answers to English
        // Update your logic to store answers in English


             translateAndAdd(context, languageCode,"en",question,"else" )
        resultMap.forEach { (key, value) ->
            // Access 'key' and 'value' for each element in the map
            println("Key: $key, Value: $value")
            Toast.makeText(
                context,
                "Key: $key, Value: $value",
                Toast.LENGTH_SHORT
            ).show()

        }

    }
    private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { result -> continuation.resume(result) }
        addOnFailureListener { exception -> continuation.resumeWithException(exception) }
    }

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
        if(index >=11){
            _allQuestionsAnswered.value = true

        }
    }
     fun translateQuestion(
        question: Question,
        language: String,
        targetLanguage: String,
        context: Context
    ) {
        setLanguageCode(targetLanguage)

                // Translate boolean question and options (e.g., "Yes" and "No")
              translateAndAdd(context,language,targetLanguage,question,"ques")

                question.options?.let { options ->
                        translateAndAdd(context,language,targetLanguage,question,"option")
                }

              question.sub1?.let{
                  translateAndAdd(context,language,targetLanguage,question,"sub")
              }

              question.sub1?.options?.let {
                  translateAndAdd(context,language,targetLanguage,question,"subOption")
              }

            // Handle other types as needed

    }

    private fun translateAndAdd(context: Context, language: String, targetLanguage: String,question: Question,type:String): String {
        setLanguageCode(targetLanguage)
        val options = TranslatorOptions
            .Builder()
            .setSourceLanguage(language)
            .setTargetLanguage(targetLanguage)
            .build()

        val languageTranslator = Translation
            .getClient(options)
        var t = question.ques
        when(type) {
            "ques"-> {
                languageTranslator.translate(question.ques)
                    .addOnSuccessListener { translatedText ->
                        t = translatedText
                        addTranslatedQuestion(question.id,translatedText)
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
            "option"->{
                val translatedOptionsList = mutableListOf<String>()

                for(option in question.options!!){
                    languageTranslator.translate(option)
                        .addOnSuccessListener { translatedText ->
                            t = translatedText
                           translatedOptionsList.add(translatedText)
                            println(translatedText)
                            if (translatedOptionsList.size == question.options.size) {
                                addTranslatedOptions(question.id, translatedOptionsList)
                            }
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
            }
            "sub"->{
                question.sub1?.let {
                    languageTranslator.translate(it.ques)
                        .addOnSuccessListener { translatedText ->
                            t = translatedText
                            addTranslatedSubQuestion(it.id,translatedText)
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
            }
            "subOption"->{
                val translatedOptionsList = mutableListOf<String>()

                for(option in question.sub1?.options!!){
                    languageTranslator.translate(option)
                        .addOnSuccessListener { translatedText ->
                            t = translatedText
                            translatedOptionsList.add(translatedText)

                            if (translatedOptionsList.size == question.sub1.options.size) {
                                addTranslatedSubQuestionOptions(question.sub1.id, translatedOptionsList)
                            }
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
            }
            else ->{
                state.value.textFields.forEachIndexed { index, s ->
                    languageTranslator.translate(s)
                        .addOnSuccessListener { translatedText ->
                            t = translatedText
                          resultMap = resultMap+(index to translatedText)
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
            }
        }
        return t
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