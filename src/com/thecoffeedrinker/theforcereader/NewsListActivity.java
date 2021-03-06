package com.thecoffeedrinker.theforcereader;

import java.util.List;

import com.thecoffeedrinker.feedparser.Parser;
import com.thecoffeedrinker.theforcereader.R;
import com.thecoffeedrinker.theforcereader.newsmanager.LatestNewsRetrService;
import com.thecoffeedrinker.theforcereader.newsmanager.FeedNews;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Main activity to show the list of news and, if the user is using a tablet on landscape, the news article selected
 * will be shown on the left of the screen
 * @author carlo
 *
 */
public class NewsListActivity extends FragmentActivity implements NewsListFragment.OnNewsSelectedListener, NewsListFragment.ViewCreated{
	public static final String NEWS_INDEX_TO_SHOW_EXTRA_KEY="Index of the news selected by the User";
	public static final String NEWS_TO_SHOW_EXTRA_KEY = "News selected by the User";
	private boolean dualScreen;//if the activity is displaying both the news list and the news
	private List<FeedNews> newsRetrieved;
	private NewsReaderContext context;
	private NewsHandler newsReceiver;
	private NewsListFragment listFragment;
    private final static int ARTICLE_ACTIVITY_REQ_CODE=0;
    private int indexNewsSelected = NO_NEWS_SELECTED_INDEX;//used to update the news
    private final static int NO_NEWS_SELECTED_INDEX = -1;
    private final static String SELECTED_NEWS_INDEX_ON_SAVE_KEY="Index of the news Selected";
    private SwipeRefreshLayout swipeLayout;
    
    private View.OnClickListener messageButtonListener =  new View.OnClickListener() {
		
		public void onClick(View v) {
			if(Parser.Util.isNetworkAvailable(context)){
	        	initActivity(null);
				refresh();
	        }
		}
	};
    
    
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context=NewsReaderContext.getInstance(getApplicationContext());
        if(Parser.Util.isNetworkAvailable(context)){
        	initActivity(savedInstanceState);
        }else{
        	showMessageLayout(R.string.unconnected_message);
        }
    }
    
    
    
    private void showMessageLayout(int messageId) {
    	super.setContentView(R.layout.message);
    	TextView errorMessageTv = (TextView) findViewById(R.id.error_message);
    	errorMessageTv.setText(messageId);
    	Button connectButton = (Button) findViewById(R.id.connect_button);
    	connectButton.setOnClickListener(messageButtonListener);
		
	}



	public List<FeedNews> getNewsRetrieved(){
    	return newsRetrieved;
    }
    
    private void initActivity(Bundle savedInstanceState){
    	super.setContentView(R.layout.main);
    	newsRetrieved= context.getNewsRetrieved();
    	View articleView = findViewById(R.id.article_fragment);
    	dualScreen= (!(articleView==null));
        newsReceiver=new NewsHandler();
    	Messenger newsMessenger=new Messenger(newsReceiver);
    	context.setActivityMessenger(newsMessenger);
    	inflateListFragment();
    	
    	if(savedInstanceState!=null){
    		indexNewsSelected=savedInstanceState.getInt(SELECTED_NEWS_INDEX_ON_SAVE_KEY);
    	}
		if(Parser.Util.isNetworkAvailable(this)) {
    		listFragment.loadList(newsRetrieved,indexNewsSelected); 
    	}
    	if(savedInstanceState!=null && dualScreen){
    		onNewsSelected(indexNewsSelected);
    	}
    	
    	swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			
			@Override
			public void onRefresh() {
				refresh();
			}
		});
        swipeLayout.setColorScheme(R.color.yellow, 
                android.R.color.holo_orange_light, 
                android.R.color.holo_orange_dark, 
                android.R.color.holo_red_light);
        listFragment.setOnViewCreated(this);
    }
    
    private void inflateListFragment() {
    	listFragment = new NewsListFragment();
    	FragmentManager  fm = getSupportFragmentManager();
    	FragmentTransaction ft = fm.beginTransaction();
    	ft.replace(R.id.news_list_fragment, listFragment);
    	ft.commit(); 
	}



	public boolean isSplitScreen(){
    	return dualScreen;
    }
    
    
    protected void onSaveInstanceState (Bundle outState){
    	super.onSaveInstanceState(outState);
        outState.putInt(SELECTED_NEWS_INDEX_ON_SAVE_KEY, indexNewsSelected);
    }
	

    
    @Override
    protected void onResume(){
    	super.onResume();
    	if(context.getNewsRetrieved()==null){
    		refresh();
    	}else{
    		if(listFragment!=null && listFragment.isListEmpty()){
    			fillList();
    		}
    	}
    }
    
	protected void refresh(){
		startService(new Intent(this, LatestNewsRetrService.class));
	}
	

	
	class NewsHandler extends Handler{	
    	
    	public void handleMessage(Message message) {
    		swipeLayout.setRefreshing(false);
			if(message.what==RESULT_OK){
				fillList();
			}else{
				switch(message.arg1){
					case LatestNewsRetrService.PROBLEM_IO:
						showMessageLayout(R.string.file_not_found_message);
						break;
					
					case LatestNewsRetrService.PROBLEM_XMLPARSER:
						showMessageLayout(R.string.invalid_xml_message);
						break;
					
					case LatestNewsRetrService.PROBLEM_NO_CONNECTION:
						showMessageLayout(R.string.unconnected_message);
						break;
				}
			}
		}
    	
    }

	
	private void fillList(){
		newsRetrieved=context.getNewsRetrieved();
		if(listFragment!=null)listFragment.loadList(newsRetrieved,indexNewsSelected);
	}
	
	public void onNewsSelected(int newsIndex) {
		indexNewsSelected=newsIndex;
		if(dualScreen){
			if(newsIndex!=NO_NEWS_SELECTED_INDEX){
				ArticleFragment articleFrag = (ArticleFragment) this.getSupportFragmentManager().findFragmentById(R.id.article_fragment);
				articleFrag.showArticle(newsIndex);
			}
		}else{
			//start the activity to read the article if there is not the fragment into the layout
			Intent readArticle=new Intent(this, ArticleActivity.class);
			readArticle.putExtra(NEWS_INDEX_TO_SHOW_EXTRA_KEY, newsIndex);
			startActivityForResult(readArticle,ARTICLE_ACTIVITY_REQ_CODE);
		}
	}



	@Override
	public void onViewCreatedListener() {
		listFragment.getListView().setOnScrollListener(new AbsListView.OnScrollListener() {
			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				ListView guidesList = listFragment.getListView();
				int topRowVerticalPosition = (guidesList == null || guidesList.getChildCount() == 0) ? 
						        0 : guidesList.getChildAt(0).getTop();
				swipeLayout.setEnabled(firstVisibleItem == 0 && topRowVerticalPosition >= 0);
			}
		});
	}

	
}
    
    
