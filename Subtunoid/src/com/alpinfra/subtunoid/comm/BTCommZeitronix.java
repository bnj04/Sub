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

public class BTCommZeitronix extends Activity 
{
	private static final String TAG = "Subtunoid-BTCommZeitronix";
	// SPP UUID service
	private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	// MAC-address of Bluetooth module
	public static String address = "00:01:95:16:AB:94" ;	
	
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
	
	
	public double AFRv;
	public int EGTv;
	public double Boostv;


	public BTCommZeitronix(Context context, Handler myHandler, Runnable myRunnable)
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
				List<Byte> b = readdata(inStream); 
				if (b.size() > 10)
				{
					EGTv = asUnsignedInt(b.get(5)) * 256 + asUnsignedInt(b.get(4));
					AFRv = 1.0 * asUnsignedInt(b.get(3)) / 10.0;
					
					// MAP = (MAP(low) + MAP(high) * 256) / 10 Units inHg vacuum/PSI boost
					if ((b.get(9) & 128) == 128)
					{						
						//byte t = ;
						Boostv = 0.0 - (asUnsignedInt((byte) (b.get(9) & 0x7f)) * 256.0) - asUnsignedInt(b.get(8));	
						// conversion inhg en bar
						Boostv = Boostv * 0.00295299830714;						
						
					}
					else
					{
						Boostv = asUnsignedInt(b.get(9)) * 256.0 + asUnsignedInt(b.get(8));
						// conversion psi en bar
						Boostv = Boostv * 0.0068947573;
					}		        		        		       
					_myHandler.post(_myRunnable);
				}
				else
				{
					timerserial.purge();
					timerserial.cancel();
					
					try {
						btSocket.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}					
				}
			}			
		};			
	}

	
	public void onResume()
	{

	    Log.d(TAG, "...onResume - try connect...");
	    
	    checkBTState();
	    
	    // Set up a pointer to the remote node using it's address.
	    BluetoothDevice device = btAdapter.getRemoteDevice(address);
	    
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
	    Log.d(TAG, "...Connecting...");
	    try 
	    {
	      btSocket.connect();
	      Log.d(TAG, "...Connection ok...");
	      
	      // Create a data stream so we can talk to server.
		    Log.d(TAG, "...Create Socket...");
		  
		    try 
		    {
		      //outStream = btSocket.getOutputStream();
		      inStream = btSocket.getInputStream();
		      Log.d(TAG, "...lancement du timer...");
		      timerserial.scheduleAtFixedRate(tt, 100, 100);
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
	        Log.d(TAG, "...Connection closed...");
	        errorExit("Subtunoid", "Unable to connect");
	      } 
	      catch (IOException e2) 
	      {	    	  
	        errorExit("Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
	      }
	    }	      	   	    	 
	}
	
	public void onPause()
	{
		timerserial.purge();
	    timerserial.cancel();
	    Log.d(TAG, "...In onPause()...");
	  
	    if (outStream != null) {
	      try {
	        outStream.flush();
	      } catch (IOException e) {
	        errorExit("Fatal Error", "In onPause() and failed to flush output stream: " + e.getMessage() + ".");
	      }
	    }
	  
	    try     {
	      btSocket.close();
	    } catch (IOException e2) {
	      errorExit("Fatal Error", "In onPause() and failed to close socket." + e2.getMessage() + ".");
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
	
	private int asUnsignedInt(byte bytes) 
	{
        int i = 0;
        for (int j = 0; j < 1; j++)
        {
            if (j > 0) 
            {
                i <<= 8;
            }
            i |= bytes & 0xFF;
        }
        return i;
    }
	
	private List<Byte> readdata(InputStream is) 
	{
		boolean stop = false;
		boolean packetStarted = false;
		List<Byte> buffer = new ArrayList<Byte>(14);

		int a = 0;
		try 
		{
			a = is.available();
			if (a > 28)
			{
				is.skip(a-28);
			}
		} 
		catch (IOException e1) 
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}


		while (!stop) 
		{
			byte[] ba = new byte[1];
			try {
				is.read(ba);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				stop = true;
			}
			byte b = ba[0];
			if (b == 0x02
					&& buffer.size() >= 2
					&& buffer.get(buffer.size() - 1) == 0x01
					&& buffer.get(buffer.size() - 2) == 0x00) 
			{
				packetStarted = true;
				buffer.clear();
				buffer.add((byte) 0x00);
				buffer.add((byte) 0x01);
				buffer.add(b);

			} 
			else 
				if (packetStarted && buffer.size() <= 14) 
				{
					buffer.add(b);         
					switch (buffer.size()) {
					case 14:
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
		return buffer;		  
	}
	
}
