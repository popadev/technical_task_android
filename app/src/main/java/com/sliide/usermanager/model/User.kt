package com.sliide.usermanager.model

data class User(
    val id: Int,
    val name: String,
    val email: String,
    val gender: String,
    val status: String
)