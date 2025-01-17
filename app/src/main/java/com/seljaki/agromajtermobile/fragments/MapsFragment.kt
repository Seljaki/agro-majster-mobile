package com.seljaki.agromajtermobile.fragments

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.core.content.res.ResourcesCompat.getDrawable
import androidx.fragment.app.Fragment
import com.seljaki.agromajtermobile.MyApplication
import com.seljaki.agromajtermobile.R
import com.seljaki.agromajtermobile.databinding.FragmentMapsBinding
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.overlay.Marker
import java.time.Instant
import java.util.Date
import kotlin.math.log

class MapsFragment : Fragment(), AdapterView.OnItemSelectedListener {
    lateinit var binding: FragmentMapsBinding
    lateinit var app: MyApplication
    var selectedTimeFrame: Long = 0
    val points: MutableList<GeoPoint> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        app = requireActivity().application as MyApplication
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMapsBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showDropdown()
        setupMap()
        showMarkers()
    }

    private fun setupMap() {
        val map = binding.mapView

        Configuration.getInstance().userAgentValue = requireContext().getPackageName()
        if(!Build.PRODUCT.startsWith("sdk"))
            map.zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
        map.setMultiTouchControls(true)
        map.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE)
        map.setZoomLevel(10.0)
        map.setExpectedCenter(GeoPoint(46.056946, 14.505751))
    }

    private fun showMarkers() {
        points.clear()
        val map = binding.mapView
        val blocks = app.blockchain.blocks;
        map.overlays.clear()
        for(block in blocks) {
            Log.d("MapsFragment", "Processing block: index=${block.index}, timestamp=${block.timestamp}, selectedTimeFrame=$selectedTimeFrame")
            if(block.index == 0 || (block.timestamp < selectedTimeFrame && selectedTimeFrame != 0L)) continue
            val geoPoint = GeoPoint(block.data.longitude, block.data.latitude)
            points.add(geoPoint)
            val marker = Marker(map)
            marker.position = geoPoint
            marker.title = "Block Info"
            marker.snippet = "Latitude: ${block.data.latitude}, Longitude: ${block.data.longitude}"
            //marker.infoWindow = MarkerInfoWindow(R.layout.bonuspack_bubble, map) // Optional, customize if needed
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
            marker.icon = when(block.data.prediction.getPredicted()) {
                "clear" -> getDrawable(getResources(), R.drawable.sunny, requireActivity().theme);
                "cloudy" -> getDrawable(getResources(), R.drawable.cloudy, requireActivity().theme);
                else -> getDrawable(getResources(), R.drawable.rainy, requireActivity().theme);
            }

            // Add the marker to the map
            map.overlays.add(marker)
        }

        if (points.isNotEmpty()) {
            val boundingBox = BoundingBox.fromGeoPointsSafe(points)
            map.setExpectedCenter(points.get(0))
            //map.zoomToBoundingBox(boundingBox, true) // Adjusts zoom level to fit all markers
        }
        map.invalidate()
    }

    private fun showDropdown(){
        val spinner: Spinner = binding.spinner

        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.dates_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }
        spinner.onItemSelectedListener = this
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
        val selectedItem = parent.getItemAtPosition(pos).toString()
        val date = Instant.now().epochSecond
        val oneDayInMillis: Long = 24L * 60L * 60L
        when (selectedItem) {
            "Last day" -> {
                selectedTimeFrame = date - oneDayInMillis
                //points.clear()
                showMarkers()
            }
            "Last week" -> {
                selectedTimeFrame = date - 7 * oneDayInMillis
                //points.clear()
                showMarkers()
            }
            "Last month" -> {
                selectedTimeFrame = date - 30 * oneDayInMillis
                //points.clear()
                showMarkers()
            }
            else -> {
                selectedTimeFrame = 0
                //points.clear()
                showMarkers()
            }
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>) {
    }

    override fun onResume() {
        super.onResume()

        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()

        binding.mapView.onPause()
    }


}