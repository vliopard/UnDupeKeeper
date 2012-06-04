package com.OldBackup;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import com.OTDSHCo.FileQueue;
import com.OTDSHCo.Logger;

public class Monitor
{
	private int								director	=0;
	private final boolean					recursive;
	private final WatchService				watcher;
	private final Map<WatchKey,Path>		keys;
	private boolean							trace		=false;
	private String							state		=null;
	private final BlockingQueue<FileQueue>	queue;
	private FileQueue						fq;

	@SuppressWarnings("unchecked")
	static <T> WatchEvent<T> cast(WatchEvent<?> event)
	{
		log(" WatchEvent Started...");
		return (WatchEvent<T>)event;
	}

	private void register(Path dir) throws IOException
	{
		log(" Register Dir Started.");
		WatchKey key=dir.register(	watcher,
									ENTRY_CREATE,
									ENTRY_DELETE,
									ENTRY_MODIFY);
		if(trace)
		{
			Path prev=keys.get(key);
			if(prev==null)
			{
				director++;
				log("["+
					director+
					"]\tRegister "+
					dir);
			}
			else
			{
				if(!dir.equals(prev))
				{
					log("Updating "+
						prev+
						" -> "+
						dir);
				}
			}
		}
		keys.put(	key,
					dir);
		log(" Register Dir Ended.");
	}

	private void registerAll(final Path start) throws IOException
	{
		log(" Register All Started.");
		Files.walkFileTree(	start,
							new SimpleFileVisitor<Path>()
								{
									@Override
									public FileVisitResult preVisitDirectory(	Path dir,
																				BasicFileAttributes attrs) throws IOException
									{
										register(dir);
										return FileVisitResult.CONTINUE;
									}
								});
		log(" Register All Ended...");
	}

	Monitor(Path dir,
			boolean recursive,
			BlockingQueue<FileQueue> q) throws IOException
	{
		log(" Monitor Started.");
		this.queue=q;
		this.watcher=FileSystems.getDefault()
								.newWatchService();
		this.keys=new HashMap<WatchKey,Path>();
		this.recursive=recursive;
		if(recursive)
		{
			log(" Scanning Directories...");
			log("Scanning "+
				dir+
				"...");
			registerAll(dir);
		}
		else
		{
			log(" Registering Dir...");
			register(dir);
		}
		this.trace=true;
		log(" Monitor Ended.");
	}

	void processEvents()
	{
		log(" Processor Started...");
		for(int i=0; i<1;)
		{
			WatchKey key;
			try
			{
				key=watcher.take();
			}
			catch(InterruptedException x)
			{
				return;
			}
			Path dir=keys.get(key);
			if(dir==null)
			{
				System.err.println("WatchKey not recognized!!");
				continue;
			}
			for(WatchEvent<?> event : key.pollEvents())
			{
				WatchEvent.Kind<?> kind=event.kind();
				if(kind==OVERFLOW)
				{
					log("!OVERFLOW");
					continue;
				}
				WatchEvent<Path> ev=cast(event);
				Path name=ev.context();
				Path child=dir.resolve(name);
				if(event.kind()
						.name()
						.equals("ENTRY_CREATE"))
				{
					if(child.toString()
							.endsWith("stopUnDupeKeeperRightNow"))
					{
						log(" Ending Service. Good Bye!");
						i=1;
						break;
					}
					state="ENTRY_CREATE";
					log(" CR - Adding To Queue...");
					fq=new FileQueue();
					fq.set(	1,
							child.toString());
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
				if(event.kind()
						.name()
						.equals("ENTRY_MODIFY"))
				{
					if(state!=null&&
						state.equals("ENTRY_CREATE"))
					{
						state="ENTRY_MODIFY";
						log(" MD - Adding To Queue...");
						fq=new FileQueue();
						fq.set(	2,
								child.toString());
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
				}
				if(event.kind()
						.name()
						.equals("ENTRY_DELETE"))
				{
					log(" DL - Adding To Queue...");
					fq=new FileQueue();
					fq.set(	3,
							child.toString());
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
				if(recursive&&
					(kind==ENTRY_CREATE))
				{
					try
					{
						if(Files.isDirectory(	child,
												NOFOLLOW_LINKS))
						{
							log(" New Directory Found...");
							registerAll(child);
							log(" New Directory Mapped...");
						}
					}
					catch(IOException x)
					{
					}
				}
			}
			boolean valid=key.reset();
			if(!valid)
			{
				keys.remove(key);
				if(keys.isEmpty())
				{
					break;
				}
			}
		}
	}

	static void log(String logMessage)
	{
		Logger.log(	Thread.currentThread(),
					logMessage,
					Logger.TOOLS_STATUS);
	}
}
