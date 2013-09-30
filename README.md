#UnDupeKeeper

*Keeps a directory without duplicate files.*
--
    Just copy files to there and they will be automatically unduplicated.

For complete information, please, refer to:


[Main Page](http://vliopard.wordpress.com/undupekeeper/) - [Introduction](http://vliopard.wordpress.com/undupekeeper/introduction/) - [Usage Samples](http://vliopard.wordpress.com/undupekeeper/usage-samples/)

##How it works:
This software start monitoring an empty folder of your choice. Every file you copy to that folder will be kept unique. If you try to copy the same file more than once, even inside a sub folder hierarchy, this software will keep just your first unique entry, the other copies will be replaced by a link to the original file: [Settings Screenshot](https://github.com/vliopard/UnDupeKeeper/raw/master/undupekeeper1.png)

After copying a bunch of files to your monitored folder, you can see a report to check results: [Report Viewer Screenshot](https://github.com/vliopard/UnDupeKeeper/raw/master/undupekeeper2.png)

##How to install:
1.) Download [UnDupeKeeper.jar](https://github.com/vliopard/UnDupeKeeper/blob/master/UnDupeKeeper.jar?raw=true) to your computer. 

2.) Download [JNotify](http://jnotify.sourceforge.net/) to your computer: [JNotify094.zip](http://sourceforge.net/projects/jnotify/files/jnotify/jnotify-0.94/jnotify-lib-0.94.zip/download)

3.) Copy the corresponding library from *JNotify ZIP Package* to your **Operating System**'s path:

* **[Windows32bit]** jnotify-lib-0.94.zip/jnotify.dll
* **[Windows64bit]** jnotify-lib-0.94.zip/jnotify_64bit.dll
- **[Linux 64bit]** jnotify-lib-0.94.zip/64-bit Linux/libjnotify.so
- **[Linux 32bit]** jnotify-lib-0.94.zip/libjnotify.jnilib
- **[Linux 32bit]** jnotify-lib-0.94.zip/libjnotify.so

##How to run:
You can type the following commands on a shell prompt or create a batch/script file for doing that:

```
java -jar UnDupeKeeper.jar > UnDupeKeeper.log 2>&1
```

**Other options:**

- Pass directory from line command

```
java -jar UnDupeKeeper.jar -r <DIRECTORY>
```

- Provide a text file list with complete file paths. It will scan files and rename duplicates. Avoid renaming original files from Ascending or Descending order.

```
java -jar UnDupeKeeper.jar -f [<TEXT_FILE_LIST>|<DIRECTORY>] [KEEP_FIRST|KEEP_LAST]
```

**Note** - An entire directory can be saved from renaming process by appending the string in to it:

```
_N0R3n@me_
```

**Example** - \<MyDirectory\> is going to be like below:

```
<MyDirectory_N0R3n@me_> OR <My_N0R3n@me_Directory>
```

##About
*This is an* ***Open Source Project*** *that uses other [General Public License](http://www.gnu.org/copyleft/gpl.html) (GPL) sources from the web.*

By OTDS H Co.
___
    Vincent Liopard. is a BIUCS Project.
___
