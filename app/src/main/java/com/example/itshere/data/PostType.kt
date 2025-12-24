package com.example.itshere.data

enum class PostType {
    LOST, FOUND
}

data class QuestionAnswer(
    val question: String = "",
    val answer: String = ""
)