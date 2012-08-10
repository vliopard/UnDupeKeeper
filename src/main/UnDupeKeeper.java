package main;
import java.nio.file.*;
import java.awt.AWTException;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.*;
import java.text.DecimalFormat;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import settings.Settings;
import settings.Strings;
import tools.Comparison;
import tools.DataBase;
import tools.FileQueue;
import tools.Logger;
import tools.SettingsHandler;
import tools.TrayImage;

/**
 * UnDupeKeeper is the main class. It calls and starts everything on the system,
 * regarding to User Interface, and working threads. Also takes care about
 * settings.
 * 
 * @author vliopard
 */
public class UnDupeKeeper
{
    private static boolean                  recursiveFolderScan =false;
    private static TrayIcon                 trayIcon;
    private static Blinker                  guiThread;
    private static Worker                   workerThread;
    private static SettingsHandler          settingsHandler;
    private static BlockingQueue<Integer>   stopSignal;
    private static BlockingQueue<FileQueue> transferQueue;
    private static JFrame                   reportViewer        =new JFrame();

    /**
     * In case of UnDupeKeeper is called by command prompt this method will show
     * an usage message after receiving wrong parameters.
     */
    private static void usage()
    {
        err(Strings.ukUsage);
        System.exit(-1);
    }

    /**
     * This method checks if a path is really a directory.
     * 
     * @param dirName
     *            A <code>Path</code> to a file location.
     * @return Return <code>false</code> when dirName is: <code>null</code>, not
     *         exists and is a Directory. Return <code>true</code> if it is an
     *         actual file.
     */
    private static boolean isDir(Path dirName)
    {
        if(null==dirName)
        {
            return false;
        }
        return (new File(dirName.toString()).exists())&&
               (new File(dirName.toString()).isDirectory());
    }

    /**
     * This method validates arguments passed to UnDupeKeeper via command
     * prompt.
     * 
     * @param arguments
     *            A <code>String</code> array containing the command prompt
     *            arguments.
     * @return Returns a <code>Path</code> containing the directory passed as
     *         argument. Sets recursive mode 'on'.
     */
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

    /**
     * This is the main method of UnDupeKeeper that starts all the system.
     * 
     * @param args
     *            A <code>String</code> array from command prompt that contains
     *            arguments or empty.
     */
    public static void main(String[] args)
    {
        settingsHandler=DataBase.loadSettings();
        Path directoryToWatch=null;
        if(args.length>0)
        {
            if(args[0].equals(Settings.TextFileList))
            {
                // TODO: CREATE FILE NOT FOUND MSG
                // TODO: EXTERNALIZE FILE NOT FOUND MSG
                File textFile=new File(args[1]);
                if((!(textFile.exists()&&
                      textFile.isFile()&&(textFile.length()>1)))&&
                   (args.length<Settings.TotalArguments))
                {
                    usage();
                }
                else
                {
                    if(args.length==Settings.TotalArguments)
                    {
                        Comparison.compare(args[1],
                                           args[2]);
                    }
                    else
                    {
                        Comparison.compare(args[1],
                                           null);
                    }
                    System.exit(0);
                }
            }
            directoryToWatch=checkPromptArguments(args);
            settingsHandler.setDirectory(directoryToWatch.toString());
            DataBase.saveDir(directoryToWatch.toString());
            DataBase.saveSettings(settingsHandler);
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
            Settings.CypherMethod=Settings.CypherMethodList[settingsHandler.getEncryptionMethod()];
            Settings.notComparing=settingsHandler.getComparisonMethod();
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
                  (!stopSignal.contains(Settings.BlinkerStopped)))
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
        System.exit(0);
    }

    /**
     * This is the method that starts the shutdown process and inform all the
     * working threads they must have to exit.
     */
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

    /**
     * This is a method for starting the user interface. It puts a System Tray
     * Icon, sets the Look and Feel and other many options.
     */
    private static void startUI()
    {
        try
        {
            UIManager.setLookAndFeel(Settings.LookAndFeelPackages[settingsHandler.getLookAndFeel()]);
        }
        catch(UnsupportedLookAndFeelException|IllegalAccessException
                |InstantiationException|ClassNotFoundException e)
        {
            err(Strings.ukErrorLoadingLookAndFeel+
                e);
        }
        // UIManager.put("swing.boldMetal", Boolean.FALSE);
        SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    createAndShowGUI();
                }
            });
    }

    /**
     * This method creates and display an About dialog containing some
     * information about the UnDupeKeeper.
     */
    private static void showAbout()
    {
        JOptionPane.showMessageDialog(null,
                                      Strings.ukAboutUndupekeeperDialog+
                                              "\nUsing: "+
                                              Settings.CypherMethod+
                                              " with binary "+
                                              (Settings.notComparing?Strings.ukComparisonOn
                                                                    :Strings.ukComparisonOff)+
                                              "\nGUI: "+
                                              Settings.LookAndFeelNames[settingsHandler.getLookAndFeel()]+
                                              "\nTotal DB items: "+
                                              new DecimalFormat(Strings.numberFormatMask).format(workerThread.size()));
    }

    /**
     * This method creates and display the Graphical User Interface, putting an
     * Icon on System Tray and its pop-up menu.
     */
    private static void createAndShowGUI()
    {
        if(!SystemTray.isSupported())
        {
            JOptionPane.showMessageDialog(null,
                                          Strings.ukSystemTrayNotSupported);
            err(Strings.ukSystemTrayNotSupported);
            startShutdown();
            return;
        }
        final PopupMenu popupMenu=new PopupMenu();
        // trayIcon=TrayImage.setSystemTrayIcon(Settings.IconDnaGreen);
        final SystemTray systemTray=SystemTray.getSystemTray();
        MenuItem saveDatabase=new MenuItem(Strings.ukSaveDatabase);
        MenuItem clearDatabase=new MenuItem(Strings.ukClearDatabase);
        final MenuItem checkManager=new MenuItem(Strings.ukViewReports);
        final MenuItem settingsItem=new MenuItem(Strings.ukSettingsMenu);
        final MenuItem aboutItem=new MenuItem(Strings.ukAboutUndupekeeperMenu);
        MenuItem exitItem=new MenuItem(Strings.ukExitUndupekeeper);
        popupMenu.add(saveDatabase);
        popupMenu.add(clearDatabase);
        popupMenu.addSeparator();
        popupMenu.add(checkManager);
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
                    aboutItem.setEnabled(false);
                    showAbout();
                    aboutItem.setEnabled(true);
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
                    aboutItem.setEnabled(false);
                    showAbout();
                    aboutItem.setEnabled(true);
                }
            });
        checkManager.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent event)
                {
                    checkManager.setEnabled(false);
                    reportViewer=ReportViewer.show(settingsHandler);
                    reportViewer.addWindowListener(new WindowListener()
                        {
                            public void windowClosed(WindowEvent e)
                            {
                                checkManager.setEnabled(true);
                            }

                            @Override
                            public void windowActivated(WindowEvent arg0)
                            {
                            }

                            @Override
                            public void windowClosing(WindowEvent arg0)
                            {
                            }

                            @Override
                            public void windowDeactivated(WindowEvent arg0)
                            {
                            }

                            @Override
                            public void windowDeiconified(WindowEvent arg0)
                            {
                            }

                            @Override
                            public void windowIconified(WindowEvent arg0)
                            {
                            }

                            @Override
                            public void windowOpened(WindowEvent arg0)
                            {
                            }
                        });
                }
            });
        settingsItem.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent event)
                {
                    settingsItem.setEnabled(false);
                    DataBase.useWorker(workerThread);
                    if(DataBase.openSettings())
                    {
                        startShutdown();
                        systemTray.remove(trayIcon);
                    }
                    settingsItem.setEnabled(true);
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
