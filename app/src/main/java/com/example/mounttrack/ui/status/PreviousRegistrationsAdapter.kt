package com.example.mounttrack.ui.status

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mounttrack.R
import com.example.mounttrack.data.model.Registration
import com.example.mounttrack.databinding.ItemPreviousRegistrationBinding
import com.example.mounttrack.utils.DateUtils

class PreviousRegistrationsAdapter : ListAdapter<Registration, PreviousRegistrationsAdapter.RegistrationViewHolder>(
    RegistrationDiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RegistrationViewHolder {
        val binding = ItemPreviousRegistrationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RegistrationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RegistrationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class RegistrationViewHolder(
        private val binding: ItemPreviousRegistrationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(registration: Registration) {
            binding.tvMountainName.text = registration.mountainName
            binding.tvDate.text = DateUtils.formatDate(registration.createdAt)

            // Set status badge
            val context = binding.root.context
            when (registration.status) {
                Registration.STATUS_APPROVED -> {
                    binding.tvStatus.text = "● ${context.getString(R.string.approved)}"
                    binding.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.status_approved))
                    binding.tvStatus.setBackgroundResource(R.drawable.bg_status_approved)
                }
                Registration.STATUS_REJECTED -> {
                    binding.tvStatus.text = "● ${context.getString(R.string.rejected)}"
                    binding.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.status_rejected))
                    binding.tvStatus.setBackgroundResource(R.drawable.bg_status_rejected)
                }
                else -> {
                    binding.tvStatus.text = "● ${context.getString(R.string.pending)}"
                    binding.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.status_pending))
                    binding.tvStatus.setBackgroundResource(R.drawable.bg_status_pending)
                }
            }
        }
    }

    class RegistrationDiffCallback : DiffUtil.ItemCallback<Registration>() {
        override fun areItemsTheSame(oldItem: Registration, newItem: Registration): Boolean {
            return oldItem.registrationId == newItem.registrationId
        }

        override fun areContentsTheSame(oldItem: Registration, newItem: Registration): Boolean {
            return oldItem == newItem
        }
    }
}

