// AccountOperationPreference 
package com.mk4droid.IMC_Core;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.mk4droid.IMC_Activities.Activity_Register;
import com.mk4droid.IMC_Activities.Activity_Setup;
import com.mk4droid.IMC_Services.InternetConnCheck;
import com.mk4droid.IMC_Services.Security;
import com.mk4droid.IMC_Services.Service_Data;
import com.mk4droid.IMC_Store.Constants_API;
import com.mk4droid.IMC_Store.Phptasks;
import com.mk4droid.IMCity_PackDemo.R;

/**
 * Custom preference item in setup menu for management of IMC account (remind, login, logout) 
 * 
 * @copyright   Copyright (C) 2012 - 2013 Information Technology Institute ITI-CERTH. All rights reserved.
 * @license     GNU Affero General Public License version 3 or later; see LICENSE.txt
 * @author      Dimitrios Ververidis for the Multimedia Group (http://mklab.iti.gr). 
 *
 */
public class Preference_AccountOperations extends Preference {

	/** A simple dialogue with two fields for username and password */
	public static Dialog dlgLogin;
	
	int tlv = Toast.LENGTH_LONG;
	Context ctx;
	SharedPreferences mshPrefs = PreferenceManager.getDefaultSharedPreferences(Activity_Setup.ctx);
	int CaseSW = 0;
	Preference_AccountOperations pref;
	

    /**
     * Constructor with 2 arguments
     * @param context
     * @param attrs
     */
	public Preference_AccountOperations(Context context, AttributeSet attrs) {
		super(context, attrs);
		ctx = context;
		pref = this;
	}
	
	/**
	 * Constructor with 3 arguments
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public Preference_AccountOperations(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	/**
	 *  The menu has items depending if user is logged in or not
	 */
	@Override
	protected void onClick() {

		AlertDialog.Builder builder = new AlertDialog.Builder(Activity_Setup.ctx);
		builder.setTitle(""); 
		builder.setIcon(null);

		String loginSTR = Activity_Setup.resources.getString(R.string.Login);
		String logoutSTR = Activity_Setup.resources.getString(R.string.LogOut);
		String registerSTR = Activity_Setup.resources.getString(R.string.Register);
		String remindSTR = Activity_Setup.resources.getString(R.string.Remind);
		String nointernetSTR = Activity_Setup.resources.getString(R.string.NoInternet);

		boolean AuthFlag    = mshPrefs.getBoolean("AuthFlag", false);
		boolean HasInternet = Service_Data.HasInternet;

		List<String> listItems = new ArrayList<String>();

		if (AuthFlag && HasInternet){
			CaseSW = 1;                // Logout show only
			listItems.add(logoutSTR);
		}else if (!AuthFlag && !HasInternet){
			CaseSW = 2;                // No internet message
			listItems.add(nointernetSTR);
		}else if (!AuthFlag && HasInternet){
			CaseSW = 3;                // Login, remind, register
			listItems.add(loginSTR);
			listItems.add(remindSTR);
			listItems.add(registerSTR);
		}else if (AuthFlag && !HasInternet){
			CaseSW = 4;                // No internet message
			listItems.add(nointernetSTR);
		}

		CharSequence[] CurrOptions = listItems.toArray(new CharSequence[listItems.size()]);


		builder.setItems(CurrOptions , new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {

				if (CaseSW==1 && which==0){
					logoutMTH();
				} else if (CaseSW==2 || CaseSW==4){

				} else if (CaseSW == 3){
					if (which==0){
						loginMTH();
					} else if (which==1){
						remindMTH();
					} else if (which==2){
						ctx.startActivity(new Intent(ctx, Activity_Register.class));
					}
				}
			}
		});

		builder.setNeutralButton(Activity_Setup.resources.getString(R.string.Cancel), 
				new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});

		builder.create();
		builder.show();

		super.onClick();
	}

	/**
	 *           Login
	 */
	public void loginMTH(){

		dlgLogin = new Dialog(Activity_Setup.ctx);
		dlgLogin.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dlgLogin.setContentView(R.layout.dialog_login);

		Button btSetupLoginCancel = (Button) dlgLogin.findViewById(R.id.btSetupLoginCancel);

		btSetupLoginCancel.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				dlgLogin.dismiss();
			}
		});

		Button btSetupLogin= (Button) dlgLogin.findViewById(R.id.btSetupLogin);

		btSetupLogin.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {

				if (InternetConnCheck.getInstance(ctx).isOnline(ctx)){

					String UserNameSTR = ((EditText)dlgLogin.findViewById(R.id.etSetupUsername)).getText().toString();
					String PassSTR = ((EditText)dlgLogin.findViewById(R.id.etSetupPassword)).getText().toString();

					Security.AuthFun(UserNameSTR, PassSTR, Activity_Setup.resources, ctx, true);
				} else {
					Toast.makeText(ctx, ctx.getResources().getString(R.string.NoInternet), tlv).show();
				}
			}});

		dlgLogin.show();
	}

	/**
	 *     Reming password
	 */
	public void remindMTH(){

		Intent browserIntent2 = new Intent(Intent.ACTION_VIEW, 
				Uri.parse("http://"+ Constants_API.ServerSTR + 
						Constants_API.phpExec + Phptasks.TASK_RESET_PASS));

		Activity_Setup.ctx.startActivity(browserIntent2);
	}

	/**
	 *   User register
	 */
	public void logoutMTH(){
		savePreferences("AuthFlag", false, "Boolean");
		savePreferences("PasswordAR", "", "String" );
		savePreferences("UserNameAR", "", "String" );
		savePreferences("UserID_STR", "", "String" );
		savePreferences("UserRealName", "", "String" );
		savePreferences("MyIssuesSW", false, "Boolean" );

		pref.setSummary(Activity_Setup.resources.getString(R.string.LoginRegisterRemind));
	}

	/**
	 * Save a value to preferences, either string or boolean
	 * 
	 * @param key       name of the parameters to save
	 * @param value     value of the parameter to save 
	 * @param type      either "String" or "Boolean" 
	 */
	private void savePreferences(String key, Object value, String type){
		SharedPreferences shPrefs = PreferenceManager.getDefaultSharedPreferences(Activity_Setup.ctx);
		SharedPreferences.Editor editor = shPrefs.edit();

		if (type.equals("String")) 
			editor.putString(key, (String) value);
		else 
			editor.putBoolean(key, (Boolean) value);

		editor.commit();
	}
}