# Firebase Doorbell

This is a demo project for a home appliance video doorbell implemented for
Android Things with Firebase.  It's also the subject of my talk "[Connect
your Android Things with Firebase][1]".

There are three components to this project:

- An Android Things app (the doorbell)
- A companion app (homeowner app)
- A backend implemented with Cloud Functions

The Android Things app lives under "thing", and the companion app lives
under "app".  They both need to be added as apps to a Firebase project.
The backend lives under "backend" and should be deployed to the same project
with the Firebase CLI.

The latest version of the slides of the related talk can be found on
[SpeakerDeck][1].  There are some slides with code and a helpful system
diagram that shows how the following Firebase and Google APIs were used:

- Cloud Firestore
- Cloud Storage
- Cloud Messaging
- Cloud Functions
- Nearby

**NOTE**: A service account is required for the Cloud Functions code to
run correctly.  The script under `backend/scripts/config-env.sh` will help
you get your service account file added as an env var to deploy with the
functions.

## Watch the session

Recorded at Droidcon VN:

[![Video of session](https://img.youtube.com/vi/F-zq8xOntEE/0.jpg)](https://www.youtube.com/watch?v=F-zq8xOntEE) 

## License

The code in this project is licensed under the Apache License 2.0.

```text
Copyright 2018 Google LLC
 
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
 
    https://www.apache.org/licenses/LICENSE-2.0
 
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

## Disclaimer

This is not an officially supported Google product.

[1]: https://speakerdeck.com/codingdoug/connect-your-android-things-with-firebase-devfestdc
