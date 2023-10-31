package com.example.chatapp.model

data class Users(
    val uid: String,
    var age: Int = 0,
    var name: String = "",
    var avartar: String = "",
    var gender: String = "",
    var nFriends: Int = 0,
    var nPosts: Int = 0
)
