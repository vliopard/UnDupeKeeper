# UnDupeKeeper
*Keeps a directory without duplicate files.*
--
    Just copy files to there and they will be automatically unduplicated.
## How it works:
This software start monitoring an empty folder of your choice. Every file you copy to that folder will be kept unique. If you try to copy the same file more than once, even inside a sub folder hierarchy, this software will keep just your first unique entry, the other copies will be replaced by a link to the original file.
## How to use:
### Usage 1: Fresh database (when UnDupyKeeper is going to run for the first time)
1.)  Run UnDupyKeeper[.bat|.sh] passing --path=<directory_for_dedupe_files>  
2.)  Copy or Move files to <directory_for_dedupe_files>
### Usage 2: Scan database (when a previous database is already set)
1.)  Run UnDupyKeeper[.bat|.sh] passing --path=<directory_for_dedupe_files> and --scan=True  
2.)  Copy or Move files to <directory_for_dedupe_files>
## About
By OTDS H Co.
___
    Vincent Liopard. is a BIUCS Project.
___
## License
**Free Software, Hell Yeah!**