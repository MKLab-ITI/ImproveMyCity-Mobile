/**  Activity_Splash_Register */

package com.mk4droid.IMC_Activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.mk4droid.IMC_Services.Security;
import com.mk4droid.IMC_Services.Upload_Data;
import com.mk4droid.IMC_Store.Constants_API;
import com.mk4droid.IMCity_PackDemo.R;

import java.util.Locale;

/**
 * Activity for registering a new user. 
 *
 * @copyright   Copyright (C) 2012 - 2015 Information Technology Institute ITI-CERTH. All rights reserved.
 * @license     GNU Affero General Public License version 3 or later; see LICENSE.txt
 * @author      Dimitrios Ververidis for the Multimedia Group (http://mklab.iti.gr). 
 *
 */
public class Activity_Register extends Activity {

	static Context ctx;
	int tlv = Toast.LENGTH_LONG;
	Resources resources;
	TextView tvmes;
	String LangSTR;
	private BroadcastReceiver mReceiverAuth_Register;
	private IntentFilter intentFilter;
	boolean isReg_mReceiverAuth_Register = false; 

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		resources = setResources();

		//--------------- Receiver for Authenticated -------
		intentFilter = new IntentFilter("android.intent.action.MAIN");

		mReceiverAuth_Register = new BroadcastReceiver() {
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

		if (!isReg_mReceiverAuth_Register){
			this.registerReceiver(mReceiverAuth_Register, intentFilter);
			isReg_mReceiverAuth_Register = true;
		}

		//--------------- Receiver for Authenticated -------
		setContentView(R.layout.activity_register);
		ctx = this;

		tvmes = (TextView) findViewById(R.id.tv_register_advise);

		Button bt_tf_regORcreate = (Button) findViewById(R.id.bt_imc_register);

		bt_tf_regORcreate.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {

				EditText et_imc_username = (EditText) findViewById(R.id.et_imc_username);
				EditText et_imc_email    = (EditText) findViewById(R.id.et_imc_email);
				EditText et_imc_Password = (EditText) findViewById(R.id.et_imc_Password);
				EditText et_imc_name     = (EditText) findViewById(R.id.et_imc_name);

				String imc_username = et_imc_username.getText().toString();
				String imc_email    = et_imc_email.getText().toString();
				String imc_password = et_imc_Password.getText().toString();
				String imc_name     = et_imc_name.getText().toString();

				if (imc_name.length()>0 && imc_username.length()>0 && imc_email.contains("@") && imc_password.length()<=16){

					new AsyncRegister(imc_username, imc_email, imc_password, imc_name).execute();		

				} else if (imc_username.length()==0){
					tvmes.setText(resources.getString(R.string.Giveausername));
				} else if (imc_name.length()==0){
					tvmes.setText(resources.getString(R.string.Givealsoyourname));
				} else if (!imc_email.contains("@")){
					tvmes.setText(resources.getString(R.string.NotValidEmail));
				} else if (imc_password.length()>16){
					tvmes.setText(resources.getString(R.string.PasswordShorter));
				}


			}});


		Button bt_tf_regcancel = (Button) findViewById(R.id.bt_imc_register_cancel);

		bt_tf_regcancel.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {

				finish();
			}
		});
	}


	/* (non-Javadoc)
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {

		if (isReg_mReceiverAuth_Register){
			unregisterReceiver(mReceiverAuth_Register);
			isReg_mReceiverAuth_Register = false;
		}

		super.onDestroy();
	}


	/* (non-Javadoc)
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {

		if (isReg_mReceiverAuth_Register){
			unregisterReceiver(mReceiverAuth_Register);
			isReg_mReceiverAuth_Register = false;
		}

		super.onPause();
	}


	@Override
	protected void onResume() {


		if (!isReg_mReceiverAuth_Register){	
			this.registerReceiver(mReceiverAuth_Register,intentFilter);
			isReg_mReceiverAuth_Register = true;
		}

		super.onPause();
	}

	//=============== setResources =============================
	/* Retrieve preferences and set resources language */ 
	private Resources setResources(){
		SharedPreferences mshPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		LangSTR          = mshPrefs.getString("LanguageAR", Constants_API.DefaultLanguage);

		Configuration conf = getResources().getConfiguration();
		conf.locale = new Locale(LangSTR.substring(0, 2)); //----- Convert Greek -> el ---------
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		return new Resources(getAssets(), metrics, conf);
	}



	private class AsyncRegister extends AsyncTask<String, String, String>{


		private String imc_username, imc_email, imc_password, imc_name;

		/**
		 * 
		 */
		public AsyncRegister(String imc_username_in, String imc_email_in, String imc_password_in, String imc_name_in) {

			imc_username = imc_username_in;
			imc_email    = imc_email_in;
			imc_password = imc_password_in;
			imc_name     = imc_name_in;

		}


		/* (non-Javadoc)
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		@Override
		protected String doInBackground(String... params) {

			//------------ URL CREATE ACCOUNT HERE ------------
			String response = Upload_Data.SendRegistrStreaming(imc_username, imc_email, imc_password, imc_name); 
			//-------------------------------------------------

			return response;
		};


		/* (non-Javadoc)
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(final String response) {

			final Dialog dlgNotif = new Dialog(ctx);
			dlgNotif.requestWindowFeature(Window.FEATURE_NO_TITLE);
			dlgNotif.setContentView(R.layout.dialog_register_completed);
			dlgNotif.show();
			TextView tv_imc_reg_comp = (TextView)dlgNotif.findViewById(R.id.tv_imc_registration_response);
			tv_imc_reg_comp.setText(response);

			Button bt_register_fin = (Button)dlgNotif.findViewById(R.id.bt_imc_register_completed_close);

			bt_register_fin.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {


					if (response.contains("Non valid")){
						dlgNotif.dismiss();	
					} else {
						//------------ Authenticate -----------
						Security.AuthFun(imc_username, imc_password, resources, ctx, false);
						finish();
					}
				}
			});

			super.onPostExecute(response);
		}
	}



}
