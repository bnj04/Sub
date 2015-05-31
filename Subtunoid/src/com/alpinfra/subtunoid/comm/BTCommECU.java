package com.alpinfra.subtunoid.comm;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;
import com.alpinfra.subtunoid.MainActivity;
import com.alpinfra.subtunoid.ZeitronixActivity;
import android.content.Context;
import android.os.Handler;
import android.util.Log;


public class BTCommECU extends CustomBTComm
{
	private static final String TAG = "Subtunoid-BTCommECU";
		
	// MAC-address of Bluetooth module for ECU		
	public static String addressBTECU = "00:01:95:16:A4:B7";
	public static int rate = 150; 
		
	public double RPM;
	public double IAT;
	public double Load;
	public double FBKC;
	public double FLKC;
	public double FLTO;
	public double PreviousFLTO;
	public double PreviousFLKC;
	public double PreviousFBKC;
	
	public BTCommECU(Context context, Handler myHandler, Runnable myRunnable)
	{	
		super(addressBTECU, rate, context, myHandler, myRunnable);
		Log.d(TAG, "...onCreation...");
		tt = new TimerTask() 
		{			
			@Override
			public void run() 
			{	    	
				Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
				MainActivity ma;
				ZeitronixActivity zeintronixact;										
				
				sendData();
				
																	
				List<Integer> b = readdata(inStream); 				
				if (b != null)
				{
					// debug													
					RPM = (b.get(6) + b.get(5) * 0xff) / 4 ;
					IAT = b.get(7) - 40;					
					Load = (b.get(9) + b.get(8) * 0xff) * .00006103515625;				    				  
				    
					PreviousFBKC = FBKC;
			    	PreviousFLKC = FLKC;
			    	PreviousFLTO = FLTO;			    	
			    	FLTO = b.get(12)+1;	
					
				    if (Load > 0.8)
				    {				    	
				    	FBKC = b.get(10)*.3515625 - 45;
					    FLKC = b.get(11)*.3515625 - 45;				    					    	
				    }
				    else
				    {
				    	FBKC = 0;
				    	FLKC = 0;
				    }
				    
				    if (FBKC < PreviousFBKC - 2.10)
				    {
				    					    		   	
				    	ke.time = sdf.format(new Date());	
				    	ke.type = "FBKC";
				    	ke.rpm = RPM;
				    	ke.value = FBKC;
				    	ke.load = Load;
				    	ma = (MainActivity) _context;
				    	zeintronixact = (ZeitronixActivity) ma.fragments.get(0);
				    	ke.afr = ((BTCommZeitronix)zeintronixact.btComm).AFRv;
				    }
				    
				    // /!\ FLTO
				    if ((FLKC < PreviousFLKC - 2.10) && (PreviousFLTO == FLTO))
				    {
				    	ke.time = sdf.format(new Date());	
				    	ke.type = "FLKC";
				    	ke.rpm = RPM;
				    	ke.value = FLKC;
				    	ke.load = Load;		
				    	ma = (MainActivity) _context;
				    	zeintronixact = (ZeitronixActivity) ma.fragments.get(0);
				    	ke.afr = ((BTCommZeitronix)zeintronixact.btComm).AFRv;
				    }				    				  				 
				}
				else
				{
					IAT = 0;
				}
				_myHandler.post(_myRunnable);	
				
			}			
		};			
	}

	private void sendData() 
	{
	    try 
	    {	    		    
	    	/* 

	    	;P8 - Engine Speed(rpm)
	    	paramname = Engine_Speed(rpm)
	    	paramid = 0x00000E
	    	databits = 16
	    	scalingrpn = x,4,/
	    	0E 0F rpm

	    	;P11 - Intake Air Temperature(C)
	    	paramname = Intake_Air_Temperature(C)
	    	paramid = 0x000012
	    	scalingrpn = x,40,-

	    	;E2 - Engine Load*(g/rev)
	    	paramname = Engine_Load*(g/rev)
	    	paramid = 0x0200B4
	    	databits = 16
	    	scalingrpn = x,.00006103515625,*

	    	;E10 - Feedback Knock Correction*(degrees)
	    	paramname = Feedback_Knock_Correction*(degrees)
	    	paramid = 0x020CAE
	    	scalingrpn = x,.3515625,*,45,-

	    	;E12 - Fine Learning Knock Correction*(degrees)
	    	paramname = Fine_Learning_Knock_Correction*(degrees)
	    	paramid = 0x020CB3
	    	scalingrpn = x,.3515625,*,45,-

	    	;P13" name="Throttle Opening Angle" desc="P13-Engine throttle opening angle." ecubyteindex="9" ecubit="3" target="1">
	    	 0x000015
	    	<conversion units="%" expr="x*100/255" format="0.00" />
	    	
	    	;E26" name="Fine Learning Table Offset*" desc="E26-Current index position for the Fine Learning Knock Correction table being applied (and potentially adjusted), stored in ram." target="1">
	    	0x020CB4
	    	<conversion units="index position" expr="x+1" format="0" />
	    	                                   
	    	*/	    		    
	    	
	    	//int[] sentValues = {0x80, 0x10, 0xF0, 0x1A, 0xA8, 0x00, 0x00, 0x00, 0x0E, 0x00, 0x00, 0x0F, 0x00, 0x00, 0x12, 0x02, 0x00, 0xb4, 0x02, 0x00, 0xb5, 0x02, 0x0c, 0xae, 0x02, 0x0c, 0xb3, 0x00, 0x00, 0x15};
	    	  
	    	//int[] sentValues = {0x80, 0x10, 0xF0, 0x05, 0xA8, 0x00, 0x00, 0x00, 0x0E, 0x3B};
	    	//8010F005A80000000E3B
	    	
	    	int[] sentValues = {0x80, 0x10, 0xF0, 0x1A, 0xA8, 0x00, 0x00, 0x00, 0x0e, 0x00, 0x00, 0x0f, 0x00, 0x00, 0x12, 0x02, 0x00, 0xb4, 0x02, 0x00, 0xb5, 0x02, 0x0c, 0xae, 0x02, 0x0c, 0xb3, 0x02, 0x0c, 0xb4, 0x1d};
	    	for (int i : sentValues)
	    	{
	    		outStream.write(i);
	    	}	         
	        outStream.flush();	        
	         
	    } 
	    catch (IOException e) 
	    {
	        //e.printStackTrace();
	    	Log.d(TAG, "Error during sendDataECU");
	    }
	}		
	
	private List<Integer> readdata(InputStream is) 
	{		
		int responseSize = 15;
		List<Integer> buffer = new ArrayList<Integer>(responseSize);
		boolean stop = false;
		boolean packetStarted = false;
		
		try {
			if (is.available() >= 44)
			{
				Log.d(TAG, "...sup..."+is.available());
			}
			else
			{
				Log.d(TAG, "...not sup..."+is.available());
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		
		try 
		{
			while ((!stop ) && (is.available()>responseSize))
			//long startTime = System.currentTimeMillis();
			//while ((!stop) && (System.currentTimeMillis()-startTime<15))				
			{
				byte[] ba = new byte[1];
				is.read(ba);
				int b = ba[0] & 0xff;

				if (b == 0x10
						&& buffer.size() >= 2
						&& buffer.get(buffer.size() - 1) == 0xF0
						&& buffer.get(buffer.size() - 2) == 0x80) 
				{
					packetStarted = true;
					buffer.clear();
					buffer.add(0x80);
					buffer.add(0xF0);
					buffer.add(b);
				} 
				else 
				{
					if (packetStarted && buffer.size() <= responseSize) 
					{
						buffer.add(b); 
						if (buffer.size() == responseSize)
						{
							packetStarted = false;
							stop = true;
							//Log.d(TAG, "...Reading...");
						}
					} 
					else 
					{
						buffer.add(b);
						packetStarted = false;
					} 
				}
			}
		} 
		catch (IOException e) 
		{
			//e.printStackTrace();
			Log.d(TAG, "(is.available()>0) is.read(ba);");
			stop = true;
		}
		
		try
		{
			if (calculcrc(buffer) == buffer.get(buffer.get(3)+4))
			{			
				//Log.d(TAG, "...CRC OK...");
			}
			else
			{
				Log.d(TAG, "=>...Bad CRC...");
				buffer = null;
			}
		}
		catch (Exception e)
		{
			//e.printStackTrace();
			Log.d(TAG, "==>...Bad CRC...");
			buffer = null;
		}
							
		return buffer;		  
	}
	
	private int calculcrc(List<Integer> data)
	{
		int t = 0;
		if (data.size()>4)
		{
			if ((data.get(3) == 9) && (data.get(3)+4 <= data.size()-1))
			{		   			
				for (int i=0;i<data.get(3)+4;i++)
				{      
					t += data.get(i);     
					t = t & 0xff;
				}
		    }
			else
			{
				t = -1;
			}
		}
		else
		{
			t = -1;
		}		
	   return t;
	}
	 	
}
