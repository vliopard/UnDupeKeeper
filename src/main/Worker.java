package main;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import settings.Settings;
import settings.Strings;
import tools.DataBase;
import tools.FileQueue;
import tools.FileOperations;
import tools.Logger;
import tools.Utils;
import tools.UniqueFile;

/**
 * Worker class is responsible for checking a
 * <code>BlockingQueue&lt;FileQueue&gt;</code> and take actions related to
 * unduplicate already indexed files.
 * 
 * @author vliopard
 */
public class Worker implements Runnable
{
    private long                           filesIncluded =0;
    private long                           filesReplaced =0;

    private final BlockingQueue<Integer>   stopSignal;
    private final BlockingQueue<FileQueue> transferQueue;

    private HashMap<Path,String>           linkMapTable  =new HashMap<Path,String>();
    private HashMap<String,UniqueFile>     hashMapTable  =new HashMap<String,UniqueFile>();

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
    Worker(BlockingQueue<FileQueue> fileQueue, BlockingQueue<Integer> signalQueue)
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
        linkMapTable=DataBase.loadMap1();
        Logger.msg(Strings.wkStartup);
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
            Logger.err("MSG_016: " + Strings.wkProblemRunningWorker+ e);
        }
        save();
        try
        {
            stopSignal.put(Settings.WorkerStopped);
        }
        catch(InterruptedException e)
        {
            Logger.err("MSG_017: " + Strings.wkErrorSendingShutdownMessage);
        }
        Logger.msg(Strings.wkWorkerShutdown);
    }

    /**
     * This method saves the hash map of included files for later use.
     */
    public synchronized void save()
    {
        synchronized(this)
        {
            DataBase.saveMap(hashMapTable);
            DataBase.saveMap1(linkMapTable);
        }
    }

    /**
     * This method clear the hash map database for starting a new fresh unduplicate task.
     */
    public synchronized void clear()
    {
        synchronized(this)
        {
            DataBase.clear();
        }
    }

    /**
     * This method returns the size of the hash map database to inform how many items are already worked.
     * @return Returns a <code>long</code> value with the size of the file hash database.
     */
    public synchronized long size()
    {
        synchronized(this)
        {
            return hashMapTable.size();
        }
    }

    /**
     * This method loads the hash map database from disk to restart later saved unduplication tasks.
     */
    public synchronized void load()
    {
        synchronized(this)
        {
            hashMapTable=DataBase.loadMap();
            linkMapTable=DataBase.loadMap1();
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
                log(" includeFileToHashTable(" + fileQueue.getPath() + ")");
            break;

            case Settings.FileModified:
                log(" FILE MODIFIED (" + fileQueue.getPath().toString() + ")");
                if(!FileOperations.isLink(fileQueue.getPath()))
                {
                    if(FileOperations.isFile(fileQueue.getPath()))
                    {
                        UniqueFile newfile = new UniqueFile(fileQueue.getPath());
                        if (!hashMapTable.containsKey(newfile.getSha()))
                        {
                            log(" manageNewSha("+newfile.getFileStr()+")");
                            manageNewSha(newfile);
                        }
                        else
                        {
                            log(" manageExistingSha("+newfile.getFileStr()+")");
                            manageExistingSha(newfile);
                        }
                    }
                    else
                    {
                        log(" FILE MODIFIED IS NOT A FILE [" + fileQueue.getPath() + "]");
                    }
                }
                else
                {
                    log(" FILE MODIFIED IS NOT A LINK [" + fileQueue.getPath() + "]");
                }
            break;

            case Settings.FileDeleted:
                log(" DELETED FILE (" + fileQueue.getPath() + ")");
                ArrayList<String> shas = getShaFromUri(fileQueue.getPath());
                if(shas.size() > 0)
                {
                    for(int i=0; i<shas.size();i++)
                    {
                        UniqueFile file = hashMapTable.get(shas.get(i));
                        file.removeLinks();
                        file.clearUrl();
                    }
                }
                else
                {
                    if(FileOperations.isLink(fileQueue.getPath()))
                    {
                        // TODO: WHEN MOVE DO SOMETHING HERE
                        log(" HERE FILE IS A LINK ["+fileQueue.getPath().toString()+"]");
                    }
                    else
                    {
                        log(" HERE FILE IS NOT A LINK ["+fileQueue.getPath().toString()+"]");
                        // TODO: FOR NEVER REACHED - MUST GET NULL LINKS FROM HASH-MAP-TABLE TO REMOVE
                        for(int i=0; i<shas.size(); i++)
                        {
                            if(FileOperations.isFile(fileQueue.getPath()))
                            {
                                log(" DEL LINK ["+fileQueue.getPath().toString()+"]");
                                hashMapTable.get(shas.get(i)).delLink(fileQueue.getPath());
                            }
                            else
                            {
                                log(" UN LINK ["+fileQueue.getPath().toString()+"]");
                                hashMapTable.get(shas.get(i)).unLink(fileQueue.getPath());
                            }
                        }
                    }
                }
            break;

            case Settings.FileRenamed:
                log(" RENAMED FILE FROM ("+fileQueue.getPath().toString()+")");
                log(" RENAMED FILE TO   ("+fileQueue.getPath2().toString()+")");
                if(FileOperations.isLink(fileQueue.getPath()))
                {
                    // se moveu link, atualizar caminho do link na tabela
                    // se moveu o arquivo, atualizar path no sha do arquivo
                    // se moveu o arquivo, atualizar todos os links do arquivo
                }
                else
                {
                    ArrayList<String> shaz = getShaFromUri(fileQueue.getPath());
                    for(int i = 0; i < shaz.size(); i++)
                    {
                        hashMapTable.get(shaz.get(i)).setFilePath(fileQueue.getPath2());
                    }
                }
            break;
            default:
        }

        Iterator<Entry<String, UniqueFile>> it = hashMapTable.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, UniqueFile> pair = (Map.Entry<String, UniqueFile>)it.next();
            pair.getValue().show();
        }

        Iterator<Entry<Path, String>> lm = linkMapTable.entrySet().iterator();
        while (lm.hasNext()) {
            Map.Entry<Path, String> pair = (Map.Entry<Path, String>)lm.next();
            Logger.msg("["+pair.getKey()+"]["+pair.getValue()+"]");
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
    private void manageNewSha(UniqueFile fileName)
    {
        ArrayList<String> oldsha = getShaFromUri(fileName.getFilePath());
        if(oldsha.size() > 0)
        {
            for(int i = 0; i < oldsha.size(); i++)
            {
                hashMapTable.remove(oldsha.get(i));
            }
        }
        hashMapTable.put(fileName.getSha(), fileName);
        filesIncluded++;
        Logger.msg("[" + Utils.addLeadingZeros(filesIncluded) + "]["+ Utils.addLeadingZeros(filesReplaced) + "]" +
            Settings.Tab + "[" + fileName.getSha() + "]" + Settings.Tab + Strings.wkIncluding + fileName.getFileStr());
    }

    /**
     * This method removes an already included file in the hash table database
     * in case it is deleted from file system.
     * 
     * @param fileName
     *            A <code>String</code> containing a file location path.
     */
    private void manageExistingSha(UniqueFile newfile)
    {
        
        // SHA do URI j√° est√° cadastrado na base
        // Retorna o URI da base com o SHA de entrada
        UniqueFile currentfile = hashMapTable.get(newfile.getSha());
        if(null == currentfile)
        {
            newfile.makeLinks();
            hashMapTable.put(newfile.getSha(), newfile);
            linkMapTable.put(newfile.getFilePath(), newfile.getSha());
        }
        else
        {
            if(0==currentfile.getFilePath().compareTo(newfile.getFilePath()))
            {
                // Se o URI da base È igual ao URI de entrada
                // Mesmo SHA, Mesmo URI
                // Ent„o Arquivo n„o mudou
                log(" manageExistingSha({newfile}): {currentfile} == {newfile.fileUri}");
            }
            else
            {
                // Se o URI da base È diferente do URI de entrada
                // Verifica se a URI de entrada tem algum SHA na base
                ArrayList<String> oldsha = getShaFromUri(newfile.getFilePath());
                if (oldsha.size() > 0)
                {
                    for(int i = 0; i < oldsha.size(); i++)
                    {
                        if (oldsha.get(i).equals(newfile.getSha()))
                        {
                            log(" manageExistingSha("+newfile.toString()+"): A and B are the same, no changes");
                            // se o SHA da URI de entrada È igual ao SHA da URI da base
                            // ent„o o conte˙do do URI foi salvo mas n„o mudou o conte˙do
                        }
                        else
                        {
                            if (hashMapTable.get(oldsha.get(i)).getSha().equals(newfile.getSha()))
                            {
                                log(" manageExistingSha("+newfile.toString()+"): HANDLING THE SAME FILE");
                                log(" manageExistingSha("+newfile.toString()+"): SHA: "+oldsha.get(i));
                                log(" manageExistingSha("+newfile.toString()+"): FILE: "+hashMapTable.get(oldsha.get(i)).getFileStr());
                            }
                            else
                            {
                                filesReplaced++;
                                Logger.msg("[" + Utils.addLeadingZeros(filesIncluded) + "][" + Utils.addLeadingZeros(filesReplaced) + "]" +
                                    Settings.Tab + "[" + newfile.getSha() + "]" + Settings.Tab + Strings.wkReplacing + newfile.getFileStr());
                                hashMapTable.get(newfile.getSha()).addLink(newfile.getFileStr());
                            }
                            // se o SHA da URI de entrada È diferente do SHA da uri de base
                            // ent„o o conte˙do do URI foi salvo e mudou o conte˙do
                            // atualizar SHA do URI
                            Logger.msg("[" + Utils.addLeadingZeros(filesIncluded) + "][" + Utils.addLeadingZeros(filesReplaced) + "]" +
                                Settings.Tab + Strings.wkRemoving + newfile.getFileStr());
                                filesIncluded--;
                            hashMapTable.remove(oldsha.get(i));
                        }
                    }
                }
                else
                {
                    // URI de entrada È um arquivo duplicado
                    if (FileOperations.isFile(newfile.getFileStr()))
                    {
                        if (hashMapTable.get(newfile.getSha()).getFileStr().isEmpty())
                        {
                            hashMapTable.get(newfile.getSha()).setFilePath(newfile.getFilePath());
                            hashMapTable.get(newfile.getSha()).makeLinks();
                        }
                        else
                        {
                            hashMapTable.get(newfile.getSha()).addLink(newfile.getFileStr());
                            linkMapTable.put(newfile.getFilePath(), newfile.getSha());
                        }
                    }
                    else
                    {
                        log(" manageExistingSha("+newfile.toString()+"): FROM {dupelist[newfile.fileSha]} TO:");
                        log(" manageExistingSha("+newfile.toString()+"): dupelist[{newfile.fileSha}] = {newfile}");
                    }
                }
            }
        }
    }

    private ArrayList<String> getShaFromUri(Path uri)
    {
        ArrayList<String> shas = new ArrayList<String>();
        Iterator<Entry<String, UniqueFile>> it = hashMapTable.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, UniqueFile> pair = (Map.Entry<String, UniqueFile>)it.next();
            if(0==pair.getValue().getFilePath().compareTo(uri))
            {
                shas.add(pair.getKey());
            }
        }
        return shas;
    }

    /**
     * This method displays a log message through the embedded log system.
     * 
     * @param logMessage
     *            A <code>String</code> containing the log message to display.
     */
    private static void log(String logMessage)
    {
        Logger.log(Thread.currentThread(), logMessage, Logger.MONITOR);
    }
}
