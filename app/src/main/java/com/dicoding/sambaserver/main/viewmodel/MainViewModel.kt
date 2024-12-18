package com.dicoding.sambaserver.main.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.sambaserver.repository.SambaRepository
import jcifs.smb.SmbFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(private val repository: SambaRepository) : ViewModel() {

    private val _files = MutableLiveData<List<String>>()
    val files: LiveData<List<String>> get() = _files

    private val _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String> get() = _toastMessage

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    fun initializeRepository() = viewModelScope.launch {
        try {
            repository.initialize()
            Log.d("MainViewModel", "Repository initialized successfully")
        } catch (e: Exception) {
            Log.e("MainViewModel", "Error initializing repository: ${e.message}", e)
            _toastMessage.postValue("Error initializing repository: ${e.message}")
        }
    }

    fun fetchFiles() {
        _loading.value = true
        viewModelScope.launch {
            try {
                val fileList = repository.fetchFiles()
                Log.d("MainViewModel", "Files fetched: $fileList")
                _files.postValue(fileList)
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error fetching files: ${e.message}", e)
                _toastMessage.postValue("Error fetching files: ${e.message}")
            } finally {
                _loading.postValue(false)
            }
        }
    }

    fun deleteFile(fileName: String) {
        viewModelScope.launch {
            try {
                repository.deleteFile(fileName)
                fetchFiles()
                _toastMessage.postValue("File deleted: $fileName")
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error deleting file: ${e.message}", e)
                _toastMessage.postValue("Error deleting file: ${e.message}")
            }
        }
    }

    fun downloadFileToCustomLocation(context: Context, fileName: String, fileUri: Uri) {
        viewModelScope.launch {
            try {
                repository.downloadFileToCustomLocation(context, fileName, fileUri)
                _toastMessage.postValue("File downloaded to $fileUri")
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error downloading file: ${e.message}", e)
                _toastMessage.postValue("Error downloading file: ${e.message}")
            }
        }
    }

    fun createFolder(folderName: String) {
        viewModelScope.launch {
            try {
                repository.createFolder(folderName)
                fetchFiles()
                _toastMessage.postValue("Folder created: $folderName")
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error creating folder: ${e.message}", e)
                _toastMessage.postValue("Error creating folder: ${e.message}")
            }
        }
    }

    fun onFileClick(context: Context, fileName: String) {
        viewModelScope.launch {
            try {
                repository.openFile(context, fileName)
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error opening file: ${e.message}", e)
                _toastMessage.postValue("Error opening file: ${e.message}")
            }
        }
    }

    fun updateFileList(newFileList: List<String>) {
        _files.postValue(newFileList)
    }

    fun uploadFile(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                repository.uploadFile(context, uri)
                fetchFiles() // Refresh daftar file setelah upload
                _toastMessage.postValue("File uploaded successfully")
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error uploading file: ${e.message}", e)
                _toastMessage.postValue("Error uploading file: ${e.message}")
            }
        }
    }

    fun navigateToParentDirectory() {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val currentDir = SmbFile(repository.smbUrl, repository.cifsContext)
                    val parentPath = currentDir.parent

                    if (parentPath != null) {
                        val parentDir = SmbFile(parentPath, repository.cifsContext)
                        if (parentDir.exists() && parentDir.isDirectory) {
                            repository.updateSmbUrl(parentDir.canonicalPath)
                            withContext(Dispatchers.Main) {
                                fetchFiles() // Refresh daftar file pada thread utama
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                _toastMessage.postValue("Cannot navigate to parent directory. Invalid path.")
                            }
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            _toastMessage.postValue("Already at the root directory.")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error navigating back: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    _toastMessage.postValue("Error navigating back: ${e.message}")
                }
            }
        }
    }

    fun isAtRootDirectory(): Boolean {
        return repository.isAtRootDirectory()
    }
}
