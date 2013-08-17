package com.andrubyrne.rezzo;
import android.app.*;
import android.content.*;
import android.graphics.*;
import android.os.*;
import android.preference.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.net.*;

public class PointDetail extends Activity
{
//	Intent homeIntent;
	Intent batchIntent;
	Intent intent;
	Bundle bundle;
	boolean batch;	
	TextView finalGIStext;
	TextView regionName;
	EditText namePoint;
	EditText notesPoint;
	EditText resNat;
	EditText resInf;
	EditText resSkl;
	ImageView imageView;
	Bitmap bitmap;
	private final String TAG = getClass().getSimpleName();
	private String filepath;
	SharedPreferences preferences;
	File chop;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.point_detail);
		finalGIStext = (TextView)findViewById(R.id.finalGISdata);
		regionName = (TextView)findViewById(R.id.regionName);
		namePoint = (EditText)findViewById(R.id.namePoint);
		notesPoint = (EditText)findViewById(R.id.notesPoint);
		resNat = (EditText)findViewById(R.id.nat_res);
		resInf = (EditText)findViewById(R.id.inf_res);
		resSkl = (EditText)findViewById(R.id.skl_res);
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
  		preferences = PreferenceManager.getDefaultSharedPreferences(this); 
		Log.e(TAG, preferences.getString("region", "none"));
		regionName.setText(preferences.getString("region", "none"));
		bitmap = BitmapFactory.decodeFile(intent.getStringExtra("filepath"));
		imageView.setImageBitmap(bitmap);
		chop = new File(intent.getStringExtra("filepath"));
	}

	public void doneNaming(View v)
	{
		new PostJSONTask().execute();
		if (batch)
		{
			chop.delete();
			startActivity(batchIntent);
		}
	    finish();
	}
	private class PostJSONTask extends AsyncTask<Void, Void, Boolean> {
		protected Boolean doInBackground(Void... params) {
		     if(pushToServer()) return true;
		     else return false;
		}

//		protected void onProgressUpdate(Integer... progress) {
//			setProgressPercent(progress[0]);
//		}
//
		protected void onPostExecute() {
			Toast.makeText(getBaseContext(), "Successful", Toast.LENGTH_SHORT).show();
		}
	}
	public void writeJsonStream(OutputStream out) throws IOException
	{
		JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, "UTF-8"));
		writer.setIndent("    ");
		writer.beginObject();
//		writer.name("GIS coordinates");
		writer.name("latitude").value(intent.getDoubleExtra("Latitude", 0.0)).toString();
		writer.name("longitude").value(intent.getDoubleExtra("Longitude", 0.0)).toString();
	//	writeGIS(writer);
		writer.name("title").value(namePoint.getText().toString());
        writer.name("notes").value(notesPoint.getText().toString());
		writer.name("region").value(preferences.getString("region", "none"));
        writer.name("resources");
		writeRes(writer);
		writer.endObject();
		writer.close();
    }

	public void writeRes(JsonWriter writer) throws IOException
	{
		writer.beginObject();
		writer.name("Natural Resources").value(resNat.getText().toString());
		writer.name("Infrastructure Resources").value(resInf.getText().toString());
		writer.name("Skilled Resources").value(resSkl.getText().toString());
		writer.endObject();
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
					Intent settingsIntent = new Intent(PointDetail.this, UserSettings.class);
					PointDetail.this.startActivity(settingsIntent);
					return false;
				}
			});
		return true;
	}


	public boolean pushToServer()
	{
		try
		{
			HttpURLConnection httpcon = (HttpURLConnection) ((new URL("http://rezzo.herokuapp.com/iOS").openConnection()));

			httpcon.setDoOutput(true);
			httpcon.setRequestProperty("Content-Type", "application/json");
			httpcon.setRequestProperty("Accept", "application/json");
			httpcon.setRequestMethod("POST");
			httpcon.connect();

			OutputStream os = httpcon.getOutputStream();
			writeJsonStream(os);
			os.close();
			return true;
		}
		catch (IOException e)
		{Log.e(TAG, e.toString());
			return false;}
	}
}
