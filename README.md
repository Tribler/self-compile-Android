# Self-compiling Android application (Experimental)

_Disclaimer: only works on Android 4.3_r3.1 with Galaxy Nexus. Or Cyanogenmod 12.1 of android 5.1.1, comfirmed on Nexus 5,6 and 10 devices._

## Autonomous smartphone apps: self-compilation, mutation, and viral spreading

Abstract [from the scientific paper](http://arxiv.org/abs/1511.00444):
We present the first smart phone tool that is capable of self-compilation, mutation and viral spreading. Our autonomous app does not require a host computer to alter its functionality, change its appearance and lacks the normal necessity of a central app store to spread among hosts. We pioneered survival skills for mobile software in order to overcome disrupted Internet access due to natural disasters and human made interference, like Internet kill switches or censored networks. Internet kill switches have proven to be an effective tool to eradicate open Internet access and all forms of digital communication within an hour on a country-wide basis. We present the first operational tool that is capable of surviving such digital eradication.

{Part of [Tribler](https://github.com/Tribler/tribler/wiki): 82 contributing developers so far, 1.7 unique users installs to date}

## Dependencies:

### JAVA

platform  -> android-18.jar
compiler  -> ecj-4.5.jar
dexer     -> SDK\build-tools\23.0.0\lib\dx.jar
apk build -> sdklib-24.3.4.jar
signer    -> zipsigner-lib-1.17.jar
             zipsigner-lib-optional-1.17.jar
             zipio-lib-1.8.jar
             kellinwood-loggin-lib-1.1.jar
             sc-core-1.52.0.0.jar
             sc-prov-1.52.0.0.jar
             sc-pkix-1.52.0.0.jar
             sc-pg-1.52.0.0.jar


### NATIVE

aapt      -> Platform_Framework_Base\tools\aapt
aidl      -> Platform_Framework_Base\tools\aidl
androidfw -> Platform_Framework_Base\include\androidfw

zipalign  -> Platform_Build\tools\zipalign
host      -> Platform_Build\lib\host

libpng    -> Platform_External_Libpng
expat     -> Platform_External_Expat
zlib      -> Platform_External_Zlib

libcutils -> Platform_System_Core\libcutils
cutils    -> Platform_System_Core\include\cutils

liblog    -> Platform_System_Core\liblog
log       -> Platform_System_Core\include\log

libutils  -> Platform_System_Core\libutils
utils     -> Platform_System_Core\include\utils

log.h     -> Platform_System_Core\include\android

asset_manager.h -> Platform_Framework_Native\include\android
looper.h        -> Platform_Framework_Native\include\android

zopfli    -> zopfli\src

ld        -> Tool_Chain_Utils\binutils-2.25\ld



## STABLE APIs (at least api level 9)

https://developer.android.com/ndk/guides/stable_apis.html#a18

LOCAL_LDLIBS := -llog (liblog)

LOCAL_LDLIBS := -lz (zlib)

LOCAL_LDLIBS += -landroid (o.a. looper.h)

LOCAL_LDLIBS := -ldl (dynamic linker)


### android-4.3_r3.1
