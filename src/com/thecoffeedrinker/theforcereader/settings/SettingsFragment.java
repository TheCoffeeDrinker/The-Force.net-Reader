package com.thecoffeedrinker.theforcereader.settings;

import com.thecoffeedrinker.theforcereader.NewsReaderContext;
import com.thecoffeedrinker.theforcereader.R;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener{
	 public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        addPreferencesFromResource(R.xml.preferences);
	    }
	 
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
				String key) {
		NewsReaderContext context = NewsReaderContext.getInstance(getActivity());
		if(key.equals(getString(R.string.setting_preference_static_refresh_time_key))){
			context.restartPeriodicRetrievalService();
		}
	}
	 
	 @Override
	 public void onResume() {
	     super.onResume();
	     getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

	 }

	 @Override
	 public void onPause() {
	     getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	     super.onPause();
	 }
}
