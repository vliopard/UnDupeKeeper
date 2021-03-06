package main;

import java.io.BufferedInputStream;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import settings.Settings;
import settings.Strings;
import tools.FileOperations;
import tools.Logger;
import tools.ProgressBarDialog;
import tools.ReportGenerator;
import tools.StreamManager;
import tools.TimeControl;
import tools.Utils;
import org.apache.commons.io.FileUtils;

public class Comparison
{
    static ProgressBarDialog progressBarDialog;

    public static boolean compareBySize(Path f1, Path f2)
    {
        long fsize = 0;
        try
        {
            fsize = Files.size(f1);
            // TODO: REPLACE NUMBER FOR CONSTANT (CONFIGURABLE SETTINGS)
            if (fsize < 350000000)
            {
                return isArrayEqual(f1, f2);
            }
        }
        catch (IOException e)
        {
            // TODO: REPLACE GENERIC EXCEPTION
            Logger.err("MSG_002: " + Strings.generic + e);
        }
        return runSystemCompare(f1, f2, 0);
    }

    public static boolean isArrayEqual(Path firstFile, Path secondFile)
    {
        try
        {
            if (Files.size(firstFile) != Files.size(secondFile))
            {
                return false;
            }

            byte[ ] first  = Files.readAllBytes(firstFile);
            byte[ ] second = Files.readAllBytes(secondFile);
            return Arrays.equals(first, second);
        }
        catch (IOException e)
        {
            // TODO: REPLACE GENERIC EXCEPTION
            Logger.err("MSG_003: " + Strings.generic + e);
        }
        return false;
    }

    public static boolean isBufferedEqual(Path firstFile, Path secondFile)
    {
        try
        {
            long size = Files.size(firstFile);
            if (size != Files.size(secondFile))
            {
                return false;
            }

            if (size < 2048)
            {
                return Arrays.equals(Files.readAllBytes(firstFile), Files.readAllBytes(secondFile));
            }

            // Compare character-by-character
            BufferedReader bufferedFile1 = Files.newBufferedReader(firstFile, Charset.forName("ISO-8859-1"));
            BufferedReader bufferedFile2 = Files.newBufferedReader(secondFile, Charset.forName("ISO-8859-1"));

            int ch = bufferedFile1.read( );
            while (ch != -1)
            {
                if (ch != bufferedFile2.read( ))
                {
                    return false;
                }
                ch = bufferedFile1.read( );
            }
            return true;
        }
        catch (IOException e)
        {
            // TODO: REPLACE GENERIC EXCEPTION
            Logger.err("MSG_004: " + Strings.generic + e);
        }
        return false;
    }

    public static boolean isFileEqual(Path file1, Path file2)
    {
        try
        {
            return FileUtils.contentEquals(file1.toFile( ), file2.toFile( ));
        }
        catch (IOException e)
        {
            // TODO: REPLACE GENERIC EXCEPTION
            Logger.err("MSG_005: " + Strings.generic + e);
            return false;
        }
    }

    public static boolean compareFiles(Path f1, Path f2)
    {
        if (0 == f1.compareTo(f2))
        {
            return true;
        }
        return false;
    }

    public static boolean isBinaryIdentical(Path binaryFilePath1, Path binaryFilePath2)
    {
        if ((binaryFilePath1.toFile( ).exists( )) && (binaryFilePath1.toFile( ).isFile( )) &&
                (binaryFilePath2.toFile( ).exists( )) && (binaryFilePath2.toFile( ).isFile( )))
        {
            try
            {
                boolean a = false;
                boolean b = false;
                boolean c = false;
                if (Files.isSameFile(binaryFilePath1, binaryFilePath2))
                {
                    a = true;
                }
                if (binaryFilePath1.toFile( ).getCanonicalPath( ).equals(binaryFilePath2.toFile( ).getCanonicalPath( )))
                {
                    b = true;
                }
                if (0 == binaryFilePath1.compareTo(binaryFilePath2))
                {
                    c = true;
                }
                if (a && b && c)
                {
                    return true;
                }
            }
            catch (IOException e)
            {
                /* In case of path equality comparison failure, it is safe to return false instead of letting it
                 * continue next steps, otherwise, file could be erroneously marked as duplicate. This is not a
                 * desirable behavior. Better returning false instead. */
                Logger.err("MSG_006: " + Strings.cannotCompareFiles + binaryFilePath1 +
                        Settings.Blank + binaryFilePath2);
                return false;
            }
            if (binaryFilePath1.toFile( ).length( ) != binaryFilePath2.toFile( ).length( ))
            {
                /* If sizes are different there is no reason for continuing any other way of comparison, including the
                 * expensive binary one. */
                return false;
            }
            /* All checks passed. Files are eligible to be checked. Going ahead! */
            return isFileBinaryEqual(binaryFilePath1, binaryFilePath2);
        }
        else
        {
            /* There is a problem with some of those files. Maybe not exist, maybe is not a file. Must not proceed. */
            return false;
        }
    }

    public static boolean runSystemCompare(Path f1, Path f2, int type)
    {
        switch (type)
        {
            case 0:
                return Utils.runSystemCommand(Settings.SysNatCompareCommand + Settings.QuoteFile + f1.toString( )
                        + Settings.QuoteFile + Settings.Blank + Settings.QuoteFile + f2.toString( )
                        + Settings.QuoteFile, 1);

            case 1:
                return Utils.runSystemCommand(Settings.SysExeCompareCommand + Settings.QuoteFile + f1.toString( )
                        + Settings.QuoteFile + Settings.Blank + Settings.QuoteFile + f2.toString( )
                        + Settings.QuoteFile, 1);

            default:
                return false;
        }
    }

    public static long totalLines(String textFileName)
    {
        long lineCount = 0;
        try
        {
            // Third
            InputStreamReader textFileNameInputStreamReader = StreamManager.InputStreamReader(textFileName);
            BufferedReader    textFileNameBufferedReader    = new BufferedReader(textFileNameInputStreamReader);
            while ((textFileNameBufferedReader.readLine( )) != null)
            {
                lineCount++;
            }
            textFileNameInputStreamReader.close( );
            textFileNameBufferedReader.close( );
        }
        catch (IOException e)
        {
            Logger.err("MSG_007: " + Strings.problemCountingLines + e);
            return -1;
        }
        return lineCount;
    }

    public static long totalLinesFast(String textFileName)
    {
        InputStream is;
        try
        {
            is = new BufferedInputStream(new FileInputStream(textFileName));
            byte[ ] c         = new byte[1024];
            int     count     = 0;
            int     readChars = 0;
            while ((readChars = is.read(c)) != -1)
            {
                for (int i = 0; i < readChars; ++i)
                {
                    if (c[i] == '\n')
                    {
                        ++count;
                    }
                }
            }
            is.close( );
            return count;
        }
        catch (IOException e)
        {
            e.printStackTrace( );
        }
        return -1;
    }

    private static String checkFileName(String fileName)
    {
        if (fileName.length( ) > 255)
        {
            String path1 = Settings.Empty;
            String name1 = Settings.Empty;
            String path2 = Settings.Empty;
            String name2 = Settings.Empty;
            String tail  = Settings.Empty;
            int    slash = fileName.lastIndexOf(Settings.Slash) + 1;
            int    at    = fileName.lastIndexOf("@@");
            int    start = fileName.indexOf("-_(");
            int    end   = fileName.lastIndexOf(")_[_");
            if (slash > 0)
            {
                path1 = fileName.substring(0, slash);
                if (slash < at)
                {
                    name1 = fileName.substring(slash, at);
                }
                else
                {
                    Logger.err("MSG_008: " + Strings.fileNameContainsDoubleAt + name1);
                    System.exit( -1);
                }
                if (at < start)
                {
                    path2 = fileName.substring(at, start);
                }
                else
                {
                    Logger.err("MSG_009: " + Strings.fileNameWrongDoubleAtPosition + path2);
                    System.exit( -1);
                }
                if (start < end)
                {
                    name2 = fileName.substring(start, end);
                }
                else
                {
                    Logger.err("MSG_010: " + Strings.fileNameMismatchStartEnd + name2);
                    System.exit( -1);
                }
                tail = fileName.substring(end);
            }
            int n1l = name1.length( );
            int n2l = name2.length( );
            int p2l = path2.length( );
            do
            {
                if (n2l > 1)
                {
                    n2l--;
                }
                else
                {
                    if (n1l > 1)
                    {
                        n1l--;
                    }
                    else
                    {
                        if (p2l > 2)
                        {
                            p2l--;
                            path2 = path2.substring(0, p2l);
                        }
                        else
                        {
                            Logger.err("MSG_011: " + Strings.cannotShrinkFilename);
                            Logger.err("[" + fileName.length( ) + "] " + fileName);
                            break;
                        }
                    }
                }
                fileName = path1 + name1.substring(0, n1l) + path2 + name2.substring(0, n2l) + tail;
            }
            while (fileName.length( ) > 255);
        }
        return fileName;
    }

    private static void renameDuplicatedFile(Path fileName1, Path fileName2, long fileCounter)
    {
        if (fileName1.toString( ).contains(Settings.UnDupeKeeperNoRename))
        {
            return;
        }
        String removeMarker           = Settings.UnDupeKeeperMarker + "_[" + fileCounter + "]_";
        String originalSourceFileName = FileOperations.getDirectory(fileName2.toString( )) + "_("
                + FileOperations.getFileName(fileName2.toString( ))
                + FileOperations.getFileExtension(fileName2.toString( )) + ")_";
        originalSourceFileName = originalSourceFileName.replace(':', '#');
        originalSourceFileName = originalSourceFileName.replace(Settings.cSlash, '-');
        String newFileName1 = FileOperations.getDirectory(fileName1.toString( ))
                + FileOperations.getFileName(fileName1.toString( ))
                + FileOperations.getFileExtension(fileName1.toString( )) + "@@" + originalSourceFileName + removeMarker;
        if (fileName1.toString( ).lastIndexOf(Settings.Dot) > fileName1.toString( ).lastIndexOf(Settings.Slash))
        {
            newFileName1 = newFileName1 + FileOperations.getFileExtension(fileName1.toString( ));
        }
        try
        {
            // TODO: CHECK A SAFE WAY TO CREATE A SYMBOLIC LINK INSTEAD OF RENAMING FILE
            newFileName1 = checkFileName(newFileName1);
            java.nio.file.Files.move(fileName1, Paths.get(newFileName1));
            // FileUtils.file(fileName1).renameTo(FileUtils.file(newFileName1));
        }
        catch (IOException e)
        {
            Logger.err(Settings.Separator + Settings.Separator);
            Logger.err(Strings.sourceFileName + "[" + fileName1.toString( ).length( ) + "] " + fileName1.toString( )
                    + Settings.endl + Settings.SeparatorSingle + Settings.SeparatorSingle);
            Logger.err(Strings.targetFileName + "[" + newFileName1.length( ) + "] " + newFileName1 + Settings.endl
                    + Settings.SeparatorDouble + Settings.SeparatorDouble);
            Logger.err("MSG_012: " + Strings.unableToRenameFile + e);
        }
    }

    private static void displayProgress(double currentFile, long percentage, double totalFileCount, long renamedFileCount, long elapsedTime)
    {
        progressBarDialog.setProgress((int) percentage);
        progressBarDialog.setMessage(percentage + Strings.percentageFrom + (long) totalFileCount + Strings.filec
                + renamedFileCount + Strings.renamed + Strings.current + (long) currentFile + " - ["
                + TimeControl.getTotal(TimeControl.getElapsedNano(elapsedTime)) + "]");
        Logger.msg(Settings.Tab + Utils.addCustomLeadingZeros("03", percentage) + Strings.percentageFrom
                + (long) totalFileCount + Strings.filec + renamedFileCount + Strings.renamed + Strings.current
                + (long) currentFile);
    }

    private static long showProgress(double currentFile, double totalFileCount, long displayFactorControl, long renamedFileCount, int displaySteps, long elapsedTime)
    {
        long percentage = (long) (((double) currentFile / (double) totalFileCount) * 100.0);
        if ((percentage >= displayFactorControl) && (percentage <= displayFactorControl + displaySteps))
        {
            displayProgress(currentFile, percentage, totalFileCount, renamedFileCount, elapsedTime);
            displayFactorControl = displayFactorControl + displaySteps;
            return displayFactorControl;
        }
        if (percentage > displayFactorControl +
                displaySteps)
        {
            double delta = percentage - (displayFactorControl + displaySteps);
            double fac   = (displayFactorControl + displaySteps + delta) / 5.0;
            displayFactorControl = (int) (Math.floor(fac) * displayFactorControl) + displaySteps;
        }
        return displayFactorControl;
    }

    private static boolean isAscending(String orderType)
    {
        return (null == orderType) || (orderType.trim( ).equals(Settings.Empty))
                || (orderType.toLowerCase( ).equals(Settings.CompareAsc));
    }

    private static boolean sortTextFile(String textFileName, String sortMethodType)
    {
        if (FileOperations.isFile(textFileName))
        {
            try
            {
                progressBarDialog.setIndeterminate(true);
                progressBarDialog.setMessage(Strings.sortingFileList);
                // Second
                BufferedReader                 textFileBufferedReader = new BufferedReader(StreamManager.InputStreamReader(textFileName));
                String                         fileLine               = Settings.Empty;
                Map <Long, ArrayList <String>> textFileSortedHashList = new TreeMap <Long, ArrayList <String>>( );
                while ((fileLine = textFileBufferedReader.readLine( )) != null)
                {
                    Long               textFileLineSize      = FileOperations.file(fileLine).length( );
                    ArrayList <String> textFileLineArrayList = (ArrayList <String>) textFileSortedHashList.get(textFileLineSize);
                    if (null == textFileLineArrayList)
                    {
                        textFileLineArrayList = new ArrayList <String>( );
                    }
                    textFileLineArrayList.add(fileLine);
                    textFileSortedHashList.put(textFileLineSize, textFileLineArrayList);
                }
                textFileBufferedReader.close( );
                NavigableMap <Long, ArrayList <String>> textFileNavigableMap = null;
                if (isAscending(sortMethodType))
                {
                    textFileNavigableMap = ((TreeMap <Long, ArrayList <String>>) textFileSortedHashList);
                }
                else
                {
                    textFileNavigableMap = ((TreeMap <Long, ArrayList <String>>) textFileSortedHashList).descendingMap( );
                }
                Writer  textFileWriter = StreamManager.FileWriter(textFileName);
                boolean firstTime      = true;
                for (ArrayList <String> textFileArrayList:textFileNavigableMap.values( ))
                {
                    if (isAscending(sortMethodType))
                    {
                        Collections.sort(textFileArrayList);
                    }
                    else
                    {
                        Collections.sort(textFileArrayList, Collections.reverseOrder( ));
                    }
                    Iterator <String> textFileArrayListElement = textFileArrayList.iterator( );
                    while (textFileArrayListElement.hasNext( ))
                    {
                        if ( ! firstTime)
                        {
                            textFileWriter.write(Settings.EndLine);
                        }
                        firstTime = false;
                        textFileWriter.write((String) textFileArrayListElement.next( ));
                    }
                }
                textFileWriter.flush( );
                textFileWriter.close( );
            }
            catch (IOException e)
            {
                Logger.err("MSG_013: " + Strings.problemSortingTextFile + e);
                return false;
            }
            return true;
        }
        return false;
    }

    private static String[ ] checkIfListIsDirectory(String textFileList)
    {
        String             textFileType  = Strings.file;
        ArrayList <String> arrayFileList = new ArrayList <String>( );
        if (FileOperations.isDirectory(textFileList) &&
                ( ! FileOperations.isEmpty(textFileList)))
        {
            arrayFileList = ReportGenerator.generateFileList(FileOperations.file(textFileList).listFiles( ), Settings.Empty);
            textFileList = FileOperations.getDirectory(textFileList) + FileOperations.getFileName(textFileList)
                    + Settings.UnDupeKeeperTextFile;
            textFileType = Strings.directory;
            FileOperations.file(textFileList).deleteOnExit( );
        }
        else
        {
            if (FileOperations.isFile(textFileList) && ( ! FileOperations.isEmpty(textFileList)))
            {
                try
                {
                    // First
                    InputStreamReader textFileListInputStreamReader = StreamManager.InputStreamReader(textFileList);
                    BufferedReader    textFileListBufferedReader    = new BufferedReader(textFileListInputStreamReader);
                    String            textFileLine                  = Settings.Empty;
                    while ((textFileLine = textFileListBufferedReader.readLine( )) != null)
                    {
                        arrayFileList.add(textFileLine);
                    }
                    textFileListBufferedReader.close( );
                }
                catch (IOException e)
                {
                    Logger.err("MSG_014: " + Strings.cannotOpenTextFile + e);
                    textFileList = null;
                    arrayFileList = null;
                }
            }
            else
            {
                textFileList = null;
                arrayFileList = null;
            }
        }
        if ((null != textFileList) && (null != arrayFileList))
        {
            try
            {
                Writer            textFileListWriter    = StreamManager.FileWriter(textFileList);
                Iterator <String> arrayFileListIterator = arrayFileList.iterator( );
                boolean           firstTime             = true;
                while (arrayFileListIterator.hasNext( ))
                {
                    if ( ! firstTime)
                    {
                        textFileListWriter.write(Settings.EndLine);
                    }
                    firstTime = false;
                    textFileListWriter.write((String) arrayFileListIterator.next( ));
                }
                textFileListWriter.flush( );
                textFileListWriter.close( );
            }
            catch (IOException e)
            {
                Logger.err("MSG_015: " + Strings.couldNotWriteOrderedFileList + e);
                textFileList = null;
            }
        }
        String returnStringArray[] = {textFileList, textFileType
        };
        return returnStringArray;
    }

    public static void searchAndMarkDuplicatedFiles(String fileListToCompare, String ascendingOrDescendingMethod)
    {
        long processStartTime = TimeControl.getNano( );
        Logger.msg(Strings.undpueVersion + Settings.undupeVersion);
        Logger.msg(Strings.generatingFileListPleaseWait);
        progressBarDialog = new ProgressBarDialog(Strings.generatingFileList, Strings.pleaseWait);
        String fileOrDirectory[] = checkIfListIsDirectory(fileListToCompare);
        fileListToCompare = fileOrDirectory[0];
        progressBarDialog.setTitle(Strings.undupe + fileOrDirectory[1]);
        if (sortTextFile(fileListToCompare, ascendingOrDescendingMethod))
        {
            progressBarDialog.setIndeterminate(false);
            Logger.msg(Strings.startingComparison);
            long               displayFactorControl   = 0;
            int                i                      = 0;
            int                j                      = 0;
            long               currentFile            = 0;
            long               totalFileCount         = 0;
            long               renamedFileCount       = 0;
            boolean            stop                   = false;
            String             fileListToCompareLine1 = Settings.Empty;
            String             fileListToCompareLine2 = Settings.Empty;
            ArrayList <String> linesUnderComparison   = new ArrayList <String>( );
            try
            {
                totalFileCount = totalLines(fileListToCompare);
                progressBarDialog.setMessage("0" + Strings.percentageFrom + totalFileCount + Strings.files);
                Logger.msg(Settings.Tab + "000" + Strings.percentageFrom + totalFileCount + Strings.files);
                // Fourth
                InputStreamReader fileListToCompareInputStreamReader = StreamManager.InputStreamReader(fileListToCompare);
                BufferedReader    fileListToCompareBufferedReader    = new BufferedReader(fileListToCompareInputStreamReader);
                fileListToCompareLine1 = fileListToCompareBufferedReader.readLine( );
                while (( ! stop) && ((fileListToCompareLine2 = fileListToCompareBufferedReader.readLine( )) != null))
                {
                    currentFile++;
                    linesUnderComparison.add(fileListToCompareLine1);
                    while (( ! stop)
                            && ((FileOperations.file(fileListToCompareLine1).length( )) == (FileOperations.file(fileListToCompareLine2).length( ))))
                    {
                        linesUnderComparison.add(fileListToCompareLine2);
                        fileListToCompareLine1 = fileListToCompareLine2;
                        fileListToCompareLine2 = fileListToCompareBufferedReader.readLine( );
                        currentFile++;
                        if (null == fileListToCompareLine2)
                        {
                            stop = true;
                        }
                    }
                    i = 0;
                    while (i < linesUnderComparison.size( ))
                    {
                        j = i + 1;
                        while (j < linesUnderComparison.size( ))
                        {
                            if (compareBySize(Paths.get(linesUnderComparison.get(i)), Paths.get(linesUnderComparison.get(j))))
                            {
                                renameDuplicatedFile(Paths.get(linesUnderComparison.get(j)), Paths.get(linesUnderComparison.get(i)), renamedFileCount);
                                linesUnderComparison.remove(j);
                                renamedFileCount++;
                            }
                            else
                            {
                                j++;
                            }
                        }
                        i++;
                    }
                    linesUnderComparison.clear( );
                    fileListToCompareLine1 = fileListToCompareLine2;
                    displayFactorControl = showProgress(currentFile, totalFileCount, displayFactorControl, renamedFileCount, 1, processStartTime);
                }
                fileListToCompareInputStreamReader.close( );
                fileListToCompareBufferedReader.close( );
            }
            catch (IOException e)
            {
                Logger.err("MSG_016: " + Strings.problemDuringComparisonProcess + e);
            }
            progressBarDialog.setMessage("100" + Strings.percentageFrom + totalFileCount + Strings.filec
                    + renamedFileCount + Strings.renamed);
            Logger.msg(Settings.Tab + "100" + Strings.percentageFrom + totalFileCount + Strings.filec + renamedFileCount
                    + Strings.renamed);
            Logger.msg(Settings.endl + renamedFileCount + Strings.renamedAndMarked);
            Logger.msg(Strings.finishingExecution);
            Logger.msg(Strings.done + Settings.Dot);
            long totalProcessTime = TimeControl.getElapsedNano(processStartTime);
            Logger.msg(Settings.Tab + Strings.totalTime + TimeControl.getTotal(totalProcessTime));
            progressBarDialog.setProgress(100);
            progressBarDialog.setMessage(progressBarDialog.getMessage( ) + " - " + Strings.totalTime
                    + TimeControl.getTotal(totalProcessTime));
            progressBarDialog.setDismiss( );
            progressBarDialog.keep( );
        }
        else
        {
            Logger.err("MSG_017: " + Strings.unableToSortInputFile);
        }
    }

    /**
     * Compare binary files. Both files must be files (not directories) and exist.
     * 
     * @param file1
     *                  - first file
     * @param file2
     *                  - second file
     * 
     * @return boolean - true if files are binary equal
     * 
     * @throws IOException
     *                         - error in function
     */
    public static boolean isFileBinaryEqual(Path file1, Path file2)
    {
        try
        {
            int                 BUFFER_SIZE                = 65536;
            FileInputStream     file1stInputStream         = new FileInputStream(file1.toFile( ));
            FileInputStream     file2ndInputStream         = new FileInputStream(file2.toFile( ));
            BufferedInputStream file1stBufferedInputStream = new BufferedInputStream(file1stInputStream, BUFFER_SIZE);
            BufferedInputStream file2ndBufferedInputStream = new BufferedInputStream(file2ndInputStream, BUFFER_SIZE);
            int                 file1stByte;
            int                 file2ndByte;
            do
            {
                file1stByte = file1stBufferedInputStream.read( );
                file2ndByte = file2ndBufferedInputStream.read( );
                if (file1stByte != file2ndByte)
                {
                    file1stInputStream.close( );
                    file2ndInputStream.close( );
                    file1stBufferedInputStream.close( );
                    file2ndBufferedInputStream.close( );
                    return false;
                }
            }
            while ( ! ((file1stByte < 0) && (file2ndByte < 0)));
            file1stInputStream.close( );
            file2ndInputStream.close( );
            file1stBufferedInputStream.close( );
            file2ndBufferedInputStream.close( );
            return true;
        }
        catch (IOException e)
        {
            Logger.err("MSG_018: " + Strings.comparisonFailed + e);
        }
        return false;
    }
}
