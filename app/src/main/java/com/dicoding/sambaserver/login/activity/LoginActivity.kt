package com.dicoding.sambaserver.login.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.dicoding.sambaserver.main.activity.MainActivity
import com.dicoding.sambaserver.databinding.ActivityLoginBinding
import com.dicoding.sambaserver.login.viewmodel.LoginViewModel
import com.dicoding.sambaserver.repository.SambaRepository
import com.dicoding.sambaserver.viewmodelfactory.ViewModelFactory
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val repository = SambaRepository("")
        val factory = ViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[LoginViewModel::class.java]

        setupObservers()
        // Resolusi hostname, tetapi jangan langsung navigasi ke MainActivity
        lifecycleScope.launch {
            val hostname = "linux-samba"
            viewModel.resolveAndSetHostname(hostname)
        }

        // Navigasi hanya saat tombol login ditekan
        binding.loginButton.setOnClickListener {
            val serverUrl = binding.serverUrl.text.toString()
            if (serverUrl.isNotEmpty()) {
                navigateToMainActivity(serverUrl)
            } else {
                Toast.makeText(this, "Please enter the Server URL", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // mengarahkan pengguna ke MainActivity setelah memasukkan URL server.
    private fun navigateToMainActivity(serverUrl: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("server_url", serverUrl)
        }
        startActivity(intent)
        finish()
    }

    // memantau serverUrl dari LoginViewModel, yang diatur berdasarkan hostname yang telah direalisasi.
    private fun setupObservers() {
        viewModel.serverUrl.observe(this) { url ->
            if (!url.isNullOrEmpty()) {
                binding.serverUrl.setText(url)
            }
        }

        viewModel.toastMessage.observe(this) { message ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }
}