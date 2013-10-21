/**  Activity_Splash_Register */

package com.mk4droid.IMC_Activities;


import java.util.Locale;

import com.mk4droid.IMC_Services.InternetConnCheck;
import com.mk4droid.IMC_Services.Security;
import com.mk4droid.IMC_Store.Constants_API;
import com.mk4droid.IMCity_PackDemo.R;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

/**
 * This is a splash activity with no interaction elements that it is shown while trying to connect with the server for 
 * user authentication. 
 *
 * @copyright   Copyright (C) 2012 - 2013 Information Technology Institute ITI-CERTH. All rights reserved.
 * @license     GNU Affero General Public License version 3 or later; see LICENSE.txt
 * @author      Dimitrios Ververidis for the Multimedia Group (http://mklab.iti.gr). 
 *
 */
public class Activity_Thinking extends Activity {
	
	int tlv = Toast.LENGTH_LONG;
	String LangSTR, usernameSTR="", passwordSTR="", emailSTR = "";
	String userRealName;
	
	Context ctx;
	private BroadcastReceiver mReceiverAuth_Thinking;
	boolean isReg_mReceiverAuth_Thinking = false;
	Resources resources;
	
	boolean AuthFlag = false;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    //--------------- Receiver for Authenticated -------
	    IntentFilter intentFilter = new IntentFilter("android.intent.action.MAIN");
	    
	    mReceiverAuth_Thinking = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {

				String Auth = intent.getStringExtra("Authenticated");         
				
				if (Auth!=null)
					if (Auth.equals("success")){                                 // 3 Auth success go to TabHost                                   
						finish();
						startActivity(new Intent(ctx,FActivity_TabHost.class));
					} else if (Auth.equals("failed")) {                          // 4 Auth failed go to Splash_Register
						finish();
						startActivity(new Intent(ctx,Activity_Splash_Login.class));
					}
			}
		};

		this.registerReceiver(mReceiverAuth_Thinking, intentFilter);
		isReg_mReceiverAuth_Thinking = true;
		//----------------------------------------
	    
		resources = setResources();
	    setContentView(R.layout.activity_thinking);
	    ctx  = this;
	    
	    //------- Check GPS ------
	    LocationManager lm = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
	    if ( !lm.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
	        Toast.makeText(this, "No GPS", tlv).show();
	    }
	    	    

	    //------- Check Internet -----
	    if (InternetConnCheck.getInstance(this).isOnline(this)){
	    	if( usernameSTR.length()>0 ||  passwordSTR.length() >0)
	        	Security.AuthFun(usernameSTR, passwordSTR, resources, ctx, false);                 // 1. if internet try to Auth
	    	else {
	    		isReg_mReceiverAuth_Thinking = false;
	    		unregisterReceiver(mReceiverAuth_Thinking);
	    		finish();
	    		startActivity(new Intent(ctx,Activity_Splash_Login.class));                            // 5. No credentials
	    		
	    	}
	    }else{
	    	Toast.makeText(this, "No Internet", tlv).show();                                   // 2. No Internet go TabHost
	    	finish();
	    	startActivity(new Intent(ctx,FActivity_TabHost.class));
		}
	}

	
	/* (non-Javadoc)
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		if (isReg_mReceiverAuth_Thinking){
			try {
				unregisterReceiver(mReceiverAuth_Thinking);
			} catch (Exception e){
				Log.e("IMC Activity_Thinking","Receiver was already unregistered");
			}
		}
		super.onPause();
	}
	
	/*  onDestroy */
	@Override
	protected void onDestroy() {
		if (isReg_mReceiverAuth_Thinking){
			try {
				unregisterReceiver(mReceiverAuth_Thinking);
			} catch (Exception e){
				//Log.e("IMC Activity_Thinking","Receiver was already unregistered");
			}
		}
		super.onDestroy();
	}
	

    /* Retrieve preferences and set resources language */ 
	private Resources setResources(){
		SharedPreferences mshPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
	    LangSTR          = mshPrefs.getString("LanguageAR", Constants_API.DefaultLanguage);
        
	    usernameSTR      = mshPrefs.getString("UserNameAR", "");
	    emailSTR         = mshPrefs.getString("emailAR", "");
	    passwordSTR      = mshPrefs.getString("PasswordAR", "");
	    userRealName     = mshPrefs.getString("UserRealName", "");
	    
   	    Configuration conf = getResources().getConfiguration();
        conf.locale = new Locale(LangSTR.substring(0, 2)); //----- Convert Greek -> el ---------
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return new Resources(getAssets(), metrics, conf);
    }
}