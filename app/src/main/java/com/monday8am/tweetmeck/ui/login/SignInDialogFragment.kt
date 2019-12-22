package com.monday8am.tweetmeck.ui.login

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.monday8am.tweetmeck.R
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

/**
 * Dialog that tells the user to sign in to continue the operation.
 */
class SignInDialogFragment : AppCompatDialogFragment() {

    private val viewModel: AuthViewModel by sharedViewModel()

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
