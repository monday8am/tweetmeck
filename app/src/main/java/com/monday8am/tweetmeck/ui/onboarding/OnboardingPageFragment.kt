package com.monday8am.tweetmeck.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelStoreOwner
import com.monday8am.tweetmeck.R
import com.monday8am.tweetmeck.util.lazyFast
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class OnboardingPageFragment : Fragment() {

    companion object {
        private const val ON_PAGE_INDEX = "arg.OnPAGE_INDEX"

        @JvmStatic
        fun newInstance(index: Int) =
            OnboardingPageFragment().apply {
                arguments = Bundle().apply {
                    putInt(ON_PAGE_INDEX, index)
                }
            }
    }

    private val viewModel: OnboardingViewModel by sharedViewModel(from = { parentFragment as ViewModelStoreOwner })

    private val listId: Long by lazyFast {
        val args = arguments ?: throw IllegalStateException("Missing arguments!")
        args.getLong(ON_PAGE_INDEX)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return TextView(activity).apply {
            setText(R.string.hello_blank_fragment)
        }
    }
}
