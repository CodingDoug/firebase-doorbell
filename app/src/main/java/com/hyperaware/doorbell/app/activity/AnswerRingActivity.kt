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
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.hyperaware.doorbell.app.R
import com.hyperaware.doorbell.app.model.Ring

class AnswerRingActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "AnswerActivity"
        private val FIR_STORAGE = FirebaseStorage.getInstance()
        const val EXTRA_RING_ID = "ring_id"
    }

    private lateinit var ivGuest: ImageView
    private lateinit var ringReference: DocumentReference
    private lateinit var uid: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_answer)

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            uid = user.uid
        }
        else {
            val msg = getString(R.string.msg_answer_ring_requires_login)
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
            Log.e(TAG, msg)
            finish()
        }

        val extras = intent.extras
        if (extras == null) {
            Log.e(TAG, "$EXTRA_RING_ID was not provided")
            finish()
            return
        }

        val ringId = extras.getString(EXTRA_RING_ID)
        if (ringId.isEmpty()) {
            Log.e(TAG, "$EXTRA_RING_ID was empty")
            finish()
            return
        }

        initViews()

        Log.d(TAG, "Ring id: $ringId")
        ringReference = FirebaseFirestore.getInstance().collection("rings").document(ringId)
        populateViews(ringId)
    }

    private fun initViews() {
        ivGuest = findViewById(R.id.iv_guest)

        findViewById<Button>(R.id.btn_no).setOnClickListener {
            updateAnswer(false)
        }

        findViewById<Button>(R.id.btn_yes).setOnClickListener {
            updateAnswer(true)
        }
    }

    private fun populateViews(ringId: String) {
        ringReference.get()
            .addOnSuccessListener(this) { snap ->
                if (snap.exists()) {
                    val ring = snap.toObject(Ring::class.java)!!
                    Log.d(TAG, "imagePath ${ring.imagePath!!}")
                    Glide.with(this@AnswerRingActivity)
                        .load(FIR_STORAGE.getReference(ring.imagePath!!))
                        .into(ivGuest)
                }
                else {
                    Log.e(TAG, "No document for ring $ringId")
                    finish()
                }
            }
            .addOnFailureListener(this) { error ->
                Log.e(TAG, "Can't fetch ring $ringId", error)
                finish()
            }
    }

    private fun updateAnswer(disposition: Boolean) {
        ringReference.update(
            "answer.uid", uid,
            "answer.disposition", disposition)
            .addOnCompleteListener(this) {
                Log.d(TAG, "Answer written to database")
                finish()
            }
            .addOnFailureListener(this) { e ->
                Log.d(TAG, "Answer not written to database", e)
                finish()
            }
    }

}
