package com.app.dicodingstoryapp.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class StoryModel(
    val id: String? = null,
    var name: String? = null,
    val desc: String? = null,
    val image: String? = null
) : Parcelable
