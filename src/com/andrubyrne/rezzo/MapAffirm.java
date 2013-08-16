package com.andrubyrne.rezzo;
import android.app.*;
import android.graphics.*;
import android.os.*;
import android.view.*;
import android.view.animation.*;
import android.widget.*;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.CancelableCallback;

import android.graphics.Interpolator;
import android.content.*;
import com.google.android.gms.common.*;
import android.util.*;

public class MapAffirm extends Activity
{
	//  private ImageView imageView;
	private GoogleMap map;
	LatLng latLng;
	LatLng newLatLng;
	CameraPosition cameraPosition;
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
	int stepSpinMax = 4;
	//number of degrees in stepSpin
	int stepSpinDetent = 360 / stepSpinMax;

	Intent detailIntent;
	Intent intent;
	Bundle bundle;
	boolean batch;		
	Marker marker;
	private final String TAG = getClass().getSimpleName();
	final int mapHopDelay = 2000;

    @Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_affirm);
		try
		{MapsInitializer.initialize(this);}
		catch (GooglePlayServicesNotAvailableException impossible)
		{	/* Impossible */ Log.e(TAG, "the impossible occurred");}
	    intent = this.getIntent();
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
																		//	 .bearing(40*aniStep)
																		//	 .tilt(60)
																		.build()), mapHopDelay, cameraAnimation);

			}
			else if (stepZoom >= stepZoomMax)// ending position hard coded for this application
			{map.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.builder()
																	 .target(latLng)
																	 .zoom(18)
																	 //	 .bearing(0)
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
