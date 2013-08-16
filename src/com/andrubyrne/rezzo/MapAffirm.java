package com.andrubyrne.rezzo;
import android.app.*;
import android.content.*;
import android.os.*;
import android.preference.*;
import android.util.*;
import android.view.*;
import com.andrubyrne.rezzo.*;
import com.google.android.gms.common.*;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.GoogleMap.*;
import com.google.android.gms.maps.model.*;

public class MapAffirm extends Activity
{
	//map object
	private GoogleMap map;
	//from extras
	LatLng latLng;
	//user defined latLng
	LatLng newLatLng;
	//camera location object
	CameraPosition cameraPosition;
	//pause between map travels
	final int mapHopDelay = 2000;
	//initial zoom
	static final int initZoom = 8;
	//steps the zoom
	int stepZoom = 0;
	// number of steps in zoom, be careful with this number!
	int stepZoomMax = 5;
	//number of .zoom steps in a step
	int stepZoomDetent = (18 - initZoom) / stepZoomMax;
	//when topause zoom for spin
	int stepToSpin = 4;
	//steps the spin
	int stepSpin = 0;
	//number of steps in spin (factor of 360)
	int stepSpinMax = 8;
	//number of degrees in stepSpin
	int stepSpinDetent = 360 / stepSpinMax;
    //intent to pass
	Intent detailIntent;
	//intent to receive
	Intent intent;
	//bundle to transfer extras
	Bundle bundle;
	//set from extras
	boolean batch;	
    //token for user to describe where the camera was	
	Marker marker;
	//debug tag
	private final String TAG = getClass().getSimpleName();
	boolean animate;
	SharedPreferences preferences;
	
    @Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_affirm);
	    intent = this.getIntent();
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		if(preferences.getBoolean("use_ani", true) == false) stepZoom = 666;
		try
		{MapsInitializer.initialize(this);}
		catch (GooglePlayServicesNotAvailableException impossible)
		{	/* Impossible */ Log.e(TAG, "the impossible occurred");}
		latLng = new LatLng(intent.getDoubleExtra("Latitude", 0.0), intent.getDoubleExtra("Longitude", 0.0));
		map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
    	map.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.builder()
																.target(latLng)
																.zoom(initZoom-1)
																.build())
						  , mapHopDelay
						  , cameraAnimation
						  );
		marker = map.addMarker(new MarkerOptions()
							   .draggable(true)
							   .position(latLng)
							   .title("Location of Photographer"));

	}

	public CancelableCallback cameraAnimation = new CancelableCallback(){

		@Override
		public void onFinish()
		{
			if (stepZoom < stepZoomMax && stepZoom != stepToSpin)
			{
				stepZoom++;
				map.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.builder()
																		.target(latLng)
																		.zoom(initZoom + (stepZoomDetent * (stepZoom - 1)))
																		.build()), mapHopDelay, cameraAnimation);

			}
			else if (stepZoom >= stepZoomMax)// ending position hard coded for this application
			{map.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.builder()
																	 .target(latLng)
																	 .zoom(18)
																	 .tilt(0)
																	 .build()));
			}
			else
			{
				if (stepSpin <= stepSpinMax)
				{
					stepSpin++;
					map.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.builder()
																			.target(latLng)
																			.zoom(initZoom + stepZoomDetent * stepZoom)
																			.bearing(stepSpinDetent * (stepSpin - 1))
																			.tilt(60)
																			.build()), mapHopDelay, cameraAnimation);
				}
				else
				{
					stepZoom++;
					map.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.builder()
																			.target(latLng)
																			.zoom(initZoom + stepZoomDetent * stepZoom)
																			.bearing(0)
																			.tilt(0)
																			.build()), mapHopDelay, cameraAnimation);
				}
			}
		}

		@Override
		public void onCancel()
		{}

	};

	public void goodPoint(View v)
	{
		newLatLng = marker.getPosition();
		detailIntent.putExtra("Latitude", newLatLng.latitude);
		detailIntent.putExtra("Longitude", newLatLng.longitude);
		startActivity(detailIntent);
		finish();
	}
	@Override
	public void onResume()
	{
		super.onResume();
		detailIntent = new Intent(this, PointDetail.class);
		intent = this.getIntent();
		bundle = intent.getExtras();
		detailIntent.putExtras(bundle);
	}

}
