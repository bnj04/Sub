package com.alpinfra.subtunoid;

import java.text.DecimalFormat;

import com.alpinfra.subtunoid.comm.BTCommZeitronix;
import com.alpinfra.subtunoid.graph.Graph;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;


public class ZeitronixActivity extends android.support.v4.app.Fragment 
{
	Activity activity; 
	
	// Log
	private static final String TAG = "Subtunoid-Zeitronix";

	// Communication bluetooth
	public BTCommZeitronix zeitronixcomm;
	

	// TextView
	TextView tvEGT;
	TextView tvAFR;
	TextView tvBoost;	  	 

	// Graph	 	 
	Graph EGTGraph;
	Graph AFRGraph;
	Graph BoostGraph;
	
	public ZeitronixActivity() 
	{
	}

	// Update GUI
	final Handler myHandler = new Handler(); 
	final Runnable myRunnable = new Runnable() 
	{
		public void run() 
		{						
			//tvEGT.setText(String.valueOf(zeitronixcomm.EGTv)+"°");		        
			tvEGT.setText(String.valueOf(new DecimalFormat("###").format(EGTGraph._graphViewSeries.getMaxY()))+"°");		
			
			//tvAFR.setText(String.valueOf(zeitronixcomm.AFRv));
			tvAFR.setText(String.valueOf(new DecimalFormat("##.#").format(AFRGraph._graphViewSeries.getMinY())));
			
			//tvBoost.setText(new DecimalFormat("#.##").format(zeitronixcomm.Boostv));	
			tvBoost.setText(new DecimalFormat("#.##").format(BoostGraph._graphViewSeries.getMaxY()));
							
			ViewPager myPager = (ViewPager) activity.findViewById(R.id.panelpager);		
			
			if (EGTGraph.addData(zeitronixcomm.EGTv)) myPager.setCurrentItem(0);	       
			if (AFRGraph.addData(zeitronixcomm.AFRv)) myPager.setCurrentItem(0);
			if (BoostGraph.addData(zeitronixcomm.Boostv)) myPager.setCurrentItem(0);	
			
				
			
		}
	};		


	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		activity = getActivity();
		//setContentView(R.layout.activity_main);
		 
		// Communication
		zeitronixcomm = new BTCommZeitronix(activity,myHandler,myRunnable);

		// Graph	    	   
		EGTGraph = new Graph(activity, "EGT", 200, 900, 800, 850);	   
		AFRGraph = new Graph(activity, "AFR", 9, 16, 0, 0);	   	   
		BoostGraph = new Graph(activity, "Boost", 0, 1.4, 1.25, 1.3);
		
		Log.d(TAG, "...onCreate");
	}

	@Override
	public void onResume() 
	{
		super.onResume();	    		
		zeitronixcomm.onResume();
	}

	@Override
	public void onPause() 
	{		
		zeitronixcomm.onPause();
		super.onPause();	  	    	    	  
	}

		
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		View view = inflater.inflate(R.layout.activityzeitronix, container, false);
		
		tvEGT = (TextView)view.findViewById(R.id.EGT);
		tvAFR = (TextView)view.findViewById(R.id.AFR);
		tvBoost = (TextView)view.findViewById(R.id.Boost);       
		
		LinearLayout layout = (LinearLayout) view.findViewById(R.id.graph1);	    
		layout.addView(EGTGraph._graphView);	  

		layout = (LinearLayout) view.findViewById(R.id.graph2);	    
		layout.addView(AFRGraph._graphView);

		layout = (LinearLayout) view.findViewById(R.id.graph3);	    
		layout.addView(BoostGraph._graphView);	  
		
			
		return view;
	}
}
