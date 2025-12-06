package com.example.itshere.ViewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory

object PostViewModelFactory {
    fun getFactory(context: Context): ViewModelProvider.Factory {
        return viewModelFactory {
            initializer {
                PostViewModel(context)
            }
        }
    }
}