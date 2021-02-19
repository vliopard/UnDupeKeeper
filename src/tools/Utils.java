package tools;

import java.awt.AWTException;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.util.Scanner;

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
     * This is a method for customizing a number format value based on a provided pattern.
     * 
     * @param pattern
     *                    A <code>String</code> value that represents the format to be applied on provided number.
     * @param value
     *                    A <code>double</code> value to be formated using provided pattern.
     * 
     * @return Returns a <code>String</code> containing a representation of the formated number.
     */
    public static String customFormat(String pattern, double value)
    {
        return new DecimalFormat(pattern).format(value);
    }

    /**
     * This is a method to format a number using the default pattern.
     * 
     * @param value
     *                  A <code>double</code> value to be formated using default pattern.
     * 
     * @return Returns a <code>String</code> containing a representation of the formated number.
     */
    public static String numberFormat(double value)
    {
        return new DecimalFormat(Strings.numberFormatMask).format(value);
    }

    /**
     * This method adds leading zeros to a <code>String</code> representation of a number to keep display organized in
     * columns.
     * 
     * @param numberToFormat
     *                           A <code>long</code> number that will receive leading zeros.
     * 
     * @return Returns an <code>String</code> representing a <code>long</code> number with leading zeros.
     */
    public static String addLeadingZeros(long numberToFormat)
    {
        return String.format("%06d", numberToFormat);
    }

    /**
     * This method adds leading zeros to a <code>String</code> representation of a number to keep display organized in
     * columns.
     * 
     * @param mask
     *                           An <code>String</code> informing the amount of leading zeros.
     * @param numberToFormat
     *                           A <code>long</code> number that will receive leading zeros.
     * 
     * @return Returns an <code>String</code> representing a <code>long</code> number with customized leading zeros.
     */
    public static String addCustomLeadingZeros(String mask, long numberToFormat)
    {
        return String.format("%" + mask + "d", numberToFormat);
    }

    /**
     * This method creates and displays a notification Balloon on the System Tray.
     * 
     * @param title
     *                    A <code>String</code> value containing the message title.
     * @param message
     *                    A <code>String</code> value containing the message body.
     * @param type
     *                    A <code>MessageType</code> value containing the message type
     *                    <code>[ERROR | INFO | WARNING | NONE]</code>.
     */
    public static void displayBallon(String title, String message, MessageType type)
    {
        SystemTray systemTray = SystemTray.getSystemTray( );
        TrayIcon   trayIcon   = TrayImage.setSystemTrayIcon(Settings.IconYellow);
        try
        {
            systemTray.add(trayIcon);
            trayIcon.displayMessage(title, message, type);
            Thread.sleep(5000);
        }
        catch (AWTException | InterruptedException e)
        {
            Logger.err("MSG_036: " + Strings.utBalloonError + e);
        }
        systemTray.remove(trayIcon);
    }

    public static boolean runSystemCommand(String command, int option)
    {
        try
        {
            Process process = Runtime.getRuntime( ).exec(command);
            process.waitFor( );
            if (process.exitValue( ) != 0)
            {
                // TODO: CHANGE TO INDEXED MESSAGE
                Logger.msg("Abnormal process termination 1");
                return false;
            }

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream( )));
            String         line;
            switch (option)
            {
                case 0:
                    line = bufferedReader.readLine( );
                    do
                    {
                        if (null == line || line.trim( ).equals(Settings.CompareNatCommandResult)
                                || line.trim( ).equals(Settings.CompareExeCommandResult))
                        {
                            return true;
                        }
                    }
                    while ((line = bufferedReader.readLine( )) != null);
                break;

                case 1:
                    while ((line = bufferedReader.readLine( )) != null)
                    {
                        Logger.msg(line);
                    }
            }

            Scanner scanner = new Scanner(process.getInputStream( ));
            scanner.useDelimiter(Settings.delimiter);
            while (scanner.hasNext( ))
            {
                Logger.msg(scanner.next( ));
            }
            scanner.close( );

            bufferedReader = new BufferedReader(new InputStreamReader(process.getErrorStream( )));
            while ((line = bufferedReader.readLine( )) != null)
            {
                Logger.err(Strings.outputError + line);
            }
            bufferedReader.close( );

            process.destroy( );
        }
        catch (Exception e)
        {
            Logger.err("MSG_023: " + Strings.processRuntimeError + e);
        }
        return true;
    }

    public static void file(String text, String filename)
    {
        try
        {
            // Files.createFile(dummyPath);
            text = text + "\n";
            Files.write(Paths.get(filename), text.toLowerCase( ).getBytes( ), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        }
        catch (IOException e)
        {
            // TODO: REPLACE BY INDEXED MESSAGE
            e.printStackTrace( );
        }
    }
}
