package com.thecoffeedrinker.theforcereader;

import java.io.IOException;
import java.util.List;

import com.thecoffeedrinker.feedparser.Parser;
import com.thecoffeedrinker.theforcereader.newsmanager.FeedNews;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import android.support.v4.app.Fragment;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ShareActionProvider;
import android.widget.TextView;

/**
 * Fragment to show a news article previously selected; here using the menu it will be possible to share the news.
 * @author carlo
 *
 */
public class ArticleFragment extends Fragment{
	private final String SHARING_TYPE="text/plain";
	private WebView articleWV; //webview that will display the news article
	private ShareActionProvider shareAction;
	private final static String NEWS_INDEX = "Index of the selected news";//the reference is the list
	private final static String NEWS = "Selected News";
	
	public static ArticleFragment newInstance(int articleIndex){
		ArticleFragment fragment = new ArticleFragment();
		if(articleIndex!=-1){
			Bundle args = new Bundle();
			args.putInt(NEWS_INDEX, articleIndex);
			fragment.setArguments(args);
		}
		return fragment;
	}
	
	public static ArticleFragment newInstance(FeedNews news) {
		ArticleFragment fragment = new ArticleFragment();
		Bundle args = new Bundle();
		args.putSerializable(NEWS, news);
		fragment.setArguments(args);
		return fragment;
	}
	
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
     	return inflater.inflate(R.layout.article_empty, null);
    }
    
    
	public void onResume(){
		super.onResume();
		if(hasNews()){
			Bundle args = this.getArguments();
			if(args.containsKey(NEWS)){
				showArticle((FeedNews)args.getSerializable(NEWS));
			}else{
				showArticle(args.getInt(NEWS_INDEX));
			}
		}
	}
    
    
    public void onCreate(Bundle savedInstanceState){
    	super.onCreate(savedInstanceState);
    	setHasOptionsMenu(true);
    }
    
    
	public void onPause(){
		super.onPause();
		if(articleWV!=null){
			articleWV.loadUrl("about:blank");
		}
	}
    
	
   	/**
   	 * Display the news on the frontend
   	 * @param index of the news to show on the arrayList in the Application context that keeps
   	 * every news
   	 */
    public void showArticle(int index){
    	initArticleView();
    	NewsReaderContext context =  NewsReaderContext.getInstance(getActivity()); 
    	List<FeedNews> newsList = context.getNewsRetrieved();
    	FeedNews newsToShow = newsList.get(index); 
    	if(Parser.Util.isNetworkAvailable(getActivity())){
    		ArticleLoader HTMLloaderTask = new ArticleLoader();
    		HTMLloaderTask.execute(newsToShow);
    	}
    }
    
    private void initArticleView(){
    	if(getView().getTag()==null){
    		LayoutInflater inflater = getLayoutInflater(getArguments());
    		FrameLayout fragView = (FrameLayout) getView();
    		fragView.removeAllViews();
    		FrameLayout articleView = (FrameLayout) inflater.inflate(R.layout.article, null);
    		fragView.setTag(true);
    		fragView.addView(articleView);
    	}
    	articleWV=(WebView) getView().findViewById(R.id.articleView);
    	articleWV.setWebChromeClient(new WebChromeClient() {
    	});
		WebSettings settings = articleWV.getSettings();
		settings.setJavaScriptEnabled(true);
		settings.setPluginState(WebSettings.PluginState.ON);
    }
    
    public void showArticle(FeedNews news){
    	initArticleView();
    	if(Parser.Util.isNetworkAvailable(getActivity())){
    		ArticleLoader HTMLloaderTask = new ArticleLoader();
    		HTMLloaderTask.execute(news);
    	}
    }
    
    /**
     * Asynchtask to retrieve the html article from the web page, since this is an heavy operation; when this will
     * be done it will be loaded into the webview of the fragment
     * @author carlo
     *
     */
	private class ArticleLoader extends AsyncTask<FeedNews, Void, String>{
		private FeedNews newsToShow;
		private final static String JS_OBJECT_NAME="news";
		FrameLayout newsLayout;
		
		protected void onPreExecute(){
			articleWV.clearView();
			ProgressBar loadingView=new ProgressBar(getActivity(),null,android.R.attr.progressBarStyleLarge);
			LayoutParams progressParam=new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
			loadingView.setIndeterminate(true);
			progressParam.gravity = Gravity.CENTER;
			loadingView.setLayoutParams(progressParam);
			newsLayout = (FrameLayout) getView();
			newsLayout.addView(loadingView);
			
		}
		
		protected String doInBackground(FeedNews... arg0) {
			newsToShow=arg0[0];
			if(Parser.Util.isNetworkAvailable(ArticleFragment.this.getActivity())){
				try {
					String htmlNewsBody = newsToShow.getHTMLArticle();
					return htmlNewsBody;
				} catch (IOException e) {
					return null;
				}
			}else return null;
		}
		
		protected void onPostExecute(String htmlNewsBody){
			
			if(htmlNewsBody!=null){ 
				articleWV.addJavascriptInterface(htmlNewsBody, JS_OBJECT_NAME);
				//load the local page; inside that put the news content using an object
				articleWV.loadUrl("file:///android_asset/www/news_page.html");
				//This line is used to avoid trouble of overlay for tablets
				if(shareAction!=null){
					shareAction.setShareIntent(getSharingIntent(newsToShow.getTitle(), newsToShow.getAddress()));
				}
				newsLayout.removeViewAt(newsLayout.getChildCount()-1);
			}
		}
    	
    }

	
	/**
	 *  Get the proper intent to share the news
	 * @param title the news title
	 * @param address the address title
	 * @return the intent that will be use when the user will touch the share menu
	 */
	private Intent getSharingIntent(String title, String address){
		Intent sharingIntent = new Intent(Intent.ACTION_SEND);
		sharingIntent.setType(SHARING_TYPE);
		sharingIntent.putExtra(Intent.EXTRA_SUBJECT, title);
		sharingIntent.putExtra(Intent.EXTRA_TEXT, address);
		return sharingIntent;
	}
	
	
	/**
	 * Check if the fragment is empty or it will load a news
	 * @return
	 */
	private boolean hasNews(){
		Bundle args = this.getArguments();
		return args!= null && (args.containsKey(NEWS_INDEX) || args.containsKey(NEWS));
	}
	

	
	public void onCreateOptionsMenu(Menu menu,MenuInflater inflater) {
		inflater.inflate(R.menu.article_menu, menu);
		MenuItem item=menu.findItem(R.id.menu_share_article);
		shareAction=(ShareActionProvider)item.getActionProvider();
		
	}


	
	
	

}
