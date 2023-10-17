package com.app.dicodingstoryapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.app.dicodingstoryapp.auth.AuthRepository
import com.app.dicodingstoryapp.auth.AuthViewModel
import com.app.dicodingstoryapp.detail.DetailStoryViewModel
import com.app.dicodingstoryapp.listStory.ListStoryViewModel
import com.app.dicodingstoryapp.maps.MapsViewModel
import com.app.dicodingstoryapp.story.StoryRepository
import com.app.dicodingstoryapp.story.StoryViewModel

class ViewModelFactory(private val authRepository: AuthRepository, private val storyRepository: StoryRepository) : ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(authRepository) as T
        } else if (modelClass.isAssignableFrom(StoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StoryViewModel(storyRepository) as T
        } else if (modelClass.isAssignableFrom(ListStoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ListStoryViewModel(storyRepository) as T
        } else if (modelClass.isAssignableFrom(DetailStoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DetailStoryViewModel(storyRepository) as T
        } else if (modelClass.isAssignableFrom(MapsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MapsViewModel(storyRepository) as T
        }
        throw java.lang.IllegalArgumentException("Unknown ViewModel class: ${modelClass.simpleName}")
    }
    }
