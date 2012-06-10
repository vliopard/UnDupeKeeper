package com.OTDSHCo;
import java.util.concurrent.BlockingQueue;
import net.contentobjects.jnotify.JNotify;
import net.contentobjects.jnotify.JNotifyException;
import net.contentobjects.jnotify.JNotifyListener;

public class DiscMonitor
{
    private FileQueue                      fileQueue;
    private final BlockingQueue<Integer>   stopSignal;
    private final BlockingQueue<FileQueue> transferQueue;

    DiscMonitor(String directoryPath,
                BlockingQueue<FileQueue> transferData,
                BlockingQueue<Integer> stopCommand,
                boolean watchRecursive) throws JNotifyException,
                                       InterruptedException
    {
        msg("Monitor startup...");
        transferQueue=transferData;
        stopSignal=stopCommand;
        int jNotifyMask=JNotify.FILE_CREATED|
                        JNotify.FILE_DELETED|
                        JNotify.FILE_MODIFIED|
                        JNotify.FILE_RENAMED;
        boolean watchSubtree=watchRecursive;
        int watchID=JNotify.addWatch(directoryPath,
                                     jNotifyMask,
                                     watchSubtree,
                                     new Listener());
        do
        {
            Thread.sleep(5000);
        }
        while(!stopSignal.contains(1));
        if(!JNotify.removeWatch(watchID))
        {
            log("!Invalid Watci ID Specified");
        }
        fileQueue=new FileQueue();
        fileQueue.set(0,
                      "ExitSignal");
        transferQueue.put(fileQueue);
        msg("Monitor shutdown...");
    }

    class Listener implements
                  JNotifyListener
    {
        public void fileRenamed(int wd,
                                String rootPath,
                                String oldName,
                                String newName)
        {
            log(" RN - Adding To Queue...");
            // print("renamed "+rootPath+"\\"+oldName+" -> "+newName);
        }

        public void fileModified(int wd,
                                 String rootPath,
                                 String name)
        {
            log(" MD - Adding To Queue...");
            fileQueue=new FileQueue();
            fileQueue.set(2,
                          rootPath+
                                  "\\"+
                                  name);
            try
            {
                transferQueue.put(fileQueue);
            }
            catch(InterruptedException e)
            {
                log("!MD - Problem Adding To Queue: "+
                    e);
            }
            log(" MD - Added To Queue...");
        }

        public void fileDeleted(int wd,
                                String rootPath,
                                String name)
        {
            log(" DL - Adding To Queue...");
            fileQueue=new FileQueue();
            fileQueue.set(3,
                          rootPath+
                                  "\\"+
                                  name);
            try
            {
                transferQueue.put(fileQueue);
            }
            catch(InterruptedException e)
            {
                log("!DL - Problem Adding to Queue: "+
                    e);
            }
            log(" DL - Added To Queue...");
        }

        public void fileCreated(int wd,
                                String rootPath,
                                String name)
        {
            log(" CR - Adding To Queue...");
            fileQueue=new FileQueue();
            fileQueue.set(1,
                          rootPath+
                                  "\\"+
                                  name);
            try
            {
                transferQueue.put(fileQueue);
            }
            catch(InterruptedException e)
            {
                log("!CR - Problem Adding To Queue: "+
                    e);
            }
            log(" CR - Added To Queue...");
        }
    }

    private static void log(String logMessage)
    {
        Logger.log(Thread.currentThread(),
                   logMessage,
                   Logger.TOOLS_UTIL);
    }

    private static void msg(String message)
    {
        Logger.msg(message);
    }
}
