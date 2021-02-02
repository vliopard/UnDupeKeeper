#!/bin/bash
timeout=1
basedir="/home/vliopard/temp/"

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
        file=1
    else
        file=0
    fi
}

call_check_link()
{
    filename1=$1
    if [ -h ${basedir}${filename1} ]
    then
        link=1
    else
        link=0
    fi
}

call_compare_file()
{
    #diff --brief $1 $2
    #comp_value=$?
    #if [ $comp_value -eq 0 ]
    if [ cmp -s $1 $2 ]
    then
        file=1
    else
        file=0
    fi
}

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

call_press_key()
{
    testcase=$1
    read -p "Test ${testcase}) DONE"
}

echo -
echo ____________________________
echo Test 01) Add 1 local unique file
call_create_file aaa aaa

call_check_file aaa
call_assert file 1
call_press_key 01

echo -
echo ____________________________
echo Test 02) Add 2 local unique files
call_create_file bbb bbb

call_check_file bbb
call_assert file 1
call_press_key 02

echo -
echo ____________________________
echo Test 03) Add 3 local unique files
call_create_file ccc ccc

call_check_file ccc
call_assert file 1
call_press_key 03

echo -
echo ____________________________
echo Test 04) Delete 1 local file
call_create_file ddd ddd
call_remove_file ddd

call_check_file ddd
call_assert file 0
call_press_key 04

echo -
echo ____________________________
echo Test 05) Move 1 local file to other name
call_create_file eee eee
call_move_file eee fff

call_check_file eee
call_assert file 0

call_check_file fff
call_assert file 1
call_press_key 05

echo -
echo ____________________________
echo Test 06) Move 1 local file to other directory same file name
call_create_file ggg ggg
call_create_dir mydir
call_move_file ggg mydir/ggg

call_check_file ggg
call_assert file 0

call_check_file mydir/ggg
call_assert file 1
call_press_key 06

echo -
echo ____________________________
echo Test 07) Move 1 local file to other directory other file name
call_create_file hhh hhh
call_create_dir mydir1
call_move_file hhh mydir1/iii

call_check_file hhh
call_assert file 0

call_check_file mydir1/iii
call_assert file 1
call_press_key 07

echo -
echo ____________________________
echo Test 08) Add 1 local dupe file
call_create_file jjj jjj
call_create_file kkk jjj

call_check_file jjj
call_assert file 1

call_check_link kkk
call_assert link 1

call_compare_file jjj kkk
call_assert file 1
call_press_key 08

echo -
echo ____________________________
echo Test 09) Add 2 local dupe file
call_create_file lll lll
call_create_file mmm lll
call_create_file nnn lll

call_check_file lll
call_assert file 1

call_check_link mmm
call_assert link 1

call_check_link nnn
call_assert link 1

call_compare_file lll mmm
call_assert file 1

call_compare_file lll nnn
call_assert file 1
call_press_key 09

echo -
echo ____________________________
echo Test 10) Add 3 local dupe file
call_create_file ooo ooo
call_create_file ppp ooo
call_create_file qqq ooo
call_create_file rrr ooo

call_check_file ooo
call_assert file 1

call_check_link ppp
call_assert link 1

call_check_link qqq
call_assert link 1

call_check_link rrr
call_assert link 1

call_compare_file ooo ppp
call_assert file 1

call_compare_file ooo qqq
call_assert file 1

call_compare_file ooo rrr
call_assert file 1
call_press_key 10

echo -
echo ____________________________
echo Test 11) Delete 1 local link file
call_create_file sss sss
call_create_file ttt sss
call_remove_file ttt

call_check_file sss
call_assert file 1

call_check_link ttt
call_assert link 0
call_press_key 11

echo -
echo ____________________________
echo Test 12) Move 1 local link file to other name
call_create_file uuu uuu
call_create_file vvv uuu
call_move_file vvv xxx

call_check_file uuu
call_assert file 1

call_check_link vvv
call_assert link 0

call_check_link xxx
call_assert link 1
call_press_key 12

echo -
echo ____________________________
echo Test 13) Move 1 local link file to other directory same link name
call_create_file yyy yyy
call_create_file zzz yyy
call_create_dir mydir2
call_move_file zzz mydir2/zzz

call_check_file yyy
call_assert file 1

call_check_link zzz
call_assert link 0

call_check_link mydir2/zzz
call_assert link 1
call_press_key 13

echo -
echo ____________________________
echo Test 14) Move 1 local link file to other directory other link name
call_create_file aaaa aaaa
call_create_file bbbb aaaa
call_create_dir mydir3
call_move_file bbbb mydir3/cccc

call_check_file aaaa
call_assert file 1

call_check_link bbbb
call_assert link 0

call_check_link mydir3/cccc
call_assert link 1
call_press_key 14

echo -
echo ____________________________
echo Test 15) Delete 1 local parent file
call_create_file dddd dddd
call_create_file eeee dddd
call_create_file ffff dddd
call_create_file gggg dddd
call_remove_file dddd

call_check_file dddd
call_assert file 0

call_check_link eeee
call_assert link 0

call_check_link ffff
call_assert link 0

call_check_link gggg
call_assert link 0
call_press_key 15

echo -
echo ____________________________
echo Test 16) Move 1 local parent file to other name
call_create_file eeee eeee
call_create_file ffff eeee
call_move_file eeee gggg

call_check_file eeee
call_assert file 0

call_check_link ffff
call_assert link 1

call_check_file gggg
call_assert file 1
call_press_key 16

echo -
echo ____________________________
echo Test 17) Move 1 local parent file to other directory same file name
call_create_file hhhh hhhh
call_create_file iiii hhhh
call_create_dir mydir4
call_move_file hhhh mydir4/hhhh

call_check_file hhhh
call_assert file 0

call_check_link iiii
call_assert link 1

call_check_file mydir4/hhhh
call_assert file 1
call_press_key 17

echo -
echo ____________________________
echo Test 18) Move 1 local parent file to other directory other file name
call_create_file jjjj jjjj
call_create_file kkkk jjjj
call_create_dir mydir5
call_move_file jjjj mydir5/llll

call_check_file jjjj
call_assert file 0

call_check_link kkkk
call_assert link 1

call_check_file mydir5/llll
call_assert file 1
call_press_key 18

echo -
echo ____________________________
echo Test 19) Recover 1 local file with no links
call_create_file mmmm mmmm
call_remove_file mmmm
call_create_file mmmm mmmm

call_check_file mmmm
call_assert file 1
call_press_key 19

echo -
echo ____________________________
echo Test 20) Recover 1 local parent file with 1 link
call_create_file nnnn nnnn
call_create_file oooo nnnn
call_remove_file nnnn
call_create_file nnnn nnnn

call_check_file nnnn
call_assert file 1

call_check_link oooo
call_assert link 1
call_press_key 20

echo -
echo ____________________________
echo Test 21) Recover 1 local parent file with 2 links
call_create_file pppp pppp
call_create_file qqqq pppp
call_create_file rrrr pppp
call_remove_file pppp
call_create_file pppp pppp

call_check_file pppp
call_assert file 1

call_check_link qqqq
call_assert link 1

call_check_link rrrr
call_assert link 1
call_press_key 21

echo -
echo ____________________________
echo Test 22) Recover 1 local parent file with 3 links
call_create_file aaaaa aaaaa
call_create_file bbbbb aaaaa
call_create_file ccccc aaaaa
call_create_file ddddd aaaaa
call_remove_file aaaaa
call_create_file aaaaa aaaaa

call_check_file aaaaa
call_assert file 1

call_check_link bbbbb
call_assert link 1

call_check_link ccccc
call_assert link 1

call_check_link ddddd
call_assert link 1
call_press_key 22

echo -
echo ____________________________
echo Test 23) Recover 1 local parent file with 1 link in other directory
call_create_file aaaaaa aaaaaa
call_create_dir mydir6
call_create_file mydir6/bbbbbb aaaaaa
call_remove_file aaaaaa
call_create_file aaaaaa aaaaaa

call_check_file aaaaaa
call_assert file 1

call_check_link mydir6/bbbbbb
call_assert link 1
call_press_key 23

echo -
echo ____________________________
echo Test 24) Recover 1 local parent file with 2 links in different directories
call_create_file aaaaaaa aaaaaaa
call_create_dir mydir7
call_create_file mydir7/bbbbbbb aaaaaaa
call_create_file mydir7/ccccccc aaaaaaa
call_remove_file aaaaaaa
call_create_file aaaaaaa aaaaaaa

call_check_file aaaaaaa
call_assert file 1

call_check_link mydir7/bbbbbbb
call_assert link 1

call_check_link mydir7/ccccccc
call_assert link 1
call_press_key 24

echo -
echo ____________________________
echo Test 25) Recover 1 local parent file with 3 links in different directories
call_create_file aaaaaaaa aaaaaaaa
call_create_dir mydir8
call_create_file mydir8/bbbbbbbb aaaaaaaa
call_create_file mydir8/cccccccc aaaaaaaa
call_create_file mydir8/dddddddd aaaaaaaa
call_remove_file aaaaaaaa
call_create_file aaaaaaaa aaaaaaaa

call_check_file aaaaaaaa
call_assert file 1

call_check_link mydir8/bbbbbbbb
call_assert link 1

call_check_link mydir8/cccccccc
call_assert link 1

call_check_link mydir8/dddddddd
call_assert link 1
call_press_key 25
