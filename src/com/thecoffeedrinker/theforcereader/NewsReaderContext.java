package com.thecoffeedrinker.theforcereader;

import java.util.ArrayList;
import java.util.List;

import com.thecoffeedrinker.theforcereader.newsmanager.LatestNewsRetrService;
import com.thecoffeedrinker.theforcereader.newsmanager.FeedNews;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.LruCache;
import android.util.Log;

/**
 * Context for the application; this will keep public contants, settings, and the broadcast receiver for the service
 * @author carlo
 *
 */
public class NewsReaderContext extends ContextWrapper{
	private static NewsReaderContext readerContext; 
	private List<FeedNews> newsList;
	public final static String FEED_URL="http://www.theforce.net/rss/theforcenet.rss";
    private final static long REFRESH_INTERVAL=900000;
    public final static String[] ELEMENTS_TO_READ_FROM_FEED={"title","description","link"};
	private static ServiceResultReceiver resultReceiver;
	public final static String BROADCAST_INTENT_ACTION="com.thecoffeedrinker.theforcereader.NEWS_BROADCAST";
	public final static int MAX_LENGTH_FEED=16;
	private Messenger readerMessenger;
	
    
	private NewsReaderContext(Context context){
		super(context);
		resultReceiver=new ServiceResultReceiver();
		LocalBroadcastManager locBroadcastManager=LocalBroadcastManager.getInstance(this);
		locBroadcastManager.registerReceiver(resultReceiver, new IntentFilter(BROADCAST_INTENT_ACTION));
		startRetrievalService(this);
		
	}
	
	public static NewsReaderContext getInstance(Context appContext){
		if(readerContext == null){
			readerContext = new NewsReaderContext(appContext);
		}
		return readerContext;
	}
	

	public List<FeedNews> getNewsRetrieved(){
		return newsList;
	}
	
	public void setActivityMessenger(Messenger actMessenger){
		readerMessenger=actMessenger;
	}

	
	/**
	 * Class that will receive the service result (a list of FeedNews)
	 * @author carlo
	 *
	 */
	public class ServiceResultReceiver extends BroadcastReceiver{	
    	
		public void onReceive(Context context, Intent intent) {
    		int result=intent.getIntExtra(LatestNewsRetrService.EXTRA_SERVICE_RESULT, Activity.RESULT_OK);
    		Message resultToSendToActivity=Message.obtain();
			
    		if(result==Activity.RESULT_OK){
				Bundle serviceBundle=intent.getExtras();
				ArrayList<FeedNews> retrievedNews = (ArrayList<FeedNews>) serviceBundle.getSerializable(LatestNewsRetrService.EXTRA_NEWS_LIST);
				if(!retrievedNews.isEmpty()){
					newsList = retrievedNews;
				}
			}else{
				int problemOccurred = intent.getIntExtra(LatestNewsRetrService.EXTRA_PROBLEM_OCCURRED, LatestNewsRetrService.PROBLEM_NONE);
				resultToSendToActivity.arg1 = problemOccurred;
			}
    		resultToSendToActivity.what=result;
			if(readerMessenger!=null){
				try {
					readerMessenger.send(resultToSendToActivity);
				} catch (RemoteException e) {
					Log.e(getString(R.string.app_name),e.getMessage());
				}
			}
		}
    	
    };
    
    /**
     * Start the periodical news retrieval service
     * @param ctx application context
     */
	protected static void startRetrievalService(Context ctx) {
		AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
		Intent serviceIntent=new Intent(ctx, LatestNewsRetrService.class);
		PendingIntent pendingService = PendingIntent.getService(ctx, 0, serviceIntent, 
				PendingIntent.FLAG_CANCEL_CURRENT);
		alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), REFRESH_INTERVAL, pendingService);
	}
	
	
}
