package ru.dikoresearch.aridewarehouse.presentation.imagepreview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.viewpager2.widget.ViewPager2
import ru.dikoresearch.aridewarehouse.R
import ru.dikoresearch.aridewarehouse.databinding.FragmentImagePreviewBinding
import ru.dikoresearch.aridewarehouse.presentation.utils.IMAGES_URLS_ARRAY
import ru.dikoresearch.aridewarehouse.presentation.utils.IMAGE_URL
import kotlin.properties.Delegates

class ImagePreviewFragment: Fragment(R.layout.fragment_image_preview) {

    private var binding: FragmentImagePreviewBinding by Delegates.notNull()

    private val imagesUrls: List<String> by lazy {
        arguments?.getStringArray(IMAGES_URLS_ARRAY)?.toList() ?: emptyList()
    }

    private val selectedImageUrl: String by lazy {
        arguments?.getString(IMAGE_URL) ?: "000"
    }

    private val viewPagerAdapter: ImageViewPagerAdapter by lazy {
        ImageViewPagerAdapter(imagesUrls)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentImagePreviewBinding.inflate(inflater, container, false)

        binding.imagePreviewToolbar.setupWithNavController(findNavController(), AppBarConfiguration(findNavController().graph))

        binding.imageViewPager.adapter = viewPagerAdapter
        binding.imageViewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        val index = imagesUrls.indexOf(selectedImageUrl)
        binding.imageViewPager.currentItem = if (index < 0) 0 else index



        binding.imageViewPager.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback(){
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                }
            }
        )

        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.imageViewPager.unregisterOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {}
        )
    }
}