@echo off
IF "%1"=="" (
    python UnDupeKeeper.py > UnDupyKeeper.out
) ELSE (
    IF "%2"=="" (
        python UnDupeKeeper.py --path %1 > UnDupyKeeper.out
    ) ELSE (
        python UnDupeKeeper.py --path %1 --scan %2 > UnDupyKeeper.out
    )
)