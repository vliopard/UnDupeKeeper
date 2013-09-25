package main;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import settings.Settings;
import settings.Strings;
import tools.FileUtils;
import tools.Logger;
import tools.ProgressBarDialog;
import tools.ReportGenerator;
import tools.StreamManager;
import tools.TimeControl;
import tools.Utils;

// TODO: JAVADOC
// TODO: METHOD AND VARIABLE NAMES REFACTORING
public class Comparison
{
    static ProgressBarDialog progressBarDialog;

    public static boolean isBinaryIdentical(String binaryFilePath1,
                                            String binaryFilePath2)
    {
        File binaryFile1=FileUtils.file(binaryFilePath1);
        File binaryFile2=FileUtils.file(binaryFilePath2);
        if((binaryFile1.exists())&&
           (binaryFile1.isFile())&&
           (binaryFile2.exists())&&
           (binaryFile2.isFile()))
        {
            try
            {
                if(binaryFile1.getCanonicalPath()
                              .equals(binaryFile2.getCanonicalPath()))
                {
                    /*
                     * Two files cannot occupy the same disk space, so they are
                     * exactly the same file.
                     */
                    return true;
                }
            }
            catch(IOException e)
            {
                /*
                 * In case of path equality comparison failure, it is safe to
                 * return false instead of letting it continue next steps,
                 * otherwise, file could be erroneously marked as duplicate.
                 * This is not a desirable behavior. Better returning false
                 * instead.
                 */
                err("MSG_022: "+
                    Strings.cannotCompareFiles+
                    binaryFilePath1+
                    Settings.Blank+
                    binaryFilePath2);
                return false;
            }
            if(binaryFile1.length()!=binaryFile2.length())
            {
                /*
                 * If sizes are different there is no reason for continuing any
                 * other way of comparison, including the expensive binary one.
                 */
                return false;
            }
            /*
             * All checks passed. Files are eligible to be checked. Going
             * ahead!
             */
            return isFileBinaryEqual(binaryFilePath1,
                                     binaryFilePath2);
            // return runSystemCommand(Settings.DosCompareCommand+
            // Settings.Quote+
            // binaryFilePath1+
            // Settings.Quote+
            // Settings.Blank+
            // Settings.Quote+
            // binaryFilePath2+
            // Settings.Quote);
        }
        else
        {
            /*
             * There is a problem with some of those files. Maybe not exist,
             * maybe is not a file. Must not proceed.
             */
            return false;
        }
    }

    public static boolean runSystemCommand(String command)
    {
        try
        {
            Process proc=Runtime.getRuntime()
                                .exec(command);
            proc.waitFor();
            BufferedReader inputStream=new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String strLn=Settings.Empty;
            while((strLn=inputStream.readLine())!=null)
            {
                if(strLn.trim()
                        .equals(Settings.DosCompareCommandResult))
                {
                    return true;
                }
            }
            inputStream=new BufferedReader(new InputStreamReader(proc.getErrorStream()));
            while((strLn=inputStream.readLine())!=null)
            {
                err(Strings.outputError+
                    strLn);
            }
        }
        catch(Exception e)
        {
            err("MSG_023: "+
                Strings.processRuntimeError+
                e);
        }
        return false;
    }

    public static long totalLines(String textFileName)
    {
        long lineCount=0;
        try
        {
            // Third
            InputStreamReader textFileNameInputStreamReader=StreamManager.InputStreamReader(textFileName);
            BufferedReader textFileNameBufferedReader=new BufferedReader(textFileNameInputStreamReader);
            while((textFileNameBufferedReader.readLine())!=null)
            {
                lineCount++;
            }
            textFileNameInputStreamReader.close();
            textFileNameBufferedReader.close();
        }
        catch(IOException e)
        {
            err("MSG_039: "+
                Strings.problemCountingLines+
                e);
            return -1;
        }
        return lineCount;
        // DISABLED FAST MODE SINCE LAST LINE DOES NOT HAVE \n SCAPE CODE
        // InputStream is=new BufferedInputStream(new
        // FileInputStream(filename));
        // try
        // {
        // byte[] c=new byte[1024];
        // int count=0;
        // int readChars=0;
        // while((readChars=is.read(c))!=-1)
        // {
        // for(int i=0; i<readChars; ++i)
        // {
        // if(c[i]=='\n')
        // ++count;
        // }
        // }
        // return count;
        // }
        // finally
        // {
        // is.close();
        // }
    }

    private static String checkFileName(String fileName)
    {
        if(fileName.length()>255)
        {
            String path1=Settings.Empty;
            String name1=Settings.Empty;
            String path2=Settings.Empty;
            String name2=Settings.Empty;
            String tail=Settings.Empty;
            int slash=fileName.lastIndexOf(Settings.Slash)+1;
            int at=fileName.lastIndexOf("@@");
            int start=fileName.indexOf("-_(");
            int end=fileName.lastIndexOf(")_[_");
            if(slash>0)
            {
                path1=fileName.substring(0,
                                         slash);
                if(slash<at)
                {
                    name1=fileName.substring(slash,
                                             at);
                }
                else
                {
                    err("MSG_041: "+
                        Strings.fileNameContainsDoubleAt+
                        name1);
                    System.exit(-1);
                }
                if(at<start)
                {
                    path2=fileName.substring(at,
                                             start);
                }
                else
                {
                    err("MSG_042: "+
                        Strings.fileNameWrongDoubleAtPosition+
                        path2);
                    System.exit(-1);
                }
                if(start<end)
                {
                    name2=fileName.substring(start,
                                             end);
                }
                else
                {
                    err("MSG_043: "+
                        Strings.fileNameMismatchStartEnd+
                        name2);
                    System.exit(-1);
                }
                tail=fileName.substring(end);
            }
            int n1l=name1.length();
            int n2l=name2.length();
            int p2l=path2.length();
            do
            {
                if(n2l>1)
                {
                    n2l--;
                }
                else
                {
                    if(n1l>1)
                    {
                        n1l--;
                    }
                    else
                    {
                        if(p2l>2)
                        {
                            p2l--;
                            path2=path2.substring(0,
                                                  p2l);
                        }
                        else
                        {
                            err("MSG_044: "+
                                Strings.cannotShrinkFilename);
                            err("["+
                                fileName.length()+
                                "] "+
                                fileName);
                            break;
                        }
                    }
                }
                fileName=path1+
                         name1.substring(0,
                                         n1l)+
                         path2+
                         name2.substring(0,
                                         n2l)+
                         tail;
            }
            while(fileName.length()>255);
        }
        return fileName;
    }

    private static void renameDuplicatedFile(String fileName1,
                                             String fileName2,
                                             long fileCounter)
    {
        if(fileName1.contains(Settings.UnDupeKeeperNoRename))
        {
            return;
        }
        String removeMarker=Settings.UnDupeKeeperMarker+
                            "_["+
                            fileCounter+
                            "]_";
        String originalSourceFileName=FileUtils.getFilePath(fileName2)+
                                      "_("+
                                      FileUtils.getFileName(fileName2)+
                                      FileUtils.getFileExtension(fileName2)+
                                      ")_";
        originalSourceFileName=originalSourceFileName.replace(':',
                                                              '#');
        originalSourceFileName=originalSourceFileName.replace('\\',
                                                              '-');
        String newFileName1=FileUtils.getFilePath(fileName1)+
                            FileUtils.getFileName(fileName1)+
                            FileUtils.getFileExtension(fileName1)+
                            "@@"+
                            originalSourceFileName+
                            removeMarker;
        if(fileName1.lastIndexOf(Settings.Dot)>fileName1.lastIndexOf(Settings.Slash))
        {
            newFileName1=newFileName1+
                         FileUtils.getFileExtension(fileName1);
        }
        // FileUtils.file(fileName1).renameTo(FileUtils.file(newFileName1));
        try
        {
            newFileName1=checkFileName(newFileName1);
            java.nio.file.Files.move(Paths.get(fileName1),
                                     Paths.get(newFileName1));
        }
        catch(IOException e)
        {
            err(Settings.Separator+
                Settings.Separator);
            err(Strings.sourceFileName+
                "["+
                fileName1.length()+
                "] "+
                fileName1+
                Settings.endl+
                Settings.SeparatorSingle+
                Settings.SeparatorSingle);
            err(Strings.targetFileName+
                "["+
                newFileName1.length()+
                "] "+
                newFileName1+
                Settings.endl+
                Settings.SeparatorDouble+
                Settings.SeparatorDouble);
            err("MSG_045: "+
                Strings.unableToRenameFile+
                e);
        }
    }

    private static void displayProgress(double currentFile,
                                        long percentage,
                                        double totalFileCount,
                                        long renamedFileCount,
                                        long elapsedTime)
    {
        progressBarDialog.setProgress((int)percentage);
        progressBarDialog.setMessage(percentage+
                                     Strings.percentageFrom+
                                     (long)totalFileCount+
                                     Strings.filec+
                                     renamedFileCount+
                                     Strings.renamed+
                                     Strings.current+
                                     (long)currentFile+
                                     " - ["+
                                     TimeControl.getTotal(TimeControl.getElapsedNano(elapsedTime))+
                                     "]");
        msg(Settings.Tab+
            Utils.addCustomLeadingZeros("03",
                                        percentage)+
            Strings.percentageFrom+
            (long)totalFileCount+
            Strings.filec+
            renamedFileCount+
            Strings.renamed+
            Strings.current+
            (long)currentFile);
    }

    private static long showProgress(double currentFile,
                                     double totalFileCount,
                                     long displayFactorControl,
                                     long renamedFileCount,
                                     int displaySteps,
                                     long elapsedTime)
    {
        long percentage=(long)(((double)currentFile/(double)totalFileCount)*100.0);
        if((percentage>=displayFactorControl)&&
           (percentage<=displayFactorControl+
                        displaySteps))
        {
            displayProgress(currentFile,
                            percentage,
                            totalFileCount,
                            renamedFileCount,
                            elapsedTime);
            displayFactorControl=displayFactorControl+
                                 displaySteps;
            return displayFactorControl;
        }
        if(percentage>displayFactorControl+
                      displaySteps)
        {
            double delta=percentage-
                         (displayFactorControl+displaySteps);
            double fac=(displayFactorControl+
                        displaySteps+delta)/5.0;
            displayFactorControl=(int)(Math.floor(fac)*displayFactorControl)+
                                 displaySteps;
        }
        return displayFactorControl;
    }

    private static boolean isAscending(String orderType)
    {
        return (null==orderType)||
               (orderType.trim().equals(Settings.Empty))||
               (orderType.toLowerCase().equals(Settings.CompareAsc));
    }

    private static boolean sortTextFile(String textFileName,
                                        String sortMethodType)
    {
        if(FileUtils.isFile(textFileName))
        {
            try
            {
                progressBarDialog.setIndeterminate(true);
                progressBarDialog.setMessage(Strings.sortingFileList);
                // Second
                BufferedReader textFileBufferedReader=new BufferedReader(StreamManager.InputStreamReader(textFileName));
                String fileLine=Settings.Empty;
                Map<Long,ArrayList<String>> textFileSortedHashList=new TreeMap<Long,ArrayList<String>>();
                while((fileLine=textFileBufferedReader.readLine())!=null)
                {
                    Long textFileLineSize=FileUtils.file(fileLine)
                                                   .length();
                    ArrayList<String> textFileLineArrayList=(ArrayList<String>)textFileSortedHashList.get(textFileLineSize);
                    if(null==textFileLineArrayList)
                    {
                        textFileLineArrayList=new ArrayList<String>();
                    }
                    textFileLineArrayList.add(fileLine);
                    textFileSortedHashList.put(textFileLineSize,
                                               textFileLineArrayList);
                }
                textFileBufferedReader.close();
                NavigableMap<Long,ArrayList<String>> textFileNavigableMap=null;
                if(isAscending(sortMethodType))
                {
                    textFileNavigableMap=((TreeMap<Long,ArrayList<String>>)textFileSortedHashList);
                }
                else
                {
                    textFileNavigableMap=((TreeMap<Long,ArrayList<String>>)textFileSortedHashList).descendingMap();
                }
                Writer textFileWriter=StreamManager.FileWriter(textFileName);
                boolean firstTime=true;
                for(ArrayList<String> textFileArrayList : textFileNavigableMap.values())
                {
                    if(isAscending(sortMethodType))
                    {
                        Collections.sort(textFileArrayList);
                    }
                    else
                    {
                        Collections.sort(textFileArrayList,
                                         Collections.reverseOrder());
                    }
                    Iterator<String> textFileArrayListElement=textFileArrayList.iterator();
                    while(textFileArrayListElement.hasNext())
                    {
                        if(!firstTime)
                        {
                            textFileWriter.write(Settings.EndLine);
                        }
                        firstTime=false;
                        textFileWriter.write((String)textFileArrayListElement.next());
                    }
                }
                textFileWriter.flush();
                textFileWriter.close();
            }
            catch(IOException e)
            {
                err("MSG_024: "+
                    Strings.problemSortingTextFile+
                    e);
                return false;
            }
            return true;
        }
        return false;
    }

    private static String[] checkIfListIsDirectory(String textFileList)
    {
        String textFileType=Strings.file;
        ArrayList<String> arrayFileList=new ArrayList<String>();
        if(FileUtils.isDir(textFileList)&&
           (!FileUtils.isEmpty(textFileList)))
        {
            arrayFileList=ReportGenerator.generateFileList(FileUtils.file(textFileList)
                                                                    .listFiles(),
                                                           Settings.Empty);
            textFileList=FileUtils.getFilePath(textFileList)+
                         FileUtils.getFileName(textFileList)+
                         Settings.UnDupeKeeperTextFile;
            textFileType=Strings.directory;
            FileUtils.file(textFileList)
                     .deleteOnExit();
        }
        else
        {
            if(FileUtils.isFile(textFileList)&&
               (!FileUtils.isEmpty(textFileList)))
            {
                try
                {
                    // First
                    InputStreamReader textFileListInputStreamReader=StreamManager.InputStreamReader(textFileList);
                    BufferedReader textFileListBufferedReader=new BufferedReader(textFileListInputStreamReader);
                    String textFileLine=Settings.Empty;
                    while((textFileLine=textFileListBufferedReader.readLine())!=null)
                    {
                        arrayFileList.add(textFileLine);
                    }
                    textFileListBufferedReader.close();
                }
                catch(IOException e)
                {
                    err("MSG_038: "+
                        Strings.cannotOpenTextFile+
                        e);
                    textFileList=null;
                    arrayFileList=null;
                }
            }
            else
            {
                textFileList=null;
                arrayFileList=null;
            }
        }
        if((null!=textFileList)&&
           (null!=arrayFileList))
        {
            try
            {
                Writer textFileListWriter=StreamManager.FileWriter(textFileList);
                Iterator<String> arrayFileListIterator=arrayFileList.iterator();
                boolean firstTime=true;
                while(arrayFileListIterator.hasNext())
                {
                    if(!firstTime)
                    {
                        textFileListWriter.write(Settings.EndLine);
                    }
                    firstTime=false;
                    textFileListWriter.write((String)arrayFileListIterator.next());
                }
                textFileListWriter.flush();
                textFileListWriter.close();
            }
            catch(IOException e)
            {
                err("MSG_025: "+
                    Strings.couldNotWriteOrderedFileList+
                    e);
                textFileList=null;
            }
        }
        String returnStringArray[]=
        {
                textFileList,
                textFileType
        };
        return returnStringArray;
    }

    public static void searchAndMarkDuplicatedFiles(String fileListToCompare,
                                                    String ascendingOrDescendingMethod)
    {
        long processStartTime=TimeControl.getNano();
        msg(Strings.undpueVersion+
            Settings.undupeVersion);
        msg(Strings.generatingFileListPleaseWait);
        progressBarDialog=new ProgressBarDialog(Strings.generatingFileList,
                                                Strings.pleaseWait);
        String fileOrDirectory[]=checkIfListIsDirectory(fileListToCompare);
        fileListToCompare=fileOrDirectory[0];
        progressBarDialog.setTitle(Strings.undupe+
                                   fileOrDirectory[1]);
        if(sortTextFile(fileListToCompare,
                        ascendingOrDescendingMethod))
        {
            progressBarDialog.setIndeterminate(false);
            msg(Strings.startingComparison);
            long displayFactorControl=0;
            int i=0;
            int j=0;
            long currentFile=0;
            long totalFileCount=0;
            long renamedFileCount=0;
            boolean stop=false;
            String fileListToCompareLine1=Settings.Empty;
            String fileListToCompareLine2=Settings.Empty;
            ArrayList<String> linesUnderComparison=new ArrayList<String>();
            try
            {
                totalFileCount=totalLines(fileListToCompare);
                progressBarDialog.setMessage("0"+
                                             Strings.percentageFrom+
                                             totalFileCount+
                                             Strings.files);
                msg(Settings.Tab+
                    "000"+
                    Strings.percentageFrom+
                    totalFileCount+
                    Strings.files);
                // Fourth
                InputStreamReader fileListToCompareInputStreamReader=StreamManager.InputStreamReader(fileListToCompare);
                BufferedReader fileListToCompareBufferedReader=new BufferedReader(fileListToCompareInputStreamReader);
                fileListToCompareLine1=fileListToCompareBufferedReader.readLine();
                while((!stop)&&
                      ((fileListToCompareLine2=fileListToCompareBufferedReader.readLine())!=null))
                {
                    currentFile++;
                    linesUnderComparison.add(fileListToCompareLine1);
                    while((!stop)&&
                          ((FileUtils.file(fileListToCompareLine1).length())==(FileUtils.file(fileListToCompareLine2).length())))
                    {
                        linesUnderComparison.add(fileListToCompareLine2);
                        fileListToCompareLine1=fileListToCompareLine2;
                        fileListToCompareLine2=fileListToCompareBufferedReader.readLine();
                        currentFile++;
                        if(null==fileListToCompareLine2)
                        {
                            stop=true;
                        }
                    }
                    i=0;
                    while(i<linesUnderComparison.size())
                    {
                        j=i+1;
                        while(j<linesUnderComparison.size())
                        {
                            if(isBinaryIdentical(linesUnderComparison.get(i),
                                                 linesUnderComparison.get(j)))
                            {
                                renameDuplicatedFile(linesUnderComparison.get(j),
                                                     linesUnderComparison.get(i),
                                                     renamedFileCount);
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
                    linesUnderComparison.clear();
                    fileListToCompareLine1=fileListToCompareLine2;
                    displayFactorControl=showProgress(currentFile,
                                                      totalFileCount,
                                                      displayFactorControl,
                                                      renamedFileCount,
                                                      1,
                                                      processStartTime);
                }
                fileListToCompareInputStreamReader.close();
                fileListToCompareBufferedReader.close();
            }
            catch(IOException e)
            {
                err("MSG_026: "+
                    Strings.problemDuringComparisonProcess+
                    e);
            }
            progressBarDialog.setMessage("100"+
                                         Strings.percentageFrom+
                                         totalFileCount+
                                         Strings.filec+
                                         renamedFileCount+
                                         Strings.renamed);
            msg(Settings.Tab+
                "100"+
                Strings.percentageFrom+
                totalFileCount+
                Strings.filec+
                renamedFileCount+
                Strings.renamed);
            msg(Settings.endl+
                renamedFileCount+
                Strings.renamedAndMarked);
            msg(Strings.finishingExecution);
            msg(Strings.done+
                Settings.Dot);
            long totalProcessTime=TimeControl.getElapsedNano(processStartTime);
            msg(Settings.Tab+
                Strings.totalTime+
                TimeControl.getTotal(totalProcessTime));
            progressBarDialog.setProgress(100);
            progressBarDialog.setMessage(progressBarDialog.getMessage()+
                                         " - "+
                                         Strings.totalTime+
                                         TimeControl.getTotal(totalProcessTime));
            progressBarDialog.setDismiss();
            progressBarDialog.keep();
        }
        else
        {
            err("MSG_037: "+
                Strings.unableToSortInputFile);
        }
    }

    /**
     * Compare binary files. Both files must be files (not directories) and
     * exist.
     * 
     * @param file1
     *            - first file
     * @param file2
     *            - second file
     * @return boolean - true if files are binary equal
     * @throws IOException
     *             - error in function
     */
    public static boolean isFileBinaryEqual(String file1,
                                            String file2)
    {
        try
        {
            int BUFFER_SIZE=65536;
            File file1st=new File(file1);
            File file2nd=new File(file2);
            FileInputStream file1stInputStream=new FileInputStream(file1st);
            FileInputStream file2ndInputStream=new FileInputStream(file2nd);
            BufferedInputStream file1stBufferedInputStream=new BufferedInputStream(file1stInputStream,
                                                                                   BUFFER_SIZE);
            BufferedInputStream file2ndBufferedInputStream=new BufferedInputStream(file2ndInputStream,
                                                                                   BUFFER_SIZE);
            int file1stByte;
            int file2ndByte;
            do
            {
                file1stByte=file1stBufferedInputStream.read();
                file2ndByte=file2ndBufferedInputStream.read();
                if(file1stByte!=file2ndByte)
                {
                    file1stInputStream.close();
                    file2ndInputStream.close();
                    file1stBufferedInputStream.close();
                    file2ndBufferedInputStream.close();
                    return false;
                }
            }
            while(!((file1stByte<0)&&(file2ndByte<0)));
            file1stInputStream.close();
            file2ndInputStream.close();
            file1stBufferedInputStream.close();
            file2ndBufferedInputStream.close();
            return true;
        }
        catch(IOException e)
        {
            err("MSG_040: "+
                Strings.comparisonFailed+
                e);
        }
        return false;
    }

    /**
     * This method displays a message through the embedded log system.
     * 
     * @param message
     *            A <code>String</code> containing the message to display.
     */
    private static void msg(String message)
    {
        Logger.msg(message);
    }

    /**
     * This method displays an error message through the embedded log system.
     * 
     * @param errorMessage
     *            A <code>String</code> containing the error message to display.
     */
    private static void err(String errorMessage)
    {
        Logger.err(errorMessage);
    }
}
