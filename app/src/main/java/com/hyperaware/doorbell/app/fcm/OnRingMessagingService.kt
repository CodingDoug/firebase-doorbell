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

package com.hyperaware.doorbell.app.fcm

import android.content.Intent
import android.util.Log

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.hyperaware.doorbell.app.activity.AnswerRingActivity

class OnRingMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "RingMessagingService"
        private const val PROP_RING_ID = "ring_id"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "Ring message received")

        //
        // The incoming message should contain the ID of the ring
        //

        if (remoteMessage.data.containsKey(PROP_RING_ID)) {
            val ringId = remoteMessage.data[PROP_RING_ID]
            Log.d(TAG, "$PROP_RING_ID: $ringId")
            val intent = Intent(this, AnswerRingActivity::class.java)
            intent.putExtra(AnswerRingActivity.EXTRA_RING_ID, ringId)
            startActivity(intent)
        }
        else {
            Log.w(TAG, "Data message received without $PROP_RING_ID")
        }
    }

}
