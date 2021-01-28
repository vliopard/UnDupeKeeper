#!/bin/bash
timeout=1
basedir="/home/vliopard/temp/"

# Test 01) Add 1 local unique file
echo aaa > ${basedir}aaa
read -t ${timeout} -p "${basedir}aaa"
echo ""

#  Test 02) Add 2 local unique files
echo bbb > ${basedir}bbb
read -t ${timeout} -p "${basedir}bbb"
echo ""

#  Test 03) Add 3 local unique files
echo ccc > ${basedir}ccc
read -t ${timeout} -p "${basedir}ccc"
echo ""

#  Test 04) Delete 1 local file
echo ddd > ${basedir}ddd
read -t ${timeout} -p "${basedir}ddd"
echo ""
rm ${basedir}ddd
read -t ${timeout} -p "${basedir}ddd*"
echo ""

#  Test 05) Move 1 local file to other name
echo eee > ${basedir}eee
read -t ${timeout} -p "${basedir}eee"
echo ""
mv ${basedir}eee ${basedir}fff
read -t ${timeout} -p "${basedir}fff"
echo ""

#  Test 06) Move 1 local file to other directory same file name
echo ggg > ${basedir}ggg
read -t ${timeout} -p "${basedir}ggg"
echo ""
mkdir ${basedir}mydir
read -t ${timeout} -p "${basedir}mydir"
echo ""
mv ${basedir}ggg ${basedir}mydir/ggg
read -t ${timeout} -p "${basedir}mydir/ggg"
echo ""

#  Test 07) Move 1 local file to other directory other file name
echo hhh > ${basedir}hhh
read -t ${timeout} -p "${basedir}hhh"
echo ""
mkdir ${basedir}mydir1
read -t ${timeout} -p "${basedir}mydir1"
echo ""
mv ${basedir}hhh ${basedir}mydir1/iii
read -t ${timeout} -p "${basedir}mydir1/iii"
echo ""

#  Test 08) Add 1 local dupe file
echo jjj > ${basedir}jjj
read -t ${timeout} -p "${basedir}jjj"
echo ""
echo jjj > ${basedir}kkk
read -t ${timeout} -p "${basedir}kkk"
echo ""

#  Test 09) Add 2 local dupe file
echo lll > ${basedir}lll
read -t ${timeout} -p "${basedir}lll"
echo ""
echo lll > ${basedir}mmm
read -t ${timeout} -p "${basedir}mmm"
echo ""
echo lll > ${basedir}nnn
read -t ${timeout} -p "${basedir}nnn"
echo ""

#  Test 10) Add 3 local dupe file
echo ooo > ${basedir}ooo
read -t ${timeout} -p "${basedir}ooo"
echo ""
echo ooo > ${basedir}ppp
read -t ${timeout} -p "${basedir}ppp"
echo ""
echo ooo > ${basedir}qqq
read -t ${timeout} -p "${basedir}qqq"
echo ""
echo ooo > ${basedir}rrr
read -t ${timeout} -p "${basedir}rrr"
echo ""

#  Test 11) Delete 1 local link file
echo sss > ${basedir}sss
read -t ${timeout} -p "${basedir}sss"
echo ""
echo sss > ${basedir}ttt
read -t ${timeout} -p "${basedir}ttt"
echo ""
rm "${basedir}ttt"
read -t ${timeout} -p "${basedir}ttt*"
echo ""

#  Test 12) Move 1 local link file to other name
echo uuu > ${basedir}uuu
read -t ${timeout} -p "${basedir}uuu"
echo ""
echo uuu > ${basedir}vvv
read -t ${timeout} -p "${basedir}vvv"
echo ""
mv ${basedir}vvv ${basedir}xxx
read -t ${timeout} -p "${basedir}xxx"
echo ""

#  Test 13) Move 1 local link file to other directory same link name
echo yyy > ${basedir}yyy
read -t ${timeout} -p "${basedir}yyy"
echo ""
echo yyy > ${basedir}zzz
read -t ${timeout} -p "${basedir}zzz"
echo ""
mkdir ${basedir}mydir2
mv ${basedir}zzz ${basedir}mydir2/zzz
read -t ${timeout} -p "${basedir}mydir2/zzz"
echo ""

#  Test 14) Move 1 local link file to other directory other link name
echo 000 > ${basedir}000
read -t ${timeout} -p "${basedir}000"
echo ""
echo 000 > ${basedir}111
read -t ${timeout} -p "${basedir}111"
echo ""
mkdir ${basedir}mydir3
mv ${basedir}111 ${basedir}mydir3/222
read -t ${timeout} -p "${basedir}mydir3/222"
echo ""

#  Test 15) Delete 1 local parent file
echo 333 > ${basedir}333
read -t ${timeout} -p "${basedir}333"
echo ""
echo 333 > ${basedir}444
read -t ${timeout} -p "${basedir}444"
echo ""
echo 333 > ${basedir}555
read -t ${timeout} -p "${basedir}555"
echo ""
echo 333 > ${basedir}666
read -t ${timeout} -p "${basedir}666"
echo ""
rm ${basedir}333
read -t ${timeout} -p "${basedir}333*"

#  Test 16) Move 1 local parent file to other name
echo 444 > ${basedir}444
read -t ${timeout} -p "${basedir}444"
echo ""
echo 444 > ${basedir}555
read -t ${timeout} -p "${basedir}555"
echo ""
mv ${basedir}444 ${basedir}666
read -t ${timeout} -p "${basedir}666"
echo ""

#  Test 17) Move 1 local parent file to other directory same file name
echo 777 > ${basedir}777
read -t ${timeout} -p "${basedir}777"
echo ""
echo 777 > ${basedir}888
read -t ${timeout} -p "${basedir}888"
echo ""
mkdir ${basedir}mydir4
read -t ${timeout} -p "${basedir}mydir4"
echo ""
mv ${basedir}777 ${basedir}mydir4/777
read -t ${timeout} -p "${basedir}mydir4/777"
echo ""

#  Test 18) Move 1 local parent file to other directory other file name
echo 999 > ${basedir}999
read -t ${timeout} -p "${basedir}999"
echo ""
echo 999 > ${basedir}abc
read -t ${timeout} -p "${basedir}abc"
echo ""
mkdir ${basedir}mydir5
read -t ${timeout} -p "${basedir}mydir5"
echo ""
mv ${basedir}999 ${basedir}mydir5/def
read -t ${timeout} -p "${basedir}mydir5/def"
echo ""

