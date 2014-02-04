package com.thecoffeedrinker.theforcereader.settings;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MenuItem;


public class SettingsActivity extends FragmentActivity{
	public final static String KEY_SETTING_NOTIFICATION_ENABLED="pref_notification";
	public final static String KEY_SETTING_RUN_BOOT="pref_boot_start";
	
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
