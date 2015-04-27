/* Activity_NewIssueB */
package com.mk4droid.IMC_Activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationChangeListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.mk4droid.IMC_Services.Service_Data;
import com.mk4droid.IMC_Services.Service_Location;
import com.mk4droid.IMC_Services.Upload_Data;
import com.mk4droid.IMC_Store.Constants_API;
import com.mk4droid.IMC_Utils.GEO;
import com.mk4droid.IMCity_PackDemo.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Second fragment for submitting an issue: selecting the location of the issue and submit to remote server  
 *
 * @copyright   Copyright (C) 2012 - 2015 Information Technology Institute ITI-CERTH. All rights reserved.
 * @license     GNU Affero General Public License version 3 or later; see LICENSE.txt
 * @author      Dimitrios Ververidis for the Multimedia Group (http://mklab.iti.gr). 
 *
 */
public class Fragment_NewIssueB extends Fragment implements OnMarkerDragListener, OnMarkerClickListener, OnInfoWindowClickListener{

	/** This fragment */
	public static Fragment_NewIssueB mfrag_nIssueB;
	
	/** Strings, images, etc. */
	public static Resources resources;
		
	/** The new issue coordinates for zooming on Main map after issue has been submitted successfulyl  */
	public static double LastIssLat, LastIssLong;
	
	/** The edittext where to set automatically or manually the address string */
	public static EditText etAddress;
	
	static Handler handlerBroadcastNewIssue;

	int tlv = Toast.LENGTH_LONG;
	double Lat_D,Long_D;

	// ---------- WINDOW --------
	DisplayMetrics metrics;
	public static Context ctx;

	//----------- GPS -----------
	String Address_STR = "";
	Handler handlerAddresUPD;

	ScrollView scr_NIB;
	ImageView  imv_transparent; 

	static Marker mMarker;

	static LatLng pos;
	static Button btSubmit;
	

	Polygon poly = null;
	//------------VARs ------------

	String UserNameSTR,PasswordSTR,UserID_STR;
	SharedPreferences mshPrefs;
	int IndexCatSpinner;


	View vframeLayout_nib;
	SupportMapFragment fmap;
	GoogleMap gmap;

	private ProgressDialog progressLoc;

	private static ProgressDialog progressSending,progressReported;

	//============== OnCreate ==================
	/**
	 *  Set content view. 
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		pos = new LatLng(Service_Location.locUser.getLatitude(), Service_Location.locUser.getLongitude() );
		mshPrefs        = PreferenceManager.getDefaultSharedPreferences(getActivity());

		IndexCatSpinner = getArguments() != null ? getArguments().getInt("IndexSpinner") : -1; // Serial Index of the issue
		resources       = SetResources();
	}


	/**
	 *    on Create View of this fragment
	 */
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);		

		ctx = this.getActivity();

		if (vframeLayout_nib != null) {
			ViewGroup parent = (ViewGroup) vframeLayout_nib.getParent();
			parent.removeView(vframeLayout_nib);
		} else 
			vframeLayout_nib = inflater.inflate(R.layout.framelayout_newissue_b, container, false);

		mfrag_nIssueB = this;
		
		resources = SetResources();

		//--------------------------
		if (fmap == null){
			fmap  = SupportMapFragment.newInstance();
			getChildFragmentManager().beginTransaction().add(R.id.flmapnewissue, fmap).commit();
		}

		etAddress = (EditText) vframeLayout_nib.findViewById(R.id.etAddress);
		etAddress.setCompoundDrawablesWithIntrinsicBounds(resources.getDrawable(R.drawable.ic_location), null, null, null);

		scr_NIB = (ScrollView) vframeLayout_nib.findViewById(R.id.scrNIB);
		imv_transparent = (ImageView) vframeLayout_nib.findViewById(R.id.imv_overlaymap_NIB);

		imv_transparent.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int action = event.getAction();
				switch (action) {
				case MotionEvent.ACTION_DOWN:
					// Disallow ScrollView to intercept touch events.
					scr_NIB.requestDisallowInterceptTouchEvent(true);
					// Disable touch on transparent view
					return false;

				case MotionEvent.ACTION_UP:
					// Allow ScrollView to intercept touch events.
					scr_NIB.requestDisallowInterceptTouchEvent(false);
					return true;

				case MotionEvent.ACTION_MOVE:
					scr_NIB.requestDisallowInterceptTouchEvent(true);
					return false;

				default: 
					return true;
				}   
			}
		});


		//----- Handler for setting address string ------------
		handlerAddresUPD = new Handler()
		{
			public void handleMessage(Message msg)
			{

				if (msg.arg1 == 2) { // Location not found, get from predefined

					pos = new LatLng(Constants_API.locUserPred_Lat, Constants_API.locUserPred_Long);

					gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos,14)); 		// Cannot zoom to bounds until the map has a size.

					mMarker = gmap.addMarker(new MarkerOptions()
					.position(pos)
					.title(resources.getString(R.string.Issueposition))
					.snippet(resources.getString(R.string.Dragndrop))
					.draggable(true));
				}

				Lat_D  = mMarker.getPosition().latitude;
				Long_D = mMarker.getPosition().longitude;

				LatLng pt = new LatLng(Lat_D, Long_D); 

				Address_STR = GEO.ConvertGeoPointToAddress(pt, ctx);

				if (Address_STR!="")
					etAddress.setText(Address_STR);

				progressLoc.dismiss();

				super.handleMessage(msg);
			}
		};

		//-------- Broadcast new Issue was send through a handler ---------
		handlerBroadcastNewIssue = new Handler(){
			public void handleMessage(Message msg){
				if (msg.arg1 == 1) // Refresh Button
					ctx.sendBroadcast(new Intent("android.intent.action.MAIN").putExtra("NewIssueAdded", "ok"));  

				super.handleMessage(msg);
			}
		};

		return vframeLayout_nib;
	}

	//============== On Resume =====================
	/**
	 * Executed after activity is created or after changing tab
	 */
	@Override
	public void onResume() {
		super.onResume();

		// The new issue coordinates
		LastIssLat  = 0;
		LastIssLong = 0;

		progressLoc = ProgressDialog.show(ctx, "", "", true);
		progressLoc.setContentView(R.layout.dialog_transparent_progress);
		((TextView) progressLoc.findViewById(R.id.tv_prog)).setText("");


		resources = SetResources();



		//---------- Map ------
		gmap = fmap.getMap();

		// Check if we were successful in obtaining the map.
		if (gmap != null) {
			gmap.clear();
			gmap.setOnInfoWindowClickListener(this);
			
			//------- Create Polygon ----------
			if (poly==null)
				poly = GEO.MakeBorders(gmap, getResources());




			gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos,14));

			gmap.setOnMyLocationChangeListener(new OnMyLocationChangeListener() {
				@Override
				public void onMyLocationChange(Location arg0) {

					if (mMarker==null){
						pos = new LatLng(arg0.getLatitude(), arg0.getLongitude());

						gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos,14)); 		// Cannot zoom to bounds until the map has a size.

						mMarker = gmap.addMarker(new MarkerOptions()
						.position(pos)
						.title(resources.getString(R.string.Issueposition))
						.snippet(resources.getString(R.string.Dragndrop))
						.draggable(true));


						Message msg = new Message();
						msg.arg1 = 1;
						handlerAddresUPD.sendMessage(msg);
					}
				}
			});

			gmap.setOnMarkerDragListener(this);
			gmap.setMyLocationEnabled(true);

			//----- Timed Trigger to ensure that if loc not found then get prefined loc ---
			new Thread(new Runnable() {

				@Override
				public void run() {

					try {
						Thread.sleep(6000);

						if (progressLoc.isShowing()){
							Message msg = new Message();
							msg.arg1 = 2;
							handlerAddresUPD.sendMessage(msg);
						}

					} catch (InterruptedException e) {
					}

				}
			}).start();

		}

		// ============ SUBMIT BUTTON ====================
		btSubmit = (Button) vframeLayout_nib.findViewById(R.id.btReport_new_issue);

		btSubmit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				String titleData_STR       = Fragment_NewIssueA.et_title.getText().toString();  // 1. Title Data
				String descriptionData_STR = Fragment_NewIssueA.et_descr.getText().toString();  // 3. Description Data
				Address_STR                = etAddress.getText().toString();

				String ImageFN_target = "";

				// Check if Image is taken else do not sent  
				if (Fragment_NewIssueA.flagPictureTaken){
					String FileNameExt = new SimpleDateFormat("yyyy-MM-dd hh-mm-ss'.jpg'").format(new Date());

					FileNameExt  =  FileNameExt.replace("-", "_");
					FileNameExt  =  FileNameExt.replace(" ", "t");
					ImageFN_target = UserID_STR + "d" + FileNameExt;
				}

				double Lat_D  = mMarker.getPosition().latitude;
				double Long_D = mMarker.getPosition().longitude;

				// Check if title is long enough and sent
				if ( Fragment_NewIssueA.et_title.getText().toString().length() > 2 && 
						etAddress.getText().toString().length()>0 && 
						Fragment_NewIssueA.et_descr.getText().toString().length() > 2){

					if (GEO.insidePoly(poly, Long_D, Lat_D)){
						new AsynchTask_ReportIssue(ImageFN_target, titleData_STR, IndexCatSpinner, 
								Lat_D, Long_D, descriptionData_STR, Address_STR, UserNameSTR, PasswordSTR).execute();
					} else {
						Toast.makeText(ctx,	resources.getString(R.string.Issueoutofmunicipalitylimits),	tlv).show();
					}
				}else if ( Fragment_NewIssueA.et_title.getText().toString().length() <= 2){
					Toast.makeText(ctx, resources.getString(R.string.LongerTitle), tlv).show();
				}else if ( etAddress.getText().toString().length() == 0){
					Toast.makeText(ctx, resources.getString(R.string.WriteAddress), tlv).show();
				}else if ( Fragment_NewIssueA.et_descr.getText().toString().length() <= 2){
					Toast.makeText(ctx, resources.getString(R.string.LongerDescription), tlv).show();
				}

			}});

		//----------- Flurry Analytics --------
		boolean AnalyticsSW = mshPrefs.getBoolean("AnalyticsSW", true);
		if (AnalyticsSW)
			FlurryAgent.onStartSession(ctx, Constants_API.Flurry_Key);
	}

	//================= onPause =========================
	/**
	 *    Hinter map visibility and stop Flurry analytics
	 */
	@Override
	public void onPause() {
		super.onPause();

		btSubmit.setText(resources.getString(R.string.ReportIss));

		if (gmap!=null){
			gmap.setMyLocationEnabled(false);
			poly = null;	
		}

		mMarker = null;
		//----------- Flurry Analytics --------
		boolean AnalyticsSW = mshPrefs.getBoolean("AnalyticsSW", true);

		if (AnalyticsSW)
			FlurryAgent.onEndSession(ctx);
	}  


	//============   Set Resources =========================== 
	/* Retrieve preferences and set resources language */ 
	private Resources SetResources(){

		String LangSTR          = mshPrefs.getString("LanguageAR", Constants_API.DefaultLanguage);
		UserID_STR              = mshPrefs.getString("UserID_STR", "");

		
		
		UserNameSTR      = mshPrefs.getString("UserNameAR", "");
		PasswordSTR      = mshPrefs.getString("PasswordAR", "");

		Configuration conf = getResources().getConfiguration();
		conf.locale = new Locale(LangSTR.substring(0, 2)); //----- Convert Greek -> el ---------
		metrics = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
		return new Resources(getActivity().getAssets(), metrics, conf);
	}


	/**
	 *    OnMarker drag end set Position
	 */
	@Override
	public void onMarkerDragEnd(Marker arg0) {

		progressLoc = ProgressDialog.show(ctx, "", "", true);
		progressLoc.setContentView(R.layout.dialog_transparent_progress);
		((TextView) progressLoc.findViewById(R.id.tv_prog)).setText("");

		Message msg = new Message();
		msg.arg1 = 1;
		handlerAddresUPD.sendMessage(msg);
	}


	/**
	 *    OnMarker drag start vibrate
	 */
	@Override
	public void onMarkerDragStart(Marker arg0) {
		//Vibrate
		Vibrator vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
		vibrator.vibrate(500);    
	}

	@Override
	public void onMarkerDrag(Marker arg0) {}



	/**
	 *   Report Issue Asynchronously 
	 *   
	 * @copyright   Copyright (C) 2012 - 2015 Information Technology Institute ITI-CERTH. All rights reserved.
	 * @license     GNU Affero General Public License version 3 or later; see LICENSE.txt
	 * @author      Dimitrios Ververidis for the Multimedia Group (http://mklab.iti.gr). 
	 *
	 */
	private static class AsynchTask_ReportIssue extends AsyncTask<String, String, Boolean>{

		private String imagepathtarget = "";
		private String title = "";
		private int IndexCatSpinner;
		private double Lat_D, Long_D;
		private String descriptionData_STR, Address_STR, UserNameSTR, PasswordSTR;
		/**
		 * 
		 */
		public AsynchTask_ReportIssue(String imagepathtarget_in, String title_in, int IndexCatSpinner_in, 
				double Lat_D_in, double  Long_D_in, String descriptionData_STR_in, String Address_STR_in, String UserNameSTR_in, String PasswordSTR_in) {

			imagepathtarget = imagepathtarget_in;
			title = title_in;
			IndexCatSpinner = IndexCatSpinner_in;
			Lat_D = Lat_D_in;
			Long_D = Long_D_in;
			descriptionData_STR = descriptionData_STR_in;
			Address_STR = Address_STR_in;
			UserNameSTR =UserNameSTR_in;
			PasswordSTR = PasswordSTR_in;
		}

		/* (non-Javadoc)
		 * @see android.os.AsyncTask#onPreExecute()
		 */
		@Override
		protected void onPreExecute() {
			progressSending = ProgressDialog.show(ctx, "", "", true);
			progressSending.setContentView(R.layout.dialog_transparent_progress);
			((TextView) progressSending.findViewById(R.id.tv_prog)).setText(resources.getString(R.string.Uploading));

			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(String... params) {

			boolean successIadd = false ;

			successIadd = Upload_Data.SendIssue(Fragment_NewIssueA.image_path_source_temp, imagepathtarget, 
					title, Fragment_NewIssueA.SpinnerArrID[IndexCatSpinner],
					Lat_D, Long_D, descriptionData_STR, Address_STR, UserNameSTR, PasswordSTR );



			return successIadd;
		};


		/* (non-Javadoc)
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(Boolean success) {

			progressSending.dismiss();

			if (success){
				btSubmit.setText(resources.getString(R.string.IssueReported));

				//------- Reset GUI --------
				Fragment_NewIssueA.flagPictureTaken = false;
				Fragment_NewIssueA.flagStarter = true;
				Fragment_NewIssueA.btAttachImage.setScaleType(ScaleType.CENTER_INSIDE);
				Fragment_NewIssueA.btAttachImage.setImageResource(R.drawable.bt_custom_camera_round);
				Fragment_NewIssueA.spPosition = -1;
				Fragment_NewIssueA.et_title.setText("");
				Fragment_NewIssueA.et_descr.setText("");

				progressReported = ProgressDialog.show(ctx, "", "", true);
				progressReported.setContentView(R.layout.dialog_transparent_issue_success);

				Button btClose =  (Button) progressReported.findViewById(R.id.bt_close_issrep);

				btClose.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						progressReported.dismiss();

						LastIssLat  = Lat_D;
						LastIssLong = Long_D;

						// ------- Send Broadcast through a handler -------
						Message msg = new Message();
						msg.arg1 = 1;
						handlerBroadcastNewIssue.sendMessage(msg);
					}
				});
			} else {
				if (!Service_Data.HasInternet)
					Toast.makeText(ctx, resources.getString(R.string.NoInternet), Toast.LENGTH_LONG).show();
				else 
					Toast.makeText(ctx, resources.getString(R.string.FailMes), Toast.LENGTH_LONG).show();

				LastIssLat  = 0;
				LastIssLong = 0;
			}
			super.onPostExecute(success);
		}
	}

	/* (non-Javadoc)
	 * @see com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener#onInfoWindowClick(com.google.android.gms.maps.model.Marker)
	 */
	@Override
	public void onInfoWindowClick(Marker mM) {
		mM.hideInfoWindow();
	}

	/* (non-Javadoc)
	 * @see com.google.android.gms.maps.GoogleMap.OnMarkerClickListener#onMarkerClick(com.google.android.gms.maps.model.Marker)
	 */
	@Override
	public boolean onMarkerClick(Marker mM) {
		mM.hideInfoWindow();
		return false;
	}
}
