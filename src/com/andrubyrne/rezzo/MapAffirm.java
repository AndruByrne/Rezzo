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


    @Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_affirm);
		try
		{
			MapsInitializer.initialize(this);
		}
		catch (GooglePlayServicesNotAvailableException impossible)
		{
			/* Impossible */
		}
		Intent i = this.getIntent();
		latLng = new LatLng(i.getDoubleExtra("Latitude", 0.0), i.getDoubleExtra("Longitude", 0.0));
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

	public CancelableCallback myCancelableCallback = new CancelableCallback(){
		
		@Override
		public void onFinish()
		{
			if (++aniStep < 4)
			{
				mMap.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.builder()
						 .target(latLng)
						 .zoom(6*aniStep)
						 .bearing(120*aniStep)
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
