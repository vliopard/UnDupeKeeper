@ echo off & setlocal
cls

setlocal enabledelayedexpansion

set basedir=c:\vliopard\tests\
set delaytm=1
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
rem     echo ${basedir}${filename2}
rem     mv ${basedir}${filename1} ${basedir}${filename2}
rem     read -t ${timeout} -p "${basedir}${filename2}"
rem     echo -
rem }

rem call_create_dir()
rem {
rem     filename1=$1
rem     echo /${basedir}${filename1}/
rem     mkdir ${basedir}${filename1}
rem     read -t ${timeout} -p "${basedir}${filename1}"
rem     echo -
rem }

rem call_check_file()
rem {
rem     filename1=$1
rem     echo _
rem     echo REAL FILE ${basedir}${filename1}
rem     if [ -e ${basedir}${filename1} ]
rem     then
rem         call_assert 1 $2
rem     else
rem         call_assert 0 $2
rem     fi
rem     call_check_hard ${basedir}${filename1} $2
rem }

rem call_check_link()
rem {
rem     filename1=$1
rem     echo _
rem     echo LINK FILE ${basedir}${filename1}
rem     if [ -h ${basedir}${filename1} ]
rem     then
rem         call_assert 1 $2
rem     else
rem         call_assert 0 $2
rem     fi
rem     call_check_soft ${basedir}${filename1} $2
rem }

rem call_check_hard()
rem {
rem     echo _
rem     echo HARD FILE $1
rem     if grep -r $1 file_table.txt
rem     then
rem         call_assert 1 $2
rem     else
rem         call_assert 0 $2
rem     fi
rem }

rem call_check_soft()
rem {
rem     echo _
rem     echo SOFT FILE $1
rem     if grep -r $1 file_links.txt
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
rem         echo -------------------------------------------
rem         echo =============== [ PASSED ] ================
rem         echo -------------------------------------------
rem     else
rem         echo -------------------------------------------
rem         echo =============== [ FAILED ] ================
rem         echo -------------------------------------------
rem     fi
rem }

rem call_compare_file()
rem {
rem     #diff --brief $1 $2
rem     #comp_value=$?
rem     #if [ $comp_value -eq 0 ]
rem     echo $1 == $2
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
rem     call_pause
rem }

rem call_pause()
rem {
rem     read -p "Press any key to continue..."
rem }


REM ##############################################################
call:start_test "Add 1 local unique file"

set name1=test%label%-file1

call:create_file %name1% %name1%

call:check_file %name1% 1

call:end_test

REM ##############################################################
call:start_test "Add 2 local unique files"

set name1=test%label%-file1
set name2=test%label%-file2

call:create_file %name1% %name1%
call:create_file %name2% %name2%

call:check_file %name1% 1
call:check_file %name2% 1

call:end_test

REM ##############################################################
call:start_test "Add 3 local unique files"

set name1=test%label%-file1
set name2=test%label%-file2
set name3=test%label%-file3

call:create_file %name1% %name1%
call:create_file %name2% %name2%
call:create_file %name3% %name3%

call:check_file %name1% 1
call:check_file %name2% 1
call:check_file %name3% 1

call:end_test

REM ##############################################################
call:start_test "Delete 1 local file"

set name1=test%label%-file1

call:create_file %name1% %name1%

call:check_file %name1% 1

pause

call:remove_file %name1%

call:check_file %name1% 0

call:end_test

REM ##############################################################
call:start_test "Move 1 local file to other name"

set name1=test%label%-source1
set name2=test%label%-target1

call:create_file %name1% %name1%

pause

call:move_file %name1% %name2%

call:check_file %name1% 0
call:check_file %name2% 1

call:end_test

REM ##############################################################
call:start_test "Move 1 local file to other directory same file name"

set name1=test%label%-file1
set dir1=test%label%-dir1

call:create_file %name1% %name1%

call:create_dir %dir1%

call:check_file %name1% 1
call:check_file %dir1%\%name1% 0

pause

call:move_file %name1% %dir1%\%name1%

call:check_file %name1% 0
call:check_file %dir1%\%name1% 1

call:end_test

REM ##############################################################
call:start_test "Move 1 local file to other directory other file name"

set name1=test%label%-source1
set name2=test%label%-target1

set dir1=test%label%-dir1

call:create_file %name1% %name1%

call:create_dir %dir1%

call:check_file %name1% 1
call:check_file %dir1%\%name2% 0

pause

call:move_file %name1% %dir1%\%name2%

call:check_file %name1% 0
call:check_file %dir1%\%name2% 1

call:end_test

REM ##############################################################
call:start_test "Add 1 local dupe file"

set name1=test%label%-file1
set name2=test%label%-file2

call:create_file %name1% %name1%
call:create_file %name2% %name1%

call:check_file %name1% 1

call:check_link %name2% 1

call:compare_file %name1% %name2% 1

call:end_test

REM ##############################################################
call:start_test "Add 2 local dupe file"

set name1=test%label%-file1
set name2=test%label%-file2
set name3=test%label%-file3

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

set name1=test%label%-file1
set name2=test%label%-file2
set name3=test%label%-file3
set name4=test%label%-file4

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

set name1=test%label%-file1
set name2=test%label%-file2

call:create_file %name1% %name1%
call:create_file %name2% %name1%

pause

call:remove_file %name2%

call:check_file %name1% 1

call:check_link %name2% 0

call:end_test

REM ##############################################################
call:start_test "Move 1 local link file to other name"

set name1=test%label%-file1
set name2=test%label%-source1
set name3=test%label%-target1

call:create_file %name1% %name1%
call:create_file %name2% %name1%

pause

call:move_file %name2% %name3%

call:check_file %name1% 1

call:check_link %name2% 0
call:check_link %name3% 1

call:end_test

REM ##############################################################
call:start_test "Move 1 local link file to other directory same link name"

set name1=test%label%-source1
set name2=test%label%-target1

set dir1=test%label%-dir1

call:create_file %name1% %name1%
call:create_file %name2% %name1%

call:create_dir %dir1%

pause

call:move_file %name2% %dir1%\%name2%

call:check_file %name1% 1

call:check_link %name2% 0
call:check_link %dir1%\%name2% 1

call:end_test

REM ##############################################################
call:start_test "Move 1 local link file to other directory other link name"

set name1=test%label%-file1
set name2=test%label%-source1
set name3=test%label%-target1

set dir1=test%label%-dir1

call:create_file %name1% %name1%
call:create_file %name2% %name1%

call:create_dir %dir1%

pause

call:move_file %name2% %dir1%\%name3%

call:check_file %name1% 1

call:check_link %name2% 0
call:check_link %dir1%\%name3% 1

call:end_test

REM ##############################################################
call:start_test "Delete 1 local parent file"

set name1=test%label%-file1
set name2=test%label%-file2
set name3=test%label%-file3
set name4=test%label%-file4

call:create_file %name1% %name1%
call:create_file %name2% %name1%
call:create_file %name3% %name1%
call:create_file %name4% %name1%

pause

call:remove_file %name1%

call:check_file %name1% 0

call:check_link %name2% 0
call:check_link %name3% 0
call:check_link %name4% 0

call:end_test

REM ##############################################################
call:start_test "Move 1 local parent file to other name"

set name1=test%label%-source1
set name2=test%label%-file1
set name3=test%label%-target1

call:create_file %name1% %name1%
call:create_file %name2% %name1%

pause

call:move_file %name1% %name3%

call:check_file %name1% 0

call:check_link %name2% 1

call:check_file %name3% 1

call:end_test

REM ##############################################################
call:start_test "Move 1 local parent file to other directory same file name"

set name1=test%label%-file1
set name2=test%label%-file2

set dir1=test%label%-dir1

call:create_file %name1% %name1%
call:create_file %name2% %name1%

call:create_dir %dir1%

pause

call:move_file %name1% %dir1%\%name1%

call:check_file %name1% 0

call:check_link %name2% 1

call:check_file %dir1%\%name1% 1

call:end_test

REM ##############################################################
call:start_test "Move 1 local parent file to other directory other file name"

set name1=test%label%-source1
set name2=test%label%-file1
set name3=test%label%-target2

set dir1=test%label%-dir1

call:create_file %name1% %name1%
call:create_file %name2% %name1%

call:create_dir %dir1%

pause

call:move_file %name1% %dir1%\%name3%

call:check_file %name1% 0

call:check_link %name2% 1

call:check_file %dir1%\%name3% 1

call:end_test

REM ##############################################################
call:start_test "Recover 1 local file with no links"

set name1=test%label%-file1

call:create_file %name1% %name1%

call:check_file %name1% 1

pause

call:remove_file %name1%

call:check_file %name1% 0

pause

call:create_file %name1% %name1%

call:check_file %name1% 1

call:end_test

REM ##############################################################
call:start_test "Recover 1 local parent file with 1 link"

set name1=test%label%-file1
set name2=test%label%-file2

call:create_file %name1% %name1%
call:create_file %name2% %name1%

call:check_file %name1% 1

call:check_link %name2% 1

pause

call:remove_file %name1%

call:check_file %name1% 0

call:check_link %name2% 0

pause

call:create_file %name1% %name1%

call:check_file %name1% 1

call:check_link %name2% 1

call:end_test

REM ##############################################################
call:start_test "Recover 1 local parent file with 2 links"

set name1=test%label%-file1
set name2=test%label%-file2
set name3=test%label%-file3

call:create_file %name1% %name1%
call:create_file %name2% %name1%
call:create_file %name3% %name1%

call:check_file %name1% 1

call:check_link %name2% 1
call:check_link %name3% 1

pause

call:remove_file %name1%

call:check_file %name1% 0

call:check_link %name2% 0
call:check_link %name3% 0

pause

call:create_file %name1% %name1%

call:check_file %name1% 1

call:check_link %name2% 1
call:check_link %name3% 1

call:end_test

REM ##############################################################
call:start_test "Recover 1 local parent file with 3 links"

set name1=test%label%-file1
set name2=test%label%-file2
set name3=test%label%-file3
set name4=test%label%-file4

call:create_file %name1% %name1%
call:create_file %name2% %name1%
call:create_file %name3% %name1%
call:create_file %name4% %name1%

call:check_file %name1% 1

call:check_link %name2% 1
call:check_link %name3% 1
call:check_link %name4% 1

pause

call:remove_file %name1%

call:check_file %name1% 0

call:check_link %name2% 0
call:check_link %name3% 0
call:check_link %name4% 0

pause

call:create_file %name1% %name1%

call:check_file %name1% 1

call:check_link %name2% 1
call:check_link %name3% 1
call:check_link %name4% 1

call:end_test

REM ##############################################################
call:start_test "Recover 1 local parent file with 1 link in other directory"

set name1=test%label%-file1
set name2=test%label%-file2
set name3=test%label%-file3

set dir1=test%label%-dir1

call:create_file %name1% %name1%

call:create_dir %dir1%

call:create_file %dir1%\%name2% %name1%

call:check_file %name1% 1

call:check_link %dir1%\%name2% 1

pause

call:remove_file %name1%

call:check_file %name1% 0

call:check_link %dir1%\%name2% 0

pause

call:create_file %name1% %name1%

call:check_file %name1% 1

call:check_link %dir1%\%name2% 1

call:end_test

REM ##############################################################
call:start_test "Recover 1 local parent file with 2 links in different directories"

set name1=test%label%-file1
set name2=test%label%-file2
set name3=test%label%-file3

set dir1=test%label%-dir1

call:create_file %name1% %name1%

call:create_dir %dir1%

call:create_file %dir1%\%name2% %name1%
call:create_file %dir1%\%name3% %name1%

call:check_file %name1% 1

call:check_link %dir1%\%name2% 1
call:check_link %dir1%\%name3% 1

pause

call:remove_file %name1%

call:check_file %name1% 0

call:check_link %dir1%\%name2% 0
call:check_link %dir1%\%name3% 0

pause

call:create_file %name1% %name1%

call:check_file %name1% 1

call:check_link %dir1%\%name2% 1
call:check_link %dir1%\%name3% 1

call:end_test

REM ##############################################################
call:start_test "Recover 1 local parent file with 3 links in different directories"

set name1=test%label%-file1
set name2=test%label%-file2
set name3=test%label%-file3
set name4=test%label%-file4

set dir1=test%label%-dir1

call:create_file %name1% %name1%

call:create_dir %dir1%

call:create_file %dir1%\%name2% %name1%
call:create_file %dir1%\%name3% %name1%
call:create_file %dir1%\%name4% %name1%

call:check_file %name1% 1

call:check_link %dir1%\%name2% 1
call:check_link %dir1%\%name3% 1
call:check_link %dir1%\%name4% 1

pause

call:remove_file %name1%

call:check_file %name1% 0

call:check_link %dir1%\%name2% 0
call:check_link %dir1%\%name3% 0
call:check_link %dir1%\%name4% 0

pause

call:create_file %name1% %name1%

call:check_file %name1% 1

call:check_link %dir1%\%name2% 1
call:check_link %dir1%\%name3% 1
call:check_link %dir1%\%name4% 1

call:end_test

REM ##############################################################
call:start_test "Recover 1 parent from dir1 to dir2 with same name"

set name1=test%label%-file1

set dir1=test%label%-dir1
set dir2=test%label%-dir2

call:create_dir %dir1%
call:create_dir %dir2%

call:create_file %dir1%\%name1% %name1%

call:check_file %dir1%\%name1% 1
call:check_file %dir2%\%name1% 0

pause

call:remove_file %dir1%\%name1%

call:check_file %dir1%\%name1% 0
call:check_file %dir2%\%name1% 0

pause

call:create_file %dir2%\%name1% %name1%

call:check_file %dir1%\%name1% 0
call:check_file %dir2%\%name1% 1

call:end_test

REM ##############################################################
call:start_test "Recover 1 parent from dir1 to dir2 with same name and 1 child link"

set name1=test%label%-file1
set name2=test%label%-file2

set dir1=test%label%-dir1
set dir2=test%label%-dir2

call:create_dir %dir1%
call:create_dir %dir2%

call:create_file %dir1%\%name1% %name1%
call:create_file %dir1%\%name2% %name1%

call:check_file %dir1%\%name1% 1

call:check_link %dir1%\%name2% 1

pause

call:remove_file %dir1%\%name1%

call:check_file %dir1%\%name1% 0

call:check_link %dir1%\%name2% 0

pause

call:create_file %dir2%\%name1% %name1%

call:check_file %dir1%\%name1% 0

call:check_link %dir1%\%name2% 1

call:check_file %dir2%\%name1% 1

call:check_link %dir2%\%name2% 0

call:end_test

REM ##############################################################
call:start_test "Recover 1 parent from dir1 to dir2 with same name and 2 child link"

set name1=test%label%-file1
set name2=test%label%-file2
set name3=test%label%-file3

set dir1=test%label%-dir1
set dir2=test%label%-dir2

call:create_dir %dir1%
call:create_dir %dir2%

call:create_file %dir1%\%name1% %name1%
call:create_file %dir1%\%name2% %name1%
call:create_file %dir1%\%name3% %name1%

call:check_file %dir1%\%name1% 1

call:check_link %dir1%\%name2% 1
call:check_link %dir1%\%name3% 1

pause

call:remove_file %dir1%\%name1%

call:check_file %dir1%\%name1% 0

call:check_link %dir1%\%name2% 0
call:check_link %dir1%\%name3% 0

pause

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

set name1=test%label%-file1
set name2=test%label%-file2

set dir1=test%label%-dir1
set dir2=test%label%-dir2

call:create_dir %dir1%
call:create_dir %dir2%

call:create_file %dir1%\%name1% %name1%

call:check_file %dir1%\%name1% 1

pause

call:remove_file %dir1%\%name1%

call:check_file %dir1%\%name1% 0

pause

call:create_file %dir2%\%name2% %name1%

call:check_file %dir1%\%name1% 0
call:check_file %dir1%\%name2% 0
call:check_file %dir2%\%name1% 0
call:check_file %dir2%\%name2% 1

call:end_test

REM ##############################################################
call:start_test "Recover 1 parent from dir1 to dir2 with different name and 1 child link"

set name1=test%label%-file1
set name2=test%label%-file2
set name3=test%label%-file3

set dir1=test%label%-dir1
set dir2=test%label%-dir2

call:create_dir %dir1%
call:create_dir %dir2%

call:create_file %dir1%\%name1% %name1%
call:create_file %dir1%\%name3% %name1%

call:check_file %dir1%\%name1% 1
call:check_link %dir1%\%name3% 1

pause

call:remove_file %dir1%\%name1%

call:check_file %dir1%\%name1% 0
call:check_link %dir1%\%name3% 0

pause

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

set name1=test%label%-file1
set name2=test%label%-file2
set name3=test%label%-file3
set name4=test%label%-file4

set dir1=test%label%-dir1
set dir2=test%label%-dir2

call:create_dir %dir1%
call:create_dir %dir2%

call:create_file %dir1%\%name1% %name1%
call:create_file %dir1%\%name3% %name1%
call:create_file %dir1%\%name4% %name1%

call:check_file %dir1%\%name1% 1
call:check_link %dir1%\%name3% 1
call:check_link %dir1%\%name4% 1

pause

call:remove_file %dir1%\%name1%

call:check_file %dir1%\%name1% 0
call:check_link %dir1%\%name3% 0
call:check_link %dir1%\%name4% 0

pause

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

set name1=test%label%-file1
set name2=test%label%-file2
set name3=test%label%-file3
set name4=test%label%-file4
set name5=test%label%-file5

call:create_file %name1% %name1%
call:create_file %name2% %name1%
call:create_file %name3% %name1%
call:create_file %name4% %name1%

call:check_file %name1% 1

call:check_link %name2% 1
call:check_link %name3% 1
call:check_link %name4% 1

pause

call:remove_file %name1%

call:check_file %name1% 0

call:check_link %name2% 0
call:check_link %name3% 0
call:check_link %name4% 0

pause

call:create_file %name2% %name5%

call:check_file %name2% 1

call:check_link %name2% 0
call:check_link %name3% 0
call:check_link %name4% 0

call:end_test

REM ##############################################################
call:start_test "Remove a directory with links"
set name1=test%label%-file1
set name2=test%label%-file2
set name3=test%label%-file3
set name4=test%label%-file4

set dir1=test%label%-dir1

call:create_dir %dir1%

call:create_file %name1% %name1%
call:create_file %dir1%\%name2% %name1%
call:create_file %dir1%\%name3% %name1%
call:create_file %dir1%\%name4% %name1%

call:check_file %name1% 1

call:check_link %dir1%\%name2% 1
call:check_link %dir1%\%name3% 1
call:check_link %dir1%\%name4% 1

pause

call:remove_dir %dir1%

pause

call:check_file %name1% 1

call:check_link %dir1%\%name2% 0
call:check_link %dir1%\%name3% 0
call:check_link %dir1%\%name4% 0

call:end_test

REM ##############################################################
call:start_test "Recover a removed directory with links"

set name1=test%label%-file1
set name2=test%label%-file2
set name3=test%label%-file3
set name4=test%label%-file4

set dir1=test%label%-dir1

call:create_dir %dir1%

call:create_file %name1% %name1%
call:create_file %dir1%\%name2% %name1%
call:create_file %dir1%\%name3% %name1%
call:create_file %dir1%\%name4% %name1%

call:check_file %name1% 1

call:check_link %dir1%\%name2% 1
call:check_link %dir1%\%name3% 1
call:check_link %dir1%\%name4% 1

pause

call:remove_file %name1%

call:remove_dir %dir1%

pause

call:check_file %name1% 1

call:check_link %dir1%\%name2% 1
call:check_link %dir1%\%name3% 1
call:check_link %dir1%\%name4% 1

pause

call:create_file %name1% %name1%

call:check_file %name1% 1

call:check_link %dir1%\%name2% 1
call:check_link %dir1%\%name3% 1
call:check_link %dir1%\%name4% 1

call:end_test

REM ##############################################################
call:start_test "Bulk creation of massive files"

set /A delaytm=0

set name01=test%label%-file01
set name02=test%label%-file02
set name03=test%label%-file03
set name04=test%label%-file04
set name05=test%label%-file05
set name06=test%label%-file06
set name07=test%label%-file07
set name08=test%label%-file08
set name09=test%label%-file09
set name10=test%label%-file10
set name11=test%label%-file11
set name12=test%label%-file12
set name13=test%label%-file13
set name14=test%label%-file14
set name15=test%label%-file15
set name16=test%label%-file16
set name17=test%label%-file17
set name18=test%label%-file18
set name19=test%label%-file19
set name20=test%label%-file20
set name21=test%label%-file21
set name22=test%label%-file22
set name23=test%label%-file23
set name24=test%label%-file24
set name25=test%label%-file25
set name26=test%label%-file26
set name27=test%label%-file27
set name28=test%label%-file28
set name29=test%label%-file29
set name30=test%label%-file30
set name31=test%label%-file31
set name32=test%label%-file32
set name33=test%label%-file33
set name34=test%label%-file34
set name35=test%label%-file35
set name36=test%label%-file36
set name37=test%label%-file37
set name38=test%label%-file38
set name39=test%label%-file39
set name40=test%label%-file40

set dir1=test%label%-dir1
set dir2=test%label%-dir2
set dir3=test%label%-dir3
set dir4=test%label%-dir4

call:create_dir %dir1%
call:create_dir %dir2%
call:create_dir %dir3%
call:create_dir %dir4%

call:create_file %name01% %name01%
call:create_file %name02% %name01%
call:create_file %name03% %name01%
call:create_file %name04% %name01%
call:create_file %name05% %name01%
call:create_file %name06% %name01%
call:create_file %name07% %name01%
call:create_file %name08% %name01%
call:create_file %name09% %name01%
call:create_file %name10% %name01%

call:create_file %dir1%\%name11% %name02%
call:create_file %dir1%\%name12% %name02%
call:create_file %dir1%\%name13% %name02%
call:create_file %dir1%\%name14% %name02%
call:create_file %dir1%\%name15% %name02%
call:create_file %dir1%\%name16% %name02%
call:create_file %dir1%\%name17% %name02%
call:create_file %dir1%\%name18% %name02%
call:create_file %dir1%\%name19% %name02%
call:create_file %dir1%\%name20% %name02%

call:create_file %name21% %name03%
call:create_file %dir2%\%name22% %name03%
call:create_file %dir2%\%name23% %name03%
call:create_file %dir2%\%name24% %name03%
call:create_file %dir2%\%name25% %name03%
call:create_file %dir2%\%name26% %name03%
call:create_file %dir2%\%name27% %name03%
call:create_file %dir2%\%name28% %name03%
call:create_file %dir2%\%name29% %name03%
call:create_file %dir2%\%name30% %name03%

call:create_file %dir3%\%name31% %name04%
call:create_file %name32% %name04%
call:create_file %dir3%\%name33% %name04%
call:create_file %name34% %name04%
call:create_file %dir3%\%name35% %name04%
call:create_file %name36% %name04%
call:create_file %dir3%\%name37% %name04%
call:create_file %name38% %name04%
call:create_file %dir3%\%name39% %name04%
call:create_file %name40% %name04%

call:check_file %name01% 1
call:check_link %name02% 1
call:check_link %name03% 1
call:check_link %name04% 1
call:check_link %name05% 1
call:check_link %name06% 1
call:check_link %name07% 1
call:check_link %name08% 1
call:check_link %name09% 1
call:check_link %name10% 1

call:check_file %dir1%\%name11% 1
call:check_link %dir1%\%name12% 1
call:check_link %dir1%\%name13% 1
call:check_link %dir1%\%name14% 1
call:check_link %dir1%\%name15% 1
call:check_link %dir1%\%name16% 1
call:check_link %dir1%\%name17% 1
call:check_link %dir1%\%name18% 1
call:check_link %dir1%\%name19% 1
call:check_link %dir1%\%name20% 1

call:check_file %name21% 1
call:check_link %dir2%\%name22% 1
call:check_link %dir2%\%name23% 1
call:check_link %dir2%\%name24% 1
call:check_link %dir2%\%name25% 1
call:check_link %dir2%\%name26% 1
call:check_link %dir2%\%name27% 1
call:check_link %dir2%\%name28% 1
call:check_link %dir2%\%name29% 1
call:check_link %dir2%\%name30% 1

call:check_file %dir3%\%name31% 1
call:check_link %name32% 1
call:check_link %dir3%\%name33% 1
call:check_link %name34% 1
call:check_link %dir3%\%name35% 1
call:check_link %name36% 1
call:check_link %dir3%\%name37% 1
call:check_link %name38% 1
call:check_link %dir3%\%name39% 1
call:check_link %name40% 1

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
:remove_dir
set filename1=%~1
rmdir !basedir!%filename1%
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
echo \!basedir!%filename1%\
call:wait_time
EXIT /B 0

REM ##############################################################
:check_file
echo _
echo REAL FILE !basedir!%~1
if exist !basedir!%~1 (
    call:assert 1 %~2
) else (
    call:assert 0 %~2
)
call:check_hard !basedir!%~1 %~2
EXIT /B 0

REM ##############################################################
:check_link
echo _
echo LINK FILE !basedir!%~1
set tmpfile=temp_file.tmp
fsutil reparsepoint query "!basedir!%~1" > "%tmpfile%"
if %errorlevel% == 0 call:assert 1 %~2
if %errorlevel% == 1 call:assert 0 %~2
del temp_file.tmp
call:check_soft !basedir!%~1 %~2
EXIT /B 0

REM ##############################################################
:assert
if %~1==%~2 (
    echo -------------------------------------------
    echo --------------- [ PASSED ] ----------------
    echo -------------------------------------------
) else (
    echo -------------------------------------------
    echo --------------- [ FAILED ] ----------------
    echo -------------------------------------------
)
EXIT /B 0

REM ##############################################################
:compare_file
echo !basedir!%~1 == !basedir!%~2
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
if %delaytm% gtr 0 (
    timeout 1 > NUL
)
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
:check_hard
set txt=%~1
echo _
echo HARD FILE %txt%
findstr /C:%txt% file_table.txt >nul 2>&1
if %errorlevel% equ 0 call:assert 1 %~2
if %errorlevel% equ 1 call:assert 0 %~2
EXIT /B 0

REM ##############################################################
:check_soft
set txt=%~1
echo _
echo SOFT FILE %txt%
findstr /C:%txt% file_links.txt >nul 2>&1
if %errorlevel% equ 0 call:assert 1 %~2
if %errorlevel% equ 1 call:assert 0 %~2
EXIT /B 0

REM ##############################################################
:EOF
