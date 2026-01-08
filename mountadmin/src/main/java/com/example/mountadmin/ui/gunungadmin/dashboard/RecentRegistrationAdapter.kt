package com.example.mountadmin.ui.gunungadmin.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mountadmin.R
import com.example.mountadmin.data.model.Registration
import com.example.mountadmin.databinding.ItemRecentRegistrationBinding
import java.text.SimpleDateFormat
import java.util.Locale

class RecentRegistrationAdapter : ListAdapter<Registration, RecentRegistrationAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRecentRegistrationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: ItemRecentRegistrationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())

        fun bind(item: Registration) {
            binding.tvHikerName.text = item.fullName
            binding.tvHikerRoute.text = "Via ${item.route}"
            binding.tvHikerDate.text = dateFormat.format(item.createdAt.toDate())
            binding.tvHikerStatus.text = item.status

            // Set initials for avatar
            val initials = item.fullName.split(" ")
                .take(2)
                .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                .joinToString("")
            binding.tvAvatarInitials.text = initials

            // Set status badge color
            val (bgRes, textColor) = when (item.status) {
                Registration.STATUS_PENDING -> Pair(R.drawable.bg_status_pending, R.color.status_pending)
                Registration.STATUS_APPROVED -> Pair(R.drawable.bg_status_approved, R.color.status_approved)
                Registration.STATUS_REJECTED -> Pair(R.drawable.bg_status_rejected, R.color.status_rejected)
                else -> Pair(R.drawable.bg_status_pending, R.color.status_pending)
            }
            binding.tvHikerStatus.setBackgroundResource(bgRes)
            binding.tvHikerStatus.setTextColor(binding.root.context.getColor(textColor))
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Registration>() {
        override fun areItemsTheSame(oldItem: Registration, newItem: Registration): Boolean {
            return oldItem.registrationId == newItem.registrationId
        }

        override fun areContentsTheSame(oldItem: Registration, newItem: Registration): Boolean {
            return oldItem == newItem
        }
    }
}

