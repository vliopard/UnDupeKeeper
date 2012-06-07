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
		HashMap<String,String> hashMapToClear=new HashMap<String,String>();
		saveMap(hashMapToClear);
	}

	public static void saveMap(HashMap<String,String> hashMapToSave)
	{
		msg("Saving database...");
		try
		{
			ObjectOutputStream hashMapObjectOutput=new ObjectOutputStream(new FileOutputStream(databaseName));
			msg("Database contains "+
				hashMapToSave.size()+
				" items.");
			hashMapObjectOutput.writeObject(hashMapToSave);
			hashMapObjectOutput.close();
			hashMapObjectOutput=null;
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
				HashMap<String,String> hashMapToLoad=(HashMap<String,String>)new ObjectInputStream(new FileInputStream(databaseName)).readObject();
				msg("Database contains "+
					hashMapToLoad.size()+
					" items.");
				return hashMapToLoad;
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

	public static void saveDir(String folderName)
	{
		try
		{
			ObjectOutputStream directoryToSaveOutput=new ObjectOutputStream(new FileOutputStream(directoryName));
			directoryToSaveOutput.writeObject(folderName);
			directoryToSaveOutput.close();
			directoryToSaveOutput=null;
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
				return (String)new ObjectInputStream(new FileInputStream(directoryName)).readObject();
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

	private static void msg(String message)
	{
		Logger.msg(message);
	}

	private static void err(String errorMessage)
	{
		Logger.err(errorMessage);
	}
}
