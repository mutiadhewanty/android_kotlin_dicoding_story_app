package com.app.dicodingstoryapp.paging

import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.app.dicodingstoryapp.ApiService
import com.app.dicodingstoryapp.GetAllStoriesResponse
import com.app.dicodingstoryapp.Story
import com.app.dicodingstoryapp.model.UserPreferences
import com.app.dicodingstoryapp.story.StoryRepository

class StoryPagingSource(private val apiService: ApiService, private val userPreferences: UserPreferences) : PagingSource<Int, Story>() {

    companion object {
        const val INITIAL_PAGE_INDEX = 1
        fun snapshot(items: List<Story>): PagingData<Story> {
            return PagingData.from(items)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Story>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Story> {
        return try {
            val storyRepository = StoryRepository(userPreferences, apiService)
            val token = storyRepository.getToken()
            val generateToken = storyRepository.generateToken(token)

            val position = params.key ?: INITIAL_PAGE_INDEX
            val responseData = apiService.getAllStories(generateToken, position, params.loadSize)

            LoadResult.Page(
                data = responseData.listStory,
                prevKey = if (position == INITIAL_PAGE_INDEX) null else position -1,
                nextKey = if (responseData.listStory.isEmpty()) null else position +1

            )
        } catch (e: Exception) {
            return LoadResult.Error(e)
        }
    }
}