package hu.gerviba.campusfoglalo.packet

import hu.gerviba.campusfoglalo.model.QuestionEntity

data class ScreenAnswerPacket(var question: QuestionEntity, var answers: Map<Int, Int>, var alreadyAnswered: String)