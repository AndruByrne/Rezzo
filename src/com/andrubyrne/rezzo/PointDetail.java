package com.andrubyrne.rezzo;
import android.app.*;
import android.content.*;
import android.os.*;
import com.google.android.gms.maps.*;
import android.widget.*;
import android.location.*;
import android.graphics.*;
import android.view.inputmethod.EditorInfo;
import android.view.KeyEvent;
import android.view.*;
import java.io.*;
import java.util.*;
import android.util.*;

public class PointDetail extends Activity
{
//	Intent homeIntent;
	Intent batchIntent;
	Intent intent;
	Bundle bundle;
	boolean batch;	
	TextView finalGIStext;
	EditText namingPlace;
	ImageView imageView;
	Bitmap bitmap;
	private final String TAG = getClass().getSimpleName();
	private String filepath;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.point_detail);
		finalGIStext = (TextView)findViewById(R.id.finalGISdata);
		namingPlace = (EditText)findViewById(R.id.namePoint);
		imageView = (ImageView)findViewById(R.id.finalImageView);
    }

	@Override	
    public void onResume()
	{
		super.onResume();
//		homeIntent = new Intent(this, Home.class);
		batchIntent = new Intent(this, GIScraper.class);
		intent = this.getIntent();
		bundle = intent.getExtras();
		filepath = intent.getStringExtra("filepath");
//		homeIntent.putExtras(bundle);
		batchIntent.putExtras(bundle);
		batch = intent.getBooleanExtra("batch", false);
		finalGIStext.setText(getString(R.string.final_gis_text) +
							 "Latitude: " + intent.getDoubleExtra("Latitude", 0.0) + " Longitude: " + intent.getDoubleExtra("Longitude", 0.0));
	    bitmap = BitmapFactory.decodeFile(intent.getStringExtra("filepath"));
		imageView.setImageBitmap(bitmap);

	}

	public void doneNaming(View v)
	{
		File outFile = new File(filepath + ".json");
		try
		{
			OutputStream out = new FileOutputStream(outFile, false);
			writeJsonStream(out);
			out.flush();
		    out.close();
			out = null;
		}
		catch (FileNotFoundException e)
		{Log.e(TAG, "json not opened");}		
		catch (IOException e)
		{Log.e(TAG, "json not written");}

		if (batch)
		{
			deleteFile(filepath);
			startActivity(batchIntent);
		}
	    finish();
	}

	public void writeJsonStream(OutputStream out) throws IOException
	{
		JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, "UTF-8"));
		writer.setIndent("  ");
		writer.beginObject();
		writer.name("Position Label").value(namingPlace.getText().toString());
		writer.name("GIS coordinates");
		writeGIS(writer);
		writer.endObject();
		writer.close();
    }

	public void writeGIS(JsonWriter writer) throws IOException
	{
		writer.beginObject();
		writer.name("Latitude").value(intent.getDoubleExtra("Latitude", 0.0)).toString();
		writer.name("Longitude").value(intent.getDoubleExtra("Longitude", 0.0)).toString();
		writer.endObject();
	}
}
