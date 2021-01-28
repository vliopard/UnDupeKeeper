@ echo off
set basedir=c:\Temp\
REM Test 01) Add 1 local unique file
echo aaa > %basedir%aaa
pause
REM Test 02) Add 2 local unique files
echo bbb > %basedir%bbb
pause
REM Test 03) Add 3 local unique files
echo ccc > %basedir%ccc
pause
REM Test 04) Delete 1 local file
del %basedir%ccc
pause
REM Test 05) Move 1 local file to other name
REM Test 06) Move 1 local file to other directory same file name
REM Test 07) Move 1 local file to other directory other file name
REM Test 08) Add 1 local dupe file
REM Test 09) Add 2 local dupe file
REM Test 10) Add 3 local dupe file
REM Test 11) Delete 1 local link file
REM Test 12) Move 1 local link file to other name
REM Test 13) Move 1 local link file to other directory same link name
REM Test 14) Move 1 local link file to other directory other link name
REM Test 15) Delete 1 local parent file
REM Test 16) Move 1 local parent file to other name
REM Test 17) Move 1 local parent file to other directory same file name
REM Test 18) Move 1 local parent file to other directory other file name