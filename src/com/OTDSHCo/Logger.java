package com.OTDSHCo;
public class Logger
{
	public static final int	ALL_SOFTWARE			=0;
	public static final int	MAIN_THREAD				=1;
	public static final int	MAIN_SETTINGS			=2;
	public static final int	TOOLS_STATUS			=3;
	public static final int	TOOLS_FILE				=4;
	public static final int	TOOLS_PACKAGE			=5;
	public static final int	TOOLS_SUPERUSER			=6;
	public static final int	MAIN_SOFTWARE			=7;
	public static final int	TOOLS_CONVERT			=8;
	public static final int	TOOLS_UTIL				=9;
	public static final int	TOOLS_DIALOG			=10;
	/* ========================================================= */
	private static boolean	COMPLETE_DISABLED		=false;
	/* ========================================================= */
	private static boolean	ALL_SOFTWARE_ENABLED	=false;
	/* ========================================================= */
	private static boolean	MAIN_SOFTWARE_ENABLED	=false;
	private static boolean	MAIN_THREAD_ENABLED		=false;
	private static boolean	MAIN_SETTINGS_ENABLED	=false;
	private static boolean	TOOLS_STATUS_ENABLED	=false;
	private static boolean	TOOLS_FILE_ENABLED		=false;
	private static boolean	TOOLS_PACKAGE_ENABLED	=false;
	private static boolean	TOOLS_SUPERUSER_ENABLED	=false;
	private static boolean	TOOLS_CONVERT_ENABLED	=false;
	private static boolean	TOOLS_UTIL_ENABLED		=false;
	private static boolean	TOOLS_DIALOG_ENABLED	=false;
	private static boolean	undo					=false;

	public static void msg(String msg)
	{
		System.out.println(msg);
	}

	public static void log(	Thread type,
							String logMessage)
	{
		if(((ALL_SOFTWARE_ENABLED)&&
			(!COMPLETE_DISABLED)&&(!logMessage.startsWith("*")))||
			(logMessage.startsWith("!")))
		{
			log(type,
				logMessage,
				ALL_SOFTWARE);
		}
	}

	public static void log(	Thread typo,
							String logMessage,
							int module)
	{
		if(((!COMPLETE_DISABLED)&&(!logMessage.startsWith("*")))||
			(logMessage.startsWith("!")))
		{
			if(logMessage.startsWith("!"))
			{
				module=ALL_SOFTWARE;
				if(ALL_SOFTWARE_ENABLED==false)
				{
					ALL_SOFTWARE_ENABLED=true;
					undo=true;
				}
			}
			String type=null;
			if(logMessage.startsWith(" ")||
				logMessage.startsWith("!"))
			{
				type=typo.getStackTrace()[2].getClassName();
				type="["+
						type.substring(type.lastIndexOf(".")+1)+
						"."+
						typo.getStackTrace()[3].getMethodName()+
						"]\t";
			}
			else
			{
				type="[___________]\t";
			}
			switch(module)
			{
				case MAIN_SOFTWARE:
					if(MAIN_SOFTWARE_ENABLED||
						ALL_SOFTWARE_ENABLED)
					{
						lprint(	type,
								logMessage);
					}
				break;
				case MAIN_THREAD:
					if(MAIN_THREAD_ENABLED||
						ALL_SOFTWARE_ENABLED)
					{
						lprint(	type,
								logMessage);
					}
				break;
				case MAIN_SETTINGS:
					if(MAIN_SETTINGS_ENABLED||
						ALL_SOFTWARE_ENABLED)
					{
						lprint(	type,
								logMessage);
					}
				break;
				case TOOLS_STATUS:
					if(TOOLS_STATUS_ENABLED||
						ALL_SOFTWARE_ENABLED)
					{
						lprint(	type,
								logMessage);
					}
				break;
				case TOOLS_FILE:
					if(TOOLS_FILE_ENABLED||
						ALL_SOFTWARE_ENABLED)
					{
						lprint(	type,
								logMessage);
					}
				break;
				case TOOLS_PACKAGE:
					if(TOOLS_PACKAGE_ENABLED||
						ALL_SOFTWARE_ENABLED)
					{
						lprint(	type,
								logMessage);
					}
				break;
				case TOOLS_SUPERUSER:
					if(TOOLS_SUPERUSER_ENABLED||
						ALL_SOFTWARE_ENABLED)
					{
						lprint(	type,
								logMessage);
					}
				break;
				case TOOLS_CONVERT:
					if(TOOLS_CONVERT_ENABLED||
						ALL_SOFTWARE_ENABLED)
					{
						lprint(	type,
								logMessage);
					}
				break;
				case TOOLS_UTIL:
					if(TOOLS_UTIL_ENABLED||
						ALL_SOFTWARE_ENABLED)
					{
						lprint(	type,
								logMessage);
					}
				break;
				case TOOLS_DIALOG:
					if(TOOLS_DIALOG_ENABLED||
						ALL_SOFTWARE_ENABLED)
					{
						lprint(	type,
								logMessage);
					}
				break;
				default:
					if(ALL_SOFTWARE_ENABLED)
					{
						lprint(	type,
								logMessage);
					}
			}
			if(undo)
			{
				ALL_SOFTWARE_ENABLED=false;
			}
		}
	}

	private static void lprint(	String t,
								String l)
	{
		System.out.println(t+
							"\t"+
							l);
	}
}
