package com.monday8am.tweetmeck.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.monday8am.tweetmeck.databinding.LoginFragmentBinding
import com.monday8am.tweetmeck.util.getViewModelFactory
import timber.log.Timber

class LoginFragment : Fragment() {

    private lateinit var binding: LoginFragmentBinding

    private val viewModel by viewModels<AuthViewModel> { getViewModelFactory() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = LoginFragmentBinding.inflate(layoutInflater)
        binding.button.setOnClickListener {
            viewModel.triggerAuth()
        }

        viewModel.authState.observe(this,  Observer<AuthState> { state ->
            when (state) {
               is AuthState.Going -> this.findNavController().navigate()
            }
            Timber.d(state.toString())
            //Timber.d("nada recibido!")
        })

        return binding.root
    }
}
