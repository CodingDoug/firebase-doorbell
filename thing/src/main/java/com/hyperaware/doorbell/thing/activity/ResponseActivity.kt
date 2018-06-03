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
import com.hyperaware.doorbell.thing.R

class ResponseActivity : Activity() {

    companion object {
        private const val TAG = "ResponseActivity"
        const val EXTRA_DISPOSITION = "disposition"
    }

    private lateinit var tvDisposition: TextView
    private var disposition: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = intent

        val extras = intent.extras
        if (extras == null || !extras.containsKey(EXTRA_DISPOSITION)) {
            Log.e(TAG, "$EXTRA_DISPOSITION was not provided")
            finish()
            return
        }

        disposition = extras.getBoolean(EXTRA_DISPOSITION)

        initViews()
        Handler().postDelayed({ finish() }, 5000)
    }

    private fun initViews() {
        setContentView(R.layout.activity_response)
        tvDisposition = findViewById(R.id.disposition)
        tvDisposition.text = if (disposition) {
            getString(R.string.disposition_come_in)
        }
        else {
            getString(R.string.disposition_go_away)
        }
    }

}
