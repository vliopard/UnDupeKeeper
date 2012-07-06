UnDupeKeeper
============

Keep a directory without duplicate files. Just copy files to there and they will be automatically unduplicated.

___________________________
How it works:

This software start monitoring an empty folder of your choice. Every file you copy to that folder will be kept unique. If you try to copy the same file more than once, even inside a sub folder hierarchy, this software will keep just your first unique entry, the other copies will be replaced by a link to the original file.
https://github.com/vliopard/UnDupeKeeper/raw/master/undupekeeper1.png

After copying a bunch of files to your monitored folder, you can see a report to check results:
https://github.com/vliopard/UnDupeKeeper/raw/master/undupekeeper2.png

___________________________
How to install:

Download UnDupeKeeper.jar to your computer: https://github.com/vliopard/UnDupeKeeper/blob/master/UnDupeKeeper.jar?raw=true
Download JNotify (http://jnotify.sourceforge.net/) to your computer: http://sourceforge.net/projects/jnotify/files/jnotify/jnotify-0.94/jnotify-lib-0.94.zip/download 

Copy libraries from JNotify ZIP Package to your Operating System's path:

[Windows32bit] jnotify-lib-0.94.zip\jnotify.dll
[Windows64bit] jnotify-lib-0.94.zip\jnotify_64bit.dll
[Linux 64bit] jnotify-lib-0.94.zip\64-bit Linux\libjnotify.so
[Linux 32bit] jnotify-lib-0.94.zip\libjnotify.jnilib
[Linux 32bit] jnotify-lib-0.94.zip\libjnotify.so

___________________________
How to run:

You can type the following command on a shell prompt or create a batch/script file for doing that:

java -jar UnDupeKeeper.jar > UnDupeKeeper.log 2>&1