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
import android.preference.*;
import com.andrubyrne.filefilters.*;
import com.andrubyrne.*;

public class Utils extends Activity
{
	private static final int TWO_MINUTES = 1000 * 60 * 2;
	private static StringBuilder stringBuilder;
	private final String TAG = getClass().getSimpleName();
	SharedPreferences preferences;
	
	public void askForGPS(Context context)
	{
		final String warning_gps = context.getResources().getString(R.string.gps_req);
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(context.getResources().getString(R.string.loc_man));
		builder.setMessage(context.getResources().getString(R.string.ask_for_gps));
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
					Toast.makeText(getBaseContext(), warning_gps, Toast.LENGTH_SHORT).show();
					//				Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
					//				startActivity(i);
					finish();
				}
			});
		builder.create().show();
	}
	
	public void batchNotification(Context context)
	{

		NotificationManager mgr = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
		int icon = R.drawable.ic_launcher;
		CharSequence tickerText = "Rezzo batch function available";
		CharSequence notiTitle = "Rezzo";
		
		long when = System.currentTimeMillis();

		//make notification details
		Notification noti = new Notification(icon, tickerText, when);
		
		Intent notificationIntent = new Intent(context, GIScraper.class);
		notificationIntent.putExtra("batch", true);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		noti.setLatestEventInfo(context, "Rezzo", "Use wifi to process cached reference images", contentIntent);
		long[] vibrate = {0, 100, 100, 100, 100, 100, 100, 100, 375, 100, 100, 100, 100, 100, 100, 100};
		noti.vibrate = vibrate;
		noti.defaults = Notification.DEFAULT_VIBRATE; //need permissions
		//to have countdown, call NotificationManager::notify with the same ID

		final int HELLO_ID = R.id.select_batch;

		mgr.notify(HELLO_ID, noti);
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

	public void informOfGPS(Context context)
	{
		Toast.makeText(context, R.string.gps_found, Toast.LENGTH_SHORT).show();	
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

}
