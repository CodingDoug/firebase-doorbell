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
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.google.firebase.storage.FirebaseStorage
import com.hyperaware.doorbell.thing.R
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

abstract class BaseMainActivity : Activity() {

    companion object {
        private const val TAG = "BaseMainActivity"
        private const val REQUEST_TAKE_PICTURE = 0
    }

    protected lateinit var buttonRing: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "Starting MainActivity")
        initViews()
    }

    private fun initViews() {
        setContentView(R.layout.main)
        buttonRing = findViewById<Button>(R.id.ring)
        buttonRing.setOnClickListener { onRingClick() }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (resultCode) {
            Activity.RESULT_CANCELED -> {}
            Camera2Activity.RESULT_PICTURE -> {
                val file = data!!.getStringExtra(Camera2Activity.EXTRA_PICTURE_FILE)
                uploadFile(File(file))
            }
        }
    }

    private fun uploadFile(file: File) {
        val sdf = SimpleDateFormat("yyyyMMddHHmmss", Locale.US)
        val storagePath = "/pictures/${sdf.format(Date())}.jpg"
        Log.i(TAG, "Uploading to $file to $storagePath")
        val ref = FirebaseStorage.getInstance().getReference(storagePath)
        ref.putFile(Uri.fromFile(file))
            .addOnSuccessListener(this) {
                Log.i(TAG, "Picture uploaded")
            }
            .addOnFailureListener(this) { e ->
                Log.i(TAG, "Upload failed", e)
            }
            .addOnCompleteListener(this) {
                file.delete()
            }
    }

    protected fun onRingClick() {
        startActivityForResult(Intent(this, Camera2Activity::class.java), REQUEST_TAKE_PICTURE)
    }

}
