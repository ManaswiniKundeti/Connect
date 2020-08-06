package com.manu.connect.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.manu.connect.repository.IUsersRepository
import com.manu.connect.repository.UsersRepository
import java.lang.IllegalArgumentException

class MainActivityViewModelFactory (private val context : Context) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(MainActivityViewModel::class.java)){
            return MainActivityViewModel(UsersRepository()) as T
        }
        throw IllegalArgumentException("Unknown view model class")
    }
}