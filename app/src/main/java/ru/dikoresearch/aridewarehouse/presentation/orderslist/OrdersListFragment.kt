package ru.dikoresearch.aridewarehouse.presentation.orderslist

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.dikoresearch.aridewarehouse.R
import ru.dikoresearch.aridewarehouse.databinding.FragmentOrdersListBinding
import ru.dikoresearch.aridewarehouse.presentation.utils.NavigationEvent
import ru.dikoresearch.aridewarehouse.presentation.utils.getAppComponent
import kotlin.properties.Delegates

class OrdersListFragment: Fragment(R.layout.fragment_orders_list) {

    private var binding: FragmentOrdersListBinding by Delegates.notNull()

    private val viewModel: OrdersListViewModel by viewModels{
        getAppComponent().viewModelFactory
    }

    private val ordersListAdapter: OrdersAdapter by lazy {
        OrdersAdapter(
            onItemClicked = { orderName ->
                viewModel.showOrderDetails(orderName)
            }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOrdersListBinding.inflate(inflater, container, false)

        showToolBarMenu()

        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.action_ordersListFragment_to_cameraScannerFragment)
        }

        binding.btnSearch.setOnClickListener{
            viewModel.search(binding.etSearch.text.toString())
        }

        val linearLayoutManager = LinearLayoutManager(requireContext())
        binding.ordersRecyclerView.layoutManager = linearLayoutManager
        binding.ordersRecyclerView.adapter = ordersListAdapter

        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.loadOrders()
        }

        binding.etSearch.setOnKeyListener { _, keyCode, keyEvent ->
            if ((keyEvent.action == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)){
                viewModel.search(binding.etSearch.text.toString())
                return@setOnKeyListener true
            }
            false
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launchWhenStarted {
            launch {
                viewModel.listOfOrders.collectLatest {
                    ordersListAdapter.listOfOrders = it.toMutableList()
                }
            }

            launch {
                viewModel.navigationEvent.collectLatest{
                    when(it){
                        is NavigationEvent.Navigate -> {
                            if (it.destination == "Login"){
                                findNavController().navigate(R.id.action_ordersListFragment_to_loginFragment)
                            }
                            else if (it.destination == "Details"){
                                findNavController().navigate(R.id.action_ordersListFragment_to_orderDetailsFragment, it.bundle)
                            }
                        }
                        is NavigationEvent.ShowToast -> {
                            Toast.makeText(requireActivity(), it.message, Toast.LENGTH_LONG).show()
                        }
                        else -> {

                        }
                    }
                }
            }

            launch {
                viewModel.showProgressBar.collectLatest {
                    binding.swipeRefreshLayout.isRefreshing = it
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadOrders()
    }

    private fun showToolBarMenu(){
        binding.ordersListToolbar.title = getString(R.string.list_of_orders)

        val menuHost: MenuHost = binding.ordersListToolbar

        menuHost.addMenuProvider(object: MenuProvider{
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_with_sort, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when(menuItem.itemId){
                    R.id.actionLogout -> {
                        showLogOutDialog()
                    }
                    R.id.clearFiles -> {
                        viewModel.clearFiles()
                    }
                    R.id.actionSortBy -> {
                        showSortingPopupMenu(requireActivity().findViewById(R.id.actionSortBy))
                    }
                    else -> {

                    }
                }

                return true
            }

        }, viewLifecycleOwner, Lifecycle.State.STARTED)
    }

    private fun showSortingPopupMenu(anchorView: View){
        val popupMenu = PopupMenu(requireActivity(), anchorView)
        popupMenu.inflate(R.menu.menu_sort_selection)
        popupMenu.setOnMenuItemClickListener {
            when(it.itemId){
                R.id.actionSortByName -> {
                    viewModel.sortByName()
                }
                R.id.actionSortByDate -> {
                    viewModel.sortByDate()
                }
                else -> {

                }
            }
            true
        }
        popupMenu.show()
    }

    private fun showLogOutDialog(){
        val dialogBuilder = AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.log_out_hint))
            .setPositiveButton(getString(R.string.ok)){_, _ ->
                viewModel.logout()
            }
            .setNegativeButton(R.string.cancel){ dialog, _ ->
                dialog.cancel()
            }

        dialogBuilder.show()

    }

    companion object {
        const val TAG = "List Of orders Fragment"
    }
}