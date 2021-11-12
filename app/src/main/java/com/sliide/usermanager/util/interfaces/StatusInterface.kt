package com.sliide.usermanager.util.interfaces

interface StatusInterface<T> {
    fun success(response: T)

    fun fail(message: String)
}