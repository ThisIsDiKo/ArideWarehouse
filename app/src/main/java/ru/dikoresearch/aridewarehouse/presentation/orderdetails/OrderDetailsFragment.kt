package ru.dikoresearch.aridewarehouse.presentation.orderdetails

import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.work.WorkManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.dikoresearch.aridewarehouse.MainActivity
import ru.dikoresearch.aridewarehouse.R
import ru.dikoresearch.aridewarehouse.databinding.FragmentOrderDetailsBinding
import ru.dikoresearch.aridewarehouse.domain.entities.OrderImage
import ru.dikoresearch.aridewarehouse.presentation.camera.CameraViewModel
import ru.dikoresearch.aridewarehouse.presentation.utils.*
import java.io.File
import kotlin.properties.Delegates

class OrderDetailsFragment: Fragment(R.layout.fragment_order_details) {

    private var imageUri: Uri by Delegates.notNull()
    private var imagePath: String by Delegates.notNull()

    private var workManager: WorkManager by Delegates.notNull()

    private val viewModel: OrderDetailsViewModel by viewModels {
        getAppComponent().viewModelFactory
    }

    private val cameraViewModel: CameraViewModel by viewModels({activity as MainActivity }){
        getAppComponent().viewModelFactory
    }

    private var binding: FragmentOrderDetailsBinding by Delegates.notNull<FragmentOrderDetailsBinding>()
    private val adapter: OrderImagesAdapter by lazy {
        OrderImagesAdapter(
            onAddImage = {
                //takePicture(orderName)
                val bundle = bundleOf(
                    ORDER_NAME to orderName,
                    ALLOWED_NUMBER_OF_IMAGES to viewModel.getAllowedNumberOfImages()
                )
                         findNavController().navigate(R.id.action_orderDetailsFragment_to_cameraFragment, bundle)
            },
            onRemoveImage = {
                viewModel.removeImage(it)
            },
            onImageClicked = {
                val bundle = bundleOf(IMAGE_URL to it)
                findNavController().navigate(R.id.action_orderDetailsFragment_to_imagePreviewFragment, bundle)
            }

        )
    }
    private val orderName by lazy {
        arguments?.getString(ORDER_NAME, "Unknown") ?: "Unknown"
    }

    private val makePhoto = registerForActivityResult(ActivityResultContracts.TakePicture()){ result ->
        result?.let{
            if (it){
                val imageName = imageUri.toString().split("/").last()
                val image = OrderImage(
                    imageName = imageName,
                    loaded = false,
                    imageUri = imagePath,
                    newImageActionHolder = false
                )
                viewModel.addImage(image)
            }
            else {
                Toast.makeText(requireActivity(), "Error while storing image", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        workManager = WorkManager.getInstance(requireContext().applicationContext)
        binding = FragmentOrderDetailsBinding.inflate(inflater, container, false)

        binding.orderDetailsToolbar.setupWithNavController(findNavController(), AppBarConfiguration(findNavController().graph))

        val layoutManager = GridLayoutManager(requireContext(), 3)
        binding.orderImagesRecyclerView.layoutManager = layoutManager
        binding.orderImagesRecyclerView.adapter = adapter

        binding.orderUploadToServerBtn.setOnClickListener {
            viewModel.uploadToServer(
                orderName = orderName,
                comment = binding.orderCommentEditTextView.text.toString(),
                workManager = workManager
            )
        }

        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.loadOrder(orderName)
    }

    override fun onStart() {
        super.onStart()
        val l = cameraViewModel.getImagesPaths()
        Log.e("", "Got image paths from cameraViewModel: $l")
        if (l.isNotEmpty()){
            l.map {imagePath ->
                val orderImage = OrderImage(
                    imageName = imagePath.split("/").last(),
                    imageUri = imagePath,
                    loaded = false,
                    newImageActionHolder = false
                )
                viewModel.addImage(orderImage)
            }

        }
        cameraViewModel.refreshImagePaths()

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launchWhenStarted {
            launch {
                viewModel.showProgressBar.collectLatest {
                    if (it){
                        binding.orderDetailsProgressBar.visible()
                        binding.orderDetailsViewGroup.gone()
                    }
                    else {
                        binding.orderDetailsProgressBar.gone()
                        binding.orderDetailsViewGroup.visible()
                    }
                }
            }

            launch {
                viewModel.navigationEvent.collectLatest {
                    when(it){
                        is NavigationEvent.Navigate -> {
                            if (it.destination == "Details"){
                                findNavController().navigate(R.id.action_orderDetailsFragment_to_imagePreviewFragment, it.bundle)
                            }
                        }
                        else -> {

                        }
                    }
                }
            }

            launch {
                viewModel.orderDetailsState.collectLatest { state ->
                    binding.orderStatusTextView.text = if(state.status == "New") getString(R.string.new_order) else getString(R.string.published_order)
                    binding.orderNameTextView.text = state.orderName
                    binding.orderUsernameTextView.text = state.username
                    binding.orderCreatedAtTextView.text = state.createdAt
                    binding.orderCommentEditTextView.setText(state.comment)

                    binding.orderUploadToServerBtn.isEnabled = state.hasImagesToUpload

                    if (state.status != "New"){
                        binding.orderCommentEditTextView.isEnabled = false
                    }
                }
            }

            launch {
                viewModel.listOfImages.collectLatest {
                    adapter.listOfImages = it
                }
            }
        }
    }

    private fun takePicture(orderName: String){
        val photoFile = File.createTempFile(
            "IMG_${orderName}_",
            ".jpeg",
            requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        )

        imagePath = photoFile.absolutePath

        imageUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.provider",
            photoFile
        )

        makePhoto.launch(imageUri)
    }

    companion object {
        const val TAG = "Order Details Fragment"
    }
}