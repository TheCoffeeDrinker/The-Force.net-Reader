package com.thecoffeedrinker.theforcereader.newsmanager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;

import com.thecoffeedrinker.theforcereader.ArticleActivity;
import com.thecoffeedrinker.theforcereader.NewsListActivity;
import com.thecoffeedrinker.theforcereader.NewsReaderContext;
import com.thecoffeedrinker.theforcereader.R;
import com.thecoffeedrinker.theforcereader.settings.SettingsActivity;
import com.thecoffeedrinker.feedparser.FeedParser;
import com.thecoffeedrinker.feedparser.Item;
import com.thecoffeedrinker.feedparser.Parser;

import android.app.Activity;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.support.v4.content.LocalBroadcastManager;

/**
 * Service to retrieve the news. It will be periodically run and send notification of new news if the user selected this option;
 * if everything goes well it will send the list of the retrieved news by a broadcast.
 * @author carlo
 *
 */
public class LatestNewsRetrService extends IntentService {
    private final static String SERVICE_NAME="List News Retriever";
    public final static String EXTRA_NEWS_LIST="News List retreived";
    public static final String EXTRA_SERVICE_RESULT = "Service result";
	public static final String EXTRA_PROBLEM_OCCURRED = "Problem occurred";
    private static String lastTitleNotified;
    public final static int PROBLEM_NONE=0;
    public final static int PROBLEM_IO=1;
    public final static int PROBLEM_XMLPARSER=2;
    public final static int PROBLEM_NO_CONNECTION=3;

    public LatestNewsRetrService() {
		super(SERVICE_NAME);
	}
	
	
	protected void onHandleIntent(Intent intent) {
		NewsReaderContext context=NewsReaderContext.getInstance(getApplication());
		List<Item> lastItemsFetched=null;
		ArrayList<FeedNews> latestNewsFetched = null;
		int retrProblem = PROBLEM_NONE;
		if(Parser.Util.isNetworkAvailable(context)){
			//we only process a part of the feed, because the user would not be intereted in really old news and to make the
			//application faster
			FeedParser newsParser = new FeedParser(NewsReaderContext.FEED_URL, NewsReaderContext.MAX_LENGTH_FEED);
			try {
				lastItemsFetched=newsParser.parse(NewsReaderContext.ELEMENTS_TO_READ_FROM_FEED);
			} catch (XmlPullParserException e) {
				retrProblem = PROBLEM_XMLPARSER;
			} catch (IOException e) {
				retrProblem = PROBLEM_IO;
			}
		}else{
			retrProblem = PROBLEM_NO_CONNECTION;
		}
		int result=0;
		if(lastItemsFetched!=null){
			result=Activity.RESULT_OK;
			FeedNews lastNews = new FeedNews(lastItemsFetched.get(0));
			//take the top news to check if new news have been published (and therefore update the UI)
			if(context.getNewsRetrieved()==null || !context.getNewsRetrieved().get(0).equals(lastNews)){
				latestNewsFetched = new ArrayList<FeedNews>();
				//now convert the feed item into FeedNews
				for(Item newsItem: lastItemsFetched){
					latestNewsFetched.add(new FeedNews(newsItem));
					if(latestNewsFetched.size()==1){
						//use the first news title to send a notification to the user (according to his preferences)
						checkNotification(lastItemsFetched.get(0));
					}
				}
			}else{
				latestNewsFetched = new ArrayList<FeedNews>();
				//empty list: nothing new. An empty list will be send to notify that the situation didn't change
			}
		}else{
			result=Activity.RESULT_CANCELED;
		}
		LocalBroadcastManager broadcastManager=LocalBroadcastManager.getInstance(this);
		Intent broadcastIntent=new Intent(NewsReaderContext.BROADCAST_INTENT_ACTION);
		broadcastIntent.putExtra(EXTRA_SERVICE_RESULT, result);
		broadcastIntent.putExtra(EXTRA_NEWS_LIST, latestNewsFetched);
		if(retrProblem!=PROBLEM_NONE){
			broadcastIntent.putExtra(EXTRA_PROBLEM_OCCURRED, retrProblem);
		}
		broadcastManager.sendBroadcast(broadcastIntent);
	}
	
	/**
	 * send a notification ( it is the case ) to send a notification about the lastes news to the user
	 * @param topNews the news that should be notified
	 */
	private void checkNotification(Item topItemNews){
		FeedNews topNews = new FeedNews(topItemNews);
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		boolean defaultNotificationSetting = getResources().getBoolean(R.bool.default_setting_notification);
		boolean notificationEnabled = settings.getBoolean(SettingsActivity.KEY_SETTING_NOTIFICATION_ENABLED, 
				defaultNotificationSetting);
		if(notificationEnabled){
			if(lastTitleNotified==null || lastTitleNotified.isEmpty()){
				lastTitleNotified=topNews.getTitle();
			}else{
				//if the titles are different, send the notification
				if(!topNews.getTitle().equals(lastTitleNotified) ){
					notifyNews(topNews);
					lastTitleNotified = topNews.getTitle();
				}
			}
		}
	}
	
	/**
	 * Send the notification to the screen of the user
	 * @param title The notification title
	 */
	private void notifyNews(FeedNews news) {
		NotificationManager notManager= (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		Builder notificationBuilder=new NotificationCompat.Builder(this);
		Intent showArticleIntent=new Intent(this,ArticleActivity.class);
		showArticleIntent.putExtra(NewsListActivity.NEWS_TO_SHOW_EXTRA_KEY, news);
		PendingIntent showArtPendInt=PendingIntent.getActivity(this, 0, showArticleIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		notificationBuilder.setAutoCancel(true)
			.setContentIntent(showArtPendInt)
			.setContentTitle(getString(R.string.app_name))
			.setContentText(news.getTitle())
			.setWhen(System.currentTimeMillis())
			.setSmallIcon(R.drawable.ic_stat_notification)
			.setDefaults(Notification.DEFAULT_SOUND);
		notManager.notify(0, notificationBuilder.build());
	}
	
	
	
}
