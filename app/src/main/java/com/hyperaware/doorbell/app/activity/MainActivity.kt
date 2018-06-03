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

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.hyperaware.doorbell.app.R

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
        private const val RC_SIGN_IN = 1
    }

    private val auth = FirebaseAuth.getInstance()
    private var user: FirebaseUser? = null

    private lateinit var rootView : View
    private lateinit var vSignIn: Button
    private lateinit var vSignOut: Button
    private lateinit var vAuthConns: Button
    private lateinit var vAuthMessaging: Button
    private lateinit var vRings: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rootView = findViewById(R.id.root)

        vSignIn = findViewById<Button>(R.id.btn_sign_in)
        vSignIn.setOnClickListener {
            startActivityForResult(
                AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(listOf(AuthUI.IdpConfig.GoogleBuilder().build()))
                    .build(),
                RC_SIGN_IN)
        }

        vSignOut = findViewById<Button>(R.id.btn_sign_out)
        vSignOut.setOnClickListener {
            AuthUI.getInstance().signOut(this)
                .addOnSuccessListener(this) { Log.d(TAG, "Signed out") }
                .addOnFailureListener(this) { e -> Log.d(TAG, "Error signing out", e) }
        }

        vAuthConns = findViewById<Button>(R.id.btn_nearby_conns)
        vAuthConns.setOnClickListener {
            startActivity(Intent(this, NearbyConnectionsActivity::class.java))
        }

        vAuthMessaging = findViewById<Button>(R.id.btn_nearby_messaging)
        vAuthMessaging.setOnClickListener {
            startActivity(Intent(this, NearbyMessagingActivity::class.java))
        }

        vRings = findViewById<Button>(R.id.btn_rings)
        vRings.setOnClickListener {
            startActivity(Intent(this, ListRingsActivity::class.java))
        }
    }

    override fun onStart() {
        super.onStart()
        auth.addAuthStateListener(authStateListener)
    }

    override fun onStop() {
        auth.removeAuthStateListener(authStateListener)
        super.onStop()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                Snackbar.make(rootView, "Signed in", Snackbar.LENGTH_SHORT).show()
                if (response != null) {
                    Log.d(TAG, "idpToken ${response.idpToken}")
                }
            }
            else {
                Snackbar.make(rootView, "Not signed in", Snackbar.LENGTH_SHORT).show()
                if (response != null) {
                    Log.e(TAG, "Sign in error ${response.error}")
                }
            }
        }
    }

    private val authStateListener = FirebaseAuth.AuthStateListener {
        user = auth.currentUser
        updateUi()
    }

    private fun updateUi() {
        vSignIn.isEnabled = user == null
        vSignOut.isEnabled = user != null
        vAuthConns.isEnabled = user != null
        vAuthMessaging.isEnabled = user != null
        vSignOut.isEnabled = user != null
        vRings.isEnabled = user != null
    }

}
