package com.andrubyrne.filefilters;
import java.io.*;
import android.app.*;


class JPGFileFilter extends Activity implements FileFilter
{

	@Override
	public boolean accept(File pathname)
	{
		String suffix = ".jpg";
		if (pathname.getName().toLowerCase().endsWith(suffix))
		{
			return true;
		}
		return false;
	}

}
