package main;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import settings.Settings;
import tools.FileUtils;
import tools.Logger;
import tools.ProgressBarDialog;
import tools.ReportGenerator;
import tools.TimeControl;

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
                // TODO: KEEP AS AN ERROR MESSAGE OR LOG MESSAGE?
                err("MSG_022: "+
                    // TODO: EXTERNALIZE STRING
                    "Cannot compare files: "+
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
                // TODO: EXTERNALIZE STRING
                err("OUTPUT ERROR: "+
                    strLn);
            }
        }
        catch(Exception e)
        {
            // TODO: EXTERNALIZE STRING
            err("MSG_023: "+
                "Process Runtime Error: "+
                e);
        }
        return false;
    }

    public static long totalLines(String textFileName)
    {
        long lineCount=0;
        try
        {
            FileInputStream textFileNameInputStream=new FileInputStream(textFileName);
            InputStreamReader textFileNameInputStreamReader=new InputStreamReader(textFileNameInputStream);
            BufferedReader textFileNameBufferedReader=new BufferedReader(textFileNameInputStreamReader);
            while((textFileNameBufferedReader.readLine())!=null)
            {
                lineCount++;
            }
            textFileNameInputStream.close();
            textFileNameInputStreamReader.close();
            textFileNameBufferedReader.close();
        }
        catch(IOException e)
        {
            // TODO: EXTERNALIZE STRING
            err("MSG_039"+
                "Problem while counting lines: "+
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

    private static void renameDuplicatedFile(String fileName1,
                                             String fileName2,
                                             long fileCounter)
    {
        String removeMarker="[_REMOVE_]_(Dup3K33p)_["+
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
        if(fileName1.lastIndexOf(".")>fileName1.lastIndexOf("\\"))
        {
            newFileName1=newFileName1+
                         FileUtils.getFileExtension(fileName1);
        }
        FileUtils.file(fileName1)
                 .renameTo(FileUtils.file(newFileName1));
    }

    private static void displayProgress(double currentFile,
                                        long percentage,
                                        double totalFileCount,
                                        long renamedFileCount)
    {
        progressBarDialog.setProgress((int)percentage);
        progressBarDialog.setMessage(percentage+
                                     "% from "+
                                     (long)totalFileCount+
                                     " files ("+
                                     renamedFileCount+
                                     ") renamed, current "+
                                     (long)currentFile);
        msg("\t"+
            percentage+
            "% from "+
            (long)totalFileCount+
            " files ("+
            renamedFileCount+
            ") renamed, current "+
            (long)currentFile);
    }

    private static long showProgress(double currentFile,
                                     double totalFileCount,
                                     long displayFactorControl,
                                     long renamedFileCount,
                                     int displaySteps)
    {
        long percentage=(long)(((double)currentFile/(double)totalFileCount)*100.0);
        if((percentage>=displayFactorControl)&&
           (percentage<=displayFactorControl+
                        displaySteps))
        {
            displayProgress(currentFile,
                            percentage,
                            totalFileCount,
                            renamedFileCount);
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
                progressBarDialog.setMessage("Sorting file list...");
                InputStream textFileInputStream=new FileInputStream(textFileName);
                BufferedReader textFileBufferedReader=new BufferedReader(new InputStreamReader(textFileInputStream));
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
                textFileInputStream.close();
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
                FileWriter textFileWriter=new FileWriter(textFileName);
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
                textFileWriter.close();
            }
            catch(IOException e)
            {
                // TODO: EXTERNALIZE STRING
                err("MSG_024: "+
                    "Problem Sorting Text File: "+
                    e);
                return false;
            }
            return true;
        }
        return false;
    }

    private static String[] checkIfListIsDirectory(String textFileList)
    {
        // TODO: EXTERNALIZE STRING
        String textFileType="File";
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
            // TODO: EXTERNALIZE STRING
            textFileType="Directory";
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
                    InputStream textFileListInputStream=new FileInputStream(textFileList);
                    InputStreamReader textFileListInputStreamReader=new InputStreamReader(textFileListInputStream);
                    BufferedReader textFileListBufferedReader=new BufferedReader(textFileListInputStreamReader);
                    String textFileLine=Settings.Empty;
                    while((textFileLine=textFileListBufferedReader.readLine())!=null)
                    {
                        arrayFileList.add(textFileLine);
                    }
                    textFileListInputStream.close();
                    textFileListInputStreamReader.close();
                    textFileListBufferedReader.close();
                }
                catch(IOException e)
                {
                    // TODO: EXTERNALIZE STRING
                    err("MSG_038: "+
                        "Can not open text file: "+
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
                FileWriter textFileListWriter=new FileWriter(textFileList);
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
                textFileListWriter.close();
            }
            catch(IOException e)
            {
                // TODO: EXTERNALIZE STRING
                err("MSG_025: "+
                    "Could not write ordered file list: "+
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
        String fileOrDirectory[]=checkIfListIsDirectory(fileListToCompare);
        fileListToCompare=fileOrDirectory[0];
        // TODO: EXTERNALIZE STRING
        progressBarDialog=new ProgressBarDialog("Undupe "+
                                                        fileOrDirectory[1],
                                                "Starting...");
        if(sortTextFile(fileListToCompare,
                        ascendingOrDescendingMethod))
        {
            progressBarDialog.setIndeterminate(false);
            msg("UnDupe version 12.08.17.01.50");
            msg("Starting comparison...");
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
                progressBarDialog.setMessage("0% from "+
                                             totalFileCount+
                                             " files");
                msg("\t0% from "+
                    totalFileCount+
                    " files");
                InputStream fileListToCompareInputStream=new FileInputStream(fileListToCompare);
                InputStreamReader fileListToCompareInputStreamReader=new InputStreamReader(fileListToCompareInputStream);
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
                                                      1);
                }
                fileListToCompareInputStream.close();
                fileListToCompareInputStreamReader.close();
                fileListToCompareBufferedReader.close();
            }
            catch(IOException e)
            {
                // TODO: EXTERNALIZE STRING
                err("MSG_026: "+
                    "Problem during comparison process: "+
                    e);
            }
            progressBarDialog.setMessage("100% from "+
                                         totalFileCount+
                                         " files ("+
                                         renamedFileCount+
                                         ") renamed");
            msg("\t100% from "+
                totalFileCount+
                " files ("+
                renamedFileCount+
                ") renamed");
            msg("\n"+
                renamedFileCount+
                " files renamed and marked to be deleted!");
            msg("Finishing execution...");
            msg("Done.");
            long totalProcessTime=TimeControl.getElapsedNano(processStartTime);
            msg("\tTotal time: "+
                TimeControl.getTotal(totalProcessTime));
            progressBarDialog.setProgress(100);
            progressBarDialog.setMessage(progressBarDialog.getMessage()+
                                         " - Total time: "+
                                         TimeControl.getTotal(totalProcessTime));
            progressBarDialog.setDismiss();
            progressBarDialog.keep();
        }
        else
        {
            // TODO: EXTERNALIZE STRING
            err("MSG_037: "
                +"ERROR: Unable to sort input file");
        }
    }

    /**
     * Compare binary files. Both files must be files (not directories) and
     * exist.
     * 
     * @param first
     *            - first file
     * @param second
     *            - second file
     * @return boolean - true if files are binery equal
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
            // TODO: EXTERNALIZE STRING
            err("MSG_040: "+
                "Comparison failed: "+
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
