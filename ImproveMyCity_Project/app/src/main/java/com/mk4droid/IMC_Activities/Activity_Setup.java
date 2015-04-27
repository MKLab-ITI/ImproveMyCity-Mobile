/** Activity_Setup */

package com.mk4droid.IMC_Activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.mk4droid.IMC_Core.Preference_AccountOperations;
import com.mk4droid.IMC_Services.InternetConnCheck;
import com.mk4droid.IMC_Store.Constants_API;
import com.mk4droid.IMCity_PackDemo.R;

import java.util.Locale;

/**
 * This is the activity where application settings can be modified.
 * 
 * @copyright   Copyright (C) 2012 - 2015 Information Technology Institute ITI-CERTH. All rights reserved.
 * @license     GNU Affero General Public License version 3 or later; see LICENSE.txt
 * @author      Dimitrios Ververidis for the Multimedia Group (http://mklab.iti.gr). 
 *
 */
public class Activity_Setup extends PreferenceActivity {

	int tlv = Toast.LENGTH_LONG;

	public static Resources resources;     //  for Language
	public static Context ctx;

	//preferenceUN, preferencePass

	Preference prefAccountOper, prefLang, prefDistance,
	prefRefrate, prefIssuesNo, prefFlurryAnal, prefVersion, prefAbout, prefEmail, prefReset;
	Preference CategCustomPref,CategLangPref,CategSystemPref, CategAboutPref; 

	SharedPreferences prefs;

	boolean AuthFlag;

	String LangSTR, PassSTR, UserNameSTR,emailSTR = "";

	public static String UserRealName;

	int RefrateSTR,IssuesNoSTR;

	int UserID;

	private BroadcastReceiver mReceiverAuth_Setup;

	//================ onCreate ====================
	/**
	 *    Set contents of setup activity   
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ctx = this;
		
		//-------- Set resources ------
		resources  = setResources();

		// if can not inflate layout, destroy activity
		try {
			PreferenceManager.setDefaultValues(this, R.xml.myprefs, true);

			//------ Load User Name and Pass from preferences
			setContentView(R.layout.activity_setup);
			addPreferencesFromResource(R.xml.myprefs);

			//----------- DATA --------------------
			CategCustomPref= findPreference("CategCustom");
			CategLangPref  = findPreference("CategLang");
			CategSystemPref   = findPreference("CategSystem");
			CategAboutPref = findPreference("CategAbout");
			prefAccountOper= findPreference("Account_Operations_IMC");


			if (AuthFlag)
				prefAccountOper.setSummary(resources.getString(R.string.LogOut));
			else 
				prefAccountOper.setSummary(resources.getString(R.string.LoginRegisterRemind));

			prefLang   = findPreference("LanguageAR");
			prefLang.setSummary(LangSTR);

			prefRefrate = findPreference("RefrateAR");
			prefRefrate.setSummary(RefrateSTR + " " + resources.getString(R.string.minutes));
			prefRefrate.setOnPreferenceChangeListener(prefRefrate_change);

			prefIssuesNo     = findPreference("IssuesNoAR");
			prefIssuesNo.setSummary(IssuesNoSTR + " " + resources.getString(R.string.Issues));

			prefIssuesNo.setOnPreferenceChangeListener(prefIssuesNo_change);

			prefFlurryAnal   = findPreference("AnalyticsSW");
			prefDistance     = findPreference("distance_seekBar");

			prefLang.setOnPreferenceChangeListener(prefLang_change);

			prefVersion = findPreference("Version");
			prefAbout   = findPreference("About");
			prefEmail   = findPreference("Email");
			prefReset   = findPreference("Reset");

			try {
				PackageInfo pack_inf = getPackageManager().getPackageInfo(getPackageName(), 0);
				String versionName = pack_inf.versionName;
				int versionCode = pack_inf.versionCode;
				prefVersion.setSummary(versionName +", serial code: " + Integer.toString(versionCode));
			} catch (NameNotFoundException e) {
			}

			//--------------- Receiver for Authenticated -------
			IntentFilter intentFilter = new IntentFilter("android.intent.action.MAIN");

			mReceiverAuth_Setup = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {

					String Auth = intent.getStringExtra("Authenticated");         

					if (Auth!=null)
						if (Auth.equals("success")){                                 // 3 Auth success go to TabHost                                   
							prefAccountOper.setSummary(resources.getString(R.string.LogOut));

							if (Preference_AccountOperations.dlgLogin!=null)
								if (Preference_AccountOperations.dlgLogin.isShowing())
									Preference_AccountOperations.dlgLogin.dismiss();

						} else if (Auth.equals("failed")) {                          // 4 Auth failed go to Splash_Register
							prefAccountOper.setSummary(resources.getString(R.string.LoginRegisterRemind));						
						}
				}
			};

			this.registerReceiver(mReceiverAuth_Setup, intentFilter);

		} catch (Exception e){ // Shutdown
			ctx = null;
			finish();
		}
	}

	//============ onResume =================
	/**
	 *     on resume from tab changing
	 */
	@Override
	protected void onResume(){
		super.onResume();

		if (ctx != null){
			resources  = setResources();

			if (InternetConnCheck.getInstance(ctx).isOnline(ctx)){
				prefIssuesNo.setEnabled(true);
				prefRefrate.setEnabled(true);
			} else {
				prefIssuesNo.setEnabled(false);
				prefRefrate.setEnabled(false);
				prefDistance.setEnabled(false);
			}

			//----------- Flurry Analytics --------
			SharedPreferences mshPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
			boolean AnalyticsSW                = mshPrefs.getBoolean("AnalyticsSW", true);

			if (AnalyticsSW)
				FlurryAgent.onStartSession(this, Constants_API.Flurry_Key);
		}
	}

	//============= onPause =================
	/**
	 *     Pausing this activity 
	 */
	@Override
	public void onPause()
	{
		super.onPause();
		SharedPreferences mshPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

		if (mshPrefs.getBoolean("AnalyticsSW", true))
			FlurryAgent.onEndSession(this);
	}


	/* (non-Javadoc)
	 * @see android.preference.PreferenceActivity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		unregisterReceiver(mReceiverAuth_Setup);
		super.onDestroy();
	}

	//================= prefIssuesNo_change =================================
	/**
	 *   
	 */
	private OnPreferenceChangeListener prefRefrate_change = new OnPreferenceChangeListener() {
		public boolean onPreferenceChange(Preference preference, Object NewValue) {
			prefRefrate.setSummary(NewValue + " " + resources.getString(R.string.minutes));
			return true;
		}
	};

	//================= prefIssuesNo_change =================================
	private OnPreferenceChangeListener prefIssuesNo_change = new OnPreferenceChangeListener() {
		public boolean onPreferenceChange(Preference preference, Object NewValue) {

			prefIssuesNo.setSummary(NewValue + " " + resources.getString(R.string.Issues));
			return true;
		}
	};

	//================= prefLang_change =================================
	/**
	 *   Get language from GUI input and change widgets accordingly 
	 */
	private OnPreferenceChangeListener prefLang_change = new OnPreferenceChangeListener() {
		public boolean onPreferenceChange(Preference preference, Object NewValue) {

			LangSTR     = NewValue.toString();
			savePreferences("LanguageAR",LangSTR,"String");
			resources   = setResources();

			((TextView)FActivity_TabHost.mTabHost.getTabWidget().getChildAt(0).findViewWithTag("tv")).setText(resources.getString(R.string.Map));
			((TextView)FActivity_TabHost.mTabHost.getTabWidget().getChildAt(1).findViewWithTag("tv")).setText(resources.getString(R.string.List));
			((TextView)FActivity_TabHost.mTabHost.getTabWidget().getChildAt(2).findViewWithTag("tv")).setText(resources.getString(R.string.Report));
			((TextView)FActivity_TabHost.mTabHost.getTabWidget().getChildAt(3).findViewWithTag("tv")).setText(resources.getString(R.string.Filter));
			((TextView)FActivity_TabHost.mTabHost.getTabWidget().getChildAt(4).findViewWithTag("tv")).setText(resources.getString(R.string.Settings));


			CategCustomPref.setTitle(resources.getString(R.string.CustAccount));
			CategSystemPref.setTitle(resources.getString(R.string.System));
			CategAboutPref.setTitle(resources.getString(R.string.About));

			prefAccountOper.setTitle(resources.getString(R.string.AccountOperations));
			if (AuthFlag)
				prefAccountOper.setSummary(resources.getString(R.string.LogOut));
			else 
				prefAccountOper.setSummary(resources.getString(R.string.LoginRegisterRemind));



			prefLang.setSummary(LangSTR);

			prefDistance.setTitle(resources.getString(R.string.ViewRange)); 
			prefDistance.setSummary(resources.getString(R.string.ViewRadius));

			prefRefrate.setTitle(resources.getString(R.string.Refrinter));
			prefRefrate.setSummary(RefrateSTR + " " + resources.getString(R.string.minutes));

			prefIssuesNo.setTitle(resources.getString(R.string.IssuesNo));
			prefIssuesNo.setSummary(IssuesNoSTR + " " + resources.getString(R.string.Issues));

			prefFlurryAnal.setTitle(resources.getString(R.string.Analytics));

			prefVersion.setTitle(resources.getString(R.string.Version));

			prefAbout.setTitle(resources.getString(R.string.About));
			prefEmail.setTitle(resources.getString(R.string.Yourproposal));
			prefReset.setTitle(resources.getString(R.string.Reset));

			prefReset.setSummary(resources.getString(R.string.Deleteallissuedatastoredlocallytoyourphone));
			return true;
		}
	};  

	/*
	 *  Set language Resources depending on the language saved in the preferences   
	 */
	private Resources setResources(){

		prefs = PreferenceManager.getDefaultSharedPreferences(ctx);     	// Get Preferences -------

		UserNameSTR    = prefs.getString("UserNameAR" , "");
		emailSTR       = prefs.getString("emailAR"    , "");
		PassSTR        = prefs.getString("PasswordAR" , "");
		UserRealName   = prefs.getString("UserRealName" , "");
		LangSTR        = prefs.getString("LanguageAR" , Constants_API.DefaultLanguage);

		RefrateSTR     = Integer.parseInt( prefs.getString("RefrateAR"  , "5"));	

		IssuesNoSTR     = Integer.parseInt( prefs.getString("IssuesNoAR"  , "40"));

		AuthFlag  = prefs.getBoolean("AuthFlag", false);


		Configuration conf = getResources().getConfiguration();
		conf.locale = new Locale(LangSTR.substring(0, 2)); //----- Convert Greek -> el ---------
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		return new Resources(getAssets(), metrics, conf);
	}


	/*
	 * Save a value to preferences, either string or boolean
	 * 
	 * @param key       name of the parameters to save
	 * @param value     value of the parameter to save 
	 * @param type      either "String" or "Boolean" 
	 */
	private void savePreferences(String key, Object value, String type){
		SharedPreferences shPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		SharedPreferences.Editor editor = shPrefs.edit();

		if (type.equals("String")) 
			editor.putString(key, (String) value);
		else if (type.equals("Boolean"))
			editor.putBoolean(key, (Boolean) value);
		else if (type.equals("Int")){

			int intval = 0;
			try { 
				intval = Integer.parseInt((String) value);
			} catch (Exception e){
				intval = Integer.valueOf(value.toString());
			}
			editor.putInt(key, intval);
		}

		editor.commit();
	}
}