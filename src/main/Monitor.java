package main;
import java.util.concurrent.BlockingQueue;
import settings.Settings;
import settings.Strings;
import tools.FileQueue;
import tools.Logger;
import net.contentobjects.jnotify.JNotify;
import net.contentobjects.jnotify.JNotifyException;
import net.contentobjects.jnotify.JNotifyListener;

/**
 * Monitor class is responsible for watching file system changes and notify
 * Worker class about that changes through a shared queue.
 * 
 * @author vliopard
 */
public class Monitor
{
    private FileQueue                      fileQueue;
    private final BlockingQueue<Integer>   stopSignal;
    private final BlockingQueue<FileQueue> transferQueue;

    /**
     * Monitor Constructor - Initialize a Monitor object for watching file
     * system changes.
     * 
     * @param directoryPath
     *            - A <code>String</code> containing the path to a file system
     *            directory to be observed.
     * @param transferData
     *            - A shared <code>BlockingQueue&lt;FileQueue&gt;</code> to
     *            write notifications about file changes.
     * @param stopCommand
     *            - A shared signaling queue to notify all threads the system is
     *            shutting down.
     * @param watchRecursive
     *            - A <code>boolean</code> flag that indicates whether a
     *            directory will be recursively observed.
     */
    Monitor(String directoryPath,
            BlockingQueue<FileQueue> transferData,
            BlockingQueue<Integer> stopCommand,
            boolean watchRecursive)
    {
        msg(Strings.mtMonitorStartup);
        transferQueue=transferData;
        stopSignal=stopCommand;
        int jNotifyMask=JNotify.FILE_CREATED|
                        JNotify.FILE_DELETED|
                        JNotify.FILE_MODIFIED|
                        JNotify.FILE_RENAMED;
        boolean watchSubtree=watchRecursive;
        int watchID;
        try
        {
            watchID=JNotify.addWatch(directoryPath,
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
        }
        catch(JNotifyException|InterruptedException e)
        {
            err("002: "+Strings.mtProblemCreatingMonitorObject+
                e);
        }
        msg(Strings.mtMonitorShutdown);
    }

    /**
     * Listener class is responsible for watching Operating System events
     * regarding to file access and add notifications to a
     * <code>BlockingQueue&lt;FileQueue&gt;</code>.
     * 
     * @author vliopard
     */
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
                log("003: "+Strings.mtProblemAddingToCreatingQueue+
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
                log("004: "+Strings.mtProblemAddingToModifyingQueue+
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
                log("005: "+Strings.mtProblemAddingToDeletingQueue+
                    e);
            }
        }

        public void fileRenamed(int wd,
                                String rootPath,
                                String oldName,
                                String newName)
        {
            // TODO: [Feed queue with renamed file information]
            // print("renamed "+rootPath+"\\"+oldName+" -> "+newName);
        }
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
                   Logger.MONITOR);
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
