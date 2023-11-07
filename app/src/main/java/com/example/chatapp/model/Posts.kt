package com.example.chatapp.model

data class Posts(
    var pid: String? = null,
    var title: String? = null,
    var listPhoto: List<String> ?= null,
    var uid: String ?= null
)
