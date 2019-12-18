/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.monday8am.tweetmeck.ui.login

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.monday8am.tweetmeck.R
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

/**
 * Dialog that confirms that a user wishes to sign out.
 */
class SignOutDialogFragment : AppCompatDialogFragment() {

    private val viewModel: AuthViewModel by sharedViewModel()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.dialog_sign_out_title)
            .setMessage(R.string.dialog_sign_out_content)
            .setNegativeButton(R.string.not_now, null)
            .setPositiveButton(R.string.sign_out) { _, _ ->
                viewModel.logout()
            }
            .create()
    }
}
