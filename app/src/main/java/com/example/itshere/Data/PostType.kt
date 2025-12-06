package com.example.itshere.Data

enum class PostType {
    LOST, FOUND
}

data class QuestionAnswer(
    val question: String = "",
    val answer: String = ""
)