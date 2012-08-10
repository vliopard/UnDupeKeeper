package tools;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

// TODO: JAVADOC
// TODO: REFACTORING NAMES
// TODO: EXTERNALIZE STRINGS
public class Comparison
{
    static ProgressBarDialog pbd;

    public static boolean isEqual(String file1,
                                  String file2)
    {
        File f1=new File(file1);
        File f2=new File(file2);
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
                // TODO: 02 EXTERNALIZE STRING
                err("");
            }
            if(f1.length()!=f2.length())
            {
                return false;
            }
            // TODO: EXTERNALIZE COMMAND
            return runSystemCommand("fc /B \""+
                                    file1+
                                    "\" \""+
                                    file2+
                                    "\"");
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
            proc.waitFor();
            BufferedReader inputStream=new BufferedReader(new InputStreamReader(proc.getInputStream()));
            BufferedReader errorStream=new BufferedReader(new InputStreamReader(proc.getErrorStream()));
            String strLn="";
            // boolean first=true;
            while((strLn=inputStream.readLine())!=null)
            {
                // TODO: use log system
                // if(first)
                // {
                // first=false;
                // msg("Output: ");
                // }
                if(strLn.trim()
                        .equals("FC: no differences encountered"))
                {
                    return true;
                }
                // msg(strLn);
            }
            // first=true;
            while((strLn=errorStream.readLine())!=null)
            {
                // TODO: use log system
                // if(first)
                // {
                // first=false;
                // err("Errors: ");
                // }
                // err("E: "+ strLn);
            }
        }
        catch(Exception e)
        {
            // TODO: EXTERNALIZE STRING
            err("");
        }
        return false;
    }

    public static int count(String filename) throws IOException
    {
        InputStream is=new BufferedInputStream(new FileInputStream(filename));
        try
        {
            byte[] c=new byte[1024];
            int count=0;
            int readChars=0;
            while((readChars=is.read(c))!=-1)
            {
                for(int i=0; i<readChars; ++i)
                {
                    if(c[i]=='\n')
                        ++count;
                }
            }
            return count;
        }
        finally
        {
            is.close();
        }
    }

    private static void markFile(String line1,
                                 String linecp,
                                 int counter)
    {
        String removCount="[_REMOVE_]_["+
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
        new File(line1).renameTo(new File(newname));
    }

    private static void display(double k,
                                long val,
                                double total,
                                long ct)
    {
        // TODO: ADD PROGRESS BAR
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

    private static void fileSort(String file,
                                 String type)
    {
        try
        {
            InputStream fis1=new FileInputStream(file);
            BufferedReader myINfile=new BufferedReader(new InputStreamReader(fis1));
            String line1="";
            Map<Long,ArrayList<String>> hashList=new TreeMap<Long,ArrayList<String>>();
            while((line1=myINfile.readLine())!=null)
            {
                Long sz=new File(line1).length();
                ArrayList<String> als=(ArrayList<String>)hashList.get(sz);
                if(null==als)
                {
                    als=new ArrayList<String>();
                }
                als.add(line1);
                hashList.put(sz,
                             als);
            }
            myINfile.close();
            boolean firstTime=true;
            FileWriter writer=new FileWriter(file);
            NavigableMap<Long,ArrayList<String>> nmap=null;
            if((null==type)||
               (type.equals("asc")))
            {
                nmap=((TreeMap<Long,ArrayList<String>>)hashList);
            }
            else
            {
                nmap=((TreeMap<Long,ArrayList<String>>)hashList).descendingMap();
            }
            for(ArrayList<String> val : nmap.values())
            {
                Iterator<String> itr=val.iterator();
                while(itr.hasNext())
                {
                    if(!firstTime)
                    {
                        writer.write('\r');
                        writer.write('\n');
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
            // TODO: HANDLE ERROR
            // TODO: EXTERNALIZE STRING
            err("");
        }
    }

    public static void compare(String file,
                               String method)
    {
        // TODO: ADD OPTION TO PROVIDE DIRECTORY PATH INSTEAD OF TEXT FILE
        long start=TimeControl.getNano();
        fileSort(file,
                 method);
        pbd=new ProgressBarDialog("Undupe File List",
                                  "Starting...");
        msg("UnDupe version 12.08.09.10.10");
        msg("Starting comparison...");
        int max=0;
        int i=0;
        int j=0;
        int k=0;
        int total=0;
        int counter=0;
        boolean stop=false;
        String line1="";
        String line2="";
        ArrayList<String> file_lines=new ArrayList<String>();
        try
        {
            total=count(file)+1;
            msg("\t0% from "+
                total+
                " files");
            InputStream fis1=new FileInputStream(file);
            BufferedReader myINfile=new BufferedReader(new InputStreamReader(fis1));
            line1=myINfile.readLine();
            while((!stop)&&
                  ((line2=myINfile.readLine())!=null))
            {
                k++;
                file_lines.add(line1);
                while((!stop)&&
                      ((new File(line1).length())==(new File(line2).length())))
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
            err("");
        }
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
        pbd.setMessage(pbd.getMessage()+
                       " - Total time: "+
                       TimeControl.getTotal(totTime));
        pbd.setDismiss();
        pbd.keep();
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
