package com.OTDSHCo;
import java.net.URL;
import java.nio.file.*;
import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class UnDupeKeeper
{
	private static String					iconSize			="16";
	private static String					imageDirectory		="images/";
	private static String[]					iconList			=
																{
			imageDirectory+
					"dnaColor"+
					iconSize+
					".jpg",
			imageDirectory+
					"dnaGray"+
					iconSize+
					".jpg",
			imageDirectory+
					"dnaGreen"+
					iconSize+
					".jpg",
			imageDirectory+
					"dnaRed"+
					iconSize+
					".jpg",
			imageDirectory+
					"dupeArrow"+
					iconSize+
					".jpg",
			imageDirectory+
					"dupeFile"+
					iconSize+
					".jpg",
			imageDirectory+
					"dupeLoupe"+
					iconSize+
					".jpg"
																};
	private static String[]					lookAndFeel			=
																{
			"com.sun.java.swing.plaf.gtk.GTKLookAndFeel",
			"com.sun.java.swing.plaf.motif.MotifLookAndFeel",
			"com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel",
			"com.sun.java.swing.plaf.windows.WindowsLookAndFeel",
			"javax.swing.plaf.basic.BasicLookAndFeel",
			"javax.swing.plaf.metal.MetalLookAndFeel",
			"javax.swing.plaf.multi.MultiLookAndFeel",
			"javax.swing.plaf.nimbus.NimbusLookAndFeel",
			"javax.swing.plaf.synth.SynthLookAndFeel"
																};
	private static boolean					recursiveFolderScan	=false;
	private static Consumer					worker;
	private static BlockingQueue<Integer>	stopSignal;
	private static BlockingQueue<FileQueue>	transferQueue;

	private static void usage()
	{
		err("usage: java UnDupeKeeper [-r] <DIRECTORY>");
		System.exit(-1);
	}

	private static boolean isDir(Path dirName)
	{
		if(null==dirName)
		{
			return false;
		}
		return (new File(dirName.toString()).exists())&&
				(new File(dirName.toString()).isDirectory());
	}

	private static String chooseDir()
	{
		JFrame frame=new JFrame();
		JFileChooser chooser;
		chooser=new JFileChooser();
		String directoryToLoad=DataBase.loadDir();
		if(null==directoryToLoad)
		{
			directoryToLoad="/";
		}
		chooser.setCurrentDirectory(new java.io.File(directoryToLoad));
		chooser.setDialogTitle("Select a folder to keep unduplicated");
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setAcceptAllFileFilterUsed(false);
		if(chooser.showOpenDialog(frame)==JFileChooser.APPROVE_OPTION)
		{
			directoryToLoad=chooser.getSelectedFile()
									.toString();
			DataBase.saveDir(directoryToLoad);
			return directoryToLoad;
		}
		return null;
	}

	private static Path checkPromptArguments(String[] arguments)
	{
		int argumentIndex=0;
		if(arguments.length==0||
			arguments.length>2)
		{
			usage();
		}
		if(arguments[0].equals("-r"))
		{
			recursiveFolderScan=true;
			if(arguments.length<2)
			{
				usage();
			}
			argumentIndex++;
		}
		return Paths.get(arguments[argumentIndex]);
	}

	public static void main(String[] args) throws IOException
	{
		Path directoryToWatch=null;
		if(args.length>0)
		{
			directoryToWatch=checkPromptArguments(args);
		}
		while(!isDir(directoryToWatch))
		{
			recursiveFolderScan=true;
			String directoryName=chooseDir();
			if(null==directoryName)
			{
				usage();
			}
			directoryToWatch=Paths.get(directoryName);
		}
		try
		{
			UIManager.setLookAndFeel(lookAndFeel[2]);
		}
		catch(UnsupportedLookAndFeelException|IllegalAccessException
				|InstantiationException|ClassNotFoundException ex)
		{
			err("Error loading look and feel.");
		}
		// UIManager.put( "swing.boldMetal", Boolean.FALSE);
		SwingUtilities.invokeLater(new Runnable()
			{
				public void run()
				{
					createAndShowGUI();
				}
			});
		log(" Initializing Threads...");
		stopSignal=new LinkedBlockingQueue<Integer>();
		transferQueue=new LinkedBlockingQueue<FileQueue>();
		log(" Producer Started...");
		try
		{
			stopSignal.put(0);
			worker=new Consumer(transferQueue,
								stopSignal);
			log(" Consumer Started...");
			new Thread(worker).start();
			log(" Producer Started...");
			msg("UnDupeKeeper is working...");
			DiscMonitor dm=new DiscMonitor(	directoryToWatch.toString(),
											transferQueue,
											stopSignal,
											recursiveFolderScan);
			while(!stopSignal.contains(2))
			{
				Thread.sleep(100);
			}
		}
		catch(InterruptedException e)
		{
			err("Problem While Starting Producer: "+
				e);
		}
		msg("UnDupeKeeper Normal Shutdown.\nExit.");
	}

	private static void shutup()
	{
		try
		{
			msg("Stopping...");
			stopSignal.put(1);
		}
		catch(InterruptedException e)
		{
			err("Cannot Shutdown Consumer.");
		}
	}

	private static void createAndShowGUI()
	{
		if(!SystemTray.isSupported())
		{
			err("SystemTray is not supported...");
			return;
		}
		final PopupMenu popup=new PopupMenu();
		Image img=createImage(	iconList[2],
								"Tray Icon");
		if(img==null)
		{
			Toolkit tk=Toolkit.getDefaultToolkit();
			byte[] bt=new byte[]
			{
				0
			};
			img=tk.createImage(	bt,
								16,
								16);
		}
		final TrayIcon trayIcon=new TrayIcon(img);
		final SystemTray tray=SystemTray.getSystemTray();
		MenuItem saveDatabase=new MenuItem("Save Database");
		MenuItem clearDatabase=new MenuItem("Clear Database");
		MenuItem aboutItem=new MenuItem("About");
		MenuItem exitItem=new MenuItem("Exit");
		popup.add(saveDatabase);
		popup.add(clearDatabase);
		popup.addSeparator();
		popup.add(aboutItem);
		popup.add(exitItem);
		trayIcon.setPopupMenu(popup);
		try
		{
			tray.add(trayIcon);
		}
		catch(AWTException e)
		{
			err("TrayIcon could not be added.");
			return;
		}
		trayIcon.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					JOptionPane.showMessageDialog(	null,
													"UnDupeKeeper by OTDS H Co.");
				}
			});
		saveDatabase.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					worker.save();
				}
			});
		clearDatabase.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					if(JOptionPane.showConfirmDialog(	null,
														"Database will be empty. Are you sure?")==JOptionPane.YES_OPTION)
					{
						worker.clear();
						worker.load();
					}
					else
					{
						JOptionPane.showMessageDialog(	null,
														"Operation canceled.");
					}
				}
			});
		aboutItem.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					JOptionPane.showMessageDialog(	null,
													"UnDupeKeeper by OTDS H Co.");
				}
			});
		exitItem.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					shutup();
					tray.remove(trayIcon);
				}
			});
	}

	protected static Image createImage(	String path,
										String description)
	{
		URL imageURL=UnDupeKeeper.class.getResource(path);
		if(imageURL==null)
		{
			err("Resource not found: "+
				path);
			return null;
		}
		else
		{
			return (new ImageIcon(	imageURL,
									description)).getImage();
		}
	}

	static void log(String logMessage)
	{
		Logger.log(	Thread.currentThread(),
					logMessage,
					Logger.MAIN_SOFTWARE);
	}

	static void msg(String message)
	{
		Logger.msg(message);
	}

	static void err(String errorMessage)
	{
		Logger.err(errorMessage);
	}
}
