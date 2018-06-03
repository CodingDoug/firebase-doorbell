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
import android.net.ConnectivityManager
import android.net.Network
import android.os.Bundle
import android.util.Log
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class NearbyConnectionsActivity : Activity() {

    companion object {
        private const val TAG = "NearbyConnectionsActivity"
    }

    private lateinit var connectivityManager : ConnectivityManager
    private lateinit var connectionsClient: ConnectionsClient
    private var token : String? = null

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network?) {
            super.onAvailable(network)
            Log.d(TAG, "onAvailable $network")
            if (token != null) {
                trySignIn()
            }
        }

        override fun onLost(network: Network?) {
            Log.d(TAG, "onLost $network")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        connectionsClient = Nearby.getConnectionsClient(this)

        connectivityManager = getSystemService(ConnectivityManager::class.java)
        connectivityManager.registerDefaultNetworkCallback(networkCallback)

        FirebaseAuth.getInstance().signOut()
    }

    override fun onStart() {
        super.onStart()
        startAdvertising()
    }

    override fun onStop() {
        stopAdvertising()
        super.onStop()
    }

    override fun onDestroy() {
        connectionsClient.stopAllEndpoints()
        connectivityManager.unregisterNetworkCallback(networkCallback)
        super.onDestroy()
    }

    private fun startAdvertising() {
        connectionsClient.startAdvertising(
            "Firebase Doorbell Thing",
            packageName,
            connectionLifecycleCallback,
            AdvertisingOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build()
        )
            .addOnSuccessListener(this) {
                Log.d(TAG, "startAdvertising onSuccess")
            }
            .addOnFailureListener(this) { e ->
                Log.e(TAG, "startAdvertising onFailure", e)
            }
    }

    private fun stopAdvertising() {
        connectionsClient.stopAdvertising()
    }

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            Log.d(TAG, "onConnectionInitiated $endpointId name: ${info.endpointName}")
            connectionsClient.acceptConnection(endpointId, payloadCallback)
                .addOnSuccessListener(this@NearbyConnectionsActivity) { _ ->
                    Log.d(TAG, "acceptConnection onSuccess")
                }
                .addOnFailureListener(this@NearbyConnectionsActivity) { e ->
                    Log.e(TAG, "acceptConnection onFailure", e)
                }
        }

        override fun onConnectionResult(endpointId: String, resolution: ConnectionResolution) {
            Log.d(TAG, "onConnectionResult $endpointId status: ${resolution.status}")
        }

        override fun onDisconnected(endpointId: String) {
            Log.d(TAG, "onDisconnected $endpointId")
            trySignIn()
        }
    }

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String,  payload: Payload) {
            Log.d(TAG, "onPayloadReceived $endpointId payload: ${payload.id}")
            token = String(payload.asBytes()!!)
            Log.d(TAG, token)
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
            Log.d(TAG, "onPayloadTransferUpdate $endpointId ${update.status}")
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
