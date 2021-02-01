@ echo off
cls

setlocal enabledelayedexpansion

set basedir=c:\vliopard\tests\

echo -
echo ____________________________
echo Test 01) Add 1 local unique file
call:create_file aaa aaa

call:check_file aaa file
call:assert %file% 1
echo Test 01) DONE

echo -
echo ____________________________
echo Test 02) Add 2 local unique files
call:create_file bbb bbb

call:check_file bbb file
call:assert %file% 1
echo Test 02) DONE

echo -
echo ____________________________
echo Test 03) Add 3 local unique files
call:create_file ccc ccc

call:check_file ccc file
call:assert %file% 1
echo Test 03) DONE

echo -
echo ____________________________
echo Test 04) Delete 1 local file
call:create_file ddd ddd
call:remove_file ddd

call:check_file ddd file
call:assert %file% 0
echo Test 04) DONE

echo -
echo ____________________________
echo Test 05) Move 1 local file to other name
call:create_file eee eee
call:move_file eee fff

call:check_file eee file
call:assert %file% 0

call:check_file fff file
call:assert %file% 1
echo Test 05) DONE

echo -
echo ____________________________
echo Test 06) Move 1 local file to other directory same file name
call:create_file ggg ggg
call:create_dir mydir
call:move_file ggg mydir\ggg

call:check_file ggg file
call:assert %file% 0

call:check_file mydir\ggg file
call:assert %file% 1
echo Test 06) DONE

echo -
echo ____________________________
echo Test 07) Move 1 local file to other directory other file name
call:create_file hhh hhh
call:create_dir mydir1
call:move_file hhh mydir1\iii

call:check_file hhh file
call:assert %file% 0

call:check_file mydir1\iii file
call:assert %file% 1
echo Test 07) DONE

echo -
echo ____________________________
echo Test 08) Add 1 local dupe file
call:create_file jjj jjj
call:create_file kkk jjj

call:check_file jjj file
call:assert %file% 1

call:check_link kkk file
call:assert %file% 1

call:compare_file jjj kkk file
call:assert %file% 0
echo Test 08) DONE

echo -
echo ____________________________
echo Test 09) Add 2 local dupe file
call:create_file lll lll
call:create_file mmm lll
call:create_file nnn lll

call:check_file lll file
call:assert %file% 1

call:check_link mmm file
call:assert %file% 1

call:check_link nnn file
call:assert %file% 1

call:compare_file lll mmm file
call:assert %file% 0

call:compare_file lll nnn file
call:assert %file% 0
echo Test 09) DONE

echo -
echo ____________________________
echo Test 10) Add 3 local dupe file
call:create_file ooo ooo
call:create_file ppp ooo
call:create_file qqq ooo
call:create_file rrr ooo

call:check_file ooo file
call:assert %file% 1

call:check_link ppp file
call:assert %file% 1

call:check_link qqq file
call:assert %file% 1

call:check_link rrr file
call:assert %file% 1

call:compare_file ooo ppp file
call:assert %file% 0

call:compare_file ooo qqq file
call:assert %file% 0

call:compare_file ooo rrr file
call:assert %file% 0
echo Test 10) DONE

echo -
echo ____________________________
echo Test 11) Delete 1 local link file
call:create_file sss sss
call:create_file ttt sss
call:remove_file ttt

call:check_file sss file
call:assert %file% 1

call:check_link ttt file
call:assert %file% 0
echo Test 11) DONE

echo -
echo ____________________________
echo Test 12) Move 1 local link file to other name
call:create_file uuu uuu
call:create_file vvv uuu 
call:move_file vvv xxx

call:check_file uuu file
call:assert %file% 1

call:check_link vvv file
call:assert %file% 0

call:check_link xxx file
call:assert %file% 1
echo Test 12) DONE

echo -
echo ____________________________
echo Test 13) Move 1 local link file to other directory same link name
call:create_file yyy yyy
call:create_file zzz yyy 
call:create_dir mydir2
call:move_file zzz mydir2\zzz

call:check_file yyy file
call:assert %file% 1

call:check_link zzz file
call:assert %file% 0

call:check_link mydir2\zzz file
call:assert %file% 1
echo Test 13) DONE

echo -
echo ____________________________
echo Test 14) Move 1 local link file to other directory other link name
call:create_file aaaa aaaa
call:create_file bbbb aaaa
call:create_dir mydir3
call:move_file bbbb mydir3\cccc

call:check_file aaaa file
call:assert %file% 1

call:check_link bbbb file
call:assert %file% 0

call:check_link mydir3\cccc file
call:assert %file% 1
echo Test 14) DONE

echo -
echo ____________________________
echo Test 15) Delete 1 local parent file
call:create_file dddd dddd
call:create_file eeee dddd
call:create_file ffff dddd
call:create_file gggg dddd
call:remove_file dddd

call:check_file dddd file
call:assert %file% 0

call:check_link eeee file
call:assert %file% 0

call:check_link ffff file
call:assert %file% 0

call:check_link gggg file
call:assert %file% 0
echo Test 15) DONE

echo -
echo ____________________________
echo Test 16) Move 1 local parent file to other name
call:create_file eeee eeee
call:create_file ffff eeee
call:move_file eeee gggg

call:check_file eeee file
call:assert %file% 0

call:check_link ffff file
call:assert %file% 1

call:check_file gggg file
call:assert %file% 1
echo Test 16) DONE

echo -
echo ____________________________
echo Test 17) Move 1 local parent file to other directory same file name
call:create_file hhhh hhhh
call:create_file iiii hhhh
call:create_dir mydir4
call:move_file hhhh mydir4\hhhh

call:check_file hhhh file
call:assert %file% 0

call:check_link iiii file
call:assert %file% 1

call:check_link mydir4\hhhh file
call:assert %file% 1
echo Test 17) DONE

echo -
echo ____________________________
echo Test 18) Move 1 local parent file to other directory other file name
call:create_file jjjj jjjj
call:create_file kkkk jjjj
call:create_dir mydir5
call:move_file jjjj mydir5\llll

call:check_file jjjj file
call:assert %file% 0

call:check_link kkkk file
call:assert %file% 1

call:check_link mydir5\llll file
call:assert %file% 1
echo Test 18) DONE

echo -
echo ____________________________
echo Test 19) Recover 1 local file with no links
call:create_file mmmm mmmm
call:remove_file mmmm
call:create_file mmmm mmmm

call:check_file mmmm file
call:assert %file% 1
echo Test 19) DONE

echo -
echo ____________________________
echo Test 20) Recover 1 local parent file with 1 link
call:create_file nnnn nnnn
call:create_file oooo nnnn
call:remove_file nnnn
call:create_file nnnn nnnn

call:check_file nnnn file
call:assert %file% 1

call:check_link oooo file
call:assert %file% 1
echo Test 20) DONE

echo -
echo ____________________________
echo Test 21) Recover 1 local parent file with 2 links
call:create_file pppp pppp
call:create_file qqqq pppp
call:create_file rrrr pppp
call:remove_file pppp
call:create_file pppp pppp

call:check_file pppp file
call:assert %file% 1

call:check_link qqqq file
call:assert %file% 1

call:check_link rrrr file
call:assert %file% 1
echo Test 21) DONE

echo -
echo ____________________________
echo Test 22) Recover 1 local parent file with 3 links
call:create_file aaaaa aaaaa
call:create_file bbbbb aaaaa
call:create_file ccccc aaaaa
call:create_file ddddd aaaaa
call:remove_file aaaaa
call:create_file aaaaa aaaaa

call:check_file aaaaa file
call:assert %file% 1

call:check_link bbbbb file
call:assert %file% 1

call:check_link ccccc file
call:assert %file% 1

call:check_link ddddd file
call:assert %file% 1
echo Test 22) DONE

echo -
echo ____________________________
echo Test 23) Recover 1 local parent file with 1 link in other directory
call:create_file aaaaaa aaaaaa
call:create_dir mydir6
call:create_file mydir6\bbbbbb aaaaaa
call:remove_file aaaaaa
call:create_file aaaaaa aaaaaa

call:check_file aaaaaa file
call:assert %file% 1

call:check_link mydir6\bbbbbb file
call:assert %file% 1
echo Test 23) DONE

echo -
echo ____________________________
echo Test 24) Recover 1 local parent file with 2 links in different directories
call:create_file aaaaaaa aaaaaaa
call:create_dir mydir7
call:create_file mydir7\bbbbbbb aaaaaaa
call:create_file mydir7\ccccccc aaaaaaa
call:remove_file aaaaaaa
call:create_file aaaaaaa aaaaaaa

call:check_file aaaaaaa file
call:assert %file% 1

call:check_link mydir7\bbbbbbb file
call:assert %file% 1

call:check_link mydir7\ccccccc file
call:assert %file% 1
echo Test 24) DONE

echo -
echo ____________________________
echo Test 25) Recover 1 local parent file with 3 links in different directories
call:create_file aaaaaaaa aaaaaaaa
call:create_dir mydir8
call:create_file mydir8\bbbbbbbb aaaaaaaa
call:create_file mydir8\cccccccc aaaaaaaa
call:create_file mydir8\dddddddd aaaaaaaa
call:remove_file aaaaaaaa
call:create_file aaaaaaaa aaaaaaaa

call:check_file aaaaaaaa file
call:assert %file% 1

call:check_link mydir8\bbbbbbbb file
call:assert %file% 1

call:check_link mydir8\cccccccc file
call:assert %file% 1

call:check_link mydir8\dddddddd file
call:assert %file% 1
echo Test 25) DONE

:create_file
set filename1=%~1
set fcontent1=%~2
echo %fcontent1% > !basedir!%filename1%
echo !basedir!%filename1%
call:press_key
EXIT /B 0

:remove_file
set filename1=%~1
del !basedir!%filename1%
echo !basedir!%filename1%*
call:press_key
EXIT /B 0

:move_file
set filename1=%~1
set filename2=%~2    
move !basedir!%filename1% !basedir!%filename2%
echo !basedir!%filename2%
call:press_key
EXIT /B 0

:create_dir
set filename1=%~1
mkdir !basedir!%filename1%
echo !basedir!%filename1%
call:press_key
EXIT /B 0

:check_file
if exist !basedir!%~1 (
    set "%~2=1"
) else (
    set "%~2=0"
)
EXIT /B 0

:check_link
set tmpfile=temp_file.tmp
fsutil reparsepoint query "!basedir!%~1" > "%tmpfile%"
if %errorlevel% == 0 set "%~2=1"
if %errorlevel% == 1 set "%~2=0"
del temp_file.tmp
EXIT /B 0

:assert
if %~1==%~2 (
    echo =
    echo ======= [ PASSED ] =======
    echo =
) else (
    echo =
    echo ======= [ FAILED ] =======
    echo =
)
EXIT /B 0

:compare
fc /b !basedir!%~1 !basedir!%~2 > nul
if %errorlevel% == 0 set "%~3=1"
if %errorlevel% == 1 set "%~3=0"
EXIT /B 0

:press_key
timeout 1 > NUL
EXIT /B 0