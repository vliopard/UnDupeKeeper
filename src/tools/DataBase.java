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
import main.Options;
import main.Worker;
import settings.Settings;
import settings.Strings;

/**
 * DataBase class is responsible for managing all the serialized information
 * related to the UnDupeKeeper for later use.
 * 
 * @author vliopard
 */
public class DataBase
{
    private static int    systemLanguage =0;
    private static Worker workerThread;

    /**
     * This method receives a running worker to handle their information that
     * will be persisted.
     * 
     * @param worker
     *            A <code>Worker</code> thread to get its hash map of processing
     *            values.
     */
    public static void useWorker(Worker worker)
    {
        workerThread=worker;
    }

    /**
     * This method just clear the hash map serialized data with a new fresh
     * object without any values.
     */
    public static void clear()
    {
        msg(Strings.dbEraseDatabase);
        HashMap<String,String> hashMapToClear=new HashMap<String,String>();
        saveMap(hashMapToClear);
    }

    /**
     * This method saves a hash map of working values to the file system for
     * later use.
     * 
     * @param hashMapToSave
     *            A <code>HashMap</code> of <code>Strings</code> containing the
     *            values to be written to the disk.
     */
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

    /**
     * This method restores a previous saved session containing all the data
     * already worked.
     * 
     * @return Returns a <code>HashMap</code> of Strings containing an Encrypted
     *         representation and its file path location.
     */
    public static HashMap<String,String> loadMap()
    {
        if(new File(Settings.UnDupeKeeperDatabaseName).exists())
        {
            msg(Strings.dbLoadingDatabase);
            try
            {
                FileInputStream fileInputStream=new FileInputStream(Settings.UnDupeKeeperDatabaseName);
                @SuppressWarnings("unchecked")
                HashMap<String,String> hashMapToLoad=(HashMap<String,String>)new ObjectInputStream(fileInputStream).readObject();
                msg(Strings.dbDatabaseContains+
                    hashMapToLoad.size()+
                    Strings.dbItems);
                fileInputStream.close();
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

    /**
     * This method saves the dialog chooser's directory to remember the last
     * place you visit.
     * 
     * @param folderName
     *            A <code>String</code> containing the path to the dialog
     *            chooser's directory.
     */
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

    /**
     * This method restores the last saved directory selected by the dialog
     * chooser.
     * 
     * @return Returns a <code>String</code> that contains the path to a saved
     *         serialized Directory.
     */
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

    /**
     * This method informs the last language index selected by the settings
     * dialog.
     * 
     * @return Returns an <code>int</code> that contains the language index to
     *         be used.
     */
    public static int loadLanguageIndex()
    {
        return systemLanguage;
    }

    /**
     * This method informs the last language selected by the settings dialog.
     * 
     * @return Returns a <code>String</code> that contains the language to be
     *         used.
     */
    public static String loadLanguage()
    {
        return Settings.languageValues[systemLanguage];
    }

    /**
     * This method saves all settings obtained from the settings dialog.
     * 
     * @param settingsTransfer
     *            A <code>SettingsHandler</code> object containing the software
     *            settings to be saved to disk.
     */
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

    /**
     * This method restores all dialog settings previously saved to the disk.
     * 
     * @return Returns a <code>SettingsHandler</code> object that contains
     *         previous serialized saved settings.
     */
    public static SettingsHandler loadSettings()
    {
        if(new File(Settings.UnDupeKeeperSettings).exists())
        {
            // msg(Strings.dbLoadingSettings);
            try
            {
                FileInputStream fileInputStream=new FileInputStream(Settings.UnDupeKeeperSettings);
                SettingsHandler settingsTransfer=(SettingsHandler)new ObjectInputStream(fileInputStream).readObject();
                systemLanguage=settingsTransfer.getLanguageIndex();
                fileInputStream.close();
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

    /**
     * This method displays the settings dialog window containing all changeable
     * UndupeKeeper settings.
     * 
     * @return Returns <code>true</code> if settings were changed and confirmed.
     *         Returns <code>false</code> if dialog is canceled or closed.
     */
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

    /**
     * This method displays the Directory Chooser Dialog for selecting a
     * directory to be handled by UnDupeKeeper.
     * 
     * @return Returns a <code>String</code> that contains the selected
     *         directory path from dialog box. Returns <code>null</code> if
     *         dialog box is closed without selecting a Directory.
     */
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
                   Logger.DATABASE);
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
}
