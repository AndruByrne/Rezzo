package com.andrubyrne.utils;
import android.widget.*;
import java.util.*;
import android.content.*;
import android.view.*;
import com.andrubyrne.rezzo.*;

public class ResourcesAdapter extends ArrayAdapter<String>
{

	private ArrayList<String> items;
	private Context context;

	public ResourcesAdapter(Context context, int textViewResourceId, ArrayList<String> items)
	{
		super(context, textViewResourceId, items);
		this.items = items;
		this.context = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		View v = convertView;
		if (v == null)
		{
			LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.res_row, null);
		}
		String s = items.get(position);
		if (s != null)
		{
			TextView tt = (TextView) v.findViewById(R.id.toptext);
			if (tt != null)
			{
				tt.setText(s);                            
				}
		}
		return v;
	}
    
	@Override
	public ArrayList<String> getItems(){
		return this.items;
	}
}
