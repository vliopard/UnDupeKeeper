package com.OTDSHCo;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

public class DataBase
{
	private static String	databaseName	="OTDSHCo_UnDupeKeeper_Database.dbn";

	static void saveMap(HashMap<String,String> productMap)
	{
		msg("Start Saving Map.");
		try
		{
			ObjectOutputStream objOut=new ObjectOutputStream(new FileOutputStream(databaseName));
			objOut.writeObject(productMap);
			objOut.close();
			msg("Database Saved!");
		}
		catch(IOException e)
		{
			log("!Problems To Save Map: "+
				e);
		}
		msg("Finished Saving Map.");
	}

	static HashMap<String,String> loadMap()
	{
		if(new File(databaseName).exists())
		{
			log(" Creating Database...");
			try
			{
				ObjectInputStream objIn=new ObjectInputStream(new FileInputStream(databaseName));
				return (HashMap<String,String>)objIn.readObject();
			}
			catch(ClassNotFoundException|IOException e)
			{
				log("!Problems During Database Creation: "+
					e);
			}
		}
		msg("WARINIG: New Database Created!");
		return new HashMap<String,String>();
	}

	static void log(String logMessage)
	{
		Logger.log(	Thread.currentThread(),
					logMessage,
					Logger.TOOLS_PACKAGE);
	}

	static void msg(String msg)
	{
		Logger.msg(msg);
	}
}
