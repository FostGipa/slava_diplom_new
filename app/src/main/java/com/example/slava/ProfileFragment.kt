package com.example.slava

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val view = binding.root

        Glide.with(this)
            .load(R.drawable.profile_avatar_bg)
            .apply(RequestOptions.bitmapTransform(CircleCrop()))
            .into(binding.profileImageView)

        binding.dateEditText.setOnClickListener {
            showDatePickerDialog()
        }

        binding.updateButton.setOnClickListener{
            lifecycleScope.launch {
                supabaseClient.updateUser(requireContext(), supabaseClient.getUserById(supabaseClient.getToken(requireContext())
                    .toString())!!.id!!, binding.nameEditText.text.toString(), binding.phoneEditText.text.toString(), binding.dateEditText.text.toString())
            }
        }
        return view
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