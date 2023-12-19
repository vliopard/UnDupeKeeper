@ echo off
set result=sedresult.sh
copy %1 %result%

sed -i "s/set //g" %result%
sed -i "s/call:/call_/g" %result%
sed -i "s/pause/call_pause/g" %result%
sed -i "s/\%%label\%%/\$\{label\}/g" %result%
sed -i "s/REM/\# REM/g" %result%
sed -i "s/\\/\//g" %result%
sed -i -r "s/\%%name([0-9]+)\%%/\$\{name\1\}/g" %result%
sed -i -r "s/\%%dir([0-9]+)\%%/\$\{dir\1\}/g" %result%
