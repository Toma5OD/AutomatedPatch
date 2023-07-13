#!/bin/bash

set -e # Exit script on first error

# Prepare Environment
echo "Cleaning workspace..."
rm -rf *

# Download and Unpack Zip
echo "Downloading zip file..."
curl -O $URL/$ZIP_FILE
echo "Unpacking zip file..."
unzip $ZIP_FILE

# Patch Creation
echo "Creating patch..."
java -jar $PATCH_CREATOR_PATH -p sso -v $RELEASE -tm -debug -i 12345 -d "test patch" -c $JAR_FILE

# Patch Implementation
echo "Implementing patch..."
mv $RH_SSO_PATH $RH_SSO_PATH.backup
cp /path/to/generated/patch/file $RH_SSO_PATH

# Upload and Stage Release
echo "Uploading zip file..."
rsync -rlp --info=progress2 $ZIP_FILE $SSH_USER@$SSH_HOST:$TARGET_DIR
echo "Staging release..."
ssh $SSH_USER@$SSH_HOST 'stage-mw-release $RELEASE'
