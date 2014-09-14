package com.thecoffeedrinker.theforcereader;

import java.util.List;

import com.thecoffeedrinker.feedparser.Parser;
import com.thecoffeedrinker.theforcereader.newsmanager.FeedNews;
import com.thecoffeedrinker.theforcereader.newsmanager.LatestNewsRetrService;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

/**
 * Activity executed on the mobile or when the application is in portait mode for the tablets, to show a news content
 * @author carlo
 *
 */
public class ArticleActivity extends FragmentActivity {
	public static final String NEWS_TO_UPDATE_EXTRA_KEY="News to update on the list";
	private static final int NO_NEWS_INDEX = -1;
	
	
	public void onCreate (Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		if(Parser.Util.isNetworkAvailable(getApplicationContext())){
			initArticleActivity();
		}else{
			//if there is no connection load the views to reconnect
			super.setContentView(R.layout.no_connection);
        	Button connectButton = (Button) findViewById(R.id.connect_button);
        	connectButton.setOnClickListener(new View.OnClickListener() {
				
				public void onClick(View v) {
					if(Parser.Util.isNetworkAvailable(getApplicationContext())){
			        	initArticleActivity();
			        }
				}
			});
		}
	}
	
	private void initArticleActivity(){
		if(NewsReaderContext.getInstance(this).getNewsRetrieved()==null){
			//if the activity is resumed and there is not the list of the news retrieve it back
			startService(new Intent(this, LatestNewsRetrService.class));
		}
		super.setContentView(R.layout.article_act);
		ViewPager scrollPager = (ViewPager) findViewById(R.id.pager);
		PagerAdapter articleAdapter = new PagerAdapter(getSupportFragmentManager());
		scrollPager.setAdapter(articleAdapter);
		//retrieve the index of the selected news from the intent than launch this activity, to handle the adapter
		int newsSelected=getIntent().getIntExtra(NewsListActivity.NEWS_TO_SHOW_EXTRA_KEY,NO_NEWS_INDEX);
		if(newsSelected != NO_NEWS_INDEX){
			scrollPager.setCurrentItem(newsSelected);
		}
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	public boolean onOptionsItemSelected(MenuItem menuItem){       
	    onBackPressed();
	    return true;
	}
	
	/**
	 * Page Adapter that will allows to swipe from one news to another within this activity
	 * @author carlo
	 *
	 */
	private class PagerAdapter extends FragmentStatePagerAdapter{
		private List<FeedNews> newsList;
		private NewsReaderContext context;
		
		public PagerAdapter(FragmentManager fm) {
			super(fm);
			context = NewsReaderContext.getInstance(getApplicationContext());
			newsList = context.getNewsRetrieved();
		}

		@Override
		public Fragment getItem(int index) {
			ArticleFragment articleFrag = ArticleFragment.newInstance(index);
			return articleFrag;
		}

		@Override
		public int getCount() {
			if(newsList==null){
				//if no other news are available avoid swiping.
				return 1;
			}else{
				if(newsList.get(0).equals(newsList.get(1))){
					return 1;
				}
				return newsList.size();
			}
		}
		
	}
	
}
