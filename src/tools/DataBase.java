package tools;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import settings.Settings;
import settings.Strings;

public class DataBase
{
    public static void clear()
    {
        msg(Strings.dbEraseDatabase);
        HashMap<String,String> hashMapToClear=new HashMap<String,String>();
        saveMap(hashMapToClear);
    }

    public static void saveMap(HashMap<String,String> hashMapToSave)
    {
        msg(Strings.dbSaveDatabase);
        try
        {
            ObjectOutputStream hashMapObjectOutput=new ObjectOutputStream(new FileOutputStream(Settings.UnDupeKeeperDatabaseName));
            msg(Strings.dbDatabaseContains+
                hashMapToSave.size()+
                Strings.dbItems);
            hashMapObjectOutput.writeObject(hashMapToSave);
            hashMapObjectOutput.close();
            hashMapObjectOutput=null;
            msg(Strings.dbDatabaseSaved);
        }
        catch(IOException e)
        {
            log(Strings.dbProblemToSaveMap+
                e);
        }
    }

    public static HashMap<String,String> loadMap()
    {
        if(new File(Settings.UnDupeKeeperDatabaseName).exists())
        {
            msg(Strings.dbLoadingDatabase);
            try
            {
                @SuppressWarnings("unchecked")
                HashMap<String,String> hashMapToLoad=(HashMap<String,String>)new ObjectInputStream(new FileInputStream(Settings.UnDupeKeeperDatabaseName)).readObject();
                msg(Strings.dbDatabaseContains+
                    hashMapToLoad.size()+
                    Strings.dbItems);
                return hashMapToLoad;
            }
            catch(ClassNotFoundException|IOException e)
            {
                log(Strings.dbProblemDatabaseCreation+
                    e);
            }
        }
        msg(Strings.dbWarningNewDatabase);
        return new HashMap<String,String>();
    }

    public static void saveDir(String folderName)
    {
        try
        {
            ObjectOutputStream directoryToSaveOutput=new ObjectOutputStream(new FileOutputStream(Settings.WatchedDirectoryName));
            directoryToSaveOutput.writeObject(folderName);
            directoryToSaveOutput.close();
            directoryToSaveOutput=null;
        }
        catch(IOException e)
        {
            log(Strings.dbProblemSavingDir+
                e);
        }
    }

    public static String loadDir()
    {
        if(new File(Settings.WatchedDirectoryName).exists())
        {
            try
            {
                return (String)new ObjectInputStream(new FileInputStream(Settings.WatchedDirectoryName)).readObject();
            }
            catch(ClassNotFoundException|IOException e)
            {
                log(Strings.dbProblemStoringSettings+
                    e);
            }
        }
        return null;
    }

    private static void log(String logMessage)
    {
        Logger.log(Thread.currentThread(),
                   logMessage,
                   Logger.DATABASE);
    }

    private static void msg(String message)
    {
        Logger.msg(message);
    }
}
