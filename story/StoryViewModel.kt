package com.app.dicodingstoryapp.story

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.dicodingstoryapp.FileUploadResponse
import com.app.dicodingstoryapp.utils.Result
import kotlinx.coroutines.launch
import java.io.File

class StoryViewModel(private val storyRepository: StoryRepository) : ViewModel() {

    private val _uploadResult = MutableLiveData<Result<FileUploadResponse>>()
    val uploadResult: LiveData<Result<FileUploadResponse>> = _uploadResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun uploadStory(description: String, imageFile: File) {
        viewModelScope.launch {
            _isLoading.value = true
            val token = storyRepository.getToken()
            val generatedToken = storyRepository.generateToken(token)

            storyRepository.uploadStory(generatedToken, description, imageFile)
            _isLoading.value = false
        }
    }
}