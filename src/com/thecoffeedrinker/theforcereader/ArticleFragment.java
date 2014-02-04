package com.thecoffeedrinker.theforcereader;

import java.io.IOException;
import java.util.List;

import com.thecoffeedrinker.feedparser.Parser;
import com.thecoffeedrinker.theforcereader.newsmanager.FeedNews;
//import com.thecoffeedrinker.theforcereader.widget.DisconnectedViewFactory;

import android.app.Activity;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.ShareActionProvider;

public class ArticleFragment extends Fragment{
	private final String SHARING_TYPE="text/plain";
	private WebView articleWV;
	private RelativeLayout mainLayout;
	private boolean isLayoutToClean;
	private ShareActionProvider shareAction;
	private final static String NEWS_INDEX = "Index of the selected news";//the reference is the list
	private ArticleLoader HTMLloaderTask;
	
	public static ArticleFragment newInstance(int articleIndex){
		ArticleFragment fragment = new ArticleFragment();
		Bundle args = new Bundle();
		args.putInt(NEWS_INDEX, articleIndex);
		fragment.setArguments(args);
		return fragment;
	}
	
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	Activity activity=getActivity();
     	mainLayout = new RelativeLayout(activity);
     	if(getActivity() instanceof NewsListActivity){
	     	ImageView rebelLogo=new ImageView(activity);
	     	rebelLogo.setImageResource(R.drawable.fill_logo);
	     	LinearLayout bigLogoContainer=new LinearLayout(activity);
	     	bigLogoContainer.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
	     	bigLogoContainer.setGravity(Gravity.CENTER);
	     	bigLogoContainer.addView(rebelLogo);
	     	mainLayout.addView(bigLogoContainer);
	     	isLayoutToClean=true;
     	}
     	return mainLayout;
    }
    
    private void cleanLayout(){
    	if(isLayoutToClean){
    		mainLayout.removeViewAt(mainLayout.getChildCount()-1);
    		isLayoutToClean=false;
    	}
    }
    
	public void onActivityCreated (Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		Bundle args = this.getArguments();
		if(hasNews()){
			showArticle(args.getInt(NEWS_INDEX));
		}
	}
    
    
    public void onCreate(Bundle savedInstanceState){
    	super.onCreate(savedInstanceState);
    	setHasOptionsMenu(true);
    }
    
    
	
   	/**
   	 * Display the news on the frontend
   	 * @param index of the news to show on the arrayList in the Application context that keeps
   	 * every news
   	 */
    private void showArticle(int index){
    	articleWV=new WebView(getActivity());
    	articleWV.setWebChromeClient(new WebChromeClient() {
    	});
		WebSettings settings = articleWV.getSettings();
		settings.setJavaScriptEnabled(true);
		settings.setPluginState(WebSettings.PluginState.ON);
    	NewsReaderContext context =  NewsReaderContext.getInstance(getActivity()); 
    	List<FeedNews> newsList = context.getNewsRetrieved();
    	FeedNews newsToShow = newsList.get(index); 
    	if(Parser.Util.isNetworkAvailable(getActivity())){
    		HTMLloaderTask = new ArticleLoader();
    		HTMLloaderTask.execute(newsToShow);
    	}
    }
    

	private class ArticleLoader extends AsyncTask<FeedNews, Void, String>{
		private FeedNews newsToShow;
		private final static String JS_OBJECT_NAME="news";
		
		
		protected void onPreExecute(){
			cleanLayout();
			ProgressBar loadingView=new ProgressBar(getActivity(),null,android.R.attr.progressBarStyleLarge);
			LayoutParams progressParam=new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
			loadingView.setIndeterminate(true);
			progressParam.addRule(RelativeLayout.CENTER_IN_PARENT);
			loadingView.setLayoutParams(progressParam);
			mainLayout.addView(loadingView);
			isLayoutToClean=true;
			
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
			cleanLayout();
			if(htmlNewsBody!=null){ 
				articleWV.addJavascriptInterface(htmlNewsBody, JS_OBJECT_NAME);
				articleWV.loadUrl("file:///android_asset/www/news_page.html");
				//messo questo if per evitare di riaggiungere la webview, per i tablet.
				if(mainLayout.getChildCount()==0){
					mainLayout.addView(articleWV,new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
					if(shareAction!=null){
						shareAction.setShareIntent(getSharingIntent(newsToShow.getTitle(), newsToShow.getAddress()));
					}
				}
			}
		}
    	
    }

	public void onPause(){
		super.onPause();
		if(HTMLloaderTask!=null){
			HTMLloaderTask.cancel(true);
		}
		if(articleWV!=null){
			articleWV.loadUrl("file:///android_asset/nonexistent.html");
		}
	}
	
	
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
		return args!= null && args.containsKey(NEWS_INDEX);
	}
	

	
	public void onCreateOptionsMenu(Menu menu,MenuInflater inflater) {
		if(hasNews()){
			inflater.inflate(R.menu.article_menu, menu);
			MenuItem item=menu.findItem(R.id.menu_share_article);
			shareAction=(ShareActionProvider)item.getActionProvider();
		}
	}
	
	
	

}
