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
	private static String					size		="16";
	private static String					direc		="images/";
	private static String[]					iconList	=
														{
			direc+
					"dnaColor"+
					size+
					".jpg",
			direc+
					"dnaGray"+
					size+
					".jpg",
			direc+
					"dnaGreen"+
					size+
					".jpg",
			direc+
					"dnaRed"+
					size+
					".jpg",
			direc+
					"dupeArrow"+
					size+
					".jpg",
			direc+
					"dupeFile"+
					size+
					".jpg",
			direc+
					"dupeLoupe"+
					size+
					".jpg"
														};
	private static boolean					recursive	=false;
	private static Consumer					c;
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
		String d=DataBase.loadDir();
		if(null==d)
		{
			d="/";
		}
		chooser.setCurrentDirectory(new java.io.File(d));
		chooser.setDialogTitle("Select a folder to keep unduplicated");
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setAcceptAllFileFilterUsed(false);
		if(chooser.showOpenDialog(frame)==JFileChooser.APPROVE_OPTION)
		{
			d=chooser.getSelectedFile()
						.toString();
			DataBase.saveDir(d);
			return d;
		}
		return null;
	}

	private static Path checkArgs(String[] args)
	{
		int dirArg=0;
		if(args.length==0||
			args.length>2)
		{
			usage();
		}
		if(args[0].equals("-r"))
		{
			recursive=true;
			if(args.length<2)
			{
				usage();
			}
			dirArg++;
		}
		return Paths.get(args[dirArg]);
	}

	public static void main(String[] args) throws IOException
	{
		Path dir=null;
		if(args.length>0)
		{
			dir=checkArgs(args);
		}
		while(!isDir(dir))
		{
			recursive=true;
			String dirName=chooseDir();
			if(null==dirName)
			{
				usage();
			}
			dir=Paths.get(dirName);
		}
		try
		{
			int val=3;
			switch(val)
			{
				case 1:
					UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
				break;
				case 2:
					UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
				break;
				case 3:
					UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
				break;
				case 4:
					UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
				break;
				case 5:
					UIManager.setLookAndFeel("javax.swing.plaf.basic.BasicLookAndFeel");
				break;
				case 6:
					UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
				break;
				case 7:
					UIManager.setLookAndFeel("javax.swing.plaf.multi.MultiLookAndFeel");
				break;
				case 8:
					UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
				break;
				case 9:
					UIManager.setLookAndFeel("javax.swing.plaf.synth.SynthLookAndFeel");
			}
		}
		catch(UnsupportedLookAndFeelException|IllegalAccessException
				|InstantiationException|ClassNotFoundException ex)
		{
			err("Error loading look and feel.");
		}
		UIManager.put(	"swing.boldMetal",
						Boolean.FALSE);
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
			c=new Consumer(	transferQueue,
							stopSignal);
			log(" Consumer Started...");
			new Thread(c).start();
			log(" Producer Started...");
			msg("UnDupeKeeper is working...");
			DiscMonitor dm=new DiscMonitor(	dir.toString(),
											transferQueue,
											stopSignal,
											recursive);
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
					c.save();
				}
			});
		clearDatabase.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					if(JOptionPane.showConfirmDialog(	null,
														"Database will be empty. Are you sure?")==JOptionPane.YES_OPTION)
					{
						c.clear();
						c.load();
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

	static void msg(String msg)
	{
		Logger.msg(msg);
	}

	static void err(String msg)
	{
		Logger.err(msg);
	}
}
