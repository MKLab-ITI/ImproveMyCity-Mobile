// Fragment_List
package com.mk4droid.IMC_Activities;

import java.util.ArrayList;
import java.util.Locale;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.mk4droid.IMC_Constructors.Issue;
import com.mk4droid.IMC_Constructors.IssueListItem;
import com.mk4droid.IMC_Core.Issues_ListAdapter;
import com.mk4droid.IMC_Services.InternetConnCheck;
import com.mk4droid.IMC_Services.Service_Data;
import com.mk4droid.IMC_Store.Constants_API;
import com.mk4droid.IMCity_PackDemo.R;

/**
 * Show a list containing all issues (Filtered)
 * 
 * @copyright   Copyright (C) 2012 - 2013 Information Technology Institute ITI-CERTH. All rights reserved.
 * @license     GNU Affero General Public License version 3 or later; see LICENSE.txt
 * @author      Dimitrios Ververidis for the Multimedia Group (http://mklab.iti.gr). 
 * 
 */
public class Fragment_List extends Fragment {
    
	/** Resources of current fragment */
	public static Resources resources;
	
	/** Issues listview object */
	public static ListView lvIssues;
    
	/** The fragment view of the list */ 
	public static View vFragment_List;
	
	/** The list fragment */
	public static Fragment mFragment_List;
	
	/** The issue details fragment to call class */
	public static Fragment_Issue_Details newFrag_Issue_Details;

	
	//-- Private ------------
	static Context ctx;
	ListView lv;
	String LangSTR;
	String UserID_STR = "";
	Button btListMyIss, btListAllIss;
	int UserID = -1;
	static boolean MyIssuesSW;
	boolean ClosedSW = true, OpenSW = true, AckSW  = true;
	ArrayList<IssueListItem> list_data;
	Issues_ListAdapter adapterIssues;
	SharedPreferences mshPrefs;
	static LinearLayout ll_listissues_MyIssues;
	Handler handlerBroadcastRefresh; 	// Handler for refreshing data 

	
	BroadcastReceiver mReceiverDataChanged;
	boolean isReg_mReceiverDataChanged = false;
	Handler handlerBroadcastListRefresh;
	IntentFilter intentFilter;


	//================ onCreate ======================
	/**    Create the list of issues  */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mshPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		resources = setResources();
	}


	/** on destroy this fragment view */
	@Override
	public void onDestroyView() {

		if (Service_Data.dbHandler.db.isOpen())
			Service_Data.dbHandler.db.close();
	
		if (isReg_mReceiverDataChanged){
			ctx.unregisterReceiver(mReceiverDataChanged);
			isReg_mReceiverDataChanged = false;
		}
		super.onDestroyView();
	}


	/** on create this fragment view */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		FActivity_TabHost.IndexGroup = 1;

		if (vFragment_List != null) {
			ViewGroup parent = (ViewGroup) vFragment_List.getParent();
			if (parent != null)
				parent.removeView(vFragment_List);
		}

		try {
			if (vFragment_List == null) 
				vFragment_List = inflater.inflate(R.layout.fragment_list, container, false);
		} catch (InflateException e) {
			/* map is already there, just return view as it is */
		}

		mFragment_List = this;
		ctx = mFragment_List.getActivity(); 

		lvIssues    = (ListView)vFragment_List.findViewById(R.id.lvIssues);

		//--------------- Receiver for Data change ------------
		HandlersAndReceivers();

		resources = setResources();
		handlerBroadcastListRefresh.sendMessage(new Message());

		//---------- Buttons -----------
		ll_listissues_MyIssues = (LinearLayout) vFragment_List.findViewById(R.id.ll_listissues_MyIssues);

		btListMyIss  = (Button)  vFragment_List.findViewById(R.id.bt_listissues_MyIssues);
		btListAllIss = (Button)  vFragment_List.findViewById(R.id.bt_listissues_AllIssues);

		// onclick listeners
		btListMyIss.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SetComplementaryButtons(); 
			}
		});

		btListAllIss.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SetComplementaryButtons();
			}
		});

		//-----------------------
		if (Fragment_Issue_Details.mfrag_issue_details!=null)
			if ( Fragment_Issue_Details.mfrag_issue_details.isVisible()  )
				getChildFragmentManager().beginTransaction().hide(mFragment_List).commit();

		return vFragment_List;
	}



	/* ==== Set button colors =========== */
	private void SetComplementaryButtons(){

		if (MyIssuesSW)
			MyIssuesSW = false;
		else 
			MyIssuesSW = true;

		handlerBroadcastListRefresh.sendMessage(new Message());

		savePreferences("MyIssuesSW", MyIssuesSW, "boolean");

		if (MyIssuesSW){
			// _-1
			btListMyIss.setCompoundDrawablesWithIntrinsicBounds( resources.getDrawable(R.drawable.my_issues_icon_white32) 
					, null, null, null);
			btListMyIss.setBackgroundDrawable(resources.getDrawable(R.drawable.bt_custom_click_orange));
			btListMyIss.setTextColor(Color.WHITE);
			btListMyIss.setClickable(false);

			// 0-_
			btListAllIss.setCompoundDrawablesWithIntrinsicBounds( resources.getDrawable(R.drawable.all_issues_icon_gray32)
					, null, null, null);
			btListAllIss.setBackgroundDrawable( resources.getDrawable(android.R.drawable.btn_default));
			btListAllIss.setTextColor(Color.BLACK);
			btListAllIss.setClickable(true);

		}else{ 
			// _-0
			btListMyIss.setCompoundDrawablesWithIntrinsicBounds( resources.getDrawable(R.drawable.my_issues_icon_gray32)
					, null, null, null);
			btListMyIss.setBackgroundDrawable( resources.getDrawable(android.R.drawable.btn_default));
			btListMyIss.setTextColor(Color.BLACK);
			btListMyIss.setClickable(true);

			// 1-_
			btListAllIss.setCompoundDrawablesWithIntrinsicBounds( resources.getDrawable(R.drawable.all_issues_icon_white32) 
					, null, null, null);
			btListAllIss.setBackgroundDrawable(resources.getDrawable(R.drawable.bt_custom_click_orange));
			btListAllIss.setTextColor(Color.WHITE);
			btListAllIss.setClickable(false);
		}

	}

	/* 
	 * =========  List initialization  ========== 
	 * 
	 * */
	private void InitList(){

		//== Initialization of List == 
		list_data = new ArrayList<IssueListItem>();

		// Add each issue to the list  --------
		int NIssues = 0;

		if (Service_Data.mIssueL!=null)
			NIssues = Service_Data.mIssueL.size();


		for (int i= 0; i< NIssues; i++){
			for (int j=0; j< Service_Data.mCategL.size(); j++){
				//------ iterate to find category of issue and icon to display
				if ( (Service_Data.mIssueL.get(i)._currentstatus == 1 && OpenSW) || 
						(Service_Data.mIssueL.get(i)._currentstatus == 2 && AckSW) || 
						(Service_Data.mIssueL.get(i)._currentstatus == 3 && ClosedSW)){

					if (Service_Data.mIssueL.get(i)._catid == Service_Data.mCategL.get(j)._id ){
						if (Service_Data.mCategL.get(j)._visible==1){  // Filters for Visibility

							if (MyIssuesSW && Service_Data.mIssueL.get(i)._userid != UserID)
								continue;

							Issue mIssue = Service_Data.mIssueL.get(i);

							list_data.add(new IssueListItem(	   null,  			 // bm is null
									mIssue._id,  
									mIssue._title,
									mIssue._currentstatus,
									mIssue._address,
									mIssue._reported,
									mIssue._votes,
									mIssue._latitude,
									mIssue._longitude,
									mIssue._urlphoto,
									mIssue));
						}}}}}



		//--- Set Adapter ------------
		resources = setResources();
		adapterIssues = new Issues_ListAdapter(getActivity(), R.layout.listissues_item, list_data);
		lvIssues.setAdapter(adapterIssues);

		//----- Set on Click Listener -------
		lvIssues.setOnItemClickListener(
				new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, android.view.View view,
							int position, long id) {
					    
						    newFrag_Issue_Details = new Fragment_Issue_Details();
						    
							Bundle args = new Bundle();
							args.putInt("issueId", Integer.parseInt( list_data.get(position)._id.substring(1) ));
							newFrag_Issue_Details.setArguments(args);

							// Add the fragment to the activity, pushing this transaction
							// on to the back stack.
							FragmentTransaction ft = getFragmentManager().beginTransaction();
							ft.replace(R.id.fl_IssuesList_container, newFrag_Issue_Details,"FTAG_ISSUE_DETAILS_LIST");
							ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
							ft.addToBackStack(null);
							ft.commit();
					}}
				);

	}

	/** on Resume this fragment  */
	@Override
	public void onResume() {

		resources = setResources();

		//set initial state
		if (MyIssuesSW){
			btListMyIss.setCompoundDrawablesWithIntrinsicBounds( resources.getDrawable(R.drawable.my_issues_icon_white32) 
					, null, null, null);
			btListMyIss.setBackgroundDrawable(resources.getDrawable(R.drawable.bt_custom_click_orange));
			btListMyIss.setTextColor(Color.WHITE);
			btListMyIss.setClickable(false);
			
			btListAllIss.setCompoundDrawablesWithIntrinsicBounds( resources.getDrawable(R.drawable.all_issues_icon_gray32) 
					, null, null, null);
			btListAllIss.setBackgroundDrawable(resources.getDrawable(android.R.drawable.btn_default));
			btListAllIss.setTextColor(Color.BLACK);
			btListAllIss.setClickable(true);
			
		} else {
			btListAllIss.setCompoundDrawablesWithIntrinsicBounds( resources.getDrawable(R.drawable.all_issues_icon_white32) 
					, null, null, null);
			btListAllIss.setBackgroundDrawable(resources.getDrawable(R.drawable.bt_custom_click_orange));
			btListAllIss.setTextColor(Color.WHITE);
			btListAllIss.setClickable(false);
			
			btListMyIss.setCompoundDrawablesWithIntrinsicBounds( resources.getDrawable(R.drawable.my_issues_icon_gray32) 
					, null, null, null);
			btListMyIss.setBackgroundDrawable(resources.getDrawable(android.R.drawable.btn_default));
			btListMyIss.setTextColor(Color.BLACK);
			btListMyIss.setClickable(true);
		}

		if (UserID_STR.length() == 0){
			ll_listissues_MyIssues.setVisibility(View.GONE);
		} else {
			ll_listissues_MyIssues.setVisibility(View.VISIBLE);
		}


		if (InternetConnCheck.getInstance(ctx).isOnline(ctx)){

			//-------------- Check if distance has changed -------------
			int distanceData    = mshPrefs.getInt("distanceData", Constants_API.initRange);
			int distanceDataOLD = mshPrefs.getInt("distanceDataOLD", Constants_API.initRange);


			//---------- Check if IssuesNo has changed ---------
			int IssuesNoAR    =  Integer.parseInt(mshPrefs.getString("IssuesNoAR", "40"));
			int IssuesNoAROLD =  Integer.parseInt(mshPrefs.getString("IssuesNoAROLD", "40"));

			if (distanceData!=distanceDataOLD){
				Message msg = new Message();
				msg.arg1 = 2;
				handlerBroadcastRefresh.sendMessage(msg);
			} else if (IssuesNoAR!=IssuesNoAROLD){
				Message msg = new Message();
				msg.arg1 = 3;
				handlerBroadcastRefresh.sendMessage(msg);
			}
		}

		super.onResume();
	}

	//=========== setResources =================
	private Resources setResources(){

		LangSTR          = mshPrefs.getString("LanguageAR", Constants_API.DefaultLanguage);
		OpenSW           = mshPrefs.getBoolean("OpenSW", true);
		AckSW            = mshPrefs.getBoolean("AckSW", true);
		ClosedSW         = mshPrefs.getBoolean("ClosedSW", true);
		UserID_STR       = mshPrefs.getString("UserID_STR", "");
		MyIssuesSW       = mshPrefs.getBoolean("MyIssuesSW", false);
				
		if (UserID_STR.length()>0) 
			UserID = Integer.parseInt(UserID_STR);
		else 
			UserID = -1;

		Configuration conf = getResources().getConfiguration();
		conf.locale = new Locale(LangSTR.substring(0, 2)); //----- Convert Greek -> el ---------
		DisplayMetrics metrics = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
		return new Resources(getActivity().getAssets(), metrics, conf);
	}


	/* ========================== savePreferences =================================
	 * @param key    the name of the preference
	 * @param value  the value of the preference
	 * @param type either "Boolean" or "String"
	 */
	private void savePreferences(String key, Object value, String type){
		SharedPreferences shPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		SharedPreferences.Editor editor = shPrefs.edit();
		if (type.equals("String")) 
			editor.putString(key, (String) value);
		else 
			editor.putBoolean(key, (Boolean) value);
		editor.commit();
	}

	/*    ===========================
	         Handlers and Receivers 
	      ============================  */
	private void HandlersAndReceivers(){

		handlerBroadcastListRefresh = new Handler() // Broadcast  2. DistanceCh 3. IssuesNoCh
		{
			public void handleMessage(Message msg) {   
				InitList();
				super.handleMessage(msg);
			}
		};

		// --------- Broadcast that refresh is needed
		handlerBroadcastRefresh = new Handler() // Broadcast  2. DistanceCh 3. IssuesNoCh
		{
			public void handleMessage(Message msg)
			{
				if (msg.arg1 == 2)
					ctx.sendBroadcast(new Intent("android.intent.action.MAIN").putExtra("DistanceChanged", "Indeed"));
				else if (msg.arg1 == 3)
					ctx.sendBroadcast(new Intent("android.intent.action.MAIN").putExtra("IssuesNoChanged", "yep"));

				super.handleMessage(msg);
			}
		};

		//---------- Upon Data Changed start updating the UI
		intentFilter = new IntentFilter("android.intent.action.MAIN"); // DataCh

		mReceiverDataChanged = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {

				String DataChanged = intent.getStringExtra("DataChanged");

				if (DataChanged!=null){
					handlerBroadcastListRefresh.sendMessage(new Message());
				} 
			}
		};

		if (!isReg_mReceiverDataChanged){
			getActivity().registerReceiver(mReceiverDataChanged, intentFilter);
			isReg_mReceiverDataChanged = true;
		}
	}

}
