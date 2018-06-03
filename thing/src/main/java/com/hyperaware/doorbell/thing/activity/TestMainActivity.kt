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
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.hyperaware.doorbell.thing.R

class TestMainActivity : Activity() {

    private val auth = FirebaseAuth.getInstance()
    private var user: FirebaseUser? = null

    private lateinit var vMain: Button
    private lateinit var vAuthConns: Button
    private lateinit var vAuthMessaging: Button
    private lateinit var vSignOut: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_main)

        vMain = findViewById<Button>(R.id.btn_main)
        vMain.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        vAuthConns = findViewById<Button>(R.id.btn_auth_conns)
        vAuthConns.setOnClickListener {
            startActivity(Intent(this, NearbyConnectionsActivity::class.java))
        }

        vAuthMessaging = findViewById<Button>(R.id.btn_auth_messaging)
        vAuthMessaging.setOnClickListener {
            startActivity(Intent(this, NearbyMessagingActivity::class.java))
        }

        vSignOut = findViewById<Button>(R.id.btn_sign_out)
        vSignOut.setOnClickListener {
            auth.signOut()
        }

        updateUi()
    }

    override fun onStart() {
        super.onStart()
        auth.addAuthStateListener(authStateListener)
    }

    override fun onStop() {
        auth.removeAuthStateListener(authStateListener)
        super.onStop()
    }

    private val authStateListener = FirebaseAuth.AuthStateListener {
        user = auth.currentUser
        updateUi()
    }

    private fun updateUi() {
        vMain.isEnabled = user != null
        vAuthConns.isEnabled = user == null
        vAuthMessaging.isEnabled = user == null
        vSignOut.isEnabled = user != null
    }

}
