package com.seljaki.agromajtermobile.fragments

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.seljaki.agromajtermobile.BlockAdapter
import com.seljaki.agromajtermobile.MyApplication
import com.seljaki.agromajtermobile.databinding.FragmentMainBinding
import com.seljaki.lib.Block
import com.seljaki.lib.Blockchain
import com.seljaki.lib.BlockchainClient
import java.io.ByteArrayOutputStream



class MainFragment : Fragment() {
    private lateinit var binding: FragmentMainBinding
    private lateinit var app: MyApplication
    private lateinit var blockAdapter: BlockAdapter
    private val CAMERA_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = requireActivity().application as MyApplication
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(layoutInflater, container, false)

        blockAdapter = BlockAdapter(app.blockchain.blocks)
        binding.blockchainRecyclerView.layoutManager = LinearLayoutManager(context)
        //val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.blockchainRecyclerView.adapter = blockAdapter

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.takePictureBtn.setOnClickListener{ launchCamera() }
        binding.openGalleryBtn.setOnClickListener{ openImage() }

        app.blockchainClient.onNewBlockReceived = { block ->
            requireActivity().runOnUiThread {
                blockAdapter.notifyItemInserted(app.blockchain.blocks.size - 1)
            }
        }

        app.blockchainClient.onBlockchainReceived = { blockchain ->
            requireActivity().runOnUiThread {
                blockAdapter.notifyDataSetChanged()
            }
        }

    }

    private fun openImage() {
        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }


    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            Log.d("PhotoPicker", "Selected URI: $uri")
            try {
                val contentResolver = requireContext().contentResolver
                val inputStream = contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                if (bitmap != null) {
                    app.imageToPredict = bitmap;
                    val action = MainFragmentDirections.actionMainFragmentToProcessImageFragment()
                    findNavController().navigate(action)
                } else {
                    Log.e("PhotoPicker", "Failed to decode Bitmap from URI.")
                }
            } catch (e: Exception) {
                Log.e("PhotoPicker", "Error processing image", e)
            }

        } else {
            Log.d("PhotoPicker", "No media selected")
        }
    }

    private fun launchCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (cameraIntent.resolveActivity(requireActivity().packageManager) != null) {
            startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val photo: Bitmap? = data?.extras?.get("data") as Bitmap?
            if (photo == null)
                return
            app.imageToPredict = photo
            val action = MainFragmentDirections.actionMainFragmentToProcessImageFragment()
            findNavController().navigate(action)
        }
    }
}