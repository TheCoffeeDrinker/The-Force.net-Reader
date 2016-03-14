package com.thecoffeedrinker.theforcereader;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
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
		Resources resources = context.getResources();
		String keySettingBootRun = resources.getString(R.string.setting_run_at_startup_key);
		boolean defaultBootRun = context.getResources().getBoolean(R.bool.default_setting_boot_start);
		boolean runStartUp = settings.getBoolean(keySettingBootRun, defaultBootRun);
		if(runStartUp){
			NewsReaderContext newsReaderContext = NewsReaderContext.getInstance(context);
			//avoid multiple run of the same service
			newsReaderContext.restartPeriodicRetrievalService();
		}
	}

}

