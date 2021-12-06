package hu.gerviba.campusfoglalo.packet

import hu.gerviba.campusfoglalo.service.GameManagerService

data class PlaceStatus(var owner: Int, var selected: Boolean, var tower: Boolean)

data class ScreenStatusPacket(
        var users: Map<Int, GameManagerService.TeamInfo>,
        var places: Map<String, PlaceStatus>,
        var activeTeam: GameManagerService.TeamInfo?,
        var attackOrder: String)