package com.example.itshere.Data

import java.util.UUID

data class ImageItem(
    val id: String = UUID.randomUUID().toString(),
    val uri: String,
    val isLocal: Boolean = true
)
