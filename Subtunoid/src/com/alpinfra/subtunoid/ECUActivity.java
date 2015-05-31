package com.alpinfra.subtunoid;

import java.text.DecimalFormat;

import com.alpinfra.subtunoid.comm.BTCommECU;
import com.alpinfra.subtunoid.graph.Graph;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;


public class ECUActivity extends CustomActivity
{	
	//Log
	String TAG = "Subtunoid-ECUActivity";
	
	// TextView
	TextView tvIAT;
	TextView tvLoad;
	TextView tvKnock;
		
	// Graph	 	 
	Graph LoadGraph;
	Graph FBKCGraph;
	Graph FLKCGraph;
					
	// Update GUI	
	final Runnable myRunnable = new Runnable() 
	{
		public void run() 
		{						
			tvIAT.setText(String.valueOf(((BTCommECU)btComm).IAT)+"°");					
			tvLoad.setText(String.valueOf(new DecimalFormat("#.##").format(((BTCommECU)btComm).Load)));							
			tvKnock.setText(String.valueOf(new DecimalFormat("#.##").format(LoadGraph._graphViewSeries.getMaxY())));
			
			ViewPager myPager = (ViewPager) activity.findViewById(R.id.panelpager);
			
			// change la page active en cas d'alarme
			if (LoadGraph.addData(((BTCommECU)btComm).Load)) myPager.setCurrentItem(1);
			if (FBKCGraph.addData(-((BTCommECU)btComm).FBKC)) myPager.setCurrentItem(1);
			if (FLKCGraph.addData(-((BTCommECU)btComm).FLKC)) myPager.setCurrentItem(1);																				
		}
	};
		
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);	
		
		btComm = new BTCommECU(activity,myHandler,myRunnable);	
		
		// Graph	    	   
		LoadGraph = new Graph(activity, "Load", 0, 2.8, 2.45, 2.5);	   
		FBKCGraph = new Graph(activity, "FBKC", 0, 2.46, 2.10, 2.15);	   	   
		FLKCGraph = new Graph(activity, "FLKC", 0, 2.46, 2.10, 2.15);

		Log.d(TAG, "...onCreate");
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		View view = inflater.inflate(R.layout.activityecu, container, false);
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
