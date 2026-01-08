package com.example.mountadmin.ui.gunungadmin.registration

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mountadmin.R
import com.example.mountadmin.data.model.Registration
import com.example.mountadmin.databinding.ItemGunungAdminRegistrationBinding
import java.text.SimpleDateFormat
import java.util.Locale

class GunungAdminRegistrationAdapter(
    private val onApproveClick: (Registration) -> Unit,
    private val onRejectClick: (Registration) -> Unit,
    private val onViewIdCardClick: (Registration) -> Unit
) : ListAdapter<Registration, GunungAdminRegistrationAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemGunungAdminRegistrationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding, onApproveClick, onRejectClick, onViewIdCardClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemGunungAdminRegistrationBinding,
        private val onApproveClick: (Registration) -> Unit,
        private val onRejectClick: (Registration) -> Unit,
        private val onViewIdCardClick: (Registration) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())

        fun bind(item: Registration) {
            binding.tvHikerName.text = item.fullName
            binding.tvHikerPhone.text = item.phone
            binding.tvHikingDate.text = dateFormat.format(item.createdAt.toDate())
            binding.tvHikingRoute.text = item.route

            // Show/hide action buttons based on status
            val isPending = item.status == Registration.STATUS_PENDING
            binding.btnApprove.visibility = if (isPending) View.VISIBLE else View.GONE
            binding.btnReject.visibility = if (isPending) View.VISIBLE else View.GONE

            // Set status indicator if not pending
            if (!isPending) {
                binding.tvStatus.visibility = View.VISIBLE
                binding.tvStatus.text = item.status
                when (item.status) {
                    Registration.STATUS_APPROVED -> {
                        binding.tvStatus.setBackgroundResource(R.drawable.bg_status_approved)
                        binding.tvStatus.setTextColor(binding.root.context.getColor(R.color.status_approved))
                    }
                    Registration.STATUS_REJECTED -> {
                        binding.tvStatus.setBackgroundResource(R.drawable.bg_status_rejected)
                        binding.tvStatus.setTextColor(binding.root.context.getColor(R.color.status_rejected))
                    }
                }
            } else {
                binding.tvStatus.visibility = View.GONE
            }

            // Click listeners
            binding.btnViewIdCard.setOnClickListener { onViewIdCardClick(item) }
            binding.btnApprove.setOnClickListener { onApproveClick(item) }
            binding.btnReject.setOnClickListener { onRejectClick(item) }
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

