package me.kylermintah.tito.model

class User(
    var username: String,
    var email: String,
    var profilePicturePath: String?,
    var friends: ArrayList<String>?,
    var voiceSamplePath: String?
) {
    constructor() : this(
        username = "",
        email = "",
        profilePicturePath = "",
        friends = null,
        voiceSamplePath = ""
    )
}