package com.example.collegefixit.guardactivities

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.collegefixit.R
import com.example.collegefixit.adapter.GuardComplaintsAdapter
import com.example.collegefixit.databinding.ActivityGuardMainBinding
import com.example.collegefixit.viewmodel.ComplaintViewModel
import com.google.firebase.messaging.FirebaseMessaging

class GuardMainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGuardMainBinding
    private lateinit var viewModel: ComplaintViewModel
    private lateinit var complaintsAdapter: GuardComplaintsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        subscribeToGuardsTopic()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_guard_main)

        // Initialize ViewModel
        viewModel = ViewModelProvider(this).get(ComplaintViewModel::class.java)

        // Set up RecyclerView
        binding.complaintsRecyclerView.layoutManager = LinearLayoutManager(this)
        complaintsAdapter = GuardComplaintsAdapter()
        binding.complaintsRecyclerView.adapter = complaintsAdapter

        // Observe complaints from ViewModel
        viewModel.complaints.observe(this) { complaints ->
            complaintsAdapter.submitList(complaints)
        }

        // Observe error messages
        viewModel.errorMessage.observe(this) { errorMessage ->
            // Show error message to the user (e.g., using a Toast or Snackbar)
        }
    }
    private fun subscribeToGuardsTopic() {
        FirebaseMessaging.getInstance().subscribeToTopic("guards_notifications")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("FCM", "Guard subscribed to notifications topic")
                } else {
                    Log.e("FCM", "Failed to subscribe guard to notifications topic", task.exception)
                }
            }
    }
}