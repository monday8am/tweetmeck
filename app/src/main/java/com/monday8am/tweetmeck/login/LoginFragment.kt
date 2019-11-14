package com.monday8am.tweetmeck.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.monday8am.tweetmeck.R
import com.monday8am.tweetmeck.databinding.LoginFragmentBinding
import com.monday8am.tweetmeck.util.getViewModelFactory

class LoginFragment : Fragment() {

    private lateinit var binding: LoginFragmentBinding
    private val viewModel by activityViewModels<AuthViewModel> { getViewModelFactory() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = LoginFragmentBinding.inflate(layoutInflater)
        binding.button.setOnClickListener {
            viewModel.triggerAuth()
        }
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.authState.observe(viewLifecycleOwner, Observer<AuthState> { state ->
            when (state) {
                is AuthState.Loading -> {
                    binding.button.alpha = 0.5f
                    binding.button.isEnabled = false
                }
                is AuthState.WaitingForUserCredentials -> this.findNavController().navigate(R.id.action_login_dest_to_auth_dest)
                is AuthState.Logged -> {
                    this.findNavController().navigate(R.id.action_login_dest_to_timeline_dest)
                }
                else -> {
                    binding.button.alpha = 1.0f
                    binding.button.isEnabled = true
                }
            }
        })
    }
}
