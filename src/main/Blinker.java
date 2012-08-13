package main;
import java.awt.TrayIcon;
import java.util.concurrent.BlockingQueue;
import settings.Settings;
import settings.Strings;
import tools.FileQueue;
import tools.Logger;
import tools.TrayImage;

/**
 * Blinker class is responsible for controlling System Tray Icon. It changes
 * System Tray Icon according to the usage.
 * 
 * @author vliopard
 */
public class Blinker implements
                    Runnable
{
    private long                           size  =0;
    private boolean                        red   =false;
    private boolean                        gray  =false;
    private boolean                        green =false;
    private boolean                        color =false;
    private final TrayIcon                 trayIcon;
    private final BlockingQueue<Integer>   stopSignal;
    private final BlockingQueue<FileQueue> transferQueue;

    /**
     * Blinker Constructor - Initialize a Blinker object for controlling System
     * Tray Icon.
     * 
     * @param fileQueue
     *            This is to inform Blinker thread on how many items are in
     *            the processed list, for displaying the proper icon.
     * @param signalQueue
     *            This is a queue for controlling when Blinker thread is going
     *            to be terminated.
     * @param iconTray
     *            Icon object that receives new icon image for changing.
     */
    Blinker(BlockingQueue<FileQueue> fileQueue,
            BlockingQueue<Integer> signalQueue,
            TrayIcon iconTray)
    {
        transferQueue=fileQueue;
        stopSignal=signalQueue;
        trayIcon=iconTray;
    }

    /**
     * This method set that the red icon is displayed and keep it until another
     * icon is requested.
     */
    private void setRed()
    {
        red=true;
        gray=false;
        green=false;
        color=false;
    }

    /**
     * This method set that the gray icon is displayed and keep it until another
     * icon is requested.
     */
    private void setGray()
    {
        red=false;
        gray=true;
        green=false;
        color=false;
    }

    /**
     * This method set that the green icon is displayed and keep it until
     * another icon is requested.
     */
    private void setGreen()
    {
        red=false;
        gray=false;
        green=true;
        color=false;
    }

    /**
     * This method set that the color icon is displayed and keep it until
     * another icon is requested.
     */
    private void setColor()
    {
        red=false;
        gray=false;
        green=false;
        color=true;
    }

    /**
     * This is the run method of the Blinker thread, that will monitor icon
     * changes and exit when receiving stop signal from main thread.
     */
    @Override
    public void run()
    {
        msg(Strings.uiStartup);
        do
        {
            try
            {
                Thread.sleep(Settings.TraySleepTime);
            }
            catch(InterruptedException e)
            {
                err("000: "+Strings.uiProblem+
                    e);
            }
            size=transferQueue.size();
            changeSystemTrayTip(size);
            if((size<=10)&&
               (!green))
            {
                setGreen();
                changeSystemTrayIcon(Settings.IconDnaGreen);
                continue;
            }
            if((size>10)&&
               (size<=100)&&
               (!gray))
            {
                setGray();
                changeSystemTrayIcon(Settings.IconDnaGray);
                continue;
            }
            if((size>100)&&
               (size<=300)&&
               (!red))
            {
                setRed();
                changeSystemTrayIcon(Settings.IconDnaRed);
                continue;
            }
            if((size>300)&&
               (!color))
            {
                setColor();
                changeSystemTrayIcon(Settings.IconDnaColor);
                continue;
            }
        }
        while(!stopSignal.contains(Settings.StopWorking));
        try
        {
            stopSignal.put(Settings.BlinkerStopped);
        }
        catch(InterruptedException e)
        {
            err("001: "+Strings.wkErrorSendingShutdownMessage);
        }
        msg(Strings.uiShutdown);
    }

    /**
     * This method updates System Tray's Tool Tip message with the count number
     * or remaining items to process.
     * 
     * @param totalItems
     *            The total items in the queue to be processed.
     */
    private void changeSystemTrayTip(long totalItems)
    {
        trayIcon.setToolTip(Strings.bkTotalItems+
                            String.valueOf(totalItems));
    }

    /**
     * This method changes System Tray's Icon based on
     * <code>Settings.iconList[]</code> items.
     * 
     * @param iconIndex
     *            The index number of <code>Settings.iconList[]</code> to be
     *            displayed on System Tray.
     */
    private void changeSystemTrayIcon(int iconIndex)
    {
        trayIcon.setImage(TrayImage.setSystemTrayImage(iconIndex));
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
