package hu.gerviba.campusfoglalo.model

data class QuestionEntity(
        val question: String,
        val answers: List<String>,
        val trueAnswer: Int,
        val selection: Boolean
)