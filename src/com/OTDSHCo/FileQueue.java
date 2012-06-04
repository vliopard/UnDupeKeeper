package com.OTDSHCo;
public class FileQueue
{
	private int		kind;
	private String	path;

	public FileQueue()
	{
	}

	public void set(int k,
					String p)
	{
		kind=k;
		path=p;
	}

	public int getType()
	{
		return kind;
	}

	public String getPath()
	{
		return path;
	}
}
