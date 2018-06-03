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

import android.os.Handler

class CountdownTimer(private var count: Int, private val listener: Listener) {

    private val handler = Handler()
    private val runnable = CountdownRunnable()

    interface Listener {
        fun onCount(value: Int)
    }

    fun start() {
        handler.post(runnable)
    }

    fun stop() {
        handler.removeCallbacks(runnable)
    }

    private inner class CountdownRunnable : Runnable {
        override fun run() {
            listener.onCount(count)
            if (count > 0) {
                count--
                handler.postDelayed(runnable, 1000)
            }
        }
    }

}
