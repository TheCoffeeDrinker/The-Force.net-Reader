package com.thecoffeedrinker.theforcereader.newsmanager;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import android.util.Log;

import com.thecoffeedrinker.feedparser.Item;
import com.thecoffeedrinker.theforcereader.NewsReaderContext;
import com.thecoffeedrinker.theforcereader.documentprocessor.HTMLDocumentExtractor;

/**
 * This class represents a news posted into the website. The title, the description and the url are retrieved from
 * the feed RSS, instead the thumbnail and its content are retrieved from the news URL. 
 * @author carlo
 *
 */
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
	//clean news content
	private String HTMLcontent;

	/**
	 * The costructor takes an item from the feed (in this case a news) and gets from it the attributes
	 * @param news
	 */
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
	
	/**
	 * Get the thumbnail associated with the news
	 * @return the thumbnails extracted from URL of the news if it exists; if it doesn't exist return an empty object
	 * @throws IOException
	 */
	public String getThumbnailUrl() throws IOException{
		if(mPictureUrl==null){
			HTMLDocumentExtractor extractor =new HTMLDocumentExtractor(newsUrl);
			mPictureUrl = extractor.getHeaderPicture();
		}		
		return mPictureUrl;
	}
	
	public String getAddress(){return newsUrl.toString();}

	/**
	 * Get the clean HTML code of the article
	 * @return A string containing only the news article, in HTML
	 * @throws IOException
	 */
	public String getHTMLArticle() throws IOException{
		if(HTMLcontent==null){
			HTMLDocumentExtractor extractor = new HTMLDocumentExtractor(newsUrl);
			HTMLcontent = extractor.getArticle();
		}
		return HTMLcontent;
	}
    
    
	
}
