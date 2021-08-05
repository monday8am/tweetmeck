package com.monday8am.tweetmeck.ui.login

import android.net.Uri
import android.net.http.SslError
import android.os.Bundle
import android.view.View
import android.webkit.SslErrorHandler
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.monday8am.tweetmeck.R
import com.monday8am.tweetmeck.data.remote.RequestToken
import com.monday8am.tweetmeck.ui.delegates.AuthState
import com.monday8am.tweetmeck.ui.home.HomeViewModel
import com.monday8am.tweetmeck.util.EventObserver
import dagger.hilt.android.AndroidEntryPoint
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import dev.enro.core.close
import dev.enro.core.getNavigationHandle
import kotlinx.android.parcel.Parcelize

@Parcelize
class AuthenticateKey : NavigationKey

@NavigationDestination(AuthenticateKey::class)
@AndroidEntryPoint
class AuthWebViewFragment : Fragment(R.layout.fragment_auth_web_view) {

    private val viewModel: HomeViewModel by activityViewModels()

    private lateinit var webView: WebView
    private var requestToken: RequestToken? = null

    private fun setUrlResult(uri: Uri?, error: String?) {
        requestToken?.let {
            viewModel.setResult(uri, it, error)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
                setUrlResult(request?.url, null)
                return super.shouldOverrideUrlLoading(view, request)
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                setUrlResult(null, "Error authenticating user!")
            }

            override fun onReceivedSslError(
                view: WebView?,
                handler: SslErrorHandler?,
                error: SslError?
            ) {
                super.onReceivedSslError(view, handler, error)
                setUrlResult(null, "SSL error authenticating user!")
            }
        }

        viewModel.authState.observe(
            viewLifecycleOwner,
            EventObserver { state ->
                when (state) {
                    is AuthState.WaitingForUserCredentials -> {
                        requestToken = state.requestToken
                        webView.loadUrl(state.url)
                    }
                    else -> {
                        getNavigationHandle<AuthenticateKey>().close()
                    }
                }
            }
        )
    }
}
