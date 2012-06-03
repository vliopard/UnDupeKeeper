package com.OTDSHCo;
import java.nio.file.*;
import static java.nio.file.StandardWatchEventKinds.*;
import static java.nio.file.LinkOption.*;
import java.nio.file.attribute.*;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class WatchDir
{
	private final WatchService				watcher;
	private final Map<WatchKey,Path>		keys;
	private final boolean					recursive;
	private long							included		=0;
	private long							replaced		=0;
	private long							director		=0;
	private String							state			=null;
	private boolean							trace			=false;
	private static String					databaseName	="OTDSHCo_UnDupeKeeper_Database.dbn";
	private static HashMap<String,String>	productMap		=new HashMap<String,String>();
	private static final String				HEXES			="0123456789ABCDEF";

	public static byte[] createChecksum(String filename)
	{
		InputStream fis;
		try
		{
			fis=new FileInputStream(filename);
			byte[] buffer=new byte[1024];
			MessageDigest complete=MessageDigest.getInstance("SHA1");
			int numRead;
			do
			{
				numRead=fis.read(buffer);
				if(numRead>0)
				{
					complete.update(buffer,
									0,
									numRead);
				}
			}
			while(numRead!=-1);
			fis.close();
			return complete.digest();
		}
		catch(IOException|NoSuchAlgorithmException e)
		{
			log("!CATCH "+
				e);
			return null;
		}
	}

	public static String getChecksum(String filename)
	{
		waitFile(filename);
		byte[] raw=createChecksum(filename);
		final StringBuilder hex=new StringBuilder(2*raw.length);
		for(final byte b : raw)
		{
			hex.append(HEXES.charAt((b&0xF0)>>4))
				.append(HEXES.charAt((b&0x0F)));
		}
		return hex.toString()
					.toUpperCase();
	}

	@SuppressWarnings("unchecked")
	static <T> WatchEvent<T> cast(WatchEvent<?> event)
	{
		return (WatchEvent<T>)event;
	}

	private void register(Path dir) throws IOException
	{
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
	}

	private void registerAll(final Path start) throws IOException
	{
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
	}

	WatchDir(	Path dir,
				boolean recursive) throws IOException
	{
		this.watcher=FileSystems.getDefault()
								.newWatchService();
		this.keys=new HashMap<WatchKey,Path>();
		this.recursive=recursive;
		if(recursive)
		{
			log("Scanning "+
				dir+
				"...");
			registerAll(dir);
		}
		else
		{
			register(dir);
		}
		this.trace=true;
	}

	void processEvents()
	{
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
						i=1;
						break;
					}
					state="ENTRY_CREATE";
					manage_file_new(child);
				}
				if(event.kind()
						.name()
						.equals("ENTRY_MODIFY"))
				{
					if(state!=null&&
						state.equals("ENTRY_CREATE"))
					{
						state="ENTRY_MODIFY";
						//log("Modified: "+child);
						// manage_file_new(child);
					}
				}
				if(event.kind()
						.name()
						.equals("ENTRY_DELETE"))
				{
					manage_file_old(child);
				}
				if(recursive&&
					(kind==ENTRY_CREATE))
				{
					try
					{
						if(Files.isDirectory(	child,
												NOFOLLOW_LINKS))
						{
							registerAll(child);
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

	static void waitFile(String child)
	{
		boolean first=true;
		for(;;)
		{
			try
			{
				FileInputStream fi=new FileInputStream(new File(child));
				if(fi.available()>0)
				{
					fi.close();
					fi=null;
					return;
				}
				Thread.sleep(50);
			}
			catch(IOException|InterruptedException e)
			{
				log("!CATCH "+
					e);
			}
			if(first)
			{
				log("Waiting for file...");
			}
			first=false;
		}
	}

	void manage_file_new(Path child)
	{
		if(new File(child.toString()).isFile())
		{
			try
			{
				String md5=getChecksum(child.toString());
				if(!productMap.containsKey(md5))
				{
					included++;
					log("["+
						included+
						"]\t["+
						md5+
						"]\tIncluding "+
						child.toString());
					productMap.put(	md5,
									child.toString());
				}
				else
				{
					replaced++;
					log("["+
						replaced+
						"]\t["+
						md5+
						"]\tReplacing "+
						child.toString());
					File f2=new File(child.toString());
					Writer output=new BufferedWriter(new FileWriter(f2));
					output.write(productMap.get(md5)+
									" repeeKepuDnU{.-::![|MD5|]!::-.}UnDupeKeeper ["+
									md5+
									"]");
					output.close();
				}
			}
			catch(IOException e)
			{
				log("! CATCH "+
					e);
			}
		}
	}

	void manage_file_old(Path child)
	{
		if(productMap.containsValue(child.toString()))
		{
			log("["+
				included+
				"]\tRemoving "+
				child.toString());
			included--;
			productMap.values()
						.remove(child.toString());
		}
	}

	static void saveMap()
	{
		try
		{
			ObjectOutputStream objOut=new ObjectOutputStream(new FileOutputStream(databaseName));
			objOut.writeObject(productMap);
			objOut.close();
			log("Database Saved!");
		}
		catch(IOException e)
		{
			log("! CATCH "+
				e);
		}
	}

	static void loadMap()
	{
		if(new File(databaseName).exists())
		{
			try
			{
				ObjectInputStream objIn=new ObjectInputStream(new FileInputStream(databaseName));
				productMap=(HashMap<String,String>)objIn.readObject();
			}
			catch(ClassNotFoundException|IOException e)
			{
				log("! CATCH "+
					e);
			}
		}
	}

	static void usage()
	{
		System.err.println("usage: java WatchDir [-r] dir");
		System.exit(-1);
	}

	public static void main(String[] args) throws IOException
	{
		if(args.length==0||
			args.length>2)
			usage();
		boolean recursive=false;
		int dirArg=0;
		if(args[0].equals("-r"))
		{
			if(args.length<2)
				usage();
			recursive=true;
			dirArg++;
		}
		Path dir=Paths.get(args[dirArg]);
		if((new File(dir.toString()).exists())&&
			(new File(dir.toString()).isDirectory()))
		{
			loadMap();
			if(null==productMap)
			{
				log("WARNING: DATABASE IS NEW!");
				productMap=new HashMap<String,String>();
			}
			new WatchDir(	dir,
							recursive).processEvents();
			log("SAVING DATABASE FOR LATER USE");
			saveMap();
		}
		else
		{
			log("Directory does not exist!");
		}
	}

	static void log(String logMessage)
	{
		if(logMessage.startsWith(" ")||
			logMessage.startsWith("!"))
		{
			String clazz=Thread.currentThread()
								.getStackTrace()[2].getClassName();
			String metho=Thread.currentThread()
								.getStackTrace()[2].getMethodName();
			logMessage=logMessage+
						" ["+
						clazz.substring(clazz.lastIndexOf(".")+1)+
						"."+
						metho+
						"]";
		}
		if(!logMessage.startsWith("!"))
		{
			System.out.println(logMessage);
		}
	}
}
