package main;
import java.awt.TrayIcon;
import java.util.concurrent.BlockingQueue;
import settings.Settings;
import settings.Strings;
import tools.FileQueue;
import tools.Logger;
import tools.TrayImage;

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

    Blinker(BlockingQueue<FileQueue> fileQueue,
            BlockingQueue<Integer> signalQueue,
            TrayIcon iconTray)
    {
        transferQueue=fileQueue;
        stopSignal=signalQueue;
        trayIcon=iconTray;
    }

    private void setRed()
    {
        red=true;
        gray=false;
        green=false;
        color=false;
    }

    private void setGray()
    {
        red=false;
        gray=true;
        green=false;
        color=false;
    }

    private void setGreen()
    {
        red=false;
        gray=false;
        green=true;
        color=false;
    }

    private void setColor()
    {
        red=false;
        gray=false;
        green=false;
        color=true;
    }

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
                err(Strings.uiProblem+
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
            stopSignal.put(Settings.BlinkereStopped);
        }
        catch(InterruptedException e)
        {
            err(Strings.wkErrorSendingShutdownMessage);
        }
        msg(Strings.uiShutdown);
    }

    private void changeSystemTrayTip(long totalItems)
    {
        trayIcon.setToolTip(Strings.bkTotalItems+
                            String.valueOf(totalItems));
    }

    private void changeSystemTrayIcon(int iconIndex)
    {
        trayIcon.setImage(TrayImage.setSystemTrayImage(iconIndex));
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
