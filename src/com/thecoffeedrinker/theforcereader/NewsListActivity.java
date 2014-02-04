package com.thecoffeedrinker.theforcereader;

import java.util.List;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.thecoffeedrinker.feedparser.Parser;
import com.thecoffeedrinker.theforcereader.R;
import com.thecoffeedrinker.theforcereader.newsmanager.LatestNewsRetrService;
import com.thecoffeedrinker.theforcereader.newsmanager.FeedNews;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;


public class NewsListActivity extends FragmentActivity implements NewsListFragment.OnNewsSelectedListener{
	public static final String NEWS_TO_SHOW_EXTRA_KEY="News selected by the User";
	private boolean dualScreen;
	private List<FeedNews> newsRetrieved;
	private NewsReaderContext context;
	private NewsHandler newsReceiver;
	private NewsListFragment listFragment;
    private final static int ARTICLE_ACTIVITY_REQ_CODE=0;
    private int indexNewsSelected;//used to update the news
    private final static int NO_NEWS_SELECTED_INDEX = -1;
    private final static String SELECTED_NEWS_INDEX_ON_SAVE_KEY="Index of the news Selected";
	private PullToRefreshListView mPullListView;
    
    
    
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context=NewsReaderContext.getInstance(getApplicationContext());
        if(Parser.Util.isNetworkAvailable(context)){
        	initActivity(savedInstanceState);
        }else{
        	super.setContentView(R.layout.no_connection);
        	Button connectButton = (Button) findViewById(R.id.connect_button);
        	connectButton.setOnClickListener(new View.OnClickListener() {
				
				public void onClick(View v) {
					if(Parser.Util.isNetworkAvailable(context)){
			        	initActivity(savedInstanceState);
						refresh();
			        }
				}
			});
        }
        mPullListView = (PullToRefreshListView) listFragment.getPullToRefreshListView();
		mPullListView.setOnRefreshListener(new OnRefreshListener<ListView>() {

			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				refresh();
			}
		});
        
    	
    }
    
    public List<FeedNews> getNewsRetrieved(){
    	return newsRetrieved;
    }
    
    private void initActivity(Bundle savedInstanceState){
    	super.setContentView(R.layout.main);
    	newsRetrieved= context.getNewsRetrieved();
    	Fragment articleFragShown=getSupportFragmentManager().findFragmentById(R.id.article_fragment);
    	dualScreen= (!(articleFragShown==null || !articleFragShown.isInLayout()));
        newsReceiver=new NewsHandler();
    	Messenger newsMessenger=new Messenger(newsReceiver);
    	context.setActivityMessenger(newsMessenger);
    	listFragment=(NewsListFragment) getSupportFragmentManager().findFragmentById(R.id.news_list_fragment);
		startService(new Intent(this, LatestNewsRetrService.class));
		if(Parser.Util.isNetworkAvailable(this)) {
    		listFragment.loadList(newsRetrieved); 
    	}
    	if(savedInstanceState!=null){
    		if(savedInstanceState.containsKey(SELECTED_NEWS_INDEX_ON_SAVE_KEY)){
    			indexNewsSelected=savedInstanceState.getInt(SELECTED_NEWS_INDEX_ON_SAVE_KEY);
    			if(dualScreen){
    				onNewsSelected(indexNewsSelected);
    			}
    		}
    	}else{
    		indexNewsSelected = NO_NEWS_SELECTED_INDEX;
    	}
    }
    
    public boolean isSplitScreen(){
    	return dualScreen;
    }
    
    
    protected void onSaveInstanceState (Bundle outState){
    	super.onSaveInstanceState(outState);
    	if(indexNewsSelected!=NO_NEWS_SELECTED_INDEX){
    		outState.putInt(SELECTED_NEWS_INDEX_ON_SAVE_KEY, indexNewsSelected);
    	}
    }
	

    
    @Override
    protected void onResume(){
    	super.onResume();
    	refresh();
    	fillList();
    }
    
	protected void refresh(){
		startService(new Intent(this, LatestNewsRetrService.class));
		
	}
	

    
	
	
	class NewsHandler extends Handler{	
    	
    	public void handleMessage(Message message) {
			if(message.what==RESULT_OK){
				fillList();
			}else{ 
				FragmentTransaction reloadTransaction=NewsListActivity.this.getSupportFragmentManager().beginTransaction();
				reloadTransaction.detach(listFragment);
				reloadTransaction.attach(listFragment);
				reloadTransaction.commit();
			}
		}
    	
    }

	
	private void fillList(){
		if(mPullListView!=null) mPullListView.onRefreshComplete();
		newsRetrieved=context.getNewsRetrieved();
		listFragment.loadList(newsRetrieved);
	}
	
	public void onNewsSelected(int newsIndex) {
		indexNewsSelected=newsIndex;
		if(dualScreen){
			ArticleFragment articleContainerFrag = ArticleFragment.newInstance(newsIndex);
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		    ft.replace(R.id.article_fragment, articleContainerFrag);
		    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		    ft.addToBackStack(null);
		    ft.commit();
		}else{
			Intent readArticle=new Intent(this, ArticleActivity.class);
			readArticle.putExtra(NEWS_TO_SHOW_EXTRA_KEY, newsIndex);
			startActivityForResult(readArticle,ARTICLE_ACTIVITY_REQ_CODE);
		}
	}

	
}
    
    
