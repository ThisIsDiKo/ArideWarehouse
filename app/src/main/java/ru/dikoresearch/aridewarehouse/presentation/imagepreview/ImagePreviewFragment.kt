package ru.dikoresearch.aridewarehouse.presentation.imagepreview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import ru.dikoresearch.aridewarehouse.R
import ru.dikoresearch.aridewarehouse.databinding.FragmentImagePreviewBinding
import ru.dikoresearch.aridewarehouse.presentation.utils.IMAGE_URL
import kotlin.properties.Delegates

class ImagePreviewFragment: Fragment(R.layout.fragment_image_preview) {

    private var binding: FragmentImagePreviewBinding by Delegates.notNull()

    private val imageUrl: String by lazy {
        arguments?.getString(IMAGE_URL, "Unknown") ?: "Unknown"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentImagePreviewBinding.inflate(inflater, container, false)

        binding.imagePreviewToolbar.setupWithNavController(findNavController(), AppBarConfiguration(findNavController().graph))

        Glide.with(binding.imagePreviewView.context)
            .load(imageUrl)
            .fitCenter()
            .placeholder(R.drawable.airbag_spinner)
            .error(R.drawable.ic_icon_airbag)
            .into(binding.imagePreviewView)

        return binding.root
    }
}