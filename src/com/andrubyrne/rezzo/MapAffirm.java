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

public class MapAffirm extends Activity
{
	//  private ImageView imageView;
	private GoogleMap mMap;
	LatLng latLng;
	CameraPosition cameraPosition;
	int aniStep = 1;
	Intent homeIntent;
	Intent batchIntent;
	Intent intent;
	Bundle bundle;
	boolean batch;		
	
    @Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_affirm);
		try{MapsInitializer.initialize(this);}
		catch (GooglePlayServicesNotAvailableException impossible){	/* Impossible */	}
	    intent = this.getIntent();
		latLng = new LatLng(intent.getDoubleExtra("Latitude", 0.0), intent.getDoubleExtra("Longitude", 0.0));
		mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
    	mMap.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.builder()
																 .target(latLng)
																 .zoom(6)
																 .bearing(60)
																 .tilt(60)
																 .build()), myCancelableCallback);

		mMap.addMarker(new MarkerOptions()
					   .position(latLng)
					   .title("Location of Photographer"));

	}
	public void onResume(){
		super.onResume();
		homeIntent = new Intent(this, Home.class);
		batchIntent = new Intent(this, GIScraper.class);
		intent = this.getIntent();
		bundle = intent.getExtras();
		batch = intent.getBooleanExtra("batch", false);	
		homeIntent.putExtras(bundle);
		batchIntent.putExtras(bundle);
		
	}

	public CancelableCallback myCancelableCallback = new CancelableCallback(){
		
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
						 .build()), myCancelableCallback);
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
		if(batch){
			Toast.makeText(this, "filepath: "+intent.getStringExtra("filepath"), Toast.LENGTH_LONG).show();
		//	deleteFile(intent.getStringExtra("filepath"));
		} else startActivity(homeIntent);
		finish();
	}

	public void animateMarker(final Marker marker, final LatLng toPosition,
							  final boolean hideMarker)
	{
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = mMap.getProjection();
        Point startPoint = proj.toScreenLocation(marker.getPosition());
        final LatLng startLatLng = proj.fromScreenLocation(startPoint);
        final long duration = 500;

        final LinearInterpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
				@Override
				public void run()
				{
					long elapsed = SystemClock.uptimeMillis() - start;
					float t = interpolator.getInterpolation((float) elapsed
															/ duration);
					double lng = t * toPosition.longitude + (1 - t)
                        * startLatLng.longitude;
					double lat = t * toPosition.latitude + (1 - t)
                        * startLatLng.latitude;
					marker.setPosition(new LatLng(lat, lng));

					if (t < 1.0) handler.postDelayed(this, 16); //send again in 16 ms
					else if (hideMarker) marker.setVisible(false);
					else marker.setVisible(true);
				}
			});
    }
}
