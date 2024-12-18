package com.dicoding.sambaserver.menubottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dicoding.sambaserver.databinding.BottomSheetMenuBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class MenuBottomSheet (private val onActionSelected: (ActionType) -> Unit) : BottomSheetDialogFragment() {
    private var _binding: BottomSheetMenuBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Add Folder Action
        binding.btnAddFolder.setOnClickListener {
            onActionSelected(ActionType.ADD_FOLDER)
            dismiss()
        }

        // Upload File Action
        binding.btnUploadFile.setOnClickListener {
            onActionSelected(ActionType.UPLOAD_FILE)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    enum class ActionType {
        ADD_FOLDER,
        UPLOAD_FILE
    }
}
