package com.example.presentation.create_name

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.entity.MapPoint
import com.example.entity.Points
import com.example.presentation.R
import com.example.presentation.UIState
import com.example.presentation.databinding.FragmentCreateNameDialogBinding
import com.example.presentation.main.MainActivity.Companion.POINT
import com.example.presentation.main.MainViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class CreateNameDialogFragment : BottomSheetDialogFragment() {
    private var _binding: FragmentCreateNameDialogBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels()

    private var mapPoint: MapPoint? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mapPoint = it.getParcelable(POINT)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateNameDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        onClick(binding.background)
        onClick(binding.applyBtn)
        observeInsertPoint()
    }

    private fun observeInsertPoint() {
        viewModel.insertPoint.onEach {
            when (it) {
                is UIState.Initial -> {}
                is UIState.InsertSuccess -> {
                    Toast.makeText(
                        requireActivity(),
                        getString(R.string.add_point_access), Toast.LENGTH_SHORT
                    ).show()
                    dismiss()
                }
                else -> {
                    Toast.makeText(requireActivity(), getString(R.string.error), Toast.LENGTH_SHORT)
                        .show()
                    dismiss()
                }
            }
        }.launchIn(lifecycleScope)
    }

    private fun onClick(btn: View) {
        btn.setOnClickListener {
            viewModel.insertPoint(
                Points(
                    latitude = mapPoint!!.latitude,
                    longitude = mapPoint!!.longitude,
                    name = binding.editName.text?.toString()
                )
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {
        const val TAG = "CreateNameDialogFragment"
    }
}