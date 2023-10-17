package com.app.dicodingstoryapp.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.dicodingstoryapp.GetDetailResponse
import com.app.dicodingstoryapp.story.StoryRepository
import kotlinx.coroutines.launch

class DetailStoryViewModel(private val storyRepository: StoryRepository) : ViewModel() {

    val getDetailResponse: LiveData<GetDetailResponse> = storyRepository.detailStory
    val showLoading: LiveData<Boolean> = storyRepository.isLoading

    suspend fun getDetailStory(id: String) {
        viewModelScope.launch {
            val token = storyRepository.getToken()
            val generatedToken = storyRepository.generateToken(token)

            storyRepository.getDetailStory(generatedToken, id)
        }
    }
}