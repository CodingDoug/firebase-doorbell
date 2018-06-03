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

const sourceMapSupport = require('source-map-support')
sourceMapSupport.install()

import * as functions from 'firebase-functions'
import * as admin from 'firebase-admin'
import { basename } from 'path'
import { DocumentSnapshot } from 'firebase-functions/lib/providers/firestore';

type Ring = {
    id: string,
    date: Date,
    imagePath: string,
    answer?: RingAnswer
}

type RingAnswer = {
    uid: string
    disposition: boolean
}

const cert = JSON.parse(functions.config().serviceaccount.cert.replace(new RegExp("'", 'g'), ''))
const adminConfig = JSON.parse(process.env.FIREBASE_CONFIG)
adminConfig.credential = admin.credential.cert(cert)
admin.initializeApp(adminConfig)

const firestore = admin.firestore()
const fcm = admin.messaging()

export const onRing = functions.storage.object().onFinalize(_onRing)
async function _onRing(object: functions.storage.ObjectMetadata): Promise<any> {
    console.log(object)

    const path = object.name
    const id = basename(path, '.jpg')

    try {
        // Add a document to Firestore with the details of this ring
        //
        const ring: Ring = {
            id: id,
            date: new Date(),
            imagePath: path,
        }
        console.log('Ring:', ring)
        await firestore.collection('rings').doc(id).set(ring)

        // Send a notification to the app
        //
        const payload = {
            notification: {
                title: 'Ring Ring!',
                body: 'There is someone at the door!',
                click_action: 'com.hyperaware.doorbell.ANSWER_RING'
            },
            data: {
                ring_id: id
            }
        }

        console.log('Sending FCM payload to topic "rings"', payload)
        const response = await fcm.sendToTopic('rings', payload)
        console.log('ring sent:', response)
    }
    catch (err) {
        console.error('ring not sent:', err)
    }
}


export const onAnswer = functions.firestore.document('/rings/{ringId}').onUpdate(_onAnswer)
async function _onAnswer(change: functions.Change<DocumentSnapshot>): Promise<any> {
    const ringId = change.before.id  // e.g. 20180327123000
    const previous = change.before.data() as Ring
    const ring = change.after.data() as Ring
    console.log(`Ring`, ring)

    // Only interested in rings that have a new answer
    if (previous.answer || !ring.answer) {
        console.log("This is not the update you're looking for.")
        return Promise.resolve()
    }

    const payload = {
        data: {
            disposition: ring.answer.disposition.toString(),
            ring_id: ringId
        }
    }
    try {
        const response = await fcm.sendToTopic('answers', payload)
        console.log(`ring ${ringId} answer sent:`, response)
    }
    catch (err) {
        console.error(`ring ${ringId} answer error:`, err)
    }
}
