/**
 *         Activity_TabHost
 */

package com.mk4droid.IMC_Activities;

import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import com.mk4droid.IMC_Services.Service_Data;
import com.mk4droid.IMC_Store.Constants_API;
import com.mk4droid.IMC_Utils.My_System_Utils;
import com.mk4droid.IMCity_PackDemo.R;

/**
 * Tabs are hosted in this FragmentActivity. (1=Main, 2=List, 3=New, 4=Filters,
 * 5=Setup)
 * 
 * @copyright Copyright (C) 2012 - 2013 Information Technology Institute
 *            ITI-CERTH. All rights reserved.
 * @license GNU Affero General Public License version 3 or later; see
 *          LICENSE.txt
 * @author Dimitrios Ververidis for the Multimedia Group (http://mklab.iti.gr).
 * 
 */
public class FActivity_TabHost extends FragmentActivity implements OnTabChangeListener {

	/** The object that hosts all tabs */
	public static FragmentTabHost mTabHost;

	/**
	 * This context is related to the whole application. It is useful for
	 * presenting messages with Toast from everywhere in the application
	 * (READ-only).
	 */
	public static Context ctx;

	/**
	 * These resources are related mainly to the language of the GUI and they
	 * can be retrieved from the whole application for presenting localized
	 * messages (READ-only)
	 */
	public static Resources resources;

	/**
	 * Refresh rate in minutes for updating data (DEFAULT:5, Here READ-ONLY, can
	 * be modified by Activity_Setup)
	 */
	public static int RefrateAR = 5;

	/** Current active tab (1=Main, 2=List, 3=New, 4=Filters, 5=Setup) */
	public static int IndexGroup = 0;

	/** Button to overlay above Setup tab because PreferenceActivities are not supported as a fragment in Android 2.3 */
	public static Button btSetup;

	Configuration conf;
	String LangSTR;
	public static DisplayMetrics metrics;

	int NTabs = 5;
	int prevTab = 0; // previous tab

	TabSpec[] mTabSpec = new TabSpec[NTabs]; // Each Tab
	Drawable mD_Main, mD_Report, mD_Setup, mD_Filters, mD_List;



	/*
	 *  on Resume Fragments
	 */
	@Override
	protected void onResumeFragments() {
		resources = SetResources();
		super.onResumeFragments();
	}


	/**
	 *    Helper for overlaying patch button setup over tab button
	 */
	@Override
	public void onWindowFocusChanged (boolean hasFocus) {
		int[] a = new int[2];
		btSetup.getLocationOnScreen(a);
		btSetup.setWidth(mTabHost.getTabWidget().getChildAt(4).getWidth());
		btSetup.setHeight(mTabHost.getTabWidget().getChildAt(4).getHeight());

		if (a[0]==0){
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)btSetup.getLayoutParams();
			params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			params.setMargins(1, 0, 0, 1);
			btSetup.setLayoutParams(params);
		}

		btSetup.bringToFront();		
		btSetup.invalidate();
	}

	/** On screen orientation change 
	 */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {

		super.onConfigurationChanged(newConfig);

		onWindowFocusChanged(true);

		if(newConfig.orientation==Configuration.ORIENTATION_LANDSCAPE){
			Log.e("On Config Change","LANDSCAPE");
		}else{
			Log.e("On Config Change","PORTRAIT");
		}
	}

	// ------------------- on CREATE --------------------
	/**
	 * Executed when tabhost is created
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// ----------------- GUI ------------
		resources = SetResources(); // ---Load Prefs and Modify resources
		// accordingly
		setContentView(R.layout.factivity_tabhost); // ---------- Content view
		ctx = this;

		btSetup = new Button(ctx);

		LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		btSetup.setLayoutParams(params );

		RelativeLayout rl = (RelativeLayout)findViewById(R.id.tbs);
		rl.addView(btSetup);
		btSetup.bringToFront();


		btSetup.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				if (Fragment_Issue_Details.mfrag_issue_details != null) {
					FragmentTransaction ft = getSupportFragmentManager()
							.beginTransaction();
					ft.remove(Fragment_Issue_Details.mfrag_issue_details);
					ft.commit();
					getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
				}

				mTabHost.getTabWidget().getChildAt(4).findViewWithTag("hbar").
				setBackgroundDrawable(resources.getDrawable(R.drawable.gradient_orange));
				startActivity(new Intent(ctx, Activity_Setup.class));
			}
		});

		mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost); 

		mTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);

		// ---------------------------------------
		for (int i = 0; i < NTabs; i++)
			mTabSpec[i] = mTabHost.newTabSpec("tid" + Integer.toString(i));

		mD_Main   = getResources().getDrawable(R.drawable.ic_map);
		mD_List   = getResources().getDrawable(R.drawable.ic_list);
		mD_Report = getResources().getDrawable(R.drawable.ic_plus);
		mD_Filters= getResources().getDrawable(R.drawable.ic_filter);
		mD_Setup  = getResources().getDrawable(R.drawable.ic_settings);

		// -------------- Set icons and texts localized per tab -------------
		LinearLayout llA = make_Active_Tab(  resources.getString(R.string.Map), mD_Main);
		llA.setClickable(true);
		LinearLayout llB = make_Inactive_Tab(resources.getString(R.string.List), mD_List);
		LinearLayout llC = make_Inactive_Tab(resources.getString(R.string.Report), mD_Report);
		LinearLayout llD = make_Inactive_Tab(resources.getString(R.string.Filter), mD_Filters);
		LinearLayout llE = make_Inactive_Tab(resources.getString(R.string.Settings), mD_Setup);


		mTabSpec[0].setIndicator(llA);
		mTabSpec[1].setIndicator(llB);
		mTabSpec[2].setIndicator(llC);
		mTabSpec[3].setIndicator(llD);
		mTabSpec[4].setIndicator(llE);

		// Add tabSpec to the TabHost to display
		mTabHost.addTab(mTabSpec[0], Fragment_Map.class, null);
		mTabHost.addTab(mTabSpec[1], Fragment_List.class, null);
		mTabHost.addTab(mTabSpec[2], Fragment_NewIssueA.class, null);
		mTabHost.addTab(mTabSpec[3], Fragment_Filters.class, null);
		mTabHost.addTab(mTabSpec[4], null, null); // implemented with a button
		// overlapped because there
		// was no fragment for
		// Preferences in support
		// lib v4

		mTabHost.setOnTabChangedListener(this);

		mTabHost.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				btSetup.setWidth(mTabHost.getTabWidget().getChildAt(4).getWidth());
				btSetup.setHeight(mTabHost.getTabWidget().getChildAt(4).getHeight());
				btSetup.bringToFront();
			}
		});

	}// ---- End OnCreate ----


	@Override
	protected void onResume() {

		mTabHost.getTabWidget().getChildAt(4).setBackgroundDrawable(resources.getDrawable(R.drawable.gradient_tabs));

		View setup_hbar =mTabHost.getTabWidget().getChildAt(4).findViewWithTag("hbar"); 

		setup_hbar.setBackgroundDrawable(null);
		setup_hbar.setBackgroundColor(resources.getColor(R.color.graylight));

		btSetup.setBackgroundDrawable(null);

		btSetup.bringToFront();
		super.onResume();
		btSetup.bringToFront();

		//--------- Vanish zoom view of issue ------------
		if (Fragment_Issue_Details.dialogZoomIm!=null )
			if (Fragment_Issue_Details.dialogZoomIm.isShowing())
				Fragment_Issue_Details.dialogZoomIm.dismiss();
	}


	// ============ on Destroy Application ===================
	/**
	 * Close database
	 */
	@Override
	protected void onDestroy() {
		if (Service_Data.dbHandler.db.isOpen())
			Service_Data.dbHandler.db.close();

		if (My_System_Utils.isServiceRunning(
				"com.mk4droid.IMC_Services.Service_Data", ctx))
			stopService(new Intent(this, Service_Data.class));

		super.onDestroy();
	}


	// ========== Menu hard button ============
	/**
	 * Menu hard button (only to exit)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	// =========== Menu hard button pressed option =====
	/**
	 * Only exit option for the time being
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.exit:
			finish();
			break;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onSaveInstanceState(android.os.Bundle)
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {

		if (FActivity_TabHost.IndexGroup==0 )
			if ( Fragment_Issue_Details.mfrag_issue_details != null) {
				FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
				ft.remove(Fragment_Issue_Details.mfrag_issue_details);
				ft.commit();
				Fragment_Issue_Details.mfrag_issue_details = null;
				getSupportFragmentManager().popBackStack();
			}


		super.onSaveInstanceState(outState);
	}

	// ================== OnTabChange =========================
	/**
	 * When changing tab manage the other tabs and change tab button colors
	 */
	@Override
	public void onTabChanged(String arg0) {

		// if setup is pressed return as it is
		if (mTabHost.getCurrentTab()==4)
			return;

		// remove some irrelevant fragments from previous tabs
		if (prevTab == 1 || prevTab == 0 ){
			if (Fragment_Comments.mfrag_comments != null) {
				FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
				ft.remove(Fragment_Comments.mfrag_comments);
				ft.commit();
				Fragment_Comments.mfrag_comments = null;
				getSupportFragmentManager().popBackStack();
			}

			if ( Fragment_Issue_Details.mfrag_issue_details != null) {
				FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
				ft.remove(Fragment_Issue_Details.mfrag_issue_details);
				ft.commit();
				Fragment_Issue_Details.mfrag_issue_details = null;
				getSupportFragmentManager().popBackStack();
			}
		}




		// Stack of tab new Issue: Two maps v2 are not allowed, so remove the
		// newIssueMap
		if (prevTab == 2) {
			if (Fragment_NewIssueB.mfrag_nIssueB != null) {
				FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
				ft.remove(Fragment_NewIssueB.mfrag_nIssueB);
				ft.commit();
				Fragment_NewIssueB.mfrag_nIssueB = null;
				getSupportFragmentManager().popBackStack();
			}
			Fragment_NewIssueA.llnewissue_a.setVisibility(View.VISIBLE);
		}

		// --------- Set Color of Tabs ---------------
		for (int i = 0; i < NTabs; i++) {
			LinearLayout ll = (LinearLayout) mTabHost.getTabWidget().getChildAt(i);
			TextView tv = (TextView) ll.findViewWithTag("tv");
			String txt = tv.getText().toString();
			Drawable[] dr = tv.getCompoundDrawables();

			if (mTabHost.getCurrentTab() == i) {
				ll = ActivateColorize(ll, txt, dr[1]);
				FActivity_TabHost.IndexGroup = i;
				prevTab = i;
			} else
				ll = InActivateColorize(ll, txt, dr[1]);
		}
	}

	// ------------------ Colorize Tabs ----------------------------
	private LinearLayout make_Active_Tab(String text, Drawable dr) {

		LinearLayout ll = new LinearLayout(this);
		ll.setPadding(0, 0, 2, 1);
		ll.setBackgroundColor(Color.GRAY);

		ll.setTag("ll");
		ll.setOrientation(LinearLayout.VERTICAL);
		ll.setLayoutParams(new LinearLayout.LayoutParams(
				0,
				android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,1));

		//------ Text 
		TextView tv = new TextView(this);
		tv.setBackgroundColor(Color.TRANSPARENT);
		tv.setTag("tv");
		ll.addView(tv);

		// ------ hbar
		View hbar = new View(this);
		hbar.setTag("hbar");
		hbar.setLayoutParams(new LinearLayout.LayoutParams(android.widget.LinearLayout.LayoutParams.FILL_PARENT,10));

		ll.addView(hbar);

		////////////////////////////////////////
		return ActivateColorize(ll, text, dr);
	}

	private LinearLayout make_Inactive_Tab(String text, Drawable dr) {

		LinearLayout ll = new LinearLayout(this);

		ll.setPadding(0, 0, 2, 1);

		ll.setBackgroundColor(Color.GRAY);

		ll.setTag("ll");
		ll.setOrientation(LinearLayout.VERTICAL);		
		ll.setLayoutParams(new LinearLayout.LayoutParams(
				0,android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,1));



		//------ Text 
		TextView tv = new TextView(this);
		tv.setBackgroundColor(Color.TRANSPARENT);
		tv.setTag("tv");
		ll.addView(tv);

		// ------ hbar
		View hbar = new View(this);
		hbar.setTag("hbar");
		hbar.setLayoutParams(new LinearLayout.LayoutParams(android.widget.LinearLayout.LayoutParams.FILL_PARENT,10));

		ll.addView(hbar);

		/////////////////////////////////////
		return InActivateColorize(ll, text, dr);
	}




	/*
	 *    Colorize active button
	 */
	private LinearLayout ActivateColorize(LinearLayout ll, String text, Drawable dr) {

		// text
		TextView v = (TextView) ll.findViewWithTag("tv");
		v.setText(text);
		v.setTextSize(10);
		v.setTextColor(Color.BLACK); //v.setTextColor(Color.WHITE);
		v.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);

		//dr.setColorFilter(0xFF888888, android.graphics.PorterDuff.Mode.MULTIPLY);
		dr.setColorFilter(resources.getColor(R.color.orange), android.graphics.PorterDuff.Mode.SRC_ATOP);

		v.setCompoundDrawablesWithIntrinsicBounds(null, dr, null, null);
		v.setPadding(0, 5, 0, 2);

		v.setBackgroundDrawable(resources.getDrawable(R.drawable.gradient_tabs_focused));

		// hbar
		View hbar = ll.findViewWithTag("hbar");
		//hbar.setBackgroundColor(resources.getColor(R.color.orange));
		hbar.setBackgroundDrawable(resources.getDrawable(R.drawable.gradient_orange));

		return ll;
	}



	private LinearLayout InActivateColorize(LinearLayout ll, String text, Drawable dr) {

		// text
		TextView v = (TextView) ll.findViewWithTag("tv");

		v.setText(text);
		v.setTextSize(10);
		v.setTextColor(Color.GRAY);
		v.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);

		dr.setColorFilter(0xFF888888, android.graphics.PorterDuff.Mode.SRC_ATOP);
		v.setCompoundDrawablesWithIntrinsicBounds(null, dr, null, null);
		v.setPadding(0, 5, 0, 2);

		v.setBackgroundDrawable(resources.getDrawable(R.drawable.gradient_tabs));

		// hbar
		View hbar = ll.findViewWithTag("hbar");
		hbar.setBackgroundDrawable(null);
		hbar.setBackgroundColor(resources.getColor(R.color.graylight));
		return ll;
	}

	// ================= Set Resources =============
	/*
	 * Retrieve Language, Username, Password, and AuthenticationFlag, Refresh
	 * rate as it was stored in preferences
	 */
	private Resources SetResources() {
		SharedPreferences mshPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

		My_System_Utils.CheckPrefs(mshPrefs);

		LangSTR   = mshPrefs.getString("LanguageAR", Constants_API.DefaultLanguage);
		RefrateAR = Integer.parseInt(mshPrefs.getString("RefrateAR", "5"));

		conf = getResources().getConfiguration();
		conf.locale = new Locale(LangSTR.substring(0, 2)); // ----- Convert
		// Greek -> el
		// ---------
		metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		return new Resources(getAssets(), metrics, conf);
	}
}