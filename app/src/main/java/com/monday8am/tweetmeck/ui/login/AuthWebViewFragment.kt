package com.monday8am.tweetmeck.ui.login

import android.net.http.SslError
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.monday8am.tweetmeck.R
import com.monday8am.tweetmeck.ui.delegates.AuthState
import com.monday8am.tweetmeck.util.EventObserver
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class AuthWebViewFragment : Fragment() {

    private val viewModel: AuthViewModel by sharedViewModel()

    private lateinit var webView: WebView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_auth_web_view, container, false)
        val spinner = view.findViewById<ProgressBar>(R.id.webViewSpinner)
        webView = view.findViewById(R.id.webView)

        webView.apply {
            visibility = View.INVISIBLE
            isVerticalScrollBarEnabled = false
            isHorizontalScrollBarEnabled = false
            settings.apply {
                allowFileAccess = false
                javaScriptEnabled = false
            }
        }

        webView.webViewClient = object : WebViewClient() {

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                spinner.visibility = View.GONE
                webView.visibility = View.VISIBLE
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                viewModel.setResult(request?.url, null)
                return super.shouldOverrideUrlLoading(view, request)
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                viewModel.setResult(null, "Error authenticating user!")
            }

            override fun onReceivedSslError(
                view: WebView?,
                handler: SslErrorHandler?,
                error: SslError?
            ) {
                super.onReceivedSslError(view, handler, error)
                viewModel.setResult(null, "SSL error authenticating user!")
            }
        }

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.authState.observe(viewLifecycleOwner, EventObserver { state ->
            when (state) {
                is AuthState.WaitingForUserCredentials -> webView.loadUrl(state.url)
                else -> this.findNavController().navigateUp()
            }
        })
    }
}
