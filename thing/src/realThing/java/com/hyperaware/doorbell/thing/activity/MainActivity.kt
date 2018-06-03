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

import android.os.Bundle
import android.util.Log
import android.view.View

import com.google.android.things.contrib.driver.button.Button
import com.google.android.things.contrib.driver.rainbowhat.RainbowHat
import com.google.android.things.pio.Gpio
import java.io.Closeable

import java.io.IOException

class MainActivity : BaseMainActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private var buttonA: Button? = null
    private var buttonB: Button? = null
    private var buttonC: Button? = null
    private var red: Gpio? = null
    private var green: Gpio? = null
    private var blue: Gpio? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            initThing()
        }
        catch (e: IOException) {
            Log.e(TAG, "Error initializing Thing hardware", e)
            finish()
        }

        buttonRing.visibility = View.GONE
    }

    override fun onDestroy() {
        closeThing()
        super.onDestroy()
    }

    @Throws(IOException::class)
    private fun initThing() {
        initLeds()
        initButtons()
    }

    @Throws(IOException::class)
    private fun initLeds() {
        red = RainbowHat.openLedRed()
        red!!.value = false
        green = RainbowHat.openLedGreen()
        green!!.value = false
        blue = RainbowHat.openLedBlue()
        blue!!.value = false
    }

    @Throws(IOException::class)
    private fun initButtons() {
        buttonA = RainbowHat.openButtonA()
        buttonA!!.setOnButtonEventListener { _, pressed ->
            Log.i(TAG, "Button A $pressed")
            if (pressed) {
                onRingClick()
            }
            try {
                red!!.value = pressed
            }
            catch (e: IOException) {
                Log.e(TAG, "", e)
            }
        }
        buttonB = RainbowHat.openButtonB()
        buttonB!!.setOnButtonEventListener { _, pressed ->
            Log.i(TAG, "Button B $pressed")
            try {
                green!!.value = pressed
            }
            catch (e: IOException) {
                Log.e(TAG, "", e)
            }
        }
        buttonC = RainbowHat.openButtonC()
        buttonC!!.setOnButtonEventListener { _, pressed ->
            Log.i(TAG, "Button C $pressed")
            if (pressed) {
                finish()
            }
            try {
                blue!!.value = pressed
            }
            catch (e: IOException) {
                Log.e(TAG, "", e)
            }
        }
    }

    private fun closeThing() {
        closeLeds()
        closeButtons()
    }

    private fun closeLeds() {
        safeClose(red)
        safeClose(green)
        safeClose(blue)
    }

    private fun closeButtons() {
        safeClose(buttonA)
        safeClose(buttonB)
        safeClose(buttonC)
    }

    private fun safeClose(closeable: Gpio?) {
        try {
            closeable?.close()
        }
        catch (e: IOException) {
            Log.e(TAG, "", e)
        }
    }

    private fun safeClose(closeable: AutoCloseable?) {
        try {
            closeable?.close()
        }
        catch (e: IOException) {
            Log.e(TAG, "", e)
        }
    }

}
