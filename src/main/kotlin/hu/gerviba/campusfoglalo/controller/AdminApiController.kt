package hu.gerviba.campusfoglalo.controller

import hu.gerviba.campusfoglalo.service.GameManagerService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

data class NewQuestionDto (var uuid: String, var forTeams: List<Int>)
data class SetOwnerDto (var uuid: String, var place: String, var owner: Int)
data class SetSelectedDto (var uuid: String, var place: String, var selected: String)
data class SetTowerDto (var uuid: String, var place: String, var tower: String)
data class SetTeamSelectedDto (var uuid: String, var selected: Int)
data class OnlyUuidDto (var uuid: String)

@RequestMapping("/api/admin")
@RestController
class AdminApiController {

    @Autowired
    private lateinit var game: GameManagerService

    @PostMapping("/new-sel-question")
    fun newSelQuestion(@RequestBody data: NewQuestionDto): Boolean  {
        if (!game.isUuidValid(data.uuid))
            return false

        game.sendNewSelQuestion(data.forTeams)
        return true
    }

    @PostMapping("/new-num-question")
    fun newNumQuestion(@RequestBody data: NewQuestionDto): Boolean  {
        if (!game.isUuidValid(data.uuid))
            return false

        game.sendNewNumQuestion(data.forTeams)
        return true
    }

    @PostMapping("/set-owner")
    fun setOwner(@RequestBody data: SetOwnerDto): Boolean {
        if (!game.isUuidValid(data.uuid))
            return false

        game.setOwner(data.place, data.owner)
        return true
    }

    @PostMapping("/set-selected")
    fun setSelected(@RequestBody data: SetSelectedDto): Boolean {
        if (!game.isUuidValid(data.uuid))
            return false

        game.setSelected(data.place, data.selected.toLowerCase() == "true")
        return true
    }

    @PostMapping("/set-tower")
    fun setTower(@RequestBody data: SetTowerDto): Boolean {
        if (!game.isUuidValid(data.uuid))
            return false

        game.setTower(data.place, data.tower.toLowerCase() == "true")
        return true
    }

    @PostMapping("/set-team-selected")
    fun setSelectedTeam(@RequestBody data: SetTeamSelectedDto): Boolean {
        if (!game.isUuidValid(data.uuid))
            return false

        game.setSelectedTeam(data.selected)
        game.sendMapUpdate()
        return true
    }

    @PostMapping("/show-answer")
    fun showAnswer(@RequestBody data: OnlyUuidDto): Boolean  {
        if (!game.isUuidValid(data.uuid))
            return false

        game.sendShowAnswer()
        return true
    }

    @PostMapping("/show-map")
    fun showMap(@RequestBody data: OnlyUuidDto): Boolean  {
        if (!game.isUuidValid(data.uuid))
            return false

        game.sendMapUpdate()
        game.sendHideQuestion()
        return true
    }
}