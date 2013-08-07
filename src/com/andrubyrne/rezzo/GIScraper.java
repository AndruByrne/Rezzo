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
	File inFile;
	Intent intent;
	Boolean batch;
	int batchIndex;
	private final String TAG = getClass().getName();
	private static String PATH = Environment.getExternalStorageDirectory().getPath() + "/Rezzo/";
	File outDir = new File(PATH+"/");
	Intent homeIntent;
	Intent mapIntent;
	
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
			JPGFileFilter jpgFileFilter = new JPGFileFilter();
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
			else {
				 inFile = refImages[batchIndex];
		//		 mapIntent.putExtra("filepath", refImages[batchIndex].getAbsolutePath().toString());
			     }
			} 
		else inFile = new File(getFilesDir(), "newImage.jpg");
		try
		{ 
			String filepath = inFile.getAbsolutePath();
			ExifHelper mEH = new ExifHelper();
			mEH.createInFile(filepath);
			mEH.readExifData();

			String LATITUDE = mEH.getGpsLatitude();
			String LATITUDE_REF = mEH.getGpsLatitudeRef();
			String LONGITUDE = mEH.getGpsLongitude();
			String LONGITUDE_REF = mEH.getGpsLongitudeRef();

			if ((LATITUDE != null)
				&& (LATITUDE_REF != null)
				&& (LONGITUDE != null)
				&& (LONGITUDE_REF != null))
			{
				if (LATITUDE_REF.equals("N")) Latitude = Math.abs(convertToDegree(LATITUDE));
				else Latitude = 0 - convertToDegree(LATITUDE);
				if (LONGITUDE_REF.equals("E")) Longitude = Math.abs(convertToDegree(LONGITUDE));
				else Longitude = 0 - convertToDegree(LONGITUDE);
			}
			textView.setText("This picture seems to have been taken at " + 
							 Latitude + " latitude and " + 
							 Longitude + " longitude. On the following map, please refine or define this calculation by moving the indicator and pressing the button on the upper right.");
			Bitmap mBitmap = BitmapFactory.decodeFile(inFile.getAbsolutePath());
			imageView.setImageBitmap(mBitmap);
			if (Latitude != null) mapIntent.putExtra("Latitude", Latitude.doubleValue());
			else mapIntent.putExtra("Latitude", "null");
			if (Longitude != null) mapIntent.putExtra("Longitude", Longitude.doubleValue());
			else mapIntent.putExtra("Longitude", "null");
		}
		catch (java.io.IOException e)
		{Toast.makeText(this, R.string.no_internal, Toast.LENGTH_LONG);}
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


class JPGFileFilter implements FileFilter
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
