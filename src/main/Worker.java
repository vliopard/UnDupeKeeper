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
import tools.CheckSum;
import tools.DataBase;
import tools.FileQueue;
import tools.FileOperations;
import tools.Logger;
import tools.Storage;
import tools.Utils;
import tools.UniqueFile;

/**
 * Worker class is responsible for checking a <code>BlockingQueue&lt;FileQueue&gt;</code> and take actions related to
 * unduplicate already indexed files.
 * 
 * @author vliopard
 */
public class Worker implements Runnable
{
    private long savedb        = 0;
    private long filesIncluded = 0;
    private long filesReplaced = 0;

    private final BlockingQueue <Integer>   stopSignal;
    private final BlockingQueue <FileQueue> transferQueue;

    private HashMap <Storage, String>    linkMapTable = new HashMap <Storage, String>( );
    private HashMap <String, UniqueFile> hashMapTable = new HashMap <String, UniqueFile>( );

    /**
     * Worker Constructor - Initializes a Worker object that starts to keep files unduplicated.
     * 
     * @param fileQueue
     *                        A <code>BlockingQueue&lt;FileQueue&gt;</code> containing all the file system notifications
     *                        sent from <code>Monitor</code> class.
     * @param signalQueue
     *                        A <code>BlockingQueue&lt;Integer&gt;</code> that will receive signals to shutdown
     *                        gracefully.
     */
    Worker(BlockingQueue <FileQueue> fileQueue, BlockingQueue <Integer> signalQueue)
    {
        transferQueue = fileQueue;
        stopSignal = signalQueue;
    }

    /**
     * This is the runnable method that starts background processing of file system contents.
     */
    public void run( )
    {
        hashMapTable = DataBase.loadMap( );
        linkMapTable = DataBase.loadMap1( );
        Logger.msg(Strings.wkStartup);
        try
        {
            do
            {
                consume(transferQueue.take( ));
            }
            while ( !stopSignal.contains(Settings.StopWorking));
        }
        catch (InterruptedException e)
        {
            Logger.err("MSG_016: " + Strings.wkProblemRunningWorker + e);
        }
        save( );
        try
        {
            stopSignal.put(Settings.WorkerStopped);
        }
        catch (InterruptedException e)
        {
            Logger.err("MSG_017: " + Strings.wkErrorSendingShutdownMessage);
        }
        Logger.msg(Strings.wkWorkerShutdown);
    }

    /**
     * This method saves the hash map of included files for later use.
     */
    public synchronized void save( )
    {
        synchronized (this)
        {
            DataBase.saveMap(hashMapTable);
            DataBase.saveMap1(linkMapTable);
        }
    }

    /**
     * This method clear the hash map database for starting a new fresh unduplicate task.
     */
    public synchronized void clear( )
    {
        synchronized (this)
        {
            DataBase.clear( );
        }
    }

    /**
     * This method returns the size of the hash map database to inform how many items are already worked.
     * 
     * @return Returns a <code>long</code> value with the size of the file hash database.
     */
    public synchronized long size( )
    {
        synchronized (this)
        {
            return hashMapTable.size( );
        }
    }

    /**
     * This method loads the hash map database from disk to restart later saved unduplication tasks.
     */
    public synchronized void load( )
    {
        synchronized (this)
        {
            hashMapTable = DataBase.loadMap( );
            linkMapTable = DataBase.loadMap1( );
        }
    }

    /**
     * This method will work on each <code>FileQueue</code> object to determine if a file is new on directory of if it
     * already exist for taking the right action.
     * 
     * @param fileQueueObject
     *                            A <code>FileQueue</code> object containing an file system action code and a path to
     *                            the working file.
     */
    private void consume(Object fileQueueObject)
    {
        savedb++;
        FileQueue fileQueue = (FileQueue) fileQueueObject;
        switch (fileQueue.getType( ))
        {
            case Settings.FileCreated:
                log(" case Settings.FileCreated: [" + fileQueue.getStorage( ) + "]");
                if (FileOperations.isLink(fileQueue.getStorage( )))
                {
                    String filesha = linkMapTable.get(fileQueue.getStorage( ));
                    log(" case Settings.FileCreated: if (FileOperations.isLink(" + fileQueue.getStorage( )
                            + ")) [ TRUE ]");
                    if (null == filesha)
                    {
                        log(" case Settings.FileCreated: if(null == filesha) TRUE");
                        String fileSha = CheckSum.getChecksumElegant(fileQueue.getStorage( ));
                        hashMapTable.get(fileSha).includeLink(fileQueue.getStorage( ));
                        linkMapTable.put(fileQueue.getStorage( ), fileSha);
                    }
                    else
                    {
                        log(" case Settings.FileCreated: if(null==filesha) FALSE { " + filesha + " } ]");
                    }
                }
                else
                {
                    log(" case Settings.FileCreated: if(FileOperations.isLink(" + fileQueue.getStorage( ) + ")) FALSE");
                }
            break;

            case Settings.FileModified:
                log(" case Settings.FileModified: [" + fileQueue.getStorage( ) + "]");
                if ( !FileOperations.isLink(fileQueue.getStorage( )))
                {
                    if (FileOperations.isFile(fileQueue.getStorage( )))
                    {
                        UniqueFile newfile = new UniqueFile(fileQueue.getStorage( ));
                        String     linksha = linkMapTable.get(newfile.getStorage( ));
                        log(" if (" + linksha + " != " + newfile.getSha( ) + ")");
                        if (null != linksha && linksha != newfile.getSha( ))
                        {
                            linkMapTable.remove(newfile.getStorage( ));
                            UniqueFile oldfile = hashMapTable.get(linksha);
                            oldfile.unLink(newfile.getPath( ));
                        }
                        else
                        {
                            log(" if(linksha != newfile.getSha( )) FALSE");
                        }

                        if ( !hashMapTable.containsKey(newfile.getSha( )))
                        {
                            log(" case Settings.FileModified: manageNewSha(" + newfile.getStorage( ) + ")");
                            manageNewSha(newfile);
                        }
                        else
                        {
                            log(" case Settings.FileModified: manageExistingSha(" + newfile.getStorage( ) + ")");
                            manageExistingSha(newfile);
                        }
                    }
                    else
                    {
                        log(" case Settings.FileModified: if(FileOperations.isFile(fileQueue.getPath())) FALSE ["
                                + fileQueue.getStorage( ) + "]");
                    }
                }
                else
                {
                    log(" case Settings.FileModified: if(!FileOperations.isLink(fileQueue.getPath())) FALSE ["
                            + fileQueue.getStorage( ) + "]");
                }
            break;

            case Settings.FileDeleted:
                ArrayList <String> shas = getShaFromUri(fileQueue.getStorage( ));
                log(" Settings.FileDeleted: [" + fileQueue.getStorage( ) + "] SHASIZE {" + shas.size( ) + "}");
                if (shas.size( ) > 0)
                {
                    for (int i = 0; i < shas.size( ); i++)
                    {
                        UniqueFile file = hashMapTable.get(shas.get(i));
                        file.removeLinks( );
                        file.clearUrl( );
                    }
                }
                else
                {
                    if (FileOperations.isLink(fileQueue.getStorage( )))
                    {
                        // TODO: WHEN MOVE DO SOMETHING HERE
                        log(" Settings.FileDeleted: if(FileOperations.isLink(fileQueue.getPath())) TRUE ["
                                + fileQueue.getStorage( ) + "]");
                    }
                    else
                    {
                        log(" Settings.FileDeleted: if(FileOperations.isLink(" + fileQueue.getStorage( ) + ")) FALSE ["
                                + fileQueue.getStorage( ) + "]");
                        String filesha = linkMapTable.get(fileQueue.getStorage( ));
                        if (FileOperations.isFile(fileQueue.getStorage( )))
                        {
                            log(" Settings.FileDeleted: hashMapTable.get(filesha).delLink(" + fileQueue.getStorage( )
                                    + "); [" + fileQueue.getStorage( ) + "]");
                            // TODO: CHECK NULL POINTER
                            hashMapTable.get(filesha).delLink(fileQueue.getStorage( ));
                        }
                        else
                        {
                            log(" Settings.FileDeleted: if(FileOperations.isFile(" + fileQueue.getStorage( )
                                    + ")) FALSE [" + fileQueue.getStorage( ) + "]");

                            // TODO: MUST CHECK IF PATH IS NOT NULL
                            Storage filepath = null;
                            if (null != hashMapTable.get(filesha))
                            {
                                filepath = hashMapTable.get(filesha).getStorage( );
                            }
                            else
                            {
                                log(" if (! hashMapTable.get(" + filesha + ")) NULL");
                            }
                            if (null != filepath.getString( ) && !filepath.isEmpty( ))
                            {
                                log(" Settings.FileDeleted: hashMapTable.get(" + filesha
                                        + ").unLink(fileQueue.getPath()); [" + fileQueue.getStorage( ) + "]");
                                log(" Settings.FileDeleted: [" + hashMapTable.get(filesha).getStorage( ) + "]");
                                hashMapTable.get(filesha).unLink(fileQueue.getStorage( ));
                                linkMapTable.remove(fileQueue.getStorage( ));
                            }
                            else
                            {
                                log(" if (! hashMapTable.get(" + filesha
                                        + ").getFilePath( ).toString( ).isEmpty( )) FALSE");
                            }
                        }
                    }
                }
            break;

            case Settings.FileRenamed:
                log(" case Settings.FileRenamed: FROM [" + fileQueue.getStorage( ) + "]");
                log(" case Settings.FileRenamed: TO   [" + fileQueue.getStorageTarget( ) + "]");
                if (FileOperations.isLink(fileQueue.getStorageTarget( )))
                {
                    // se moveu link, atualizar caminho do link na tabela
                    String filesha = linkMapTable.get(fileQueue.getStorage( ));
                    linkMapTable.put(fileQueue.getStorageTarget( ), filesha);
                    linkMapTable.remove(fileQueue.getStorage( ));
                    hashMapTable.get(filesha).renLink(fileQueue.getStorage( ), fileQueue.getStorageTarget( ));
                    // se moveu o arquivo, atualizar path no sha do arquivo
                    // se moveu o arquivo, atualizar todos os links do arquivo
                }
                else
                {
                    ArrayList <String> shaz = getShaFromUri(fileQueue.getStorage( ).getPath( ));
                    for (int i = 0; i < shaz.size( ); i++)
                    {
                        hashMapTable.get(shaz.get(i)).setPath(fileQueue.getStorageTarget( ).getPath( ));
                    }
                }
            break;

            default:
        }

        Logger.msg("-- [ LOG END    ] ----------------------------------------------------------------------------------------------------------------------------------------\n");
        Logger.msg("\n== [ FILE TABLE ] ========================================================================================================================================");
        Iterator <Entry <String, UniqueFile>> it = hashMapTable.entrySet( ).iterator( );
        while (it.hasNext( ))
        {
            Map.Entry <String, UniqueFile> pair = (Map.Entry <String, UniqueFile>) it.next( );
            pair.getValue( ).show( );
        }

        Logger.msg("__ [ LINK TABLE ] ________________________________________________________________________________________________________________________________________");
        Iterator <Entry <Storage, String>> lm = linkMapTable.entrySet( ).iterator( );
        while (lm.hasNext( ))
        {
            Map.Entry <Storage, String> pair = (Map.Entry <Storage, String>) lm.next( );
            Logger.msg("[" + pair.getValue( ) + "] [" + pair.getKey( ) + "]");
        }
        Logger.msg("\n__ [ LOG START  ] ________________________________________________________________________________________________________________________________________");

        // TODO: set savedb max files as configurable parameter
        if (savedb > 10)
        {
            this.save( );
            savedb = 0;
        }
    }

    /**
     * This method includes a new fresh file to the hash table database or replaces the file content to a path that
     * points to its identical file already added to the hash table database.
     * 
     * @param fileName
     *                     A <code>String</code> containing a file location path.
     */
    private void manageNewSha(UniqueFile fileName)
    {
        ArrayList <String> oldsha = getShaFromUri(fileName.getStorage( ));
        if (oldsha.size( ) > 0)
        {
            for (int i = 0; i < oldsha.size( ); i++)
            {
                hashMapTable.remove(oldsha.get(i));
            }
        }
        hashMapTable.put(fileName.getSha( ), fileName);
        filesIncluded++;
        Logger.msg("[" + Utils.addLeadingZeros(filesIncluded) + "][" + Utils.addLeadingZeros(filesReplaced) + "]" +
                Settings.Tab + "[" + fileName.getSha( ) + "]" + Settings.Tab + Strings.wkIncluding
                + fileName.getStorage( ));
    }

    /**
     * This method removes an already included file in the hash table database in case it is deleted from file system.
     * 
     * @param fileName
     *                     A <code>String</code> containing a file location path.
     */
    private void manageExistingSha(UniqueFile newfile)
    {

        // SHA do URI j� est� cadastrado na base
        // Retorna o URI da base com o SHA de entrada
        UniqueFile currentfile = hashMapTable.get(newfile.getSha( ));
        if (null == currentfile)
        {
            newfile.makeLinks( );
            hashMapTable.put(newfile.getSha( ), newfile);
            linkMapTable.put(newfile.getStorage( ), newfile.getSha( ));
        }
        else
        {
            if (0 == currentfile.getPath( ).compareTo(newfile.getPath( )))
            {
                // Se o URI da base � igual ao URI de entrada
                // Mesmo SHA, Mesmo URI
                // Ent�o Arquivo n�o mudou
                log(" if(0 == currentfile.getPath().compareTo(" + newfile.getStorage( ) + ")) TRUE");
            }
            else
            {
                // Se o URI da base � diferente do URI de entrada
                // Verifica se a URI de entrada tem algum SHA na base
                ArrayList <String> oldsha = getShaFromUri(newfile.getStorage( ));
                if (oldsha.size( ) > 0)
                {
                    for (int i = 0; i < oldsha.size( ); i++)
                    {
                        if (oldsha.get(i).equals(newfile.getSha( )))
                        {
                            log(" if (oldsha.get(i).equals(" + newfile.getSha( ) + ")) TRUE (" + newfile.getStorage( )
                                    + "): A and B are the same, no changes");
                            // se o SHA da URI de entrada � igual ao SHA da URI da base
                            // ent�o o conte�do do URI foi salvo mas n�o mudou o conte�do
                        }
                        else
                        {
                            if (hashMapTable.get(oldsha.get(i)).getSha( ).equals(newfile.getSha( )))
                            {
                                log(" if (hashMapTable.get(" + oldsha.get(i) + ").getSha().equals(" + newfile.getSha( )
                                        + ")) TRUE (" + newfile.getStorage( ) + "): HANDLING THE SAME FILE");
                                log(" if (hashMapTable.get(" + oldsha.get(i) + ").getSha().equals(" + newfile.getSha( )
                                        + ")) TRUE (" + newfile.getStorage( ) + "): SHA: " + oldsha.get(i));
                                log(" if (hashMapTable.get(" + oldsha.get(i) + ").getSha().equals(" + newfile.getSha( )
                                        + ")) TRUE (" + newfile.getStorage( ) + "): FILE: "
                                        + hashMapTable.get(oldsha.get(i)).getStorage( ));
                            }
                            else
                            {
                                filesReplaced++;
                                Logger.msg("[" + Utils.addLeadingZeros(filesIncluded) + "]["
                                        + Utils.addLeadingZeros(filesReplaced) + "]" +
                                        Settings.Tab + "[" + newfile.getSha( ) + "]" + Settings.Tab
                                        + Strings.wkReplacing + newfile.getStorage( ));
                                hashMapTable.get(newfile.getSha( )).addLink(newfile.getStorage( ));
                            }
                            // se o SHA da URI de entrada � diferente do SHA da uri de base
                            // ent�o o conte�do do URI foi salvo e mudou o conte�do
                            // atualizar SHA do URI
                            Logger.msg("[" + Utils.addLeadingZeros(filesIncluded) + "]["
                                    + Utils.addLeadingZeros(filesReplaced) + "]" + Settings.Tab + Strings.wkRemoving
                                    + newfile.getStorage( ));
                            filesIncluded--;
                            hashMapTable.remove(oldsha.get(i));
                        }
                    }
                }
                else
                {
                    // URI de entrada � um arquivo duplicado
                    if (FileOperations.isFile(newfile.getStorage( )))
                    {
                        if (hashMapTable.get(newfile.getSha( )).isEmpty( ))
                        {
                            hashMapTable.get(newfile.getSha( )).setPath(newfile.getStorage( ));
                            hashMapTable.get(newfile.getSha( )).makeLinks( );
                        }
                        else
                        {
                            hashMapTable.get(newfile.getSha( )).addLink(newfile.getStorage( ));
                            linkMapTable.put(newfile.getStorage( ), newfile.getSha( ));
                        }
                    }
                    else
                    {
                        log(" if (FileOperations.isFile(" + newfile.getStorage( ) + ")) FALSE "
                                + "): FROM {" + hashMapTable.get(newfile.getSha( )).getStorage( ) + "} TO:");
                        log(" if (FileOperations.isFile(" + newfile.getStorage( ) + ")) FALSE "
                                + "): dupelist[{" + newfile.getSha( ) + "}] = {" + newfile.getStorage( ) + "}");
                    }
                }
            }
        }
    }

    private ArrayList <String> getShaFromUri(Path uri)
    {
        return getShaFromUri(new Storage(uri));
    }

    private ArrayList <String> getShaFromUri(Storage uri)
    {
        ArrayList <String>                    shas = new ArrayList <String>( );
        Iterator <Entry <String, UniqueFile>> it   = hashMapTable.entrySet( ).iterator( );
        while (it.hasNext( ))
        {
            Map.Entry <String, UniqueFile> pair = (Map.Entry <String, UniqueFile>) it.next( );
            if (0 == pair.getValue( ).getPath( ).compareTo(uri.getPath( )))
            {
                shas.add(pair.getKey( ));
            }
        }
        return shas;
    }

    /**
     * This method displays a log message through the embedded log system.
     * 
     * @param logMessage
     *                       A <code>String</code> containing the log message to display.
     */
    private static void log(String logMessage)
    {
        Logger.log(Thread.currentThread( ), logMessage, Logger.WORKER);
    }
}
