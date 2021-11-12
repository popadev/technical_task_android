package com.sliide.usermanager.model

data class UsersData(
    val meta: PageMeta,
    val data: Array<User>
)