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
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.hyperaware.doorbell.app.R
import com.hyperaware.doorbell.app.model.Ring
import java.text.DateFormat

class ListRingsActivity : AppCompatActivity() {

    companion object {
        private val FIR_STORAGE = FirebaseStorage.getInstance()
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_list_rings)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val ringRecyclerView = findViewById<RecyclerView>(R.id.ringRecyclerView)
        ringRecyclerView.layoutManager = LinearLayoutManager(this)
        ringRecyclerView.adapter = mAdapter
    }

    public override fun onStart() {
        super.onStart()
        mAdapter.startListening()
    }

    public override fun onStop() {
        mAdapter.stopListening()
        super.onStop()
    }


    private inner class RingHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
        val ringerImageView: ImageView = itemView.findViewById(R.id.iv_guest)
        val answerImageView: ImageView = itemView.findViewById(R.id.iv_disposition)
    }

    private val query = FirebaseFirestore.getInstance()
        .collection("rings").orderBy("date", Query.Direction.DESCENDING)
    private val options = FirestoreRecyclerOptions.Builder<Ring>()
        .setQuery(query, Ring::class.java)
        .build()

    private val mAdapter = object : FirestoreRecyclerAdapter<Ring, RingHolder>(options) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RingHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.ring_item, parent, false)
            return RingHolder(view)
        }

        override fun onBindViewHolder(holder: RingHolder, position: Int, ring: Ring) {
            val imageRef = FIR_STORAGE.getReference(ring.imagePath!!)
            val date = ring.date
            val dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG)
            holder.dateTextView.text = dateFormat.format(date)
            Glide.with(this@ListRingsActivity).load(imageRef).into(holder.ringerImageView)
            val answer = ring.answer
            if (answer != null && answer.disposition!!) {
                holder.answerImageView.setImageDrawable(resources.getDrawable(R.drawable.ic_check_black_24dp))
            }
            else {
                holder.answerImageView.setImageDrawable(resources.getDrawable(R.drawable.ic_do_not_disturb_black_24dp))
            }
        }
    }

}
