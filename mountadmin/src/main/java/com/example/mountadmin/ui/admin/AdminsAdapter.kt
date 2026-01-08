package com.example.mountadmin.ui.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mountadmin.R
import com.example.mountadmin.data.model.Admin
import com.example.mountadmin.databinding.ItemAdminBinding

class AdminsAdapter(
    private val onEditClick: (Admin) -> Unit,
    private val onToggleStatusClick: (Admin) -> Unit,
    private val onDeleteClick: (Admin) -> Unit
) : ListAdapter<Admin, AdminsAdapter.AdminViewHolder>(AdminDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminViewHolder {
        val binding = ItemAdminBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AdminViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AdminViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class AdminViewHolder(private val binding: ItemAdminBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(admin: Admin) {
            val context = binding.root.context

            val initials = admin.fullName.split(" ").take(2).mapNotNull { it.firstOrNull()?.uppercase() }.joinToString("")
            binding.tvAvatar.text = initials.ifEmpty { "?" }
            binding.tvName.text = admin.fullName
            binding.tvEmail.text = admin.email
            binding.tvRoleBadge.text = admin.getRoleDisplayName()

            when (admin.role) {
                Admin.ROLE_SUPER_ADMIN -> {
                    binding.tvRoleBadge.setBackgroundResource(R.drawable.bg_role_super_admin)
                    binding.tvRoleBadge.setTextColor(ContextCompat.getColor(context, R.color.role_super_admin))
                }
                Admin.ROLE_MOUNTAIN_ADMIN -> {
                    binding.tvRoleBadge.setBackgroundResource(R.drawable.bg_role_mountain_admin)
                    binding.tvRoleBadge.setTextColor(ContextCompat.getColor(context, R.color.role_mountain_admin))
                }
                Admin.ROLE_NEWS_ADMIN -> {
                    binding.tvRoleBadge.setBackgroundResource(R.drawable.bg_role_news_admin)
                    binding.tvRoleBadge.setTextColor(ContextCompat.getColor(context, R.color.role_news_admin))
                }
            }

            if (admin.isActive()) {
                binding.tvStatusBadge.text = context.getString(R.string.active)
                binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_status_active)
                binding.tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.status_active))
            } else {
                binding.tvStatusBadge.text = context.getString(R.string.disabled)
                binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_status_disabled)
                binding.tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.status_disabled))
            }

            binding.btnEdit.setOnClickListener { onEditClick(admin) }
            binding.btnToggleStatus.setOnClickListener { onToggleStatusClick(admin) }
            binding.btnDelete.setOnClickListener { onDeleteClick(admin) }
        }
    }

    class AdminDiffCallback : DiffUtil.ItemCallback<Admin>() {
        override fun areItemsTheSame(oldItem: Admin, newItem: Admin) = oldItem.uid == newItem.uid
        override fun areContentsTheSame(oldItem: Admin, newItem: Admin) = oldItem == newItem
    }
}

