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
import android.widget.AdapterView.*;

public class PointDetail extends Activity implements OnItemSelectedListener
{
//	Intent homeIntent;
//intent for batch processing
	Intent batchIntent;
//single intent
	Intent intent;
//bundlefor extras
	Bundle bundle;
//batch flag
	boolean batch;	
//new gis data
	TextView finalGIStext;
//region from settings
	TextView regionName;
//name of point
	EditText namePoint;
//notes on point
	EditText notesPoint;
//resource spinners
	Spinner natSpinner;
	Spinner infSpinner;
	Spinner sklSpinner;
	String resNat;
	String resInf;
	String resSkl;
	String[] resNatArray;
	String[] resInfArray;
	String[] resSklArray;

	//last we'll see of the pictures
	ImageView imageView;
	Bitmap bitmap;
	//debug tag
	private final String TAG = getClass().getSimpleName();
    //referencefor currentfile
	private String filepath;
	SharedPreferences preferences;
	//the creme de la creme
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
		imageView = (ImageView)findViewById(R.id.finalImageView);
		resNatArray = getResources().getStringArray(R.array.nat_res_array);
		resInfArray = getResources().getStringArray(R.array.inf_res_array);
		resSklArray = getResources().getStringArray(R.array.skl_res_array);
		setSpinners();
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
							 " Latitude: " + intent.getDoubleExtra("Latitude", 0.0) + " Longitude: " + intent.getDoubleExtra("Longitude", 0.0));
  		preferences = PreferenceManager.getDefaultSharedPreferences(this); 
	//	Log.e(TAG, preferences.getString("region", "none"));
		regionName.setText(preferences.getString("region", "none"));
		bitmap = BitmapFactory.decodeFile(intent.getStringExtra("filepath"));
		imageView.setImageBitmap(bitmap);
		chop = new File(intent.getStringExtra("filepath"));
	}
    public void setSpinners()
	{
	//	Log.e(TAG, "setting spinners");
		sklSpinner = (Spinner) findViewById(R.id.skl_res);
		infSpinner = (Spinner) findViewById(R.id.inf_res);
		natSpinner = (Spinner) findViewById(R.id.nat_res);
		ArrayAdapter<CharSequence> sklAdapter = ArrayAdapter.createFromResource(this,
																				R.array.skl_res_array, android.R.layout.simple_spinner_item);
		ArrayAdapter<CharSequence> infAdapter = ArrayAdapter.createFromResource(this,
																				R.array.inf_res_array, android.R.layout.simple_spinner_item);
		ArrayAdapter<CharSequence> natAdapter = ArrayAdapter.createFromResource(this,
																				R.array.nat_res_array, android.R.layout.simple_spinner_item);
		sklAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		infAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		natAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		sklSpinner.setAdapter(sklAdapter);
		infSpinner.setAdapter(infAdapter);
		natSpinner.setAdapter(natAdapter);
		sklSpinner.setOnItemSelectedListener(this);
		infSpinner.setOnItemSelectedListener(this);
		natSpinner.setOnItemSelectedListener(this);
	}

	public void onItemSelected(AdapterView<?> parent, View view, 
							   int pos, long id)
	{
		Log.e(TAG, "selected" +parent.getId());
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
		switch (parent.getId())
		{
			case 2131099680:
				resNat = resNatArray[pos];
				Log.e(TAG, "resNat: " + resNat);
				break;
			case 2131099682:
				resInf = resInfArray[pos];
				break;
		    case 2131099684:
				resSkl = resSklArray[pos];
				break;
		}
	}

    public void onNothingSelected(AdapterView<?> parent)
	{
        // no op
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
	private class PostJSONTask extends AsyncTask<Void, Void, Boolean>
	{
		protected Boolean doInBackground(Void... params)
		{
			if (pushToServer()) return true;
			else return false;
		}
		protected void onPostExecute()
		{
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
		writer.name("Natural Resources").value(resNat);
		writer.name("Infrastructure Resources").value(resInf);
		writer.name("Skilled Resources").value(resSkl);
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
