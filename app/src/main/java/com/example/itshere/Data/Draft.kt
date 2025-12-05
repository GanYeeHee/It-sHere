package com.example.itshere.Data

data class Draft(
    val postType: String = "",
    val title: String = "",
    val description: String = "",
    val date: String = "",
    val category: String = "",
    val phone: String = "",
    val images: List<String> = emptyList(),
    val question1: String = "",
    val answer1: String = "",
    val question2: String = "",
    val answer2: String = "",
    val question3: String = "",
    val answer3: String = ""
)
