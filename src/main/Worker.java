package main;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import settings.Settings;
import settings.Strings;
import tools.CheckSum;
import tools.DataBase;
import tools.FileQueue;
import tools.Logger;

/**
 * Worker class is responsible for checking a
 * <code>BlockingQueue&lt;FileQueue&gt;</code> and take actions related to
 * unduplicate already indexed files.
 * 
 * @author vliopard
 */
public class Worker implements
                   Runnable
{
    private long                           filesIncluded =0;
    private long                           filesReplaced =0;
    private final BlockingQueue<Integer>   stopSignal;
    private final BlockingQueue<FileQueue> transferQueue;
    private static HashMap<String,String>  hashMapTable  =new HashMap<String,String>();

    /**
     * Worker Constructor - Initializes a Worker object that starts to keep
     * files unduplicated.
     * 
     * @param fileQueue
     *            A <code>BlockingQueue&lt;FileQueue&gt;</code> containing all
     *            the file system notifications sent from <code>Monitor</code>
     *            class.
     * @param signalQueue
     *            A <code>BlockingQueue&lt;Integer&gt;</code> that will receive
     *            signals to shutdown gracefully.
     */
    Worker(BlockingQueue<FileQueue> fileQueue,
           BlockingQueue<Integer> signalQueue)
    {
        transferQueue=fileQueue;
        stopSignal=signalQueue;
    }

    /**
     * This is the runnable method that starts background processing of file
     * system contents.
     */
    public void run()
    {
        hashMapTable=DataBase.loadMap();
        msg(Strings.wkStartup);
        try
        {
            do
            {
                consume(transferQueue.take());
            }
            while(!stopSignal.contains(Settings.StopWorking));
        }
        catch(InterruptedException e)
        {
            log(Strings.wkProblemRunningWorker+
                e);
        }
        save();
        try
        {
            stopSignal.put(Settings.WorkerStopped);
        }
        catch(InterruptedException e)
        {
            err(Strings.wkErrorSendingShutdownMessage);
        }
        msg(Strings.wkWorkerShutdown);
    }

    /**
     * This method saves the hash map of included files for later use.
     */
    public synchronized void save()
    {
        synchronized(this)
        {
            DataBase.saveMap(hashMapTable);
        }
    }

    /**
     * This method clear the hash map database for starting a new fresh
     * unduplicate task.
     */
    public synchronized void clear()
    {
        synchronized(this)
        {
            DataBase.clear();
        }
    }

    /**
     * This method returns the size of the hash map database to inform how many
     * items are already worked.
     * 
     * @return Returns a <code>long</code> value with the size of the file hash
     *         database.
     */
    public synchronized long size()
    {
        synchronized(this)
        {
            return hashMapTable.size();
        }
    }

    /**
     * This method loads the hash map database from disk to restart later saved
     * unduplication tasks.
     */
    public synchronized void load()
    {
        synchronized(this)
        {
            hashMapTable=DataBase.loadMap();
        }
    }

    /**
     * This method will work on each <code>FileQueue</code> object to determine
     * if a file is new on directory of if it already exist for taking the right
     * action.
     * 
     * @param fileQueueObject
     *            A <code>FileQueue</code> object containing an file system
     *            action code and a path to the working file.
     */
    private void consume(Object fileQueueObject)
    {
        FileQueue fileQueue=(FileQueue)fileQueueObject;
        switch(fileQueue.getType())
        {
            case Settings.FileCreated:
                includeFileToHashTable(fileQueue.getPath());
            break;
            case Settings.FileModified:
            // TODO: Action to handle modified files
            break;
            case Settings.FileDeleted:
                replaceFileFromHashTable(fileQueue.getPath());
            break;
            case Settings.FileRenamed:
            // TODO: Action to handle renamed files
            break;
            default:
        }
    }

    /**
     * This method includes a new fresh file to the hash table database or
     * replaces the file content to a path that points to its identical file
     * already added to the hash table database.
     * 
     * @param fileName
     *            A <code>String</code> containing a file location path.
     */
    private void includeFileToHashTable(String fileName)
    {
        if(new File(fileName).isFile())
        {
            try
            {
                String cypherMethod=CheckSum.getChecksumElegant(fileName);
                if(!hashMapTable.containsKey(cypherMethod))
                {
                    filesIncluded++;
                    msg("["+
                        addLeadingZeros(filesIncluded)+
                        "]["+
                        addLeadingZeros(filesReplaced)+
                        "]\t["+
                        cypherMethod+
                        "]\t"+
                        Strings.wkIncluding+
                        fileName);
                    hashMapTable.put(cypherMethod,
                                     fileName);
                }
                else
                {
                    filesReplaced++;
                    msg("["+
                        addLeadingZeros(filesIncluded)+
                        "]["+
                        addLeadingZeros(filesReplaced)+
                        "]\t["+
                        cypherMethod+
                        "]\t"+
                        Strings.wkReplacing+
                        fileName);
                    File fileToRename=new File(fileName);
                    Writer outputFile=new BufferedWriter(new FileWriter(fileToRename));
                    outputFile.write(hashMapTable.get(cypherMethod)+
                                     Settings.UnDupeKeeperSignature+
                                     "["+
                                     cypherMethod+
                                     "]");
                    outputFile.close();
                    // CheckSum.waitFile(child);
                    // f2.renameTo(new File(child + ".(Dup3K33p)"));
                    Path fileNamePath=Paths.get(fileName);
                    java.nio.file.Files.move(fileNamePath,
                                             fileNamePath.resolveSibling(fileNamePath.getFileName()
                                                                                     .toString()+
                                                                         Settings.UnDupeKeeperExtension));
                }
            }
            catch(IOException e)
            {
                log(Strings.wkProblemIncludingNewFile+
                    e);
            }
        }
    }

    /**
     * This method removes an already included file in the hash table database
     * in case it is deleted from file system.
     * 
     * @param fileName
     *            A <code>String</code> containing a file location path.
     */
    private void replaceFileFromHashTable(String fileName)
    {
        if(hashMapTable.containsValue(fileName))
        {
            msg("["+
                addLeadingZeros(filesIncluded)+
                "]["+
                addLeadingZeros(filesReplaced)+
                "]\t"+
                Strings.wkRemoving+
                fileName);
            filesIncluded--;
            hashMapTable.values()
                        .remove(fileName);
        }
    }

    /**
     * This method adds leading zeros to a <code>String</code> representation of
     * a number to keep display organized in columns.
     * 
     * @param numberToFormat
     *            A <code>long</code> number that will receive leading zeros.
     * @return Returns an <code>String</code> representing a <code>long</code>
     *         number with leading zeros.
     */
    private String addLeadingZeros(long numberToFormat)
    {
        return String.format("%06d",
                             numberToFormat);
    }

    /**
     * This method displays a log message through the embedded log system.
     * 
     * @param logMessage
     *            A <code>String</code> containing the log message to display.
     */
    private static void log(String logMessage)
    {
        Logger.log(Thread.currentThread(),
                   logMessage,
                   Logger.WORKER);
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
