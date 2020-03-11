package hu.gerviba.campusfoglalo.packet

data class PlaceStatus(var owner: Int, var selected: Boolean, var tower: Boolean)

data class ScreenStatusPacket(var users: Map<String, Int>, var places: Map<String, PlaceStatus>)