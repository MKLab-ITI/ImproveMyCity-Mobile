// Fragment_Main 
package com.mk4droid.IMC_Activities;

import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_HYBRID;
import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_NORMAL;

import java.util.ArrayList;
import java.util.Locale;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.mk4droid.IMC_Core.InfoWindowAdapterButtoned;
import com.mk4droid.IMC_Services.InternetConnCheck;
import com.mk4droid.IMC_Services.Service_Data;
import com.mk4droid.IMC_Services.Service_Location;
import com.mk4droid.IMC_Services.Service_Location.LocalBinder;
import com.mk4droid.IMC_Store.Constants_API;
import com.mk4droid.IMC_Utils.GEO;
import com.mk4droid.IMCity_PackDemo.R;

/**
 *  Main Fragment that contains a map showing all issues 
 * 
 * @copyright   Copyright (C) 2012 - 2013 Information Technology Institute ITI-CERTH. All rights reserved.
 * @license     GNU Affero General Public License version 3 or later; see LICENSE.txt
 * @author      Dimitrios Ververidis for the Multimedia Group (http://mklab.iti.gr). 
 * 
 */
public class Fragment_Map extends Fragment implements OnMarkerClickListener, OnInfoWindowClickListener { 

	public static Context ctx;

	/** User id number  */
	public String UserID_STR;

	/** User actual name  */
	public String UserRealName;

	/** Handler for updating Markers */
	public Handler handlerMarkersUPD;

	/** Handler for refreshing data */
	public Handler handlerBroadcastRefresh;
	
	/** The size of the screen used to resize category images */
	public static DisplayMetrics metrics;  
	

	// ========= Private ======
	SharedPreferences mshPrefs; // Shared preferences for storing/retrieving setting
	Polygon mPoly = null;       // polygon of LatLng coordinates from where issues can be sent
	View vframelayout_main;       


	static ProgressBar pbgeneral; // progress bar to show the downloading of data 
	static Resources resources;    // string, drawables etc.

	
	static Fragment_Issue_Details frag_issue_details; // The issue details fragment to call when clicking on info window 
	
	//----- Map -------------------------------
	SupportMapFragment fmap_main; // Map fragment 
	GoogleMap gmap;               // Map 

	ArrayList<Marker> mMarkers; 
	Marker lastOpenned = null;

	boolean FirstAnimToLoc = true;
	boolean isLocServBound = false;
	double minLat=0,minLong=0,maxLat=0,maxLong=0; // For Zooming correctly

	//----------- GUI ----------
	ImageButton btMaps,btRefresh,btMyIss;
	
	static boolean MyIssuesSW;

	int tlv = Toast.LENGTH_LONG;

	//------ Dialogs, Threads, Intents, IntentFilters --------
	Intent IntDataServ,IntLocServ;
	BroadcastReceiver mReceiverDataChanged;
	IntentFilter intentFilter;

	public Service_Location mService_Location = null;

	private ServiceConnection mLocConnection = new ServiceConnection() {

		@Override 
		public void onServiceConnected(ComponentName className, IBinder service) {
			LocalBinder binder = (LocalBinder) service;
			mService_Location = binder.getService();

			if(mService_Location != null){
				Log.d("service-bind", "Service is bonded successfully!");
			} else {
				Log.e("service-bind", "null");
			}
			isLocServBound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName className) {
			Log.d("service-bind", "disconnected");
			isLocServBound = false;
		}
	};


	/**
	 *   OnCreate Create GUI and Handlers for broadcasting messages
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mshPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		resources = SetResources();
	}


	/**
	 *            OnCreateView 
	 * 
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		// ----- Inflate the view ---------
		if (vframelayout_main != null) {
			ViewGroup parent = (ViewGroup) vframelayout_main.getParent();
			parent.removeView(vframelayout_main);
		} else {		
			vframelayout_main = inflater.inflate(R.layout.framelayout_map, container, false);
		}

		ctx = vframelayout_main.getContext();

		//------- Add fragment_map ----
		if (fmap_main==null){
			fmap_main = SupportMapFragment.newInstance();
			FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
			fragmentTransaction.add(R.id.flmain, fmap_main);
			fragmentTransaction.commit();
		}

		getFragmentManager().addOnBackStackChangedListener(new OnBackStackChangedListener() {    
			public void onBackStackChanged() {
				if (fmap_main == null && !Fragment_Issue_Details.isVisible && FActivity_TabHost.IndexGroup==0){
					onResume();
				} 
			}
		});

		//-----------------------------
		pbgeneral = (ProgressBar) vframelayout_main.findViewById(R.id.pbgeneral);
		pbgeneral.bringToFront();

		//---- Bind Location Service --------------
		IntLocServ = new Intent(ctx, Service_Location.class);

		if (!isLocServBound && FActivity_TabHost.IndexGroup == 0)
			ctx.bindService(IntLocServ, mLocConnection, Context.BIND_AUTO_CREATE);

		FActivity_TabHost.IndexGroup = 0;


		//-----Start Data Service ------------
		IntDataServ = new Intent(ctx, Service_Data.class);
		ctx.startService(IntDataServ);


		return vframelayout_main;
	}// end of CreateView 


	/**
	 * 
	 *                   On Resume 
	 * 
	 */
	@Override
	public void onResume() {
		super.onResume();

		//------------ Create Items on Maps -----------------------
		if (fmap_main == null){
			fmap_main = SupportMapFragment.newInstance();
			FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
			fragmentTransaction.add(R.id.flmain, fmap_main);
			fragmentTransaction.commit();

			getChildFragmentManager().executePendingTransactions();
		}

		if (fmap_main!=null)
			gmap = fmap_main.getMap();

		if (gmap!=null){	
			gmap.setInfoWindowAdapter(new InfoWindowAdapterButtoned(getActivity()));
			gmap.setMyLocationEnabled(true);
			
			gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Service_Location.locUserPred.getLatitude(),
					                                                     Service_Location.locUserPred.getLongitude()),14));
		}

		pbgeneral.bringToFront();

		//------- Button Maps Types  ------------
		btMaps    = (ImageButton)  vframelayout_main.findViewById(R.id.btMapChange);
		btRefresh = (ImageButton)  vframelayout_main.findViewById(R.id.btRefresh);
		btMyIss   = (ImageButton)  vframelayout_main.findViewById(R.id.btFilterMineIssMap);

		btRefresh.bringToFront();

		btRefresh.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				//---- Show dialog Refresh ----------
				//				dialogRefresh = ProgressDialog.show(ctx, resources.getString(R.string.Downloading),
				//						resources.getString(R.string.Refresh), true);

				// ------- Broadcast Refresh through a handler
				Message msg = new Message();
				msg.arg1 = 1;
				handlerBroadcastRefresh.sendMessage(msg);
			}
		});

		//------ set Handlers and Receivers ------
		HandlersAndReceivers();


		//------------------------------------------------------------		
		btMaps.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//------------- Other maps views click --------
				String bt_Tag = btMaps.getTag().toString();
				if (bt_Tag.equals("Satellite")){
					gmap.setMapType(MAP_TYPE_NORMAL);
					//btMaps.setText(resources.getString(R.string.NormalMap));
					btMaps.setTag("Normal");
				} else if (bt_Tag.equals("Normal")){
					gmap.setMapType(MAP_TYPE_HYBRID);
					//btMaps.setText(resources.getString(R.string.Satellite));
					btMaps.setTag("Satellite");
				}
			}
		});

		btMaps.bringToFront();
		//--------------------------------------------------------------
		resources = SetResources(); // to get new UserID

		if (!isLocServBound)
			ctx.bindService(IntLocServ, mLocConnection, Context.BIND_AUTO_CREATE);

		//---------- Maps ------------
		String bt_Tag = btMaps.getTag().toString();

		if (bt_Tag.equals("Normal") && gmap !=null){
			gmap.setMapType(MAP_TYPE_NORMAL);
			btMaps.setTag("Normal");
		} else if (bt_Tag.equals("Satellite") && gmap !=null){
			gmap.setMapType(MAP_TYPE_HYBRID);
			btMaps.setTag("Satellite");
		}

		//-------------- Check if distance has changed -------------
		int distanceData    = mshPrefs.getInt("distanceData"   , Constants_API.initRange);
		int distanceDataOLD = mshPrefs.getInt("distanceDataOLD", Constants_API.initRange);

		//---------- Check if IssuesNo has changed ---------
		int IssuesNoAR    =  Integer.parseInt( mshPrefs.getString("IssuesNoAR", "40") );
		int IssuesNoAROLD =  Integer.parseInt(mshPrefs.getString("IssuesNoAROLD", "40"));

		if (InternetConnCheck.getInstance(ctx).isOnline(ctx)){

			if (distanceData!=distanceDataOLD){
				// ------- Broadcast Refresh through a handle
				Message msg = new Message();
				msg.arg1 = 2;
				handlerBroadcastRefresh.sendMessage(msg);

			} else if (IssuesNoAR!=IssuesNoAROLD){
				// ------- Broadcast Refresh through a handle
				Message msg = new Message();
				msg.arg1 = 3;
				handlerBroadcastRefresh.sendMessage(msg);
			}
			btRefresh.setVisibility(View.VISIBLE);

		} else {
			btRefresh.setVisibility(View.GONE);
		}

		//-------------- BT myIssues -------
		btMyIss.bringToFront();

		if (UserRealName.length() == 0){
			btMyIss.setVisibility(View.GONE);
		}


		if (MyIssuesSW)
			btMyIss.setImageResource(R.drawable.ic_my_issues_gray);
		else
			btMyIss.setImageResource(R.drawable.ic_all_issues_gray);

		btMyIss.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				// Internal variables
				if (MyIssuesSW)
					MyIssuesSW = false;
				else 
					MyIssuesSW = true;

				savePreferences("MyIssuesSW", MyIssuesSW, "boolean");

				// Button appearance
				if (MyIssuesSW)
					btMyIss.setImageResource(R.drawable.my_issues_icon_gray32);
				else
					btMyIss.setImageResource(R.drawable.all_issues_icon_gray32);

				// now update the markers visible
				Message msg = new Message();
				msg.arg1 = 2;
				handlerMarkersUPD.sendMessage(msg);

			}
		});


		//-------- Init markers  ---------
		Message msg = new Message();
		msg.arg1 = 1;
		handlerMarkersUPD.sendMessage(msg);
		//----------- Flurry Analytics --------
		if (mshPrefs.getBoolean("AnalyticsSW", true))
			FlurryAgent.onStartSession(ctx,Constants_API.Flurry_Key);
		//-----------------------------------
	}



	/**
	 * OnSaveInstanceState 
	 *  The lifecycle of Destroy 
	 *  
	 *  1. onSaveInstance
	 *  2. onPause
	 *  3. onDestroyView
	 *  4. onDestroy
	 */
	@Override
	public void onSaveInstanceState(Bundle outState) {

		if (fmap_main!=null){
			getChildFragmentManager().beginTransaction().remove(fmap_main).commit();
			fmap_main = null;
		}

		if (gmap!=null){
			gmap.clear();
			gmap = null;	
			mPoly = null;
		}

		super.onSaveInstanceState(outState);
	}


	//===================================================
	/**
	 *       OnPause 
	 */
	@Override
	public void onPause() {
		
		if (fmap_main!=null){
			getChildFragmentManager().beginTransaction().remove(fmap_main).commit();
			fmap_main = null;
		}

		try {
			ctx.unregisterReceiver(mReceiverDataChanged);
		} catch (Exception e){}

		if (gmap!=null)
			gmap.setMyLocationEnabled(false);

		//------ Unbind Services -------
		if (isLocServBound){
			ctx.unbindService(mLocConnection);
			isLocServBound = false;	
		}

		//----------- Flurry Analytics --------
		if (mshPrefs.getBoolean("AnalyticsSW", true))
			FlurryAgent.onEndSession(ctx);

		super.onPause();
	}


	/**
	 *          onDestroyView
	 * 
	 */
	@Override
	public void onDestroyView() {
		gmap = null; 
		mPoly = null;
		super.onDestroyView();	
	}

	/**
	 *            OnDestroy
	 * 
	 */
	@Override
	public void onDestroy() {

		if (isLocServBound){
			ctx.unbindService(mLocConnection);
			isLocServBound = false;
		}
		ctx.stopService(IntDataServ);
		super.onDestroy();
	}


	/**
	 *    SetUp Map
	 */
	private void setUpMap() {

		//------- Create Polygon ----------
		if (mPoly==null && gmap !=null)
			mPoly = GEO.MakeBorders(gmap, getResources());

		if (gmap!=null){
			gmap.setOnMarkerClickListener(this);
			gmap.setOnInfoWindowClickListener(this);
		}
	}

	//=========== PutMarkers() =======================
	/*               
	 *  Markers are placed in overlays. Each overlay contains markers that have the same icon image.
	 *      
	 *  @param reZoomSW:  1 re-zoom, 2: no re-zoom         
	 */     
	private void PutMarkers(int reZoomSW){

		if (gmap==null){return;}


		if (mMarkers != null)
			if (mMarkers.size()>0)
				for (int i=0; i< mMarkers.size(); i++)
					mMarkers.get(i).remove();

		mMarkers = new ArrayList<Marker>();

		if (Service_Data.mIssueL==null)
			return;

		if (Service_Data.mIssueL.size() > 0) {
			minLat =  Service_Data.mIssueL.get(0)._latitude;
			minLong = Service_Data.mIssueL.get(0)._longitude;
			maxLat =  Service_Data.mIssueL.get(0)._latitude;
			maxLong=  Service_Data.mIssueL.get(0)._longitude;
		}

		//------------------ Urban Problems ----------------
		int NIssues = Service_Data.mIssueL.size(); 


		for (int j=0; j< Service_Data.mCategL.size(); j++){
			if (Service_Data.mCategL.get(j)._visible==1){  // Filters for Visibility

				// Every categ has an overlayitem with multiple overlays in it !!! 
				//---------- Drawable icon -------
				byte[] b  =  Service_Data.mCategL.get(j)._icon;

				Bitmap bm = null;
				if (b != null) {
					bm = BitmapFactory.decodeByteArray(b, 0, b.length);
				} else {
					// Load a standard icon 
					bm = BitmapFactory.decodeResource(getResources(), R.drawable.comments_icon);
				}

				for(int i=0; i<NIssues; i++){

					//------ iterate to find category of issue and icon to display
					if ( (Service_Data.mIssueL.get(i)._currentstatus == 1 && mshPrefs.getBoolean("OpenSW", true)) || 
							(Service_Data.mIssueL.get(i)._currentstatus == 2 && mshPrefs.getBoolean("AckSW", true) ) || 
							(Service_Data.mIssueL.get(i)._currentstatus == 3 && mshPrefs.getBoolean("ClosedSW", true)) ){

						if (Service_Data.mIssueL.get(i)._catid == Service_Data.mCategL.get(j)._id ){

							if (mshPrefs.getBoolean("MyIssuesSW", false) && 
									!Integer.toString(Service_Data.mIssueL.get(i)._userid).equals(UserID_STR))
								continue;

							//--------- upd view limits -------------
							maxLat  = Math.max( Service_Data.mIssueL.get(i)._latitude  , maxLat );
							minLat  = Math.min( Service_Data.mIssueL.get(i)._latitude  , minLat );
							maxLong = Math.max( Service_Data.mIssueL.get(i)._longitude , maxLong);
							minLong = Math.min( Service_Data.mIssueL.get(i)._longitude , minLong);




							mMarkers.add( gmap.addMarker(new MarkerOptions()
							.position(new LatLng(Service_Data.mIssueL.get(i)._latitude, Service_Data.mIssueL.get(i)._longitude))
							.title(Service_Data.mIssueL.get(i)._title)
							.snippet("# " + Integer.toString(Service_Data.mIssueL.get(i)._id))
							.icon(BitmapDescriptorFactory.fromBitmap(bm)))
									);

						} // cat match 
					} // open closed 
				} // i
			} // visible
		} // j


		if (reZoomSW==1){
			if (Fragment_NewIssueB.LastIssLat!= 0 && Fragment_NewIssueB.LastIssLong!= 0){
				gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(
						new LatLng(Fragment_NewIssueB.LastIssLat,
								Fragment_NewIssueB.LastIssLong),14));
				
				Fragment_NewIssueB.LastIssLat = 0;
				Fragment_NewIssueB.LastIssLong= 0;
				
				
			} else {

				if (Math.abs((maxLat - minLat)) + Math.abs(maxLong - minLong) !=0){
					LatLngBounds bounds = new LatLngBounds.Builder()
					.include(new LatLng(maxLat,minLong))           // Upper Right 
					.include(new LatLng(minLat,maxLong))           // Lower Left
					.build();

					metrics = new DisplayMetrics();
					getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

					gmap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, metrics.widthPixels, metrics.heightPixels, 50));

				}else if (Service_Location.locUser.getLatitude()!=0 && Service_Location.locUser.getLongitude()!=0){
					gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Service_Location.locUser.getLatitude(),
							Service_Location.locUser.getLongitude()),14));
				}else {
					gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Service_Location.locUserPred.getLatitude(),
							Service_Location.locUserPred.getLongitude()),14));
				}
			}
		}
	}

	/**
	 *  On marker click show issue title and id in Information Window
	 */
	@Override
	public boolean onMarkerClick(Marker marker) {

		if (lastOpenned != null) {
			// Close the info window
			lastOpenned.hideInfoWindow();

			// Is the marker the same marker that was already open
			if (lastOpenned.equals(marker)) {
				// Nullify the lastOpenned object
				lastOpenned = null;
				// Return so that the info window isn't openned again
				return true;
			} 
		}

		// Open the info window for the marker
		marker.showInfoWindow();
		// Re-assign the last openned such that we can close it later
		lastOpenned = marker;

		CameraUpdate camUpdate = CameraUpdateFactory.newLatLng(marker.getPosition());

		gmap.animateCamera(camUpdate, 10, null); 
		return true;
	}

	/**
	 *  on Information window click -> go to Issue_Details
	 */
	@Override
	public void onInfoWindowClick(Marker marker) {
		String markerSnippet = marker.getSnippet();
		// find id
		int Snippet_id = Integer.parseInt(markerSnippet.substring(2));

		// remove location services
		gmap.setMyLocationEnabled(false);
		if (isLocServBound){
			ctx.unbindService(mLocConnection);
			isLocServBound = false;
		}

		FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();

		// new 			        
		frag_issue_details = new Fragment_Issue_Details();
		Bundle args = new Bundle();
		args.putInt("issueId", Snippet_id);
		frag_issue_details.setArguments(args);


		//stop map
		getChildFragmentManager().beginTransaction().remove(fmap_main).commit();
		fmap_main = null;
		gmap = null;
		mPoly = null;

		//--------------------------------
        try {
		ctx.unregisterReceiver(mReceiverDataChanged);
        } catch (Exception e){}
		
		ft.replace(R.id.flmain,  frag_issue_details, "MAIN_FTAG_ISSUE_DETAILS");
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		ft.addToBackStack(null);
		ft.commit();
	}

	
	/*   
	 *      Load all handlers and Receivers necessary for this fragment
	 */
	private void HandlersAndReceivers(){

		//----- Handler for Redrawing Markers from update thread ------------
		handlerMarkersUPD = new Handler()
		{
			public void handleMessage(Message msg)
			{
				
				setUpMap(); 	
				PutMarkers(msg.arg1);  // 1 rezoom, 2 no-rezoom
				super.handleMessage(msg);
			}
		};

		//----- Handler for Redrawing Markers from update thread ------------
		handlerBroadcastRefresh = new Handler() // Broadcast 1. Refresh Button 2. DistanceCh 3. IssuesNoCh
		{
			public void handleMessage(Message msg)
			{
				if (msg.arg1 == 1) // Refresh Button
					ctx.sendBroadcast(new Intent("android.intent.action.MAIN").putExtra("Refresh", "ok"));  
				else if (msg.arg1 == 2)
					ctx.sendBroadcast(new Intent("android.intent.action.MAIN").putExtra("DistanceChanged", "Indeed"));
				else if (msg.arg1 == 3)
					ctx.sendBroadcast(new Intent("android.intent.action.MAIN").putExtra("IssuesNoChanged", "yep"));

				super.handleMessage(msg);
			}
		};

		//--------------- Receiver for Data change ------------
		intentFilter = new IntentFilter("android.intent.action.MAIN"); // DataCh

		mReceiverDataChanged = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				String DataChanged     = intent.getStringExtra("DataChanged");
				String mes_touser      = intent.getStringExtra("mes_touser");
				int progressval        = intent.getIntExtra("progressval",-1);
				String progressBarVis  = intent.getStringExtra("ProgressBar");

				if (progressBarVis!=null){
					if (progressBarVis.length()>0){
						if (progressBarVis.equals("Visible"))
							pbgeneral.setVisibility(View.VISIBLE);
						else if (progressBarVis.equals("Gone"))
							pbgeneral.setVisibility(View.GONE);
					}
				}


				if (DataChanged!=null){
					Log.e("FMain: DataChanged", " " + DataChanged);

					if (mes_touser!=null)
						if ((mes_touser.trim()).length()>0)
							Toast.makeText(ctx, mes_touser, Toast.LENGTH_LONG).show();

					pbgeneral.setVisibility(View.GONE);

					Message msg = new Message();
					msg.arg1 = 1;
					handlerMarkersUPD.sendMessage(msg);
				}


				if (progressval!=-1){
					pbgeneral.setProgress(progressval);
				}

			}
		};

		
		ctx.registerReceiver(mReceiverDataChanged, intentFilter);
	}

	/* ===========     Set Resources  =========================
	 *      Obtain resources from preferences 
	 */
	private Resources SetResources(){

		String LangSTR   = mshPrefs.getString("LanguageAR", Constants_API.DefaultLanguage);
		UserID_STR       = mshPrefs.getString("UserID_STR", "");
		UserRealName     = mshPrefs.getString("UserRealName", "");
		MyIssuesSW       = mshPrefs.getBoolean("MyIssuesSW", false);

		Configuration conf = getResources().getConfiguration();
		conf.locale = new Locale(LangSTR.substring(0, 2)); //----- Convert Greek -> el ---------
		metrics = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

		getActivity().getResources().updateConfiguration(conf, getActivity().getResources().getDisplayMetrics());

		return new Resources(getActivity().getAssets(), metrics, conf);
	}


	/*
	 * ========================== savePreferences =================================
	 * 
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

} // end of Fragment_Map