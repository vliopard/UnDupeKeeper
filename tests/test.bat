@ echo off & setlocal
cls

setlocal enabledelayedexpansion

call:read_ini_value "..\python\UnDupeKeeper.ini" "PATHS" "MAIN_PATH" basedir
set file_links=link_table.txt
set file_table=file_table.txt
set delaycount=1
set delaytm=1
set ptests=0
set ftests=0
set testnro=0
set last_test=042
set label=

rem call_start_test()
rem {
rem     let testnro=testnro+1
rem     echo -e \\"e[1;37;44m===========================================\\e[0m"
rem     upperword="$1"
rem     label=$(printf "%03d" ${testnro})
rem     echo TEST \#${label}/${last_test}\) ${upperword^^}
rem     echo -e \\"e[1;37;44m===========================================\\e[0m"
rem }

rem call_create_file()
rem {
rem     filename1=$1
rem     fcontent1=$2
rem     echo ${fcontent1} > ${basedir}${filename1}
rem     read -t ${timeout} -p "${basedir}${filename1}"
rem     echo  
rem }

rem call_remove_file()
rem {
rem     filename1=$1
rem     rm ${basedir}${filename1}
rem     read -t ${timeout} -p "${basedir}${filename1}*"
rem     echo  
rem }

rem call_move_file()
rem {
rem     filename1=$1
rem     filename2=$2    
rem     echo ${basedir}${filename2}
rem     mv ${basedir}${filename1} ${basedir}${filename2}
rem     read -t ${timeout} -p "${basedir}${filename2}"
rem     echo  
rem }

rem call_create_dir()
rem {
rem     filename1=$1
rem     echo /${basedir}${filename1}/
rem     mkdir ${basedir}${filename1}
rem     read -t ${timeout} -p "${basedir}${filename1}"
rem     echo  
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
rem     if [ $2 -eq 2 ]
rem     then
rem         mark=0
rem         marq=1
rem     else
rem         mark=$2
rem         marq=$2
rem     fi
rem     if [ -h ${basedir}${filename1} ]
rem     then
rem         call_assert 1 ${mark}
rem     else
rem         call_assert 0 ${mark}
rem     fi
rem     call_check_soft ${basedir}${filename1} ${marq}
rem }

rem call_check_hard()
rem {
rem     echo _
rem     echo HARD FILE $1
rem     if grep -r $1 ${file_table}
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
rem     if grep -r $1 ${file_links}
rem     then
rem         call_assert 1 $2
rem     else
rem         call_assert 0 $2
rem     fi
rem }

rem call_remove_dir()
rem {
rem    dirname1=$1
rem    if [ "$(ls -A ${basedir}${dirname1})" ]
rem    then
rem        file_list=${basedir}${dirname1}/'*'
rem        rm ${file_list}
rem    fi
rem    rmdir "${basedir}${dirname1}"
rem    read -t ${timeout} -p "${basedir}${dirname1}*"
rem    echo  
rem }

rem call_assert()
rem {
rem     if [ $1 == $2 ]
rem     then
rem         echo -------------------------------------------
rem         echo -e \\"e[0;30;42m=============== [ PASSED ] ================\\e[0m"
rem         echo -------------------------------------------
rem     else
rem         echo -------------------------------------------
rem         echo -e \\"e[0;30;41m=============== [ FAILED ] ================\\e[0m"
rem         echo -------------------------------------------
rem     fi
rem }

rem call_compare_file()
rem {
rem     #diff --brief $1 $2
rem     #comp_value=$?
rem     #if [ $comp_value -eq 0 ]
rem     echo ${basedir}$1 == ${basedir}$2
rem     if cmp -s ${basedir}$1 ${basedir}$2
rem     then
rem         call_assert 1 $3
rem     else
rem         call_assert 0 $3
rem     fi
rem }

rem call_end_test()
rem {
rem     echo ===========================================
rem     echo TEST \#${label}/${last_test}\) DONE
rem     echo ===========================================
rem     call_pause
rem }

rem call_pause()
rem {
rem     # read -p "Press any key to continue..."
rem     if [ $# -eq 0 ]
rem         then
rem             if [ $delaytm -gt 0 ]
rem                 then
rem                     read -t ${delaycount} -p "Next..."
rem             fi
rem         else
rem             echo ___________________________________
rem             echo Waiting for $1 seconds...
rem             echo -----------------------------------
rem             read -t $1 -p "Next..."
rem     fi
rem     echo ""
rem }

REM ############################################################## TEST_NO_01_TITLE
call:start_test "Add 1 local unique file"

set name1=test%label%-file1

call:create_file %name1% %name1%

call:check_file %name1% 1

call:end_test

REM ############################################################## TEST_NO_02_TITLE
call:start_test "Add 2 local unique files"

set name1=test%label%-file1
set name2=test%label%-file2

call:create_file %name1% %name1%
call:create_file %name2% %name2%

call:check_file %name1% 1
call:check_file %name2% 1

call:end_test

REM ############################################################## TEST_NO_03_TITLE
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

REM ############################################################## TEST_NO_04_TITLE
call:start_test "Delete 1 local file"

set name1=test%label%-file1

call:create_file %name1% %name1%

call:check_file %name1% 1

call:delay_pause

call:remove_file %name1%

call:check_file %name1% 0

call:end_test

REM ############################################################## TEST_NO_05_TITLE
call:start_test "Move 1 local file to other name"

set name1=test%label%-source1
set name2=test%label%-target1

call:create_file %name1% %name1%

call:check_file %name1% 1
call:check_file %name2% 0

call:delay_pause

call:move_file %name1% %name2%

call:check_file %name1% 0
call:check_file %name2% 1

call:end_test

REM ############################################################## TEST_NO_06_TITLE
call:start_test "Move 1 local file to other directory same file name"

set name1=test%label%-file1
set dir1=test%label%-dir1

call:create_file %name1% %name1%

call:create_dir %dir1%

call:check_file %name1% 1
call:check_file %dir1%\%name1% 0

call:delay_pause

call:move_file %name1% %dir1%\%name1%

call:check_file %name1% 0
call:check_file %dir1%\%name1% 1

call:end_test

REM ############################################################## TEST_NO_07_TITLE
call:start_test "Move 1 local file to other directory other file name"

set name1=test%label%-source1
set name2=test%label%-target1

set dir1=test%label%-dir1

call:create_file %name1% %name1%

call:create_dir %dir1%

call:check_file %name1% 1
call:check_file %dir1%\%name2% 0

call:delay_pause

call:move_file %name1% %dir1%\%name2%

call:check_file %name1% 0
call:check_file %dir1%\%name2% 1

call:end_test

REM ############################################################## TEST_NO_08_TITLE
call:start_test "Add 1 local dupe file"

set name1=test%label%-file1
set name2=test%label%-file2

call:create_file %name1% %name1%
call:create_file %name2% %name1%

call:check_file %name1% 1

call:check_link %name2% 1

call:compare_file %name1% %name2% 1

call:end_test

REM ############################################################## TEST_NO_09_TITLE
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

REM ############################################################## TEST_NO_10_TITLE
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

REM ############################################################## TEST_NO_11_TITLE
call:start_test "Delete 1 local link file"

set name1=test%label%-file1
set name2=test%label%-file2

call:create_file %name1% %name1%
call:create_file %name2% %name1%

call:delay_pause

call:remove_file %name2%

call:check_file %name1% 1

call:check_link %name2% 0

call:end_test

REM ############################################################## TEST_NO_12_TITLE
call:start_test "Move 1 local link file to other name"

set name1=test%label%-file1
set name2=test%label%-source1
set name3=test%label%-target1

call:create_file %name1% %name1%
call:create_file %name2% %name1%

call:delay_pause

call:move_file %name2% %name3%

call:check_file %name1% 1

call:check_link %name2% 0
call:check_link %name3% 1

call:end_test

REM ############################################################## TEST_NO_13_TITLE
call:start_test "Move 1 local link file to other directory same link name"

set name1=test%label%-source1
set name2=test%label%-target1

set dir1=test%label%-dir1

call:create_file %name1% %name1%
call:create_file %name2% %name1%

call:create_dir %dir1%

call:delay_pause

call:move_file %name2% %dir1%\%name2%

call:check_file %name1% 1

call:check_link %name2% 0
call:check_link %dir1%\%name2% 1

call:end_test

REM ############################################################## TEST_NO_14_TITLE
call:start_test "Move 1 local link file to other directory other link name"

set name1=test%label%-file1
set name2=test%label%-source1
set name3=test%label%-target1

set dir1=test%label%-dir1

call:create_file %name1% %name1%
call:create_file %name2% %name1%

call:create_dir %dir1%

call:delay_pause

call:move_file %name2% %dir1%\%name3%

call:check_file %name1% 1

call:check_link %name2% 0
call:check_link %dir1%\%name3% 1

call:end_test

REM ############################################################## TEST_NO_15_TITLE
call:start_test "Delete 1 local parent file"

set name1=test%label%-file1
set name2=test%label%-file2
set name3=test%label%-file3
set name4=test%label%-file4

call:create_file %name1% %name1%
call:create_file %name2% %name1%
call:create_file %name3% %name1%
call:create_file %name4% %name1%

call:delay_pause

call:remove_file %name1%

call:check_file %name1% 0

call:check_link %name2% 2
call:check_link %name3% 2
call:check_link %name4% 2

call:end_test

REM ############################################################## TEST_NO_16_TITLE
call:start_test "Move 1 local parent file to other name"

set name1=test%label%-source1
set name2=test%label%-file1
set name3=test%label%-target1

call:create_file %name1% %name1%
call:create_file %name2% %name1%

call:delay_pause

call:move_file %name1% %name3%

call:check_file %name1% 0

call:check_link %name2% 1

call:check_file %name3% 1

call:end_test

REM ############################################################## TEST_NO_17_TITLE
call:start_test "Move 1 local parent file to other directory same file name"

set name1=test%label%-file1
set name2=test%label%-file2

set dir1=test%label%-dir1

call:create_file %name1% %name1%
call:create_file %name2% %name1%

call:create_dir %dir1%

call:delay_pause

call:move_file %name1% %dir1%\%name1%

call:check_file %name1% 0

call:check_link %name2% 1

call:check_file %dir1%\%name1% 1

call:end_test

REM ############################################################## TEST_NO_18_TITLE
call:start_test "Move 1 local parent file to other directory other file name"

set name1=test%label%-source1
set name2=test%label%-file1
set name3=test%label%-target2

set dir1=test%label%-dir1

call:create_file %name1% %name1%
call:create_file %name2% %name1%

call:create_dir %dir1%

call:delay_pause

call:move_file %name1% %dir1%\%name3%

call:check_file %name1% 0

call:check_link %name2% 1

call:check_file %dir1%\%name3% 1

call:end_test

REM ############################################################## TEST_NO_19_TITLE
call:start_test "Recover 1 local file with no links"

set name1=test%label%-file1

call:create_file %name1% %name1%

call:check_file %name1% 1

call:delay_pause

call:remove_file %name1%

call:check_file %name1% 0

call:delay_pause

call:create_file %name1% %name1%

call:check_file %name1% 1

call:end_test

REM ############################################################## TEST_NO_20_TITLE
call:start_test "Recover 1 local parent file with 1 link"

set name1=test%label%-file1
set name2=test%label%-file2

call:create_file %name1% %name1%
call:create_file %name2% %name1%

call:check_file %name1% 1

call:check_link %name2% 1

call:delay_pause

call:remove_file %name1%

call:check_file %name1% 0

call:check_link %name2% 2

call:delay_pause

call:create_file %name1% %name1%

call:check_file %name1% 1

call:check_link %name2% 1

call:end_test

REM ############################################################## TEST_NO_21_TITLE
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

call:delay_pause

call:remove_file %name1%

call:check_file %name1% 0

call:check_link %name2% 2
call:check_link %name3% 2

call:delay_pause

call:create_file %name1% %name1%

call:check_file %name1% 1

call:check_link %name2% 1
call:check_link %name3% 1

call:end_test

REM ############################################################## TEST_NO_22_TITLE
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

call:delay_pause

call:remove_file %name1%

call:check_file %name1% 0

call:check_link %name2% 2
call:check_link %name3% 2
call:check_link %name4% 2

call:delay_pause

call:create_file %name1% %name1%

call:check_file %name1% 1

call:check_link %name2% 1
call:check_link %name3% 1
call:check_link %name4% 1

call:end_test

REM ############################################################## TEST_NO_23_TITLE
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

call:delay_pause

call:remove_file %name1%

call:check_file %name1% 0

call:check_link %dir1%\%name2% 2

call:delay_pause

call:create_file %name1% %name1%

call:check_file %name1% 1

call:check_link %dir1%\%name2% 1

call:end_test

REM ############################################################## TEST_NO_24_TITLE
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

call:delay_pause

call:remove_file %name1%

call:check_file %name1% 0

call:check_link %dir1%\%name2% 2
call:check_link %dir1%\%name3% 2

call:delay_pause

call:create_file %name1% %name1%

call:check_file %name1% 1

call:check_link %dir1%\%name2% 1
call:check_link %dir1%\%name3% 1

call:end_test

REM ############################################################## TEST_NO_25_TITLE
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

call:delay_pause

call:remove_file %name1%

call:check_file %name1% 0

call:check_link %dir1%\%name2% 2
call:check_link %dir1%\%name3% 2
call:check_link %dir1%\%name4% 2

call:delay_pause

call:create_file %name1% %name1%

call:check_file %name1% 1

call:check_link %dir1%\%name2% 1
call:check_link %dir1%\%name3% 1
call:check_link %dir1%\%name4% 1

call:end_test

REM ############################################################## TEST_NO_26_TITLE
call:start_test "Recover 1 parent from dir1 to dir2 with same name"

set name1=test%label%-file1

set dir1=test%label%-dir1
set dir2=test%label%-dir2

call:create_dir %dir1%
call:create_dir %dir2%

call:create_file %dir1%\%name1% %name1%

call:check_file %dir1%\%name1% 1
call:check_file %dir2%\%name1% 0

call:delay_pause

call:remove_file %dir1%\%name1%

call:check_file %dir1%\%name1% 0
call:check_file %dir2%\%name1% 0

call:delay_pause

call:create_file %dir2%\%name1% %name1%

call:check_file %dir1%\%name1% 0
call:check_file %dir2%\%name1% 1

call:end_test

REM ############################################################## TEST_NO_27_TITLE
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

call:delay_pause

call:remove_file %dir1%\%name1%

call:check_file %dir1%\%name1% 0

call:check_link %dir1%\%name2% 2

call:delay_pause

call:create_file %dir2%\%name1% %name1%

call:check_file %dir1%\%name1% 0

call:check_link %dir1%\%name2% 1

call:check_file %dir2%\%name1% 1

call:check_link %dir2%\%name2% 0

call:end_test

REM ############################################################## TEST_NO_28_TITLE
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

call:delay_pause

call:remove_file %dir1%\%name1%

call:check_file %dir1%\%name1% 0

call:check_link %dir1%\%name2% 2
call:check_link %dir1%\%name3% 2

call:delay_pause

call:create_file %dir2%\%name1% %name1%

call:check_file %dir1%\%name1% 0

call:check_link %dir1%\%name2% 1
call:check_link %dir1%\%name3% 1

call:check_file %dir2%\%name1% 1

call:check_link %dir2%\%name2% 0
call:check_link %dir2%\%name3% 0

call:end_test

REM ############################################################## TEST_NO_29_TITLE
call:start_test "Recover 1 parent from dir1 to dir2 with different name"

set name1=test%label%-file1
set name2=test%label%-file2

set dir1=test%label%-dir1
set dir2=test%label%-dir2

call:create_dir %dir1%
call:create_dir %dir2%

call:create_file %dir1%\%name1% %name1%

call:check_file %dir1%\%name1% 1

call:delay_pause

call:remove_file %dir1%\%name1%

call:check_file %dir1%\%name1% 0

call:delay_pause

call:create_file %dir2%\%name2% %name1%

call:check_file %dir1%\%name1% 0
call:check_file %dir1%\%name2% 0
call:check_file %dir2%\%name1% 0
call:check_file %dir2%\%name2% 1

call:end_test

REM ############################################################## TEST_NO_30_TITLE
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

call:delay_pause

call:remove_file %dir1%\%name1%

call:check_file %dir1%\%name1% 0
call:check_link %dir1%\%name3% 2

call:delay_pause

call:create_file %dir2%\%name2% %name1%

call:check_file %dir1%\%name1% 0
call:check_file %dir1%\%name2% 0

call:check_link %dir1%\%name3% 1

call:check_file %dir2%\%name1% 0
call:check_file %dir2%\%name2% 1

call:check_link %dir2%\%name3% 0

call:end_test

REM ############################################################## TEST_NO_31_TITLE
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

call:delay_pause

call:remove_file %dir1%\%name1%

call:check_file %dir1%\%name1% 0
call:check_link %dir1%\%name3% 2
call:check_link %dir1%\%name4% 2

call:delay_pause

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

REM ############################################################## TEST_NO_32_TITLE
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

call:delay_pause 10

call:remove_file %name1%

call:delay_pause 10

call:check_file %name1% 0

call:check_link %name2% 2
call:check_link %name3% 2
call:check_link %name4% 2

call:delay_pause 10

call:create_file %name2% %name5%

call:delay_pause 10

call:check_file %name2% 1

call:check_link %name2% 0
call:check_link %name3% 2
call:check_link %name4% 2

call:end_test

REM ############################################################## TEST_NO_33_TITLE
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

call:delay_pause

call:remove_dir %dir1%

call:delay_pause

call:check_file %name1% 1

call:check_link %dir1%\%name2% 0
call:check_link %dir1%\%name3% 0
call:check_link %dir1%\%name4% 0

call:end_test

REM ############################################################## TEST_NO_34_TITLE
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

call:delay_pause

call:remove_file %name1%

call:remove_dir %dir1%

call:delay_pause

call:check_file %name1% 0

call:check_link %dir1%\%name2% 2
call:check_link %dir1%\%name3% 2
call:check_link %dir1%\%name4% 2

call:delay_pause

call:create_file %name1% %name1%

call:check_file %name1% 1

call:check_link %dir1%\%name2% 1
call:check_link %dir1%\%name3% 1
call:check_link %dir1%\%name4% 1

call:end_test

REM ############################################################## TEST_NO_35_TITLE
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

call:delay_pause 20

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

call:check_link %name21% 1
call:check_file %dir2%\%name22% 1
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

REM ############################################################## TEST_NO_36_TITLE
call:start_test "Delete second child file"

set name1=test%label%-file1
set name2=test%label%-file2
set name3=test%label%-file3
set name4=test%label%-file4

call:create_file %name1% %name1%
call:create_file %name2% %name1%
call:create_file %name3% %name1%
call:create_file %name4% %name1%

call:delay_pause

call:remove_file %name2%

call:check_file %name1% 1

call:check_link %name2% 0
call:check_link %name3% 1
call:check_link %name4% 1

call:end_test

REM ############################################################## TEST_NO_37_TITLE
call:start_test "Replace parent file with a different SHA"

set name1=test%label%-file1
set name2=test%label%-file2
set name3=test%label%-file3
set name4=test%label%-file4
set name5=test%label%-file5

call:create_file %name1% %name1%
call:create_file %name2% %name1%
call:create_file %name3% %name1%
call:create_file %name4% %name1%

call:delay_pause

call:create_file %name1% %name5%

call:check_file %name1% 1

call:check_link %name2% 1
call:check_link %name3% 1
call:check_link %name4% 1

call:end_test

REM ############################################################## TEST_NO_38_TITLE
call:start_test "Replace child file with a diferent SHA"

set name1=test%label%-file1
set name2=test%label%-file2
set name3=test%label%-file3
set name4=test%label%-file4
set name5=test%label%-file5

call:create_file %name1% %name1%
call:create_file %name2% %name1%
call:create_file %name3% %name1%
call:create_file %name4% %name1%

call:delay_pause

call:create_file %name2% %name5%

call:check_file %name1% 1
call:check_file %name2% 1

call:check_link %name3% 1
call:check_link %name4% 1

call:end_test

REM ############################################################## TEST_NO_39_TITLE
call:start_test "Replace second child file with same SHA"

set name1=test%label%-file1
set name2=test%label%-file2
set name3=test%label%-file3
set name4=test%label%-file4
set name5=test%label%-file5

call:create_file %name1% %name1%
call:create_file %name2% %name1%
call:create_file %name3% %name1%
call:create_file %name4% %name1%

call:delay_pause

call:create_file %name2% %name1%

call:check_file %name1% 1

call:check_link %name2% 1
call:check_link %name3% 1
call:check_link %name4% 1

call:end_test

REM ############################################################## TEST_NO_40_TITLE
call:start_test "Delete parent. Create second child file. Delete second child file. Create parent."

set name1=test%label%-file1
set name2=test%label%-file2
set name3=test%label%-file3
set name4=test%label%-file4

call:create_file %name1% %name1%
call:create_file %name2% %name1%
call:create_file %name3% %name1%
call:create_file %name4% %name1%

call:delay_pause

call:remove_file %name1%

call:delay_pause 5

call:create_file %name2% %name1%

call:delay_pause 5

call:check_file %name1% 0
call:check_file %name2% 1 REM - CHECK 1) LINK AND FILE URI AT THE SAME TIME 2) REMOVE LINK AND FILE URI FROM TABLE

call:check_link %name3% 1
call:check_link %name4% 1

call:delay_pause

call:remove_file %name2%

call:delay_pause 10

call:check_file %name1% 0

call:check_link %name2% 0
call:check_link %name3% 2
call:check_link %name4% 2

call:delay_pause 5

call:create_file %name1% %name1%

call:delay_pause 5

call:check_file %name1% 1

call:check_link %name2% 0
call:check_link %name3% 1
call:check_link %name4% 1

call:end_test

REM ############################################################## TEST_NO_41_TITLE
call:start_test "Delete parent. Replace second file with different SHA. Delete second file. Create parent."

set name1=test%label%-file1
set name2=test%label%-file2
set name3=test%label%-file3
set name4=test%label%-file4
set name5=test%label%-file5

call:create_file %name1% %name1%
call:create_file %name2% %name1%
call:create_file %name3% %name1%
call:create_file %name4% %name1%

call:delay_pause

call:remove_file %name1%

call:delay_pause 10

call:create_file %name2% %name5%

call:delay_pause 10

call:check_file %name1% 0
call:check_file %name2% 1

call:check_link %name3% 2
call:check_link %name4% 2

call:delay_pause

call:remove_file %name2%

call:delay_pause 10

call:check_file %name1% 0

call:check_link %name2% 0
call:check_link %name3% 2
call:check_link %name4% 2

call:delay_pause 5

call:create_file %name1% %name1%

call:delay_pause 5

call:check_file %name1% 1

call:check_link %name2% 0
call:check_link %name3% 1
call:check_link %name4% 1

call:end_test

REM ############################################################## TEST_NO_42_TITLE
call:start_test "Delete parent. Replace second file with different SHA. Create second child. Delete second file. Create parent."

set name1=test%label%-file1
set name2=test%label%-file2
set name3=test%label%-file3
set name4=test%label%-file4
set name5=test%label%-file5
set name6=test%label%-file6
set name7=test%label%-file7

call:create_file %name1% %name1%
call:create_file %name2% %name1%
call:create_file %name3% %name1%
call:create_file %name4% %name1%

call:delay_pause

call:remove_file %name1%

call:delay_pause 10

call:create_file %name2% %name5%
call:create_file %name6% %name5%
call:create_file %name7% %name5%

call:delay_pause 10

call:check_file %name1% 0
call:check_file %name2% 1

call:check_link %name3% 2
call:check_link %name4% 2

call:check_link %name6% 1
call:check_link %name7% 1

call:delay_pause

call:remove_file %name2%

call:delay_pause 10

call:check_file %name1% 0

call:check_link %name2% 0
call:check_link %name3% 2
call:check_link %name4% 2

call:check_link %name6% 2
call:check_link %name7% 2

call:delay_pause 5

call:create_file %name1% %name1%

call:delay_pause 5

call:check_file %name1% 1

call:check_link %name2% 0
call:check_link %name3% 1
call:check_link %name4% 1

call:check_link %name6% 2
call:check_link %name7% 2

call:end_test

REM ##############################################################
GOTO EOF

REM ##############################################################
:start_test
set /A testnro=testnro+1
%Windir%\System32\WindowsPowerShell\v1.0\Powershell.exe write-host -foregroundcolor White -backgroundcolor Blue ===========================================
call:to_upper "%~1" upperword
call:leading %testnro%
echo TEST #%label%/%last_test%) %upperword%
%Windir%\System32\WindowsPowerShell\v1.0\Powershell.exe write-host -foregroundcolor White -backgroundcolor Blue ===========================================
EXIT /B 0

REM ##############################################################
:create_file
set filename1=%~1
set fcontent1=%~2

fsutil reparsepoint query "!basedir!%filename1%" >nul && (
    echo Replacing [!basedir!%filename1%]
    del "!basedir!%filename1%"
)

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
del !basedir!%filename1%\*
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
if %~2==2 (
    set mark=0
    set marq=1
) else (
    set mark=%~2
    set marq=%~2
)
fsutil reparsepoint query "!basedir!%~1" > "%tmpfile%"
if %errorlevel% == 0 call:assert 1 %mark%
if %errorlevel% == 1 call:assert 0 %mark%
del temp_file.tmp
call:check_soft !basedir!%~1 %marq%
EXIT /B 0

REM ##############################################################
:assert
if %~1==%~2 (
    set /A ptests=ptests+1
    echo -------------------------------------------
        %Windir%\System32\WindowsPowerShell\v1.0\Powershell.exe write-host -foregroundcolor Black -backgroundcolor Green =============== [ PASSED ] ================
        echo -------------------------------------------
) else (
    set /A ftests=ftests+1
        echo -------------------------------------------
        %Windir%\System32\WindowsPowerShell\v1.0\Powershell.exe write-host -foregroundcolor Black -backgroundcolor Red =============== [ FAILED ] ================
    echo -------------------------------------------
)
EXIT /B 0

REM ##############################################################
:compare_file
echo !basedir!%~1 == !basedir!%~2
REM fc /b !basedir!%~1 !basedir!%~2 > nul 2>&1
type !basedir!%~1 > temp0.tmp
type !basedir!%~2 > temp1.tmp
fc /b temp0.tmp temp1.tmp > nul 2>&1
if %errorlevel% == 0 call:assert 1 %~3
if %errorlevel% == 1 call:assert 0 %~3
del temp0.tmp
del temp1.tmp
EXIT /B 0

REM ##############################################################
:end_test
echo ===========================================
echo TEST #%label%/%last_test%) DONE
echo ===========================================
call:delay_pause
EXIT /B 0

REM ##############################################################
:wait_time
if %delaytm% gtr 0 (
    timeout %delaycount% > NUL
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
findstr /C:%txt% %file_table% >nul 2>&1
if %errorlevel% equ 0 call:assert 1 %~2
if %errorlevel% equ 1 call:assert 0 %~2
EXIT /B 0

REM ##############################################################
:check_soft
set txt=%~1
echo _
echo SOFT FILE %txt%
findstr /C:%txt% %file_links% >nul 2>&1
if %errorlevel% equ 0 call:assert 1 %~2
if %errorlevel% equ 1 call:assert 0 %~2
EXIT /B 0

REM ##############################################################
:delay_pause
if "%1"=="" (
    set /A delaycount=2
) else (
    set /A delaytm=1
    set /A delaycount=%~1
    echo ___________________________________
    echo Waiting for %1 seconds...
    echo -----------------------------------
)
call:wait_time
set /A delaycount=1
EXIT /B 0

REM ##############################################################
:read_ini_value
set inifile=%~1
set section=%~2
set key=%~3
for /F "delims== tokens=2* usebackq" %%i in (`type %inifile% ^| find "main_path ="`) do (set basedir=%%i)
for /f "tokens=* delims= " %%a in ("%basedir%") do set "basedir=%%a"
set "basedir=!basedir:/=\!"
EXIT /B 0

REM ##############################################################
:EOF
set /A ttests=%ptests%+%ftests%
echo -------------------------------------------
%Windir%\System32\WindowsPowerShell\v1.0\Powershell.exe write-host -foregroundcolor White -backgroundcolor Blue ______________[TEST RESULTS]_______________
%Windir%\System32\WindowsPowerShell\v1.0\Powershell.exe write-host -foregroundcolor Black -backgroundcolor Green =============== [ PASSED ] ================
for /L %%i in (1, 1, %ptests%) do (
     set "formattedValue=000000%%i"
     set apass=!formattedValue:~-3!
)
echo TOTAL PASSED TESTS: [%apass%/%ttests%]
%Windir%\System32\WindowsPowerShell\v1.0\Powershell.exe write-host -foregroundcolor Black -backgroundcolor Red =============== [ FAILED ] ================
for /L %%i in (1, 1, %ftests%) do (
     set "formattedValue=000000%%i"
     set afail=!formattedValue:~-3!
)
echo TOTAL FAILED TESTS: [%afail%/%ttests%]
%Windir%\System32\WindowsPowerShell\v1.0\Powershell.exe write-host -foregroundcolor White -backgroundcolor Blue ______________[TEST RESULTS]_______________
echo -------------------------------------------
