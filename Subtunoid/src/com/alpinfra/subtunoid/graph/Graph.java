package com.alpinfra.subtunoid.graph;

import java.text.DecimalFormat;
import android.content.Context;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import com.alpinfra.subtunoid.R;
import com.jjoe64.graphview.CustomLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;
import com.jjoe64.graphview.GraphViewStyle.GridStyle;

public class Graph 
{
	public GraphViewSeries _graphViewSeries;
	private GraphViewSeriesStyle _graphViewSeriesStyle;
	public GraphView _graphView;
	private int x=1;
	private int xwe;
	private double _miny;
	private double _maxy;
	private double _warning = 0;
	private double _error = 0;
	private Context _context;
	public Boolean enableAlarm = true;
	public int AlarmDuration = 30;
	
	
	
	public Graph(Context context,String name,double miny, double maxy, double warning, double error)
	{
		_context = context;
		_miny = miny;
		_maxy = maxy;
		_error = error;
		_warning = warning;
		if ((_error == 0) && (_warning == 0))
		{
			enableAlarm = false;
		}

		_graphViewSeriesStyle = new GraphViewSeriesStyle(Color.rgb(0, 0, 255), 4);		
		_graphViewSeries = new GraphViewSeries(name,_graphViewSeriesStyle,new GraphViewData[] { new GraphViewData(1, 0) });		
		_graphView = new LineGraphView(context, name);
		_graphView.setScalable(true);
		_graphView.setViewPort(1,50);
		_graphView.addSeries(_graphViewSeries);	   	    	   
		_graphView.getGraphViewStyle().setGridStyle(GridStyle.HORIZONTAL);	    
		_graphView.getGraphViewStyle().setNumVerticalLabels(8);
		_graphView.setShowHorizontalLabels(false);
		_graphView.setDisableTouch(true);	  	  
		_graphView.setManualYAxisBounds(_maxy, _miny);
		
		if ((name == "Boost") || (name == "Load")|| (name == "FBKC") || (name == "FLKC"))
		{
		_graphView.setCustomLabelFormatter(
				new CustomLabelFormatter()
                {
                        public String formatLabel(double value, boolean isValueX)
                        {
                                if (!isValueX)
                                {                                      
                                       
                                                return new DecimalFormat("#.#").format(value);                                                                                                          
                                }
                                return null;
                        }
                }
                ); 
		}
		else
		{
			_graphView.setCustomLabelFormatter(
		    		new CustomLabelFormatter() 
		    		{ 
		    			public String formatLabel(double value, boolean isValueX) 
		    			{ 
		    				if (!isValueX)
		    				{ 
		    					if (value >= 100)
		    					{
		    						return String.valueOf(value).substring(0,3);	    						
		    					}
		    					else if (value >= 10)
		    					{
		    						return String.valueOf(value).substring(0,2);
		    					}
		    					else
		    					{
		    						return " "+String.valueOf(value).substring(0,1);
		    					}
		    				} 
		    				return null; 
		    			} 
		    		}
		    		); 	 		
		}
		 
	}
	
	public boolean addData(double v)
	{
		_graphViewSeries.appendData(new GraphViewData(++x, v),true,50);		
		if (enableAlarm)
		{
			if (v > _error) 
			{
				_graphViewSeriesStyle.color = Color.RED;
				playAlarm();
				xwe = x;
				return true;
			}
			else if (v > _warning) 
			{
				_graphViewSeriesStyle.color = Color.rgb(255, 128, 0); //Orange
				playAlarm();
				xwe = x;
				return true;
			}
			else if (x > xwe + AlarmDuration)
			{
				_graphViewSeriesStyle.color = Color.BLUE;
			}
		}
		return false;
	}
	
	private void playAlarm() 
	 {		 
		
		MediaPlayer mp = MediaPlayer.create(_context, R.raw.error);
		 mp.start();
		 mp.setOnCompletionListener(new OnCompletionListener() 
		 {
			 @Override
			 public void onCompletion(MediaPlayer mp) 
			 {
				 mp.release();
			 }
		 });
	 }
	

}
