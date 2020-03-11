package hu.gerviba.campusfoglalo.packet

data class PlayerQuestionPacket(var question: String, var selection: Boolean, var answers: List<String>)