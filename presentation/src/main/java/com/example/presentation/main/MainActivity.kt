package com.example.presentation.main

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.entity.MapPoint
import com.example.presentation.R
import com.example.presentation.UIState
import com.example.presentation.create_name.CreateNameDialogFragment
import com.example.presentation.create_name.CreateNameDialogFragment.Companion.TAG
import com.example.presentation.databinding.ActivityMainBinding
import com.example.presentation.point_info.PointInfoDialogFragment
import com.example.presentation.point_info.PointInfoDialogFragment.Companion.POINT_INFO_TAG
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.RequestPoint
import com.yandex.mapkit.RequestPointType
import com.yandex.mapkit.directions.DirectionsFactory
import com.yandex.mapkit.directions.driving.DrivingOptions
import com.yandex.mapkit.directions.driving.DrivingRoute
import com.yandex.mapkit.directions.driving.DrivingRouter
import com.yandex.mapkit.directions.driving.DrivingRouterType
import com.yandex.mapkit.directions.driving.DrivingSession
import com.yandex.mapkit.directions.driving.DrivingSession.DrivingRouteListener
import com.yandex.mapkit.directions.driving.VehicleOptions
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.InputListener
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.map.PolylineMapObject
import com.yandex.mapkit.user_location.UserLocationLayer
import com.yandex.runtime.Error
import com.yandex.runtime.image.ImageProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.Locale

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private var binding: ActivityMainBinding? = null

    private var map: Map? = null
    private val markerDataList = mutableListOf<PlacemarkMapObject>()
    private val mapTabListeners = mutableListOf<MapObjectTapListener>()
    private val inputListeners = mutableListOf<InputListener>()
    private lateinit var userLocation: UserLocationLayer
    private val drivingSessionList = mutableListOf<DrivingSession>()
    private val drivingRouter: DrivingRouter by lazy {
        DirectionsFactory.getInstance().createDrivingRouter(DrivingRouterType.ONLINE)
    }
    private val drivingOptions: DrivingOptions by lazy { DrivingOptions() }
    private val vehicleOptions: VehicleOptions by lazy { VehicleOptions() }
    private val polylineList = mutableListOf<MutableList<PolylineMapObject>>()

    private val dialog: AlertDialog by lazy {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.dialog_title))
            .setCancelable(true)
            .setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                dialog.cancel()
            }
            .create()
    }

    private val viewModel: MainViewModel by viewModels()

    private val launcher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { map ->
        if (map.values.isNotEmpty() && map.values.all { it }) {
            startLocalisation()
            return@registerForActivityResult
        } else {
            dialog.show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setApiKey(savedInstanceState)
        MapKitFactory.initialize(layoutInflater.context)
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding!!.root)
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        binding!!.mapView.onStart()

        map = binding!!.mapView.mapWindow.map
        checkPermissions()
        setInputListener()
        refreshPoints()
        observeDeletePoints()
        observeRouting()
    }

    override fun onStop() {
        MapKitFactory.getInstance().onStop()
        binding!!.mapView.onStop()
        super.onStop()
    }

    private fun refreshPoints() {
        map!!.mapObjects.clear()
        viewModel.getAllPoints()
        observeAllPoints()
    }

    private fun startRouting(point: MapPoint) {
        if (drivingSessionList.isNotEmpty()) {
            removePolyline()
        }
        val startPoint = userLocation.cameraPosition()?.target
        val endpoint = Point(point.latitude, point.longitude)
        val points = buildList {
            add(RequestPoint(startPoint!!, RequestPointType.WAYPOINT, null, null))
            add(RequestPoint(endpoint, RequestPointType.WAYPOINT, null, null))
        }

        val drivingRouteListener = object : DrivingRouteListener {
            override fun onDrivingRoutes(drivingRoutes: MutableList<DrivingRoute>) {
                val polyline = mutableListOf<PolylineMapObject>()
                for (route in drivingRoutes) {
                    polyline.add(map!!.mapObjects.addPolyline(route.geometry))
                }
                polylineList.add(polyline)
            }

            override fun onDrivingRoutesError(error: Error) {
                Toast.makeText(this@MainActivity, getString(R.string.error), Toast.LENGTH_SHORT)
                    .show()
            }
        }

        val drivingSession = drivingRouter.requestRoutes(
            points,
            drivingOptions,
            vehicleOptions,
            drivingRouteListener
        )
        drivingSessionList.add(drivingSession)
        onCancelDrivingSession()
    }

    private fun onCancelDrivingSession() {
        binding!!.backButton.visibility = View.VISIBLE
        binding!!.backButton.setOnClickListener {
            removePolyline()
            binding!!.backButton.visibility = View.GONE
        }
    }

    private fun removePolyline() {
        val session = drivingSessionList.last()
        val polyline = polylineList.last()
        session.cancel()
        polyline.forEach {
            map!!.mapObjects.remove(it)
        }
        drivingSessionList.remove(session)
        polylineList.remove(polyline)
    }

    private fun startLocalisation() {
        userLocation =
            MapKitFactory.getInstance().createUserLocationLayer(binding!!.mapView.mapWindow)
        userLocation.isVisible = true

        lifecycleScope.launch {
            do {
                delay(1000)
                val point = userLocation.cameraPosition()?.target
                if (point != null) {
                    moveToPosition(point)
                }
            } while (point == null)
        }
    }

    private fun setInputListener() {
        val inputListener = object : InputListener {
            override fun onMapTap(map: Map, point: Point) {}

            override fun onMapLongTap(map: Map, point: Point) {
                createMarkers(point)
                val dialog = CreateNameDialogFragment()
                val mapPoint = MapPoint(point.latitude, point.longitude)
                val bundle = Bundle().apply {
                    putParcelable(POINT, mapPoint)
                }
                dialog.arguments = bundle
                dialog.show(supportFragmentManager, TAG)
            }
        }
        inputListeners.add(inputListener)
        map!!.addInputListener(inputListeners.last())
    }

    private fun checkPermissions() {
        val isAllGranted = REQUEST_PERMISSIONS.all { permission ->
            ActivityCompat.checkSelfPermission(
                this,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        }
        if (!isAllGranted) {
            launcher.launch(REQUEST_PERMISSIONS)
        } else {
            startLocalisation()
        }
    }

    private fun moveToPosition(point: Point) {
        map!!.move(
            CameraPosition(point, 15f, 0f, 0f),
        )
    }

    private fun createMarkers(point: Point) {
        val marker = createBitmapFromVector(R.drawable.ic_pin_red_svg)
        val placemark = map!!.mapObjects.addPlacemark().apply {
            setIcon(ImageProvider.fromBitmap(marker))
            geometry = point
            opacity = 0.5f
        }
        setTapListener(placemark)
        markerDataList.add(placemark)
    }

    private fun setTapListener(placemark: PlacemarkMapObject) {
        val mapObjectTapListener = MapObjectTapListener { _, point ->
            val returnedAddress: String
            val geocoder = Geocoder(this, Locale.getDefault())
            try {
                val address = geocoder.getFromLocation(point.latitude, point.longitude, 1)
                returnedAddress = if (address != null) {
                    address[0].adminArea
                } else {
                    getString(R.string.address_not_found)
                }
                val mapPoint = MapPoint(
                    latitude = placemark.geometry.latitude,
                    longitude = placemark.geometry.longitude
                )
                val bundle = Bundle().apply {
                    putString(ADDRESS, returnedAddress)
                    putParcelable(POINT, mapPoint)
                }
                val pointInfo = PointInfoDialogFragment()
                pointInfo.arguments = bundle
                pointInfo.show(supportFragmentManager, POINT_INFO_TAG)
            } catch (e: Exception) {
                Toast.makeText(this, getString(R.string.error), Toast.LENGTH_SHORT).show()
            }
            true
        }
        mapTabListeners.add(mapObjectTapListener)
        placemark.addTapListener(mapTabListeners.last())
    }

    private fun observeAllPoints() {
        viewModel.allPoints.onEach {
            when (it) {
                is UIState.Initial -> {}
                is UIState.Error -> {
                    Toast.makeText(this, getString(R.string.error), Toast.LENGTH_SHORT).show()
                }
                is UIState.ListSuccess -> {
                    it.points.forEach { point ->
                        createMarkers(Point(point.latitude, point.longitude))
                    }
                }
            }
        }.launchIn(lifecycleScope)
    }

    private fun observeDeletePoints() {
        viewModel.deletePoint.onEach {
            when (it) {
                is UIState.Initial -> {}
                is UIState.Error -> {
                    Toast.makeText(this, getString(R.string.error), Toast.LENGTH_SHORT)
                        .show()
                }
                is UIState.DeleteSuccess -> {
                    Toast.makeText(
                        this,
                        getString(R.string.delete_point_success), Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }.launchIn(lifecycleScope)
        viewModel.pointToDelete.onEach { point ->
            if (point != null) {
                var marker: PlacemarkMapObject? = null
                markerDataList.forEach {
                    if (it.geometry.latitude == point.latitude && it.geometry.longitude == point.longitude) {
                        map!!.mapObjects.remove(it)
                        marker = it
                    }
                }
                if (marker != null) {
                    markerDataList.remove(marker)
                    marker = null
                }
            }
        }.launchIn(lifecycleScope)
    }

    private fun observeRouting() {
        viewModel.routePoint.onEach {
            if (it != null) startRouting(it)
        }.launchIn(lifecycleScope)
    }

    private fun setApiKey(savedInstanceState: Bundle?) {
        val haveApiKey = savedInstanceState?.getBoolean("haveApiKey") ?: false
        if (!haveApiKey) {
            MapKitFactory.setApiKey(MAP_KEY)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("haveApiKey", true)
    }

    private fun createBitmapFromVector(art: Int): Bitmap? {
        val drawable = ContextCompat.getDrawable(this, art) ?: return null
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    companion object {
        private const val MAP_KEY = "76733c6f-4984-4ce4-9dc7-aad6ace210f3"
        val REQUEST_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        const val ADDRESS = "ADDRESS"
        const val POINT = "POINT"
    }
}