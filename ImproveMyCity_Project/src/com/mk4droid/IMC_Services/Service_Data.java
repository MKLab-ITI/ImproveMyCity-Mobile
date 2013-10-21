/** Service_Data */
package com.mk4droid.IMC_Services;

import java.util.ArrayList;
import java.util.Locale;

import android.app.ProgressDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

import com.mk4droid.IMC_Activities.FActivity_TabHost;
import com.mk4droid.IMC_Constructors.Category;
import com.mk4droid.IMC_Constructors.Issue;
import com.mk4droid.IMC_Constructors.VersionDB;
import com.mk4droid.IMC_Store.Constants_API;
import com.mk4droid.IMCity_PackDemo.R;


/**
 * It is a controller to decide when to update local database.
 * 
 * @copyright   Copyright (C) 2012 - 2013 Information Technology Institute ITI-CERTH. All rights reserved.
 * @license     GNU Affero General Public License version 3 or later; see LICENSE.txt
 * @author      Dimitrios Ververidis for the Multimedia Group (http://mklab.iti.gr). 
 *
 */
public class Service_Data extends Service {

	/** Provides information about connectivity to internet */ 
	public static boolean HasInternet    = false;

	/** Access to local database contents */ 
	public static DatabaseHandler dbHandler;

	/** Data of Categories */
	public static ArrayList<Category> mCategL; // List of Issues Categories

	/** Data of Issues */
	public static ArrayList<Issue> mIssueL;    // List of Issues 

	/** Flag to avoid simultaneous update from two operations */
	public static boolean StartedUPD =false;
	
	//------- Internet connection Listener ---
	MyConnectivityListener connListener  = null;
	IntentFilter connIntentFilter        = null;
	boolean connIntentFilterIsRegistered = false;

	//------- Receivers -----
	private BroadcastReceiver mReceiverRefreshData, mReceiverRefreshCategs; 
	IntentFilter intentFilter;

	//------- Database -----------
	VersionDB versionDB, versionDB_Past, versionCategDB_Past;//Hold versions of MySQL  

	Thread updThr;
	private boolean stopThread = false;

	String LangSTR;
	static Resources resources;
	static Context ctx;
	static String UserNameSTR,PasswordSTR;
	static Handler handlerDialog;
	static ProgressDialog progressReceiving;

	//------------------- onBind ----------------------
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	//=======================   onCreate        ================================
	/**
	 *   1. Register internet connectivity listener
	 *   2. Get local db data
	 *   3. Register receiver for refreshing data if any out of 5 refreshing events occurs
	 *   4. Register receiver for refresing visualization if any category filter has changed (no downloading). 
	 */
	@Override
	public void onCreate() {
		resources = setResources();
		ctx       = getApplicationContext();

		//----- Handler for Redrawing Markers from update thread ------------
		handlerDialog = new Handler()
		{
			public void handleMessage(Message msg)
			{
				if (msg.arg1 == 1){
					try {
						progressReceiving = ProgressDialog.show(FActivity_TabHost.ctx, "", "", true);
						progressReceiving.setContentView(R.layout.dialog_transparent_progress);
					} catch (Exception e){
						Log.e("SD PRODIALG", "ERROR", e);
					}
				} else {
					if (progressReceiving!=null && progressReceiving.isShowing())
						progressReceiving.dismiss();
				}

				super.handleMessage(msg);
			}
		};

		//------- Register internet connectivity listener --------
		connIntentFilter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
		connListener = new MyConnectivityListener();

		if (!connIntentFilterIsRegistered) {
			registerReceiver(connListener, connIntentFilter);
			connIntentFilterIsRegistered = true;
		}

		HasInternet = InternetConnCheck.getInstance(this).isOnline(this);

		//----------- GET LOCAL DATA --------------------
		dbHandler = new DatabaseHandler(this);
		versionDB_Past       = dbHandler.getVersion();
		versionCategDB_Past  = dbHandler.getCategVersion();
		mCategL              = dbHandler.getAllCategories();
		mIssueL              = dbHandler.getAllIssues();

		dbHandler.db.close();

		//-------------   Receiver for changes in DB --------------- 
		intentFilter = new IntentFilter("android.intent.action.MAIN");

		mReceiverRefreshData = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {

				if (HasInternet){ 
					String Refresh          = intent.getStringExtra("Refresh");         // 1
					String DistanceChanged  = intent.getStringExtra("DistanceChanged"); // 2
					String IssuesNoChanged  = intent.getStringExtra("IssuesNoChanged"); // 3
					String NewIssueAdded    = intent.getStringExtra("NewIssueAdded");   // 4
					String LocChanged       = intent.getStringExtra("LocChanged");      // 5 

					SharedPreferences mshPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
					int distanceData    = mshPrefs.getInt("distanceData", Constants_API.initRange);
					int IssuesNoAR      = Integer.parseInt(mshPrefs.getString("IssuesNoAR", "40"));

					if (!StartedUPD){
						if (Refresh!=null || DistanceChanged!=null ||  IssuesNoChanged!=null || 
								NewIssueAdded != null || LocChanged!= null){  

							StartedUPD = true;
							
							Log.e("ServData: Refresh DistCh IssuesNoCh NewIssue LocCh ", 
									Refresh + DistanceChanged +  IssuesNoChanged +  NewIssueAdded + LocChanged); //

							boolean isNewIssue = false;
							if (NewIssueAdded != null)
								isNewIssue =true;

							//----- Refresh DB -----------------
							new DBRefreshActions(distanceData, IssuesNoAR, "CalledByReceiver", isNewIssue).execute();
						}
					}
				}
			}
		};

		this.registerReceiver(mReceiverRefreshData, intentFilter);


		//----------- Receiver for category change (no downloading) -------------
		mReceiverRefreshCategs = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {

				String FiltersChanged = intent.getStringExtra("FiltersChanged");         // 1

				if (FiltersChanged!=null){
					mCategL =  dbHandler.getAllCategories();
					sendBroadcast(new Intent("android.intent.action.MAIN").putExtra("DataChanged", "ok"));	
				}
			}
		};

		this.registerReceiver(mReceiverRefreshCategs, intentFilter);
	}

	//=======================  onDestroy         ================================
	/**
	 * On destroy service unregister receivers and stop updating if thread is alive
	 */
	@Override
	public void onDestroy() {

		// --- Unregister Receivers
		if (connIntentFilterIsRegistered) { 		// Unregister Connectivity Listener 
			unregisterReceiver(connListener);
			connIntentFilterIsRegistered = false;
		}

		this.unregisterReceiver(mReceiverRefreshData);    //Downloading
		this.unregisterReceiver(mReceiverRefreshCategs);  //Category filtering

		// ------ stop updating thread if is alive ---------
		stopThread = true;
		try{
			if (updThr.isAlive())
				updThr.interrupt();
		} catch (Exception e){

		}

		dbHandler.db.close();
		stopSelf();
		super.onDestroy();
	}


	//========================= onStart ==================================
	/**    
	 *   Start a thread for periodic check (default is 5 minutes) if the local database has the same 
	 *   version of remote database. If differ than perform an update of local database.
	 */
	@Override
	public void onStart(Intent intent, int startid) {

		updThr = new Thread(new Runnable() { 
			public void run(){

				while(!stopThread && ! Thread.interrupted()){
					if (HasInternet){ 

						try{						
							versionDB = Download_Data.DownloadTimeStamp(ctx, "versionDB CalledonStart");

							//------- Get previous session distance range of data 
							if (versionDB!=null && !StartedUPD ){				

								versionDB_Past       = dbHandler.getVersion();
								if (!versionDB._time.equals(versionDB_Past._time) || versionDB_Past._time==null ){

									StartedUPD = true;
					
									try{
										SharedPreferences mshPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
										int distanceData           = mshPrefs.getInt("distanceData" , Constants_API.initRange);
										int IssuesNoAR            = Integer.parseInt(mshPrefs.getString("IssuesNoAR"   , "40"));

										new DBRefreshActions(distanceData, IssuesNoAR, "CalledByStarter", false).execute();
									} catch (Exception e){
										StartedUPD = false;
									}
								} else { // No change it is already updated
									sendBroadcast(new Intent("android.intent.action.MAIN").putExtra("ProgressBar", "Gone"));
								}
							} 

						} catch (NullPointerException e){
							Log.e(Constants_API.TAG, "Service_Data:Failed to periodically syncronize because app was closed"
									+e.getMessage());
						} catch (Exception e){
							Log.e(Constants_API.TAG, "Service_Data:Failed to periodically syncronize because of unkown event"
									+e.getMessage());
						}

					} else { // end has internet
						sendBroadcast(new Intent("android.intent.action.MAIN").putExtra("ProgressBar", "Gone"));
					}



					try{
						Thread.sleep(FActivity_TabHost.RefrateAR * 60 * 1000); 
					} catch (InterruptedException e) {
						Log.e(Constants_API.TAG, "Service_Data:Thread was unable to sleep:" + e.getMessage() );
						stopThread = true;
					}
					
				} // stop thread
			} // end run
		} // end runnable
				); // end thread 
		updThr.start();
	}

	//=======================  DBRefreshActions         ================================
	/**
	 *   Asynchronous refresh local database.  
	 *   
	 *   1. Download category timestamp and perform update of categories
	 *   2. Update local table issues
	 *   3. Broadcast that data has changed
	 * 
	 * @param distanceData    range to download issues
	 * @param IssuesNoAR      no of issues to download
	 * @param NewVersionDB    the version of the downloaded data DB
	 */
	private static class DBRefreshActions extends AsyncTask<String, String , String>{

		private int distanceData, IssuesNoAR;
		private VersionDB versionDB = null;
		private VersionDB versionCategDB_Past = null;
		private int DEBUG_FLAG;
		private String CalledBy;
		private boolean isNewIssue;

		public DBRefreshActions(int distanceData_in, int IssuesNoAR_in, String CalledBy_in, boolean isNewIssue_in) {
			distanceData = distanceData_in;
			IssuesNoAR   = IssuesNoAR_in;
			CalledBy = CalledBy_in;
			isNewIssue = isNewIssue_in;
		}

		/* (non-Javadoc)
		 * @see android.os.AsyncTask#onPreExecute()
		 */
		@Override
		protected void onPreExecute() {
			Message msg = new Message();
			msg.arg1 = 1;
			handlerDialog.sendMessage(msg);

			ctx.sendBroadcast(new Intent("android.intent.action.MAIN").putExtra("ProgressBar", "Visible"));

			StartedUPD  = true;
			versionCategDB_Past    = dbHandler.getCategVersion();
			DEBUG_FLAG = 0;

			super.onPreExecute();
		}

		@Override
		protected String doInBackground(String... params) {

			try {
				versionDB = Download_Data.DownloadTimeStamp(ctx, CalledBy);

				if (versionDB==null){
					return "";
				}

				DEBUG_FLAG = 1;

				//--------- Check to update categories in sqlitedb -------
				VersionDB versionCategDB_Down = Download_Data.DownloadCategTimeStamp();

				DEBUG_FLAG = 11;

				int kbCateg=0;
				if (versionCategDB_Past._time == null || !versionCategDB_Down._time.equals(versionCategDB_Past._time) ||	versionCategDB_Down._id==0 ){

					kbCateg = dbHandler.addUpdCateg(ctx);
					DEBUG_FLAG = 12;
					dbHandler.AddUpdCategVersion(versionCategDB_Down);
					versionCategDB_Past = versionCategDB_Down;
				}

				DEBUG_FLAG = 2;
				//---------Update Issues ---------------------------------
				// download and add to SQLite
				int kbIssues = dbHandler.addUpdIssues(Service_Location.locUser.getLongitude(), 
						Service_Location.locUser.getLatitude(),
						distanceData, IssuesNoAR,ctx);



				if (kbIssues==0){
					Toast.makeText(ctx, "Connection error", Toast.LENGTH_LONG).show();
					return ""; 	
				}
				DEBUG_FLAG = 3;

				//---------- Update Votes -----------------
				int kbVotes = dbHandler.AddUpdUserVotes(UserNameSTR, PasswordSTR, ctx);

				DEBUG_FLAG = 31;

				// Retrieve from SQLite
				mCategL =  dbHandler.getAllCategories();

				DEBUG_FLAG = 32;

				mIssueL =  dbHandler.getAllIssues();

				DEBUG_FLAG = 33;

				if (mIssueL.size()>0){
					dbHandler.AddUpdVersion(versionDB);
				}

				DEBUG_FLAG = 4;

				//---------
				dbHandler.db.close();


				int KB_down = kbIssues + kbVotes + kbCateg;

				DEBUG_FLAG = 5;

				//-----  Broadcast Data has changed ---------------
				String mes_touser =  resources.getString(R.string.Downloaded) + ": " + ( (int) (KB_down/1000) ) + " kB";

				DEBUG_FLAG = 6;

				ctx.sendBroadcast(new Intent("android.intent.action.MAIN").putExtra("DataChanged", "ok").putExtra("mes_touser", mes_touser));
				Log.e("SD DBRefreshActions", "DO OK");

				return mes_touser;
			} catch (Exception e){
				Log.e(Constants_API.TAG,"Service_DATA: DBRefreshActions: Unable to perform all actions: DEBUG_FLAG:" + DEBUG_FLAG);

				String mess = "";

				switch (DEBUG_FLAG) {
				case 1:
					mess = "Unable to download categories version time stamp";
					break;
				case 11:	
					mess = "Unable to update categories table";
					break;
				case 12:	
					mess = "Unable to update categories version table";
				case 2:
					mess = "Unable to update issues table";
					break;
				case 3:
					mess = "Unable to update user votes table";
					break;
				case 31:
					mess = "Unable to get content of categories table";
					break;
				case 32:
					mess = "Unable to get information from issues table";
					break;	
				case 33:
					mess = "Unable to update issues version table";
					break;	
				case 4:
					mess = "Unable to close local database";
					break;
				}

				Log.e("SD FAIL Reason:", mess);
				
				Toast.makeText(ctx, resources.getString(R.string.FailMes), Toast.LENGTH_LONG).show();

				return "";
			}
		}

		@Override
		protected void onPostExecute(String kbSTR) {


			Message msg = new Message();
			msg.arg1 = 2;
			handlerDialog.sendMessage(msg);


			dbHandler.db.close();

			if (kbSTR.length()> 0){
				//-----  Broadcast Data has changed to close all dialogues ---------------
				ctx.sendBroadcast(new Intent("android.intent.action.MAIN").putExtra("DataChanged", "ok"));


				//-------- Save Prefs ----------
				SharedPreferences shPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
				SharedPreferences.Editor editor = shPrefs.edit();
				editor.putString("IssuesNoAROLD", Integer.valueOf(IssuesNoAR).toString());
				editor.putInt("distanceDataOLD", distanceData);
				editor.commit();
				//-----------------------------
			}

			StartedUPD = false;

			Log.e("SD DBRefreshActions", "POST OK");

			if (isNewIssue)
				FActivity_TabHost.mTabHost.setCurrentTab(0);

			super.onPostExecute(kbSTR);

		}
	}
	//---------------------- End of Async ------------------ 


	//=======================  MyConnectivityListener         ================================
	/**
	 * Receiver for any change in internet connectivity
	 */
	protected class MyConnectivityListener extends BroadcastReceiver {
		Context ctx;

		@Override
		public void onReceive(Context context, Intent intent) {
			ctx = context;

			HasInternet = !intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);

			//-------- Caused by ------------
			//String reason = intent.getStringExtra(ConnectivityManager.EXTRA_REASON);
			//boolean isFailover = intent.getBooleanExtra(ConnectivityManager.EXTRA_IS_FAILOVER, false);
			//NetworkInfo currentNetworkInfo = (NetworkInfo) intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
			//NetworkInfo otherNetworkInfo = (NetworkInfo) intent.getParcelableExtra(ConnectivityManager.EXTRA_OTHER_NETWORK_INFO);
			// do application-specific task(s) based on the current network state, such
			// as enabling queuing of HTTP requests when currentNetworkInfo is connected etc.
			//ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			//NetworkInfo info = cm.getActiveNetworkInfo();
		}
	}; 



	//=======================   setResources        ================================
	/**
	 *  Set language Resources depending on the language saved in the preferences   
	 * @return resources depending on the language chosen
	 */
	public Resources setResources(){
		SharedPreferences mshPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		LangSTR          = mshPrefs.getString("LanguageAR", Constants_API.DefaultLanguage);
		UserNameSTR      = mshPrefs.getString("UserNameAR", "");
		PasswordSTR      = mshPrefs.getString("PasswordAR", "");

		Configuration conf = getResources().getConfiguration();
		conf.locale = new Locale(LangSTR.substring(0, 2)); //----- Convert Greek -> el ---------
		DisplayMetrics metrics = new DisplayMetrics();
		return new Resources(getAssets(), metrics, conf);
	}

}//------ End of Serv DATA