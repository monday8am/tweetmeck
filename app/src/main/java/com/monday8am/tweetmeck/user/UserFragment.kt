package com.monday8am.tweetmeck.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.monday8am.tweetmeck.databinding.UserFragmentBinding
import com.monday8am.tweetmeck.login.AuthViewModel
import org.koin.androidx.viewmodel.ext.android.getViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.core.parameter.parametersOf

class UserFragment : Fragment() {

    private val navArgs: UserFragmentArgs by navArgs()
    private val authViewModel: AuthViewModel by sharedViewModel()
    private lateinit var userViewModel: UserViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        userViewModel = getViewModel { parametersOf(navArgs.userId) }
        val binding = UserFragmentBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
            viewmodel = this@UserFragment.userViewModel
            authViewModel = this@UserFragment.authViewModel
        }
        return binding.root
    }
}
