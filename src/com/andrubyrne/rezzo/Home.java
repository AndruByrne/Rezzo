/*
 Licensed to the Apache Software Foundation (ASF) under one
 or more contributor license agreements.  See the NOTICE file
 distributed with this work for additional information
 regarding copyright ownership.  The ASF licenses this file
 to you under the Apache License, Version 2.0 (the
 "License"); you may not use this file except in compliance
 with the License.  You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.
 */

package com.andrubyrne.rezzo;
import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.graphics.*;
import android.nfc.*;
import android.os.*;
import android.provider.*;
import android.util.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import com.andrubyrne.*;
import android.text.format.*;
import android.net.*;
import android.location.*;
import java.io.*;
import com.andrubyrne.exifhelper.*;
import java.lang.Double;
import android.content.res.*;

public class Home extends Activity
{ 
	private final int CAMERA_REQUEST = 1;
	private static final int TWO_MINUTES = 1000 * 60 * 2;
	private final int GALLERY_REQUEST = 2;
	private final String Tag = getClass().getName();
	ImageView imageView1;
	ExifHelper mEH = new ExifHelper();
	Time today = new Time(Time.getCurrentTimezone());
	LocationManager locationManager;
	Location bestLocation;
	private static StringBuilder stringBuilder;
	private static String PATH = Environment.getExternalStorageDirectory().getPath() + "/Rezzo/";

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		imageView1 = (ImageView)findViewById(R.id.imageView1);
	}
    @Override
	public void onResume()
	{
		super.onResume();
		//todo: check for wifi; if and if wifi pref not set to ignore, then notify
		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		String locationProvider = LocationManager.NETWORK_PROVIDER;
		bestLocation = locationManager.getLastKnownLocation(locationProvider);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 900000, 100, locationListener); // 15min
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 500000, 0, locationListener);
	}
	public void fromPhoto(View v)
	{
		today.setToNow();
		PackageManager pm = getPackageManager();
		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
		{
			if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA))
			{
				Intent i = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
				i.putExtra(MediaStore.EXTRA_OUTPUT, MyFileContentProvider.CONTENT_URI);
				i.putExtra("camerasensortype", 1); // call the rear camera
				i.putExtra("autofocus", true);
				i.putExtra("fullScreen", false);
				i.putExtra("showActionIcons", false);
				startActivityForResult(i, CAMERA_REQUEST);
			}
			else if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT))
			{
				Intent i = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
				i.putExtra(MediaStore.EXTRA_OUTPUT, MyFileContentProvider.CONTENT_URI);
				i.putExtra("camerasensortype", 2); // call the front camera
				i.putExtra("autofocus", true);
				i.putExtra("fullScreen", false);
				i.putExtra("showActionIcons", false);
				startActivityForResult(i, CAMERA_REQUEST);
			}
			else Toast.makeText(getBaseContext(), "Camera is not available", Toast.LENGTH_LONG).show();
		}		
		else askForGPS();

		locationManager = null;	
	}

	public void fromGallery(View v)
	{
		PackageManager pm = getPackageManager();
		if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA))
		{
			Intent i = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
			i.putExtra(MediaStore.EXTRA_OUTPUT, MyFileContentProvider.CONTENT_URI);
			startActivityForResult(i, GALLERY_REQUEST);
		}
		else Toast.makeText(getBaseContext(), "Camera is not available", Toast.LENGTH_LONG).show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		Log.i(Tag, "Receive the camera result");
		if (resultCode == RESULT_OK && requestCode == CAMERA_REQUEST)
		{
			//need to add a preference for using gsm for map connection before demo
			//if wifi service, 
			if (isConnected(this))
			{
				//	attach GIS data by getLatitude()&&
				File out = new File(getFilesDir(), "newImage.jpg");
				try
				{
					mEH.createOutFile(out.getAbsolutePath());
					mEH.setGpsLatitude(convertToRational(bestLocation.getLatitude()));	
				    mEH.setGpsLongitude(convertToRational(bestLocation.getLongitude()));
			    	mEH.setGpsLatitudeRef(latitudeRef(bestLocation.getLatitude()));
					mEH.setGpsLongitudeRef(longitudeRef(bestLocation.getLongitude()));
					mEH.writeExifData();
				}
				catch (IOException e)
				{Toast.makeText(this, R.string.no_internal, Toast.LENGTH_LONG);}
				finally
				{}

				Intent i = new Intent(this, GIScraper.class);
				startActivity(i);
			}
			else
			{//save image file to external(?) storage
				Toast.makeText(getBaseContext(), 
							   R.string.no_wifi, 
							   Toast.LENGTH_LONG)
					.show();
				try
				{
					copyImage();
				}
				catch (IOException e)
				{Toast.makeText(this, R.string.no_internal, Toast.LENGTH_SHORT).show();
					Log.e(getBaseContext().toString(), e.toString());
				}
 			}
		}
		if (resultCode == RESULT_OK  && requestCode == GALLERY_REQUEST)
		{

		}
	}

	LocationListener locationListener = new LocationListener() {
		public void onLocationChanged(Location location)
		{
			if (isBetterLocation(location, bestLocation)) bestLocation = location;
		}

		public void onStatusChanged(String provider, int status, Bundle extras)
		{}

		public void onProviderEnabled(String provider)
		{}

		public void onProviderDisabled(String provider)
		{}
	};

	private static boolean isConnected(Context context)
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
	protected boolean isBetterLocation(Location location, Location currentBestLocation)
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
	private boolean isSameProvider(String provider1, String provider2)
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

	public void askForGPS()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
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

	private void copyImage() 
	throws IOException
	{ 
        File outDir = new File(PATH+"/");
		File outFile = new File(PATH + "/" + DateFormat.format("dd-MM-yyyy:hh:mm:ss", new java.util.Date()).toString() + ".jpg");
		Toast.makeText(this, "savefile: " + outFile.getAbsolutePath().toString(), Toast.LENGTH_SHORT).show();
		if (!outDir.exists())
		{
			try
			{
				outDir.mkdirs();
			}
			catch (SecurityException e)
			{
				Log.e(Tag, "unable to write on the sd card " + e.toString());
			}
		}
		File inFile = new File(getFilesDir(), "newImage.jpg");
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
	}

	@Override
	protected void onPause()
	{
		super.onPause();
//		locationManager.removeUpdates(locationManager);
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		locationManager.removeUpdates(locationListener);
		imageView1 = null;
	}
}
