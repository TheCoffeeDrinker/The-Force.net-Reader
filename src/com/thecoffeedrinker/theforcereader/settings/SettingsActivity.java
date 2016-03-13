package com.thecoffeedrinker.theforcereader.settings;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MenuItem;


public class SettingsActivity extends FragmentActivity{
	
	 protected void onCreate(Bundle savedInstanceState) {
		 super.onCreate(savedInstanceState);

	     getFragmentManager().beginTransaction()
	     	.replace(android.R.id.content, new SettingsFragment())
	        .commit();
	     getActionBar().setDisplayHomeAsUpEnabled(true);
	    }
	 
	 public boolean onOptionsItemSelected(MenuItem menuItem){       
		    onBackPressed();
		    return true;
		}

}
