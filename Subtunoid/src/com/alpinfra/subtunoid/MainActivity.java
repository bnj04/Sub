package com.alpinfra.subtunoid;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Vector;
import com.alpinfra.subtunoid.comm.KnockEvent;
import com.alpinfra.subtunoid.knocklistener.KnockListener;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Toast;



public class MainActivity extends FragmentActivity 
{
	// Log
	private static final String TAG = "Subtunoid";
	
	private float pressedX;
	private float pressedY;
	public Vector<Fragment> fragments;
	
	private KnockListener kl;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);						
		
		fragments = new Vector<Fragment>();		
		ZeitronixActivity zeitronixactivity = new ZeitronixActivity();		
		fragments.add(zeitronixactivity);		
		ECUActivity ecuactivity = new ECUActivity();			
		fragments.add(ecuactivity);
		
		AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
		audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 100, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
		kl = new KnockListener();
				
		ViewPager myPager = (ViewPager) findViewById(R.id.panelpager);
		FragmentAdapter adapter = new FragmentAdapter(getSupportFragmentManager(), fragments);
		myPager.setAdapter(adapter);		

		myPager.setOnPageChangeListener(new OnPageChangeListener() 
		{

			@Override
			public void onPageSelected(int arg0) 
			{
				String s;
				if (arg0 == 1)
				{
					s = "ECU";
				}
				else
				{
					s = "Zeitronix";
				}
				Toast.makeText(MainActivity.this, s + " page selected", Toast.LENGTH_LONG).show();
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
		
		
		myPager.setOnTouchListener(new OnTouchListener() {
			
			@SuppressLint("ClickableViewAccessibility")
			@Override
			public boolean onTouch(View v, MotionEvent event) 
			{
				
				int eventaction = event.getAction();
								  
			    switch (eventaction) 
			    {

			    case MotionEvent.ACTION_DOWN:
			    	//Log.d(TAG, "Down");
			    	pressedX = event.getX();
			        pressedY = event.getY();					    			     
			        break;
			    case MotionEvent.ACTION_UP:
			    	//Log.d(TAG, "up");
			    	pressedX = pressedX - event.getX();
			        pressedY = pressedY - event.getY();			        
			        if (pressedY < -200)
			        {
				        //Log.d(TAG, "x=" + pressedX + "-" + "y="+ pressedY);
			        	saveknock();
				        return true;
			        }		
			        if (pressedY > 200)
			        {
				        //Log.d(TAG, "x=" + pressedX + "-" + "y="+ pressedY);
			        	clearsavedknock();
				        return true;
			        }		
			        break;
			    }				
				return false;
			}
		});
		

		Log.d(TAG, "...onCreate");
	}
	
	   @Override
       public boolean onTouchEvent(MotionEvent event) {
       // TODO Auto-generated method stub
		   Toast.makeText(this, "rr", Toast.LENGTH_LONG).show();
       return super.onTouchEvent(event);
        }

	@Override
	public void onResume() 
	{
		super.onResume();	
					
		if (BluetoothAdapter.getDefaultAdapter().isEnabled()) 			
		{
			Log.d(TAG, "...Bluetooth ON...");
		} 
		else 
		{
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, 1);
		}				
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
		if (id == R.id.action_Quitter) 
		{
			finish();
			return true;
		}
		if (id == R.id.action_KnockListener) 
		{
			// enable or disable knock listener
			boolean b = kl.toogle();
			if (b)
			{
				Toast.makeText(MainActivity.this,"Enabling Knock listening...", Toast.LENGTH_LONG).show();
			}
			else
			{
				Toast.makeText(MainActivity.this,"Disabling Knock listening...", Toast.LENGTH_LONG).show();
			}
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@SuppressLint("SimpleDateFormat")
	public void saveknock()
	{
		Log.d(TAG, "Saving knock event...");
		ECUActivity ecuact = (ECUActivity) fragments.get(1);
		
		KnockEvent ke = ecuact.btComm.getlastknockevent();
		
		if (ke.time != null)
		{
			Toast.makeText(MainActivity.this,"Saving knock event...", Toast.LENGTH_LONG).show();
			try
			{							
				File logFile = new File("/storage/extSdCard/subtunoid/log.txt");												
				if (!logFile.exists())
				{
					logFile.createNewFile();
					logFile.setWritable(true, false);
				}
				FileWriter fw = new FileWriter(logFile, true);
				BufferedWriter writer = new BufferedWriter(fw);							
				writer.write(ke.time + ";"+ke.type+";"+String.valueOf(new DecimalFormat("####").format(ke.rpm))+";"+String.valueOf(new DecimalFormat("#.##").format(ke.load))+";"+String.valueOf(new DecimalFormat("#.##").format(ke.value))+";"+String.valueOf(new DecimalFormat("##.#").format(ke.afr))+"\r\n");
				writer.close();
				fw.close();					
				ke.time = null;
			}
			catch (IOException e)
			{
				Log.e(TAG, "Unable to write to the LogFile.");
			}
		}							
	}
	
	public void clearsavedknock()
	{
		ECUActivity ecuact = (ECUActivity) fragments.get(1);		
		KnockEvent ke = ecuact.btComm.getlastknockevent();
		if (ke.time != null)
		{		
			ke.time = null;
			Toast.makeText(MainActivity.this,"last knock event cleared...", Toast.LENGTH_LONG).show();
		}
	}
	
	
	
}
