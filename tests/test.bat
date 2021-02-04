@ echo off & setlocal
cls

setlocal enabledelayedexpansion

set basedir=c:\vliopard\tests\
set testnro=0
set label=

rem call_start_test()
rem {
rem     let testnro=testnro+1
rem     echo ===========================================
rem     upperword="$1"
rem     label=$(printf "%03d" ${testnro})
rem     echo TEST #${label}) ${upperword^^}
rem     echo ===========================================
rem }

rem call_create_file()
rem {
rem     filename1=$1
rem     fcontent1=$2
rem     echo ${fcontent1} > ${basedir}${filename1}
rem     read -t ${timeout} -p "${basedir}${filename1}"
rem     echo -
rem }

rem call_remove_file()
rem {
rem     filename1=$1
rem     rm ${basedir}${filename1}
rem     read -t ${timeout} -p "${basedir}${filename1}*"
rem     echo -
rem }

rem call_move_file()
rem {
rem     filename1=$1
rem     filename2=$2    
rem     mv ${basedir}${filename1} ${basedir}${filename2}
rem     read -t ${timeout} -p "${basedir}${filename2}"
rem     echo -
rem }

rem call_create_dir()
rem {
rem     filename1=$1
rem     mkdir ${basedir}${filename1}
rem     read -t ${timeout} -p "${basedir}${filename1}"
rem     echo -
rem }

rem call_check_file()
rem {
rem     filename1=$1
rem     if [ -e ${basedir}${filename1} ]
rem     then
rem         call_assert 1 $2
rem     else
rem         call_assert 0 $2
rem     fi
rem }

rem call_check_link()
rem {
rem     filename1=$1
rem     if [ -h ${basedir}${filename1} ]
rem     then
rem         call_assert 1 $2
rem     else
rem         call_assert 0 $2
rem     fi
rem }

rem call_assert()
rem {
rem     if [ $1 == $2 ]
rem     then
rem         echo ==========================
rem         echo ======= [ PASSED ] =======
rem         echo ==========================
rem     else
rem         echo ==========================
rem         echo ======= [ FAILED ] =======
rem         echo ==========================
rem     fi
rem }

rem call_compare_file()
rem {
rem     #diff --brief $1 $2
rem     #comp_value=$?
rem     #if [ $comp_value -eq 0 ]
rem     if [ cmp -s $1 $2 ]
rem     then
rem         call_assert 1 $3
rem     else
rem         call_assert 0 $3
rem     fi
rem }

rem call_end_test()
rem {
rem     echo ===========================================
rem     echo TEST #${label}) DONE
rem     echo ===========================================
rem     read -p "Press any key to continue..."
rem }


REM ##############################################################
call:start_test "Add 1 local unique file"

set name1=file1-test%label%

call:create_file %name1% %name1%

call:check_file %name1% 1

call:end_test

REM ##############################################################
call:start_test "Add 2 local unique files"

set name1=file1-test%label%
set name2=file2-test%label%

call:create_file %name1% %name1%
call:create_file %name2% %name2%

call:check_file %name1% 1
call:check_file %name2% 1

call:end_test

REM ##############################################################
call:start_test "Add 3 local unique files"

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
call:start_test "Delete 1 local file"

set name1=file1-test%label%

call:create_file %name1% %name1%

call:remove_file %name1%

call:check_file %name1% 0

call:end_test

REM ##############################################################
call:start_test "Move 1 local file to other name"

set name1=fileOri1-test%label%
set name2=fileRen1-test%label%

call:create_file %name1% %name1%

call:move_file %name1% %name2%

call:check_file %name1% 0
call:check_file %name2% 1

call:end_test

REM ##############################################################
call:start_test "Move 1 local file to other directory same file name"

set name1=file1-test%label%
set dir1=dir1-test%label%

call:create_file %name1% %name1%

call:create_dir %dir1%

call:move_file %name1% %dir1%\%name1%

call:check_file %name1% 0
call:check_file %dir1%\%name1% 1

call:end_test

REM ##############################################################
call:start_test "Move 1 local file to other directory other file name"

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
call:start_test "Add 1 local dupe file"

set name1=file1-test%label%
set name2=file2-test%label%

call:create_file %name1% %name1%
call:create_file %name2% %name1%

call:check_file %name1% 1

call:check_link %name2% 1

call:compare_file %name1% %name2% 1

call:end_test

REM ##############################################################
call:start_test "Add 2 local dupe file"

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
call:start_test "Add 3 local dupe file"

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
call:start_test "Delete 1 local link file"

set name1=file1-test%label%
set name2=file2-test%label%

call:create_file %name1% %name1%
call:create_file %name2% %name1%

call:remove_file %name2%

call:check_file %name1% 1

call:check_link %name2% 0

call:end_test

REM ##############################################################
call:start_test "Move 1 local link file to other name"

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
call:start_test "Move 1 local link file to other directory same link name"

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
call:start_test "Move 1 local link file to other directory other link name"

set name1=fileOri1-test%label%
set name2=fileOri2-test%label%
set name3=fileRen2-test%label%

set dir1=dir1-test%label%

call:create_file %name1% %name1%
call:create_file %name2% %name1%

call:create_dir %dir1%

call:move_file %name2% %dir1%\%name3%

call:check_file %name1% 1

call:check_link %name2% 0
call:check_link %dir1%\%name3% 1

call:end_test

REM ##############################################################
call:start_test "Delete 1 local parent file"

set name1=file1-test%label%
set name2=file2-test%label%
set name3=file3-test%label%
set name4=file4-test%label%

call:create_file %name1% %name1%
call:create_file %name2% %name1%
call:create_file %name3% %name1%
call:create_file %name4% %name1%

call:remove_file %name1%

call:check_file %name1% 0

call:check_link %name2% 0
call:check_link %name3% 0
call:check_link %name4% 0

call:end_test

REM ##############################################################
call:start_test "Move 1 local parent file to other name"

set name1=fileOri1-test%label%
set name2=file2-test%label%
set name3=fileRen1-test%label%

call:create_file %name1% %name1%
call:create_file %name2% %name1%

call:move_file %name1% %name3%

call:check_file %name1% 0

call:check_link %name2% 1

call:check_file %name3% 1

call:end_test

REM ##############################################################
call:start_test "Move 1 local parent file to other directory same file name"

set name1=file1-test%label%
set name2=file2-test%label%

set dir1=dir1-test%label%

call:create_file %name1% %name1%
call:create_file %name2% %name1%

call:create_dir %dir1%

call:move_file %name1% %dir1%\%name1%

call:check_file %name1% 0

call:check_link %name2% 1

call:check_file %dir1%\%name1% 1

call:end_test

REM ##############################################################
call:start_test "Move 1 local parent file to other directory other file name"

set name1=file1-test%label%
set name2=file2-test%label%
set name3=file2-test%label%

set dir1=dir1-test%label%

call:create_file %name1% %name1%
call:create_file %name2% %name1%

call:create_dir %dir1%

call:move_file %name1% %dir1%\%name3%

call:check_file %name1% 0

call:check_link %name2% 1

call:check_file %dir1%\%name3% 1

call:end_test

REM ##############################################################
call:start_test "Recover 1 local file with no links"

set name1=file1-test%label%

call:create_file %name1% %name1%

call:check_file %name1% 1

call:remove_file %name1%

call:check_file %name1% 0

call:create_file %name1% %name1%

call:check_file %name1% 1

call:end_test

REM ##############################################################
call:start_test "Recover 1 local parent file with 1 link"

set name1=file1-test%label%
set name2=file2-test%label%

call:create_file %name1% %name1%
call:create_file %name2% %name1%

call:check_file %name1% 1

call:check_link %name2% 1

call:remove_file %name1%

call:check_file %name1% 0

call:check_link %name2% 0

call:create_file %name1% %name1%

call:check_file %name1% 1

call:check_link %name2% 1

call:end_test

REM ##############################################################
call:start_test "Recover 1 local parent file with 2 links"

set name1=file1-test%label%
set name2=file2-test%label%
set name3=file3-test%label%

call:create_file %name1% %name1%
call:create_file %name2% %name1%
call:create_file %name3% %name1%

call:check_file %name1% 1

call:check_link %name2% 1
call:check_link %name3% 1

call:remove_file %name1%

call:check_file %name1% 0

call:check_link %name2% 0
call:check_link %name3% 0

call:create_file %name1% %name1%

call:check_file %name1% 1

call:check_link %name2% 1
call:check_link %name3% 1

call:end_test

REM ##############################################################
call:start_test "Recover 1 local parent file with 3 links"

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

call:remove_file %name1%

call:check_file %name1% 0

call:check_link %name2% 0
call:check_link %name3% 0
call:check_link %name4% 0

call:create_file %name1% %name1%

call:check_file %name1% 1

call:check_link %name2% 1
call:check_link %name3% 1
call:check_link %name4% 1

call:end_test

REM ##############################################################
call:start_test "Recover 1 local parent file with 1 link in other directory"

set name1=file1-test%label%
set name2=file2-test%label%
set name3=file3-test%label%

set dir1=dir1-test%label%

call:create_file %name1% %name1%

call:create_dir %dir1%

call:create_file %dir1%\%name2% %name1%

call:check_file %name1% 1

call:check_link %dir1%\%name2% 1

call:remove_file %name1%

call:check_file %name1% 0

call:check_link %dir1%\%name2% 0

call:create_file %name1% %name1%

call:check_file %name1% 1

call:check_link %dir1%\%name2% 1

call:end_test

REM ##############################################################
call:start_test "Recover 1 local parent file with 2 links in different directories"

set name1=file1-test%label%
set name2=file2-test%label%
set name3=file3-test%label%

set dir1=dir1-test%label%

call:create_file %name1% %name1%

call:create_dir %dir1%

call:create_file %dir1%\%name2% %name1%
call:create_file %dir1%\%name3% %name1%

call:check_file %name1% 1

call:check_link %dir1%\%name2% 1
call:check_link %dir1%\%name3% 1

call:remove_file %name1%

call:check_file %name1% 0

call:check_link %dir1%\%name2% 0
call:check_link %dir1%\%name3% 0

call:create_file %name1% %name1%

call:check_file %name1% 1

call:check_link %dir1%\%name2% 1
call:check_link %dir1%\%name3% 1

call:end_test

REM ##############################################################
call:start_test "Recover 1 local parent file with 3 links in different directories"

set name1=file1-test%label%
set name2=file2-test%label%
set name3=file3-test%label%
set name4=file4-test%label%

set dir1=dir1-test%label%

call:create_file %name1% %name1%

call:create_dir %dir1%

call:create_file %dir1%\%name2% %name1%
call:create_file %dir1%\%name3% %name1%
call:create_file %dir1%\%name4% %name1%

call:check_file %name1% 1

call:check_link %dir1%\%name2% 1
call:check_link %dir1%\%name3% 1
call:check_link %dir1%\%name4% 1

call:remove_file %name1%

call:check_file %name1% 0

call:check_link %dir1%\%name2% 0
call:check_link %dir1%\%name3% 0
call:check_link %dir1%\%name4% 0

call:create_file %name1% %name1%

call:check_file %name1% 1

call:check_link %dir1%\%name2% 1
call:check_link %dir1%\%name3% 1
call:check_link %dir1%\%name4% 1

call:end_test

REM ##############################################################
call:start_test "Recover 1 parent from dir1 to dir2 with same name"

set name1=file1-test%label%

set dir1=dir1-test%label%
set dir2=dir2-test%label%

call:create_dir %dir1%
call:create_dir %dir2%

call:create_file %dir1%\%name1% %name1%

call:check_file %dir1%\%name1% 1
call:check_file %dir2%\%name1% 0

call:remove_file %dir1%\%name1%

call:create_file %dir2%\%name1% %name1%

call:check_file %dir1%\%name1% 0
call:check_file %dir2%\%name1% 1

call:end_test

REM ##############################################################
call:start_test "Recover 1 parent from dir1 to dir2 with same name and 1 child link"

set name1=file1-test%label%
set name2=file2-test%label%

set dir1=dir1-test%label%
set dir2=dir2-test%label%

call:create_dir %dir1%
call:create_dir %dir2%

call:create_file %dir1%\%name1% %name1%
call:create_file %dir1%\%name2% %name1%

call:check_file %dir1%\%name1% 1

call:check_link %dir1%\%name2% 1

call:remove_file %dir1%\%name1%

call:check_file %dir1%\%name1% 0

call:check_link %dir1%\%name2% 0

call:create_file %dir2%\%name1% %name1%

call:check_file %dir1%\%name1% 0

call:check_link %dir1%\%name2% 1

call:check_file %dir2%\%name1% 1

call:check_link %dir2%\%name2% 0

call:end_test

REM ##############################################################
call:start_test "Recover 1 parent from dir1 to dir2 with same name and 2 child link"

set name1=file1-test%label%
set name2=file2-test%label%
set name3=file3-test%label%

set dir1=dir1-test%label%
set dir2=dir2-test%label%

call:create_dir %dir1%
call:create_dir %dir2%

call:create_file %dir1%\%name1% %name1%
call:create_file %dir1%\%name2% %name1%
call:create_file %dir1%\%name3% %name1%

call:check_file %dir1%\%name1% 1

call:check_link %dir1%\%name2% 1
call:check_link %dir1%\%name3% 1

call:remove_file %dir1%\%name1%

call:check_file %dir1%\%name1% 0

call:check_link %dir1%\%name2% 0
call:check_link %dir1%\%name3% 0

call:create_file %dir2%\%name1% %name1%

call:check_file %dir1%\%name1% 0

call:check_link %dir1%\%name2% 1
call:check_link %dir1%\%name3% 1

call:check_file %dir2%\%name1% 1

call:check_link %dir2%\%name2% 0
call:check_link %dir2%\%name3% 0

call:end_test

REM ##############################################################
call:start_test "Recover 1 parent from dir1 to dir2 with different name"

set name1=file1-test%label%
set name2=file2-test%label%

set dir1=dir1-test%label%
set dir2=dir2-test%label%

call:create_dir %dir1%
call:create_dir %dir2%

call:create_file %dir1%\%name1% %name1%

call:check_file %dir1%\%name1% 1

call:remove_file %name1%

call:check_file %dir1%\%name1% 0

call:create_file %dir2%\%name2% %name1%

call:check_file %dir1%\%name1% 0
call:check_file %dir1%\%name2% 0
call:check_file %dir2%\%name1% 0
call:check_file %dir2%\%name2% 1

call:end_test

REM ##############################################################
call:start_test "Recover 1 parent from dir1 to dir2 with different name and 1 child link"

set name1=file1-test%label%
set name2=file2-test%label%
set name3=file3-test%label%

set dir1=dir1-test%label%
set dir2=dir2-test%label%

call:create_dir %dir1%
call:create_dir %dir2%

call:create_file %dir1%\%name1% %name1%
call:create_file %dir1%\%name3% %name1%

call:check_file %dir1%\%name1% 1
call:check_link %dir1%\%name3% 1

call:remove_file %name1%

call:check_file %dir1%\%name1% 0
call:check_link %dir1%\%name3% 0

call:create_file %dir2%\%name2% %name1%

call:check_file %dir1%\%name1% 0
call:check_file %dir1%\%name2% 0

call:check_link %dir1%\%name3% 1

call:check_file %dir2%\%name1% 0
call:check_file %dir2%\%name2% 1

call:check_link %dir2%\%name3% 0

call:end_test

REM ##############################################################
call:start_test "Recover 1 parent from dir1 to dir2 with different name and 2 child link"

set name1=file1-test%label%
set name2=file2-test%label%
set name3=file3-test%label%
set name4=file4-test%label%

set dir1=dir1-test%label%
set dir2=dir2-test%label%

call:create_dir %dir1%
call:create_dir %dir2%

call:create_file %dir1%\%name1% %name1%
call:create_file %dir1%\%name3% %name1%
call:create_file %dir1%\%name4% %name1%

call:check_file %dir1%\%name1% 1
call:check_link %dir1%\%name3% 1
call:check_link %dir1%\%name4% 1

call:remove_file %name1%

call:check_file %dir1%\%name1% 0
call:check_link %dir1%\%name3% 0
call:check_link %dir1%\%name4% 0

call:create_file %dir2%\%name2% %name1%

call:check_file %dir1%\%name1% 0
call:check_file %dir1%\%name2% 0

call:check_link %dir1%\%name3% 1
call:check_link %dir1%\%name4% 1

call:check_file %dir2%\%name1% 0
call:check_file %dir2%\%name2% 1

call:check_link %dir2%\%name3% 0
call:check_link %dir2%\%name4% 0

call:end_test

REM ##############################################################
call:start_test "Create new unique file which its path is the same of a removed link from a removed parent"

set name1=file1-test%label%
set name2=file2-test%label%
set name3=file3-test%label%
set name4=file4-test%label%
set name5=file5-test%label%

call:create_file %name1% %name1%
call:create_file %name2% %name1%
call:create_file %name3% %name1%
call:create_file %name4% %name1%

call:check_file %name1% 1

call:check_link %name2% 1
call:check_link %name3% 1
call:check_link %name4% 1

call:remove_file %name1%

call:check_file %name1% 0

call:check_link %name2% 0
call:check_link %name3% 0
call:check_link %name4% 0

call:create_file %name1% %name5%

call:check_file %name1% 0

call:check_link %name2% 0
call:check_link %name3% 0
call:check_link %name4% 0

call:end_test

REM ##############################################################
GOTO EOF

REM ##############################################################
:start_test
set /A testnro=testnro+1
echo ===========================================
call:to_upper "%~1" upperword
call:leading %testnro%
echo TEST #%label%) %upperword%
echo ===========================================
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
    echo ##########################
    echo ####### [ PASSED ] #######
    echo ##########################
) else (
    echo ##########################
    echo ####### [ FAILED ] #######
    echo ##########################
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
echo TEST #%label%) DONE
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
set label=%retval%
EXIT /B 0

REM ##############################################################
:EOF
