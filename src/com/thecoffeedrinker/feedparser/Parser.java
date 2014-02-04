package com.thecoffeedrinker.feedparser;

import java.io.IOException;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public abstract class Parser {
	private String feedAddress;
	
	public Parser(String url){
		feedAddress = url;
	}
	
	public abstract List<Item> parse(String[] tag) throws XmlPullParserException, IOException;
	
	public String getFeedAddress(){
		return feedAddress;
	}
	
	public static class Util{
		public static boolean isNetworkAvailable(Context ctx){
			ConnectivityManager connectivityManager 
	        	= (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
			return activeNetworkInfo != null && activeNetworkInfo.isConnected();
		}
	}
}
