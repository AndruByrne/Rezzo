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
import android.location.*;
import android.net.*;
import android.os.*;
import android.provider.*;
import android.text.format.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import com.andrubyrne.exifhelper.*;
import com.andrubyrne.utils.*;
import java.io.*;

public class Home extends Activity
{ 
	private final int CAMERA_REQUEST = 2885;
	private final String TAG = getClass().getSimpleName();
	ImageView imageView1;
	ExifHelper mEH = new ExifHelper();
	Utils utils = new Utils();
	Time today = new Time(Time.getCurrentTimezone());
	LocationManager locationManager;
	Location bestLocation;
	private static String PATH = Environment.getExternalStorageDirectory().getPath() + "/Rezzo/";
	File outDir = new File(PATH + "/");
	File cameraPic;

	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		cameraPic = new File(getFilesDir(), "newImage.jpg");
	}
    @Override
	public void onResume()
	{
		super.onResume();
		//todo: check for wifi; if and if wifi pref not set to ignore, then notify
//		if(isConnected(this));
		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
//just changedthis to gps_proviuder
		//	String locationProvider = LocationManager.GPS_PROVIDER;
//		bestLocation = locationManager.getLastKnownLocation(locationProvider);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 100, locationListener); // 15min
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 500, 0, locationListener);
	}
	public void fromPhoto(View v)
	{
		today.setToNow();
		PackageManager pm = getPackageManager();
		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
		{
			Intent i = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
			i.putExtra(MediaStore.EXTRA_OUTPUT, FileContentProvider.CONTENT_URI);		
			if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA))
				i.putExtra("camerasensortype", 1); // call the rear camera			
			else if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT))
				i.putExtra("camerasensortype", 2); // call the front camera			
			else Toast.makeText(getBaseContext(), "Camera is not available", Toast.LENGTH_LONG).show();
			i.putExtra("autofocus", true);
			i.putExtra("fullScreen", false);
			i.putExtra("showActionIcons", false);
			startActivityForResult(i, CAMERA_REQUEST);	
		}		
		else utils.askForGPS(this);

		locationManager = null;	
	}

	public void fromGallery(View v)
	{
		Intent i = new Intent(this, GIScraper.class);
		i.putExtra("batch", true);
		startActivity(i);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		Log.i(TAG, "Receive the camera result");
		if (resultCode == RESULT_OK && requestCode == CAMERA_REQUEST)
		{
			if (bestLocation == null) Toast.makeText(this, R.string.no_gps_yet, Toast.LENGTH_LONG).show();
			else 
			{
				//need to add a preference for using gsm for map connection before demo
				//if wifi service, 
				if (utils.isConnected(this))
				{
					//	attach GIS data by getLatitude()&&
					File out = new File(getFilesDir(), "newImage.jpg");
					writeExif(out);

					Intent i = new Intent(this, GIScraper.class);
					i.putExtra("batch", false);
					startActivity(i);
				}
				else
				{//save image file to external storage
					Toast.makeText(getBaseContext(), R.string.no_wifi, Toast.LENGTH_LONG).show();
					try {writeExif(utils.copyImage(cameraPic, outDir));}
					catch (IOException e) {Log.e(TAG, e.toString());}
				}
			}
		}
	}

	LocationListener locationListener = new LocationListener() {
		public void onLocationChanged(Location location)
		{
			if (utils.isBetterLocation(location, bestLocation))
			{
				if (bestLocation == null) foundGPS();
				bestLocation = location;
			}
		}
 
		public void onStatusChanged(String provider, int status, Bundle extras)
		{}

		public void onProviderEnabled(String provider)
		{}

		public void onProviderDisabled(String provider)
		{}
	};
	
	private void foundGPS(){
		utils.informOfGPS(this);
	}
	
	private void writeExif(File outFile)
	{
		try
		{
			mEH.createOutFile(outFile.getAbsolutePath());
			mEH.gpsLatitude = utils.convertToRational(bestLocation.getLatitude());	
			mEH.gpsLongitude = utils.convertToRational(bestLocation.getLongitude());
			mEH.gpsLatitudeRef = utils.latitudeRef(bestLocation.getLatitude());
			mEH.gpsLongitudeRef = utils.longitudeRef(bestLocation.getLongitude());
			mEH.writeExifData();
		}
		catch (IOException e)
		{Toast.makeText(this, R.string.no_internal, Toast.LENGTH_LONG);}
		finally
		{}
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
		//thisiswrong
//		locationManager.removeUpdates(locationListener);
		imageView1 = null;
	}
}
