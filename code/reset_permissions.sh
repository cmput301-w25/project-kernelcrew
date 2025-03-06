#!/bin/bash
# Code from Anthropic, Claude 3.7 Sonnet, "Create a shell script to reset Android app permissions for testing", accessed 05-12-2024
#
# This script resets the permissions and data for the MoodApp for testing purposes.
# It can be run before UI tests to ensure a clean testing environment.
#
# The script performs the following operations:
# 1. Clears the app data, removing any saved preferences, database content, and permissions
# 2. Explicitly revokes location permissions on Android 6.0+ devices
#
# Usage: ./reset_permissions.sh 
#
# Requirements:
# - adb must be installed and accessible in the PATH
# - An Android device or emulator must be connected
#
# Author: Anthropic, Claude 3.7 Sonnet
# Version: 1.0

# Package name of the application
PACKAGE_NAME="com.kernelcrew.moodapp"

# Get Android SDK version of connected device
SDK_VERSION=$(adb shell getprop ro.build.version.sdk)

echo "Device SDK version: $SDK_VERSION"

# Clear app data
echo "Clearing app data..."
adb shell pm clear $PACKAGE_NAME

# For Android 6 and above, explicitly revoke permissions
if [ "$SDK_VERSION" -ge 23 ]; then
  echo "Revoking location permissions..."
  adb shell pm revoke $PACKAGE_NAME android.permission.ACCESS_FINE_LOCATION
  adb shell pm revoke $PACKAGE_NAME android.permission.ACCESS_COARSE_LOCATION
fi

echo "Done. Permissions have been reset for $PACKAGE_NAME" 