package com.OTDSHCo;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;

class Consumer	implements
				Runnable
{
	private final BlockingQueue<?>			queue;
	private long							included	=0;
	private long							replaced	=0;
	private static HashMap<String,String>	productMap	=new HashMap<String,String>();

	Consumer(BlockingQueue<?> q)
	{
		log(" Constructing Consumer...");
		queue=q;
	}

	public void run()
	{
		log(" Starting Consumer.");
		try
		{
			while(true)
			{
				consume(queue.take());
			}
		}
		catch(InterruptedException ex)
		{
			log("!Problem Running Consumer: "+
				ex);
		}
		log(" Ending Consumer.");
	}

	void consume(Object x)
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
						included+
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
						replaced+
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
				included+
				"]\tRemoving "+
				child);
			included--;
			productMap.values()
						.remove(child);
			log(" File Removed.");
		}
	}

	static void log(String logMessage)
	{
		Logger.log(	Thread.currentThread(),
					logMessage,
					Logger.TOOLS_SUPERUSER);
	}

	static void msg(String msg)
	{
		Logger.msg(msg);
	}
}
