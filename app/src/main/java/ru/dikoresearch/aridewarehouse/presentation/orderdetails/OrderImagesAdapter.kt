package ru.dikoresearch.aridewarehouse.presentation.orderdetails

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.dikoresearch.aridewarehouse.R
import ru.dikoresearch.aridewarehouse.databinding.OrderImageItemBinding
import ru.dikoresearch.aridewarehouse.domain.entities.OrderImage
import ru.dikoresearch.aridewarehouse.presentation.utils.gone
import ru.dikoresearch.aridewarehouse.presentation.utils.visible

class OrderImagesAdapter(
    private val onAddImage: () -> Unit,
    private val onRemoveImage: (OrderImage) -> Unit,
    private val onImageClicked: (String) -> Unit
): RecyclerView.Adapter<OrderImagesAdapter.OrderImageViewHolder>(), View.OnClickListener {

    var listOfImages: List<OrderImage> = emptyList()
        @SuppressLint("NotifyDataSetChanged")
        set(newValue){
            field = newValue
            notifyDataSetChanged()
        }

    class OrderImageViewHolder(
        val binding: OrderImageItemBinding
    ): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderImageViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = OrderImageItemBinding.inflate(inflater, parent, false)
        binding.deleteImageBtn.setOnClickListener(this)
        binding.imageView.setOnClickListener(this)

        return OrderImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderImageViewHolder, position: Int) {
        val imageListItem = listOfImages[position]
        holder.binding.root.setOnClickListener(this@OrderImagesAdapter)
        holder.binding.imageView.tag = imageListItem
        holder.binding.deleteImageBtn.tag = imageListItem

        if (imageListItem.newImageActionHolder){
            holder.binding.deleteImageBtn.gone()
            Glide.with(holder.binding.imageView.context)
                .load(R.drawable.ic_baseline_add_24)
                .fitCenter()
                .into(holder.binding.imageView)
        }
        else {
            if (imageListItem.loaded){
                holder.binding.deleteImageBtn.gone()
            }
            else {
                holder.binding.deleteImageBtn.visible()
            }

            if (imageListItem.imageUri.isNotBlank()){
                Glide.with(holder.binding.imageView.context)
                    .load(imageListItem.imageUri)
                    .centerCrop()
                    .placeholder(R.drawable.airbag_spinner)
                    .error(R.drawable.ic_icon_airbag)
                    .into(holder.binding.imageView)
            }

        }

    }

    override fun getItemCount(): Int = listOfImages.size

    override fun onClick(p0: View) {
        val image = p0.tag as OrderImage
        when(p0.id){
            R.id.imageView -> {
                if (image.newImageActionHolder){
                    onAddImage()
                }
                else {
                    onImageClicked(image.imageUri)
                }
            }
            R.id.deleteImageBtn -> {
                onRemoveImage(image)
            }
            else -> {
            }
        }
    }
}