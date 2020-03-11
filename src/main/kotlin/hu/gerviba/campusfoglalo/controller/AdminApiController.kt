package hu.gerviba.campusfoglalo.controller

import hu.gerviba.campusfoglalo.service.GameManagerService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class AdminApiController {

    @Autowired
    private lateinit var game: GameManagerService

    @PostMapping("/api/admin/new-question")
    fun newQuestion(@RequestParam uuid: String) {

    }

    @PostMapping("/api/admin/set-owner")
    fun setOwner(@RequestParam uuid: String, @RequestParam place: String, @RequestParam owner: Int): Boolean {
        if (!game.isUuidValid(uuid))
            return false

        game.setOwner(place, owner)

        return true
    }

}