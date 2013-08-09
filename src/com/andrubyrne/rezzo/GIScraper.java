package com.andrubyrne.rezzo;
import android.app.*;
import android.os.*;
import android.view.*;
import android.content.*;
import android.widget.*;
import java.io.*;
import android.graphics.*;
import com.andrubyrne.exifhelper.ExifHelper;

import android.location.*;
import android.provider.*;
import android.util.*;
import android.nfc.*;


public class GIScraper extends Activity
{
    ImageView imageView;
	TextView textView;
	Float Latitude, Longitude;
	Boolean batch;
	int batchIndex;
	private final String TAG = getClass().getSimpleName();
	private static String PATH = Environment.getExternalStorageDirectory().getPath() + "/Rezzo/";
	File inFile;
	File outDir = new File(PATH+"/");
	Intent intent;
	Intent homeIntent;
	Intent mapIntent;
	ExifHelper mEH = new ExifHelper();
	JPGFileFilter jpgFileFilter;
    public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		intent = this.getIntent();
		batch = intent.getBooleanExtra("batch", false);
		if (batch)
		{
			setContentView(R.layout.giscraperbatch);
            imageView = (ImageView)findViewById(R.id.smallerImageViewBatch);
			textView = (TextView)findViewById(R.id.mapInstructionBatch);
		}
		else
		{
			setContentView(R.layout.giscraper);
			imageView = (ImageView)findViewById(R.id.smallerImageView);
			textView = (TextView)findViewById(R.id.mapInstruction);
		}
		batchIndex = 0;
		jpgFileFilter = new JPGFileFilter();
		
	}	
	public void onResume()
	{
		super.onResume();
		Bundle bundle = intent.getExtras();
		homeIntent = new Intent(this, Home.class);
		mapIntent = new Intent(this, MapAffirm.class);
		mapIntent.putExtras(bundle);
		if (batch)
		{
			File[] refImages = outDir.listFiles(jpgFileFilter);
			if (refImages.length == 0)
			{	//check that there are files
				Toast.makeText(this, R.string.no_cache, Toast.LENGTH_LONG).show();
				startActivity(homeIntent);
				finish();
			
			} else if(batchIndex >= refImages.length){ //stop at the end
				Toast.makeText(this, R.string.batch_compete, Toast.LENGTH_LONG).show();
			    startActivity(homeIntent);	
				finish();
			}
			else inFile = refImages[batchIndex];
			} 
		else inFile = new File(getFilesDir(), "newImage.jpg");
		try
		{ 
			String filepath = inFile.getAbsolutePath();
			mEH.createInFile(filepath);
			mEH.readExifData();

			String latitude = mEH.gpsLatitude;
			String latitude_ref = mEH.gpsLatitudeRef;
			String longitude = mEH.gpsLongitude;
			String longitude_ref = mEH.gpsLongitudeRef;

			if ((latitude != null)
				&& (latitude_ref != null)
				&& (longitude != null)
				&& (longitude_ref != null))
			{
				if (latitude_ref.equals("N")) Latitude = Math.abs(convertToDegree(latitude));
				else Latitude = 0 - convertToDegree(latitude);
				if (longitude_ref.equals("E")) Longitude = Math.abs(convertToDegree(longitude));
				else Longitude = 0 - convertToDegree(longitude);
			}
			textView.setText("This picture seems to have been taken at " + 
							 Latitude + " latitude and " + 
							 Longitude + " longitude. "+getString(R.string.map_instructions));
			Bitmap mBitmap = BitmapFactory.decodeFile(inFile.getAbsolutePath());
			imageView.setImageBitmap(mBitmap);
			mapIntent.putExtra("filepath", inFile.getAbsolutePath());			
			if (Latitude != null) mapIntent.putExtra("Latitude", Latitude.doubleValue());
			else mapIntent.putExtra("Latitude", "null");
			if (Longitude != null) mapIntent.putExtra("Longitude", Longitude.doubleValue());
			else mapIntent.putExtra("Longitude", "null");
		}
		catch (java.io.IOException e)
		{Log.e(TAG, e.getMessage());}
		finally
		{}
	}
	public void launchAffirm(View v)
	{
		startActivity(mapIntent);
        finish();		
	}

	public void skipBatchPic(View v)
	{
		batchIndex++;
		onResume();
	}

	public void deleteBatchPic(View v)
	{
		inFile.delete();
		onResume();
	}

	public void exitBatch(View v)
	{
	    startActivity(homeIntent);
		finish();
	}

	private Float convertToDegree(String stringDMS)
	{
		Float result = null;
		String[] DMS = stringDMS.split(",", 3);
		String[] stringD = DMS[0].split("/", 2);
		Double D0 = new Double(stringD[0]);
		Double D1 = new Double(stringD[1]);
		Double FloatD = D0 / D1;

		String[] stringM = DMS[1].split("/", 2);
		Double M0 = new Double(stringM[0]);
		Double M1 = new Double(stringM[1]);
		Double FloatM = M0 / M1;

		String[] stringS = DMS[2].split("/", 2);
		Double S0 = new Double(stringS[0]);
		Double S1 = new Double(stringS[1]);
		Double FloatS = S0 / S1;

		result = new Float(FloatD + (FloatM / 60) + (FloatS / 3600));

		return result;
	};
}
	

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
