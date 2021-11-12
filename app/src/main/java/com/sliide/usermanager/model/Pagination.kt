package com.sliide.usermanager.model

data class Pagination(
    val total: Int,
    val pages: Int,
    val page: Int,
    val limit: Int,
    val links: Links
)