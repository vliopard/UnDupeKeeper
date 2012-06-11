package main;
import java.util.concurrent.BlockingQueue;
import settings.Settings;
import settings.Strings;
import tools.FileQueue;
import tools.Logger;
import net.contentobjects.jnotify.JNotify;
import net.contentobjects.jnotify.JNotifyException;
import net.contentobjects.jnotify.JNotifyListener;

public class Monitor
{
    private FileQueue                      fileQueue;
    private final BlockingQueue<Integer>   stopSignal;
    private final BlockingQueue<FileQueue> transferQueue;

    Monitor(String directoryPath,
            BlockingQueue<FileQueue> transferData,
            BlockingQueue<Integer> stopCommand,
            boolean watchRecursive) throws JNotifyException,
                                   InterruptedException
    {
        msg(Strings.mtMonitorStartup);
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
            Thread.sleep(Settings.ThreadSleepTime);
        }
        while(!stopSignal.contains(Settings.StopWorking));
        if(!JNotify.removeWatch(watchID))
        {
            log(Strings.mtInvalidWatchID);
        }
        fileQueue=new FileQueue();
        fileQueue.set(Settings.WorkerStopSignal,
                      Settings.WorkerPrepareToExit);
        transferQueue.put(fileQueue);
        msg(Strings.mtMonitorShutdown);
    }

    class Listener implements
                  JNotifyListener
    {
        public void fileCreated(int wd,
                                String rootPath,
                                String name)
        {
            fileQueue=new FileQueue();
            fileQueue.set(Settings.FileCreated,
                          rootPath+
                                  "\\"+
                                  name);
            try
            {
                transferQueue.put(fileQueue);
            }
            catch(InterruptedException e)
            {
                log(Strings.mtProblemAddingToCreatingQueue+
                    e);
            }
        }

        public void fileModified(int wd,
                                 String rootPath,
                                 String name)
        {
            fileQueue=new FileQueue();
            fileQueue.set(Settings.FileModified,
                          rootPath+
                                  "\\"+
                                  name);
            try
            {
                transferQueue.put(fileQueue);
            }
            catch(InterruptedException e)
            {
                log(Strings.mtProblemAddingToModifyingQueue+
                    e);
            }
        }

        public void fileDeleted(int wd,
                                String rootPath,
                                String name)
        {
            fileQueue=new FileQueue();
            fileQueue.set(Settings.FileDeleted,
                          rootPath+
                                  "\\"+
                                  name);
            try
            {
                transferQueue.put(fileQueue);
            }
            catch(InterruptedException e)
            {
                log(Strings.mtProblemAddingToDeletingQueue+
                    e);
            }
        }

        public void fileRenamed(int wd,
                                String rootPath,
                                String oldName,
                                String newName)
        {
            // TODO: Rename
            // print("renamed "+rootPath+"\\"+oldName+" -> "+newName);
        }
    }

    private static void log(String logMessage)
    {
        Logger.log(Thread.currentThread(),
                   logMessage,
                   Logger.MONITOR);
    }

    private static void msg(String message)
    {
        Logger.msg(message);
    }
}
