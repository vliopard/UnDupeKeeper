#!/bin/bash

timeout=1


basedir="/home/vliopard/tests/"
testnro=0
label=""

call_start_test()
{
    let testnro=testnro+1
    echo ===========================================
    upperword="$1"
    label=$(printf "%03d" ${testnro})
    echo TEST #${label}) ${upperword^^}
    echo ===========================================
}

call_create_file()
{
    filename1=$1
    fcontent1=$2
    echo ${fcontent1} > ${basedir}${filename1}
    read -t ${timeout} -p "${basedir}${filename1}"
    echo -
}

call_remove_file()
{
    filename1=$1
    rm ${basedir}${filename1}
    read -t ${timeout} -p "${basedir}${filename1}*"
    echo -
}

call_move_file()
{
    filename1=$1
    filename2=$2    
    mv ${basedir}${filename1} ${basedir}${filename2}
    read -t ${timeout} -p "${basedir}${filename2}"
    echo -
}

call_create_dir()
{
    filename1=$1
    mkdir ${basedir}${filename1}
    read -t ${timeout} -p "${basedir}${filename1}"
    echo -
}

call_check_file()
{
    filename1=$1
    if [ -e ${basedir}${filename1} ]
    then
        call_assert 1 $2
    else
        call_assert 0 $2
    fi
}

call_check_link()
{
    filename1=$1
    if [ -h ${basedir}${filename1} ]
    then
        call_assert 1 $2
    else
        call_assert 0 $2
    fi
}

call_assert()
{
    if [ $1 == $2 ]
    then
        echo ==========================
        echo ======= [ PASSED ] =======
        echo ==========================
    else
        echo ==========================
        echo ======= [ FAILED ] =======
        echo ==========================
    fi
}

call_compare_file()
{
    #diff --brief $1 $2
    #comp_value=$?
    #if [ $comp_value -eq 0 ]
    if [ cmp -s $1 $2 ]
    then
        call_assert 1 $3
    else
        call_assert 0 $3
    fi
}

call_end_test()
{
    echo ===========================================
    echo TEST #${label}) DONE
    echo ===========================================
    read -p "Press any key to continue..."
}


# REM ##############################################################
call_start_test "Add 1 local unique file"

name1=file1-test${label}

call_create_file ${name1} ${name1}

call_check_file ${name1} 1

call_end_test

# REM ##############################################################
call_start_test "Add 2 local unique files"

name1=file1-test${label}
name2=file2-test${label}

call_create_file ${name1} ${name1}
call_create_file ${name2} ${name2}

call_check_file ${name1} 1
call_check_file ${name2} 1

call_end_test

# REM ##############################################################
call_start_test "Add 3 local unique files"

name1=file1-test${label}
name2=file2-test${label}
name3=file3-test${label}

call_create_file ${name1} ${name1}
call_create_file ${name2} ${name2}
call_create_file ${name3} ${name3}

call_check_file ${name1} 1
call_check_file ${name2} 1
call_check_file ${name3} 1

call_end_test

# REM ##############################################################
call_start_test "Delete 1 local file"

name1=file1-test${label}

call_create_file ${name1} ${name1}

call_remove_file ${name1}

call_check_file ${name1} 0

call_end_test

# REM ##############################################################
call_start_test "Move 1 local file to other name"

name1=fileOri1-test${label}
name2=fileRen1-test${label}

call_create_file ${name1} ${name1}

call_move_file ${name1} ${name2}

call_check_file ${name1} 0
call_check_file ${name2} 1

call_end_test

# REM ##############################################################
call_start_test "Move 1 local file to other directory same file name"

name1=file1-test${label}
dir1=dir1-test${label}

call_create_file ${name1} ${name1}

call_create_dir ${dir1}

call_move_file ${name1} ${dir1}/${name1}

call_check_file ${name1} 0
call_check_file ${dir1}/${name1} 1

call_end_test

# REM ##############################################################
call_start_test "Move 1 local file to other directory other file name"

name1=fileOri1-test${label}
name2=fileRen1-test${label}

dir1=dir1-test${label}

call_create_file ${name1} ${name1}

call_create_dir ${dir1}

call_move_file ${name1} ${dir1}/${name2}

call_check_file ${name1} 0
call_check_file ${dir1}/${name2} 1

call_end_test

# REM ##############################################################
call_start_test "Add 1 local dupe file"

name1=file1-test${label}
name2=file2-test${label}

call_create_file ${name1} ${name1}
call_create_file ${name2} ${name1}

call_check_file ${name1} 1

call_check_link ${name2} 1

call_compare_file ${name1} ${name2} 1

call_end_test

# REM ##############################################################
call_start_test "Add 2 local dupe file"

name1=file1-test${label}
name2=file2-test${label}
name3=file3-test${label}

call_create_file ${name1} ${name1}
call_create_file ${name2} ${name1}
call_create_file ${name3} ${name1}

call_check_file ${name1} 1

call_check_link ${name2} 1
call_check_link ${name3} 1

call_compare_file ${name1} ${name2} 1
call_compare_file ${name1} ${name3} 1

call_end_test

# REM ##############################################################
call_start_test "Add 3 local dupe file"

name1=file1-test${label}
name2=file2-test${label}
name3=file3-test${label}
name4=file4-test${label}

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

# REM ##############################################################
call_start_test "Delete 1 local link file"

name1=file1-test${label}
name2=file2-test${label}

call_create_file ${name1} ${name1}
call_create_file ${name2} ${name1}

call_remove_file ${name2}

call_check_file ${name1} 1

call_check_link ${name2} 0

call_end_test

# REM ##############################################################
call_start_test "Move 1 local link file to other name"

name1=file1-test${label}
name2=file2-test${label}
name3=file3-test${label}

call_create_file ${name1} ${name1}
call_create_file ${name2} ${name1}

call_move_file ${name2} ${name3}

call_check_file ${name1} 1

call_check_link ${name2} 0
call_check_link ${name3} 1

call_end_test

# REM ##############################################################
call_start_test "Move 1 local link file to other directory same link name"

name1=fileOri1-test${label}
name2=fileRen1-test${label}

dir1=dir1-test${label}

call_create_file ${name1} ${name1}
call_create_file ${name2} ${name1}

call_create_dir ${dir1}

call_move_file ${name2} ${dir1}/${name2}

call_check_file ${name1} 1

call_check_link ${name2} 0
call_check_link ${dir1}/${name2} 1

call_end_test

# REM ##############################################################
call_start_test "Move 1 local link file to other directory other link name"

name1=fileOri1-test${label}
name2=fileOri2-test${label}
name3=fileRen2-test${label}

dir1=dir1-test${label}

call_create_file ${name1} ${name1}
call_create_file ${name2} ${name1}

call_create_dir ${dir1}

call_move_file ${name2} ${dir1}/${name3}

call_check_file ${name1} 1

call_check_link ${name2} 0
call_check_link ${dir1}/${name3} 1

call_end_test

# REM ##############################################################
call_start_test "Delete 1 local parent file"

name1=file1-test${label}
name2=file2-test${label}
name3=file3-test${label}
name4=file4-test${label}

call_create_file ${name1} ${name1}
call_create_file ${name2} ${name1}
call_create_file ${name3} ${name1}
call_create_file ${name4} ${name1}

call_remove_file ${name1}

call_check_file ${name1} 0

call_check_link ${name2} 0
call_check_link ${name3} 0
call_check_link ${name4} 0

call_end_test

# REM ##############################################################
call_start_test "Move 1 local parent file to other name"

name1=fileOri1-test${label}
name2=file2-test${label}
name3=fileRen1-test${label}

call_create_file ${name1} ${name1}
call_create_file ${name2} ${name1}

call_move_file ${name1} ${name3}

call_check_file ${name1} 0

call_check_link ${name2} 1

call_check_file ${name3} 1

call_end_test

# REM ##############################################################
call_start_test "Move 1 local parent file to other directory same file name"

name1=file1-test${label}
name2=file2-test${label}

dir1=dir1-test${label}

call_create_file ${name1} ${name1}
call_create_file ${name2} ${name1}

call_create_dir ${dir1}

call_move_file ${name1} ${dir1}/${name1}

call_check_file ${name1} 0

call_check_link ${name2} 1

call_check_file ${dir1}/${name1} 1

call_end_test

# REM ##############################################################
call_start_test "Move 1 local parent file to other directory other file name"

name1=file1-test${label}
name2=file2-test${label}
name3=file2-test${label}

dir1=dir1-test${label}

call_create_file ${name1} ${name1}
call_create_file ${name2} ${name1}

call_create_dir ${dir1}

call_move_file ${name1} ${dir1}/${name3}

call_check_file ${name1} 0

call_check_link ${name2} 1

call_check_file ${dir1}/${name3} 1

call_end_test

# REM ##############################################################
call_start_test "Recover 1 local file with no links"

name1=file1-test${label}

call_create_file ${name1} ${name1}

call_check_file ${name1} 1

call_remove_file ${name1}

call_check_file ${name1} 0

call_create_file ${name1} ${name1}

call_check_file ${name1} 1

call_end_test

# REM ##############################################################
call_start_test "Recover 1 local parent file with 1 link"

name1=file1-test${label}
name2=file2-test${label}

call_create_file ${name1} ${name1}
call_create_file ${name2} ${name1}

call_check_file ${name1} 1

call_check_link ${name2} 1

call_remove_file ${name1}

call_check_file ${name1} 0

call_check_link ${name2} 0

call_create_file ${name1} ${name1}

call_check_file ${name1} 1

call_check_link ${name2} 1

call_end_test

# REM ##############################################################
call_start_test "Recover 1 local parent file with 2 links"

name1=file1-test${label}
name2=file2-test${label}
name3=file3-test${label}

call_create_file ${name1} ${name1}
call_create_file ${name2} ${name1}
call_create_file ${name3} ${name1}

call_check_file ${name1} 1

call_check_link ${name2} 1
call_check_link ${name3} 1

call_remove_file ${name1}

call_check_file ${name1} 0

call_check_link ${name2} 0
call_check_link ${name3} 0

call_create_file ${name1} ${name1}

call_check_file ${name1} 1

call_check_link ${name2} 1
call_check_link ${name3} 1

call_end_test

# REM ##############################################################
call_start_test "Recover 1 local parent file with 3 links"

name1=file1-test${label}
name2=file2-test${label}
name3=file3-test${label}
name4=file4-test${label}

call_create_file ${name1} ${name1}
call_create_file ${name2} ${name1}
call_create_file ${name3} ${name1}
call_create_file ${name4} ${name1}

call_check_file ${name1} 1

call_check_link ${name2} 1
call_check_link ${name3} 1
call_check_link ${name4} 1

call_remove_file ${name1}

call_check_file ${name1} 0

call_check_link ${name2} 0
call_check_link ${name3} 0
call_check_link ${name4} 0

call_create_file ${name1} ${name1}

call_check_file ${name1} 1

call_check_link ${name2} 1
call_check_link ${name3} 1
call_check_link ${name4} 1

call_end_test

# REM ##############################################################
call_start_test "Recover 1 local parent file with 1 link in other directory"

name1=file1-test${label}
name2=file2-test${label}
name3=file3-test${label}

dir1=dir1-test${label}

call_create_file ${name1} ${name1}

call_create_dir ${dir1}

call_create_file ${dir1}/${name2} ${name1}

call_check_file ${name1} 1

call_check_link ${dir1}/${name2} 1

call_remove_file ${name1}

call_check_file ${name1} 0

call_check_link ${dir1}/${name2} 0

call_create_file ${name1} ${name1}

call_check_file ${name1} 1

call_check_link ${dir1}/${name2} 1

call_end_test

# REM ##############################################################
call_start_test "Recover 1 local parent file with 2 links in different directories"

name1=file1-test${label}
name2=file2-test${label}
name3=file3-test${label}

dir1=dir1-test${label}

call_create_file ${name1} ${name1}

call_create_dir ${dir1}

call_create_file ${dir1}/${name2} ${name1}
call_create_file ${dir1}/${name3} ${name1}

call_check_file ${name1} 1

call_check_link ${dir1}/${name2} 1
call_check_link ${dir1}/${name3} 1

call_remove_file ${name1}

call_check_file ${name1} 0

call_check_link ${dir1}/${name2} 0
call_check_link ${dir1}/${name3} 0

call_create_file ${name1} ${name1}

call_check_file ${name1} 1

call_check_link ${dir1}/${name2} 1
call_check_link ${dir1}/${name3} 1

call_end_test

# REM ##############################################################
call_start_test "Recover 1 local parent file with 3 links in different directories"

name1=file1-test${label}
name2=file2-test${label}
name3=file3-test${label}
name4=file4-test${label}

dir1=dir1-test${label}

call_create_file ${name1} ${name1}

call_create_dir ${dir1}

call_create_file ${dir1}/${name2} ${name1}
call_create_file ${dir1}/${name3} ${name1}
call_create_file ${dir1}/${name4} ${name1}

call_check_file ${name1} 1

call_check_link ${dir1}/${name2} 1
call_check_link ${dir1}/${name3} 1
call_check_link ${dir1}/${name4} 1

call_remove_file ${name1}

call_check_file ${name1} 0

call_check_link ${dir1}/${name2} 0
call_check_link ${dir1}/${name3} 0
call_check_link ${dir1}/${name4} 0

call_create_file ${name1} ${name1}

call_check_file ${name1} 1

call_check_link ${dir1}/${name2} 1
call_check_link ${dir1}/${name3} 1
call_check_link ${dir1}/${name4} 1

call_end_test

REM ##############################################################
call_start_test "Recover 1 parent from dir1 to dir2 with same name"

name1=file1-test${label}

dir1=dir1-test${label}
dir2=dir2-test${label}

call_create_dir ${dir1}
call_create_dir ${dir2}

call_create_file ${dir1}/${name1} ${name1}

call_check_file ${dir1}/${name1} 1
call_check_file ${dir2}/${name1} 0

call_remove_file ${dir1}/${name1}

call_create_file ${dir2}/${name1} ${name1}

call_check_file ${dir1}/${name1} 0
call_check_file ${dir2}/${name1} 1

call_end_test

REM ##############################################################
call_start_test "Recover 1 parent from dir1 to dir2 with same name and 1 child link"

name1=file1-test${label}
name2=file2-test${label}

dir1=dir1-test${label}
dir2=dir2-test${label}

call_create_dir ${dir1}
call_create_dir ${dir2}

call_create_file ${dir1}/${name1} ${name1}
call_create_file ${dir1}/${name2} ${name1}

call_check_file ${dir1}/${name1} 1

call_check_link ${dir1}/${name2} 1

call_remove_file ${dir1}/${name1}

call_check_file ${dir1}/${name1} 0

call_check_link ${dir1}/${name2} 0

call_create_file ${dir2}/${name1} ${name1}

call_check_file ${dir1}/${name1} 0

call_check_link ${dir1}/${name2} 1

call_check_file ${dir2}/${name1} 1

call_check_link ${dir2}/${name2} 0

call_end_test

REM ##############################################################
call_start_test "Recover 1 parent from dir1 to dir2 with same name and 2 child link"

name1=file1-test${label}
name2=file2-test${label}
name3=file3-test${label}

dir1=dir1-test${label}
dir2=dir2-test${label}

call_create_dir ${dir1}
call_create_dir ${dir2}

call_create_file ${dir1}/${name1} ${name1}
call_create_file ${dir1}/${name2} ${name1}
call_create_file ${dir1}/${name3} ${name1}

call_check_file ${dir1}/${name1} 1

call_check_link ${dir1}/${name2} 1
call_check_link ${dir1}/${name3} 1

call_remove_file ${dir1}/${name1}

call_check_file ${dir1}/${name1} 0

call_check_link ${dir1}/${name2} 0
call_check_link ${dir1}/${name3} 0

call_create_file ${dir2}/${name1} ${name1}

call_check_file ${dir1}/${name1} 0

call_check_link ${dir1}/${name2} 1
call_check_link ${dir1}/${name3} 1

call_check_file ${dir2}/${name1} 1

call_check_link ${dir2}/${name2} 0
call_check_link ${dir2}/${name3} 0

call_end_test

REM ##############################################################
call_start_test "Recover 1 parent from dir1 to dir2 with different name"

name1=file1-test${label}
name2=file2-test${label}

dir1=dir1-test${label}
dir2=dir2-test${label}

call_create_dir ${dir1}
call_create_dir ${dir2}

call_create_file ${dir1}/${name1} ${name1}

call_check_file ${dir1}/${name1} 1

call_remove_file ${name1}

call_check_file ${dir1}/${name1} 0

call_create_file ${dir2}/${name2} ${name1}

call_check_file ${dir1}/${name1} 0
call_check_file ${dir1}/${name2} 0
call_check_file ${dir2}/${name1} 0
call_check_file ${dir2}/${name2} 1

call_end_test

REM ##############################################################
call_start_test "Recover 1 parent from dir1 to dir2 with different name and 1 child link"

name1=file1-test${label}
name2=file2-test${label}
name3=file3-test${label}

dir1=dir1-test${label}
dir2=dir2-test${label}

call_create_dir ${dir1}
call_create_dir ${dir2}

call_create_file ${dir1}/${name1} ${name1}
call_create_file ${dir1}/${name3} ${name1}

call_check_file ${dir1}/${name1} 1
call_check_link ${dir1}/${name3} 1

call_remove_file ${name1}

call_check_file ${dir1}/${name1} 0
call_check_link ${dir1}/${name3} 0

call_create_file ${dir2}/${name2} ${name1}

call_check_file ${dir1}/${name1} 0
call_check_file ${dir1}/${name2} 0

call_check_link ${dir1}/${name3} 1

call_check_file ${dir2}/${name1} 0
call_check_file ${dir2}/${name2} 1

call_check_link ${dir2}/${name3} 0

call_end_test

REM ##############################################################
call_start_test "Recover 1 parent from dir1 to dir2 with different name and 2 child link"

name1=file1-test${label}
name2=file2-test${label}
name3=file3-test${label}
name4=file4-test${label}

dir1=dir1-test${label}
dir2=dir2-test${label}

call_create_dir ${dir1}
call_create_dir ${dir2}

call_create_file ${dir1}/${name1} ${name1}
call_create_file ${dir1}/${name3} ${name1}
call_create_file ${dir1}/${name4} ${name1}

call_check_file ${dir1}/${name1} 1
call_check_link ${dir1}/${name3} 1
call_check_link ${dir1}/${name4} 1

call_remove_file ${name1}

call_check_file ${dir1}/${name1} 0
call_check_link ${dir1}/${name3} 0
call_check_link ${dir1}/${name4} 0

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

REM ##############################################################
call_start_test "Create new unique file which its path is the same of a removed link from a removed parent"

name1=file1-test${label}
name2=file2-test${label}
name3=file3-test${label}
name4=file4-test${label}
name5=file5-test${label}

call_create_file ${name1} ${name1}
call_create_file ${name2} ${name1}
call_create_file ${name3} ${name1}
call_create_file ${name4} ${name1}

call_check_file ${name1} 1

call_check_link ${name2} 1
call_check_link ${name3} 1
call_check_link ${name4} 1

call_remove_file ${name1}

call_check_file ${name1} 0

call_check_link ${name2} 0
call_check_link ${name3} 0
call_check_link ${name4} 0

call_create_file ${name1} ${name5}

call_check_file ${name1} 0

call_check_link ${name2} 0
call_check_link ${name3} 0
call_check_link ${name4} 0

call_end_test