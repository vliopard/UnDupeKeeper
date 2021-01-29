@ echo off
cls

setlocal enabledelayedexpansion

set basedir=c:\vliopard\tests\

echo ""
echo ==========
echo Test 01) Add 1 local unique file
call:create_file aaa aaa
echo Test 01) DONE

echo ""
echo ==========
echo Test 02) Add 2 local unique files
call:create_file bbb bbb
echo Test 02) DONE

echo ""
echo ==========
echo Test 03) Add 3 local unique files
call:create_file ccc ccc
echo Test 03) DONE

echo ""
echo ==========
echo Test 04) Delete 1 local file
call:create_file ddd ddd
call:remove_file ddd
echo Test 04) DONE

echo ""
echo ==========
echo Test 05) Move 1 local file to other name
call:create_file eee eee
call:move_file eee fff
echo Test 05) DONE

echo ""
echo ==========
echo Test 06) Move 1 local file to other directory same file name
call:create_file ggg ggg
call:create_dir mydir
call:move_file ggg mydir\ggg
echo Test 06) DONE

echo ""
echo ==========
echo Test 07) Move 1 local file to other directory other file name
call:create_file hhh hhh
call:create_dir mydir1
call:move_file hhh mydir1\iii
echo Test 07) DONE

echo ""
echo ==========
echo Test 08) Add 1 local dupe file
call:create_file jjj jjj
call:create_file kkk jjj
echo Test 08) DONE

echo ""
echo ==========
echo Test 09) Add 2 local dupe file
call:create_file lll lll
call:create_file mmm lll
call:create_file nnn lll
echo Test 09) DONE

echo ""
echo ==========
echo Test 10) Add 3 local dupe file
call:create_file ooo ooo
call:create_file ppp ooo
call:create_file qqq ooo
call:create_file rrr ooo
echo Test 10) DONE

echo ""
echo ==========
echo Test 11) Delete 1 local link file
call:create_file sss sss
call:create_file ttt sss
call:remove_file ttt
echo Test 11) DONE

echo ""
echo ==========
echo Test 12) Move 1 local link file to other name
call:create_file uuu uuu
call:create_file vvv uuu 
call:move_file vvv xxx
echo Test 12) DONE

echo ""
echo ==========
echo Test 13) Move 1 local link file to other directory same link name
call:create_file yyy yyy
call:create_file zzz yyy 
call:create_dir mydir2
call:move_file zzz mydir2\zzz
echo Test 13) DONE

echo ""
echo ==========
echo Test 14) Move 1 local link file to other directory other link name
call:create_file aaaa aaaa
call:create_file bbbb aaaa
call:create_dir mydir3
call:move_file bbbb mydir3\cccc
echo Test 14) DONE

echo ""
echo ==========
echo Test 15) Delete 1 local parent file
call:create_file dddd dddd
call:create_file eeee dddd
call:create_file ffff dddd
call:create_file gggg dddd
call:remove_file dddd
echo Test 15) DONE


echo ""
echo ==========
echo Test 15a) Delete 1 local parent file
call:create_file dddd dddd
call:create_file eeee dddd
call:create_file ffff dddd
call:create_file gggg dddd
call:remove_file dddd
call:create_file dddd dddd
echo Test 15a) DONE


echo ""
echo ==========
echo Test 16) Move 1 local parent file to other name
call:create_file eeee eeee
call:create_file ffff eeee
call:move_file eeee gggg
echo Test 16) DONE

echo ""
echo ==========
echo Test 17) Move 1 local parent file to other directory same file name
call:create_file hhhh hhhh
call:create_file iiii hhhh
call:create_dir mydir4
call:move_file hhhh mydir4\hhhh
echo Test 17) DONE

echo ""
echo ==========
echo Test 18) Move 1 local parent file to other directory other file name
call:create_file jjjj jjjj
call:create_file kkkk jjjj
call:create_dir mydir5
call:move_file jjjj mydir5\llll
echo Test 18) DONE

echo ""
echo ==========
echo Test 19) Recover 1 local file with no links
call:create_file mmmm mmmm
call:remove_file mmmm
call:create_file mmmm mmmm
echo Test 19) DONE

echo ""
echo ==========
echo Test 20) Recover 1 local parent file with 1 link
call:create_file nnnn nnnn
call:create_file oooo nnnn
call:remove_file nnnn
call:create_file nnnn nnnn
echo Test 20) DONE

echo ""
echo ==========
echo Test 21) Recover 1 local parent file with 2 links
call:create_file pppp pppp
call:create_file qqqq pppp
call:create_file rrrr pppp
call:remove_file pppp
call:create_file pppp pppp
echo Test 21) DONE

echo ""
echo ==========
echo Test 22) Recover 1 local parent file with 3 links
call:create_file aaaaa aaaaa
call:create_file bbbbb aaaaa
call:create_file ccccc aaaaa
call:create_file ddddd aaaaa
call:remove_file aaaaa
call:create_file aaaaa aaaaa
echo Test 22) DONE

echo ""
echo ==========
echo Test 23) Recover 1 local parent file with 1 link in other directory
call:create_file aaaaaa aaaaaa
call:create_dir mydir6
call:create_file mydir6\bbbbbb aaaaaa
call:remove_file aaaaaa
call:create_file aaaaaa aaaaaa
echo Test 23) DONE

echo ""
echo ==========
echo Test 24) Recover 1 local parent file with 2 links in different directories
call:create_file aaaaaaa aaaaaaa
call:create_dir mydir7
call:create_file mydir7\bbbbbbb aaaaaaa
call:create_file mydir7\ccccccc aaaaaaa
call:remove_file aaaaaaa
call:create_file aaaaaaa aaaaaaa
echo Test 24) DONE

echo ""
echo ==========
echo Test 25) Recover 1 local parent file with 3 links in different directories
call:create_file aaaaaaaa aaaaaaaa
call:create_dir mydir8
call:create_file mydir8\bbbbbbbb aaaaaaaa
call:create_file mydir8\cccccccc aaaaaaaa
call:create_file mydir8\dddddddd aaaaaaaa
call:remove_file aaaaaaaa
call:create_file aaaaaaaa aaaaaaaa
echo Test 25) DONE

:create_file
set filename1=%~1
set fcontent1=%~2
echo %fcontent1% > !basedir!%filename1%
echo !basedir!%filename1%
pause
EXIT /B 0

:remove_file
set filename1=%~1
del !basedir!%filename1%
echo !basedir!%filename1%*
pause
EXIT /B 0

:move_file
set filename1=%~1
set filename2=%~2    
move !basedir!%filename1% !basedir!%filename2%
echo !basedir!%filename2%
pause
EXIT /B 0

:create_dir
set filename1=%~1
mkdir !basedir!%filename1%
echo !basedir!%filename1%
pause
EXIT /B 0