package com.thecoffeedrinker.theforcereader;

import java.util.List;

import com.thecoffeedrinker.feedparser.Parser;
import com.thecoffeedrinker.theforcereader.newsmanager.FeedNews;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class ArticleActivity extends FragmentActivity {
	//Attività di mezzo eseguita solo ed esclusivamente su mobile.
	public static final String NEWS_TO_UPDATE_EXTRA_KEY="News to update on the list";
	private static final int NO_NEWS_INDEX = -1;
	
	
	public void onCreate (Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		if(Parser.Util.isNetworkAvailable(getApplicationContext())){
			initArticleActivity();
		}else{
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
		super.setContentView(R.layout.article);
		ViewPager scrollPager = (ViewPager) findViewById(R.id.pager);
		PagerAdapter articleAdapter = new PagerAdapter(getSupportFragmentManager());
		scrollPager.setAdapter(articleAdapter);
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
			return newsList.size();
		}
		
	}
	
}
