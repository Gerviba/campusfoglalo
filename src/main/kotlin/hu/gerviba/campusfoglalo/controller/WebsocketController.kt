package hu.gerviba.campusfoglalo.controller

import hu.gerviba.campusfoglalo.packet.PlayerAnswerPacket
import hu.gerviba.campusfoglalo.service.GameManagerService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.messaging.simp.annotation.SendToUser
import org.springframework.stereotype.Controller

@Controller
class WebsocketController {

    @Autowired
    lateinit var game: GameManagerService

    @MessageMapping("/answer")
    fun userAnswered(answer: PlayerAnswerPacket, @Header("simpSessionId") sessionId: String) {
        val user = game.getUser(sessionId)
        game.answerQuestion(user, answer.answer)
    }

}