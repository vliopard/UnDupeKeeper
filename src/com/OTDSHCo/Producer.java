package com.OTDSHCo;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.BlockingQueue;

class Producer	implements
				Runnable
{
	private Path							d;
	private boolean							rec;
	private final BlockingQueue<FileQueue>	queue;

	Producer(	Path dir,
				boolean recursive,
				BlockingQueue<FileQueue> q)
	{
		log(" Constructing Producer.");
		d=dir;
		rec=recursive;
		queue=q;
	}

	public void run()
	{
		log(" Starting Producer.");
		try
		{
			new Monitor(d,
						rec,
						queue).processEvents();
			/*
			 * while(true)
			 * {
			 * queue.put(produce());
			 * }
			 */
		}
		catch(IOException ex)
		{
			log("!Problem Running Producer: "+
				ex);
		}
		log(" Ending Producer.");
	}

	static void log(String logMessage)
	{
		Logger.log(	Thread.currentThread(),
					logMessage,
					Logger.TOOLS_UTIL);
	}
}
