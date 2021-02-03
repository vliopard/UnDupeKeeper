@ echo off & setlocal
cls

setlocal enabledelayedexpansion

set basedir=c:\vliopard\tests\
set testnro=0

REM ##############################################################
call:start_test "Add 1 local unique file" label

set name1=file1-test%label%

call:create_file %name1% %name1%

call:check_file %name1% 1

call:end_test

REM ##############################################################
call:start_test "Add 2 local unique files" label

set name1=file1-test%label%
set name2=file2-test%label%

call:create_file %name1% %name1%
call:create_file %name2% %name2%

call:check_file %name1% 1
call:check_file %name2% 1

call:end_test

REM ##############################################################
call:start_test "Add 3 local unique files" label

set name1=file1-test%label%
set name2=file2-test%label%
set name3=file3-test%label%

call:create_file %name1% %name1%
call:create_file %name2% %name2%
call:create_file %name3% %name3%

call:check_file %name1% 1
call:check_file %name2% 1
call:check_file %name3% 1

call:end_test

REM ##############################################################
call:start_test "Delete 1 local file" label

set name1=file1-test%label%

call:create_file %name1% %name1%

call:remove_file %name1%

call:check_file %name1% 0

call:end_test

REM ##############################################################
call:start_test "Move 1 local file to other name" label

set name1=fileOri1-test%label%
set name2=fileRen1-test%label%

call:create_file %name1% %name1%

call:move_file %name1% %name2%

call:check_file %name1% 0
call:check_file %name2% 1

call:end_test

REM ##############################################################
call:start_test "Move 1 local file to other directory same file name" label

set name1=file1-test%label%
set dir1=dir1-test%label%

call:create_file %name1% %name1%

call:create_dir %dir1%
call:move_file %name1% %dir1%\%name1%

call:check_file %name1% 0
call:check_file %dir1%\%name1% 1

call:end_test

REM ##############################################################
call:start_test "Move 1 local file to other directory other file name" label

set name1=fileOri1-test%label%
set name2=fileRen1-test%label%

set dir1=dir1-test%label%

call:create_file %name1% %name1%
call:create_dir %dir1%
call:move_file %name1% %dir1%\%name2%

call:check_file %name1% 0
call:check_file %dir1%\%name2% 1

call:end_test

REM ##############################################################
call:start_test "Add 1 local dupe file" label

set name1=file1-test%label%
set name2=file2-test%label%

call:create_file %name1% %name1%
call:create_file %name2% %name1%

call:check_file %name1% 1

call:check_link %name2% 1

call:compare_file %name1% %name2% 1

call:end_test

REM ##############################################################
call:start_test "Add 2 local dupe file" label

set name1=file1-test%label%
set name2=file2-test%label%
set name3=file3-test%label%

call:create_file %name1% %name1%
call:create_file %name2% %name1%
call:create_file %name3% %name1%

call:check_file %name1% 1

call:check_link %name2% 1
call:check_link %name3% 1

call:compare_file %name1% %name2% 1
call:compare_file %name1% %name3% 1

call:end_test

REM ##############################################################
call:start_test "Add 3 local dupe file" label

set name1=file1-test%label%
set name2=file2-test%label%
set name3=file3-test%label%
set name4=file4-test%label%

call:create_file %name1% %name1%
call:create_file %name2% %name1%
call:create_file %name3% %name1%
call:create_file %name4% %name1%

call:check_file %name1% 1

call:check_link %name2% 1
call:check_link %name3% 1
call:check_link %name4% 1

call:compare_file %name1% %name2% 1
call:compare_file %name1% %name3% 1
call:compare_file %name1% %name4% 1

call:end_test

REM ##############################################################
call:start_test "Delete 1 local link file" label

set name1=file1-test%label%
set name2=file2-test%label%

call:create_file %name1% %name1%
call:create_file %name2% %name1%

call:remove_file %name2%

call:check_file %name1% 1

call:check_link %name2% 0

call:end_test

REM ##############################################################
call:start_test "Move 1 local link file to other name" label

set name1=file1-test%label%
set name2=file2-test%label%
set name3=file3-test%label%

call:create_file %name1% %name1%
call:create_file %name2% %name1%
call:move_file %name2% %name3%

call:check_file %name1% 1

call:check_link %name2% 0
call:check_link %name3% 1

call:end_test

REM ##############################################################
call:start_test "Move 1 local link file to other directory same link name" label

set name1=fileOri1-test%label%
set name2=fileRen1-test%label%

set dir1=dir1-test%label%

call:create_file %name1% %name1%
call:create_file %name2% %name1%

call:create_dir %dir1%

call:move_file %name2% %dir1%\%name2%

call:check_file %name1% 1

call:check_link %name2% 0
call:check_link %dir1%\%name2% 1

call:end_test

REM ##############################################################
call:start_test "Move 1 local link file to other directory other link name" label
call:create_file aaaa aaaa
call:create_file bbbb aaaa
call:create_dir mydir3
call:move_file bbbb mydir3\cccc

call:check_file aaaa file
call:assert %file% 1

call:check_link bbbb link
call:assert %link% 0

call:check_link mydir3\cccc link
call:assert %link% 1
call:end_test

REM ##############################################################
call:start_test "Delete 1 local parent file" label
call:create_file dddd dddd
call:create_file eeee dddd
call:create_file ffff dddd
call:create_file gggg dddd
call:remove_file dddd

call:check_file dddd file
call:assert %file% 0

call:check_link eeee link
call:assert %link% 0

call:check_link ffff link
call:assert %link% 0

call:check_link gggg link
call:assert %link% 0
call:end_test

REM ##############################################################
call:start_test "Move 1 local parent file to other name" label
call:create_file eeee eeee
call:create_file ffff eeee
call:move_file eeee gggg

call:check_file eeee file
call:assert %file% 0

call:check_link ffff link
call:assert %link% 1

call:check_file gggg file
call:assert %file% 1
call:end_test

REM ##############################################################
call:start_test "Move 1 local parent file to other directory same file name" label
call:create_file hhhh hhhh
call:create_file iiii hhhh
call:create_dir mydir4
call:move_file hhhh mydir4\hhhh

call:check_file hhhh file
call:assert %file% 0

call:check_link iiii link
call:assert %link% 1

call:check_file mydir4\hhhh file
call:assert %file% 1
call:end_test

REM ##############################################################
call:start_test "Move 1 local parent file to other directory other file name" label
call:create_file jjjj jjjj
call:create_file kkkk jjjj
call:create_dir mydir5
call:move_file jjjj mydir5\llll

call:check_file jjjj file
call:assert %file% 0

call:check_link kkkk link
call:assert %link% 1

call:check_file mydir5\llll file
call:assert %file% 1
call:end_test

REM ##############################################################
call:start_test "Recover 1 local file with no links" label
call:create_file mmmm mmmm

call:check_file mmmm file
call:assert %file% 1

call:remove_file mmmm

call:check_file mmmm file
call:assert %file% 0

call:create_file mmmm mmmm

call:check_file mmmm file
call:assert %file% 1
call:end_test

REM ##############################################################
call:start_test "Recover 1 local parent file with 1 link" label
call:create_file nnnn nnnn
call:create_file oooo nnnn

call:check_file nnnn file
call:assert %file% 1

call:check_link oooo link
call:assert %link% 1

call:remove_file nnnn

call:check_file nnnn file
call:assert %file% 0

call:check_link oooo link
call:assert %link% 0

call:create_file nnnn nnnn

call:check_file nnnn file
call:assert %file% 1

call:check_link oooo link
call:assert %link% 1
call:end_test

REM ##############################################################
call:start_test "Recover 1 local parent file with 2 links" label
call:create_file pppp pppp
call:create_file qqqq pppp
call:create_file rrrr pppp

call:check_file pppp file
call:assert %file% 1

call:check_link qqqq link
call:assert %link% 1

call:check_link rrrr link
call:assert %link% 1

call:remove_file pppp

call:check_file pppp file
call:assert %file% 0

call:check_link qqqq link
call:assert %link% 0

call:check_link rrrr link
call:assert %link% 0

call:create_file pppp pppp

call:check_file pppp file
call:assert %file% 1

call:check_link qqqq link
call:assert %link% 1

call:check_link rrrr link
call:assert %link% 1
call:end_test

REM ##############################################################
call:start_test "Recover 1 local parent file with 3 links" label
call:create_file aaaaa aaaaa
call:create_file bbbbb aaaaa
call:create_file ccccc aaaaa
call:create_file ddddd aaaaa

call:check_file aaaaa file
call:assert %file% 1

call:check_link bbbbb link
call:assert %link% 1

call:check_link ccccc link
call:assert %link% 1

call:check_link ddddd link
call:assert %link% 1

call:remove_file aaaaa

call:check_file aaaaa file
call:assert %file% 0

call:check_link bbbbb link
call:assert %link% 0

call:check_link ccccc link
call:assert %link% 0

call:check_link ddddd link
call:assert %link% 0

call:create_file aaaaa aaaaa

call:check_file aaaaa file
call:assert %file% 1

call:check_link bbbbb link
call:assert %link% 1

call:check_link ccccc link
call:assert %link% 1

call:check_link ddddd link
call:assert %link% 1
call:end_test

REM ##############################################################
call:start_test "Recover 1 local parent file with 1 link in other directory" label
call:create_file aaaaaa aaaaaa
call:create_dir mydir6
call:create_file mydir6\bbbbbb aaaaaa

call:check_file aaaaaa file
call:assert %file% 1

call:check_link mydir6\bbbbbb link
call:assert %link% 1

call:remove_file aaaaaa

call:check_file aaaaaa file
call:assert %file% 0

call:check_link mydir6\bbbbbb link
call:assert %link% 0

call:create_file aaaaaa aaaaaa

call:check_file aaaaaa file
call:assert %file% 1

call:check_link mydir6\bbbbbb link
call:assert %link% 1
call:end_test

REM ##############################################################
call:start_test "Recover 1 local parent file with 2 links in different directories" label
call:create_file aaaaaaa aaaaaaa
call:create_dir mydir7
call:create_file mydir7\bbbbbbb aaaaaaa
call:create_file mydir7\ccccccc aaaaaaa

call:check_file aaaaaaa file
call:assert %file% 1

call:check_link mydir7\bbbbbbb link
call:assert %link% 1

call:check_link mydir7\ccccccc link
call:assert %link% 1

call:remove_file aaaaaaa

call:check_file aaaaaaa file
call:assert %file% 0

call:check_link mydir7\bbbbbbb link
call:assert %link% 0

call:check_link mydir7\ccccccc link
call:assert %link% 0

call:create_file aaaaaaa aaaaaaa

call:check_file aaaaaaa file
call:assert %file% 1

call:check_link mydir7\bbbbbbb link
call:assert %link% 1

call:check_link mydir7\ccccccc link
call:assert %link% 1
call:end_test

REM ##############################################################
call:start_test "Recover 1 local parent file with 3 links in different directories" label
call:create_file aaaaaaaa aaaaaaaa
call:create_dir mydir8
call:create_file mydir8\bbbbbbbb aaaaaaaa
call:create_file mydir8\cccccccc aaaaaaaa
call:create_file mydir8\dddddddd aaaaaaaa

call:check_file aaaaaaaa file
call:assert %file% 1

call:check_link mydir8\bbbbbbbb link
call:assert %link% 1

call:check_link mydir8\cccccccc link
call:assert %link% 1

call:check_link mydir8\dddddddd link
call:assert %link% 1

call:remove_file aaaaaaaa

call:check_file aaaaaaaa file
call:assert %file% 0

call:check_link mydir8\bbbbbbbb link
call:assert %link% 0

call:check_link mydir8\cccccccc link
call:assert %link% 0

call:check_link mydir8\dddddddd link
call:assert %link% 0

call:create_file aaaaaaaa aaaaaaaa

call:check_file aaaaaaaa file
call:assert %file% 1

call:check_link mydir8\bbbbbbbb link
call:assert %link% 1

call:check_link mydir8\cccccccc link
call:assert %link% 1

call:check_link mydir8\dddddddd link
call:assert %link% 1
call:end_test

REM ##############################################################
call:start_test "Recover 1 parent from dir1 to dir2 with same name" label
call:end_test

REM ##############################################################
call:start_test "Recover 1 parent from dir1 to dir2 with same name and 1 child link" label
call:end_test

REM ##############################################################
call:start_test "Recover 1 parent from dir1 to dir2 with same name and 2 child link" label
call:end_test

REM ##############################################################
call:start_test "Recover 1 parent from dir1 to dir2 with different name" label
call:end_test

REM ##############################################################
call:start_test "Recover 1 parent from dir1 to dir2 with different name and 1 child link" label
call:end_test

REM ##############################################################
call:start_test "Recover 1 parent from dir1 to dir2 with different name and 2 child link" label
call:end_test

REM ##############################################################
call:start_test "Create new unique file which its path is the same of a removed link from a removed parent" label
call:end_test

REM ##############################################################
GOTO EOF

REM ##############################################################
:start_test
set /A testnro=testnro+1
echo ===========================================
call:to_upper "%~1" upperword
call:leading %testnro% retval
echo TEST #%retval%) %upperword%
echo ===========================================
set %~2=%retval%
EXIT /B 0

REM ##############################################################
:create_file
set filename1=%~1
set fcontent1=%~2
echo %fcontent1% > !basedir!%filename1%
echo !basedir!%filename1%
call:wait_time
EXIT /B 0

REM ##############################################################
:remove_file
set filename1=%~1
del !basedir!%filename1%
echo !basedir!%filename1%*
call:wait_time
EXIT /B 0

REM ##############################################################
:move_file
set filename1=%~1
set filename2=%~2    
move !basedir!%filename1% !basedir!%filename2%
echo !basedir!%filename2%
call:wait_time
EXIT /B 0

REM ##############################################################
:create_dir
set filename1=%~1
mkdir !basedir!%filename1%
echo !basedir!%filename1%
call:wait_time
EXIT /B 0

REM ##############################################################
:check_file
if exist !basedir!%~1 (
    call:assert 1 %~2
) else (
    call:assert 0 %~2
)
EXIT /B 0

REM ##############################################################
:check_link
set tmpfile=temp_file.tmp
fsutil reparsepoint query "!basedir!%~1" > "%tmpfile%"
if %errorlevel% == 0 call:assert 1 %~2
if %errorlevel% == 1 call:assert 0 %~2
del temp_file.tmp
EXIT /B 0

REM ##############################################################
:assert
if %~1==%~2 (
    echo =
    echo ======= [ PASSED ] =======
    echo =
) else (
    echo =
    echo ======= [ FAILED ] =======
    echo =
)
EXIT /B 0

REM ##############################################################
:compare_file
fc /b !basedir!%~1 !basedir!%~2 > nul 2>&1
if %errorlevel% == 0 call:assert 1 %~3
if %errorlevel% == 1 call:assert 0 %~3
EXIT /B 0

REM ##############################################################
:end_test
echo ===========================================
call:leading %testnro% retval
echo TEST #%retval%) DONE
echo ===========================================
pause
EXIT /B 0

REM ##############################################################
:wait_time
timeout 1 > NUL
EXIT /B 0

REM ##############################################################
:to_upper
set upper=
set "str=%~1"
for /f "skip=2 delims=" %%I in ('tree "\%str%"') do if not defined upper set "upper=%%~I"
set "upper=%upper:~3%"
set %~2=%upper%
EXIT /B 0

REM ##############################################################
:leading
set count=%~1
for /L %%i in (1, 1, %count%) do (
     set "formattedValue=000000%%i"
     set retval=!formattedValue:~-3!
)
set %~2=%retval%
EXIT /B 0

REM ##############################################################
:EOF