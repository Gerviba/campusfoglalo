package hu.gerviba.campusfoglalo.config

import hu.gerviba.campusfoglalo.service.GameManagerService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.event.EventListener
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.GenericMessage
import org.springframework.stereotype.Component
import org.springframework.web.socket.messaging.SessionConnectEvent
import org.springframework.web.socket.messaging.SessionConnectedEvent
import org.springframework.web.socket.messaging.SessionDisconnectEvent
import org.springframework.web.socket.messaging.SessionSubscribeEvent

@Component
class WebsocketEventListener {

    @Autowired
    lateinit var game: GameManagerService

    @EventListener
    fun handleConnect(event: SessionConnectEvent) {
        println("Connected: ${event.message}")
    }

    @EventListener
    fun handleConnected(event: SessionConnectedEvent) {
        val headerAccessor = StompHeaderAccessor.wrap(event.message)
        println("Connected: ${event.message}")

        val username = decodeNativeHeader(headerAccessor, "username")
        val teamId = decodeNativeHeader(headerAccessor, "teamId")

        if (username == null || teamId == null || teamId == "screen") {
            val user = game.getOrCreateUser(headerAccessor.sessionId!!)
            user.screen = true
            user.teamId = 0
            println("New screen joined")
        } else {
            val user = game.getOrCreateUser(headerAccessor.sessionId!!)
            user.name = username
            user.screen = false
            user.teamId = teamId.toInt()
            println("Received a new web socket connection by user: '${headerAccessor.sessionId}' -> ${user.name}")
        }
    }

    @EventListener
    fun handleDisconnect(event: SessionDisconnectEvent) {
        val headerAccessor = StompHeaderAccessor.wrap(event.message)
        println("Disconnected user with sessionId: $headerAccessor.sessionId")
    }

    @EventListener
    fun handleSubscribe(event: SessionSubscribeEvent) {
        println("Subscribe: $event.message")
    }

    fun decodeNativeHeader(headerAccessor: StompHeaderAccessor, header: String): String? {
        return try {
            val nativeHeaders = decodeNativeHeaders(headerAccessor)
            nativeHeaders.get(header)?.get(0)
        } catch (e: Exception) {
            null
        }
    }

    fun decodeNativeHeaders(headerAccessor: StompHeaderAccessor): Map<String, List<String>> {
        val simpConnectMessage = headerAccessor.messageHeaders["simpConnectMessage"] as GenericMessage<Object>?
        checkNotNull(simpConnectMessage)
        val nativeHeaders = simpConnectMessage.headers["nativeHeaders"] as Map<String, List<String>>?
        checkNotNull( nativeHeaders)
        return nativeHeaders
    }

}
