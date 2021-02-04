#!/bin/bash
timeout=1
basedir="/home/vliopard/temp/"
set testnro=0
set label=""

# REM ##############################################################
call_start_test()
{
	let testnro=testnro+1
	echo ===========================================
	call_to_upper "$1" upperword
	call_leading ${testnro}
	echo TEST #${label}) ${upperword}
	echo ===========================================
}

# REM ##############################################################
call_create_file()
{
    filename1=$1
    fcontent1=$2
    echo ${fcontent1} > ${basedir}${filename1}
    read -t ${timeout} -p "${basedir}${filename1}"
    echo -
}

# REM ##############################################################
call_remove_file()
{
    filename1=$1
    rm ${basedir}${filename1}
    read -t ${timeout} -p "${basedir}${filename1}*"
    echo -
}

# REM ##############################################################
call_move_file()
{
    filename1=$1
    filename2=$2    
    mv ${basedir}${filename1} ${basedir}${filename2}
    read -t ${timeout} -p "${basedir}${filename2}"
    echo -
}

# REM ##############################################################
call_create_dir()
{
    filename1=$1
    mkdir ${basedir}${filename1}
    read -t ${timeout} -p "${basedir}${filename1}"
    echo -
}

# REM ##############################################################
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

# REM ##############################################################
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

# REM ##############################################################
call_assert()
{
    if [ $1 == $2 ]
    then
        echo =
        echo ======= [ PASSED ] =======
        echo =
    else
        echo =
        echo ======= [ FAILED ] =======
        echo =
    fi
}

# REM ##############################################################
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

# REM ##############################################################
call_end_test()
{
	echo ===========================================
	echo TEST #${label}) DONE
	echo ===========================================
    read -p "Press any key to continue..."
}

# REM ##############################################################
call_to_upper()
{
	set upper=
	set "str=%~1"
	for /f "skip=2 delims=" %%I in ('tree "\%str%"') do if not defined upper set "upper=%%~I"
	set "upper=%upper:~3%"
	set %~2=%upper%
}

# REM ##############################################################
call_leading()
{
	set count=%~1
	for /L %%i in (1, 1, %count%) do (
		 set "formattedValue=000000%%i"
		 set retval=!formattedValue:~-3!
	)
	set label=%retval%
}

# REM ##############################################################
call_start_test "Add 1 local unique file"

call_create_file aaa aaa

call_check_file aaa 1

call_end_test

# REM ##############################################################
call_start_test "Add 2 local unique files"

call_create_file bbb bbb

call_check_file bbb 1

call_end_test

# REM ##############################################################
call_start_test "Add 3 local unique files"

call_create_file ccc ccc

call_check_file ccc 1

call_end_test

# REM ##############################################################
call_start_test "Delete 1 local file"

call_create_file ddd ddd

call_remove_file ddd

call_check_file ddd 0

call_end_test

# REM ##############################################################
call_start_test "Move 1 local file to other name"

call_create_file eee eee

call_move_file eee fff

call_check_file eee 0

call_check_file fff 1

call_end_test

# REM ##############################################################
call_start_test "Move 1 local file to other directory same file name"

call_create_file ggg ggg

call_create_dir mydir

call_move_file ggg mydir/ggg

call_check_file ggg 0

call_check_file mydir/ggg 1

call_end_test

# REM ##############################################################
call_start_test "Move 1 local file to other directory other file name"

call_create_file hhh hhh

call_create_dir mydir1

call_move_file hhh mydir1/iii

call_check_file hhh 0

call_check_file mydir1/iii 1

call_end_test

# REM ##############################################################
call_start_test "Add 1 local dupe file"

call_create_file jjj jjj
call_create_file kkk jjj

call_check_file jjj 1

call_check_link kkk 1

call_compare_file jjj kkk 1

call_end_test

# REM ##############################################################
call_start_test "Add 2 local dupe file"

call_create_file lll lll
call_create_file mmm lll
call_create_file nnn lll

call_check_file lll 1

call_check_link mmm 1

call_check_link nnn 1

call_compare_file lll mmm 1

call_compare_file lll nnn 1

call_end_test

# REM ##############################################################
call_start_test "Add 3 local dupe file"

call_create_file ooo ooo
call_create_file ppp ooo
call_create_file qqq ooo
call_create_file rrr ooo

call_check_file ooo 1

call_check_link ppp 1
call_check_link qqq 1
call_check_link rrr 1

call_compare_file ooo ppp 1
call_compare_file ooo qqq 1
call_compare_file ooo rrr 1

call_end_test

# REM ##############################################################
call_start_test "Delete 1 local link file"

call_create_file sss sss
call_create_file ttt sss

call_remove_file ttt

call_check_file sss 1

call_check_link ttt 0

call_end_test

# REM ##############################################################
call_start_test "Move 1 local link file to other name"

call_create_file uuu uuu
call_create_file vvv uuu
call_move_file vvv xxx

call_check_file uuu 1

call_check_link vvv 0
call_check_link xxx 1

call_end_test

# REM ##############################################################
call_start_test "Move 1 local link file to other directory same link name"

call_create_file yyy yyy
call_create_file zzz yyy

call_create_dir mydir2

call_move_file zzz mydir2/zzz

call_check_file yyy 1

call_check_link zzz 0

call_check_link mydir2/zzz 1

call_end_test

# REM ##############################################################
call_start_test "Move 1 local link file to other directory other link name"

call_create_file aaaa aaaa
call_create_file bbbb aaaa

call_create_dir mydir3

call_move_file bbbb mydir3/cccc

call_check_file aaaa 1

call_check_link bbbb 0

call_check_link mydir3/cccc 1

call_end_test

# REM ##############################################################
call_start_test "Delete 1 local parent file"

call_create_file dddd dddd
call_create_file eeee dddd
call_create_file ffff dddd
call_create_file gggg dddd

call_remove_file dddd

call_check_file dddd 0

call_check_link eeee 0
call_check_link ffff 0
call_check_link gggg 0

call_end_test

# REM ##############################################################
call_start_test "Move 1 local parent file to other name"

call_create_file eeee eeee
call_create_file ffff eeee

call_move_file eeee gggg

call_check_file eeee 0

call_check_link ffff 1

call_check_file gggg 1

call_end_test

# REM ##############################################################
call_start_test "Move 1 local parent file to other directory same file name"

call_create_file hhhh hhhh
call_create_file iiii hhhh

call_create_dir mydir4

call_move_file hhhh mydir4/hhhh

call_check_file hhhh 0

call_check_link iiii 1

call_check_file mydir4/hhhh 1

call_end_test

# REM ##############################################################
call_start_test "Move 1 local parent file to other directory other file name"

call_create_file jjjj jjjj
call_create_file kkkk jjjj

call_create_dir mydir5

call_move_file jjjj mydir5/llll

call_check_file jjjj 0

call_check_link kkkk 1

call_check_file mydir5/llll 1

call_end_test

# REM ##############################################################
call_start_test "Recover 1 local file with no links"
call_create_file mmmm mmmm

call_check_file mmmm 1

call_remove_file mmmm

call_check_file mmmm 0

call_create_file mmmm mmmm

call_check_file mmmm 1

call_end_test

# REM ##############################################################
call_start_test "Recover 1 local parent file with 1 link"

call_create_file nnnn nnnn
call_create_file oooo nnnn

call_check_file nnnn 1

call_check_link oooo 1

call_remove_file nnnn

call_check_file nnnn 0

call_check_link oooo 0

call_create_file nnnn nnnn

call_check_file nnnn 1

call_check_link oooo 1

call_end_test

# REM ##############################################################
call_start_test "Recover 1 local parent file with 2 links"

call_create_file pppp pppp
call_create_file qqqq pppp
call_create_file rrrr pppp

call_check_file pppp 1

call_check_link qqqq 1
call_check_link rrrr 1

call_remove_file pppp

call_check_file pppp 0

call_check_link qqqq 0

call_check_link rrrr 0

call_create_file pppp pppp

call_check_file pppp 1

call_check_link qqqq 1

call_check_link rrrr 1

call_end_test

# REM ##############################################################
call_start_test "Recover 1 local parent file with 3 links"

call_create_file aaaaa aaaaa
call_create_file bbbbb aaaaa
call_create_file ccccc aaaaa
call_create_file ddddd aaaaa

call_check_file aaaaa 1

call_check_link bbbbb 1
call_check_link ccccc 1
call_check_link ddddd 1

call_remove_file aaaaa

call_check_file aaaaa 0

call_check_link bbbbb 0
call_check_link ccccc 0
call_check_link ddddd 0

call_create_file aaaaa aaaaa

call_check_file aaaaa 1

call_check_link bbbbb 1
call_check_link ccccc 1
call_check_link ddddd 1

call_end_test

# REM ##############################################################
call_start_test "Recover 1 local parent file with 1 link in other directory"

call_create_file aaaaaa aaaaaa

call_create_dir mydir6

call_create_file mydir6/bbbbbb aaaaaa

call_check_file aaaaaa 1

call_check_link mydir6/bbbbbb 1

call_remove_file aaaaaa

call_check_file aaaaaa 0

call_check_link mydir6/bbbbbb 0

call_create_file aaaaaa aaaaaa

call_check_file aaaaaa 1

call_check_link mydir6/bbbbbb 1

call_end_test

# REM ##############################################################
call_start_test "Recover 1 local parent file with 2 links in different directories"

call_create_file aaaaaaa aaaaaaa

call_create_dir mydir7

call_create_file mydir7/bbbbbbb aaaaaaa
call_create_file mydir7/ccccccc aaaaaaa

call_check_file aaaaaaa 1

call_check_link mydir7/bbbbbbb 1
call_check_link mydir7/ccccccc 1

call_remove_file aaaaaaa

call_check_file aaaaaaa 0

call_check_link mydir7/bbbbbbb 0
call_check_link mydir7/ccccccc 0

call_create_file aaaaaaa aaaaaaa

call_check_file aaaaaaa 1

call_check_link mydir7/bbbbbbb 1
call_check_link mydir7/ccccccc 1

call_end_test

# REM ##############################################################
call_start_test "Recover 1 local parent file with 3 links in different directories"

call_create_file aaaaaaaa aaaaaaaa

call_create_dir mydir8

call_create_file mydir8/bbbbbbbb aaaaaaaa
call_create_file mydir8/cccccccc aaaaaaaa
call_create_file mydir8/dddddddd aaaaaaaa

call_check_file aaaaaaaa 1

call_check_link mydir8/bbbbbbbb 1
call_check_link mydir8/cccccccc 1
call_check_link mydir8/dddddddd 1

call_remove_file aaaaaaaa

call_check_file aaaaaaaa 0

call_check_link mydir8/bbbbbbbb 0

call_check_link mydir8/cccccccc 0
call_check_link mydir8/dddddddd 0

call_create_file aaaaaaaa aaaaaaaa

call_check_file aaaaaaaa 1

call_check_link mydir8/bbbbbbbb 1
call_check_link mydir8/cccccccc 1
call_check_link mydir8/dddddddd 1

call_end_test
