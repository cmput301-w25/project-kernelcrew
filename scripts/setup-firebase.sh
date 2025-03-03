#!/usr/bin/env bash

set -e

REPO_ROOT=$(cd "$(dirname $0)/.."; pwd)

# --------------------------------------------------------------------------------

log() {
  ### Write a message to stderr.
  echo "$@" >&2
}

die() {
  ### Write an error message with the script name, and exit with status 1.
  log "$0: fatal: $@"
  exit 1
}

# --------------------------------------------------------------------------------

if ! command -v firebase >/dev/null; then
  die 'Cannot run firebase. Is the Firebase CLI installed?'
fi

if test -f "$REPO_ROOT/.firebaserc"; then
  die 'Firebase already initialized. Delete .firebaserc if you want to reinit'
fi

firebase projects:list

while true; do
  log
  log 'Please select a project to connect to with its "Project ID"'
  read -p 'Project ID> ' PROJECT_ID

  # Not perfect, but prevents against some typos
  if firebase projects:list 2>/dev/null | grep $PROJECT_ID >/dev/null; then
    break 
  fi

  log 'Is that project id in your project list? Check for typos.'
done

log 'Updating .firebaserc'

cat >"$PROJECT_ROOT/.firebaserc" <<EOF
{
  "projects": {
    "default": "$PROJECT_ID"
  }
}
EOF

log 'Done!'
