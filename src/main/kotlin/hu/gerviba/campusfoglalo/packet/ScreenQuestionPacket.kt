package hu.gerviba.campusfoglalo.packet

import hu.gerviba.campusfoglalo.model.QuestionEntity

data class ScreenQuestionPacket(var question: QuestionEntity, var visible: Boolean)