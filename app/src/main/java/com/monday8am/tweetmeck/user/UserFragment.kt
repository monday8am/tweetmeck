package com.monday8am.tweetmeck.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.monday8am.tweetmeck.R
import com.monday8am.tweetmeck.databinding.SettingsFragmentBinding
import com.monday8am.tweetmeck.login.AuthState
import com.monday8am.tweetmeck.login.AuthViewModel
import com.monday8am.tweetmeck.util.getViewModelFactory

class UserFragment : Fragment() {

    private lateinit var binding: SettingsFragmentBinding

    private val authViewModel by activityViewModels<AuthViewModel> { getViewModelFactory() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = SettingsFragmentBinding.inflate(layoutInflater)
        binding.logoutBtn.setOnClickListener {
            authViewModel.logout()
        }
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        authViewModel.authState.observe(viewLifecycleOwner, Observer<AuthState> { state ->
            when (state) {
                is AuthState.NotLogged -> {
                    this.findNavController().navigate(R.id.action_settings_dest_to_login_dest)
                }
                else -> {
                    binding.logoutBtn.alpha = 1.0f
                    binding.logoutBtn.isEnabled = true
                }
            }
        })
    }
}
