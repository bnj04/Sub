package com.alpinfra.subtunoid;


import java.util.Vector;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;


public class MainActivity extends FragmentActivity 
{
	// Log
	private static final String TAG = "Subtunoid";

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Vector<Fragment> fragments = new Vector<Fragment>();
		
		ZeitronixActivity zeitronixactivity = new ZeitronixActivity();		
		fragments.add(zeitronixactivity);
		
		ECUActivity ecuactivity = new ECUActivity();			
		fragments.add(ecuactivity);
		
	

		ViewPager myPager = (ViewPager) findViewById(R.id.panelpager);
		FragmentAdapter adapter = new FragmentAdapter(getSupportFragmentManager(), fragments);
		myPager.setAdapter(adapter);
		myPager.setCurrentItem(1);

		myPager.setOnPageChangeListener(new OnPageChangeListener() 
		{

			@Override
			public void onPageSelected(int arg0) 
			{
				Toast.makeText(MainActivity.this,
						"Page Selected " + arg0, Toast.LENGTH_LONG).show();
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) 
			{
			}

			@Override
			public void onPageScrollStateChanged(int arg0) 
			{
			}
		});

		

		Log.d(TAG, "...onCreate");
	}

	@Override
	public void onResume() 
	{
		super.onResume();	    
		//btcZ.onResume();
		
	}

	@Override
	public void onPause() 
	{		
		//btcZ.onPause();
		
		super.onPause();	  	    	    	  
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
