# Dependencies:


## JAVA

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


## NATIVE

aapt      -> Platform_Framework_Base\tools
aidl      -> Platform_Framework_Base\tools
androidfw -> Platform_Framework_Base\include

zipalign  -> Platform_Build\tools
host      -> Platform_Build\lib

libcutils -> Platform_System_Core\libcutils
cutils    -> Platform_System_Core\include\cutils

liblog    -> Platform_System_Core\liblog
log       -> Platform_System_Core\include\log

libutils  -> Platform_System_Core\libutils
utils     -> Platform_System_Core\include\utils

asset_manager.h -> Platform_Framework_Native\include\android
looper.h        -> Platform_Framework_Native\include\android

zopfli    -> zopfli


### android-4.3_r3.1