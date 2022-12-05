package ru.dikoresearch.aridewarehouse.presentation.login

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.dikoresearch.aridewarehouse.R
import ru.dikoresearch.aridewarehouse.databinding.FragmentLoginBinding
import ru.dikoresearch.aridewarehouse.presentation.utils.*

class LoginFragment: Fragment(R.layout.fragment_login) {
    private lateinit var binding: FragmentLoginBinding

    private val viewModel: LogInViewModel by viewModels{
        getAppComponent().viewModelFactory
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentLoginBinding.inflate(inflater, container, false)

        binding.etPassword.setOnKeyListener { _, keyCode, event ->
            if ((event.action == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)){
                loginAction()
                return@setOnKeyListener true
            }
            return@setOnKeyListener false
        }

        binding.loginBtn.setOnClickListener {
            loginAction()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {

            launch {
                viewModel.navigationEvent.collect{
                    when(it){
                        is NavigationEvent.Navigate -> {
                            if (it.destination == NavigationConstants.ORDERS_LIST_SCREEN) {
                                findNavController().navigate(R.id.action_loginFragment_to_ordersListFragment)
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
                    if(it){
                        binding.loginProgressBar.visible()
                        binding.loginBtn.gone()
                        binding.loginErrorMessageTextView.gone()
                    }
                    else {
                        binding.loginProgressBar.gone()
                        binding.loginBtn.visible()
                        binding.loginErrorMessageTextView.visible()
                    }
                }
            }

            launch {
                viewModel.errorMessage.collectLatest {
                    binding.loginErrorMessageTextView.text = it
                }
            }
        }

    }

    private fun loginAction(){
        val username = binding.etUsername.text.toString()
        val password = binding.etPassword.text.toString()

        viewModel.login(username, password)

        requireActivity().hideKeyboard()
    }
}