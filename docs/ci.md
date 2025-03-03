# How CI Works

On every PR or commit to `main` github can run some scripts for us. This is
called "continuous integration" or, by github marketing, "GitHub Actions".

Regardless of the term, the idea of CI is to regularly run automated tests and
tools to check the quality of our code. Additionally, we can use CI to generate
some build files such as a debug APK for testing. (This is sometimes called
"continuous deployment" or CD, but usually CD would also deploy to a website,
or publish to google play.)

In our CI system we run 4 scripts:
 - Run a "linter"
 - Run all unit tests
 - Run all UI/expresso tests
 - Build a debug APK

A "linter" is a tool to check for common programming errors. You have one built
into your IDE. We run it here to make sure nothing gets past. Linters are not
fool-proof. They don't always find real bugs and definitely missing some too.
It's best for enforcing a common-ish code style and catching trivial mistakes.

Unit tests and the UI tests are exactly like what you'd run in your IDE. It's
nice to automate these so you can't forget to run them. The UI tests in CI also
have firebase emulators setup, so feel free to expect them to be present when
writing your tests. (Do note, UI tests are slow, so it would be a good idea to
get them working locally first.)

We also build a full debug APK. This is helpful for a few reasons:
- We will always have a latest APK to send off for testing
- We check that the app can actually fully build -- it is surprising how many
  test systems just assume the app can build without error

## Our Implementation

GitHub Actions requires a lot of YAML, but setting up all of this requires a
ton of code. I have kept that all in a `scripts/ci.sh` shell script. It uses a
"task file" pattern so it can be called like `scripts/ci.sh <task-to-run>`.
This helps with code-reuse across CI tasks.

So, the `.github/workflows/android.yml` is mostly a bunch of calls to that
script. It mainly tells GitHub what order to run things in and, most
importantly, how to map "secrets" to environment variables.

Unfortunately Firebase requires an account for event local testing. This sucks
because then I need to given CI access to my account. I don't want anyone else
getting into there, so I've setup "secrets". These are variables which store
the sensitive account keys and tokens which I don't want to share. GitHub
excrypts the tokens and censors them when you print them out. Of course, you
could still write a test which sends my tokens to a database you control.
Please don't.

## Fixing CI Errors

If a test fails, CI will usually turn red. If you click on the failing job from
your PR, you can view all the logs. I usually recommend trying to reproduce
that failure locally though. GitHub actions lacks a lot of tools to debug code
running in CI -- whereas your local machine has it all.
