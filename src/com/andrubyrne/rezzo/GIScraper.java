package com.andrubyrne.rezzo;
import android.app.*;
import android.os.*;
import android.view.*;
import android.content.*;
import android.widget.*;

public class GIScraper extends Activity
{
    ImageView imageView;
    public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_affirm);
		//imageView = (ImageView)findViewById(R.id.imageViewSmaller);
		
	}
	public void launchScraper(View v){
		Intent i = new Intent(this, MapAffirm.class);
		startActivity(i);
	}
}
