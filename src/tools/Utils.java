package tools;
import java.awt.AWTException;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.text.DecimalFormat;
import settings.Settings;
import settings.Strings;

/**
 * Utils class is responsible for providing a set of miscellaneous tools
 * 
 * @author vliopard
 */
public class Utils
{
    /**
     * 
     * This is a method for customizing a number format value based on a
     * provided pattern.
     * 
     * @param pattern
     *            A <code>String</code> value that represents the format to be
     *            applied on provided number.
     * @param value
     *            A <code>double</code> value to be formated using provided
     *            pattern.
     * @return Returns a <code>String</code> containing a representation of the
     *         formated number.
     */
    public static String customFormat(String pattern,
                                      double value)
    {
        return new DecimalFormat(pattern).format(value);
    }

    /**
     * This is a method to format a number using the default pattern.
     * 
     * @param value
     *            A <code>double</code> value to be formated using default
     *            pattern.
     * @return Returns a <code>String</code> containing a representation of the
     *         formated number.
     */
    public static String numberFormat(double value)
    {
        return new DecimalFormat(Strings.numberFormatMask).format(value);
    }

    /**
     * This method adds leading zeros to a <code>String</code> representation of
     * a number to keep display organized in columns.
     * 
     * @param numberToFormat
     *            A <code>long</code> number that will receive leading zeros.
     * @return Returns an <code>String</code> representing a <code>long</code>
     *         number with leading zeros.
     */
    public static String addLeadingZeros(long numberToFormat)
    {
        return String.format("%06d",
                             numberToFormat);
    }

    /**
     * This method adds leading zeros to a <code>String</code> representation of
     * a number to keep display organized in columns.
     * 
     * @param mask
     *            An <code>String</code> informing the amount of leading zeros.
     * @param numberToFormat
     *            A <code>long</code> number that will receive leading zeros.
     * @return Returns an <code>String</code> representing a <code>long</code>
     *         number with customized leading zeros.
     */
    public static String addCustomLeadingZeros(String mask,
                                               long numberToFormat)
    {
        return String.format("%"+
                                     mask+
                                     "d",
                             numberToFormat);
    }

    /**
     * This method creates and displays a notification Balloon on the System
     * Tray.
     * 
     * @param title
     *            A <code>String</code> value containing the message title.
     * @param message
     *            A <code>String</code> value containing the message body.
     * @param type
     *            A <code>MessageType</code> value containing the message type
     *            <code>[ERROR | INFO | WARNING | NONE]</code>.
     */
    public static void displayBallon(String title,
                                     String message,
                                     MessageType type)
    {
        SystemTray systemTray=SystemTray.getSystemTray();
        TrayIcon trayIcon=TrayImage.setSystemTrayIcon(Settings.IconYellow);
        try
        {
            systemTray.add(trayIcon);
            trayIcon.displayMessage(title,
                                    message,
                                    type);
            Thread.sleep(5000);
        }
        catch(AWTException|InterruptedException e)
        {
            err("MSG_036: "
                    +Strings.utBalloonError+
                e);
        }
        systemTray.remove(trayIcon);
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
