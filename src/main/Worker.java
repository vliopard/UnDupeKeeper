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

public class Worker implements
                   Runnable
{
    private long                           filesIncluded =0;
    private long                           filesReplaced =0;
    private final BlockingQueue<Integer>   stopSignal;
    private final BlockingQueue<FileQueue> transferQueue;
    private static HashMap<String,String>  hashMapTable  =new HashMap<String,String>();

    Worker(BlockingQueue<FileQueue> fileQueue,
           BlockingQueue<Integer> signalQueue)
    {
        transferQueue=fileQueue;
        stopSignal=signalQueue;
    }

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
        catch(InterruptedException ex)
        {
            log(Strings.wkProblemRunningWorker+
                ex);
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

    public synchronized void save()
    {
        synchronized(this)
        {
            DataBase.saveMap(hashMapTable);
        }
    }

    public synchronized void clear()
    {
        synchronized(this)
        {
            DataBase.clear();
        }
    }

    public synchronized void load()
    {
        synchronized(this)
        {
            hashMapTable=DataBase.loadMap();
        }
    }

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

    private String addLeadingZeros(long numberToFormat)
    {
        return String.format("%06d",
                             numberToFormat);
    }

    private static void log(String logMessage)
    {
        Logger.log(Thread.currentThread(),
                   logMessage,
                   Logger.WORKER);
    }

    private static void msg(String message)
    {
        Logger.msg(message);
    }

    private static void err(String errorMessage)
    {
        Logger.err(errorMessage);
    }
}
