package com.interwoven.teamsite.nikon.util;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;;

public class FolderFileFilter implements FileFilter
{
	ArrayList<String> nameExclude;
	
	public FolderFileFilter () {}
	public FolderFileFilter( ArrayList<String> m )
	{
		this.nameExclude = m;
	}
	
	public boolean accept(File f)
	{
		
		String name = f.getName();
		// ignore names that have been explicitly excluded
		if (nameExclude.contains(name)) {
			return false;
		}
		
		if ( f.isDirectory() )
		{
			if (name.matches("^[a-z]_[A-Z]$")) {
				return true;
			} else {
				return false;
			}
		}

		return false;
	}

}
