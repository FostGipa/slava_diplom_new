package com.example.slava.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.slava.R
import com.example.slava.databinding.ActivityChallengeAddFileBinding
import com.example.slava.utils.SupabaseClient
import kotlinx.coroutines.launch

class ChallengeAddFileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChallengeAddFileBinding
    private lateinit var _uri : Uri
    private var _fileName = ""
    private val supabaseClient = SupabaseClient()
    private var taskId = 0
    private var userId = 0
    private var challengeId = 0

    // Лаунчер для выбора одного файла
    private val filePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.data?.let { uri ->
                    handleSelectedFile(uri)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChallengeAddFileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        taskId = intent.getIntExtra("taskId", 0)
        userId = intent.getIntExtra("userId", 0)
        challengeId = intent.getIntExtra("challengeId", 0)

        binding.backButton.setOnClickListener { finish() }

        binding.addButton.setOnClickListener {
            lifecycleScope.launch {
                val response = supabaseClient.uploadUserTasksFiles(_uri, _fileName)
                response.onSuccess { url ->
                    supabaseClient.insertTestResult(
                        userId = userId,
                        taskId = taskId,
                        result = url,
                        challengeId = challengeId
                    )
                    startActivity(Intent(this@ChallengeAddFileActivity, AddFileResultActivity::class.java))
                    finish()
                }

            }
        }
        binding.fileAddLayout.setOnClickListener { openFilePicker() }
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "*/*"
        }
        filePickerLauncher.launch(intent)
    }

    private fun handleSelectedFile(uri: Uri) {
        val fileName = getFileName(uri) ?: "file_${System.currentTimeMillis()}"
        Log.d("FilePicker", "Выбран файл: $fileName - $uri")
        _fileName = fileName
        _uri = uri
        binding.plusTextView.visibility = View.GONE
        binding.addTextView.visibility = View.GONE
        binding.fileNameTextView.text = fileName.toString()
    }

    private fun getFileName(uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                }
            }
        }
        return result ?: uri.lastPathSegment
    }
}
