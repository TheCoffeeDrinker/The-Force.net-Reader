package com.thecoffeedrinker.feedparser;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;
import android.util.Xml;

public class FeedParser extends Parser{
	private final static String ITEM="item";
	protected List<Item> itemList;
	private int maxItems;
	
	public FeedParser(String url){
		super(url);
		itemList = new ArrayList<Item>();
		
	}
	
	public FeedParser(String url, int length){
		this(url);
		this.maxItems = length;
	}

	
	public List<Item> parse(String[] tag) throws XmlPullParserException, IOException{
		ParserTask task = new ParserTask(tag);
		Thread parseThread = new Thread(task);
		parseThread.start();
		synchronized (itemList) {
			try {
				itemList.wait();
			} catch (InterruptedException e) {
				Log.e(FeedParser.class.getCanonicalName(), e.getMessage(), e);
			}
		}
		return itemList;
	}
	
	private class ParserTask implements Runnable{
		private String[] tagToRead;
		
		public ParserTask(String[] tags) {
			tagToRead = tags;
		}
		
		public void run() {
			try {
				URL feedURL = new URL(getFeedAddress());
				InputStream is = feedURL.openStream();
				XmlPullParser parser = Xml.newPullParser();
				parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
				parser.setInput(is,null);
				parser.nextTag();
				readFeed(parser,tagToRead);
				is.close();
			} catch (IOException e) {
				Log.e(FeedParser.class.getCanonicalName(), e.getMessage(), e);
			} catch (XmlPullParserException e) {
				Log.e(FeedParser.class.getCanonicalName(), e.getMessage(), e);
			}
		}
		
	
		private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
		    if (parser.getEventType() != XmlPullParser.START_TAG) {
		        throw new IllegalStateException();
		    }
		    int depth = 1;
		    while (depth != 0) {
		        switch (parser.next()) {
		        case XmlPullParser.END_TAG:
		            depth--;
		            break;
		        case XmlPullParser.START_TAG:
		            depth++;
		            break;
		        }
		    }
		 }
		
		private void readFeed(XmlPullParser parser, String[] tag) throws XmlPullParserException, IOException {
			/**
			 * 1) Salta tutta l'intestazione finchè non arrivi al primo tag item
			 * 2) A questo punto, finchè non è stato trovato il tag item di chiusura analizza tutti i tag per 
			 * mettere via via in un oggetto item;
			 * 3) Continua a ciclare finchè non si arriva alla chiudura del tag channel.
			 */
			parser.require(XmlPullParser.START_TAG, null, "rss");
			parser.nextTag();
			parser.require(XmlPullParser.START_TAG, null, "channel");
			parser.nextTag();
			//Get to the first item tag
			while(!parser.getName().equalsIgnoreCase(ITEM)){
				skip(parser);
				parser.nextTag();
			}
			//Now that we header is over we can start to save every item
			List<String> tagToRead =  Arrays.asList(tag);
			Item feedItem = new Item(); 
			while(parser.next()!=XmlPullParser.END_DOCUMENT){//finchè non termina il documento
				if(maxItems!=0 && itemList.size()==maxItems){
					break;
				}
				if(parser.getEventType() != XmlPullParser.START_TAG){
					continue;
				}
				String currentTag = parser.getName();
				if(currentTag.equals(ITEM)){
					//we are now on a new item; add the previous one to the list.
					itemList.add(feedItem);
					feedItem = new Item();
				}else{
					if(tagToRead.contains(currentTag)){
						String elemContent = getSelectedElemContent(parser);
						feedItem.add(currentTag, elemContent);
					}else{
						skip(parser);
					}
				}
						
			}
			synchronized (itemList){
				itemList.notify();
			}
		}
		
		private String getSelectedElemContent(XmlPullParser parser) throws IOException, XmlPullParserException {
		    String result = "";
		    if (parser.next() == XmlPullParser.TEXT) {
		        result = parser.getText();
		        parser.nextTag();
		    }
		    return result;
		}

	}


}
