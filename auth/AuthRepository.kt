package com.app.dicodingstoryapp.auth

import com.app.dicodingstoryapp.ApiService
import com.app.dicodingstoryapp.LoginResponse
import com.app.dicodingstoryapp.RegisterResponse
import com.app.dicodingstoryapp.utils.Result
import com.app.dicodingstoryapp.model.DataUser
import com.app.dicodingstoryapp.model.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class AuthRepository(
    private val userPreferences: UserPreferences,
    private val apiService: ApiService
) {

    fun isLogin(): Flow<Boolean> {
        return userPreferences.isUserLoggedIn()
    }


    fun getUser(): Flow<DataUser> {
        return userPreferences.getUser()
    }

    suspend fun saveUser(dataUser: Result<LoginResponse>) {
        if (dataUser is com.app.dicodingstoryapp.utils.Result.Success){
            val token = dataUser.data.loginResult.token
            val result = DataUser(token, true)
            userPreferences.saveUser(result)
            userPreferences.saveToken(token)
        }
    }

    suspend fun loginUser(email: String, password: String): Result<LoginResponse> {
        return try {
            val response = withContext(Dispatchers.IO) {
                apiService.login(email, password).execute()
            }
            val responseBody = response.body()

            if (response.isSuccessful && responseBody != null) {
                val user = DataUser(responseBody.loginResult.token, true)
                userPreferences.saveUser(user)
                userPreferences.saveToken(responseBody.loginResult.token)
                Result.Success(responseBody)
            } else {
                val errorBody = response.errorBody()?.toString()
                val errorMessage = errorBody ?: response.message()
                Result.Failure(Throwable(errorMessage))
            }
        } catch (e: Exception) {
            Result.Failure(Throwable(e.localizedMessage))
        }

    }

    suspend fun registerUser(name: String, email: String, password: String): Result<RegisterResponse> {
        return try {
            val response = withContext(Dispatchers.IO) {
                apiService.register(name, email, password).execute()
            }
            val responseBody = response.body()

            if (response.isSuccessful && responseBody != null) {
                Result.Success(responseBody)
            } else {
                val errorBody = response.errorBody()?.toString()
                val errorMessage = errorBody ?: response.message()
                Result.Failure(Throwable(errorMessage))
            }
        } catch (e: Exception) {
            Result.Failure(Throwable(e.localizedMessage))
        }
    }

    suspend fun logoutUser() {
        userPreferences.logout()
    }
}