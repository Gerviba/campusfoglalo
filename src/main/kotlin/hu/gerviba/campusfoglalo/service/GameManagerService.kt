package hu.gerviba.campusfoglalo.service

import hu.gerviba.campusfoglalo.model.QuestionEntity
import hu.gerviba.campusfoglalo.model.UserEntity
import hu.gerviba.campusfoglalo.packet.PlaceStatus
import hu.gerviba.campusfoglalo.packet.ScreenStatusPacket
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.annotation.PostConstruct

@Service
class GameManagerService {

    @Autowired
    lateinit var outgoing: SimpMessagingTemplate

    val userStorage = ConcurrentHashMap<String, UserEntity>()

    var gameUuid: String = ""
    var placeStates: Map<String, PlaceStatus> = mapOf()
    var activeTeam: Int = 0
    var currentQuestion: QuestionEntity? = null
    var givenAnswers: MutableMap<Int, Int> = mutableMapOf()

    @PostConstruct
    fun init() {

    }

    fun getOrCreateUser(sessionId: String): UserEntity {
        return userStorage.computeIfAbsent(sessionId) {
            session -> UserEntity(session)
        }
    }

    fun getUser(sessionId: String): UserEntity {
        return if (userStorage.containsKey(sessionId))
            userStorage[sessionId]!!
        else
            throw RuntimeException("User `$sessionId` was not found!")
    }

    fun isUuidValid(uuid: String) = uuid == gameUuid

    fun getAll() = userStorage.values

    fun startGame(): String {
        gameUuid = UUID.randomUUID().toString()
        givenAnswers = mutableMapOf()
        placeStates = mapOf(
                "1_DelELTE"             to PlaceStatus(0, false, false),
                "2_Tuskecsarnok"        to PlaceStatus(0, false, false),
                "3_IQ"                  to PlaceStatus(0, false, false),
                "4_EszakELTE"           to PlaceStatus(0, false, false),
                "5_Goldmann"            to PlaceStatus(0, false, false),
                "6_Karman"              to PlaceStatus(0, false, false),
                "7_E"                   to PlaceStatus(0, false, false),
                "8_R"                   to PlaceStatus(0, false, false),
                "9_K"                   to PlaceStatus(0, false, false),
                "10_CHMAX"              to PlaceStatus(0, false, false),
                "11_Gellert"            to PlaceStatus(0, false, false),
                "12_Moricz"             to PlaceStatus(0, false, false),
                "13_Gardonyi"           to PlaceStatus(0, false, false),
                "14_Sarki"              to PlaceStatus(0, false, false),
                "15_Allee"              to PlaceStatus(0, false, false),
                "16_MOL"                to PlaceStatus(0, false, false),
                "17_Schonherz"          to PlaceStatus(0, false, false),
                "18_Pinyo"              to PlaceStatus(0, false, false),
                "19_Boraros"            to PlaceStatus(0, false, false),
                "20_Corvinus"           to PlaceStatus(0, false, false))

        return gameUuid
    }

    fun answerQuestion(user: UserEntity, answer: Int) {
        givenAnswers[user.teamId] = answer
    }

    fun placeNames() = placeStates.keys

    fun setOwner(place: String, owner: Int) {
        placeStates[place]!!.owner = owner
        sendMapUpdate()
    }

    fun setTower(place: String, tower: Boolean) {
        placeStates[place]!!.tower = tower
        sendMapUpdate()
    }

    fun setSeleczed(place: String, selected: Boolean) {
        for (placeState in placeStates) {
            placeState.value.selected = false
        }
        placeStates[place]!!.selected = selected

        sendMapUpdate()
    }

    fun sendMapUpdate() {

        outgoing.convertAndSend("/status", ScreenStatusPacket(
                users = mapOf(

                )
                places = placeStates
        ))
    }

}