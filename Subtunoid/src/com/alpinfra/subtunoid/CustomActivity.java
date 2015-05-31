package com.alpinfra.subtunoid;

import com.alpinfra.subtunoid.comm.CustomBTComm;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

public abstract class CustomActivity extends android.support.v4.app.Fragment
{
	Activity activity; 
	Handler myHandler = new Handler(); 
	
	String TAG = "Subtunoid-CustomActivity";
	
	//public BTCommECU ecucomm;
	//public BTCommZeitronix zeitronixcomm;
	
	public CustomBTComm btComm;
		
	public CustomActivity()
	{
		
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}
	
	@Override
	public void onResume() 
	{
		super.onResume();	
		btComm.onResume();
		/*
		if (ecucomm != null)
		{
			ecucomm.onResume();
		}
		if (zeitronixcomm != null)
		{
			zeitronixcomm.onResume();
		}*/
		
	}

	@Override
	public void onPause() 
	{		
		btComm.onPause();
		/*
		if (ecucomm != null)
		{
			ecucomm.onPause();
		}
		if (zeitronixcomm != null)
		{
			zeitronixcomm.onPause();
		}	*/		
		super.onPause();	  	    	    	  
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);	
		activity = getActivity();						
		Log.d(TAG, "...onCreate");
	}
	
	
}
