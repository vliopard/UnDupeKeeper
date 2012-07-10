package tools;
import java.awt.AWTException;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.text.DecimalFormat;
import settings.Settings;
import settings.Strings;

// TODO: JAVADOC
// TODO: METHOD AND VARIABLE NAMES REFACTORING
public class Utils
{
    public static String customFormat(String pattern,
                                      double value)
    {
        return new DecimalFormat(pattern).format(value);
    }

    public static String format(double value)
    {
        return new DecimalFormat(Strings.numberFormatMask).format(value);
    }

    public static void displayBallon(String title,
                                     String message)
    {
        SystemTray systemTray=SystemTray.getSystemTray();
        TrayIcon ti=TrayImage.setSystemTrayIcon(Settings.IconYellow);
        try
        {
            systemTray.add(ti);
            ti.displayMessage(title,
                              message,
                              MessageType.WARNING);
            Thread.sleep(5000);
        }
        catch(AWTException|InterruptedException e)
        {
            // TODO: HANDLE ERROR MESSAGE
            e.printStackTrace();
        }
        systemTray.remove(ti);
    }
}
