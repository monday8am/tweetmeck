package com.monday8am.tweetmeck.ui.login

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.monday8am.tweetmeck.R
import com.monday8am.tweetmeck.ui.home.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.parcel.Parcelize
import nav.enro.annotations.NavigationDestination
import nav.enro.core.NavigationKey

/**
 * Dialog that tells the user to sign in to continue the operation.
 */
@Parcelize
class SignInKey : NavigationKey

@NavigationDestination(SignInKey::class)
@AndroidEntryPoint
class SignInDialogFragment : AppCompatDialogFragment() {

    private val viewModel: HomeViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.dialog_sign_in_title)
            .setMessage(R.string.dialog_sign_in_content)
            .setNegativeButton(R.string.not_now, null)
            .setPositiveButton(R.string.sign_in) { _, _ ->
                viewModel.triggerLogIn()
                this.dismiss()
            }
            .create()
    }
}
