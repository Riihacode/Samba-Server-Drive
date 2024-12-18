package com.dicoding.sambaserver.main.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dicoding.sambaserver.R

class FileListAdapter(
    private val onFileClick: (String) -> Unit,
    private val onDeleteClick: (String) -> Unit,
    private val onDownloadClick: (String) -> Unit
) : ListAdapter<String, FileListAdapter.FileViewHolder>(FileDiffCallback()) {

    inner class FileViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val fileIcon: ImageView = view.findViewById(R.id.file_icon)
        private val fileName: TextView = view.findViewById(R.id.file_name)
        private val deleteButton: ImageButton = view.findViewById(R.id.delete_button)
        private val downloadButton: ImageButton = view.findViewById(R.id.download_button)

        fun bind(fileName: String) {
            this.fileName.text = fileName
            fileIcon.setImageResource(if (fileName.endsWith("/")) R.drawable.ic_folder else R.drawable.ic_file)

            deleteButton.setOnClickListener { onDeleteClick(fileName) }
            downloadButton.setOnClickListener { onDownloadClick(fileName) }
            itemView.setOnClickListener { onFileClick(fileName) }
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
