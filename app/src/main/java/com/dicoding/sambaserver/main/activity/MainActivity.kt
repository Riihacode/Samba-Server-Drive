package com.dicoding.sambaserver.main.activity

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.sambaserver.R
import com.dicoding.sambaserver.main.adapter.FileListAdapter
import com.dicoding.sambaserver.databinding.ActivityMainBinding
import com.dicoding.sambaserver.login.activity.LoginActivity
import com.dicoding.sambaserver.menubottomsheet.MenuBottomSheet
import com.dicoding.sambaserver.repository.SambaRepository
import com.dicoding.sambaserver.main.viewmodel.MainViewModel
import com.dicoding.sambaserver.viewmodelfactory.ViewModelFactory
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private var pendingDownloadFileName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (viewModel.isAtRootDirectory()) {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed() // Kembali ke parent
                } else {
                    viewModel.navigateToParentDirectory()
                }
            }
        })

        val smbUrl = intent.getStringExtra("server_url") ?: ""
        if (smbUrl.isEmpty()) {
            navigateToLogin()
            return
        }

        val repository = SambaRepository(smbUrl)
        val factory = ViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]

        setupRecyclerView()
        observeViewModel()

        lifecycleScope.launch {
            try {
                viewModel.initializeRepository().join()
                viewModel.fetchFiles()
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error initializing repository: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        binding.fabAdd.setOnClickListener { showBottomSheetMenu() }
    }

    fun updateFileList(newFileList: List<String>) {
        viewModel.updateFileList(newFileList)
    }

    private fun setupRecyclerView() {
        binding.fileList.layoutManager = LinearLayoutManager(this)
        binding.fileList.adapter = FileListAdapter(
            onFileClick = { fileName -> viewModel.onFileClick(this, fileName) },
            onDeleteClick = { fileName -> confirmDelete(fileName) },
            onDownloadClick = { fileName -> pickDownloadLocation(fileName) }
        )
    }

    private fun observeViewModel() {
        viewModel.files.observe(this) { fileNames ->
            Log.d("MainActivity", "Files loaded: $fileNames")
            (binding.fileList.adapter as FileListAdapter).submitList(fileNames)
        }

        viewModel.toastMessage.observe(this) { message ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }

        viewModel.loading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

    }

    private val uploadFileLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                viewModel.uploadFile(this, uri)
            } else {
                Toast.makeText(this, "No file selected for upload", Toast.LENGTH_SHORT).show()
            }
        }

    private val downloadFileLauncher =
        registerForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri ->
            if (uri != null && pendingDownloadFileName != null) {
                viewModel.downloadFileToCustomLocation(this, pendingDownloadFileName!!, uri)
            } else {
                Toast.makeText(this, "No location selected for download", Toast.LENGTH_SHORT).show()
            }
        }

    private fun openFilePicker() {
        uploadFileLauncher.launch("*/*")
    }

    private fun pickDownloadLocation(fileName: String) {
        pendingDownloadFileName = fileName
        downloadFileLauncher.launch(fileName)
    }

    private fun showBottomSheetMenu() {
        val bottomSheet = MenuBottomSheet { actionType ->
            when (actionType) {
                MenuBottomSheet.ActionType.ADD_FOLDER -> showAddFolderDialog()
                MenuBottomSheet.ActionType.UPLOAD_FILE -> openFilePicker()
            }
        }
        bottomSheet.show(supportFragmentManager, "MenuBottomSheet")
    }

    private fun confirmDelete(fileName: String) {
        AlertDialog.Builder(this)
            .setTitle("Delete")
            .setMessage("Are you sure you want to delete $fileName?")
            .setPositiveButton("Yes") { _, _ -> viewModel.deleteFile(fileName) }
            .setNegativeButton("No", null)
            .show()
    }

    private fun navigateToLogin() {
        Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showAddFolderDialog() {
        val dialog = AlertDialog.Builder(this)
        val input = EditText(this)
        input.hint = "Folder Name"
        dialog.setView(input)
        dialog.setPositiveButton("Create") { _, _ ->
            val folderName = input.text.toString().trim()
            if (folderName.isNotEmpty()) {
                viewModel.createFolder(folderName)
            } else {
                Toast.makeText(this, "Folder name cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.setNegativeButton("Cancel", null)
        dialog.show()
    }

    private fun performLogout() {
        // Contoh menghapus sesi pengguna
        val sharedPreferences = getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()

        // Pindah ke LoginActivity
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_logout -> {
                performLogout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
