package hu.gerviba.campusfoglalo.service

import hu.gerviba.campusfoglalo.model.QuestionEntity
import hu.gerviba.campusfoglalo.model.UserEntity
import hu.gerviba.campusfoglalo.packet.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.messaging.simp.SimpMessageHeaderAccessor
import org.springframework.messaging.simp.SimpMessageType
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.annotation.PostConstruct

@Service
class GameManagerService {

    @Autowired
    lateinit var outgoing: SimpMessagingTemplate

    @Value("\${app.root-dir:.}")
    lateinit var rootDir: String

    private lateinit var numQuestions: List<QuestionEntity>
    private lateinit var selQuestions: List<QuestionEntity>

    val userStorage = ConcurrentHashMap<String, UserEntity>()

    var numQueue: MutableList<QuestionEntity> = mutableListOf()
    var selQueue: MutableList<QuestionEntity> = mutableListOf()
    var lastQuestion: QuestionEntity = QuestionEntity("", listOf(), 0, false)

    var gameUuid: String = ""
    var order: String = ""
    var placeStates: Map<String, PlaceStatus> = mapOf()
    var activeTeam: Int = 0
    var givenAnswers: MutableMap<Int, Int> = mutableMapOf()
    var teamScores: MutableMap<Int, TeamInfo> = mutableMapOf()

    data class TeamInfo(var name: String, var score: Int, var teamId: Int)

    @PostConstruct
    fun init() {
        selQuestions = Files.readAllLines(Path.of("${rootDir}/questions-sel.csv"), StandardCharsets.UTF_8)
                .map { it.split(";") }
                .filter { it.size == 6 }
                .map {
                    QuestionEntity(
                            question = it[0],
                            answers = listOf(it[1], it[2], it[3], it[4]),
                            trueAnswer = it[5].toInt(),
                            selection = true
                    )
                }

        numQuestions = Files.readAllLines(Path.of("${rootDir}/questions-num.csv"), StandardCharsets.UTF_8)
                .map { it.split(";") }
                .filter { it.size == 2 }
                .map {
                    QuestionEntity(
                            question = it[0],
                            answers = listOf(),
                            trueAnswer = it[1].toInt(),
                            selection = false
                    )
                }
    }

    fun getOrCreateUser(sessionId: String): UserEntity {
        return userStorage.computeIfAbsent(sessionId) { session ->
            UserEntity(session)
        }
    }

    fun registerPlayer(player: UserEntity) {
        teamScores.putIfAbsent(player.teamId, TeamInfo(player.name, 0, player.teamId))
        sendMapUpdate()
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
        teamScores = mutableMapOf()
        numQueue = numQuestions.toMutableList()
        selQueue = selQuestions.toMutableList()
        placeStates = mapOf(
                "1_DelELTE" to PlaceStatus(0, false, false),
                "2_Tuskecsarnok" to PlaceStatus(0, false, false),
                "3_IQ" to PlaceStatus(0, false, false),
                "4_EszakELTE" to PlaceStatus(0, false, false),
                "5_Goldmann" to PlaceStatus(0, false, false),
                "6_Karman" to PlaceStatus(0, false, false),
                "7_E" to PlaceStatus(0, false, false),
                "8_R" to PlaceStatus(0, false, false),
                "9_K" to PlaceStatus(0, false, false),
                "10_CHMAX" to PlaceStatus(0, false, false),
                "11_Gellert" to PlaceStatus(0, false, false),
                "12_Moricz" to PlaceStatus(0, false, false),
                "13_Gardonyi" to PlaceStatus(0, false, false),
                "14_Sarki" to PlaceStatus(0, false, false),
                "15_Allee" to PlaceStatus(0, false, false),
                "16_MOL" to PlaceStatus(0, false, false),
                "17_Schonherz" to PlaceStatus(0, false, false),
                "18_Pinyo" to PlaceStatus(0, false, false),
                "19_Boraros" to PlaceStatus(0, false, false),
                "20_Corvinus" to PlaceStatus(0, false, false))

        return gameUuid
    }

    fun answerQuestion(user: UserEntity, answer: Int) {
        givenAnswers[user.teamId] = answer
        sendQuestionForScreen()
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

    fun setSelected(place: String, selected: Boolean) {
        for (placeState in placeStates)
            placeState.value.selected = false
        placeStates[place]!!.selected = selected

        sendMapUpdate()
    }

    fun setAttackOrder(attackOrder: String) {
        order = attackOrder
        sendMapUpdate()
    }

    fun setSelectedTeam(selected: Int) {
        activeTeam = selected
    }

    fun sendMapUpdate() {
        for (teamScore in teamScores)
            teamScore.value.score = placeStates.asSequence()
                    .filter { it.value.owner == teamScore.value.teamId }
                    .count()

        outgoing.convertAndSend("/topic/status", ScreenStatusPacket(
                users = teamScores,
                places = placeStates,
                activeTeam = if (teamScores.containsKey(activeTeam)) teamScores[activeTeam] else null,
                attackOrder = order
        ))
    }

    private fun isSelQuestionAvailable() = selQueue.size >= 2

    private fun isNumQuestionAvailable() = numQueue.size >= 2

    fun sendNewSelQuestion(forTeams: List<Int>) {
        if (!isSelQuestionAvailable())
            return

        initNewQuestion()
        selQueue.removeAt(0)
        lastQuestion = selQueue[0]

        outgoing.convertAndSend("/topic/question", ScreenQuestionPacket(selQueue[0], visible = true, alreadyAnswered = ""))

        val playerQuestionPacket = PlayerQuestionPacket(
                selQueue[0].question,
                selQueue[0].selection,
                selQueue[0].answers
        )
        userStorage.asSequence()
                .filter { it.value.teamId in forTeams }
                .forEach {
                    val headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE)
                    with(headerAccessor) {
                        sessionId = it.value.sessionId
                        setLeaveMutable(true)
                    }
                    outgoing.convertAndSendToUser(it.value.sessionId, "/topic/question", playerQuestionPacket, headerAccessor.messageHeaders)
                }

    }

    fun sendNewNumQuestion(forTeams: List<Int>) {
        if (!isNumQuestionAvailable())
            return

        initNewQuestion()
        numQueue.removeAt(0)
        lastQuestion = numQueue[0]

        outgoing.convertAndSend("/topic/question", ScreenQuestionPacket(numQueue[0], visible = true, alreadyAnswered = ""))

        val playerQuestionPacket = PlayerQuestionPacket(
                numQueue[0].question,
                numQueue[0].selection,
                numQueue[0].answers
        )
        userStorage.asSequence()
                .filter { it.value.teamId in forTeams }
                .forEach {
                    val headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE)
                    with(headerAccessor) {
                        sessionId = it.value.sessionId
                        setLeaveMutable(true)
                    }
                    outgoing.convertAndSendToUser(it.value.sessionId, "/topic/question", playerQuestionPacket, headerAccessor.messageHeaders)
                }
    }

    fun sendShowAnswer() {
        hideAll()
        outgoing.convertAndSend("/topic/answer", ScreenAnswerPacket(lastQuestion, givenAnswers, alreadyAnswered = fetchWhoAnswered()))
    }

    fun sendHideQuestion() {
        hideAll()
        outgoing.convertAndSend("/topic/question", ScreenQuestionPacket(lastQuestion, visible = false, alreadyAnswered = ""))
    }

    private fun initNewQuestion() {
        givenAnswers = mutableMapOf()
    }

    private fun hideAll() {
        for (user in userStorage) {
            val headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE)
            with(headerAccessor) {
                sessionId = user.value.sessionId
                setLeaveMutable(true)
            }
            outgoing.convertAndSendToUser(user.value.sessionId, "/topic/hide", PlayerHidePacket(), headerAccessor.messageHeaders)
        }
    }

    fun resendQuestion(forTeams: List<Int>) {
        sendQuestionForScreen()

        val playerQuestionPacket = PlayerQuestionPacket(
                numQueue[0].question,
                numQueue[0].selection,
                numQueue[0].answers
        )
        userStorage.asSequence()
                .filter { it.value.teamId in forTeams }
                .forEach {
                    val headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE)
                    with(headerAccessor) {
                        sessionId = it.value.sessionId
                        setLeaveMutable(true)
                    }
                    outgoing.convertAndSendToUser(it.value.sessionId, "/topic/question", playerQuestionPacket, headerAccessor.messageHeaders)
                }
    }

    private fun sendQuestionForScreen() {
        outgoing.convertAndSend(
            "/topic/question",
            ScreenQuestionPacket(lastQuestion, visible = true, alreadyAnswered = fetchWhoAnswered())
        )
    }

    private fun fetchWhoAnswered(): String {
        return givenAnswers.keys.map { teamScores[it]?.name ?: "n/a" }.joinToString()
    }


}
