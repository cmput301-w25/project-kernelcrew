#!/usr/bin/env bash

# Some parts derived from the oils-for-unix OSH stdlib [1]:
# - _die
# - _log
# - _print-funcs
# - _show-help
# - task dispatch (section at the end)
#
# [1]: https://github.com/oils-for-unix/oils/

set -e

REPO_ROOT=$(cd "$(dirname $0)/.."; pwd)

# --------------------------------------------------------------------------------

_log() {
  ### Write a message to stderr.
  echo "$@" >&2
}

_die() {
  ### Write an error message with the script name, and exit with status 1.
  _log "$0: fatal: $@"
  exit 1
}

# --------------------------------------------------------------------------------

_not-actions() {
  ### Check if we are not in github actions
  test -z "$ACTIONS"
}

_gradle() {
  ### Call the gradle binary.

  # Locally we should call the gradle wrapper
  if _not-actions; then
    "$REPO_ROOT/code/gradlew" "$@"
  fi

  # On github actions we need to use the global command gradle.
  gradle "$@"
}

lint() {
  ### Run a linter over the code
  cd "$REPO_ROOT/code"

  gradle lintDebug
}

unit-test() {
  ### Run all unit tests
  cd "$REPO_ROOT/code"

  gradle test
}

ui-test() {
  ### (ACTIONS ONLY) Run all ui tests
  cd "$REPO_ROOT/code"

  gradle connectedCheck
}

build-dbg-apk() {
  ### Build a debug APK
  cd "$REPO_ROOT/code"

  gradle assembleDebug --stacktrace
}

enable-kvm() {
  ### (ACTIONS ONLY). Enable KVM to speedup instrumented tests.

  # Should only be done in actions
  if _not-actions; then
    _die "KVM should only be enabled via this helper on github actions"
  fi

  local KVM_CFG_ENABLE='KERNEL=="kvm", GROUP="kvm", MODE="0666", OPTIONS+="static_node=kvm"'
  echo "$KVM_CFG_ENABLE" | sudo tee /etc/udev/rules.d/99-kvm4all.rules
  sudo udevadm control --reload-rules
  sudo udevadm trigger --name-match=kvm
}

inflate-ci-googleservices() {
  ### Add a google-services.json file to code/app/

  # Note, we use github actions secrets to store a CI google-services.json file.
  # This is setup for a seperate app which ideally should never be used.
  # However, Firebase has no support to unauthenticated usecases.

  # We don't want to accidentally destroy someone's google-services.json file!
  if test --file "$REPO_ROOT/code/app/google-services.json"; then
    _die "$REPO_ROOT/code/app/google-services.json already exists"
  fi

  # We store the contents as a base64 encoded payload in an environment
  # variable ACTIONS_GOOGLESERVICES
  if test -z "$ACTIONS_GOOGLESERVICES"; then
    _die "ACTIONS_GOOGLESERVICES is not defined. Is this running in github actions?"
  fi

  printf "$ACTIONS_GOOGLESERVICES" | base64 --decode >"$REPO_ROOT/code/app/google-services.json"
  _log "Inflated to $REPO_ROOT/code/app/google-services.json"
}

setup-firebaserc() {
  ### Setup all local firebase config for the CI project
  _log 'Installing the firebase CLI'
  npm install -g firebase-tools

  cat >"$REPO_ROOT/.firebaserc" <<EOF
{
  "projects": {
    "default": "$ACTIONS_FIREBASE_PROJECTID"
  }
}
EOF

  _log 'Wrote config to .firebaserc'
}

start-firebase-emulators() {
  ### Start the firebase emulators

  _log 'Starting emulators'

  firebase --token "$ACTIONS_FIREBASE_TOKEN" emulators:start
}

# - Task File Components ---------------------------------------------------------

_print-funcs() {
  ### Print shell functions in this file that don't start with _ (bash reflection)
  local funcs
  funcs=($(compgen -A function))

  # extdebug makes `declare -F` print the file path, but, annoyingly, only
  # if you pass the function names as arguments.
  shopt -s extdebug

  # bash format:
  # func1 1 path1
  # func2 2 path2  # where 2 is the line number
  declare -F "${funcs[@]}" | awk -v "path=$0" '$3 == path { print $1 }' | grep -v '^_'

  shopt -u extdebug
}

_show-help() {
  echo "Usage: $0 TASK_NAME ARGS..."
  echo
  echo "Tasks:"

  if command -v column >/dev/null; then
    _print-funcs | column
  else
    _print-funcs
  fi
}

case ${1:-} in
  ''|--help|-h)
    _show-help
    exit 0
    ;;
esac

if ! declare -f "$1" >/dev/null; then
  echo "$0: '$1' isn't an action in this task file.  Try '$0 --help'"
  exit 1
fi

"$@"
