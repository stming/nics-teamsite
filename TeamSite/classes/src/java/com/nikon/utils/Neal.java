package com.nikon.utils;

import java.io.File;
import java.io.FileWriter;

public class Neal
{
	public static void main(String[] args)
	{
		try
		{
			File f = new File ("c:/temp/someFile.txt");
			FileWriter fw = new FileWriter(f);
			fw.write("neal");
			fw.flush();
			fw.close();
		}
		catch(Exception exception)
		{

		}

	}
}