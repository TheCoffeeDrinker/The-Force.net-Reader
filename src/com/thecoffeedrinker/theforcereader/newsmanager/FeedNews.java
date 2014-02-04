package com.thecoffeedrinker.theforcereader.newsmanager;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import android.util.Log;

import com.thecoffeedrinker.feedparser.Item;
import com.thecoffeedrinker.theforcereader.NewsReaderContext;
import com.thecoffeedrinker.theforcereader.documentprocessor.HTMLDocumentExtractor;

public class FeedNews{
	private String mTitle;
	private String mDescription;
	private URL newsUrl;
	/**
	 * This can have 3 values:
	 * 1) null: not retrieved
	 * 2) empty: the article has no pictures
	 * 3) url of a picture
	 */
	private String mPictureUrl;
	private String HTMLcontent;

	public FeedNews(Item news){
		mTitle = news.get(NewsReaderContext.ELEMENTS_TO_READ_FROM_FEED[0]);
		mDescription = news.get(NewsReaderContext.ELEMENTS_TO_READ_FROM_FEED[1]);
		String newsAddress = news.get(NewsReaderContext.ELEMENTS_TO_READ_FROM_FEED[2]);
		try {
			newsUrl = new URL(newsAddress);
		} catch (MalformedURLException e) {
			Log.e(FeedNews.class.getCanonicalName(), e.getMessage(),e);
		}
	}
	
	@Override
	public boolean equals(Object news){
		if(news instanceof FeedNews){
			FeedNews feedNews = (FeedNews) news;
			return this.mTitle.equals(feedNews.getTitle()) &&
					this.mDescription.equals(feedNews.getDescription()) &&
					this.getAddress().equals(feedNews.getAddress());
		}
		return false;
	}

	
	public String getTitle(){return mTitle;}
	public String getDescription(){return mDescription;}
	
	public String getThumbnailUrl() throws IOException{
		if(mPictureUrl==null){
			HTMLDocumentExtractor extractor =new HTMLDocumentExtractor(newsUrl);
			mPictureUrl = extractor.getHeaderPicture();
		}		
		return mPictureUrl;
	}
	
	public String getAddress(){return newsUrl.toString();}

	public String getHTMLArticle() throws IOException{
		if(HTMLcontent==null){
			HTMLDocumentExtractor extractor = new HTMLDocumentExtractor(newsUrl);
			HTMLcontent = extractor.getArticle();
		}
		return HTMLcontent;
	}
    
    
	
}
