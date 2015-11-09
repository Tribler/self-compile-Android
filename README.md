# Self-compiling Android application (Experimental)

_Disclaimer: only works on Android 4.3_r3.1 with Galaxy Nexus. Or Cyanogenmod 12.1 of android 5.1.1, comfirmed on Nexus 5,6 and 10 devices._

## Autonomous smartphone apps: self-compilation, mutation, and viral spreading

*Mutate an Android app at the source code level.*

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

### Screenshots

*Howto mutate an Android app at the source code level.*

Simple proof-of-principle user interface. Both the app name and the icon are changed in real-time in the source code shipped inside the app. By selecting "install" an .apk is created of itself using self-compilation.
![screenshot_2015-09-18-16-47-38](https://cloud.githubusercontent.com/assets/325224/11036025/33389dd8-86f6-11e5-9a7b-bbfb2df85853.png)

Changing icon and app name:
![screenshot_2015-09-18-16-48-30](https://cloud.githubusercontent.com/assets/325224/11036035/4654c324-86f6-11e5-9087-12800694a6ff.png)

<selecting install> First self compile step, process resources
![screenshot_2015-09-18-16-48-49](https://cloud.githubusercontent.com/assets/325224/11036087/94ab786a-86f6-11e5-8fd0-7d80bc7cc0bf.png)

Compile source:             
![screenshot_2015-09-18-16-49-29](https://cloud.githubusercontent.com/assets/325224/11036127/df87e2ec-86f6-11e5-949c-30bdef86fdc7.png)

Integrate dependencies:             
![screenshot_2015-09-18-16-49-34](https://cloud.githubusercontent.com/assets/325224/11036134/e9d18a96-86f6-11e5-802e-6264b16869e6.png)

(this takes a while)             
![screenshot_2015-09-18-16-50-09](https://cloud.githubusercontent.com/assets/325224/11036144/f9adf6ac-86f6-11e5-850e-c7d1f832a3c2.png)

Package app:             
![screenshot_2015-09-18-16-55-49](https://cloud.githubusercontent.com/assets/325224/11036171/1d61e77a-86f7-11e5-8093-967457113539.png)

Prepare installation step:             
![screenshot_2015-09-18-17-12-52](https://cloud.githubusercontent.com/assets/325224/11036186/36335ac2-86f7-11e5-86c2-0825081163e7.png)

The magic happens. Without any user intervention it asks for a freshly compiled clone of itself to be installed.:
![screenshot_2015-09-18-17-13-11](https://cloud.githubusercontent.com/assets/325224/11036195/47464838-86f7-11e5-81b8-e864770b6b80.png)

User selected "install"             
![screenshot_2015-09-18-17-13-20](https://cloud.githubusercontent.com/assets/325224/11036216/79505eea-86f7-11e5-9f5e-8161f4d325ad.png)

Install completed             
![screenshot_2015-09-18-17-17-40](https://cloud.githubusercontent.com/assets/325224/11036218/84dd015a-86f7-11e5-8414-57d6a9570d21.png)

Finally open the self-compiled clone of itself. 
![screenshot_2015-09-18-17-17-55](https://cloud.githubusercontent.com/assets/325224/11036221/9274c01e-86f7-11e5-91f0-5d0327ac5181.png)

