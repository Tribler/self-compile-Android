NDK_TOOLCHAIN_VERSION := 4.9
APP_OPTIM    := debug #release
APP_STL      := gnustl_static #stlport_static #gabi++_static #c++_static
APP_MODULES  := zipalign aapt expat libpng libcutils libutils libhost #aidl
APP_ABI      := armeabi armeabi-v7a x86 mips
APP_PLATFORM := android-18