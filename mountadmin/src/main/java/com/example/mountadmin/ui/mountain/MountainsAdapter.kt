package com.example.mountadmin.ui.mountain

import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.mountadmin.R
import com.example.mountadmin.data.model.Mountain
import com.example.mountadmin.databinding.ItemMountainAdminBinding

class MountainsAdapter(
    private val onEditClick: (Mountain) -> Unit,
    private val onDeleteClick: (Mountain) -> Unit
) : ListAdapter<Mountain, MountainsAdapter.MountainViewHolder>(MountainDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MountainViewHolder {
        val binding = ItemMountainAdminBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MountainViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MountainViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MountainViewHolder(
        private val binding: ItemMountainAdminBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(mountain: Mountain) {
            binding.tvName.text = mountain.name
            binding.tvLocation.text = "${mountain.province}, ${mountain.country}"
            binding.chipElevation.text = "${mountain.elevation} m"

            // Reset view state for recycled holders
            binding.progressImage.visibility = View.GONE
            binding.ivMountain.setImageDrawable(null)
            binding.ivMountain.setImageResource(R.drawable.ic_mountain_placeholder)

            val value = mountain.imageUrl.trim()
            Log.d("MountainsAdapter", "mountain=${mountain.id} image(len=${value.length})=${value.take(48)}")

            if (value.isBlank()) return

            // If Base64 (old pipeline), decode and show
            if (isLikelyBase64Image(value)) {
                val bytes = try {
                    Base64.decode(value.substringAfter(",", value), Base64.DEFAULT)
                } catch (_: IllegalArgumentException) {
                    null
                }

                if (bytes == null || bytes.isEmpty()) {
                    Log.w("MountainsAdapter", "Base64 decode failed mountain=${mountain.id}")
                    return
                }

                val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                if (bmp != null) {
                    binding.ivMountain.setImageBitmap(bmp)
                } else {
                    Log.w("MountainsAdapter", "Bitmap decode failed mountain=${mountain.id}")
                }
                return
            }

            // Otherwise treat as URL. Prefer HTTPS, but allow http(s) as fallback if needed.
            if (!value.startsWith("http", ignoreCase = true)) {
                Log.w("MountainsAdapter", "Unsupported imageUrl format mountain=${mountain.id}")
                return
            }

            binding.progressImage.visibility = View.VISIBLE
            Glide.with(binding.root)
                .load(value)
                .placeholder(R.drawable.ic_mountain_placeholder)
                .error(R.drawable.ic_mountain_placeholder)
                .centerCrop()
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.e("MountainsAdapter", "Glide load failed mountain=${mountain.id} url=$value", e)
                        binding.progressImage.visibility = View.GONE
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: Target<Drawable>?,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        binding.progressImage.visibility = View.GONE
                        return false
                    }
                })
                .into(binding.ivMountain)

            binding.btnEdit.setOnClickListener { onEditClick(mountain) }
            binding.btnDelete.setOnClickListener { onDeleteClick(mountain) }
        }

        private fun isLikelyBase64Image(v: String): Boolean {
            return v.startsWith("data:image", ignoreCase = true) ||
                (v.length > 200 && v.trim().matches(Regex("^[A-Za-z0-9+/=\\r\\n]+$")))
        }
    }

    class MountainDiffCallback : DiffUtil.ItemCallback<Mountain>() {
        override fun areItemsTheSame(oldItem: Mountain, newItem: Mountain): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Mountain, newItem: Mountain): Boolean {
            return oldItem == newItem
        }
    }
}
