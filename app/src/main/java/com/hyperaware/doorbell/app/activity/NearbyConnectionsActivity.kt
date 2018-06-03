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

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import com.google.firebase.auth.FirebaseAuth

class NearbyConnectionsActivity : AppCompatActivity() {

    companion object {
        private const val MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 0
        private const val TAG = "NearbyConnsActivity"
    }

    private val firebaseAuth = FirebaseAuth.getInstance()
    private lateinit var connectionsClient: ConnectionsClient
    private var connectedEndpointId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (firebaseAuth.currentUser == null) {
            throw Exception("You can't do that here")
        }

//        setContentView(R.layout.activity_main)
        connectionsClient = Nearby.getConnectionsClient(this)
    }

    override fun onStart() {
        super.onStart()
        checkPermissionsForDiscovery()
    }

    override fun onStop() {
        stopDiscovery()
        super.onStop()
    }

    override fun onDestroy() {
        if (connectedEndpointId != null) {
            connectionsClient.disconnectFromEndpoint(connectedEndpointId!!)
            connectedEndpointId = null
        }
        super.onDestroy()
    }

    private fun checkPermissionsForDiscovery() {
        val perm = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (perm != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION
            )
        }
        else {
            startDiscovery()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (grantResults.size == 1) {
            startDiscovery()
        }
        else {
            Log.e(TAG, "User didn't grant permission")
            finish()
        }
    }

    private fun startDiscovery() {
        connectionsClient.startDiscovery(
            "com.hyperaware.doorbell.thing",
            endpointDiscoveryCallback,
            DiscoveryOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build()
        )
            .addOnSuccessListener(this) {
                Log.d(TAG, "startDiscovery onSuccess")
            }
            .addOnFailureListener(this) { e ->
                Log.e(TAG, "startAdvertising onFailure", e)
            }
    }

    private fun stopDiscovery() {
        connectionsClient.stopDiscovery()
    }

    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            Log.d(TAG, "onEndpointFound $endpointId name: ${info.endpointName} id: ${info.serviceId}")
            connect(endpointId)
        }

        override fun onEndpointLost(endpointId: String) {
            Log.d(TAG, "onEndpointLost $endpointId")
        }
    }

    private fun connect(endpointId: String) {
        connectionsClient.requestConnection(
            "",
            endpointId,
            connectionLifecycleCallback
        )
            .addOnSuccessListener(this@NearbyConnectionsActivity) {
                Log.d(TAG, "requestConnection onSuccess")
            }
            .addOnFailureListener(this@NearbyConnectionsActivity) { e ->
                Log.e(TAG, "requestConnection onFailure", e)
            }
    }

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            Log.d(TAG, "onConnectionInitiated $endpointId name: ${info.endpointName}")
            connectionsClient.acceptConnection(endpointId, payloadCallback)
                .addOnSuccessListener(this@NearbyConnectionsActivity) {
                    Log.d(TAG, "acceptConnection onSuccess")
                }
                .addOnFailureListener(this@NearbyConnectionsActivity) { e ->
                    Log.e(TAG, "acceptConnection onFailure", e)
                }
        }

        override fun onConnectionResult(endpointId: String, resolution: ConnectionResolution) {
            Log.d(TAG, "onConnectionResult $endpointId status: ${resolution.status}")
            connectedEndpointId = endpointId
            val token = GoogleSignIn.getLastSignedInAccount(this@NearbyConnectionsActivity)!!.idToken!!
            sendPayload(endpointId, token)
        }

        override fun onDisconnected(endpointId: String) {
            Log.d(TAG, "onDisconnected $endpointId")
            finish()
        }
    }

    private fun sendPayload(endpointId: String, token: String) {
        Log.d(TAG, "Sending id token $token")
        val payload = Payload.fromBytes(token.toByteArray())
        connectionsClient.sendPayload(endpointId, payload)
            .addOnSuccessListener(this) {
                Log.d(TAG, "sendPayload onSuccess")
            }
            .addOnFailureListener(this) { e ->
                Log.e(TAG, "sendPayload onFailure", e)
            }
    }

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String,  payload: Payload) {
            Log.d(TAG, "onPayloadReceived $endpointId payload: ${payload.id}")
            Log.d(TAG, String(payload.asBytes()!!))
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
            Log.d(TAG, "onPayloadTransferUpdate $endpointId ${update.status}")
            if (update.status == PayloadTransferUpdate.Status.SUCCESS) {
                Log.d(TAG, "Disconnecting")
                connectionsClient.disconnectFromEndpoint(endpointId)
                finish()
            }
        }
    }

}
