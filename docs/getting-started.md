# Project Setup

Opening the `code/` directory in Android Studio and running the "gradle sync"
task covers most of the setup. However, you will also need to initialize an
Android app in Firebase.

## Firebase

To setup Firebase, create a new Firebase app (just like we did in our lab) for
an app called "Moodable". Skip the instructions for updating any of the gradle
files, you will only need to copy over a `google-services.json` file.

Some extra configuration needs to be done. Most of this can be automated by the
[firebase CLI](https://firebase.google.com/docs/cli). First follow the [install
docs](https://firebase.google.com/docs/cli) to setup the firebase CLI. You will
also need to run `firebase login` at some point. (If you did Lab 7, then this
should be all set up.)

Next, in a terminal which can access `firebase` -- any terminal on Linux / MacOS
or the special "firebase" terminal on Windows -- run:

```sh
scripts/setup-firebase.sh  # Associate your clone with your firebase project
firebase deploy  # Configure your firebase app with our project settings
```

**Note**, if the above doesn't work on windows, create a file in your
repository root called `.firebaserc` with the following contents. (The
`setup-firebase.sh` script will do this, if you can run it.)

```json
{
  "projects": {
    "default": "your-project-id"
  }
}
```

You will have to replave `your-project-id` with the id of your firebase
project we created earlier.

After the `.firebaserc` file has been created, you can run `firebase deploy`.

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
