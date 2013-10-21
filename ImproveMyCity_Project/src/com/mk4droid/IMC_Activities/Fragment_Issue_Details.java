//    Activity_Issue_Details
package com.mk4droid.IMC_Activities;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Locale;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mk4droid.IMC_Constructors.Issue;
import com.mk4droid.IMC_Constructors.IssuePic;
import com.mk4droid.IMC_Services.DatabaseHandler;
import com.mk4droid.IMC_Services.Download_Data;
import com.mk4droid.IMC_Services.InternetConnCheck;
import com.mk4droid.IMC_Services.Service_Data;
import com.mk4droid.IMC_Services.Service_Location;
import com.mk4droid.IMC_Services.Upload_Data;
import com.mk4droid.IMC_Store.Constants_API;
import com.mk4droid.IMC_Utils.My_Date_Utils;
import com.mk4droid.IMC_Utils.My_System_Utils;
import com.mk4droid.IMCity_PackDemo.R;

/**
 * Show issue details.
 * 
 * @copyright   Copyright (C) 2012 - 2013 Information Technology Institute ITI-CERTH. All rights reserved.
 * @license     GNU Affero General Public License version 3 or later; see LICENSE.txt
 * @author      Dimitrios Ververidis for the Multimedia Group (http://mklab.iti.gr). 
 *
 */
public class Fragment_Issue_Details extends Fragment {

    /** The dialog for viewing the issue image with details */
	public static Dialog dialogZoomIm;
	
	/** Variable to check if this fragment is visible */
	public static boolean isVisible;
	
	/** The issue image as a bitmap */
	public static Bitmap bmI = null;
	
	/**  The view of this fragment  */
	public static View vfrag_issue_details;

	/** This Fragment */
	public static Fragment mfrag_issue_details;

	
	
	boolean HasVotedSW; // flag to check if current user has already voted
	boolean OwnIssue;   // flag to check if this issue is reported by current user
	
	static DatabaseHandler dbHandler; // local db sqlite handler

	static int fullLines = 0; // number of lines of the description

	String LangSTR = "en";

	static String UserNameSTR, PasswordSTR, IssuesNoSTR, UserID_STR;

	boolean AuthFlag;     

	static int distanceData;
	static Resources resources;
	static Context ctx;
	static Issue mIssue;
	static int issueId;
	static DisplayMetrics metrics;
	static ImageView imvFull;
	
	//------ map ---------
	SupportMapFragment fmap_issdet;
	GoogleMap gmap_issdet;
	MarkerOptions markerOptions;
	Marker mMarker;
	static Bitmap bmCateg;
	TextView tv_id;
	private TextView tvDescription, tvStatus_ack, tvStatus_cl;
	private View vStatus_ack, vStatus_cl;

	static OnGlobalLayoutListener globlist;

	//================= onCreate ===========================
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		issueId = getArguments().getInt("issueId"); // id of the issue

		for (int i = 0; i<Service_Data.mIssueL.size(); i++){
			if (issueId == Service_Data.mIssueL.get(i)._id){
				mIssue = Service_Data.mIssueL.get(i);
				break;
			}
		}
	}

	/**
	 *    OnCreateView
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		isVisible = true;

		vfrag_issue_details = inflater.inflate(R.layout.fragment_issue_details, container, false);
		ctx                 = vfrag_issue_details.getContext();
		resources           = setResources();
		mfrag_issue_details = this;

		//========= Image =================
		dbHandler = new DatabaseHandler(ctx);
		imvFull = (ImageView) vfrag_issue_details.findViewById(R.id.imvIssue_Full);

		IssuePic issuepic = dbHandler.getIssuePic(mIssue._id);

		if (issuepic._IssuePicData!=null){
			bmI = My_System_Utils.LowMemBitmapDecoder(issuepic._IssuePicData);
		} else {
			//------- Try to download from internet --------------  
			if (InternetConnCheck.getInstance(ctx).isOnline(ctx) && !mIssue._urlphoto.equals("null") && !mIssue._urlphoto.equals("") && 
					mIssue._urlphoto.length()>0){

				mIssue._urlphoto = mIssue._urlphoto.replaceFirst("/thumbs", "");
				new ThumbnailTask_IssDetails(mIssue._urlphoto, mIssue._id).execute();
			} 
		}

		dbHandler.db.close();

		imvFull.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				dialogZoomIm = null;

				if (FActivity_TabHost.IndexGroup == 0)
					dialogZoomIm = new Dialog(ctx, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
				else if (FActivity_TabHost.IndexGroup == 1) 
					dialogZoomIm = new Dialog(FActivity_TabHost.ctx, android.R.style.Theme_Black_NoTitleBar_Fullscreen);

				dialogZoomIm.requestWindowFeature(Window.FEATURE_NO_TITLE);
				dialogZoomIm.setContentView(R.layout.custom_dialog);
				dialogZoomIm.show();
			}}
		);

		// ============ Title and id =========
		tv_id = (TextView) vfrag_issue_details.findViewById(R.id.tv_issuenumber);
		TextView tvTitB = (TextView) vfrag_issue_details.findViewById(R.id.tvTitleIssDetB);

		tv_id.setText(Html.fromHtml("<b><big>#</big></b> "+ issueId));
		tv_id.setMovementMethod(new ScrollingMovementMethod());

		tvTitB.setText(mIssue._title);

		//=============== Description ======================
		tvDescription= (TextView ) vfrag_issue_details.findViewById(R.id.textViewDescription);
		if (!mIssue._description.equals(""))
			tvDescription.setText(mIssue._description);
		else {
			tvDescription.setVisibility(View.GONE);
		}

		// ============== CATEGORY ===============================
		TextView tvCateg = (TextView ) vfrag_issue_details.findViewById(R.id.textViewCategContent);

		int iCateg = 0;
		for (int i=0; i<Service_Data.mCategL.size(); i++)
			if (Service_Data.mCategL.get(i)._id == mIssue._catid){
				iCateg= i;
				break;
			}

		tvCateg.setText(Service_Data.mCategL.get(iCateg)._name);

		try {
			bmCateg  = My_System_Utils.LowMemBitmapDecoder(Service_Data.mCategL.get(iCateg)._icon);
			BitmapDrawable drCateg = new BitmapDrawable(bmCateg);

			tvCateg.setCompoundDrawablesWithIntrinsicBounds(drCateg, null, null, null);
			tvCateg.setCompoundDrawablePadding(10);
			tvCateg.postInvalidate();
		} catch (Exception e){

		}

		markerOptions = new MarkerOptions()
		.position(new LatLng(mIssue._latitude, mIssue._longitude))
		.title(mIssue._title)
		.icon(BitmapDescriptorFactory.fromBitmap(bmCateg));


		//================  STATUS ================ 	  
		tvStatus_ack     = (TextView ) vfrag_issue_details.findViewById(R.id.tv_Status_issuedetails_ack);
		tvStatus_cl     = (TextView ) vfrag_issue_details.findViewById(R.id.tv_Status_issuedetails_cl);

		vStatus_ack     = vfrag_issue_details.findViewById(R.id.v_Status_issuedetails_acknow);
		vStatus_cl     = vfrag_issue_details.findViewById(R.id.v_Status_issuedetails_cl);

		int CurrStat = mIssue._currentstatus;

	    Colora(CurrStat);

		// ============== Time and Author ================
		TextView tvSubmitted = (TextView ) vfrag_issue_details.findViewById(R.id.tvSubmitted);

		String TimeStampRep = mIssue._reported.replace("-", "/");

		tvSubmitted.setText(
				resources.getString(R.string.Submitted) +  " " +
						My_Date_Utils.SubtractDate(TimeStampRep, LangSTR) + " " +  
						resources.getString(R.string.ago) + " " +
						resources.getString(R.string.by) + " " +
						mIssue._username);

		//============== Votes========================
		TextView tvVotes      = (TextView ) vfrag_issue_details.findViewById(R.id.textViewVotes);
		tvVotes.setText( Integer.toString(mIssue._votes) + " " + resources.getString(R.string.peoplevoted) );

		Button btVote = (Button) vfrag_issue_details.findViewById(R.id.buttonVote);
		//-------- Check if state is Ack or Closed then can not vote ----
		if (CurrStat==2 || CurrStat==3)
			btVote.setEnabled(false);

		//-------- Check if Has Voted ----------
		DatabaseHandler dbHandler = new DatabaseHandler(ctx);
		HasVotedSW = dbHandler.CheckIfHasVoted(issueId);

		OwnIssue = false;
		if (UserID_STR.length()>0)
			OwnIssue  = dbHandler.checkIfOwnIssue(Integer.toString(issueId), UserID_STR);

		dbHandler.db.close();

		// if has not voted, it is not his issue, and authenticated then able to vote 
		if (!OwnIssue && !HasVotedSW && AuthFlag){
			btVote.setEnabled(true);
		}

		if (OwnIssue || HasVotedSW) {
			btVote.setEnabled(false);
			btVote.setText(resources.getString(R.string.AlreadyVoted));
		}

		if (!AuthFlag) {
			btVote.setText(resources.getString(R.string.Vote));
		}

		btVote.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				if (InternetConnCheck.getInstance(ctx).isOnline(ctx) && AuthFlag){
					new AsynchTaskVote().execute();
				} else if (!InternetConnCheck.getInstance(ctx).isOnline(ctx)) {
					Toast.makeText(ctx, resources.getString(R.string.NoInternet), Toast.LENGTH_SHORT).show();	
				} else if (!AuthFlag){
					Toast.makeText(ctx, resources.getString(R.string.OnlyRegistered), Toast.LENGTH_SHORT).show();					
				}
			}
		});

		//============ Address - MapStatic - Button Map dynamic ========================
		TextView tvAddr       = (TextView ) vfrag_issue_details.findViewById(R.id.textViewAddressContent);
		tvAddr.setText(mIssue._address);

		fmap_issdet = SupportMapFragment.newInstance();

		FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
		fragmentTransaction.add(R.id.lliss_det_map, fmap_issdet);
		fragmentTransaction.commit();

		// ============ COMMENTS ===========================
		Button btCommentsSW = (Button) vfrag_issue_details.findViewById(R.id.btCommentsSW);

		btCommentsSW.setText(resources.getString(R.string.ViewComments));

		btCommentsSW.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (InternetConnCheck.getInstance(ctx).isOnline(ctx)){
					FragmentTransaction ft2 = getFragmentManager().beginTransaction();

					Fragment_Comments  newfrag_comments = new Fragment_Comments(); 				// Instantiate a new fragment.
					Bundle args = new Bundle();
					args.putInt("issueId", issueId);
					args.putString("issueTitle", mIssue._title);
					newfrag_comments.setArguments(args); // Add the fragment to the activity, pushing this transaction on to the back stack.

					if (FActivity_TabHost.IndexGroup == 0)
						ft2.add(R.id.flmain, newfrag_comments, "FTAG_COMMENTS");
					else if (FActivity_TabHost.IndexGroup == 1){
						ft2.add(R.id.fl_IssuesList_container, newfrag_comments, "FTAG_COMMENTS");
					}

					ft2.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
					ft2.addToBackStack(null);
					ft2.commit();
				} else {
					Toast.makeText(ctx, resources.getString(R.string.NoInternet), Toast.LENGTH_SHORT).show();	
				}
			}
		});

		return vfrag_issue_details;
	}// -------- End OnCreate -----------


	
	
	//======================= OnResume ============================
	/**
	 *                         ONRESUME
	 */
	@Override
	public void onResume() {
		super.onResume();

		fullLines = 0;

		globlist = new OnGlobalLayoutListener() {
			private boolean flagstop_lines = false;

			public void onGlobalLayout() {


				//----- tvDescription Resizable -----
				if ( tvDescription.getLineCount() > fullLines ) 
					fullLines = tvDescription.getLineCount();

				if ( fullLines > 2 && !flagstop_lines){ // if too big truncate with button to expand it
					tvDescription.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.expander_ic_minimized);
					tvDescription.setLines(2);
					flagstop_lines = true;
				}

				//-------- Set image of issue --------- 
				if (bmI == null){
					imvFull.setClickable(false);
				} else {
					imvFull.setClickable(true);
					imvFull.setImageBitmap(bmI);
					imvFull.setScaleType(ScaleType.CENTER_CROP);
				}

			}
		};


		if (vfrag_issue_details.getViewTreeObserver().isAlive()) 
			vfrag_issue_details.getViewTreeObserver().addOnGlobalLayoutListener(globlist);

		//--------------- Resize Description ------------
		tvDescription.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (fullLines > 2){
					if (tvDescription.getLineCount() == 2){
						
						int flines_plus = fullLines; 
						
						tvDescription.setLines( flines_plus ); // + x % for the drawable arrow
						tvDescription.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.expander_ic_maximized);
					}else {
						tvDescription.setLines(2);
						tvDescription.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.expander_ic_minimized);
					}
				}
			}
		});


		//-------------- Map Add Marker ------------------
		gmap_issdet = fmap_issdet.getMap();


		if (gmap_issdet == null){
			Log.e("GMAP","Gmap is null");
		} else {
			mMarker = gmap_issdet.addMarker(markerOptions);

			// make it non clickable 
			gmap_issdet.setOnMarkerClickListener(new OnMarkerClickListener() {
				@Override
				public boolean onMarkerClick(Marker arg0) {
					return true;
				}
			});

			gmap_issdet.moveCamera(CameraUpdateFactory.newLatLngZoom(mMarker.getPosition(),14));

			// ---- remove black margins ------------- 
			RelativeLayout.LayoutParams lparams =  (android.widget.RelativeLayout.LayoutParams) fmap_issdet.getView().getLayoutParams();
			lparams.setMargins(0, -70, 0, -100);
			fmap_issdet.getView().setLayoutParams(lparams);


			//-------------- My Zoom Buttons -----------------------
			gmap_issdet.getUiSettings().setZoomGesturesEnabled(true);
			gmap_issdet.getUiSettings().setZoomControlsEnabled(false);
			gmap_issdet.getUiSettings().setTiltGesturesEnabled(false);
			gmap_issdet.getUiSettings().setScrollGesturesEnabled(false);

			gmap_issdet.setOnCameraChangeListener(new OnCameraChangeListener() {
				@Override
				public void onCameraChange(CameraPosition arg0) {
					gmap_issdet.animateCamera(CameraUpdateFactory.newLatLng(mMarker.getPosition()));
				}
			});
		}


		//------------ reset colors ---------
		int CurrStat = mIssue._currentstatus;

		Colora(CurrStat);
		
	}

	/*     Colorize views based on issue status */
	private void Colora(int CurrStat){
		
		if (CurrStat == 1){
			tvStatus_ack.setTextColor(resources.getColor(R.color.graylight));
			vStatus_ack.setBackgroundColor(resources.getColor(R.color.graylight));
	
			tvStatus_cl.setTextColor(resources.getColor(R.color.graylight));
			vStatus_cl.setBackgroundColor(resources.getColor(R.color.graylight));
			
		}else if (CurrStat == 2){
			tvStatus_ack.setTextColor(resources.getColor(R.color.acknowy));
			vStatus_ack.setBackgroundColor(resources.getColor(R.color.acknowy));
			
			tvStatus_cl.setTextColor(resources.getColor(R.color.graylight));
			vStatus_cl.setBackgroundColor(resources.getColor(R.color.graylight));
		} else if (CurrStat == 3){
			tvStatus_ack.setTextColor(resources.getColor(R.color.acknowy));
			vStatus_ack.setBackgroundColor(resources.getColor(R.color.acknowy));
			
			tvStatus_cl.setTextColor(resources.getColor(R.color.cl));
			vStatus_cl.setBackgroundColor(resources.getColor(R.color.cl));
		}
		
	}
	
	//=============  Set Resources ============================
	private Resources setResources(){
		SharedPreferences mshPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		LangSTR          = mshPrefs.getString("LanguageAR", Constants_API.DefaultLanguage);
		UserNameSTR      = mshPrefs.getString("UserNameAR", "");
		PasswordSTR      = mshPrefs.getString("PasswordAR", "");
		IssuesNoSTR      = mshPrefs.getString("IssuesNoAR", "40");
		distanceData     = mshPrefs.getInt("distanceData", Constants_API.initRange);
		AuthFlag         = mshPrefs.getBoolean("AuthFlag", false);

		if (!Service_Data.HasInternet)
			AuthFlag = false;

		UserID_STR       = mshPrefs.getString("UserID_STR", "");


		Configuration conf = getResources().getConfiguration();
		conf.locale = new Locale(LangSTR.substring(0, 2)); //----- Convert Greek -> el ---------
		metrics = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
		return new Resources(getActivity().getAssets(), metrics, conf);
	}


	//---------- Flurry on Start - onStop ----------
	/** Flurry start */
	public void onStart(){
		super.onStart();
		SharedPreferences mshPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		boolean AnalyticsSW                = mshPrefs.getBoolean("AnalyticsSW", true);

		if (AnalyticsSW)
			FlurryAgent.onStartSession(ctx, Constants_API.Flurry_Key);
	}


	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onSaveInstanceState(android.os.Bundle)
	 */
	@Override
	public void onSaveInstanceState(Bundle outState) {

		//--------- Map ---------
		if (gmap_issdet!=null){
			if (FActivity_TabHost.IndexGroup==0 && fmap_issdet.isVisible()){
				  getChildFragmentManager().beginTransaction().remove(fmap_issdet ).commit();
			}
		}

		super.onSaveInstanceState(outState);
	}

	
	/** Map , Flurry stop */
	public void onPause(){
		super.onPause();

		if (vfrag_issue_details.getViewTreeObserver().isAlive()) 
			vfrag_issue_details.getViewTreeObserver().removeGlobalOnLayoutListener(globlist);
		
		bmI = null;
		isVisible = false;

		//----- FLurry --------
		SharedPreferences mshPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		boolean AnalyticsSW                = mshPrefs.getBoolean("AnalyticsSW", true);

		if (AnalyticsSW)
			FlurryAgent.onEndSession(ctx);
	}
	//----------------------------------------    

	/**
	 *   Download issue image asynchronously
	 * 
	 * @copyright   Copyright (C) 2012 - 2013 Information Technology Institute ITI-CERTH. All rights reserved.
	 * @license     GNU Affero General Public License version 3 or later; see LICENSE.txt
	 * @author      Dimitrios Ververidis for the Multimedia Group (http://mklab.iti.gr). 
	 *
	 */
	private static class ThumbnailTask_IssDetails extends AsyncTask<String, String , byte[] >{

		private String mIssueTPhotoSTR;
		private int mIssueID;

		public ThumbnailTask_IssDetails(String IssueTPhotoSTR, int IssueID) {
			mIssueTPhotoSTR = IssueTPhotoSTR;
			mIssueID        = IssueID;
		}

		@Override
		protected byte[] doInBackground(String... params) {
			String URL_STR = Constants_API.COM_Protocol + Constants_API.ServerSTR + Constants_API.remoteImages +  mIssueTPhotoSTR;
			byte[] bmBytes = Download_Data.Down_Image(URL_STR);

			return bmBytes;
		}

		@Override
		protected void onPostExecute(byte[] bmBytes) {
			bmI = My_System_Utils.LowMemBitmapDecoder(bmBytes);

			if (bmI!=null){
				imvFull.setImageBitmap(bmI);  
				imvFull.setScaleType(ScaleType.CENTER_CROP);
				imvFull.setClickable(true);
			} else {
				imvFull.setClickable(false);
			}

			try {
				dbHandler.addUpdIssuePic(mIssueID, bmBytes);
			} catch (IOException e) {
				Log.e("ThumbnailTask_IssDetails" , "Can not insert to SQLITE db");
			}


			//------ Update also the thumb db ----
			Bitmap bmIThumb = Bitmap.createScaledBitmap(bmI, 120, 120, false);

			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			bmIThumb.compress(Bitmap.CompressFormat.JPEG, 95, stream);
			
			try {
				dbHandler.addUpdIssueThumb(mIssueID, stream.toByteArray());
			} catch (IOException e) {
				Log.e("ThumbnailTask_IssDetails" , "Can not insert to SQLITE db");
			}

			super.onPostExecute(null);
		}
	}
	

	/**
	 *    Download number of votes asynchronously
	 *     
	 * @copyright   Copyright (C) 2012 - 2013 Information Technology Institute ITI-CERTH. All rights reserved.
	 * @license     GNU Affero General Public License version 3 or later; see LICENSE.txt
	 * @author      Dimitrios Ververidis for the Multimedia Group (http://mklab.iti.gr). 
	 *
	 */
	private static class AsynchTaskVote extends AsyncTask<String, String, String>{

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		//-------- Asynch ---------
		@Override
		protected String doInBackground(String... params) {

			Upload_Data.SendVote(Integer.parseInt(UserID_STR), issueId,  UserNameSTR, PasswordSTR);

			DatabaseHandler dbHandler = new DatabaseHandler(ctx);
			Service_Data.mIssueL =  dbHandler.getAllIssues();

			dbHandler.addUpdIssues(Service_Location.locUser.getLongitude(),   // Update Issues votes 
					Service_Location.locUser.getLatitude(), 
					distanceData, Integer.parseInt(IssuesNoSTR),ctx);

			dbHandler.AddUpdUserVotes(UserNameSTR, PasswordSTR, ctx);
			dbHandler.db.close();

			return null;
		}

		// UPDATE THE GUI	
		@Override
		protected void onPostExecute(String result) {

			//-------- Update IssuesList (in case of + vote)-------
			TextView tvVotes = (TextView ) vfrag_issue_details.findViewById(R.id.textViewVotes);
			tvVotes.setText( Integer.toString(mIssue._votes + 1) + " " + resources.getString(R.string.peoplevoted));

			Button btVote = (Button) vfrag_issue_details.findViewById(R.id.buttonVote);
			btVote.setEnabled(false);
			btVote.setText(resources.getString(R.string.AlreadyVoted));

			super.onPostExecute(result);
		}
	}

}
