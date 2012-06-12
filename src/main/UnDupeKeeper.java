package main;
import java.nio.file.*;
import java.awt.AWTException;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import settings.Settings;
import settings.Strings;
import tools.DataBase;
import tools.FileQueue;
import tools.Logger;
import tools.SettingsHandler;
import tools.TrayImage;

public class UnDupeKeeper
{
    private static boolean                  recursiveFolderScan =false;
    private static TrayIcon                 trayIcon;
    private static Blinker                  guiThread;
    private static Worker                   workerThread;
    private static SettingsHandler          settingsHandler;
    private static BlockingQueue<Integer>   stopSignal;
    private static BlockingQueue<FileQueue> transferQueue;

    private static void usage()
    {
        err(Strings.ukUsage);
        System.exit(-1);
    }

    private static boolean isDir(Path dirName)
    {
        if(null==dirName)
        {
            return false;
        }
        return (new File(dirName.toString()).exists())&&
               (new File(dirName.toString()).isDirectory());
    }

    private static Path checkPromptArguments(String[] arguments)
    {
        int argumentIndex=0;
        if(arguments.length==0||
           arguments.length>Settings.TotalArguments)
        {
            usage();
        }
        if(arguments[0].equals(Settings.Recursive))
        {
            recursiveFolderScan=true;
            if(arguments.length<Settings.TotalArguments)
            {
                usage();
            }
            argumentIndex++;
        }
        return Paths.get(arguments[argumentIndex]);
    }

    public static void main(String[] args) throws IOException
    {
        settingsHandler=DataBase.loadSettings();
        Path directoryToWatch=null;
        if(args.length>0)
        {
            directoryToWatch=checkPromptArguments(args);
        }
        while(!isDir(directoryToWatch))
        {
            String directoryName=null;
            recursiveFolderScan=true;
            if(settingsHandler.isDirectoryFirstTime())
            {
                directoryName=DataBase.chooseDir();
                if(null==directoryName)
                {
                    usage();
                }
                settingsHandler.setDirectory(directoryName);
                DataBase.saveSettings(settingsHandler);
            }
            else
            {
                directoryName=settingsHandler.getDirectory();
            }
            directoryToWatch=Paths.get(directoryName);
        }
        trayIcon=TrayImage.setSystemTrayIcon(Settings.IconDnaGreen);
        startUI();
        stopSignal=new LinkedBlockingQueue<Integer>();
        transferQueue=new LinkedBlockingQueue<FileQueue>();
        try
        {
            stopSignal.put(Settings.KeepWorking);
            guiThread=new Blinker(transferQueue,
                                  stopSignal,
                                  trayIcon);
            new Thread(guiThread).start();
            // TODO: Must check if this settings is really being applied during encryption
            Settings.CypherMethod=Settings.CypherMethodList[settingsHandler.getEncryptionMethod()];
            workerThread=new Worker(transferQueue,
                                    stopSignal);
            new Thread(workerThread).start();
            msg(Strings.ukUndupekeeperIsWorking);
            @SuppressWarnings("unused")
            Monitor dm=new Monitor(directoryToWatch.toString(),
                                   transferQueue,
                                   stopSignal,
                                   recursiveFolderScan);
            while((!stopSignal.contains(Settings.WorkerStopped))||
                  (!stopSignal.contains(Settings.BlinkereStopped)))
            {
                Thread.sleep(Settings.ExitSleepTime);
            }
        }
        catch(InterruptedException e)
        {
            err(Strings.ukProblemStarting+
                e);
        }
        msg(Strings.ukNormalShutdonw);
    }

    private static void startShutdown()
    {
        try
        {
            msg(Strings.ukStopping);
            stopSignal.put(Settings.StopWorking);
        }
        catch(InterruptedException e)
        {
            err(Strings.ukCantSendExitToWorker);
        }
    }

    private static void startUI()
    {
        try
        {
            UIManager.setLookAndFeel(Settings.LookAndFeelPackages[settingsHandler.getLookAndFeel()]);
        }
        catch(UnsupportedLookAndFeelException|IllegalAccessException
                |InstantiationException|ClassNotFoundException ex)
        {
            err(Strings.ukErrorLoadingLookAndFeel);
        }
        // UIManager.put( "swing.boldMetal", Boolean.FALSE);
        SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    createAndShowGUI();
                }
            });
    }

    private static void createAndShowGUI()
    {
        if(!SystemTray.isSupported())
        {
            JOptionPane.showMessageDialog(null,
                                          Strings.ukSystemTrayNotSupported);
            err(Strings.ukSystemTrayNotSupported);
            return;
        }
        final PopupMenu popupMenu=new PopupMenu();
        // trayIcon=TrayImage.setSystemTrayIcon(Settings.IconDnaGreen);
        final SystemTray systemTray=SystemTray.getSystemTray();
        MenuItem saveDatabase=new MenuItem(Strings.ukSaveDatabase);
        MenuItem clearDatabase=new MenuItem(Strings.ukClearDatabase);
        MenuItem settingsItem=new MenuItem(Strings.ukSettingsMenu);
        MenuItem aboutItem=new MenuItem(Strings.ukAboutUndupekeeperMenu);
        MenuItem exitItem=new MenuItem(Strings.ukExitUndupekeeper);
        popupMenu.add(saveDatabase);
        popupMenu.add(clearDatabase);
        popupMenu.add(settingsItem);
        popupMenu.addSeparator();
        popupMenu.add(aboutItem);
        popupMenu.add(exitItem);
        trayIcon.setPopupMenu(popupMenu);
        try
        {
            systemTray.add(trayIcon);
        }
        catch(AWTException e)
        {
            err(Strings.ukSystemTrayIconCantBeAdded);
            return;
        }
        trayIcon.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent event)
                {
                    JOptionPane.showMessageDialog(null,
                                                  Strings.ukAboutUndupekeeperDialog+
                                                          "\n"+
                                                          Settings.CypherMethod+
                                                          " "+
                                                          Settings.LookAndFeelNames[settingsHandler.getLookAndFeel()]);
                }
            });
        saveDatabase.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent event)
                {
                    workerThread.save();
                }
            });
        clearDatabase.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent event)
                {
                    if(JOptionPane.showConfirmDialog(null,
                                                     Strings.ukDatabaseWillBeEmpty)==JOptionPane.YES_OPTION)
                    {
                        workerThread.clear();
                        workerThread.load();
                    }
                    else
                    {
                        JOptionPane.showMessageDialog(null,
                                                      Strings.ukOperationCanceled);
                    }
                }
            });
        aboutItem.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent event)
                {
                    JOptionPane.showMessageDialog(null,
                                                  Strings.ukAboutUndupekeeperDialog+
                                                          "\n"+
                                                          Settings.CypherMethod+
                                                          " "+
                                                          Settings.LookAndFeelNames[settingsHandler.getLookAndFeel()]);
                }
            });
        settingsItem.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent event)
                {
                    DataBase.useWorker(workerThread);
                    if(DataBase.openSettings())
                    {
                        startShutdown();
                        systemTray.remove(trayIcon);
                    }
                }
            });
        exitItem.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent event)
                {
                    startShutdown();
                    systemTray.remove(trayIcon);
                }
            });
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
