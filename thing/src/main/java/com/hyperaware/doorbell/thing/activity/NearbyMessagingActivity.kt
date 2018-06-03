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

package com.hyperaware.doorbell.thing.activity

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.TextView
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.messages.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.hyperaware.doorbell.thing.R
import com.hyperaware.doorbell.thing.activity.NearbyMessagingActivity.UiState.*

class NearbyMessagingActivity : Activity() {

    companion object {
        private const val TAG = "NearbyMessagingActivity"
    }

    private val handler = Handler()

    private lateinit var tvStatus: TextView

    private lateinit var client: MessagesClient
    private var token : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_auth)
        tvStatus = findViewById(R.id.tvStatus)
        updateUi(WaitingForPermission)

        client = Nearby.getMessagesClient(this)
    }

    override fun onStart() {
        super.onStart()

        client.registerStatusCallback(statusCallback)
    }

    override fun onStop() {
        client.unsubscribe(messageListener)
        client.unregisterStatusCallback(statusCallback)

        super.onStop()
    }

    private enum class UiState {
        WaitingForPermission,
        Subscribing,
        SubscribeExpired,
        SubscriptionFailure,
        WaitingForMessage,
        Authenticating,
        MessageTimedOut,
        SignInFailed
    }

    private fun updateUi(uiState: UiState, finishOnDelay: Boolean = false) {
        Log.d(TAG, "uiState: $uiState")
        val message = when (uiState) {
            WaitingForPermission -> "Waiting for permission..."
            Subscribing -> "Subscribing..."
            SubscribeExpired -> "Nearby Messaging subscription expired."
            SubscriptionFailure -> "Subscription failure"
            WaitingForMessage -> "Waiting for message..."
            Authenticating -> "Found nearby message, authenticating..."
            MessageTimedOut -> "Timed out waiting for message."
            SignInFailed -> "Sign in failed."
        }

        tvStatus.text = message
        if (finishOnDelay) {
            handler.postDelayed({ finish() }, 2000)
        }
    }

    private val statusCallback = object : StatusCallback() {
        override fun onPermissionChanged(permissionGranted: Boolean) {
            Log.d(TAG, "onPermissionChanged $permissionGranted")
            updateUi(Subscribing)
            subscribe()
        }
    }

    private fun subscribe() {
        val strategy = Strategy.Builder()
            .setDiscoveryMode(Strategy.DISCOVERY_MODE_SCAN)
            .setTtlSeconds(Strategy.TTL_SECONDS_MAX)
            .build()

        val subscribeOpts = SubscribeOptions.Builder()
            .setStrategy(strategy)
            .setCallback(object : SubscribeCallback() {
                override fun onExpired() {
                    Log.d(TAG, "onExpired")
                    updateUi(SubscribeExpired, true)
                }
            })
            .build()

        client.subscribe(messageListener, subscribeOpts)
            .addOnSuccessListener(this) {
                Log.d(TAG, "subscribe success")
                updateUi(WaitingForMessage)
                handler.postDelayed({ updateUi(MessageTimedOut, true) }, 30000)
            }
            .addOnFailureListener(this) { e ->
                Log.e(TAG, "subscribe failure", e)
                updateUi(SubscriptionFailure, true)
            }
    }

    private val messageListener = object : MessageListener() {
        override fun onFound(message: Message) {
            token = String(message.content)
            Log.d(TAG, "Found message: $token")
            updateUi(Authenticating, true)
            trySignIn()
        }

        override fun onLost(message: Message) {
            Log.d(TAG, "Lost message: " + String(message.content))
        }
    }

    private fun trySignIn() {
        Log.d(TAG, "Signing in with token $token")
        val credential = GoogleAuthProvider.getCredential(token, null)
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnSuccessListener(this) { result ->
                val user = result.user
                Log.d(TAG, "signInWithCredential ${user.displayName} ${user.email}")
                finish()
            }
            .addOnFailureListener(this) { e ->
                Log.e(TAG, "signInWithCredential onFailure", e)
            }
    }

}
