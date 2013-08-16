/* Andrew Byrne licenses this file
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
import android.database.*;
import android.graphics.*;
import android.location.*;
import android.net.*;
import android.os.*;
import android.preference.*;
import android.provider.*;
import android.text.format.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import com.andrubyrne.exifhelper.*;
import com.andrubyrne.filefilters.*;
import com.andrubyrne.utils.*;
import java.io.*;

public class Home extends Activity
{ 
	private final int CAMERA_REQUEST = 2885;
	private final int GALLERY_REQUEST = 2225;
	private final String TAG = getClass().getSimpleName();
//	ImageView imageView1;
	ExifHelper mEH = new ExifHelper();
	Utils utils = new Utils();
	Time today = new Time(Time.getCurrentTimezone());
	LocationManager locationManager;
	Location bestLocation;
	private static String PATH = Environment.getExternalStorageDirectory().getPath() + "/Rezzo/";
	File outDir = new File(PATH + "/");
	File cameraPic;
	boolean gsmUser;
	SharedPreferences preferences;
	JPGFileFilter jpgFileFilter = new JPGFileFilter();

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home);
		cameraPic = new File(getFilesDir(), "newImage.jpg");
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
	}
    @Override
	public void onResume()
	{
		super.onResume();
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		gsmUser = preferences.getBoolean("use_gsm", false);
//		gsmUser = preferences.getBoolean("ignore_wifi", false);
		File[] refImages = outDir.listFiles(jpgFileFilter);
		if (!gsmUser && refImages.length > 0 && utils.isConnected(this))
		{ // notify batch processing availability if needed
			utils.batchNotification(this);
		}
		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
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
		else askForGPS(this);

		locationManager = null;	
	}

	public void fromBatch(View v)
	{
		if (utils.isConnected(this) || gsmUser)
		{
			Intent i = new Intent(this, GIScraper.class);
			i.putExtra("batch", true);
			startActivity(i);
		}
		else Toast.makeText(getBaseContext(), R.string.no_wifi_gallery, Toast.LENGTH_LONG).show();	
	}

	public void fromGallery(View v)
	{
		Intent i = new Intent(Intent.ACTION_PICK);
		i.setType("image/*");
		startActivityForResult(i, GALLERY_REQUEST);
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
				if (utils.isConnected(this) || gsmUser)
				{
					//	attach GIS data
					File out = new File(getFilesDir(), "newImage.jpg");
					writeExif(out);

					Intent i = new Intent(this, GIScraper.class);
					i.putExtra("batch", false);
					i.putExtra("filepath", getFilesDir() + "/newImage.jpg");
					startActivity(i);
				}
				else
				{//save image file to external storage
					Toast.makeText(getBaseContext(), R.string.no_wifi_photo, Toast.LENGTH_LONG).show();
					try
					{writeExif(utils.copyImage(cameraPic, outDir));}
					catch (IOException e)
					{Log.e(TAG, e.toString());}
				}
			}
		}
		else if (resultCode == RESULT_OK && requestCode == GALLERY_REQUEST)
		{
			if (utils.isConnected(this) || gsmUser)
			{
				Uri photoUri = data.getData();
				if (photoUri != null)
				{
					try
					{

						//false on first button press		Log.e(TAG, "gallery request recievwed"); 

						String[] filePathColumn = {MediaStore.Images.Media.DATA};
						Cursor cursor = getContentResolver().query(photoUri, filePathColumn, null, null, null); 
						cursor.moveToFirst();
						int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
						String filePath = cursor.getString(columnIndex);
						cursor.close();

						Intent i = new Intent(this, GIScraper.class);
						i.putExtra("batch", false);
						i.putExtra("filepath", filePath);
						startActivity(i);
					}
					catch (Exception e)
					{
						Log.e(TAG, e.toString());
					}
					
				}			
			}
			else Toast.makeText(getBaseContext(), R.string.no_wifi_photo, Toast.LENGTH_LONG).show();
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

	private void foundGPS()
	{
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
//		imageView1 = null;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuItem buttonSettings = menu.add(getString(R.string.settings));
		buttonSettings.setIcon(R.drawable.ic_launcher);
		buttonSettings.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER); //force overflow method
		buttonSettings.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

				public boolean onMenuItemClick(MenuItem item)
				{
					Intent settingsIntent = new Intent(Home.this, UserSettings.class);
					Home.this.startActivity(settingsIntent);
					return false;
				}
			});
		return true;
	}
	public void askForGPS(Context context)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(getString(R.string.loc_man));
		builder.setMessage(getString(R.string.ask_for_gps));
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
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
					Toast.makeText(getBaseContext(), getString(R.string.gps_req), Toast.LENGTH_SHORT).show();
					//				Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
					//				startActivity(i);
					finish();
				}
			});
		builder.create().show();
	}

}
	

