package com.app.dicodingstoryapp.auth

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
import androidx.lifecycle.lifecycleScope
import com.app.dicodingstoryapp.*
import com.app.dicodingstoryapp.databinding.ActivityLoginBinding
import com.app.dicodingstoryapp.listStory.ListStoryActivity
import com.app.dicodingstoryapp.model.DataUser
import com.app.dicodingstoryapp.model.UserPreferences
import com.app.dicodingstoryapp.story.StoryRepository
import com.app.dicodingstoryapp.utils.Result
import kotlinx.coroutines.launch

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userPreferences = UserPreferences.getInstance(dataStore)
        val apiService = ApiConfig.getApiService()
        val authRepository = AuthRepository(userPreferences, apiService)
        val storyRepository = StoryRepository(userPreferences, apiService)

        authViewModel = ViewModelProvider(this, ViewModelFactory(authRepository, storyRepository))[AuthViewModel::class.java]

        binding.register.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        authViewModel.checkUserLogin()

        authViewModel.isLoggedIn.observe(this, {cek ->
            if (cek) {
                startActivity(Intent(this, ListStoryActivity::class.java))
                finish()
            }
        })

        authViewModel.isLoading.observe(this, {
            showLoading(it)
        })


        observeLoginResult()
        setupAction()
    }

    private fun observeLoginResult() {
        authViewModel.loginResult.observe(this, { result ->
            when (result) {
                is Result.Success -> {
                    Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()

                    authViewModel.getUser()

                    val intent = Intent(this, ListStoryActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                is Result.Failure -> {
                    Toast.makeText(this, "Login failed: ${result.exception.message}", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

//    private fun saveUser(session: DataUser){
//        authViewModel.saveUser(session)
//    }

    companion object {
        private const val AUTH_KEY = "Bearer "
    }


    private fun setupAction() {
        binding.btnLogin.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            if (ApiService.isEmailValid(email) && ApiService.isPasswordValid(password)) {
                authViewModel.login(email, password)
            }

        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}