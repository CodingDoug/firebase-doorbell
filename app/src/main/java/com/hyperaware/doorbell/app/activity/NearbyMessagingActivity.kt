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

package com.hyperaware.doorbell.app.activity

import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.messages.*
import com.hyperaware.doorbell.app.R
import com.hyperaware.doorbell.app.activity.NearbyMessagingActivity.UiState.*

class NearbyMessagingActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "NearbyMessagingActivity"
    }

    private val handler = Handler()

    private lateinit var tvStatus: TextView

    private lateinit var client: MessagesClient
    private lateinit var message: Message

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_auth)
        tvStatus = findViewById(R.id.tvStatus)
        updateUi(WaitingForPermission)

        client = Nearby.getMessagesClient(this)

        val lastSignedInAccount = GoogleSignIn.getLastSignedInAccount(this)
        if (lastSignedInAccount != null) {
            message = Message(lastSignedInAccount.idToken!!.toByteArray())
        }
        else {
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onStart() {
        super.onStart()

        tvStatus = findViewById(R.id.tvStatus)
        updateUi(WaitingForPermission)

        client.registerStatusCallback(statusCallback)
    }

    override fun onStop() {
        client.unpublish(message)
        client.unregisterStatusCallback(statusCallback)

        super.onStop()
    }

    private enum class UiState {
        WaitingForPermission,
        Publishing,
        PublishExpired,
        PublicationFailure,
    }

    private fun updateUi(uiState: UiState, finishOnDelay: Boolean = false) {
        Log.d(TAG, "uiState: $uiState")
        val message = when (uiState) {
            WaitingForPermission -> "Waiting for permission..."
            Publishing -> "Publishing..."
            PublishExpired -> "Nearby Messaging publish expired."
            PublicationFailure -> "Publication failure"
        }

        tvStatus.text = message
        if (finishOnDelay) {
            handler.postDelayed({ finish() }, 2000)
        }
    }

    private val statusCallback = object: StatusCallback() {
        override fun onPermissionChanged(permissionGranted: Boolean) {
            Log.d(TAG, "onPermissionChanged $permissionGranted")
            updateUi(Publishing)
            publish()
        }
    }

    private fun publish() {
        val strategy = Strategy.Builder()
            .setDiscoveryMode(Strategy.DISCOVERY_MODE_BROADCAST)
            .setTtlSeconds(Strategy.TTL_SECONDS_MAX)
            .build()

        val publishOpts = PublishOptions.Builder()
            .setStrategy(strategy)
            .setCallback(object : PublishCallback() {
                override fun onExpired() {
                    Log.d(TAG, "onExpired")
                    updateUi(PublishExpired, true)
                }
            })
            .build()

        client.publish(message, publishOpts)
            .addOnSuccessListener(this) {
                Log.e(TAG, "publish success")
            }
            .addOnFailureListener(this) { e ->
                Log.e(TAG, "publish failed", e)
                updateUi(PublicationFailure, true)
            }
    }

}
