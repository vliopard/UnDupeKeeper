#!/bin/bash

timeout=1


basedir="/home/vliopard/UnDupeDir/"
file_links=link_table.txt
file_table=file_table.txt
delaycount=2
delaytm=1
testnro=0
last_test="035"
label=""

call_start_test()
{
    let testnro=testnro+1
    echo -e \\"e[1;37;44m===========================================\\e[0m"
    upperword="$1"
    label=$(printf "%03d" ${testnro})
    echo TEST \#${label}/${last_test}\) ${upperword^^}
    echo -e \\"e[1;37;44m===========================================\\e[0m"
}

call_create_file()
{
    filename1=$1
    fcontent1=$2
    echo ${fcontent1} > ${basedir}${filename1}
    read -t ${timeout} -p "${basedir}${filename1}"
    echo  
}

call_remove_file()
{
    filename1=$1
    rm ${basedir}${filename1}
    read -t ${timeout} -p "${basedir}${filename1}*"
    echo  
}

call_move_file()
{
    filename1=$1
    filename2=$2    
    echo ${basedir}${filename2}
    mv ${basedir}${filename1} ${basedir}${filename2}
    read -t ${timeout} -p "${basedir}${filename2}"
    echo  
}

call_create_dir()
{
    filename1=$1
    echo /${basedir}${filename1}/
    mkdir ${basedir}${filename1}
    read -t ${timeout} -p "${basedir}${filename1}"
    echo  
}

call_check_file()
{
    filename1=$1
    echo _
    echo REAL FILE ${basedir}${filename1}
    if [ -e ${basedir}${filename1} ]
    then
        call_assert 1 $2
    else
        call_assert 0 $2
    fi
    call_check_hard ${basedir}${filename1} $2
}

call_check_link()
{
    filename1=$1
    echo _
    echo LINK FILE ${basedir}${filename1}
    if [ $2 -eq 2 ]
    then
        mark=0
        marq=1
    else
        mark=$2
        marq=$2
    fi
    if [ -h ${basedir}${filename1} ]
    then
        call_assert 1 ${mark}
    else
        call_assert 0 ${mark}
    fi
    call_check_soft ${basedir}${filename1} ${marq}
}

call_check_hard()
{
    echo _
    echo HARD FILE $1
    if grep -r $1 ${file_table}
    then
        call_assert 1 $2
    else
        call_assert 0 $2
    fi
}

call_check_soft()
{
    echo _
    echo SOFT FILE $1
    if grep -r $1 ${file_links}
    then
        call_assert 1 $2
    else
        call_assert 0 $2
    fi
}

call_remove_dir()
{
    dirname1=$1
    if [ "$(ls -A ${basedir}${dirname1})" ]
    then
        file_list=${basedir}${dirname1}/'*'
        rm ${file_list}
    fi
    rmdir "${basedir}${dirname1}"
    read -t ${timeout} -p "${basedir}${dirname1}*"
    echo  
}

call_assert()
{
    if [ $1 == $2 ]
    then
        echo -------------------------------------------
        echo -e \\"e[0;30;42m=============== [ PASSED ] ================\\e[0m"
        echo -------------------------------------------
    else
        echo -------------------------------------------
        echo -e \\"e[0;30;41m=============== [ FAILED ] ================\\e[0m"
        echo -------------------------------------------
    fi
}

call_compare_file()
{
    #diff --brief $1 $2
    #comp_value=$?
    #if [ $comp_value -eq 0 ]
    echo ${basedir}$1 == ${basedir}$2
    if cmp -s ${basedir}$1 ${basedir}$2
    then
        call_assert 1 $3
    else
        call_assert 0 $3
    fi
}

call_end_test()
{
    echo ===========================================
    echo TEST \#${label}/${last_test}\) DONE
    echo ===========================================
    call_pause
}

call_pause()
{
    # read -p "Press any key to continue..."
    read -t ${delaycount} -p "Next..."
    echo ""
}

# REM ############################################################## TEST_NO_01_TITLE
call_start_test "Add 1 local unique file"

name1=test${label}-file1

call_create_file ${name1} ${name1}

call_check_file ${name1} 1

call_end_test

# REM ############################################################## TEST_NO_02_TITLE
call_start_test "Add 2 local unique files"

name1=test${label}-file1
name2=test${label}-file2

call_create_file ${name1} ${name1}
call_create_file ${name2} ${name2}

call_check_file ${name1} 1
call_check_file ${name2} 1

call_end_test

# REM ############################################################## TEST_NO_03_TITLE
call_start_test "Add 3 local unique files"

name1=test${label}-file1
name2=test${label}-file2
name3=test${label}-file3

call_create_file ${name1} ${name1}
call_create_file ${name2} ${name2}
call_create_file ${name3} ${name3}

call_check_file ${name1} 1
call_check_file ${name2} 1
call_check_file ${name3} 1

call_end_test

# REM ############################################################## TEST_NO_04_TITLE
call_start_test "Delete 1 local file"

name1=test${label}-file1

call_create_file ${name1} ${name1}

call_check_file ${name1} 1

call_pause

call_remove_file ${name1}

call_check_file ${name1} 0

call_end_test

# REM ############################################################## TEST_NO_05_TITLE
call_start_test "Move 1 local file to other name"

name1=test${label}-source1
name2=test${label}-target1

call_create_file ${name1} ${name1}

call_check_file ${name1} 1
call_check_file ${name2} 0

call_pause

call_move_file ${name1} ${name2}

call_check_file ${name1} 0
call_check_file ${name2} 1

call_end_test

# REM ############################################################## TEST_NO_06_TITLE
call_start_test "Move 1 local file to other directory same file name"

name1=test${label}-file1
dir1=test${label}-dir1

call_create_file ${name1} ${name1}

call_create_dir ${dir1}

call_check_file ${name1} 1
call_check_file ${dir1}/${name1} 0

call_pause

call_move_file ${name1} ${dir1}/${name1}

call_check_file ${name1} 0
call_check_file ${dir1}/${name1} 1

call_end_test

# REM ############################################################## TEST_NO_07_TITLE
call_start_test "Move 1 local file to other directory other file name"

name1=test${label}-source1
name2=test${label}-target1

dir1=test${label}-dir1

call_create_file ${name1} ${name1}

call_create_dir ${dir1}

call_check_file ${name1} 1
call_check_file ${dir1}/${name2} 0

call_pause

call_move_file ${name1} ${dir1}/${name2}

call_check_file ${name1} 0
call_check_file ${dir1}/${name2} 1

call_end_test

# REM ############################################################## TEST_NO_08_TITLE
call_start_test "Add 1 local dupe file"

name1=test${label}-file1
name2=test${label}-file2

call_create_file ${name1} ${name1}
call_create_file ${name2} ${name1}

call_check_file ${name1} 1

call_check_link ${name2} 1

call_compare_file ${name1} ${name2} 1

call_end_test

# REM ############################################################## TEST_NO_09_TITLE
call_start_test "Add 2 local dupe file"

name1=test${label}-file1
name2=test${label}-file2
name3=test${label}-file3

call_create_file ${name1} ${name1}
call_create_file ${name2} ${name1}
call_create_file ${name3} ${name1}

call_check_file ${name1} 1

call_check_link ${name2} 1
call_check_link ${name3} 1

call_compare_file ${name1} ${name2} 1
call_compare_file ${name1} ${name3} 1

call_end_test

# REM ############################################################## TEST_NO_10_TITLE
call_start_test "Add 3 local dupe file"

name1=test${label}-file1
name2=test${label}-file2
name3=test${label}-file3
name4=test${label}-file4

call_create_file ${name1} ${name1}
call_create_file ${name2} ${name1}
call_create_file ${name3} ${name1}
call_create_file ${name4} ${name1}

call_check_file ${name1} 1

call_check_link ${name2} 1
call_check_link ${name3} 1
call_check_link ${name4} 1

call_compare_file ${name1} ${name2} 1
call_compare_file ${name1} ${name3} 1
call_compare_file ${name1} ${name4} 1

call_end_test

# REM ############################################################## TEST_NO_11_TITLE
call_start_test "Delete 1 local link file"

name1=test${label}-file1
name2=test${label}-file2

call_create_file ${name1} ${name1}
call_create_file ${name2} ${name1}

call_pause

call_remove_file ${name2}

call_check_file ${name1} 1

call_check_link ${name2} 0

call_end_test

# REM ############################################################## TEST_NO_12_TITLE
call_start_test "Move 1 local link file to other name"

name1=test${label}-file1
name2=test${label}-source1
name3=test${label}-target1

call_create_file ${name1} ${name1}
call_create_file ${name2} ${name1}

call_pause

call_move_file ${name2} ${name3}

call_check_file ${name1} 1

call_check_link ${name2} 0
call_check_link ${name3} 1

call_end_test

# REM ############################################################## TEST_NO_13_TITLE
call_start_test "Move 1 local link file to other directory same link name"

name1=test${label}-source1
name2=test${label}-target1

dir1=test${label}-dir1

call_create_file ${name1} ${name1}
call_create_file ${name2} ${name1}

call_create_dir ${dir1}

call_pause

call_move_file ${name2} ${dir1}/${name2}

call_check_file ${name1} 1

call_check_link ${name2} 0
call_check_link ${dir1}/${name2} 1

call_end_test

# REM ############################################################## TEST_NO_14_TITLE
call_start_test "Move 1 local link file to other directory other link name"

name1=test${label}-file1
name2=test${label}-source1
name3=test${label}-target1

dir1=test${label}-dir1

call_create_file ${name1} ${name1}
call_create_file ${name2} ${name1}

call_create_dir ${dir1}

call_pause

call_move_file ${name2} ${dir1}/${name3}

call_check_file ${name1} 1

call_check_link ${name2} 0
call_check_link ${dir1}/${name3} 1

call_end_test

# REM ############################################################## TEST_NO_15_TITLE
call_start_test "Delete 1 local parent file"

name1=test${label}-file1
name2=test${label}-file2
name3=test${label}-file3
name4=test${label}-file4

call_create_file ${name1} ${name1}
call_create_file ${name2} ${name1}
call_create_file ${name3} ${name1}
call_create_file ${name4} ${name1}

call_pause

call_remove_file ${name1}

call_check_file ${name1} 0

call_check_link ${name2} 2
call_check_link ${name3} 2
call_check_link ${name4} 2

call_end_test

# REM ############################################################## TEST_NO_16_TITLE
call_start_test "Move 1 local parent file to other name"

name1=test${label}-source1
name2=test${label}-file1
name3=test${label}-target1

call_create_file ${name1} ${name1}
call_create_file ${name2} ${name1}

call_pause

call_move_file ${name1} ${name3}

call_check_file ${name1} 0

call_check_link ${name2} 1

call_check_file ${name3} 1

call_end_test

# REM ############################################################## TEST_NO_17_TITLE
call_start_test "Move 1 local parent file to other directory same file name"

name1=test${label}-file1
name2=test${label}-file2

dir1=test${label}-dir1

call_create_file ${name1} ${name1}
call_create_file ${name2} ${name1}

call_create_dir ${dir1}

call_pause

call_move_file ${name1} ${dir1}/${name1}

call_check_file ${name1} 0

call_check_link ${name2} 1

call_check_file ${dir1}/${name1} 1

call_end_test

# REM ############################################################## TEST_NO_18_TITLE
call_start_test "Move 1 local parent file to other directory other file name"

name1=test${label}-source1
name2=test${label}-file1
name3=test${label}-target2

dir1=test${label}-dir1

call_create_file ${name1} ${name1}
call_create_file ${name2} ${name1}

call_create_dir ${dir1}

call_pause

call_move_file ${name1} ${dir1}/${name3}

call_check_file ${name1} 0

call_check_link ${name2} 1

call_check_file ${dir1}/${name3} 1

call_end_test

# REM ############################################################## TEST_NO_19_TITLE
call_start_test "Recover 1 local file with no links"

name1=test${label}-file1

call_create_file ${name1} ${name1}

call_check_file ${name1} 1

call_pause

call_remove_file ${name1}

call_check_file ${name1} 0

call_pause

call_create_file ${name1} ${name1}

call_check_file ${name1} 1

call_end_test

# REM ############################################################## TEST_NO_20_TITLE
call_start_test "Recover 1 local parent file with 1 link"

name1=test${label}-file1
name2=test${label}-file2

call_create_file ${name1} ${name1}
call_create_file ${name2} ${name1}

call_check_file ${name1} 1

call_check_link ${name2} 1

call_pause

call_remove_file ${name1}

call_check_file ${name1} 0

call_check_link ${name2} 2

call_pause

call_create_file ${name1} ${name1}

call_check_file ${name1} 1

call_check_link ${name2} 1

call_end_test

# REM ############################################################## TEST_NO_21_TITLE
call_start_test "Recover 1 local parent file with 2 links"

name1=test${label}-file1
name2=test${label}-file2
name3=test${label}-file3

call_create_file ${name1} ${name1}
call_create_file ${name2} ${name1}
call_create_file ${name3} ${name1}

call_check_file ${name1} 1

call_check_link ${name2} 1
call_check_link ${name3} 1

call_pause

call_remove_file ${name1}

call_check_file ${name1} 0

call_check_link ${name2} 2
call_check_link ${name3} 2

call_pause

call_create_file ${name1} ${name1}

call_check_file ${name1} 1

call_check_link ${name2} 1
call_check_link ${name3} 1

call_end_test

# REM ############################################################## TEST_NO_22_TITLE
call_start_test "Recover 1 local parent file with 3 links"

name1=test${label}-file1
name2=test${label}-file2
name3=test${label}-file3
name4=test${label}-file4

call_create_file ${name1} ${name1}
call_create_file ${name2} ${name1}
call_create_file ${name3} ${name1}
call_create_file ${name4} ${name1}

call_check_file ${name1} 1

call_check_link ${name2} 1
call_check_link ${name3} 1
call_check_link ${name4} 1

call_pause

call_remove_file ${name1}

call_check_file ${name1} 0

call_check_link ${name2} 2
call_check_link ${name3} 2
call_check_link ${name4} 2

call_pause

call_create_file ${name1} ${name1}

call_check_file ${name1} 1

call_check_link ${name2} 1
call_check_link ${name3} 1
call_check_link ${name4} 1

call_end_test

# REM ############################################################## TEST_NO_23_TITLE
call_start_test "Recover 1 local parent file with 1 link in other directory"

name1=test${label}-file1
name2=test${label}-file2
name3=test${label}-file3

dir1=test${label}-dir1

call_create_file ${name1} ${name1}

call_create_dir ${dir1}

call_create_file ${dir1}/${name2} ${name1}

call_check_file ${name1} 1

call_check_link ${dir1}/${name2} 1

call_pause

call_remove_file ${name1}

call_check_file ${name1} 0

call_check_link ${dir1}/${name2} 2

call_pause

call_create_file ${name1} ${name1}

call_check_file ${name1} 1

call_check_link ${dir1}/${name2} 1

call_end_test

# REM ############################################################## TEST_NO_24_TITLE
call_start_test "Recover 1 local parent file with 2 links in different directories"

name1=test${label}-file1
name2=test${label}-file2
name3=test${label}-file3

dir1=test${label}-dir1

call_create_file ${name1} ${name1}

call_create_dir ${dir1}

call_create_file ${dir1}/${name2} ${name1}
call_create_file ${dir1}/${name3} ${name1}

call_check_file ${name1} 1

call_check_link ${dir1}/${name2} 1
call_check_link ${dir1}/${name3} 1

call_pause

call_remove_file ${name1}

call_check_file ${name1} 0

call_check_link ${dir1}/${name2} 2
call_check_link ${dir1}/${name3} 2

call_pause

call_create_file ${name1} ${name1}

call_check_file ${name1} 1

call_check_link ${dir1}/${name2} 1
call_check_link ${dir1}/${name3} 1

call_end_test

# REM ############################################################## TEST_NO_25_TITLE
call_start_test "Recover 1 local parent file with 3 links in different directories"

name1=test${label}-file1
name2=test${label}-file2
name3=test${label}-file3
name4=test${label}-file4

dir1=test${label}-dir1

call_create_file ${name1} ${name1}

call_create_dir ${dir1}

call_create_file ${dir1}/${name2} ${name1}
call_create_file ${dir1}/${name3} ${name1}
call_create_file ${dir1}/${name4} ${name1}

call_check_file ${name1} 1

call_check_link ${dir1}/${name2} 1
call_check_link ${dir1}/${name3} 1
call_check_link ${dir1}/${name4} 1

call_pause

call_remove_file ${name1}

call_check_file ${name1} 0

call_check_link ${dir1}/${name2} 2
call_check_link ${dir1}/${name3} 2
call_check_link ${dir1}/${name4} 2

call_pause

call_create_file ${name1} ${name1}

call_check_file ${name1} 1

call_check_link ${dir1}/${name2} 1
call_check_link ${dir1}/${name3} 1
call_check_link ${dir1}/${name4} 1

call_end_test

# REM ############################################################## TEST_NO_26_TITLE
call_start_test "Recover 1 parent from dir1 to dir2 with same name"

name1=test${label}-file1

dir1=test${label}-dir1
dir2=test${label}-dir2

call_create_dir ${dir1}
call_create_dir ${dir2}

call_create_file ${dir1}/${name1} ${name1}

call_check_file ${dir1}/${name1} 1
call_check_file ${dir2}/${name1} 0

call_pause

call_remove_file ${dir1}/${name1}

call_check_file ${dir1}/${name1} 0
call_check_file ${dir2}/${name1} 0

call_pause

call_create_file ${dir2}/${name1} ${name1}

call_check_file ${dir1}/${name1} 0
call_check_file ${dir2}/${name1} 1

call_end_test

# REM ############################################################## TEST_NO_27_TITLE
call_start_test "Recover 1 parent from dir1 to dir2 with same name and 1 child link"

name1=test${label}-file1
name2=test${label}-file2

dir1=test${label}-dir1
dir2=test${label}-dir2

call_create_dir ${dir1}
call_create_dir ${dir2}

call_create_file ${dir1}/${name1} ${name1}
call_create_file ${dir1}/${name2} ${name1}

call_check_file ${dir1}/${name1} 1

call_check_link ${dir1}/${name2} 1

call_pause

call_remove_file ${dir1}/${name1}

call_check_file ${dir1}/${name1} 0

call_check_link ${dir1}/${name2} 2

call_pause

call_create_file ${dir2}/${name1} ${name1}

call_check_file ${dir1}/${name1} 0

call_check_link ${dir1}/${name2} 1

call_check_file ${dir2}/${name1} 1

call_check_link ${dir2}/${name2} 0

call_end_test

# REM ############################################################## TEST_NO_28_TITLE
call_start_test "Recover 1 parent from dir1 to dir2 with same name and 2 child link"

name1=test${label}-file1
name2=test${label}-file2
name3=test${label}-file3

dir1=test${label}-dir1
dir2=test${label}-dir2

call_create_dir ${dir1}
call_create_dir ${dir2}

call_create_file ${dir1}/${name1} ${name1}
call_create_file ${dir1}/${name2} ${name1}
call_create_file ${dir1}/${name3} ${name1}

call_check_file ${dir1}/${name1} 1

call_check_link ${dir1}/${name2} 1
call_check_link ${dir1}/${name3} 1

call_pause

call_remove_file ${dir1}/${name1}

call_check_file ${dir1}/${name1} 0

call_check_link ${dir1}/${name2} 2
call_check_link ${dir1}/${name3} 2

call_pause

call_create_file ${dir2}/${name1} ${name1}

call_check_file ${dir1}/${name1} 0

call_check_link ${dir1}/${name2} 1
call_check_link ${dir1}/${name3} 1

call_check_file ${dir2}/${name1} 1

call_check_link ${dir2}/${name2} 0
call_check_link ${dir2}/${name3} 0

call_end_test

# REM ############################################################## TEST_NO_29_TITLE
call_start_test "Recover 1 parent from dir1 to dir2 with different name"

name1=test${label}-file1
name2=test${label}-file2

dir1=test${label}-dir1
dir2=test${label}-dir2

call_create_dir ${dir1}
call_create_dir ${dir2}

call_create_file ${dir1}/${name1} ${name1}

call_check_file ${dir1}/${name1} 1

call_pause

call_remove_file ${dir1}/${name1}

call_check_file ${dir1}/${name1} 0

call_pause

call_create_file ${dir2}/${name2} ${name1}

call_check_file ${dir1}/${name1} 0
call_check_file ${dir1}/${name2} 0
call_check_file ${dir2}/${name1} 0
call_check_file ${dir2}/${name2} 1

call_end_test

# REM ############################################################## TEST_NO_30_TITLE
call_start_test "Recover 1 parent from dir1 to dir2 with different name and 1 child link"

name1=test${label}-file1
name2=test${label}-file2
name3=test${label}-file3

dir1=test${label}-dir1
dir2=test${label}-dir2

call_create_dir ${dir1}
call_create_dir ${dir2}

call_create_file ${dir1}/${name1} ${name1}
call_create_file ${dir1}/${name3} ${name1}

call_check_file ${dir1}/${name1} 1
call_check_link ${dir1}/${name3} 1

call_pause

call_remove_file ${dir1}/${name1}

call_check_file ${dir1}/${name1} 0
call_check_link ${dir1}/${name3} 2

call_pause

call_create_file ${dir2}/${name2} ${name1}

call_check_file ${dir1}/${name1} 0
call_check_file ${dir1}/${name2} 0

call_check_link ${dir1}/${name3} 1

call_check_file ${dir2}/${name1} 0
call_check_file ${dir2}/${name2} 1

call_check_link ${dir2}/${name3} 0

call_end_test

# REM ############################################################## TEST_NO_31_TITLE
call_start_test "Recover 1 parent from dir1 to dir2 with different name and 2 child link"

name1=test${label}-file1
name2=test${label}-file2
name3=test${label}-file3
name4=test${label}-file4

dir1=test${label}-dir1
dir2=test${label}-dir2

call_create_dir ${dir1}
call_create_dir ${dir2}

call_create_file ${dir1}/${name1} ${name1}
call_create_file ${dir1}/${name3} ${name1}
call_create_file ${dir1}/${name4} ${name1}

call_check_file ${dir1}/${name1} 1
call_check_link ${dir1}/${name3} 1
call_check_link ${dir1}/${name4} 1

call_pause

call_remove_file ${dir1}/${name1}

call_check_file ${dir1}/${name1} 0
call_check_link ${dir1}/${name3} 2
call_check_link ${dir1}/${name4} 2

call_pause

call_create_file ${dir2}/${name2} ${name1}

call_check_file ${dir1}/${name1} 0
call_check_file ${dir1}/${name2} 0

call_check_link ${dir1}/${name3} 1
call_check_link ${dir1}/${name4} 1

call_check_file ${dir2}/${name1} 0
call_check_file ${dir2}/${name2} 1

call_check_link ${dir2}/${name3} 0
call_check_link ${dir2}/${name4} 0

call_end_test

# REM ############################################################## TEST_NO_32_TITLE
call_start_test "Create new unique file which its path is the same of a removed link from a removed parent"

name1=test${label}-file1
name2=test${label}-file2
name3=test${label}-file3
name4=test${label}-file4
name5=test${label}-file5

call_create_file ${name1} ${name1}
call_create_file ${name2} ${name1}
call_create_file ${name3} ${name1}
call_create_file ${name4} ${name1}

call_check_file ${name1} 1

call_check_link ${name2} 1
call_check_link ${name3} 1
call_check_link ${name4} 1

call_pause

call_remove_file ${name1}

call_check_file ${name1} 0

call_check_link ${name2} 2
call_check_link ${name3} 2
call_check_link ${name4} 2

call_pause

call_create_file ${name2} ${name5}

call_check_file ${name2} 1

call_check_link ${name2} 2
call_check_link ${name3} 2
call_check_link ${name4} 2

call_end_test

# REM ############################################################## TEST_NO_33_TITLE
call_start_test "Remove a directory with links"
name1=test${label}-file1
name2=test${label}-file2
name3=test${label}-file3
name4=test${label}-file4

dir1=test${label}-dir1

call_create_dir ${dir1}

call_create_file ${name1} ${name1}
call_create_file ${dir1}/${name2} ${name1}
call_create_file ${dir1}/${name3} ${name1}
call_create_file ${dir1}/${name4} ${name1}

call_check_file ${name1} 1

call_check_link ${dir1}/${name2} 1
call_check_link ${dir1}/${name3} 1
call_check_link ${dir1}/${name4} 1

call_pause

call_remove_dir ${dir1}

call_pause

call_check_file ${name1} 1

call_check_link ${dir1}/${name2} 0
call_check_link ${dir1}/${name3} 0
call_check_link ${dir1}/${name4} 0

call_end_test

# REM ############################################################## TEST_NO_34_TITLE
call_start_test "Recover a removed directory with links"

name1=test${label}-file1
name2=test${label}-file2
name3=test${label}-file3
name4=test${label}-file4

dir1=test${label}-dir1

call_create_dir ${dir1}

call_create_file ${name1} ${name1}
call_create_file ${dir1}/${name2} ${name1}
call_create_file ${dir1}/${name3} ${name1}
call_create_file ${dir1}/${name4} ${name1}

call_check_file ${name1} 1

call_check_link ${dir1}/${name2} 1
call_check_link ${dir1}/${name3} 1
call_check_link ${dir1}/${name4} 1

call_pause

call_remove_file ${name1}

call_remove_dir ${dir1}

call_pause

call_check_file ${name1} 0

call_check_link ${dir1}/${name2} 2
call_check_link ${dir1}/${name3} 2
call_check_link ${dir1}/${name4} 2

call_pause

call_create_file ${name1} ${name1}

call_check_file ${name1} 1

call_check_link ${dir1}/${name2} 1
call_check_link ${dir1}/${name3} 1
call_check_link ${dir1}/${name4} 1

call_end_test

# REM ############################################################## TEST_NO_35_TITLE
call_start_test "Bulk creation of massive files"

delaytm=0

name01=test${label}-file01
name02=test${label}-file02
name03=test${label}-file03
name04=test${label}-file04
name05=test${label}-file05
name06=test${label}-file06
name07=test${label}-file07
name08=test${label}-file08
name09=test${label}-file09
name10=test${label}-file10
name11=test${label}-file11
name12=test${label}-file12
name13=test${label}-file13
name14=test${label}-file14
name15=test${label}-file15
name16=test${label}-file16
name17=test${label}-file17
name18=test${label}-file18
name19=test${label}-file19
name20=test${label}-file20
name21=test${label}-file21
name22=test${label}-file22
name23=test${label}-file23
name24=test${label}-file24
name25=test${label}-file25
name26=test${label}-file26
name27=test${label}-file27
name28=test${label}-file28
name29=test${label}-file29
name30=test${label}-file30
name31=test${label}-file31
name32=test${label}-file32
name33=test${label}-file33
name34=test${label}-file34
name35=test${label}-file35
name36=test${label}-file36
name37=test${label}-file37
name38=test${label}-file38
name39=test${label}-file39
name40=test${label}-file40

dir1=test${label}-dir1
dir2=test${label}-dir2
dir3=test${label}-dir3
dir4=test${label}-dir4

call_create_dir ${dir1}
call_create_dir ${dir2}
call_create_dir ${dir3}
call_create_dir ${dir4}

call_create_file ${name01} ${name01}
call_create_file ${name02} ${name01}
call_create_file ${name03} ${name01}
call_create_file ${name04} ${name01}
call_create_file ${name05} ${name01}
call_create_file ${name06} ${name01}
call_create_file ${name07} ${name01}
call_create_file ${name08} ${name01}
call_create_file ${name09} ${name01}
call_create_file ${name10} ${name01}

call_create_file ${dir1}/${name11} ${name02}
call_create_file ${dir1}/${name12} ${name02}
call_create_file ${dir1}/${name13} ${name02}
call_create_file ${dir1}/${name14} ${name02}
call_create_file ${dir1}/${name15} ${name02}
call_create_file ${dir1}/${name16} ${name02}
call_create_file ${dir1}/${name17} ${name02}
call_create_file ${dir1}/${name18} ${name02}
call_create_file ${dir1}/${name19} ${name02}
call_create_file ${dir1}/${name20} ${name02}

call_create_file ${name21} ${name03}
call_create_file ${dir2}/${name22} ${name03}
call_create_file ${dir2}/${name23} ${name03}
call_create_file ${dir2}/${name24} ${name03}
call_create_file ${dir2}/${name25} ${name03}
call_create_file ${dir2}/${name26} ${name03}
call_create_file ${dir2}/${name27} ${name03}
call_create_file ${dir2}/${name28} ${name03}
call_create_file ${dir2}/${name29} ${name03}
call_create_file ${dir2}/${name30} ${name03}

call_create_file ${dir3}/${name31} ${name04}
call_create_file ${name32} ${name04}
call_create_file ${dir3}/${name33} ${name04}
call_create_file ${name34} ${name04}
call_create_file ${dir3}/${name35} ${name04}
call_create_file ${name36} ${name04}
call_create_file ${dir3}/${name37} ${name04}
call_create_file ${name38} ${name04}
call_create_file ${dir3}/${name39} ${name04}
call_create_file ${name40} ${name04}

call_pause

call_check_file ${name01} 1
call_check_link ${name02} 1
call_check_link ${name03} 1
call_check_link ${name04} 1
call_check_link ${name05} 1
call_check_link ${name06} 1
call_check_link ${name07} 1
call_check_link ${name08} 1
call_check_link ${name09} 1
call_check_link ${name10} 1

call_check_file ${dir1}/${name11} 1
call_check_link ${dir1}/${name12} 1
call_check_link ${dir1}/${name13} 1
call_check_link ${dir1}/${name14} 1
call_check_link ${dir1}/${name15} 1
call_check_link ${dir1}/${name16} 1
call_check_link ${dir1}/${name17} 1
call_check_link ${dir1}/${name18} 1
call_check_link ${dir1}/${name19} 1
call_check_link ${dir1}/${name20} 1

call_check_file ${name21} 1
call_check_link ${dir2}/${name22} 1
call_check_link ${dir2}/${name23} 1
call_check_link ${dir2}/${name24} 1
call_check_link ${dir2}/${name25} 1
call_check_link ${dir2}/${name26} 1
call_check_link ${dir2}/${name27} 1
call_check_link ${dir2}/${name28} 1
call_check_link ${dir2}/${name29} 1
call_check_link ${dir2}/${name30} 1

call_check_file ${dir3}/${name31} 1
call_check_link ${name32} 1
call_check_link ${dir3}/${name33} 1
call_check_link ${name34} 1
call_check_link ${dir3}/${name35} 1
call_check_link ${name36} 1
call_check_link ${dir3}/${name37} 1
call_check_link ${name38} 1
call_check_link ${dir3}/${name39} 1
call_check_link ${name40} 1

call_end_test
