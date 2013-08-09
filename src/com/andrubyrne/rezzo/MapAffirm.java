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
	private GoogleMap mMap;
	LatLng latLng;
	LatLng newLatLng;
	CameraPosition cameraPosition;
	int aniStep = 1;
	Intent detailIntent;
	Intent intent;
	Bundle bundle;
	boolean batch;		
	Marker marker;
	private final String TAG = getClass().getSimpleName();
	
    @Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_affirm);
		try{MapsInitializer.initialize(this);}
		catch (GooglePlayServicesNotAvailableException impossible){	/* Impossible */ Log.e(TAG, "the impossible occurred");}
	    intent = this.getIntent();
		latLng = new LatLng(intent.getDoubleExtra("Latitude", 0.0), intent.getDoubleExtra("Longitude", 0.0));
		mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
    	mMap.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.builder()
																 .target(latLng)
																 .zoom(6)
																 .bearing(60)
																 .tilt(60)
																 .build()), cancelableCallback);

		marker = mMap.addMarker(new MarkerOptions()
		               .draggable(true)
					   .position(latLng)
					   .title("Location of Photographer"));

	}
	@Override
	public void onResume(){
		super.onResume();
		detailIntent = new Intent(this, PointDetail.class);
		intent = this.getIntent();
		bundle = intent.getExtras();
		detailIntent.putExtras(bundle);
	}

	public CancelableCallback cancelableCallback = new CancelableCallback(){
		
		@Override
		public void onFinish()
		{
			if (++aniStep < 10)
			{
				mMap.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.builder()
						 .target(latLng)
						 .zoom(2*aniStep)
						 .bearing(40*aniStep)
						 .tilt(60)
						 .build()), cancelableCallback);
			} else {mMap.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.builder()
																			 .target(latLng)
																			 .zoom(18)
																			 .bearing(0)
																			 .tilt(0)
																			 .build()));
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
}
