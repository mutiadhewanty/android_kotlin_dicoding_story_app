package com.app.dicodingstoryapp.listStory

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.dicodingstoryapp.*
import com.app.dicodingstoryapp.auth.AuthRepository
import com.app.dicodingstoryapp.auth.AuthViewModel
import com.app.dicodingstoryapp.auth.LoginActivity
import com.app.dicodingstoryapp.databinding.ActivityListStoryBinding
import com.app.dicodingstoryapp.detail.DetailActivity
import com.app.dicodingstoryapp.maps.MapsActivity
import com.app.dicodingstoryapp.model.StoryModel
import com.app.dicodingstoryapp.model.UserPreferences
import com.app.dicodingstoryapp.paging.LoadingStateAdapter
import com.app.dicodingstoryapp.story.StoryRepository
import com.app.dicodingstoryapp.story.StoryViewModel
import com.google.gson.Gson
import kotlinx.coroutines.launch

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class ListStoryActivity : AppCompatActivity() {


    private lateinit var binding: ActivityListStoryBinding
    private lateinit var listStoryViewModel: ListStoryViewModel
    private lateinit var authViewModel: AuthViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityListStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userPreferences = UserPreferences.getInstance(dataStore)
        val apiService = ApiConfig.getApiService()
        val authRepository = AuthRepository(userPreferences, apiService)
        val storyRepository = StoryRepository(userPreferences, apiService)

        val viewModelFactory = ViewModelFactory(authRepository, storyRepository)
        listStoryViewModel = ViewModelProvider(this, viewModelFactory)[ListStoryViewModel::class.java]
        authViewModel = ViewModelProvider(this, viewModelFactory)[AuthViewModel::class.java]


        val layoutManager = LinearLayoutManager(this)
        binding.rvStory.layoutManager = layoutManager
        val itemDecoration = DividerItemDecoration(this, layoutManager.orientation)
        binding.rvStory.addItemDecoration(itemDecoration)

//
//        listStoryViewModel.getAllStoriesResponse.observe(this, {
//            setListStory()
//        })

        listStoryViewModel.showLoading.observe(this, {
            showLoading(it)
        })

        authViewModel.logoutResult.observe(this, {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        })

        setListStory()

//        lifecycleScope.launch {
//            listStoryViewModel.getAllStoriesList()
//        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.option_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.locStory -> {
                val intent = Intent(this, MapsActivity::class.java)
                startActivity(intent)
            }
            R.id.addStory -> {
                val intent = Intent(this, AddStoryActivity::class.java)
                startActivity(intent)
            }
            R.id.logout -> {
                authViewModel.logout()
                true
            }
        }
        return super.onOptionsItemSelected(item)
    }



    private fun setListStory() {

        val adapter = StoryAdapter()
        binding.rvStory.adapter = adapter.withLoadStateFooter(
            footer = LoadingStateAdapter{
                adapter.retry()
            }
        )

        listStoryViewModel.getAllStoriesResponse.observe(this, {
            adapter.submitData(lifecycle, it)
        })

        adapter.setOnItemClickedCallback(object : StoryAdapter.OnItemClickCallback {
            override fun onItemClicked(list: Story) {
                val intent = Intent(this@ListStoryActivity, DetailActivity::class.java)
                intent.putExtra(DetailActivity.EXTRA_ID, list.id)
                intent.putExtra(DetailActivity.EXTRA_NAME, list.name)
                intent.putExtra(DetailActivity.EXTRA_DESC, list.description)
                intent.putExtra(DetailActivity.EXTRA_IMAGE, list.photoUrl)
                startActivity(intent)

                showSelectedUser(list)
            }

        })

    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showSelectedUser(list: Story) {
        Toast.makeText(this, "Kamu memilih " + list.name, Toast.LENGTH_SHORT).show()
    }

}