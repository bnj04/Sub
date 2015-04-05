package com.alpinfra.subtunoid.comm;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

public class BTCommECU extends Activity 
{
	private static final String TAG = "Subtunoid-BTCommECU";
	// SPP UUID service
	private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	
	// MAC-address of Bluetooth module for ECU		
	public static String addressBTECU = "00:01:95:16:A4:B7";
	
	
	private Context _context;
	private Handler _myHandler;	  		  		 
	private Runnable _myRunnable;
	
	
	// BT
	private BluetoothAdapter btAdapter = null;
	private BluetoothSocket btSocket = null;
	
	private OutputStream outStream = null;
	private InputStream inStream = null;	
	private Timer timerserial;
	private TimerTask tt;
	
	
	public double RPM;
	public double IAT;
	public double Load;
	public double FBKC;
	public double FLKC;
	public double FLTO;
	public double PreviousFLTO;
	
	public BTCommECU(Context context, Handler myHandler, Runnable myRunnable)
	{
		
		_context = context;
		_myHandler = myHandler;
		_myRunnable = myRunnable;
		
		// BT
		btAdapter = BluetoothAdapter.getDefaultAdapter();
		//checkBTState();
		
		timerserial = new Timer(); 
		tt = new TimerTask() 
		{			
			@Override
			public void run() 
			{	    	
				sendData();
				List<Integer> b = readdata(inStream); 				
				if (b != null)
				{
					RPM = b.get(6) + b.get(5) * 0xff;
					IAT = b.get(7) - 40;					
					Load = (b.get(9) + b.get(8) * 0xff) * .00006103515625;				    				  
				    
				    if (Load > 0.7)
				    {
				    	FBKC = b.get(10)*.3515625 - 45;
					    FLKC = b.get(11)*.3515625 - 45;				    					    	
				    }
				    else
				    {
				    	FBKC = 0;
				    	FLKC = 0;
				    }
				    PreviousFLTO = FLTO;
				    FLTO = b.get(12)+1;	
				}															
				_myHandler.post(_myRunnable);							
			}			
		};			
	}

	
	public void onResume()
	{

	    Log.d(TAG, "...onResumeECU - try connect...");
	    
	    checkBTState();
	    
	    // Set up a pointer to the remote node using it's address.
	    BluetoothDevice device = btAdapter.getRemoteDevice(addressBTECU);
	    
	    // Two things are needed to make a connection:
	    //   A MAC address, which we got above.
	    //   A Service ID or UUID.  In this case we are using the
	    //     UUID for SPP.
	    
	    try 
	    {
	        btSocket = createBluetoothSocket(device);
	    } 
	    catch (IOException e1) 
	    {
	        errorExit("Fatal Error", "In onResume() and socket create failed: " + e1.getMessage() + ".");
	    }
	        
	    // Discovery is resource intensive.  Make sure it isn't going on
	    // when you attempt to connect and pass your message.
	    btAdapter.cancelDiscovery();
	    
	    // Establish the connection.  This will block until it connects.
	    Log.d(TAG, "...ConnectingECU...");
	    try 
	    {
	      btSocket.connect();
	      Log.d(TAG, "...ConnectionECU ok...");
	      
	      // Create a data stream so we can talk to server.
		    Log.d(TAG, "...Create SocketECU...");
		  
		    try 
		    {
		      outStream = btSocket.getOutputStream();
		      inStream = btSocket.getInputStream();
		      Log.d(TAG, "...lancement du timerECU...");
		      timerserial.scheduleAtFixedRate(tt, 150, 150);
		    } 
		    catch (IOException e) 
		    {
		      errorExit("Fatal Error", "In onResume() and output stream creation failed:" + e.getMessage() + ".");
		    }
		    
	      
	      
	    } 
	    catch (IOException e) 
	    {
	      try 
	      {
	        btSocket.close();
	        Log.d(TAG, "...ConnectionECU closed...");
	        errorExit("Subtunoid", "Unable to connectECU");
	      } 
	      catch (IOException e2) 
	      {	    	  
	        errorExit("Fatal Error ECU", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
	      }
	    }	      	   	    	 
	}
	
	public void onPause()
	{
		timerserial.purge();
	    timerserial.cancel();
	    Log.d(TAG, "...In onPauseECU()...");
	  
	    if (outStream != null) {
	      try {
	        outStream.flush();
	      } catch (IOException e) {
	        errorExit("Fatal Error", "In onPauseECU() and failed to flush output stream: " + e.getMessage() + ".");
	      }
	    }
	  
	    try     {
	      btSocket.close();
	    } catch (IOException e2) {
	      errorExit("Fatal Error", "In onPauseECU() and failed to close socket." + e2.getMessage() + ".");
	    }
	    
	}
	
	private void checkBTState() 
	{
		// Check for Bluetooth support and then check to make sure it is turned on
		// Emulator doesn't support Bluetooth and will return null
		if(btAdapter==null) 
		{		      			
			Toast.makeText(_context, "Fatal Error" + " - " + "Bluetooth not supported", Toast.LENGTH_LONG).show();
		    finish();
		} 
		else 
		{
			if (btAdapter.isEnabled()) 
			{
				Log.d(TAG, "...Bluetooth ON...");
			} 
			else 
			{
				//Prompt user to turn on Bluetooth
				Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				_context.startActivity(enableBtIntent);
				//startActivityForResult(enableBtIntent, 1);
			}
		}
	}
	
	private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException 
	  {
	      if(Build.VERSION.SDK_INT >= 10)
	      {
	          try 
	          {
	              final Method  m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", new Class[] { UUID.class });
	              return (BluetoothSocket) m.invoke(device, MY_UUID);
	          } catch (Exception e) 
	          {
	              Log.e(TAG, "Could not create Insecure RFComm Connection",e);
	          }
	      }
	      return  device.createRfcommSocketToServiceRecord(MY_UUID);
	  }
	
	private void errorExit(String title, String message)
	 {
		    Toast.makeText(_context, title + " - " + message, Toast.LENGTH_LONG).show();
		    finish();
	 }	
	
	public void sendData() 
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

		try 
		{
			while ((!stop ) && (is.available()>responseSize))
				
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
				Log.d(TAG, "...CRC OK...");
			}
			else
			{
				Log.d(TAG, "=>...Bad CRC...");
				buffer = null;
			}
		}
		catch (Exception e)
		{
			Log.d(TAG, "==>...Bad CRC...");
			buffer = null;
		}
							
		return buffer;		  
	}
	

	
	int calculcrc(List<Integer> data)
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
