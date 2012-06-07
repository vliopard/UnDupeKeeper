package com.OTDSHCo;
public class FileQueue
{
	private int		typeOfAction;
	private String	pathToFile;

	public FileQueue()
	{
	}

	public void set(int action,
					String pathAndFile)
	{
		typeOfAction=action;
		pathToFile=pathAndFile;
	}

	public int getType()
	{
		return typeOfAction;
	}

	public String getPath()
	{
		return pathToFile;
	}
}
