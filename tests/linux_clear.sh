#!/bin/sh
basedir="/home/vliopard/temp/"
# Test 01) Add 1 local unique file
rm ${basedir}aaa

#  Test 02) Add 2 local unique files
rm ${basedir}bbb

#  Test 03) Add 3 local unique files
rm ${basedir}ccc

#  Test 04) Delete 1 local file

#  Test 05) Move 1 local file to other name
rm ${basedir}fff

#  Test 06) Move 1 local file to other directory same file name
rm ${basedir}mydir/ggg
rmdir ${basedir}mydir

#  Test 07) Move 1 local file to other directory other file name
rm ${basedir}mydir1/iii
rmdir ${basedir}mydir1

#  Test 08) Add 1 local dupe file
rm ${basedir}jjj
rm ${basedir}kkk

#  Test 09) Add 2 local dupe file
rm ${basedir}lll
rm ${basedir}mmm
rm ${basedir}nnn

#  Test 10) Add 3 local dupe file
rm ${basedir}ooo
rm ${basedir}ppp
rm ${basedir}qqq
rm ${basedir}rrr

#  Test 11) Delete 1 local link file
rm ${basedir}sss

#  Test 12) Move 1 local link file to other name
rm ${basedir}uuu
rm ${basedir}xxx

#  Test 13) Move 1 local link file to other directory same link name
rm ${basedir}yyy
rm ${basedir}mydir2/zzz
rmdir ${basedir}mydir2

#  Test 14) Move 1 local link file to other directory other link name
rm ${basedir}000
rm ${basedir}mydir3/222
rmdir ${basedir}mydir3

#  Test 15) Delete 1 local parent file

#  Test 16) Move 1 local parent file to other name
rm ${basedir}555
rm ${basedir}666

#  Test 17) Move 1 local parent file to other directory same file name
rm ${basedir}888
rm ${basedir}mydir4/777
rmdir ${basedir}mydir4

#  Test 18) Move 1 local parent file to other directory other file name
rm ${basedir}abc
rm ${basedir}mydir5/def
rmdir ${basedir}mydir5

