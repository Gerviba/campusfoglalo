package hu.gerviba.campusfoglalo.model

data class UserEntity(
        val sessionId: String
) {

    var name: String = ""
    var screen: Boolean = false
    var teamId: Int = 0

}