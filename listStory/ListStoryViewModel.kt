package com.app.dicodingstoryapp.listStory

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.app.dicodingstoryapp.Story
import com.app.dicodingstoryapp.story.StoryRepository

class ListStoryViewModel(storyRepository: StoryRepository) : ViewModel() {
    val getAllStoriesResponse: LiveData<PagingData<Story>> by lazy {
        storyRepository.getAllStoriesList().cachedIn(viewModelScope)
    }
    val showLoading: LiveData<Boolean> = storyRepository.isLoading

}