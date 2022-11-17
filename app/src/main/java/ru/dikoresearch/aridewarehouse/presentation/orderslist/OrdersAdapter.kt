package ru.dikoresearch.aridewarehouse.presentation.orderslist

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.dikoresearch.aridewarehouse.databinding.OrderListItemBinding
import ru.dikoresearch.aridewarehouse.domain.entities.OrdersListItem
import ru.dikoresearch.aridewarehouse.presentation.utils.getFormattedDateFromDataBaseDate

class OrdersAdapter(
    private val onItemClicked: (String) -> Unit
): RecyclerView.Adapter<OrdersAdapter.OrderViewHolder>(), View.OnClickListener {

    var listOfOrders: MutableList<OrdersListItem> = mutableListOf()
        @SuppressLint("NotifyDataSetChanged")
        set(value){
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {

        val inflater = LayoutInflater.from(parent.context)
        val binding = OrderListItemBinding.inflate(inflater, parent, false)

        binding.root.setOnClickListener(this)

        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = listOfOrders[position]
        holder.binding.root.tag = order

        with(holder.binding){
            orderNameView.text = order.orderName
            orderUserView.text = order.username
            orderCreatedAtView.text = order.createdAt.getFormattedDateFromDataBaseDate()
        }
    }

    override fun getItemCount(): Int = listOfOrders.size

    override fun onClick(p0: View) {
        val order = p0.tag as OrdersListItem
        onItemClicked(order.orderName)
    }

    class OrderViewHolder(
        val binding: OrderListItemBinding
    ): RecyclerView.ViewHolder(binding.root)
}