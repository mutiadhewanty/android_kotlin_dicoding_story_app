package com.app.dicodingstoryapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.app.dicodingstoryapp.auth.AuthRepository
import com.app.dicodingstoryapp.auth.LoginActivity
import com.app.dicodingstoryapp.databinding.ActivityAddStoryBinding
import com.app.dicodingstoryapp.listStory.ListStoryActivity
import com.app.dicodingstoryapp.model.UserPreferences
import com.app.dicodingstoryapp.story.StoryRepository
import com.app.dicodingstoryapp.story.StoryViewModel
import com.app.dicodingstoryapp.utils.Result
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class AddStoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddStoryBinding
    private var getFile: File? = null
    private lateinit var viewModel: StoryViewModel
    private var token: String? = null

//    private val permissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE)

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (!allPermissionsGranted()) {
                Toast.makeText(
                    this,
                    "Tidak mendapatkan permission.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }

        val dataStore: DataStore<Preferences> = this.dataStore

        val userPreferences = UserPreferences.getInstance(dataStore)
        val apiService = ApiConfig.getApiService()
        val authRepository = AuthRepository(userPreferences, apiService)
        val storyRepository = StoryRepository(userPreferences, apiService)

        val viewModelFactory = ViewModelFactory(authRepository, storyRepository)
        viewModel = ViewModelProvider(this, viewModelFactory)[StoryViewModel::class.java]

        viewModel.isLoading.observe(this, {
            showLoading(it)
        })

        initializeViews()

        binding.uploadButton.setOnClickListener {
            showLoading(true)
            val description = binding.deskripsiEditText.text.toString().toRequestBody("text/plain".toMediaType())

            if (getFile != null) {
                val file = reduceFileImage(getFile as File)

                val requestImageFile = file.asRequestBody("image/jpeg".toMediaType())
                val imageMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
                    "photo",
                    file.name,
                    requestImageFile
                )

                lifecycleScope.launch {
                    token = storyRepository.getToken()
                    val generateToken = storyRepository.generateToken(token!!)

                    val apiService = ApiConfig.getApiService()
                    val uploadImageRequest = apiService.uploadImage(generateToken, imageMultipart, description)

                    uploadImageRequest.enqueue(object : Callback<FileUploadResponse> {
                        override fun onResponse(
                            call: Call<FileUploadResponse>,
                            response: Response<FileUploadResponse>
                        ) {
                            if (response.isSuccessful) {
                                val responseBody = response.body()
                                if (responseBody != null && !responseBody.error) {
                                    Toast.makeText(this@AddStoryActivity, responseBody.message, Toast.LENGTH_SHORT).show()
                                    val intent = Intent(this@AddStoryActivity, ListStoryActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                    showLoading(false)
                                }
                            } else {
                                Toast.makeText(this@AddStoryActivity, response.message(), Toast.LENGTH_SHORT).show()
                            }
                        }
                        override fun onFailure(call: Call<FileUploadResponse>, t: Throwable) {
                            Toast.makeText(this@AddStoryActivity, t.message, Toast.LENGTH_SHORT).show()
                        }
                    })
                }

            } else {
                Toast.makeText(this@AddStoryActivity, "Silakan masukkan berkas gambar terlebih dahulu.", Toast.LENGTH_SHORT).show()
            }



        }

        // Mengamati hasil unggahan cerita dari ViewModel
        viewModel.uploadResult.observe(this, { result ->
            when (result) {
                is Result.Success -> {
                    Toast.makeText(this, "Story uploaded successfully", Toast.LENGTH_SHORT).show()
                    finish()
                }
                is Result.Failure -> {
                    val throwable = result.exception
                    Toast.makeText(this, "Failed to upload story: ${throwable.message}", Toast.LENGTH_SHORT).show()
                }
            }
        })

    }

    private fun initializeViews() {
        binding.cameraButton.setOnClickListener { startTakePhoto() }
        binding.galleryButton.setOnClickListener { startGallery() }
    }

    private fun startTakePhoto() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.resolveActivity(packageManager)

        createCustomTempFile(application).also {
            val photoURI: Uri = FileProvider.getUriForFile(
                this@AddStoryActivity,
                "com.app.dicodingstoryapp",
                it
            )
            currentPhotoPath = it.absolutePath
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            launcherIntentCamera.launch(intent)
        }
    }

    private fun startGallery() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        val chooser = Intent.createChooser(intent, "Choose a Picture")
        launcherIntentGallery.launch(chooser)
    }


    private lateinit var currentPhotoPath: String
    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            val myFile = File(currentPhotoPath)
            myFile.let { file ->
                getFile = file
                binding.previewImageView.setImageBitmap(BitmapFactory.decodeFile(file.path))
            }
        }
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedImg = result.data?.data as Uri

            selectedImg.let { uri ->
                val myFile = uriToFile(uri, this@AddStoryActivity)
                getFile = myFile
                binding.previewImageView.setImageURI(uri)
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}