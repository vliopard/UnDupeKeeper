package com.OTDSHCo;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class CheckSum
{
	private static final String	HEXES	="0123456789ABCDEF";
	private static String		method	="SHA1";

	public static void setMethod(String newMeth)
	{
		log(" Setting Checksum Method...");
		method=newMeth;
	}

	public static byte[] createChecksum(String filename)
	{
		log(" Creating Checksum...");
		InputStream fis;
		try
		{
			fis=new FileInputStream(filename);
			byte[] buffer=new byte[1024];
			MessageDigest complete=MessageDigest.getInstance(method);
			int numRead;
			do
			{
				numRead=fis.read(buffer);
				if(numRead>0)
				{
					complete.update(buffer,
									0,
									numRead);
				}
			}
			while(numRead!=-1);
			fis.close();
			return complete.digest();
		}
		catch(IOException|NoSuchAlgorithmException e)
		{
			log("!CheckSum Creation Failed: "+
				e);
			return null;
		}
	}

	public static String getChecksum(String filename)
	{
		log(" Getting CheckSum...");
		waitFile(filename);
		byte[] raw=createChecksum(filename);
		final StringBuilder hex=new StringBuilder(2*raw.length);
		for(final byte b : raw)
		{
			hex.append(HEXES.charAt((b&0xF0)>>4))
				.append(HEXES.charAt((b&0x0F)));
		}
		return hex.toString()
					.toUpperCase();
	}

	static void waitFile(String child)
	{
		boolean first=true;
		for(;;)
		{
			try
			{
				FileInputStream fi=new FileInputStream(new File(child));
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
			if(first)
			{
				msg("Waiting for file...");
			}
			first=false;
		}
	}

	private static void log(String logMessage)
	{
		Logger.log(	Thread.currentThread(),
					logMessage,
					Logger.TOOLS_CONVERT);
	}

	private static void msg(String msg)
	{
		Logger.msg(msg);
	}
}
