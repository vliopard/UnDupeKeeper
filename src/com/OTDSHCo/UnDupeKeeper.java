package com.OTDSHCo;
import java.nio.file.*;
import java.io.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class UnDupeKeeper
{
	private static BlockingQueue<FileQueue>	q;
	private static Monitor					m;

	static void usage()
	{
		System.err.println("usage: java WatchDir [-r] dir");
		System.exit(-1);
	}

	public static void main(String[] args) throws IOException
	{
		log(" Initializing UnDupeKeeper");
		if(args.length==0||
			args.length>2)
		{
			usage();
		}
		boolean recursive=false;
		int dirArg=0;
		if(args[0].equals("-r"))
		{
			if(args.length<2)
			{
				usage();
			}
			recursive=true;
			dirArg++;
		}
		Path dir=Paths.get(args[dirArg]);
		if((new File(dir.toString()).exists())&&
			(new File(dir.toString()).isDirectory()))
		{
			log(" Initializing Threads...");
			q=new LinkedBlockingQueue<FileQueue>();
			Producer p=new Producer(dir,
									recursive,
									q);
			Consumer c=new Consumer(q);
			log(" Producer Started...");
			new Thread(p).start();
			log(" Consumer Started...");
			new Thread(c).start();
		}
		else
		{
			log("Directory does not exist!");
		}
	}

	static void log(String logMessage)
	{
		Logger.log(	Thread.currentThread(),
					logMessage,
					Logger.MAIN_SOFTWARE);
	}
}
