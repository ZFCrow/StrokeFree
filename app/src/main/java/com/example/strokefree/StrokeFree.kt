package com.example.strokefree

import android.app.Application
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient


class StrokeFree : Application()
{
    val webClientID: String by lazy {
        getString(R.string.default_web_client_id)
    }
}