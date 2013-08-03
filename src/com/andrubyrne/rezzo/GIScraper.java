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

public class GIScraper extends Activity
{
    ImageView imageView;
	TextView textView;
	Float Latitude, Longitude;

    public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.giscraper);
		imageView = (ImageView)findViewById(R.id.imageViewSmaller);
		textView = (TextView)findViewById(R.id.mapInstruction);
		File in = new File(getFilesDir(), "newImage.jpg");
        	try
			{ 
				String filepath = in.getAbsolutePath();
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
				Bitmap mBitmap = BitmapFactory.decodeFile(in.getAbsolutePath());
				imageView.setImageBitmap(mBitmap);
			}
			catch (java.io.IOException e)
			{Toast.makeText(this, R.string.no_internal, Toast.LENGTH_LONG);
			}
			finally
			{}
	}	
	public void launchAffirm(View v)
	{
		Intent i = new Intent(this, MapAffirm.class);
		if (Latitude != null) i.putExtra("Latitude", Latitude.doubleValue());
		else i.putExtra("Latitude", "null");
		if (Longitude != null) i.putExtra("Longitude", Longitude.doubleValue());
		else i.putExtra("Longitude", "null");
		startActivity(i);
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
