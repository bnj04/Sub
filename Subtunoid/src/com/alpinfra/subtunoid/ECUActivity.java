package com.alpinfra.subtunoid;

import java.text.DecimalFormat;

import com.alpinfra.subtunoid.comm.BTCommECU;
import com.alpinfra.subtunoid.graph.Graph;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;


public class ECUActivity extends android.support.v4.app.Fragment 
{
	Activity activity; 
	//Log
	private static final String TAG = "Subtunoid-ECU";

	// Communication bluetooth	
	BTCommECU ecucomm;

	// TextView
	TextView tvIAT;
	TextView tvLoad;
	TextView tvKnock;
		
	// Graph	 	 
	Graph LoadGraph;
	Graph FBKCGraph;
	Graph FLKCGraph;
		
		
	public ECUActivity() 
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
	
	

	// Update GUI
	final Handler myHandler = new Handler(); 
	final Runnable myRunnable = new Runnable() 
	{
		public void run() 
		{						
			tvIAT.setText(String.valueOf(ecucomm.IAT)+"°");							
			tvLoad.setText(String.valueOf(new DecimalFormat("#.##").format(ecucomm.Load)));							
			tvKnock.setText(String.valueOf(ecucomm.FLKC));
						
			
			LoadGraph.addData(ecucomm.Load);			
			FBKCGraph.addData(-ecucomm.FBKC);
			
			if (ecucomm.PreviousFLTO != ecucomm.FLTO)
			{
				FLKCGraph.addData(-ecucomm.FLKC);
			}
		}
	};
	
	
	

	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);	
		activity = getActivity();		
		ecucomm = new BTCommECU(activity,myHandler,myRunnable);	
		
		// Graph	    	   
		LoadGraph = new Graph(activity, "Load", 0, 2.8, 2.45, 2.5);	   
		FBKCGraph = new Graph(activity, "FBKC", 0, 2.46, 0.50, 2);	   	   
		FLKCGraph = new Graph(activity, "FLKC", 0, 2.46, 0.50, 2);

		Log.d(TAG, "...onCreate");
	}

	@Override
	public void onResume() 
	{
		super.onResume();	    		
		ecucomm.onResume();
	}

	@Override
	public void onPause() 
	{		
		ecucomm.onPause();
		super.onPause();	  	    	    	  
	}
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		View view = inflater.inflate(R.layout.activityecu, null);
		tvIAT = (TextView)view.findViewById(R.id.IAT);		
		tvLoad = (TextView)view.findViewById(R.id.LOAD);
		tvKnock = (TextView)view.findViewById(R.id.Knock);
		
		LinearLayout layout = (LinearLayout) view.findViewById(R.id.graph1ECU);	    
		layout.addView(LoadGraph._graphView);	  

		layout = (LinearLayout) view.findViewById(R.id.graph2ECU);	    
		layout.addView(FBKCGraph._graphView);

		layout = (LinearLayout) view.findViewById(R.id.graph3ECU);	    
		layout.addView(FLKCGraph._graphView);	  
		return view;
	}
}
