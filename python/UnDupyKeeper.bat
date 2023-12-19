@echo off
IF "%2"=="" (
    python UnDupeKeeper.py --path %1 > UnDupyKeeper.out
) ELSE (
    python UnDupeKeeper.py --path %1 --scan %2 > UnDupyKeeper.out
)