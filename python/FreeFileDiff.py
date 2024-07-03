import UnDupeKeeper

trials = [UnDupeKeeper.BUFFER,
          UnDupeKeeper.COMPARE,
          UnDupeKeeper.ZIP_LONGEST,
          UnDupeKeeper.NATIVE,
          UnDupeKeeper.EXECUTABLE,
          UnDupeKeeper.HASH_MD5,
          UnDupeKeeper.HASH_SHA,
          UnDupeKeeper.HASH_SHA256,
          UnDupeKeeper.HASH_SHA512]

trial = trials[1]

if __name__ == "__main__":

    oks = 0
    total = 0
    final_file = []
    with open('input.txt', 'r', encoding='utf-8') as ip:
        fp = False
        rp = False
        a = ''
        b = ''
        for line in ip:
            if line.strip():
                content = line.strip()
                if content == 'Folder Pairs':
                    fp = True
                    continue
                if content == 'Relative path;Size;Difference;Action;Relative path;Size':
                    rp = True
                    continue
                if fp:
                    fp = False
                    a = line.strip().split(";")[0]
                    b = line.strip().split(";")[1]
                if rp:
                    f1 = f'{a}\\{line.strip().split(";")[0]}'
                    f2 = f'{b}\\{line.strip().split(";")[4]}'
                    result = f'{f1}; {f2}\n'
                    try:
                        print('_'*50)
                        print('COMPARING')
                        print(f'[{f1}]')
                        print(f'[{f2}]')
                        print('Phase 0...')
                        answer0 = UnDupeKeeper.file_equals(f1, f2, trials[0])
                        print('Phase 1...')
                        answer1 = UnDupeKeeper.file_equals(f1, f2, trials[1])
                        print('Phase 2...')
                        answer2 = UnDupeKeeper.file_equals(f1, f2, trials[2])
                        print('Phase 3...')
                        answer3 = UnDupeKeeper.file_equals(f1, f2, trials[3])
                        print('Done...')
                        answer = answer0 and answer1 and answer2 and answer3
                        if not answer:
                            print(f"FAIL [{answer0}] [{answer1}] [{answer2}] [{answer3}]")
                            final_file.append(result)
                            total += 1
                        else:
                            print("OK")
                            oks += 1
                    except PermissionError as pe:
                        final_file.append(result)
                        total += 1
                        print(f'ERROR: [{pe}]')

    with open('output.txt', 'w', encoding='utf-8') as op:
        op.writelines(final_file)

    print(f'[{total}/{oks+total}]')
