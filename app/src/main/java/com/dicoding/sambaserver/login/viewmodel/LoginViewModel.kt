package com.dicoding.sambaserver.login.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.sambaserver.repository.SambaRepository
import kotlinx.coroutines.launch

class LoginViewModel(private val repository: SambaRepository) : ViewModel() {
    private val _serverUrl = MutableLiveData<String>()
    val serverUrl: LiveData<String> get() = _serverUrl

    private val _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String> get() = _toastMessage

    fun resolveAndSetHostname(hostname: String) {
        viewModelScope.launch {
            val resolvedUrl = "smb://${repository.resolveHostnameWithFallback(hostname)}/samba_office/"
            _serverUrl.postValue(resolvedUrl)
        }
    }
}
