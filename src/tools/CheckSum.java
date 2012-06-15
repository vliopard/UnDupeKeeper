package tools;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import settings.Settings;
import settings.Strings;

/**
 * 
 * @author vliopard
 */
public class CheckSum
{
    /**
     * 
     * @param cypherTypeMethod
     */
    public static void setMethod(int cypherTypeMethod)
    {
        Settings.CypherMethod=Settings.CypherMethodList[cypherTypeMethod];
    }

    /**
     * 
     * @param fileName
     * @return Returns a <code>byte array</code> that contains the encrypted
     *         representation
     *         of a file.
     */
    public static byte[] createChecksum(String fileName)
    {
        InputStream fileInputStream;
        try
        {
            fileInputStream=new FileInputStream(fileName);
            byte[] byteBuffer=new byte[1024];
            MessageDigest messageDigest=MessageDigest.getInstance(Settings.CypherMethod);
            int numberRead=0;
            while((numberRead=fileInputStream.read(byteBuffer))!=-1)
            {
                messageDigest.update(byteBuffer,
                                     0,
                                     numberRead);
            }
            fileInputStream.close();
            return messageDigest.digest();
        }
        catch(IOException|NoSuchAlgorithmException e)
        {
            log(Strings.csChecksumCreationFailed+
                e);
            return null;
        }
    }

    /**
     * 
     * @param fileName
     * @return Returns a <code>String</code> containing the encrypted
     *         representation of a
     *         file.
     */
    public static String getChecksumSimple(String fileName)
    {
        waitForFile(fileName);
        byte[] rawBytes=createChecksum(fileName);
        // String result="";
        StringBuffer resultBuffer=new StringBuffer();
        for(int i=0; i<rawBytes.length; i++)
        {
            // result+=Integer.toString((rawBytes[i]&0xff)+0x100,16).substring(1);
            // resultBuffer.append(Integer.toString((rawBytes[i]&0xff)+0x100,16).substring(1));
            resultBuffer.append(Integer.toHexString(0xFF&rawBytes[i]));
        }
        // return result.toUpperCase();
        return resultBuffer.toString()
                           .toUpperCase();
    }

    /**
     * 
     * @param fileName
     * @return Returns a <code>String</code> containing the encrypted
     *         representation of a
     *         file.
     * @throws UnsupportedEncodingException
     */
    public static String getChecksumFaster(String fileName) throws UnsupportedEncodingException
    {
        waitForFile(fileName);
        byte[] rawBytes=createChecksum(fileName);
        byte[] hex=new byte[2*rawBytes.length];
        int index=0;
        for(byte b : rawBytes)
        {
            int v=b&0xFF;
            hex[index++]=Settings.HEX_CHAR_TABLE[v>>>4];
            hex[index++]=Settings.HEX_CHAR_TABLE[v&0xF];
        }
        return new String(hex,
                          "ASCII").toUpperCase();
    }

    /**
     * 
     * @param fileName
     * @return Returns a <code>String</code> containing the encrypted
     *         representation of a
     *         file.
     */
    public static String getChecksumElegant(String fileName)
    {
        waitForFile(fileName);
        byte[] rawBytes=createChecksum(fileName);
        final StringBuilder hexString=new StringBuilder(2*rawBytes.length);
        for(final byte b : rawBytes)
        {
            hexString.append(Settings.HexHashValues.charAt((b&0xF0)>>4))
                     .append(Settings.HexHashValues.charAt((b&0x0F)));
        }
        return hexString.toString()
                        .toUpperCase();
    }

    /**
     * 
     * @param password
     * @return Returns a <code>String</code> containing the encrypted
     *         representation of a
     *         password.
     */
    public static String encryptPassword(String password)
    {
        String cypherSha1Method=null;
        try
        {
            MessageDigest cryptMessageDigest=MessageDigest.getInstance("SHA-1");
            cryptMessageDigest.reset();
            cryptMessageDigest.update(password.getBytes("UTF-8"));
            cypherSha1Method=byteToHexFormater(cryptMessageDigest.digest());
        }
        catch(NoSuchAlgorithmException|UnsupportedEncodingException e)
        {
            log(Strings.csPasswordEncryptionFailed+
                e);
        }
        return cypherSha1Method;
    }

    /**
     * 
     * @param hashBytes
     * @return Returns a <code>String</code> containing the encrypted
     *         representation of a <code>byte array</code>.
     */
    private static String byteToHexFormater(final byte[] hashBytes)
    {
        Formatter formatter=new Formatter();
        for(byte byteLoop : hashBytes)
        {
            formatter.format("%02x",
                             byteLoop);
        }
        return formatter.toString();
    }

    /**
     * 
     * @param byteData
     * @return Returns a <code>String</code> containing the encrypted
     *         representation of a <code>byte array</code>.
     */
    @SuppressWarnings("unused")
    private static String byteToHex(final byte[] byteData)
    {
        StringBuffer hexString=new StringBuffer();
        for(int i=0; i<byteData.length; i++)
        {
            String hex=Integer.toHexString(0xff&byteData[i]);
            if(hex.length()==1)
                hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString()
                        .toUpperCase();
    }

    /**
     * 
     * @param fileName
     */
    public static void waitForFile(String fileName)
    {
        boolean reachedFirstTime=true;
        for(;;)
        {
            try
            {
                File file=new File(fileName);
                FileInputStream fileInputStream=new FileInputStream(file);
                if(fileInputStream.available()==file.length())
                {
                    fileInputStream.close();
                    fileInputStream=null;
                    return;
                }
                Thread.sleep(Settings.WaitForFileTimeOut);
            }
            catch(IOException|InterruptedException e)
            {
                // Waiting for file. Does not need to track here.
            }
            if(reachedFirstTime)
            {
                log(Strings.csWaitingForFile+
                    " ["+
                    fileName+
                    "]");
            }
            reachedFirstTime=false;
        }
    }

    /**
     * 
     * @param logMessage
     */
    private static void log(String logMessage)
    {
        Logger.log(Thread.currentThread(),
                   logMessage,
                   Logger.CHECKSUM);
    }
}
