package com.app.dicodingstoryapp.detail

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.app.dicodingstoryapp.*
import com.app.dicodingstoryapp.auth.AuthRepository
import com.app.dicodingstoryapp.databinding.ActivityDetailBinding
import com.app.dicodingstoryapp.model.StoryModel
import com.app.dicodingstoryapp.model.UserPreferences
import com.app.dicodingstoryapp.story.StoryRepository
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import kotlinx.coroutines.launch

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Suppress("DEPRECATION")
class DetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_NAME = "extra_name"
        const val EXTRA_DESC = "extra_desc"
        const val EXTRA_IMAGE = "extra_image"
        const val EXTRA_ID = "extra_id"
    }

    private lateinit var binding: ActivityDetailBinding
    private lateinit var detailStoryViewModel: DetailStoryViewModel
    private lateinit var storyModel: StoryModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)



        val userPreferences = UserPreferences.getInstance(dataStore)
        val apiService = ApiConfig.getApiService()
        val authRepository = AuthRepository(userPreferences, apiService)
        val storyRepository = StoryRepository(userPreferences, apiService)

        val viewModelFactory = ViewModelFactory(authRepository, storyRepository)
        detailStoryViewModel = ViewModelProvider(this, viewModelFactory)[DetailStoryViewModel::class.java]

        var id: String? = intent.getStringExtra(EXTRA_ID)

        lifecycleScope.launch {
            detailStoryViewModel.getDetailStory("$id")
        }

        detailStoryViewModel.getDetailResponse.observe(this, {detailStory ->
            if (detailStory != null) {
                Log.d(TAG, "Detail Story: $detailStory")
                setDetailStoryData(detailStory)
            }
        })

        detailStoryViewModel.showLoading.observe(this, {
            showLoading(it)
        })

        var name: String? = intent.getStringExtra(EXTRA_NAME)
        var desc: String? = intent.getStringExtra(EXTRA_DESC)
        var poster: String? = intent.getStringExtra(EXTRA_IMAGE)

        binding?.apply {
            tvItemTitle.text = name
            tvItemDescription.text = desc
            Glide.with(this@DetailActivity).load(poster).into(imgItemPoster)
        }

        playAnimation()
    }

    @SuppressLint("SuspiciousIndentation")
    private fun setDetailStoryData(detailResponse: GetDetailResponse) {
            Glide.with(this@DetailActivity)
                .load(detailResponse.story.photoUrl)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.e(TAG, "Error loading image: ${e?.message}")
                        e?.logRootCauses(TAG)
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }
                })
                .into(binding.imgItemPoster)

            binding.apply {
                tvItemTitle.text = detailResponse.story.name
                tvItemDescription.text = detailResponse.story.description
            }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun playAnimation() {

        val image = ObjectAnimator.ofFloat(binding.imgItemPoster, View.ALPHA, 1f).setDuration(500)
        val name = ObjectAnimator.ofFloat(binding.tvItemTitle, View.ALPHA, 1f).setDuration(500)
        val desc = ObjectAnimator.ofFloat(binding.tvItemDescription, View.ALPHA, 1f).setDuration(500)



        AnimatorSet().apply {
            playSequentially(
                image,
                name,
                desc,

            )
            startDelay = 500
        }.start()
    }
}