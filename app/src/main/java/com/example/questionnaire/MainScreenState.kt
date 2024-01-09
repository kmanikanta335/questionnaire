package com.example.questionnaire

data class MainScreenState(
    val isButtonSpeakEnabled:Boolean = true,
    val translatedText:String = "",
    val isButtonEnabled:Boolean = true,
    val text:String = "",
    val textMap: Map<String, String> = emptyMap(),
    val textFields: MutableList<String> = MutableList(12) { "" }
)