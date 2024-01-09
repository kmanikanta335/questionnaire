import kotlinx.serialization.Serializable

@Serializable
data class SubQuestion(
    val id: Int,
    val ques: String,
    val type: String,
    val options: List<String>? = null
)

@Serializable
data class Question(
    val id: Int,
    val ques: String,
    val type: String,
    val sub1: SubQuestion? = null,
    val options: List<String>? = null
)

@Serializable
data class MyJsonObject(
    val questions: List<Question>
)
