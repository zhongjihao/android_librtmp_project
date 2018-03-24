#########################################################################
# File Name: build.sh
# Author: zhongjihao
# mail: zhongjihao100@163.com
# Created Time: 2018年01月25日 星期四 19时00分10秒
#########################################################################
#!/bin/bash

CUR_DIR=$(cd "$(dirname "${BASH_SOURCE-$0}")"; pwd)
TOOLCHAIN=$CUR_DIR/android-toolchain
SYSROOT=$CUR_DIR/sysroot

if [ -d "$TOOLCHAIN" ]; then
	rm -rf "$TOOLCHAIN"
	mkdir "$TOOLCHAIN"
else
	mkdir "$TOOLCHAIN"
fi

if [ -d "$SYSROOT" ]; then
	rm -rf "$SYSROOT"
	mkdir "$SYSROOT"
else
	mkdir "$SYSROOT"
fi

echo $ANDROID_NDK_ROOT
$ANDROID_NDK_ROOT/build/tools/make-standalone-toolchain.sh --toolchain=arm-linux-androideabi-4.9 --platform=android-21 --install-dir=$TOOLCHAIN --force 

#TOOLCHAIN=/home/apadmin/Workspace/android-toolchain/armeabi-4.9
#SYSROOT=/home/apadmin/Workspace/android-toolchain/sysroot
#/opt/android/android-ndk-r16/build/tools/make-standalone-toolchain.sh --toolchain=arm-linux-androideabi-4.9 --platform=android-21 --install-dir=$TOOLCHAIN --force

PATH=$TOOLCHAIN/bin:$PATH
make CC=arm-linux-androideabi-gcc APPS=
make install DESTDIR=$SYSROOT


if [ -d "$TOOLCHAIN" ]; then
	rm -rf "$TOOLCHAIN"
fi


