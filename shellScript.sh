#!/bin/bash

set -xeuo pipefail

gitlab_repo="git@gitlab.cee.redhat.com:keycloak/keycloak-pipeline.git"

export GIT_SSH_COMMAND='ssh -o PreferredAuthentications=publickey -l git'

workdir="patch-testing"
rm -rf "$workdir"
git clone --branch "$PIPELINE_BRANCH" --depth=1 -- "$gitlab_repo" "$workdir"
pushd "$workdir"

# Here, instead of running sync.sh, you'd run your testing script
./run-tests.sh

CHANGE_COUNT="$(git diff --name-only | wc -l)"
export CHANGE_COUNT

if [[ "$BUILD_USER_ID" == "timer" ]]
then
    message="Automatic test $(date -Ihours)"
else
    message="Manual test by $BUILD_USER_ID"
fi

# You may or may not need this git add/commit/push sequence, depending on whether your tests make changes that need to be saved
git add -A results
if git commit -m "$message"
then
    git show HEAD

    if [[ "$DRY_RUN" == "false" ]]
    then
        git push origin "$PIPELINE_BRANCH"
    else
        echo ">>> DRY RUN: GIT PUSH DISABLED <<<" >&2
    fi
else
    echo "No changes: skipping git push" >&2
fi

popd

unset GIT_SSH_COMMAND
