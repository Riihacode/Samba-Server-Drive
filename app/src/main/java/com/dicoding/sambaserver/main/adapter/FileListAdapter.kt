package com.dicoding.sambaserver.main.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dicoding.sambaserver.R
import com.dicoding.sambaserver.menubottomsheet.FileOptionsBottomSheet

class FileListAdapter(
    private val onItemClick: (String) -> Unit,
    private val onDeleteClick: (String) -> Unit,
    private val onDownloadClick: (String) -> Unit?
) : ListAdapter<String, FileListAdapter.FileViewHolder>(FileDiffCallback()) {

    inner class FileViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val itemIcon: ImageView = view.findViewById(R.id.item_icon)
        private val itemName: TextView = view.findViewById(R.id.item_name)
        private val overflowMenu: ImageButton = view.findViewById(R.id.overflow_menu)

        fun bind(itemName: String) {
            this.itemName.text = itemName
            val isFolder = itemName.endsWith("/") // Cek apakah ini folder

            this.itemName.text = itemName
            itemIcon.setImageResource(if (itemName.endsWith("/")) R.drawable.ic_folder else R.drawable.ic_file)

            // Set ikon pada item
            val iconResId = if (isFolder) R.drawable.ic_folder else R.drawable.ic_file
            itemIcon.setImageResource(iconResId)

            // Handle click on item
            itemView.setOnClickListener { onItemClick(itemName) }

            // Handle overflow menu click
            overflowMenu.setOnClickListener {
                val context = it.context
                //val isFolder = itemName.endsWith("/") // Cek apakah ini folder
                val bottomSheet = FileOptionsBottomSheet(
                    fileName = itemName,
                    fileIconResId = iconResId,
                    onDownloadClicked = if (!isFolder) {
                        { onDownloadClick(itemName) } // Hanya berikan aksi jika bukan folder
                    } else null, // Tidak ada aksi untuk folder
                    onDeleteClicked = { onDeleteClick(itemName) }
                )
                if (context is AppCompatActivity) {
                    bottomSheet.show(context.supportFragmentManager, bottomSheet.tag)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_file, parent, false)
        return FileViewHolder(view)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class FileDiffCallback : DiffUtil.ItemCallback<String>() {
    override fun areItemsTheSame(oldItem: String, newItem: String) = oldItem == newItem
    override fun areContentsTheSame(oldItem: String, newItem: String) = oldItem == newItem
}