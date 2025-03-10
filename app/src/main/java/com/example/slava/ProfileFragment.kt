package com.example.slava

import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.example.slava.databinding.FragmentProfileBinding
import com.example.slava.utils.SupabaseClient
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val calendar = Calendar.getInstance()
    private val supabaseClient : SupabaseClient = SupabaseClient()
    private var currentUserId: Int? = null
    private var currentUID: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val view = binding.root

        loadUserData()

        binding.dateEditText.setOnClickListener {
            showDatePickerDialog()
        }

        binding.updateButton.setOnClickListener {
            updateUser()
        }

        val imagePickerLauncher = registerForActivityResult(
            ActivityResultContracts.PickVisualMedia()
        ) { uri: Uri? ->
            if (uri != null) {
                lifecycleScope.launch {
                    val result = supabaseClient.uploadUserAvatar(uri, currentUID.toString())
                    result.onSuccess { imageUrl ->
                        Toast.makeText(requireContext(), "Фото обновлено!", Toast.LENGTH_SHORT).show()
                        Glide.with(requireContext())
                            .load(uri)
                            .apply(RequestOptions.bitmapTransform(CircleCrop()))
                            .into(binding.profileImageView)
                    }.onFailure {
                        Toast.makeText(requireContext(), "Ошибка загрузки: ${it.message}", Toast.LENGTH_SHORT).show()
                        Log.e("123", it.message.toString())
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Ошибка", Toast.LENGTH_SHORT).show()
            }
        }

        binding.profileImageView.setOnClickListener {
            val builder = PickVisualMediaRequest.Builder()
            val instance = builder.setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly)
            imagePickerLauncher.launch(instance.build())
        }
        return view
    }

    private fun loadUserData() {
        binding.linearLayout.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            val token = supabaseClient.getToken(requireContext()).toString()
            val response = supabaseClient.getUserById(token)

            response.onSuccess { user ->
                currentUserId = user.id_user
                currentUID = user.uid
                val response2 = supabaseClient.downloadUserAvatar(user.uid)
                response2.onSuccess { img ->
                    Glide.with(requireContext())
                        .load(img)
                        .apply(RequestOptions.bitmapTransform(CircleCrop()))
                        .into(binding.profileImageView)
                }.onFailure { error ->
                    Toast.makeText(requireContext(), "Ошибка", Toast.LENGTH_SHORT).show()
                }

                binding.apply {
                    nameEditText.setText(user.name)
                    phoneEditText.setText(user.phone)
                    dateEditText.setText(user.date_of_birth)
                }
            }.onFailure {
                Toast.makeText(requireContext(), "Ошибка получения пользователя!", Toast.LENGTH_SHORT).show()
            }
            binding.progressBar.visibility = View.GONE
            binding.linearLayout.visibility = View.VISIBLE
        }
    }

    private fun updateUser() {
        lifecycleScope.launch {
            currentUserId?.let { userId ->
                val editTexts = listOf(binding.nameEditText, binding.phoneEditText, binding.dateEditText)
                val passwordText = binding.passwordEditText.text.toString().trim()

                var allFieldsFilled = true
                var otherFieldsChanged = false

                editTexts.forEach { editText ->
                    if (editText.text.isEmpty()) {
                        editText.setBackgroundResource(R.drawable.custom_error_edittext)
                        allFieldsFilled = false
                    }
                    if (editText.text.toString() != editText.hint.toString()) {
                        otherFieldsChanged = true
                    }
                }

                val isPasswordChanged = passwordText.isNotEmpty()

                // Если пароль изменяется, он должен быть не менее 6 символов
                if (isPasswordChanged && passwordText.length < 6) {
                    binding.passwordEditText.setBackgroundResource(R.drawable.custom_error_edittext)
                    Toast.makeText(requireContext(), "Пароль должен содержать минимум 6 символов", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                if (allFieldsFilled || isPasswordChanged) {
                    val response = supabaseClient.updateUser(
                        userId,
                        binding.nameEditText.text.toString(),
                        binding.phoneEditText.text.toString(),
                        binding.dateEditText.text.toString(),
                        if (isPasswordChanged) passwordText else null // Отправляем пароль только если он изменился
                    )

                    response.onSuccess {
                        Toast.makeText(requireContext(), "Данные обновлены!", Toast.LENGTH_SHORT).show()
                    }.onFailure { error ->
                        Toast.makeText(requireContext(), "Ошибка обновления: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Вы не изменили данные!", Toast.LENGTH_SHORT).show()
                }
            } ?: Toast.makeText(requireContext(), "Ошибка: ID пользователя не найден!", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showDatePickerDialog() {
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            dateSetListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private val dateSetListener =
        DatePickerDialog.OnDateSetListener { _: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, monthOfYear)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInView()
        }

    private fun updateDateInView() {
        val simpleDateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        binding.dateEditText.setText(simpleDateFormat.format(calendar.time))
    }
}