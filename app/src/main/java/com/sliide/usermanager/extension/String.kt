package com.sliide.usermanager.extension

fun String.isEmail(): Boolean {
    return isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
}