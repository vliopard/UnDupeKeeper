package tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

import settings.Settings;
import settings.Strings;

/**
 * CheckSum class is responsible for giving checksum results according to the selected encryption algorithm.
 * 
 * @author vliopard
 */
public class CheckSum
{
    /**
     * This method uses <code>Settings.CypherMethodList[]</code> to set the encryption method for calculating checksum
     * results.
     * 
     * @param cypherTypeMethod
     *                             An <code>int</code> value index for the desired algorithm.
     */
    public static void setMethod(int cypherTypeMethod)
    {
        Settings.CypherMethod = Settings.CypherMethodList[cypherTypeMethod];
    }

    /**
     * This method creates a checksum <code>byte array</code> from a given file name.
     * 
     * @param fileName
     *                     A <code>String</code> containing a path to the file to be calculated.
     * 
     * @return Returns a <code>byte array</code> that contains the encrypted representation of a file.
     */
    public static byte[ ] createChecksum(Path fileName)
    {
        InputStream fileInputStream;
        try
        {
            fileInputStream = new FileInputStream(fileName.toString( ));
            byte[ ]       byteBuffer    = new byte[1024];
            MessageDigest messageDigest = MessageDigest.getInstance(Settings.CypherMethod);
            int           numberRead    = 0;
            while ((numberRead = fileInputStream.read(byteBuffer)) != -1)
            {
                messageDigest.update(byteBuffer, 0, numberRead);
            }
            fileInputStream.close( );
            return messageDigest.digest( );
        }
        catch (IOException | NoSuchAlgorithmException e)
        {
            Logger.err("MSG_020: " + Strings.csChecksumCreationFailed + e);
            return null;
        }
    }

    public static String getBestChecksum(Path fileName)
    {
/*
        long fsize = 0;
        try
        {
            fsize = Files.size(fileName);
        }
        catch (IOException e)
        {
            e.printStackTrace( );
        }
        if (fsize < 5000000)
        {
            return getChecksumSimple(fileName);
        }
        if (fsize >= 5000000 && fsize < 350000000)
        {
            return getChecksumFaster(fileName);
        }
*/
        return getChecksumElegant(fileName);
    }

    /**
     * This method creates a <code>String</code> checksum value from a given filename through a simple method
     * calculation.
     * 
     * @param fileName
     *                     A <code>String</code> containing a path to the file to be calculated.
     * 
     * @return Returns a <code>String</code> containing the encrypted representation of a file.
     */
    public static String getChecksumSimple(Path fileName)
    {
        waitForFile(fileName);
        byte[ ] rawBytes = createChecksum(fileName);
        // String result="";
        StringBuffer resultBuffer = new StringBuffer( );
        for (int i = 0; i < rawBytes.length; i++)
        {
            // result+=Integer.toString((rawBytes[i]&0xff)+0x100,16).substring(1);
            // resultBuffer.append(Integer.toString((rawBytes[i]&0xff)+0x100,16).substring(1));
            resultBuffer.append(Integer.toHexString(0xFF & rawBytes[i]));
        }
        // return result.toUpperCase();
        return resultBuffer.toString( ).toUpperCase( );
    }

    /**
     * This method creates a <code>String</code> checksum value from a given filename through a faster method
     * calculation.
     * 
     * @param fileName
     *                     A <code>String</code> containing a path to the file to be calculated.
     * 
     * @return Returns a <code>String</code> containing the encrypted representation of a file.
     * 
     * @throws UnsupportedEncodingException
     *                                          This exception will be raised in case the return value could not be
     *                                          converted to the ASCII.
     */
    public static String getChecksumFaster(Path fileName)
    {
        waitForFile(fileName);
        byte[ ] rawBytes = createChecksum(fileName);
        byte[ ] hex      = new byte[2 * rawBytes.length];
        int     index    = 0;
        for (byte b:rawBytes)
        {
            int v = b & 0xFF;
            hex[index++] = Settings.HEX_CHAR_TABLE[v >>> 4];
            hex[index++] = Settings.HEX_CHAR_TABLE[v & 0xF];
        }
        String retval = null;
        try
        {
            retval = new String(hex, "ASCII").toUpperCase( );
        }
        catch (UnsupportedEncodingException e)
        {
            // TODO: CREATE INDEXED ERROR MESSAGE
            log("UnsupportedEncodingException:" + e);
            e.printStackTrace( );
        }
        return retval;
    }

    /**
     * This method creates a <code>String</code> checksum value from a given filename through an elegant method
     * calculation.
     * 
     * @param fileName
     *                     A <code>String</code> containing a path to the file to be calculated.
     * 
     * @return Returns a <code>String</code> containing the encrypted representation of a file.
     */
    public static String getChecksumElegant(Path fileName)
    {
        waitForFile(fileName);
        byte[ ]             rawBytes  = createChecksum(fileName);
        final StringBuilder hexString = new StringBuilder(2 * rawBytes.length);
        for (final byte b:rawBytes)
        {
            hexString.append(Settings.HexHashValues.charAt((b & 0xF0) >> 4)).append(Settings.HexHashValues.charAt((b
                    & 0x0F)));
        }
        return hexString.toString( ).toUpperCase( );
    }

    /**
     * This method creates a <code>String</code> checksum value from a given password.
     * 
     * @param password
     *                     A <code>String</code> containing the password to be calculated.
     * 
     * @return Returns a <code>String</code> containing the encrypted representation of a password.
     */
    public static String encryptPassword(String password)
    {
        String cypherSha1Method = null;
        try
        {
            MessageDigest cryptMessageDigest = MessageDigest.getInstance("SHA-1");
            cryptMessageDigest.reset( );
            cryptMessageDigest.update(password.getBytes("UTF-8"));
            cypherSha1Method = byteToHexFormater(cryptMessageDigest.digest( ));
        }
        catch (NoSuchAlgorithmException | UnsupportedEncodingException e)
        {
            Logger.err("MSG_021: " + Strings.csPasswordEncryptionFailed + e);
        }
        return cypherSha1Method;
    }

    /**
     * This method converts a <code>byte array</code> to a String value using a formatter method.
     * 
     * @param hashBytes
     *                      A <code>byte array</code> containing an hex value.
     * 
     * @return Returns a <code>String</code> containing the encrypted representation of a <code>byte array</code>.
     */
    private static String byteToHexFormater(final byte[ ] hashBytes)
    {
        Formatter formatter = new Formatter( );
        for (byte byteLoop:hashBytes)
        {
            formatter.format("%02x", byteLoop);
        }
        String result = formatter.toString( );
        formatter.close( );
        return result;
    }

    /**
     * This method converts a <code>byte array</code> to a String value using a <code>StringBuffer</code> method.
     * 
     * @param byteData
     *                     A <code>byte array</code> containing an hex value.
     * 
     * @return Returns a <code>String</code> containing the encrypted representation of a <code>byte array</code>.
     */
    @SuppressWarnings("unused")
    private static String byteToHex(final byte[ ] byteData)
    {
        StringBuffer hexString = new StringBuffer( );
        for (int i = 0; i < byteData.length; i++)
        {
            String hex = Integer.toHexString(0xff & byteData[i]);
            if (hex.length( ) == 1)
            {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString( ).toUpperCase( );
    }

    /**
     * This method waits for a file until it gets available to be read.
     * 
     * @param fileName
     *                     A <code>String</code> containing a path to the file to wait for.
     */
    public static void waitForFile(Path fileName)
    {
        boolean reachedFirstTime = true;
        for (;;)
        {
            try
            {
                File            file            = new File(fileName.toString( ));
                FileInputStream fileInputStream = new FileInputStream(file);
                if (fileInputStream.available( ) == file.length( ))
                {
                    fileInputStream.close( );
                    fileInputStream = null;
                    return;
                }
                fileInputStream.close( );
                fileInputStream = null;
                Thread.sleep(Settings.WaitForFileTimeOut);
            }
            catch (IOException | InterruptedException e)
            {
                // Waiting for file. Does not need to track here.
            }
            if (reachedFirstTime)
            {
                log(Strings.csWaitingForFile + " [" + fileName + "]");
            }
            reachedFirstTime = false;
        }
    }

    /**
     * This method displays a log message through the embedded log system.
     * 
     * @param logMessage
     *                       A <code>String</code> containing the log message to display.
     */
    private static void log(String logMessage)
    {
        Logger.log(Thread.currentThread( ), logMessage, Logger.CHECKSUM);
    }
}
