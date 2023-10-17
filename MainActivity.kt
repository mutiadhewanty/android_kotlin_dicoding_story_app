package com.app.dicodingstoryapp

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.app.dicodingstoryapp.auth.AuthRepository
import com.app.dicodingstoryapp.auth.AuthViewModel
import com.app.dicodingstoryapp.databinding.ActivityMainBinding
import com.app.dicodingstoryapp.auth.LoginActivity
import com.app.dicodingstoryapp.model.UserPreferences
import com.app.dicodingstoryapp.story.StoryRepository
import com.app.dicodingstoryapp.utils.Result

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userPreferences = UserPreferences.getInstance(dataStore)
        val apiService = ApiConfig.getApiService()
        val authRepository = AuthRepository(userPreferences, apiService)
        val storyRepository = StoryRepository(userPreferences, apiService)

        authViewModel = ViewModelProvider(this, ViewModelFactory(authRepository, storyRepository))[AuthViewModel::class.java]

        binding.login.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        authViewModel.isLoading.observe(this, {
            showLoading(it)
        })

        binding.nameEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val name = s.toString().trim()
                if (name.isEmpty()) {
                    binding.inputEmail.error = "Masukkan nama"
                } else {
                    binding.inputEmail.error = null
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })

        setupAction()
        observeRegisterResult()
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun observeRegisterResult() {
        authViewModel.registerResult.observe(this, { result ->
            when (result) {
                is Result.Success -> {
                    // Registrasi berhasil
                    Toast.makeText(this, "Akun berhasil dibuat: ${result.data.message}", Toast.LENGTH_SHORT).show()
                    // Redirect ke halaman login atau halaman selanjutnya
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                is Result.Failure -> {
                    // Registrasi gagal
                    Toast.makeText(this, "Register Failed: ${result.exception.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun setupAction() {
        binding.btnRegister.setOnClickListener {
            val name = binding.nameEditText.text.toString()
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            when {
                name.isEmpty() -> {
                    binding.nameEditTextLayout.error = "Masukkan nama"
                }
                else -> {
                    binding.inputEmail.error = null
                }
            }

            when {
                email.isEmpty() -> {
                    binding.inputEmail.error = "Masukkan email"
                }
                !ApiService.isEmailValid(email) -> {
                    binding.inputEmail.error = "Masukkan alamat email yang valid"
                }
                else -> {
                    binding.inputEmail.error = null
                }
            }

            when {
                password.isEmpty() -> {
                    binding.inputPassword.error = "Masukkan password"
                }
                !ApiService.isPasswordValid(password) -> {
                    binding.inputPassword.error = "Password salah"
                }
                else -> {
                    binding.inputPassword.error = null
                }
            }

            if (ApiService.isEmailValid(email) && ApiService.isPasswordValid(password)) {
                authViewModel.register(name, email, password)
            }

        }
    }


}

