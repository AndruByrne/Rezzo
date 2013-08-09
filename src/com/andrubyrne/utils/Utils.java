package com.andrubyrne.utils;
import android.net.*;
import android.content.*;
import android.location.*;
import android.widget.*;
import android.app.*;
import android.provider.*;
import com.andrubyrne.rezzo.*;
import java.io.*;
import android.text.format.*;
import android.util.*;

public class Utils extends Activity
{
	private static final int TWO_MINUTES = 1000 * 60 * 2;
	private static StringBuilder stringBuilder;
	private final String TAG = getClass().getSimpleName();
	public static boolean isConnected(Context context)
	{
		ConnectivityManager connectivityManager = (ConnectivityManager)
			context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = null;
		if (connectivityManager != null)
		{
			networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		}
		return networkInfo == null ? false : networkInfo.isConnected();
	}

	/** Determines whether one Location reading is better than the current Location fix
	 * @param location  The new Location that you want to evaluate
	 * @param currentBestLocation  The current Location fix, to which you want to compare the new one
	 */
	public static boolean isBetterLocation(Location location, Location currentBestLocation)
	{
		if (currentBestLocation == null)
		{
			// A new location is always better than no location
			return true;
		}

		// Check whether the new location fix is newer or older
		long timeDelta = location.getTime() - currentBestLocation.getTime();
		boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
		boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
		boolean isNewer = timeDelta > 0;

		// If it's been more than two minutes since the current location, use the new location
		// because the user has likely moved
		if (isSignificantlyNewer)
		{
			return true;
			// If the new location is more than two minutes older, it must be worse
		}
		else if (isSignificantlyOlder)
		{
			return false;
		}

		// Check whether the new location fix is more or less accurate
		int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
		boolean isLessAccurate = accuracyDelta > 0;
		boolean isMoreAccurate = accuracyDelta < 0;
		boolean isSignificantlyLessAccurate = accuracyDelta > 200;

		// Check if the old and new location are from the same provider
		boolean isFromSameProvider = isSameProvider(location.getProvider(),
													currentBestLocation.getProvider());

		// Determine location quality using a combination of timeliness and accuracy
		if (isMoreAccurate)
		{
			return true;
		}
		else if (isNewer && !isLessAccurate)
		{
			return true;
		}
		else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider)
		{
			return true;
		}
		return false;
	}

	synchronized public static final String convertToRational(double latitude)
	{
		stringBuilder = new StringBuilder(20);
        latitude = Math.abs(latitude);
        int degree = (int) latitude;
        latitude *= 60;
        latitude -= (degree * 60.0d);
        int minute = (int) latitude;
        latitude *= 60;
        latitude -= (minute * 60.0d);
        int second = (int) (latitude * 1000.0d);

        stringBuilder.setLength(0);
        stringBuilder.append(degree);
        stringBuilder.append("/1,");
        stringBuilder.append(minute);
        stringBuilder.append("/1,");
        stringBuilder.append(second);
        stringBuilder.append("/1000,");
        return stringBuilder.toString();
    }

	/** Checks whether two providers are the same */
	public static boolean isSameProvider(String provider1, String provider2)
	{
		if (provider1 == null)
		{
			return provider2 == null;
		}
		return provider1.equals(provider2);
	}

	public static String latitudeRef(double latitude)
	{
        return latitude < 0.0d ?"S": "N";
    }

    /**
     * returns ref for latitude which is S or N.
     * @param latitude
     * @return S or N
     */
    public static String longitudeRef(double longitude)
	{
        return longitude < 0.0d ?"W": "E";
    }
	
    public void informOfGPS(Context context)
	{
		Toast.makeText(context, R.string.gps_found, Toast.LENGTH_SHORT).show();	
	}
	
	public void askForGPS(Context context)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle("Location Manager");
		builder.setMessage("Please enable GPS, then press the back key");
		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
					startActivity(i);		
				}
			});
		builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					Toast.makeText(getBaseContext(), "GPS required for this app", Toast.LENGTH_SHORT).show();
					//				Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
					//				startActivity(i);
					finish();
				}
			});
		builder.create().show();
	}
	public File copyImage(File inFile, File outDir) 
	throws IOException
	{ 
		File outFile = new File(outDir + DateFormat.format("dd-MM-yyyy:hh:mm:ss", new java.util.Date()).toString() + ".jpg");
		if (!outDir.exists())
		{
			try
			{
				outDir.mkdirs();
			}
			catch (SecurityException e)
			{
				Log.e(TAG, "unable to write on the sd card " + e.toString());
			}
		}
    	OutputStream out = new FileOutputStream(outFile, false);
		InputStream in = new FileInputStream(inFile);

		byte[] buffer = new byte[1024]; 
		int read; 
		while ((read = in.read(buffer)) != -1)
		{ 
			out.write(buffer, 0, read); 
		} 

		in.close(); 
		in = null; 
		out.flush(); 
		out.close(); 
		out = null; 
		return outFile;
	}
}
