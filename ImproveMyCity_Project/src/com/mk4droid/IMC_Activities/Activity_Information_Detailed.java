/**   Activity_Information_Detailed  */
package com.mk4droid.IMC_Activities;


import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

import com.flurry.android.FlurryAgent;
import com.mk4droid.IMC_Store.Constants_API;
import com.mk4droid.IMCity_PackDemo.R;


/**
 *  Details about the application and the authors
 *  
 * @copyright   Copyright (C) 2012 - 2013 Information Technology Institute ITI-CERTH. All rights reserved.
 * @license     GNU Affero General Public License version 3 or later; see LICENSE.txt
 * @author      Dimitrios Ververidis for the Multimedia Group (http://mklab.iti.gr). 
 *
 */
public class Activity_Information_Detailed extends Activity implements OnClickListener{

	static Context ctx;
	Resources resources;
	ImageButton btContact, btMklab, btUrenio; 
	//====================== On Create Activity ==================
	/** Set content view only */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ctx = this;

		resources = SetResources();


		setContentView(R.layout.activity_information_detailed);

		btContact = (ImageButton) findViewById(R.id.imbt_contact);
		btMklab   = (ImageButton) findViewById(R.id.imbt_mklab);
		btUrenio  = (ImageButton) findViewById(R.id.imbt_urenio);


		btContact.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Intent emailIntentQuest = new Intent(android.content.Intent.ACTION_SEND);  

				//	----------- Data ---------------			
				String aEmailListQuest[] = { Constants_API.ContactEmail, "ververid@iti.gr"};  

				emailIntentQuest.putExtra(android.content.Intent.EXTRA_EMAIL, aEmailListQuest);
				emailIntentQuest.putExtra(android.content.Intent.EXTRA_SUBJECT, "I wish an adapted version of IMC");  

				emailIntentQuest.setType("plain/text");  
				emailIntentQuest.putExtra(android.content.Intent.EXTRA_TEXT,"");
				// 	-------------------------

				startActivity(Intent.createChooser(emailIntentQuest, "Send your email with:"));
				//	-------------------------------

			}
		});


		btMklab.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Intent myWebLink = new Intent(android.content.Intent.ACTION_VIEW);
				myWebLink.setData(Uri.parse(resources.getString(R.string.CERTHlink)));
				startActivity(myWebLink);
			}
		});


		btUrenio.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent myWebLink = new Intent(android.content.Intent.ACTION_VIEW);
				myWebLink.setData(Uri.parse(resources.getString(R.string.URENIOlink)));
				startActivity(myWebLink);

			}
		});
	}

	//================= onClick ======================
	/** e-mail button */
	@Override
	public void onClick(View arg0) {
		int id = arg0.getId();

		switch(id){
		case (R.id.btSendMail):
			//----------- Data ---------------			
			Intent emailIntentQuest = new Intent(android.content.Intent.ACTION_SEND);  

		//	----------- Data ---------------			
		String aEmailListQuest[] = { Constants_API.ContactEmail, };  

		emailIntentQuest.putExtra(android.content.Intent.EXTRA_EMAIL, aEmailListQuest);


		PackageInfo pack_inf;
		String versionName = "";
		try {
			pack_inf = getPackageManager().getPackageInfo(getPackageName(), 0);
			versionName = pack_inf.versionName;
		} catch (NameNotFoundException e) {
		}


		emailIntentQuest.putExtra(android.content.Intent.EXTRA_SUBJECT, "Android v."+versionName);  

		emailIntentQuest.setType("plain/text");  
		emailIntentQuest.putExtra(android.content.Intent.EXTRA_TEXT,"");
		// 	-------------------------

		startActivity(Intent.createChooser(emailIntentQuest, "Send your email with:"));
		//	-------------------------------
		break;
		}
	}


	//=============== Flurry on Start - onStop =====================
	/** Flurry start */
	public void onStart()
	{
		super.onStart();
		SharedPreferences mshPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		boolean AnalyticsSW                = mshPrefs.getBoolean("AnalyticsSW", true);

		if (AnalyticsSW)
			FlurryAgent.onStartSession(this, Constants_API.Flurry_Key);
	}

	/** Flurry stop */
	public void onPause()
	{
		super.onPause();
		SharedPreferences mshPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		boolean AnalyticsSW                = mshPrefs.getBoolean("AnalyticsSW", true);

		if (AnalyticsSW)
			FlurryAgent.onEndSession(this);
	}
	//----------------------------------------    



	/* Retrieve Language preference	 */
	private Resources SetResources() {
		SharedPreferences mshPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

		String LangSTR = mshPrefs.getString("LanguageAR", Constants_API.DefaultLanguage);

		Configuration conf = getResources().getConfiguration();
		conf.locale = new Locale(LangSTR.substring(0, 2)); // ----- Convert
		// Greek -> el
		// ---------
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		return new Resources(getAssets(), metrics, conf);
	}
}
