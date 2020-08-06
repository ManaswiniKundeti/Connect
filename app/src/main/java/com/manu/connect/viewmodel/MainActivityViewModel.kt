package com.manu.connect.viewmodel

import androidx.lifecycle.ViewModel
import com.manu.connect.repository.IUsersRepository
import com.manu.connect.repository.UsersRepository

class MainActivityViewModel(private val usersRepository: UsersRepository) : ViewModel() {
}