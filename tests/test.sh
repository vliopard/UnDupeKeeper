#!/bin/bash
timeout=1
basedir="/home/vliopard/temp/"

call_create_file()
{
    filename1=$1
    fcontent1=$2
    echo ${fcontent1} > ${basedir}${filename1}
    read -t ${timeout} -p "${basedir}${filename1}"
    echo ""
}

call_remove_file()
{
    filename1=$1
    rm ${basedir}${filename1}
    read -t ${timeout} -p "${basedir}${filename1}*"
    echo ""
}

call_move_file()
{
    filename1=$1
    filename2=$2    
    mv ${basedir}${filename1} ${basedir}${filename2}
    read -t ${timeout} -p "${basedir}${filename2}"
    echo ""
}

call_create_dir()
{
    filename1=$1
    mkdir ${basedir}${filename1}
    read -t ${timeout} -p "${basedir}${filename1}"
    echo ""
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

call_assert()
{
    if [ $1 == $2 ]
    then
        echo PASSED
    else
        echo FAILED
    fi
}

echo ""
echo "=========="
echo Test 01) Add 1 local unique file
call_create_file aaa aaa
call_check_file aaa
call_assert file 1
echo Test 01) DONE

echo ""
echo "=========="
echo Test 02) Add 2 local unique files
call_create_file bbb bbb
call_check_file bbb
call_assert file 1
echo Test 02) DONE

echo ""
echo "=========="
echo Test 03) Add 3 local unique files
call_create_file ccc ccc
call_check_file ccc
call_assert file 1
echo Test 03) DONE

echo ""
echo "=========="
echo Test 04) Delete 1 local file
call_create_file ddd ddd
call_remove_file ddd
call_check_file ddd
call_assert file 0
echo Test 04) DONE

echo ""
echo "=========="
echo Test 05) Move 1 local file to other name
call_create_file eee eee
call_move_file eee fff
call_check_file eee
call_assert file 0
call_check_file fff
call_assert file 1
echo Test 05) DONE

echo ""
echo "=========="
echo Test 06) Move 1 local file to other directory same file name
call_create_file ggg ggg
call_create_dir mydir
call_move_file ggg mydir/ggg
call_check_file ggg
call_assert file 0
call_check_file mydir/ggg
call_assert file 1
echo Test 06) DONE

echo ""
echo "=========="
echo Test 07) Move 1 local file to other directory other file name
call_create_file hhh hhh
call_create_dir mydir1
call_move_file hhh mydir1/iii
echo Test 07) DONE

echo ""
echo "=========="
echo Test 08) Add 1 local dupe file
call_create_file jjj jjj
call_create_file kkk jjj
echo Test 08) DONE

echo ""
echo "=========="
echo Test 09) Add 2 local dupe file
call_create_file lll lll
call_create_file mmm lll
call_create_file nnn lll
echo Test 09) DONE

echo ""
echo "=========="
echo Test 10) Add 3 local dupe file
call_create_file ooo ooo
call_create_file ppp ooo
call_create_file qqq ooo
call_create_file rrr ooo
echo Test 10) DONE

echo ""
echo "=========="
echo Test 11) Delete 1 local link file
call_create_file sss sss
call_create_file ttt sss
call_remove_file ttt
echo Test 11) DONE

echo ""
echo "=========="
echo Test 12) Move 1 local link file to other name
call_create_file uuu uuu
call_create_file vvv uuu
call_move_file vvv xxx
echo Test 12) DONE

echo ""
echo "=========="
echo Test 13) Move 1 local link file to other directory same link name
call_create_file yyy yyy
call_create_file zzz yyy
call_create_dir mydir2
call_move_file zzz mydir2/zzz
echo Test 13) DONE

echo ""
echo "=========="
echo Test 14) Move 1 local link file to other directory other link name
call_create_file aaaa aaaa
call_create_file bbbb aaaa
call_create_dir mydir3
call_move_file bbbb mydir3/cccc
echo Test 14) DONE

echo ""
echo "=========="
echo Test 15) Delete 1 local parent file
call_create_file dddd dddd
call_create_file eeee dddd
call_create_file ffff dddd
call_create_file gggg dddd
call_remove_file dddd
echo Test 15) DONE

echo ""
echo "=========="
echo Test 16) Move 1 local parent file to other name
call_create_file eeee eeee
call_create_file ffff eeee
call_move_file eeee gggg
echo Test 16) DONE

echo ""
echo "=========="
echo Test 17) Move 1 local parent file to other directory same file name
call_create_file hhhh hhhh
call_create_file iiii hhhh
call_create_dir mydir4
call_move_file hhhh mydir4/hhhh
echo Test 17) DONE

echo ""
echo "=========="
echo Test 18) Move 1 local parent file to other directory other file name
call_create_file jjjj jjjj
call_create_file kkkk jjjj
call_create_dir mydir5
call_move_file jjjj mydir5/llll
echo Test 18) DONE

echo ""
echo "=========="
echo Test 19) Recover 1 local file with no links
call_create_file mmmm mmmm
call_remove_file mmmm
call_create_file mmmm mmmm
echo Test 19) DONE

echo ""
echo "=========="
echo Test 20) Recover 1 local parent file with 1 link
call_create_file nnnn nnnn
call_create_file oooo nnnn
call_remove_file nnnn
call_create_file nnnn nnnn
echo Test 20) DONE

echo ""
echo "=========="
echo Test 21) Recover 1 local parent file with 2 links
call_create_file pppp pppp
call_create_file qqqq pppp
call_create_file rrrr pppp
call_remove_file pppp
call_create_file pppp pppp
echo Test 21) DONE

echo ""
echo "=========="
echo Test 22) Recover 1 local parent file with 3 links
call_create_file aaaaa aaaaa
call_create_file bbbbb aaaaa
call_create_file ccccc aaaaa
call_create_file ddddd aaaaa
call_remove_file aaaaa
call_create_file aaaaa aaaaa
echo Test 22) DONE

echo ""
echo "=========="
echo Test 23) Recover 1 local parent file with 1 link in other directory
call_create_file aaaaaa aaaaaa
call_create_dir mydir6
call_create_file mydir6/bbbbbb aaaaaa
call_remove_file aaaaaa
call_create_file aaaaaa aaaaaa
echo Test 23) DONE

echo ""
echo "=========="
echo Test 24) Recover 1 local parent file with 2 links in different directories
call_create_file aaaaaaa aaaaaaa
call_create_dir mydir7
call_create_file mydir7/bbbbbbb aaaaaaa
call_create_file mydir7/ccccccc aaaaaaa
call_remove_file aaaaaaa
call_create_file aaaaaaa aaaaaaa
echo Test 24) DONE

echo ""
echo "=========="
echo Test 25) Recover 1 local parent file with 3 links in different directories
call_create_file aaaaaaaa aaaaaaaa
call_create_dir mydir8
call_create_file mydir8/bbbbbbbb aaaaaaaa
call_create_file mydir8/cccccccc aaaaaaaa
call_create_file mydir8/dddddddd aaaaaaaa
call_remove_file aaaaaaaa
call_create_file aaaaaaaa aaaaaaaa
echo Test 25) DONE
