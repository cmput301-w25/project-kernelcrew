# Project Setup

Opening the `code/` directory in Android Studio and running the "gradle sync"
task covers most of the setup. However, you will also need to initialize an
Android app in Firebase.

## Firebase

To setup Firebase, create a new Firebase app (just like we did in our lab) for
an app called "Moodable". Skip the instructions for updating any of the gradle
files, you will only need to copy over a `google-services.json` file.

### Firestore

Once the app is setup in Firebase, create a Firestore database **setup in
"production" mode. **Note that in this step, we differ from the lab
instructions**.

Once the Firestore database (DB) has been created, we need to update the access
rules so that our users can actually read/write data from the DB. Navigate to
the "Rules" tab and change the ruleset to the following:

```
rules_version = '2';

service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if request.auth != null;
    }
  }
}
```

> This ruleset requires that all reads/writes to any document in the database
> *must be authenticated*.

Once the ruleset edits have been made, be sure to press "Publish" to apply
them.

### Authentication

We use firebase's authentication feature. This needs to be manually enabled by:

 1. Navigate to "Authentication" under the Moodable app in the firebase dashboard
 2. Click "Get Started"
 3. Click "Email/Password" from the list of "Native Providers"
 4. Toggle the "Email/Password" option on
 5. Click "Save"

> If you forget to do this, you may get this error during sign up:
>
>   "Initial task failed for action RecaptchaAction(action=signUpPassword)with
>   exception - An internal error has occurred. [ CONFIGURATION_NOT_FOUND ]"
>
> Ensure that "Email/Password" authentication has been enabled.

For technical details of firebase authentication, see [the
docs](https://firebase.google.com/docs/auth/android/start).
