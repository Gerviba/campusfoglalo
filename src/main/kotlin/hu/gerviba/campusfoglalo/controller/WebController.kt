package hu.gerviba.campusfoglalo.controller

import hu.gerviba.campusfoglalo.service.GameManagerService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@Controller
class WebController {

    @Autowired
    private lateinit var game: GameManagerService

    @GetMapping("/")
    fun index() = "index"

    @GetMapping("/screen")
    fun screen() = "screen"

    @GetMapping("/play")
    fun play() = "play"

    @GetMapping("/start")
    fun start(): String {
        return "redirect:/admin/" + game.startGame()
    }

    @GetMapping("/admin/{uuid}")
    fun admin(@PathVariable uuid: String, model: Model): String {
        if (!game.isUuidValid(uuid))
            return "redirect:/?invalid-uuid"

        model.addAttribute("uuid", uuid)
        model.addAttribute("names", game.placeNames())
        return "admin"
    }

}