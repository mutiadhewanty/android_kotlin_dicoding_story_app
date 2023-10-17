package com.app.dicodingstoryapp.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.dicodingstoryapp.LoginResponse
import com.app.dicodingstoryapp.RegisterResponse
import com.app.dicodingstoryapp.utils.Result
import com.app.dicodingstoryapp.model.DataUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {
    private val _loginResult = MutableLiveData<Result<LoginResponse>>()
    val loginResult: LiveData<Result<LoginResponse>> = _loginResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _logoutResult = MutableLiveData<Unit>()
    val logoutResult: LiveData<Unit> = _logoutResult

    private val _registerResult = MutableLiveData<Result<RegisterResponse>>()
    val registerResult: LiveData<Result<RegisterResponse>> = _registerResult

    private val _userData = MutableLiveData<DataUser>()
    val userData: LiveData<DataUser> = _userData

    private val _isLoggedIn = MutableLiveData<Boolean>()
    val isLoggedIn: LiveData<Boolean> = _isLoggedIn

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = authRepository.loginUser(email, password)
            authRepository.saveUser(result)
            _loginResult.value = result
            _isLoading.value = false
        }
    }

    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = authRepository.registerUser(name, email, password)
            _registerResult.value = result
            _isLoading.value = false
        }
    }

    fun getUser() {
        viewModelScope.launch {
            _isLoading.value = true
            val user = authRepository.getUser().first()
            _userData.value = user
            _isLoading.value = false

        }
    }


    fun logout() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = authRepository.logoutUser()
            _logoutResult.value = result
            _isLoading.value = false
        }
    }

    fun checkUserLogin() {
        viewModelScope.launch {
            val isUserLoggedIn = authRepository.isLogin().first()
            _isLoggedIn.value = isUserLoggedIn
        }
    }


}