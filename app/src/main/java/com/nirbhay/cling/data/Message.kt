package com.nirbhay.cling.data

data class Message(
    val userDetails: User = User(),
    val createdAt: Long = 0L,
    val message: String? = "",
    val messageTo: String = "",
)
