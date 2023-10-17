package com.app.dicodingstoryapp.maps

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.dicodingstoryapp.GetDetailResponse
import com.app.dicodingstoryapp.Story
import com.app.dicodingstoryapp.story.StoryRepository
import kotlinx.coroutines.launch

class MapsViewModel(private val storyRepository: StoryRepository) : ViewModel() {

    val getStoryResponse: LiveData<List<Story>> = storyRepository.locStory
    val showLoading: LiveData<Boolean> = storyRepository.isLoading

    suspend fun getAllLocation(size: Int, location: Int) {
        viewModelScope.launch {
            val token = storyRepository.getToken()
            val generatedToken = storyRepository.generateToken(token)

            storyRepository.getAllLocation(generatedToken, size, location)
        }
    }
}