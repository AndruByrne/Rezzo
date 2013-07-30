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
import java.io.*;
import android.text.format.*;
import android.net.*;

public class Home extends Activity
{ 
	private final int CAMERA_REQUEST = 1;
	private final int GALLERY_REQUEST = 2;
	private final String Tag = getClass().getName();
	ImageView imageView1;
	Time today = new Time(Time.getCurrentTimezone());
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		imageView1 = (ImageView)findViewById(R.id.imageView1);
		
	}

	public void fromPhoto(View v)
	{
		today.setToNow();
		PackageManager pm = getPackageManager();
		if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA))
		{
			Intent i = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
			i.putExtra(MediaStore.EXTRA_OUTPUT, MyFileContentProvider.CONTENT_URI);
			i.putExtra ("camerasensortype", 1); // call the rear camera
			i.putExtra ("autofocus", true); // AF
			i.putExtra ("fullScreen", false); // full screen
			i.putExtra ("showActionIcons", false);
			startActivityForResult(i, CAMERA_REQUEST);
		}
		if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)){
			Intent i = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
			i.putExtra(MediaStore.EXTRA_OUTPUT, MyFileContentProvider.CONTENT_URI);
			i.putExtra ("camerasensortype", 2); // call the front camera
			i.putExtra ("autofocus", true); // AF
			i.putExtra ("fullScreen", false); // full screen
			i.putExtra ("showActionIcons", false);
			startActivityForResult(i, CAMERA_REQUEST);
		}
		else
		{
			Toast.makeText(getBaseContext(), "Camera is not available", Toast.LENGTH_LONG).show();
		}   
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
		else
		{
			Toast.makeText(getBaseContext(), "Camera is not available", Toast.LENGTH_LONG).show();
		}   
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		Log.i(Tag, "Receive the camera result");
		if (resultCode == RESULT_OK && requestCode == CAMERA_REQUEST)
		{
			File out = new File(getFilesDir(), "newImage.jpg");
			if (!out.exists())
			{
				Toast.makeText(getBaseContext(),
							   "Error while capturing image", Toast.LENGTH_LONG)
					.show();
				return;
			}

			Bitmap mBitmap = BitmapFactory.decodeFile(out.getAbsolutePath());
			imageView1.setImageBitmap(mBitmap);
			//need to add a preference for using gsm for map connection before demo
			//if wifi service, 
			if(isConnected(this)){
				//launch location verification intent and reference the newImage in /files
				Intent i = new Intent(this, GIScraper.class);
     			Toast.makeText(getBaseContext(),
							   "WIFI ON", Toast.LENGTH_LONG)
					.show();
				startActivity(i);	
				
			}
			else {//save image file to external(?) storage
			    
			}
		}
		if (resultCode == RESULT_OK  && requestCode == GALLERY_REQUEST){
			
		}
	}
	
	private static boolean isConnected(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager)
			context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = null;
		if (connectivityManager != null) {
			networkInfo =
				connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		}
		return networkInfo == null ? false : networkInfo.isConnected();
	}
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		imageView1 = null;
	}
}
