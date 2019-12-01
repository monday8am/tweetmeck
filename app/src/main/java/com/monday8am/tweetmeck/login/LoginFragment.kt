package com.monday8am.tweetmeck.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.monday8am.tweetmeck.R
import com.monday8am.tweetmeck.databinding.LoginFragmentBinding
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class LoginFragment : Fragment() {

    private lateinit var viewBinding: LoginFragmentBinding
    private val viewModel: AuthViewModel by sharedViewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = LoginFragmentBinding.inflate(layoutInflater)
        viewBinding.button.setOnClickListener {
            viewModel.triggerAuth()
        }
        return viewBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.authState.observe(viewLifecycleOwner, Observer<AuthState> { state ->
            when (state) {
                is AuthState.Loading -> {
                    viewBinding.button.alpha = 0.5f
                    viewBinding.button.isEnabled = false
                }
                is AuthState.WaitingForUserCredentials -> this.findNavController().navigate(R.id.action_login_dest_to_auth_dest)
                is AuthState.Logged -> {
                    this.findNavController().navigate(R.id.action_login_dest_to_timeline_dest)
                }
                else -> {
                    viewBinding.button.alpha = 1.0f
                    viewBinding.button.isEnabled = true
                }
            }
        })
    }
}
