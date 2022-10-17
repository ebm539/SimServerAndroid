# SimServerAndroid-Fork

## Forked from [the original code by zhuowei](https://github.com/zhuowei/SimServerAndroid)

An Android app (currently made for a dual SIM device) to expose an interface for EAP-AKA authentication using the SIM card in the phone (rand,autn -> ck,ik,res).
Requires root and install as a privileged app.
Used on an Android Nougat device.

I wrote a GUI for the app (instead of using the original bash script meant for Android 10+ in adb shell) as my test device wasn't on Android 10 or above, and because I couldn't get the bash script to work when I ported my test device to Android 10 (script was always SIGKILLed and I couldn't figure out why.)

