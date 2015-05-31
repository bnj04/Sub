package com.alpinfra.subtunoid.comm;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

public abstract class CustomBTComm extends Activity
{
	private static final String TAG = "Subtunoid-CustomBTComm";
	
	// SPP UUID service
	public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	
	public static String address;
	public int rate;
		
	public Context _context;
	public Handler _myHandler;	  		  		 
	public Runnable _myRunnable;
	
	// BT
	public BluetoothAdapter btAdapter = null;
	public BluetoothSocket btSocket = null;
	
	public OutputStream outStream = null;
	public InputStream inStream = null;	
	public Timer timerserial;
	public TimerTask tt;
	
	public KnockEvent ke;
	public SimpleDateFormat sdf;
	
		
	
	
	
	@SuppressLint("SimpleDateFormat")
	public CustomBTComm(String addr, int rte, Context context, Handler myHandler, Runnable myRunnable)
	{
		address = addr;
		rate = rte;
		
		ke = new KnockEvent();
		sdf = new SimpleDateFormat("yyyyMMdd;HH:mm:ss");
				
		_context = context;
		_myHandler = myHandler;
		_myRunnable = myRunnable;
				
		btAdapter = BluetoothAdapter.getDefaultAdapter();
				
		timerserial = new Timer(); 		
	}
	
	public void onResume()
	{
		Log.d(TAG, "...onResume - try connect...");
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
			Log.d(TAG, "...Connection ok, creating socket...");
			
			try 
			{
				//outStream = btSocket.getOutputStream();
				inStream = btSocket.getInputStream();
				outStream = btSocket.getOutputStream();
				Log.d(TAG, "...lancement du timer...");
				timerserial.scheduleAtFixedRate(tt, 100, rate);
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
				Log.d(TAG, "...Unable to connect...");
				//errorExit("Subtunoid", "Unable to connect");
			} 
			catch (IOException e2) 
			{	    	  
				errorExit("Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
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
	
	public void onPause()
	{
		timerserial.purge();
		timerserial.cancel();
		Log.d(TAG, "...In onPause()...");

		if (outStream != null) 
		{
			try 
			{
				outStream.flush();
			} 
			catch (IOException e) 
			{
				errorExit("Fatal Error", "In onPauseECU() and failed to flush output stream: " + e.getMessage() + ".");
			}
		}

		try     
		{
			btSocket.close();
		} 
		catch (IOException e2) 
		{
			errorExit("Fatal Error", "In onPauseECU() and failed to close socket." + e2.getMessage() + ".");
		}	    
	}
	
	public KnockEvent getlastknockevent()
	{		
		return ke;		
	}
	
		
	public void errorExit(String title, String message)
	{
		    Toast.makeText(_context, title + " - " + message, Toast.LENGTH_LONG).show();
		    Log.d(TAG,message);
	}	
	
}
