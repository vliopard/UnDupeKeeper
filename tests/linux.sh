#!/bin/bash
timeout=1
basedir="/home/vliopard/temp/"

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


