package com.dicoding.sambaserver.menubottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.dicoding.sambaserver.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class FileOptionsBottomSheet(
    private val fileName: String, // Tambahkan parameter nama file/folder
    private val fileIconResId: Int, // Tambahkan parameter ikon
    private val onDownloadClicked: (() -> Unit)?, // Nullable untuk download
    private val onDeleteClicked: () -> Unit
) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.file_options_menu, container, false)

        // Tampilkan nama file/folder
        val fileNameTextView = view.findViewById<TextView>(R.id.file_name)
        fileNameTextView.text = fileName

        // Tampilkan ikon file/folder
        val fileIconImageView = view.findViewById<ImageView>(R.id.file_icon)
        fileIconImageView.setImageResource(fileIconResId)

        // Set up Download button
        val downloadOption = view.findViewById<LinearLayout>(R.id.option_download)
        if (onDownloadClicked != null) {
            downloadOption.setOnClickListener {
                onDownloadClicked.invoke()
                dismiss()
            }
        } else {
            downloadOption.visibility = View.GONE // Sembunyikan jika folder
        }

        // Set up Delete button
        val deleteOption = view.findViewById<LinearLayout>(R.id.option_delete)
        deleteOption.setOnClickListener {
            onDeleteClicked()
            dismiss()
        }

        return view
    }
}
