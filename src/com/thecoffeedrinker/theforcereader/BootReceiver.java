package com.thecoffeedrinker.theforcereader;

import com.thecoffeedrinker.theforcereader.settings.SettingsActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Receiver to run the application when the device is switched on
 * @author carlo
 *
 */
public class BootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent arg1) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		//get the preference to determine if the service has to be started or not
		boolean runStartUp = settings.getBoolean(SettingsActivity.KEY_SETTING_RUN_BOOT, true);
		if(runStartUp){
			NewsReaderContext.startRetrievalService(context);
		}
	}

}

