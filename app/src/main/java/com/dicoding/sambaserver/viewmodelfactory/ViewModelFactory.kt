package com.dicoding.sambaserver.viewmodelfactory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dicoding.sambaserver.login.viewmodel.LoginViewModel
import com.dicoding.sambaserver.main.viewmodel.MainViewModel
import com.dicoding.sambaserver.repository.SambaRepository

@Suppress("UNCHECKED_CAST")
class ViewModelFactory(private val repository: SambaRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(MainViewModel::class.java) -> MainViewModel(repository) as T
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> LoginViewModel(repository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}