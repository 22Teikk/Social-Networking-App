package com.example.chatapp.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Users(
    var uid: String? = null,
    var age: Int? = null,
    var name: String? = null,
    var avatar: String? = null,
    var gender: String? = null,
    var nFriends: Int? = null,
    var nPosts: Int? = null
): Parcelable
