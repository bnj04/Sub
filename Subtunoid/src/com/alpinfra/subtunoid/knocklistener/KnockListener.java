package com.alpinfra.subtunoid.knocklistener;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.util.Log;

public class KnockListener 
{
	String LOG_TAG = "KnockFilter"; 
	AudioRecord m_record;
	AudioTrack m_track;
	int SAMPLE_RATE = 44100;
	int ChannelConfiguration = AudioFormat.CHANNEL_IN_MONO;
	int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
	Thread m_thread;
	byte[] buffer;
	int buffersize;
	boolean m_isRun = false;
	
	public KnockListener()
	{
		// Prepare the AudioRecord & AudioTrack
		//try 
		{
			buffersize = AudioRecord.getMinBufferSize(SAMPLE_RATE,ChannelConfiguration,audioEncoding);
			Log.i(LOG_TAG,"min buffer size : "+buffersize);

			buffer = new byte[buffersize];
			Log.i(LOG_TAG,"Initializing Audio Record and Audio Playing objects");

			
			m_record = new AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION, SAMPLE_RATE, ChannelConfiguration, audioEncoding, buffersize);
			m_track = new AudioTrack(AudioManager.STREAM_MUSIC, SAMPLE_RATE, AudioFormat.CHANNEL_OUT_DEFAULT, audioEncoding, buffersize, AudioTrack.MODE_STREAM);
			m_track.setPlaybackRate(SAMPLE_RATE);
		} 
		//catch (Throwable t) 
		{
		//	Log.e("Error", "Initializing Audio Record and Play objects Failed "+t.getLocalizedMessage());
		}
	}
	
	public void loopback() 
	{				
		m_record.startRecording();		
		m_track.play();							
		while (m_isRun) 
		{
			m_record.read(buffer, 0, buffersize);								
			m_track.write(buffer, 0, buffer.length);
		}		
		m_record.stop();		
		m_track.stop();
	}
	
	
	public Boolean toogle() 
	{
		if (m_isRun)
		{
			m_isRun = false;						
		}
		else
		{
			Log.i(LOG_TAG,"do_loopback");
			m_isRun=true;			
			m_thread = new Thread(new Runnable() 
			{
				public void run() 
				{
					loopback();
				}
			});
			m_thread.start();			
		}
		return m_isRun;
	}
		
	
}
