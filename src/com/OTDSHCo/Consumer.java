package com.OTDSHCo;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;

class Consumer	implements
				Runnable
{
	private final BlockingQueue<Integer>	stopSignal;
	private final BlockingQueue<FileQueue>	transferQueue;
	private long							included	=0;
	private long							replaced	=0;
	private static HashMap<String,String>	hashMapTable	=new HashMap<String,String>();

	Consumer(	BlockingQueue<FileQueue> fileQueue,
				BlockingQueue<Integer> signalQueue)
	{
		log(" Constructing Consumer...");
		transferQueue=fileQueue;
		stopSignal=signalQueue;
	}

	public void run()
	{
		hashMapTable=DataBase.loadMap();
		msg("Worker startup...");
		log(" Starting Consumer.");
		try
		{
			do
			{
				consume(transferQueue.take());
			}
			while(!stopSignal.contains(1));
		}
		catch(InterruptedException ex)
		{
			log("!Problem Running Consumer: "+
				ex);
		}
		log(" Ending Consumer.");
		save();
		msg("Worker shutdown...");
		try
		{
			stopSignal.put(2);
		}
		catch(InterruptedException e)
		{
			err("Cannot Send Exit Message.");
		}
	}

	public synchronized void save()
	{
		synchronized(this)
		{
			DataBase.saveMap(hashMapTable);
		}
	}

	public synchronized void clear()
	{
		synchronized(this)
		{
			DataBase.clear();
		}
	}

	public synchronized void load()
	{
		synchronized(this)
		{
			hashMapTable=DataBase.loadMap();
		}
	}

	private void consume(Object fileQueueObject)
	{
		log(" Consuming...");
		FileQueue fileQueue=(FileQueue)fileQueueObject;
		switch(fileQueue.getType())
		{
			case 1:
				log(" Including New File...");
				includeFileToHashTable(fileQueue.getPath());
			break;
			case 2:
				log(" Modifying Included File...");
			// TODO: Modify
			break;
			case 3:
				log(" Removing Included File...");
				replaceFileFromHashTable(fileQueue.getPath());
			break;
			case 4:
				log(" Renaming Included File...");
			// TODO: Rename
			break;
			case 5:
				log(" OVERFLOW!");
			// TODO: Overflow
			break;
			default:
		}
		log(" Consumed...");
	}

	private void includeFileToHashTable(String fileName)
	{
		if(new File(fileName).isFile())
		{
			log(" Start managing file.");
			try
			{
				String cypherMethod=CheckSum.getChecksum(fileName);
				if(!hashMapTable.containsKey(cypherMethod))
				{
					log(" Including new file...");
					included++;
					msg("["+
						addLeadingZeros(included)+
						"]["+
						addLeadingZeros(replaced)+
						"]\t["+
						cypherMethod+
						"]\tIncluding "+
						fileName);
					hashMapTable.put(	cypherMethod,
									fileName);
				}
				else
				{
					log(" Replacing file...");
					replaced++;
					msg("["+
						addLeadingZeros(included)+
						"]["+
						addLeadingZeros(replaced)+
						"]\t["+
						cypherMethod+
						"]\tReplacing "+
						fileName);
					File fileToRename=new File(fileName);
					Writer outputFile=new BufferedWriter(new FileWriter(fileToRename));
					outputFile.write(hashMapTable.get(cypherMethod)+
									" repeeKepuDnU{.-::![|@|]!::-.}UnDupeKeeper ["+
									cypherMethod+
									"]");
					outputFile.close();
					// CheckSum.waitFile(child);
					// f2.renameTo(new File(child + ".(Dup3K33p)"));
					Path fileNamePath=Paths.get(fileName);
					java.nio.file.Files.move(	fileNamePath,
												fileNamePath.resolveSibling(fileNamePath.getFileName()
																		.toString()+
																	".(Dup3K33p)"));
				}
			}
			catch(IOException e)
			{
				log("!Problem Including New File: "+
					e);
			}
			log(" File Managed.");
		}
	}

	private void replaceFileFromHashTable(String fileName)
	{
		if(hashMapTable.containsValue(fileName))
		{
			log(" Removing file.");
			msg("["+
				addLeadingZeros(included)+
				"]["+
				addLeadingZeros(replaced)+
				"]\tRemoving "+
				fileName);
			included--;
			hashMapTable.values()
						.remove(fileName);
			log(" File Removed.");
		}
	}

	private String addLeadingZeros(long numberToFormat)
	{
		return String.format(	"%04d",
								numberToFormat);
	}

	private static void log(String logMessage)
	{
		Logger.log(	Thread.currentThread(),
					logMessage,
					Logger.TOOLS_SUPERUSER);
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
