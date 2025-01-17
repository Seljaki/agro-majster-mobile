package com.seljaki.agromajtermobile.fragments

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat.getDrawable
import androidx.navigation.fragment.navArgs
import com.seljaki.agromajtermobile.MyApplication
import com.seljaki.agromajtermobile.R
import com.seljaki.agromajtermobile.databinding.FragmentMapsBinding
import com.seljaki.lib.Blockchain
import org.osmdroid.config.Configuration
import org.osmdroid.library.BuildConfig
import org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow
import java.util.UUID

class MapsFragment : Fragment() {
    lateinit var binding: FragmentMapsBinding
    lateinit var app: MyApplication
    private val args : MapsFragmentArgs by navArgs()

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
        val map = binding.mapView
        val points: MutableList<GeoPoint> = mutableListOf()
        val blocks = app.blockchain.blocks;
        map.overlays.clear()

        for(block in blocks) {
            if(block.index == 0) continue
            val geoPoint = GeoPoint(block.data.latitude, block.data.longitude)
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

        if (args.blockchainIndex != -1) {
            map.setExpectedCenter(points[args.blockchainIndex])
        } else if (points.isNotEmpty()) {
            val boundingBox = BoundingBox.fromGeoPointsSafe(points)
            //map.setExpectedCenter(points.get(0))
            //map.zoomToBoundingBox(boundingBox, true) // Adjusts zoom level to fit all markers
        }
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