package tools;
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

// TODO: JAVADOC
// TODO: METHOD AND VARIABLE NAMES REFACTORING
// TODO: EXTERNALIZE STRINGS
public class Comparison
{
    static ProgressBarDialog pbd;

    public static boolean isEqual(String file1,
                                  String file2)
    {
        File f1=FileUtils.file(file1);
        File f2=FileUtils.file(file2);
        if((f1.exists())&&
           (f2.exists())&&
           (f1.isFile())&&
           (f2.isFile()))
        {
            try
            {
                if(f1.getCanonicalPath()
                     .equals(f2.getCanonicalPath()))
                {
                    return true;
                }
            }
            catch(IOException e)
            {
                // TODO: EXTERNALIZE STRING
                err("MSG_022: "
                    +"aaa");
            }
            if(f1.length()!=f2.length())
            {
                return false;
            }
            return runSystemCommand(Settings.CompareCommand+
                                    Settings.Quote+
                                    file1+
                                    Settings.Quote+
                                    Settings.Blank+
                                    Settings.Quote+
                                    file2+
                                    Settings.Quote);
        }
        else
        {
            return false;
        }
    }

    public static boolean runSystemCommand(String command)
    {
        try
        {
            Process proc=Runtime.getRuntime()
                                .exec(command);
            // TODO: REMOVE THIS LOG MSG AFTER TESTING
            err("Waiting for "+
                command);
            proc.waitFor();
            BufferedReader inputStream=new BufferedReader(new InputStreamReader(proc.getInputStream()));
            BufferedReader errorStream=new BufferedReader(new InputStreamReader(proc.getErrorStream()));
            String strLn=Settings.Empty;
            while((strLn=inputStream.readLine())!=null)
            {
                if(strLn.trim()
                        .equals("FC: no differences encountered"))
                {
                    return true;
                }
            }
            while((strLn=errorStream.readLine())!=null)
            {
                // TODO: EXTERNALIZE STRING
                err("ERROR: "+
                    strLn);
            }
        }
        catch(Exception e)
        {
            // TODO: EXTERNALIZE STRING
            err("MSG_023: "+
                "bbb"+
                e);
        }
        return false;
    }

    public static long count(String filename) throws IOException
    {
        BufferedReader myINfile=new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
        long count=0;
        while((myINfile.readLine())!=null)
        {
            count++;
        }
        return count;
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

    private static void markFile(String line1,
                                 String linecp,
                                 int counter)
    {
        String removCount="[_REMOVE_]_(Dup3K33p)_["+
                          counter+
                          "]_";
        String replaced=FileUtils.getFilePath(linecp)+
                        "_("+
                        FileUtils.getFileName(linecp)+
                        FileUtils.getFileExtension(linecp)+
                        ")_";
        replaced=replaced.replace(':',
                                  '#');
        replaced=replaced.replace('\\',
                                  '-');
        String newname=FileUtils.getFilePath(line1)+
                       FileUtils.getFileName(line1)+
                       FileUtils.getFileExtension(line1)+
                       "@@"+
                       replaced+
                       removCount;
        if(line1.lastIndexOf(".")>line1.lastIndexOf("\\"))
        {
            newname=newname+
                    FileUtils.getFileExtension(line1);
        }
        FileUtils.file(line1)
                 .renameTo(FileUtils.file(newname));
    }

    private static void display(double k,
                                long val,
                                double total,
                                long ct)
    {
        pbd.setProgress((int)val);
        pbd.setMessage(val+
                       "% from "+
                       (long)total+
                       " files ("+
                       ct+
                       ") renamed, current "+
                       (long)k);
        msg("\t"+
            val+
            "% from "+
            (long)total+
            " files ("+
            ct+
            ") renamed, current "+
            (long)k);
    }

    private static int show_progress(double k,
                                     double total,
                                     int max,
                                     long ct)
    {
        long val=(long)(((double)k/(double)total)*100.0);
        if((val>=max)&&
           (val<=max+5))
        {
            display(k,
                    val,
                    total,
                    ct);
            max=max+5;
            return max;
        }
        if(val>max+5)
        {
            double dif=val-
                       (max+5);
            double fac=(max+5+dif)/5.0;
            max=(int)(Math.floor(fac)*max)+5;
        }
        return max;
    }

    private static boolean ascOrDesc(String type)
    {
        return (null==type)||
               (type.trim().equals(Settings.Empty))||
               (type.toLowerCase().equals(Settings.CompareAsc));
    }

    private static boolean fileSort(String file,
                                    String type)
    {
        if(FileUtils.isFile(file))
        {
            try
            {
                InputStream fis1=new FileInputStream(file);
                BufferedReader myINfile=new BufferedReader(new InputStreamReader(fis1));
                String line1=Settings.Empty;
                Map<Long,ArrayList<String>> hashList=new TreeMap<Long,ArrayList<String>>();
                while((line1=myINfile.readLine())!=null)
                {
                    Long sz=FileUtils.file(line1)
                                     .length();
                    ArrayList<String> als=(ArrayList<String>)hashList.get(sz);
                    if(null==als)
                    {
                        als=new ArrayList<String>();
                    }
                    als.add(line1);
                    hashList.put(sz,
                                 als);
                }
                fis1.close();
                myINfile.close();
                boolean firstTime=true;
                FileWriter writer=new FileWriter(file);
                NavigableMap<Long,ArrayList<String>> nmap=null;
                if(ascOrDesc(type))
                {
                    nmap=((TreeMap<Long,ArrayList<String>>)hashList);
                }
                else
                {
                    nmap=((TreeMap<Long,ArrayList<String>>)hashList).descendingMap();
                }
                for(ArrayList<String> val : nmap.values())
                {
                    if(ascOrDesc(type))
                    {
                        Collections.sort(val);
                    }
                    else
                    {
                        Collections.sort(val,
                                         Collections.reverseOrder());
                    }
                    Iterator<String> itr=val.iterator();
                    while(itr.hasNext())
                    {
                        if(!firstTime)
                        {
                            writer.write("\r\n");
                        }
                        firstTime=false;
                        String element=(String)itr.next();
                        writer.write(element);
                    }
                }
                writer.close();
            }
            catch(IOException e)
            {
                // TODO: EXTERNALIZE STRING
                err("MSG_024: "+
                    "ccc"+
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
        String ftype="File";
        ArrayList<String> arrayFileList=null;
        if(FileUtils.isDir(textFileList)&&
           (!FileUtils.isEmpty(textFileList)))
        {
            arrayFileList=ReportGenerator.generateFileList(FileUtils.file(textFileList)
                                                                    .listFiles(),
                                                           Settings.Empty);
            textFileList=FileUtils.getFilePath(textFileList)+
                         FileUtils.getFileName(textFileList)+
                         Settings.UnDupeKeeperTextFile;
        }
        else
        {
            if(FileUtils.isFile(textFileList)&&
               (!FileUtils.isEmpty(textFileList)))
            {
                try
                {
                    arrayFileList=new ArrayList<String>();
                    InputStream fis1=new FileInputStream(textFileList);
                    BufferedReader myINfile=new BufferedReader(new InputStreamReader(fis1));
                    String line3=Settings.Empty;
                    while(((line3=myINfile.readLine())!=null))
                    {
                        arrayFileList.add(line3);
                    }
                    myINfile.close();
                }
                catch(IOException e)
                {
                    // TODO: EXTERNALIZE STRING
                    err("MSG_038: "
                        +"canot open text file");
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
                FileWriter writ=new FileWriter(textFileList);
                Iterator<String> itr=arrayFileList.iterator();
                boolean fTime=true;
                while(itr.hasNext())
                {
                    if(!fTime)
                    {
                        writ.write(Settings.EndLine);
                    }
                    fTime=false;
                    String element=(String)itr.next();
                    writ.write(element);
                }
                writ.close();
                // TODO: EXTERNALIZE STRING
                ftype="Directory";
                // TODO: RESEARCH: _WAS WORKING, WHY IS NOT ANYMORE?
                FileUtils.file(textFileList)
                         .deleteOnExit();
            }
            catch(IOException e)
            {
                // TODO: EXTERNALIZE STRING
                err("MSG_025: "+
                    "Could not write ordered file list"+
                    e);
                textFileList=null;
            }
        }
        String vc[]=
        {
                textFileList,
                ftype
        };
        return vc;
    }

    public static void compare(String fileListToCompare,
                               String ascendingOrDescendingMethod)
    {
        // TODO: RESEARCH: _REFACTOR SPLITING FILE HANDLERS (REUSE)
        long start=TimeControl.getNano();
        String fileOrDirectory[]=checkIfListIsDirectory(fileListToCompare);
        fileListToCompare=fileOrDirectory[0];
        if(fileSort(fileListToCompare,
                    ascendingOrDescendingMethod))
        {
            // TODO: EXTERNALIZE STRING
            pbd=new ProgressBarDialog("Undupe "+
                                              fileOrDirectory[1],
                                      "Starting...");
            msg("UnDupe version 12.08.17.01.50");
            msg("Starting comparison...");
            int max=0;
            int i=0;
            int j=0;
            int k=0;
            long total=0;
            int counter=0;
            boolean stop=false;
            String line1=Settings.Empty;
            String line2=Settings.Empty;
            ArrayList<String> file_lines=new ArrayList<String>();
            try
            {
                total=count(fileListToCompare);
                pbd.setMessage("0% from "+
                               total+
                               " files");
                msg("\t0% from "+
                    total+
                    " files");
                InputStream fis1=new FileInputStream(fileListToCompare);
                BufferedReader myINfile=new BufferedReader(new InputStreamReader(fis1));
                line1=myINfile.readLine();
                while((!stop)&&
                      ((line2=myINfile.readLine())!=null))
                {
                    k++;
                    file_lines.add(line1);
                    while((!stop)&&
                          ((FileUtils.file(line1).length())==(FileUtils.file(line2).length())))
                    {
                        file_lines.add(line2);
                        line1=line2;
                        line2=myINfile.readLine();
                        k++;
                        if(null==line2)
                        {
                            stop=true;
                        }
                    }
                    i=0;
                    while(i<file_lines.size())
                    {
                        j=i+1;
                        while(j<file_lines.size())
                        {
                            if(isEqual(file_lines.get(i),
                                       file_lines.get(j)))
                            {
                                markFile(file_lines.get(j),
                                         file_lines.get(i),
                                         counter);
                                file_lines.remove(j);
                                counter++;
                            }
                            else
                            {
                                j++;
                            }
                        }
                        i++;
                    }
                    file_lines.clear();
                    line1=line2;
                    max=show_progress(k,
                                      total,
                                      max,
                                      counter);
                }
                myINfile.close();
            }
            catch(IOException e)
            {
                // TODO: EXTERNALIZE STRING
                err("MSG_026: "
                    +"yyy");
            }
            pbd.setMessage("100% from "+
                           total+
                           " files ("+
                           counter+
                           ") renamed");
            msg("\t100% from "+
                total+
                " files ("+
                counter+
                ") renamed");
            msg("\n"+
                counter+
                " files renamed and marked to be deleted!");
            msg("Finishing execution...");
            msg("Done.");
            long totTime=TimeControl.getElapsedNano(start);
            msg("\tTotal time: "+
                TimeControl.getTotal(totTime));
            pbd.setProgress(100);
            pbd.setMessage(pbd.getMessage()+
                           " - Total time: "+
                           TimeControl.getTotal(totTime));
            pbd.setDismiss();
            pbd.keep();
        }
        else
        {
            // TODO: EXTERNALIZE STRING
            err("MSG_037: "
                +"ERROR: Unable to sort input file");
        }
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
