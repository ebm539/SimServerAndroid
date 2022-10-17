#!/bin/bash

# https://stackoverflow.com/a/31703543

# prefer java 8 by default
export PATH=/usr/lib/jvm/java-8-openjdk/bin/:$PATH

app_package="com.worthdoingbadly.simserver"
dir_app_name="SimServer"
MAIN_ACTIVITY="MainActivity"

ADB="adb" # how you execute adb
ADB_SH="$ADB shell" # this script assumes using `adb root`. for `adb su` see `Caveats`

path_sysapp="/system/priv-app" # assuming the app is priviledged
# if not, read up on https://source.android.com/docs/core/permissions/perms-allowlist
# and see permissions_com.worthdoingbadly.simserver.privileged.xml
# you'll want to copy that file to the relevant location in the source.android.com URL
apk_host="./app/build/outputs/apk/debug/app-debug.apk"
apk_name=$dir_app_name".apk"
apk_target_dir="$path_sysapp/$dir_app_name"
apk_target_sys="$apk_target_dir/$apk_name"

# Delete previous APK
rm -f $apk_host

# Compile the APK: you can adapt this for production build, flavors, etc.
./gradlew assembleDebug || exit -1 # exit on failure

# Install APK: using adb root
$ADB root 2> /dev/null
#$ADB connect <yourdeviceaddresshere>:5555
#$ADB remount # mount system
$ADB shell mount -o remount,rw /system
$ADB push $apk_host $apk_target_sys

# Give permissions
$ADB_SH "chmod 755 $apk_target_dir"
$ADB_SH "chmod 644 $apk_target_sys"

#Unmount system
#$ADB_SH "mount -o remount,ro /"

# Stop the app
$ADB shell "am force-stop $app_package"

# Re execute the app - actually this is done by Android Studio now I think
#$ADB shell "am start -n \"$app_package/$app_package.$MAIN_ACTIVITY\" -a android.intent.action.MAIN -c android.intent.category.LAUNCHER"

## run the app, we don't want null
# nevermind everything has been moved to the main activity, the script isn't used anymore (I couldn't get it to work)
##$ADB shell bash /sdcard/runsimserver.sh imsi
