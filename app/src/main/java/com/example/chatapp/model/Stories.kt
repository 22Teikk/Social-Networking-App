package com.example.chatapp.model

data class Stories(
    var imageURL: String ?= null,
    var timeStart: Long = 0,
    var timeEnd: Long = 0,
    var storyID: String ?= null,
    var uid: String ?= null,
    var viewer: List<String> ?= null
)
