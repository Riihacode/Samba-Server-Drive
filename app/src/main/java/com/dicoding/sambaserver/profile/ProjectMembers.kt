package com.dicoding.sambaserver.profile

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ProjectMembers(
    val name: String,
    val descriptionNim: String,
    val photo: Int,
    val role: String,
    val duty: String
) : Parcelable
