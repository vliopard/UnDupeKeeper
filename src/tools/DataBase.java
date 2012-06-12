package tools;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import main.Worker;
import settings.Settings;
import settings.Strings;

public class DataBase
{
    private static Worker workerThread;

    public static void useWorker(Worker worker)
    {
        workerThread=worker;
    }

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

    public static void saveSettings(SettingsHandler settingsTransfer)
    {
        // msg(Strings.dbSaveSettings);
        try
        {
            ObjectOutputStream hashMapObjectOutput=new ObjectOutputStream(new FileOutputStream(Settings.UnDupeKeeperSettings));
            hashMapObjectOutput.writeObject(settingsTransfer);
            hashMapObjectOutput.close();
            hashMapObjectOutput=null;
            // msg(Strings.dbSettingsSaved);
        }
        catch(IOException e)
        {
            log(Strings.dbProblemToSaveSettings+
                e);
        }
    }

    public static SettingsHandler loadSettings()
    {
        if(new File(Settings.UnDupeKeeperSettings).exists())
        {
            // msg(Strings.dbLoadingSettings);
            try
            {
                SettingsHandler settingsTransfer=(SettingsHandler)new ObjectInputStream(new FileInputStream(Settings.UnDupeKeeperSettings)).readObject();
                return settingsTransfer;
            }
            catch(ClassNotFoundException|IOException e)
            {
                log(Strings.dbProblemSettingsCreation+
                    e);
            }
        }
        msg(Strings.dbWarningNewSettings);
        return new SettingsHandler();
    }

    public static boolean openSettings()
    {
        SettingsHandler transferSettings=loadSettings();
        Options settingsWindow=new Options(transferSettings);
        settingsWindow.setTitle(Strings.ssSettingsTitle);
        settingsWindow.pack();
        settingsWindow.setModal(true);
        settingsWindow.setResizable(false);
        settingsWindow.setVisible(true);
        if(transferSettings.isChanged())
        {
            if(transferSettings.isEncryptionChanged())
            {
                transferSettings.resetEncryptionChanged();
                workerThread.clear();
                workerThread.load();
            }
            transferSettings.setChanged(false);
            saveSettings(transferSettings);
            return true;
        }
        saveSettings(transferSettings);
        return false;
    }

    public static String chooseDir()
    {
        JFrame frame=new JFrame();
        JFileChooser chooser=new JFileChooser();
        String directoryToLoad=DataBase.loadDir();
        if(null==directoryToLoad)
        {
            directoryToLoad=Settings.RootDir;
        }
        chooser.setCurrentDirectory(new java.io.File(directoryToLoad));
        chooser.setDialogTitle(Strings.ukSelectFolder);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        if(chooser.showOpenDialog(frame)==JFileChooser.APPROVE_OPTION)
        {
            directoryToLoad=chooser.getSelectedFile()
                                   .toString();
            DataBase.saveDir(directoryToLoad);
            return directoryToLoad;
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
