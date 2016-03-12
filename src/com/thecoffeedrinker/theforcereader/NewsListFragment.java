package com.thecoffeedrinker.theforcereader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.thecoffeedrinker.theforcereader.newsmanager.FeedNews;
import com.thecoffeedrinker.theforcereader.settings.SettingsActivity;

/**
 * Show the news list
 * @author carlo
 *
 */
public class NewsListFragment extends ListFragment{
	private OnNewsSelectedListener selectionListener;
	private RelativeLayout listLayout;
	private boolean removeTopView;
	private int selectedItem=-1;
	private ViewCreated viewEditable;
	
	interface ViewCreated{
		public void onViewCreatedListener();
	}
	
	public void onAttach(Activity activity){
		super.onAttach(activity);
		selectionListener=(OnNewsSelectedListener)activity;
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this.getActivity()).build();
		ImageLoader.getInstance().init(config);
	}
	
	public interface OnNewsSelectedListener{
		public void onNewsSelected(int index);//single screen
	}
	
	
	
	public void onListItemClick(ListView l, View v, int position, long id){
		if(((NewsListActivity)getActivity()).isSplitScreen()){
			//if there is the article section on the screen the news selected will be highlighted; using this cycle
			//the last one will be restored to the normale state, before highlight the new one
			for(int i = 0;i<l.getChildCount();i++){
				View listItem = l.getChildAt(i);
				if(listItem!=null){
					listItem.setBackgroundColor(getResources().getColor(R.color.unselected));
				}
			}
			//hightlight the element
			v.setBackgroundColor(getResources().getColor(R.color.Selected));
			selectedItem = position;
		}
		//notify the selection to the activity
		selectionListener.onNewsSelected(position);
	}
	

	
	public void onActivityCreated (Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);
		if(viewEditable!=null){
			viewEditable.onViewCreatedListener();
		}
	}
	
	public void setOnViewCreated(ViewCreated viewCreated){
		this.viewEditable = viewCreated;
	}
	
	
	public void loadList(List<FeedNews> newsList, int indexNewsSelected){
		if(removeTopView){
			listLayout.removeViewAt(listLayout.getChildCount()-1);
		}
		selectedItem = indexNewsSelected;
		if(newsList!=null && getActivity()!=null){
			NewsPreviewAdapter newsAdapter = new NewsPreviewAdapter(getActivity(), 
					R.layout.news_item, newsList);
			setListAdapter(newsAdapter);
		}
	}
	
	public boolean isListEmpty(){
		return getListAdapter()==null || getListAdapter().getCount()==0;
	}
	
	/**
	 * Array Adapter for the news list
	 * @author carlo
	 *
	 */
	private class NewsPreviewAdapter extends ArrayAdapter<FeedNews> {
		private LayoutInflater  inflater;
		private ArrayList<FeedNews> newsPrevList;
		private int layoutId;
		
		public NewsPreviewAdapter(Context context, int layout, List<FeedNews> newsList) {
			super(context,layout,newsList);
			layoutId=layout;
			newsPrevList=(ArrayList<FeedNews>) newsList;
			inflater=LayoutInflater.from(context);
		}
		
		public View getView(int position, View rowView, ViewGroup parent) {
			rowView = inflater.inflate(layoutId, null);
			FeedNews news=newsPrevList.get(position);
	        TextView title=(TextView) rowView.findViewById(R.id.textTitle);
	        title.setText(news.getTitle());
	        TextView description=(TextView) rowView.findViewById(R.id.textDescription);
	        Spanned spannedDescr=Html.fromHtml(news.getDescription());
	        if(spannedDescr!=null){
	        	description.setText(spannedDescr.toString());
	        }
		    ImageView thumbnail=(ImageView) rowView.findViewById(R.id.thumbnail);
		    ThumbnailRetriever thumbRetriever=new ThumbnailRetriever(thumbnail);
		    //add the object to the queue of the task, so in case it will be interrupted.
		    thumbRetriever.execute(news);
		    if(position==selectedItem && ((NewsListActivity)getActivity()).isSplitScreen()){
		    	rowView.setBackgroundColor(getResources().getColor(R.color.Selected));
		    	//hightlight the item if it is the case
		    }
	        return rowView;
		}
		
    }
	
	/**
	 * AsynchTask class to retrieve the thumbnail picture of an article and load it on the list (display it)
	 * @author carlo
	 *
	 */
	private class ThumbnailRetriever extends AsyncTask<FeedNews, Void, String>{
		private ImageView imageViewToFill;
		ImageLoader imageLoader = ImageLoader.getInstance();
		DisplayImageOptions displayOptions = new DisplayImageOptions.Builder()
			.cacheInMemory(true)
			.cacheOnDisc(true)
			.bitmapConfig(Bitmap.Config.RGB_565)
			.imageScaleType(ImageScaleType.IN_SAMPLE_INT)
			.showImageForEmptyUri(R.drawable.tf_logo)
			.showImageOnFail(R.drawable.tf_logo)
			.displayer(new FadeInBitmapDisplayer(600))
			.build();
		
		public ThumbnailRetriever(ImageView imageView) {
			imageViewToFill=imageView;
		}
		
		@Override
		protected String doInBackground(FeedNews... arg0) {
			String thumbnailUrl=null;
			try {
				thumbnailUrl=arg0[0].getThumbnailUrl();
			} catch (IOException e) {
				Log.e("TheForce.net Reader", e.getMessage());
			}
			return thumbnailUrl;
				
		}
		
		protected void onPostExecute(String resultUrl){
			if(resultUrl!=null && !resultUrl.isEmpty()){
				imageLoader.displayImage(resultUrl, imageViewToFill, displayOptions);
			}
		}
		
	}
	
	public void onCreateOptionsMenu(Menu menu,MenuInflater inflater) {
	    inflater.inflate(R.menu.news_list_menu, menu);
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent settingIntent = new Intent(getActivity(), SettingsActivity.class);
		startActivity(settingIntent);
		return true;
	}
	
	
}
