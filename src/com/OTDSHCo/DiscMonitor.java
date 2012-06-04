package com.OTDSHCo;
import java.util.concurrent.BlockingQueue;
import net.contentobjects.jnotify.JNotify;
import net.contentobjects.jnotify.JNotifyListener;

public class DiscMonitor
{
	private boolean							stopSignal	=false;
	private FileQueue						fq;
	private final BlockingQueue<FileQueue>	queue;

	DiscMonitor(String path,
				BlockingQueue<FileQueue> q,
				boolean recursive) throws Exception
	{
		queue=q;
		int mask=JNotify.FILE_CREATED|
					JNotify.FILE_DELETED|
					JNotify.FILE_MODIFIED|
					JNotify.FILE_RENAMED;
		boolean watchSubtree=recursive;
		int watchID=JNotify.addWatch(	path,
										mask,
										watchSubtree,
										new Listener());
		do
		{
			Thread.sleep(5000);
		}
		while(!stopSignal);
		boolean res=JNotify.removeWatch(watchID);
		if(!res)
		{
			log("!Invalid Watci ID Specified");
		}
		msg("Producer Thread Stoped!");
	}

	public void stop()
	{
		stopSignal=true;
	}

	class Listener	implements
					JNotifyListener
	{
		public void fileRenamed(int wd,
								String rootPath,
								String oldName,
								String newName)
		{
			log(" RN - Adding To Queue...");
			// print("renamed "+rootPath+"\\"+oldName+" -> "+newName);
		}

		public void fileModified(	int wd,
									String rootPath,
									String name)
		{
			log(" MD - Adding To Queue...");
			fq=new FileQueue();
			fq.set(	2,
					rootPath+
							"\\"+
							name);
			try
			{
				queue.put(fq);
			}
			catch(InterruptedException e)
			{
				log("!MD - Problem Adding To Queue: "+
					e);
			}
			log(" MD - Added To Queue...");
		}

		public void fileDeleted(int wd,
								String rootPath,
								String name)
		{
			log(" DL - Adding To Queue...");
			fq=new FileQueue();
			fq.set(	3,
					rootPath+
							"\\"+
							name);
			try
			{
				queue.put(fq);
			}
			catch(InterruptedException e)
			{
				log("!DL - Problem Adding to Queue: "+
					e);
			}
			log(" DL - Added To Queue...");
		}

		public void fileCreated(int wd,
								String rootPath,
								String name)
		{
			log(" CR - Adding To Queue...");
			fq=new FileQueue();
			fq.set(	1,
					rootPath+
							"\\"+
							name);
			try
			{
				queue.put(fq);
			}
			catch(InterruptedException e)
			{
				log("!CR - Problem Adding To Queue: "+
					e);
			}
			log(" CR - Added To Queue...");
		}
	}

	private static void log(String logMessage)
	{
		Logger.log(	Thread.currentThread(),
					logMessage,
					Logger.TOOLS_UTIL);
	}

	private static void msg(String msg)
	{
		Logger.msg(msg);
	}
}
