/**  Activity_Splash_Register */

package com.mk4droid.IMC_Activities;

import java.util.Locale;

import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.mk4droid.IMC_Services.Security;
import com.mk4droid.IMC_Store.Constants_API;
import com.mk4droid.IMC_Store.Phptasks;
import com.mk4droid.IMC_Utils.My_System_Utils;
import com.mk4droid.IMCity_PackDemo.R;

/**
 * If the user is not logged in, then this activity appears. 
 *
 * @copyright   Copyright (C) 2012 - 2013 Information Technology Institute ITI-CERTH. All rights reserved.
 * @license     GNU Affero General Public License version 3 or later; see LICENSE.txt
 * @author      Dimitrios Ververidis for the Multimedia Group (http://mklab.iti.gr). 
 *
 */
public class Activity_Splash_Login extends Activity implements OnClickListener{
	
	static Handler handlerRegisterButtonDisable;
	
	public static EditText et_username, et_password;
	String LangSTR, usernameSTR="", passwordSTR="", emailSTR = "";
	static Context ctx;
	Resources resources;
	
	
	String userRealName;
	Button bt_tf_regORcreate;
	private BroadcastReceiver mReceiverAuth;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    //--------------- Receiver for Authenticated -------
	    IntentFilter intentFilter = new IntentFilter("android.intent.action.MAIN");
	    
	    mReceiverAuth = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {

				String Auth = intent.getStringExtra("Authenticated");         // 1
				
				if (Auth!=null)
					if (Auth.equals("success")){
						startActivity(new Intent(ctx,FActivity_TabHost.class));
						finish();
					} else if (Auth.equals("failed")) {
						Toast.makeText(ctx, resources.getString(R.string.tryagain), Toast.LENGTH_SHORT).show();
					}
			}
		};

		this.registerReceiver(mReceiverAuth, intentFilter);
		
		//----------------------------------------
	    resources = setResources();
	    setContentView(R.layout.activitiy_splash_register);
	    ctx  = this;
	    
	    My_System_Utils.CheckPrefs(PreferenceManager.getDefaultSharedPreferences(this));
	    	    
	    et_username = (EditText) findViewById(R.id.et_username_splash);
	    et_password = (EditText) findViewById(R.id.et_password_splash);
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		unregisterReceiver(mReceiverAuth);
		super.onDestroy();
	}

	//=========== onClick =================================================
	/**
	 *     Click listening of any button in this activity
	 */
	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()){
		case R.id.btLoginSplash:
			
			String usernameSTR = et_username.getText().toString();
			String passwordSTR = et_password.getText().toString();
			
			InputMethodManager imm = (InputMethodManager)ctx.getSystemService(Service.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(et_username.getWindowToken(), 0); 
			imm.hideSoftInputFromWindow(et_password.getWindowToken(), 0);
			
			//------------ Authenticate -----------
		    Security.AuthFun(usernameSTR, passwordSTR, resources, ctx, false);
		    
			break;
		case R.id.btSkipLogin:
			startActivity(new Intent(this,FActivity_TabHost.class));
			break;
		case R.id.tvRegisterSplash:
			startActivity(new Intent(this, Activity_Register.class));
			break;
		case R.id.tvRemindSplash:
			 startActivity(new Intent(Intent.ACTION_VIEW, 
				       Uri.parse(Constants_API.COM_Protocol+ Constants_API.ServerSTR + 
	    		   				           Constants_API.phpExec + Phptasks.TASK_RESET_PASS)));
			break;
		}
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
