package com.andrubyrne.rezzo;
import android.app.*;
import android.graphics.*;
import android.os.*;
import android.view.*;
import android.view.animation.*;
import android.widget.*;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;

import android.graphics.Interpolator;

public class MapAffirm extends Activity
{
  //  private ImageView imageView;
	private GoogleMap mMap;
    @Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_affirm);
		Toast.makeText(getBaseContext(),
					   "giscraper launched", Toast.LENGTH_LONG)
			.show();
		mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
	}
	
	public void goodPoint(View v){

	mMap.addMarker(new MarkerOptions()
		.position(new LatLng(0, 0))
		.title("Hello world"));
	}
	
	public void animateMarker(final Marker marker, final LatLng toPosition,
							  final boolean hideMarker) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = mMap.getProjection();
        Point startPoint = proj.toScreenLocation(marker.getPosition());
        final LatLng startLatLng = proj.fromScreenLocation(startPoint);
        final long duration = 500;

        final LinearInterpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
				@Override
				public void run() {
					long elapsed = SystemClock.uptimeMillis() - start;
					float t = interpolator.getInterpolation((float) elapsed
															/ duration);
					double lng = t * toPosition.longitude + (1 - t)
                        * startLatLng.longitude;
					double lat = t * toPosition.latitude + (1 - t)
                        * startLatLng.latitude;
					marker.setPosition(new LatLng(lat, lng));

					if (t < 1.0) {
						// Post again 16ms later.
						handler.postDelayed(this, 16);
					} else {
						if (hideMarker) {
							marker.setVisible(false);
						} else {
							marker.setVisible(true);
						}
					}
				}
		});
    }
}
