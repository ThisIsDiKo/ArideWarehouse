package ru.dikoresearch.aridewarehouse.presentation.imagepreview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.dikoresearch.aridewarehouse.R
import ru.dikoresearch.aridewarehouse.databinding.ViewpagerImageItemBinding

class ImageViewPagerAdapter(
    private val imagesUrlsList: List<String>
): RecyclerView.Adapter<ImageViewPagerAdapter.ImageViewPagerViewHolder>()  {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewPagerViewHolder {
        val binding = ViewpagerImageItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ImageViewPagerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewPagerViewHolder, position: Int) {
        holder.setData(imagesUrlsList[position])
    }

    override fun getItemCount(): Int = imagesUrlsList.size

    inner class ImageViewPagerViewHolder(
        private val binding: ViewpagerImageItemBinding
    ): RecyclerView.ViewHolder(binding.root){
        fun setData(imageUrl: String){
            Glide.with(binding.root.context)
                .load(imageUrl)
                .centerCrop()
                .error(R.drawable.ic_icon_airbag)
                .placeholder(R.drawable.airbag_spinner)
                .into(binding.viewPagerImageView)
        }
    }
}