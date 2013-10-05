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
import java.util.*;
import com.andrubyrne.utils.*;
import javax.crypto.*;

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
	ArrayList<String> resNat;
	ArrayList<String> resInf;
	ArrayList<String> resSkl;
	String[] resNatArray;
	String[] resInfArray;
	String[] resSklArray;
//Adapters for resources
    ResourcesAdapter resNatAdapter;
	ResourcesAdapter resInfAdapter;
    ResourcesAdapter resSklAdapter;
//view for resources
    ListView resNatDisplay;
	ListView resInfDisplay;
    ListView resSklDisplay;

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
		resNatDisplay = (ListView)findViewById(R.id.natresourcesDisplay);
		resInfDisplay = (ListView)findViewById(R.id.infresourcesDisplay);
		resSklDisplay = (ListView)findViewById(R.id.sklresourcesDisplay);
		resNat = new ArrayList<String>();
		resInf = new ArrayList<String>();
		resSkl = new ArrayList<String>();
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
		resNatDisplay.setAdapter(resNatAdapter);
		resInfDisplay.setAdapter(resInfAdapter);
		resSklDisplay.setAdapter(resSklAdapter);
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
		resNatAdapter = new ResourcesAdapter(this, R.layout.res_row, resNat);
		resInfAdapter = new ResourcesAdapter(this, R.layout.res_row, resInf);
		resSklAdapter = new ResourcesAdapter(this, R.layout.res_row, resSkl);

	}

	public void onItemSelected(AdapterView<?> parent, View view, 
							   int pos, long id)
	{
		//Log.e(TAG, "selected " +parent.getId());
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
		switch (parent.getId())
		{
			case 2131099680:
				resNatAdapter.add(resNatArray[pos]);
				//resNat.add( resNatArray[pos] );
				resNatAdapter.notifyDataSetChanged();
				//Log.e(TAG, "resNat: " + resNat);
				break;
			case 2131099682:
				//resInf = resInfArray[pos];
				resInfAdapter.add(resInfArray[pos]);
				resInfAdapter.notifyDataSetChanged();
				break;
		    case 2131099684:
				//resSkl = resSklArray[pos];
				resSklAdapter.add(resSklArray[pos]);
				resSklAdapter.notifyDataSetChanged();
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

	//jsonwriting
	public static String convertStreamToString(InputStream is) throws Exception
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		String line = null;

		while ((line = reader.readLine()) != null)
		{
			sb.append(line);
		}

		is.close();

		return sb.toString();
	}
	private class PostJSONTask extends AsyncTask<Void, Void, Boolean>
	{
		protected Boolean doInBackground(Void... params)
		{
			if (pushToServer()) 
			{
				Log.e(TAG, "pushed to server");
				return true;
			}
			else return false;
		}
		@Override
		protected void onPostExecute()
		{
			Log.e(TAG, "upload successful");
			Toast.makeText(getBaseContext(), "Upload Successful", Toast.LENGTH_SHORT).show();
		}
	}
	public String writeJsonString() throws IOException
	{
		StringWriter out = new StringWriter();
		JsonWriter writer = new JsonWriter(out);
		writer.setIndent("    ");
		writer.beginObject();
//		writer.name("GIS coordinates");
		writer.name("title").value(namePoint.getText().toString());
		writer.name("longitude").value(intent.getDoubleExtra("Longitude", 0.0)).toString();
		//	writeGIS(writer);
        writer.name("notes").value(notesPoint.getText().toString());
		writer.name("region").value(preferences.getString("region", "none"));
		writer.name("latitude").value(intent.getDoubleExtra("Latitude", 0.0)).toString();
		writer.name("resources");
		writeRes(writer);
		writer.endObject();
		writer.close();
		//	Log.e(TAG, "JSON written");
		return out.toString();
    }

	public void writeRes(JsonWriter writer) throws IOException
	{
		writer.beginObject();
		//Log.e(TAG, "writing resources");
		writer.name("Natural Resources");
		writer.beginArray();
		if (resNat.size() > 0)
		{
			boolean flag = true;
			for (String res : resNatAdapter.getItems())
			{
				if (!flag)
				{
					writer.value(res);

				}
				flag = false;
			}
		}
		writer.endArray();
		writer.name("Skilled Resources");
		writer.beginArray();
		if (resNat.size() > 0)
		{
			boolean flag = true;
			for (String res : resSklAdapter.getItems())
			{
				if (!flag)
				{
					writer.value(res);

				}
				flag = false;
			}
		}
		writer.endArray();
		writer.name("Infrastructure Resources");
		writer.beginArray();
		if (resNat.size() > 0)
		{
			boolean flag = true;
			for (String res : resInfAdapter.getItems())
			{
				if (!flag)
				{
					writer.value(res);

				}
				flag = false;
			}
		}
		writer.endArray();
		//Log.e(TAG, "written resources");
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
		OutputStream os = null;
		InputStream is = null;
		String message = null;
		HttpURLConnection httpcon = null;
	    try
		{
			URL url = new URL("http://build.phonegap.com");
			
		    message = writeJsonString();
			httpcon = ((HttpURLConnection) url.openConnection());


			httpcon.setReadTimeout(10000);
			httpcon.setConnectTimeout(15000);
			httpcon.setRequestMethod("POST");
			httpcon.setDoInput(true);
			httpcon.setDoOutput(true); 
			httpcon.setFixedLengthStreamingMode(message.getBytes().length);
          //  Log.e(TAG, "message length = " + message.getBytes().length);
		  
			//headers
			httpcon.setRequestProperty("Content-Type", "multipart/form-data;charset=utf-8");
			httpcon.setRequestProperty("X-Requested-With", "XMLHttpRequest");

			//out = httpcon.getOutputStream(); 
			//File outFile = new File(Environment.getExternalStorageDirectory().getPath() + "/" + TAG + "/testJSON");
			//out = new FileOutputStream(outFile, false);
			httpcon.connect();
			
			os = new BufferedOutputStream(httpcon.getOutputStream());
			os.write(message.getBytes());
			os.flush();
			
			
			//out.write("\r\n rezzo_entry_0 \r\n\r\n".getBytes("UTF-8"));
			//writeJsonString(out);
			//out.write("\r\n\r\n ".getBytes("UTF-8"));

		    is = httpcon.getInputStream(); 

			try
			{
				Log.i(TAG, "server response: " + convertStreamToString(is));
			}
			catch (Exception e)
			{Log.e(TAG, e.toString());}

		}
		catch (IOException e)
		{Log.e(TAG, e.toString());}
		finally
		{
			try
			{
				os.close();
				is.close();
			}
			catch (IOException e)
			{Log.e(TAG, e.toString());}
			catch (NullPointerException e)
			{Log.e(TAG, e.toString());}
			httpcon.disconnect();
			Log.i(TAG, "disconnected from " + httpcon.toString());
			return true;
		}
	}
}
