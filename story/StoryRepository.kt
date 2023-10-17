package com.app.dicodingstoryapp.story

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.app.dicodingstoryapp.*
import com.app.dicodingstoryapp.model.UserPreferences
import com.app.dicodingstoryapp.paging.StoryPagingSource
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.await
import java.io.File

class StoryRepository(
    private val userPreferences: UserPreferences,
    private val apiService: ApiService
) {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _listStory = MutableLiveData<List<Story>>()
    val listStory: LiveData<List<Story>> = _listStory

    private val _detailStory = MutableLiveData<GetDetailResponse>()
    val detailStory: LiveData<GetDetailResponse> = _detailStory

    private val _locStory = MutableLiveData<List<Story>>()
    val locStory: LiveData<List<Story>> = _locStory


    suspend fun getToken(): String {
        return userPreferences.getToken()
    }

    fun generateToken(token: String): String {
        return "Bearer $token"
    }

    fun getAllLocation(token: String, size: Int, location: Int) {
        _isLoading.value = true

        val client = apiService.getLocations(token, size, location)
        client.enqueue(object : Callback<GetAllStoriesResponse>{
            override fun onResponse(
                call: Call<GetAllStoriesResponse>,
                response: Response<GetAllStoriesResponse>
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        val story = responseBody.listStory
                        _locStory.postValue(story)
                    }
                }
                else {
                    Log.e(TAG, "onFailure: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<GetAllStoriesResponse>, t: Throwable) {
                _isLoading.value = false
                Log.e(TAG, "onFailure: ${t.message.toString()}")
            }

        })
    }


    fun getDetailStory(token: String, id: String) {
        _isLoading.value = true

            val client = apiService.detailStory(token, id)
            client.enqueue(object : retrofit2.Callback<GetDetailResponse>{
                override fun onResponse(
                    call: Call<GetDetailResponse>,
                    response: Response<GetDetailResponse>
                ) {
                    _isLoading.value = false
                    if (response.isSuccessful) {
                        val responseBody = response.body()
                        if (responseBody != null) {
                            _detailStory.value = response.body()
                        }
                    } else {
                        Log.e(TAG, "onFailure: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<GetDetailResponse>, t: Throwable) {
                    _isLoading.value = false
                    Log.e(TAG, "onFailure: ${t.message.toString()}")
                }

            })
    }

    fun getAllStoriesList(): LiveData<PagingData<Story>>{
        return Pager(
            config = PagingConfig(
                pageSize = 5
            ),
            pagingSourceFactory = {
                StoryPagingSource(apiService, userPreferences)
            }
        ).liveData
    }


    suspend fun uploadStory(token: String, description: String, imageFile: File) {

        val descriptionRequestBody = description.toRequestBody("text/plain".toMediaTypeOrNull())
        val imageRequestBody = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
        val imagePart = MultipartBody.Part.createFormData("photo", imageFile.name, imageRequestBody)

        val generateToken = generateToken(token)

        val request = apiService.uploadImage(generateToken, imagePart, descriptionRequestBody)

        request.enqueue(object : retrofit2.Callback<FileUploadResponse> {
            override fun onResponse(call: Call<FileUploadResponse>, response: Response<FileUploadResponse>) {
                if (response.isSuccessful) {
                    Log.e(TAG, "Upload success: " + response.body()?.toString())
                } else {
                    Log.e(TAG, "Upload failed: " + response.errorBody()?.string())
                }
            }

            override fun onFailure(call: Call<FileUploadResponse>, t: Throwable) {
                Log.e(TAG, "Upload failed: " + t.message)
            }
        })
    }

}
