package com.OTDSHCo;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

public class CheckSum
{
	private static final String	hexValues		="0123456789ABCDEF";
	private static String		cypherMethos	="SHA1";

	public static void setMethod(String cypherTypeMethod)
	{
		log(" Setting Checksum Method...");
		cypherMethos=cypherTypeMethod;
	}

	public static byte[] createChecksum(String fileName)
	{
		log(" Creating Checksum...");
		InputStream fileInputStream;
		try
		{
			fileInputStream=new FileInputStream(fileName);
			byte[] byteBuffer=new byte[1024];
			MessageDigest messageDigest=MessageDigest.getInstance(cypherMethos);
			int numberRead;
			do
			{
				numberRead=fileInputStream.read(byteBuffer);
				if(numberRead>0)
				{
					messageDigest.update(	byteBuffer,
											0,
											numberRead);
				}
			}
			while(numberRead!=-1);
			fileInputStream.close();
			return messageDigest.digest();
		}
		catch(IOException|NoSuchAlgorithmException e)
		{
			log("!CheckSum Creation Failed: "+
				e);
			return null;
		}
	}

	public static String getChecksum(String fileName)
	{
		log(" Getting CheckSum...");
		waitFile(fileName);
		byte[] rawBytes=createChecksum(fileName);
		final StringBuilder hexString=new StringBuilder(2*rawBytes.length);
		for(final byte b : rawBytes)
		{
			hexString.append(hexValues.charAt((b&0xF0)>>4))
						.append(hexValues.charAt((b&0x0F)));
		}
		return hexString.toString()
						.toUpperCase();
	}

	private static String encryptPassword(String password)
	{
		String cypherSha1Method="";
		try
		{
			MessageDigest cryptMessageDigest=MessageDigest.getInstance("SHA-1");
			cryptMessageDigest.reset();
			cryptMessageDigest.update(password.getBytes("UTF-8"));
			cypherSha1Method=byteToHex(cryptMessageDigest.digest());
		}
		catch(NoSuchAlgorithmException|UnsupportedEncodingException e)
		{
			log("!encryptPassword Failed: "+
				e);
		}
		return cypherSha1Method;
	}

	private static String byteToHex(final byte[] hashBytes)
	{
		Formatter formatter=new Formatter();
		for(byte b : hashBytes)
		{
			formatter.format(	"%02x",
								b);
		}
		return formatter.toString();
	}

	public static void waitFile(String fileName)
	{
		boolean reachedFirstTime=true;
		for(;;)
		{
			try
			{
				FileInputStream fi=new FileInputStream(new File(fileName));
				if(fi.available()>0)
				{
					fi.close();
					fi=null;
					return;
				}
				Thread.sleep(50);
			}
			catch(IOException|InterruptedException e)
			{
				// Waiting for file. Does not need to track here.
			}
			if(reachedFirstTime)
			{
				msg("Waiting for file...");
			}
			reachedFirstTime=false;
		}
	}

	private static void log(String logMessage)
	{
		Logger.log(	Thread.currentThread(),
					logMessage,
					Logger.TOOLS_CONVERT);
	}

	private static void msg(String message)
	{
		Logger.msg(message);
	}
}
