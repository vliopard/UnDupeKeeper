#!/bin/bash
timeout=1
basedir="/home/vliopard/temp/"

create_file()
{
    filename1=$1
    fcontent1=$2
    echo ${fcontent1} > ${basedir}${filename1}
    read -t ${timeout} -p "${basedir}${filename1}"
    echo ""
}

remove_file()
{
    filename1=$1
    rm ${basedir}${filename1}
    read -t ${timeout} -p "${basedir}${filename1}*"
    echo ""
}

move_file()
{
    filename1=$1
    filename2=$2    
    mv ${basedir}${filename1} ${basedir}${filename2}
    read -t ${timeout} -p "${basedir}${filename2}"
    echo ""
}

create_dir()
{
    filename1=$1
    mkdir ${basedir}${filename1}
    read -t ${timeout} -p "${basedir}${filename1}"
    echo ""
}

echo "=========="
echo Test 01) Add 1 local unique file
create_file aaa aaa
echo Test 01) DONE

echo "=========="
echo Test 02) Add 2 local unique files
create_file bbb bbb
echo Test 02) DONE

echo "=========="
echo Test 03) Add 3 local unique files
create_file ccc ccc
echo Test 03) DONE

echo "=========="
echo Test 04) Delete 1 local file
create_file ddd ddd
remove_file ddd
echo Test 04) DONE

echo "=========="
echo Test 05) Move 1 local file to other name
create_file eee eee
move_file eee fff
echo Test 05) DONE

echo "=========="
echo Test 06) Move 1 local file to other directory same file name
create_file ggg ggg
create_dir mydir
move_file ggg mydir/ggg
echo Test 06) DONE

echo "=========="
echo Test 07) Move 1 local file to other directory other file name
create_file hhh hhh
create_dir mydir1
move_file hhh mydir/iii
echo Test 07) DONE

echo "=========="
echo Test 08) Add 1 local dupe file
create_file jjj jjj
create_file kkk jjj
echo Test 08) DONE

echo "=========="
echo Test 09) Add 2 local dupe file
create_file lll lll
create_file mmm lll
create_file nnn nnn
echo Test 09) DONE

echo "=========="
echo Test 10) Add 3 local dupe file
create_file ooo ooo
create_file ppp ooo
create_file qqq ooo
create_file rrr ooo
echo Test 10) DONE

echo "=========="
echo Test 11) Delete 1 local link file
create_file sss sss
create_file ttt sss
remove_file ttt
echo Test 11) DONE

echo "=========="
echo Test 12) Move 1 local link file to other name
create_file uuu uuu
create_file vvv uuu
move_file vvv xxx
echo Test 12) DONE

echo "=========="
echo Test 13) Move 1 local link file to other directory same link name
create_file yyy yyy
create_file zzz yyy
create_dir mydir2
move_file zzz mydir2/zzz
echo Test 13) DONE

echo "=========="
echo Test 14) Move 1 local link file to other directory other link name
create_file aaaa aaaa
create_file bbbb aaaa
create_dir mydir3
move_file bbbb mydir3/cccc
echo Test 14) DONE

echo "=========="
echo Test 15) Delete 1 local parent file
create_file dddd dddd
create_file eeee dddd
create_file ffff dddd
create_file gggg dddd
remove_file dddd
echo Test 15) DONE

echo "=========="
echo Test 16) Move 1 local parent file to other name
create_file eeee eeee
create_file ffff eeee
move_file eeee gggg
echo Test 16) DONE

echo "=========="
echo Test 17) Move 1 local parent file to other directory same file name
create_file hhhh hhhh
create_file iiii hhhh
create_dir mydir4
move_file hhhh mydir4/hhhh
echo Test 17) DONE

echo "=========="
echo Test 18) Move 1 local parent file to other directory other file name
create_file jjjj jjjj
create_file kkkk jjjj
create_dir mydir5
move_file jjjj mydir5/llll
echo Test 18) DONE

echo "=========="
echo Test 19) Recover 1 local file with no links
create_file mmmm mmmm
remove_file mmmm
create_file mmmm mmmm
echo Test 19) DONE

echo "=========="
echo Test 20) Recover 1 local parent file with 1 link
create_file nnnn nnnn
create_file oooo nnnn
remove_file nnnn
create_file nnnn nnnn
echo Test 20) DONE

echo "=========="
echo Test 21) Recover 1 local parent file with 2 links
create_file pppp pppp
create_file qqqq pppp
create_file rrrr pppp
remove_file pppp
create_file pppp pppp
echo Test 21) DONE

echo "=========="
echo Test 22) Recover 1 local parent file with 3 links
create_file aaaaa aaaaa
create_file bbbbb aaaaa
create_file ccccc aaaaa
create_file ddddd aaaaa
remove_file aaaaa
create_file aaaaa aaaaa
echo Test 22) DONE

echo "=========="
echo Test 23) Recover 1 local parent file with 1 link in other directory
create_file aaaaaa aaaaaa
create_dir mydir5
create_file mydir5/bbbbbb aaaaaa
remove_file aaaaaa
create_file aaaaaa aaaaaa
echo Test 23) DONE

echo "=========="
echo Test 24) Recover 1 local parent file with 2 links in different directories
create_file aaaaaaa aaaaaaa
create_dir mydir6
create_file mydir6/bbbbbbb aaaaaaa
create_file mydir6/ccccccc aaaaaaa
remove_file aaaaaaa
create_file aaaaaaa aaaaaaa
echo Test 24) DONE

echo "=========="
echo Test 25) Recover 1 local parent file with 3 links in different directories
create_file aaaaaaaa aaaaaaaa
create_dir mydir7
create_file mydir7/bbbbbbbb aaaaaaaa
create_file mydir7/cccccccc aaaaaaaa
create_file mydir7/dddddddd aaaaaaaa
remove_file aaaaaaaa
create_file aaaaaaaa aaaaaaaa
echo Test 25) DONE
