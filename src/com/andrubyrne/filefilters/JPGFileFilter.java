package com.andrubyrne.filefilters;
import java.io.*;

	class JPGFileFilter implements FileFilter {

		@Override
		public boolean accept(File pathname) {
			String suffix = ".jpg";
			if( pathname.getName().toLowerCase().endsWith(suffix) ) {
				return true;
			}
			return false;
		}

	}
