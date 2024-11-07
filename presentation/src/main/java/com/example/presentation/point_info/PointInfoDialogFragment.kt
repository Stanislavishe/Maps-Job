package com.example.presentation.point_info

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.entity.MapPoint
import com.example.entity.Points
import com.example.presentation.R
import com.example.presentation.UIState
import com.example.presentation.databinding.FragmentPointInfoBinding
import com.example.presentation.main.MainActivity.Companion.ADDRESS
import com.example.presentation.main.MainActivity.Companion.POINT
import com.example.presentation.main.MainActivity.Companion.REQUEST_PERMISSIONS
import com.example.presentation.main.MainViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class PointInfoDialogFragment : BottomSheetDialogFragment() {
    private var _binding: FragmentPointInfoBinding? = null
    private val binding get() = _binding!!

    private var address: String? = null
    private var mapPoint: MapPoint? = null
    private val availability = MutableStateFlow(false)
    private var pointId = 0

    private val dialog: AlertDialog by lazy {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.dialog_title))
            .setCancelable(true)
            .setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                dialog.cancel()
            }
            .create()
    }

    private val launcher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { map ->
        if (map.values.isNotEmpty() && map.values.all { it }) {
            onRoute()
            return@registerForActivityResult
        } else {
            dialog.show()
            Toast.makeText(requireContext(), getString(R.string.no_permmission), Toast.LENGTH_SHORT)
                .show()
        }
    }

    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        arguments?.let {
            address = it.getString(ADDRESS)
            mapPoint = it.getParcelable(POINT)
        }
        _binding = FragmentPointInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        getPoint()
        onDeletePoint()
        binding.navigateBtn.setOnClickListener {
            checkPermissions()
        }
        binding.address.text = address
    }

    private fun onDeletePoint() {
        binding.deleteBtn.setOnClickListener {
            if (availability.value) {
                viewModel.deletePoint(
                    Points(
                        id = pointId,
                        latitude = mapPoint!!.latitude,
                        longitude = mapPoint!!.longitude,
                        name = binding.name.text.toString()
                    )
                )
                viewModel.pointToDelete(mapPoint!!)
                availability.value = false
                dismiss()
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.please_await), Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun onRoute() {
        if (availability.value) {
            viewModel.shareRoutePoint(mapPoint!!)
            availability.value = false
            dismiss()
        } else {
            Toast.makeText(
                requireContext(),
                getString(R.string.please_await), Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun getPoint() {
        viewModel.getSinglePoint(mapPoint!!)

        viewModel.singlePoint.onEach {
            when (it) {
                is UIState.Initial -> {}
                is UIState.SingleSuccess -> {
                    val name = it.point.name
                    binding.name.text = if (name == "") getString(R.string.no_name) else name
                    pointId = it.point.id
                    availability.value = true
                }

                is UIState.Error -> {
                    binding.name.text = getString(R.string.no_name)
                }
            }
        }.launchIn(lifecycleScope)
    }

    private fun checkPermissions() {
        val isAllGranted = REQUEST_PERMISSIONS.all { permission ->
            ActivityCompat.checkSelfPermission(
                requireContext(),
                permission
            ) == PackageManager.PERMISSION_GRANTED
        }
        if (!isAllGranted) {
            launcher.launch(REQUEST_PERMISSIONS)
        } else {
            onRoute()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {
        const val POINT_INFO_TAG = "POINT_INFO_TAG"
    }
}