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
	private static HashMap<String,String>	productMap	=new HashMap<String,String>();

	Consumer(	BlockingQueue<FileQueue> q,
				BlockingQueue<Integer> r)
	{
		log(" Constructing Consumer...");
		transferQueue=q;
		stopSignal=r;
	}

	public void run()
	{
		productMap=DataBase.loadMap();
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
			DataBase.saveMap(productMap);
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
			productMap=DataBase.loadMap();
		}
	}

	private void consume(Object x)
	{
		log(" Consuming...");
		FileQueue fq=(FileQueue)x;
		switch(fq.getType())
		{
			case 1:
				log(" Including New File...");
				manage_file_new(fq.getPath());
			break;
			case 2:
				log(" Modifying Included File...");
			// TODO: Modify
			break;
			case 3:
				log(" Removing Included File...");
				manage_file_old(fq.getPath());
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

	private void manage_file_new(String child)
	{
		if(new File(child).isFile())
		{
			log(" Start managing file.");
			try
			{
				String md5=CheckSum.getChecksum(child);
				if(!productMap.containsKey(md5))
				{
					log(" Including new file...");
					included++;
					msg("["+
						zero(included)+
						"]["+
						zero(replaced)+
						"]\t["+
						md5+
						"]\tIncluding "+
						child);
					productMap.put(	md5,
									child);
				}
				else
				{
					log(" Replacing file...");
					replaced++;
					msg("["+
						zero(included)+
						"]["+
						zero(replaced)+
						"]\t["+
						md5+
						"]\tReplacing "+
						child);
					File f2=new File(child);
					Writer output=new BufferedWriter(new FileWriter(f2));
					output.write(productMap.get(md5)+
									" repeeKepuDnU{.-::![|@|]!::-.}UnDupeKeeper ["+
									md5+
									"]");
					output.close();
					// CheckSum.waitFile(child);
					// f2.renameTo(new File(child + ".(Dup3K33p)"));
					Path dir=Paths.get(child);
					java.nio.file.Files.move(	dir,
												dir.resolveSibling(dir.getFileName()
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

	private void manage_file_old(String child)
	{
		if(productMap.containsValue(child))
		{
			log(" Removing file.");
			msg("["+
				zero(included)+
				"]["+
				zero(replaced)+
				"]\tRemoving "+
				child);
			included--;
			productMap.values()
						.remove(child);
			log(" File Removed.");
		}
	}

	private String zero(long msg)
	{
		return String.format(	"%04d",
								msg);
	}

	private static void log(String logMessage)
	{
		Logger.log(	Thread.currentThread(),
					logMessage,
					Logger.TOOLS_SUPERUSER);
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
