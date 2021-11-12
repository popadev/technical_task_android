package com.sliide.usermanager.model

data class CreateUser(
    val name: String,
    val email: String,
    val gender: String,
    val status: String
)