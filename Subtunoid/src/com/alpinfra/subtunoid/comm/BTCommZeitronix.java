package com.alpinfra.subtunoid.comm;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

public class BTCommZeitronix extends CustomBTComm
{
	private static final String TAG = "Subtunoid-BTCommZeitronix";
	// MAC-address of Bluetooth module
	public static String addressBTZeitronix = "00:01:95:16:AB:94" ;	
	public static int rate = 100; 
		
	public double AFRv;
	public int EGTv;
	public double Boostv;

	public BTCommZeitronix(Context context, Handler myHandler, Runnable myRunnable)
	{
		super(addressBTZeitronix, rate, context, myHandler, myRunnable);
		Log.d(TAG, "...onCreation...");
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
				}
				_myHandler.post(_myRunnable);				
			}			
		};			
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
