package com.OTDSHCo;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

public class DataBase
{
	private static String	directoryName	="UnDupeKeeper.dir";
	private static String	databaseName	="UnDupeKeeper.hdb";

	public static void clear()
	{
		msg("Erasing database...");
		HashMap<String,String> pm=new HashMap<String,String>();
		saveMap(pm);
	}

	public static void saveMap(HashMap<String,String> productMap)
	{
		msg("Saving database...");
		try
		{
			ObjectOutputStream objOut=new ObjectOutputStream(new FileOutputStream(databaseName));
			msg("Database contains "+
				productMap.size()+
				" items.");
			objOut.writeObject(productMap);
			objOut.close();
			objOut=null;
			msg("Database saved...");
		}
		catch(IOException e)
		{
			log("!Problems To Save Map: "+
				e);
		}
	}

	public static HashMap<String,String> loadMap()
	{
		if(new File(databaseName).exists())
		{
			msg("Loading database...");
			try
			{
				HashMap<String,String> hm=(HashMap<String,String>)new ObjectInputStream(new FileInputStream(databaseName)).readObject();
				msg("Database contains "+
					hm.size()+
					" items.");
				return hm;
			}
			catch(ClassNotFoundException|IOException e)
			{
				log("!Problems During Database Creation: "+
					e);
			}
		}
		msg("WARNING: New database created!");
		return new HashMap<String,String>();
	}

	public static void saveDir(String dir)
	{
		try
		{
			ObjectOutputStream objOut=new ObjectOutputStream(new FileOutputStream(directoryName));
			objOut.writeObject(dir);
			objOut.close();
			objOut=null;
		}
		catch(IOException e)
		{
			log("!Problems To Save Dir: "+
				e);
		}
	}

	public static String loadDir()
	{
		if(new File(directoryName).exists())
		{
			try
			{
				String hm=(String)new ObjectInputStream(new FileInputStream(directoryName)).readObject();
				return hm;
			}
			catch(ClassNotFoundException|IOException e)
			{
				log("!Problems During Settings Storage: "+
					e);
			}
		}
		return null;
	}

	private static void log(String logMessage)
	{
		Logger.log(	Thread.currentThread(),
					logMessage,
					Logger.TOOLS_PACKAGE);
	}

	private static void msg(String msg)
	{
		Logger.msg(msg);
	}

	private static void err(String msg)
	{
		Logger.err(msg);
	}
}
