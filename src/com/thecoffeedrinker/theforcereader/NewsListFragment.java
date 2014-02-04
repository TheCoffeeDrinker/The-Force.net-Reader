package com.thecoffeedrinker.theforcereader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import com.handmark.pulltorefresh.extras.listfragment.PullToRefreshListFragment;

import com.thecoffeedrinker.theforcereader.newsmanager.FeedNews;
import com.thecoffeedrinker.theforcereader.settings.SettingsActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.util.LruCache;
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


public class NewsListFragment extends PullToRefreshListFragment{
	private OnNewsSelectedListener selectionListener;
	private LruCache<String, Bitmap> thumbCache;
	private RelativeLayout listLayout;
	private boolean removeTopView;
	private int selectedItem = -1;
	
	public void onAttach(Activity activity){
		super.onAttach(activity);
		selectionListener=(OnNewsSelectedListener)activity;
		thumbCache=NewsReaderContext.getInstance(getActivity()).getThumbnailCache();
	}
	
	public interface OnNewsSelectedListener{
		public void onNewsSelected(int index);//single screen
	}
	
	public void onListItemClick(ListView l, View v, int position, long id){
		if(((NewsListActivity)getActivity()).isSplitScreen()){
			((NewsPreviewAdapter)getListAdapter()).notifyDataSetChanged();
		}
		selectedItem = position -1;
		selectionListener.onNewsSelected(selectedItem); 
	}
	
	
	public void onActivityCreated (Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);
	}
	
	
	public void loadList(List<FeedNews> newsList){
		//if reset position is true the list must be positioned on top
		if(removeTopView){
			listLayout.removeViewAt(listLayout.getChildCount()-1);
		}
		if(newsList!=null && getActivity()!=null){
			NewsPreviewAdapter newsAdapter = new NewsPreviewAdapter(getActivity(), 
					R.layout.news_item, newsList);
			setListAdapter(newsAdapter);
		}
	}
	
	public void onPause(){
		super.onPause();
		NewsPreviewAdapter adapter = (NewsPreviewAdapter) getListAdapter();
		if(adapter!=null){
			adapter.stopEveryTask();
		}
	}
	
	private class NewsPreviewAdapter extends ArrayAdapter<FeedNews> {
		private LayoutInflater  inflater;
		private ArrayList<FeedNews> newsPrevList;
		private int layoutId;
		private List<ThumbnailRetriever> thumbLoaderTaskList;
		
		public NewsPreviewAdapter(Context context, int layout, List<FeedNews> newsList) {
			super(context,layout,newsList);
			layoutId=layout;
			newsPrevList=(ArrayList<FeedNews>) newsList;
			inflater=LayoutInflater.from(context);
			thumbLoaderTaskList= new ArrayList<NewsListFragment.ThumbnailRetriever>();
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
		    thumbLoaderTaskList.add(thumbRetriever);
		    thumbRetriever.execute(news);
		    if(position==selectedItem){
		    	rowView.setBackgroundColor(getResources().getColor(R.color.Selected));
		    }
	        return rowView;
		}
		
		public void stopEveryTask(){
			for(ThumbnailRetriever task:thumbLoaderTaskList){
				task.cancel(true);
			}
		}
    }
	
	private class ThumbnailRetriever extends AsyncTask<FeedNews, Void, Bitmap>{
		private ImageView imageViewToFill;
		
		public ThumbnailRetriever(ImageView imageView) {
			imageViewToFill=imageView;
		}
		
		@Override
		protected Bitmap doInBackground(FeedNews... arg0) {
			String thumbnailUrl=null;
			try {
				thumbnailUrl=arg0[0].getThumbnailUrl();
				if(thumbnailUrl!=null){
					if(thumbnailUrl.isEmpty() ) return null;
					Bitmap cachePicture=thumbCache.get(thumbnailUrl);
					if(cachePicture!=null){
						return cachePicture;
					}else{
						try {
							URL pictureURL = new URL(thumbnailUrl);
							URLConnection picConnection=pictureURL.openConnection();
							InputStream picStream= picConnection.getInputStream();
							Bitmap loadedPicture =BitmapFactory.decodeStream(picStream);
							if(loadedPicture!=null){
								thumbCache.put(thumbnailUrl, loadedPicture);
							}
							return loadedPicture; 
						} catch (IOException e) {
							Log.e(getString(R.string.app_name), e.getMessage());
							return null;
						}
					}
				}else {
					return null;
				}
			} catch (IOException e) {
				Log.e(getString(R.string.app_name),e.getMessage());
				return null;
			}
		}
		
		protected void onPostExecute(Bitmap result){
			if(result!=null){
				imageViewToFill.setImageBitmap(result);
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
