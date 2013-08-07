package com.andrubyrne.rezzo;
import android.app.*;
import android.os.*;
import android.widget.*;

public class ParseCache extends Activity
{
    ImageView imageView;
	TextView textView;
	
    @Override
    public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.parse_cache);
		imageView = (ImageView)findViewById(R.id.imageViewSmaller);
		textView = (TextView)findViewById(R.id.mapInstruction);
	}
	
	@Override
	public void onResume(){
		super.onResume();
	}
}
