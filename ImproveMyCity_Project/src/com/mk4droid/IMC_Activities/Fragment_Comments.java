/**
 * @copyright   Copyright (C) 2012 - 2013 Information Technology Institute ITI-CERTH. All rights reserved.
 * @license     GNU Affero General Public License version 3 or later; see LICENSE.txt
 * @author      Dimitrios Ververidis for the Multimedia Group (http://mklab.iti.gr). 
 */
package com.mk4droid.IMC_Activities;

import java.util.ArrayList;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mk4droid.IMC_Constructors.Comment;
import com.mk4droid.IMC_Services.InternetConnCheck;
import com.mk4droid.IMC_Services.Service_Data;
import com.mk4droid.IMC_Services.Upload_Data;
import com.mk4droid.IMC_Store.Constants_API;
import com.mk4droid.IMC_Store.Phptasks;
import com.mk4droid.IMC_Utils.My_Date_Utils;
import com.mk4droid.IMC_Utils.RestCaller;
import com.mk4droid.IMCity_PackDemo.R;

/**
 *  Fragment to show comments
 * 
 * @copyright   Copyright (C) 2012 - 2013 Information Technology Institute ITI-CERTH. All rights reserved.
 * @license     GNU Affero General Public License version 3 or later; see LICENSE.txt
 * @author      Dimitrios Ververidis for the Multimedia Group (http://mklab.iti.gr). 
 * 
 */
public class Fragment_Comments extends Fragment {

	static int issueId;
	static TableLayout TabComments;
	static TextView tvNoCom;

	static String LangSTR = "en", UserNameSTR, PasswordSTR, UserID_STR, CommSTR = "";

	String IssuesNoSTR;
	boolean AuthFlag;
	static Resources resources;
	static Context ctx;
	static LayoutParams lparams;
	static DisplayMetrics metrics;

	static int NComments;

	/**  The view of this fragment  */
	public static View vfrag_comments;

	/** This Fragment */
	public static Fragment mfrag_comments;

	String issueTitle  = "";

	/** On Create this fragment */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		issueId = getArguments().getInt("issueId"); // id of the issue
		issueTitle = getArguments().getString("issueTitle"); // id of the issue
	}


	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		vfrag_comments = inflater.inflate(R.layout.fragment_comments, container, false);

		mfrag_comments = this;
		
		ctx = vfrag_comments.getContext();
		resources = setResources();

		//------------ Title ---------
		TextView tvTitle = (TextView) vfrag_comments.findViewById(R.id.tv_Comments_GrandTitle);
		tvTitle.setText(issueTitle);

		//------- no comments tv--------
		tvNoCom = (TextView) vfrag_comments.findViewById(R.id.tvnoComments);

		// Comments added from a new thread so as to avoid delays  
		TabComments = (TableLayout)vfrag_comments.findViewById(R.id.tlComments);
		lparams = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);

		//-------- Enable send Comment button
		Button btComment =  (Button) vfrag_comments.findViewById(R.id.btAddComment);

		// -------- Enable Commenting EditText 
		EditText etComment = (EditText) vfrag_comments.findViewById(R.id.etComment);       	  

		if (AuthFlag && InternetConnCheck.getInstance(ctx).isOnline(ctx)){

			etComment.setEnabled(true);

			// Make icon drawable more vivid
			Drawable dr = resources.getDrawable(R.drawable.ic_send_holo_light);
			dr.setColorFilter(resources.getColor(R.color.orange), android.graphics.PorterDuff.Mode.SRC_ATOP);
			btComment.setCompoundDrawablesWithIntrinsicBounds(null,  dr, null, null);
			btComment.setCompoundDrawablePadding(-40);
		} else {
			etComment.setEnabled(false);
		}


		btComment.setOnClickListener(new View.OnClickListener() {

			public void onClick(View arg0) {
				//================ SEND COMMENT TO DB ===============
				EditText etComment = (EditText) vfrag_comments.findViewById(R.id.etComment);
				CommSTR = etComment.getText().toString();

				if ( CommSTR.length()>0 && InternetConnCheck.getInstance(ctx).isOnline(ctx) && AuthFlag){
					new AsynchTask_SendComment().execute();
				} else if (!InternetConnCheck.getInstance(ctx).isOnline(ctx)) {
					Toast.makeText(ctx, resources.getString(R.string.NoInternet), Toast.LENGTH_SHORT).show();	
				} else if (!AuthFlag){
					Toast.makeText(ctx, resources.getString(R.string.OnlyRegistered), Toast.LENGTH_SHORT).show();					
				}  
				//===================================================
			}
		});

		return vfrag_comments;
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onPause()
	 */
	@Override
	public void onPause() {
		NComments = 0;
		super.onPause();
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onResume()
	 */
	@Override
	public void onResume() {

		if (InternetConnCheck.getInstance(ctx).isOnline(ctx))
			new AsynchTask_ReceiveComments(issueId).execute();
		else 
			Toast.makeText(ctx, resources.getString(R.string.NoInternet), Toast.LENGTH_SHORT).show();

		super.onResume();
	}

	//=============  Set Resources ( Load preferences ) =============
	private Resources setResources(){
		SharedPreferences mshPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		LangSTR          = mshPrefs.getString("LanguageAR", Constants_API.DefaultLanguage);
		UserNameSTR      = mshPrefs.getString("UserNameAR", "");
		PasswordSTR      = mshPrefs.getString("PasswordAR", "");
		IssuesNoSTR      = mshPrefs.getString("IssuesNoAR", "40");
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


	/*
	 * Send comments asynchronous task
	 *      
	 * @copyright   Copyright (C) 2012 - 2013 Information Technology Institute ITI-CERTH. All rights reserved.
	 * @license     GNU Affero General Public License version 3 or later; see LICENSE.txt
	 * @author      Dimitrios Ververidis for the Multimedia Group (http://mklab.iti.gr). 
	 *
	 */
	private static class AsynchTask_SendComment extends AsyncTask<String, String , Boolean >{

		public AsynchTask_SendComment() {	}

		@Override
		protected Boolean doInBackground(String... params) {
			boolean succ = false;

			succ = Upload_Data.SendCommentStreaming(issueId, Integer.parseInt(UserID_STR), CommSTR, FActivity_TabHost.ctx,
					UserNameSTR, PasswordSTR);

			return succ;
		}


		/* (non-Javadoc)
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(Boolean success) {

			EditText etComment = (EditText) vfrag_comments.findViewById(R.id.etComment);

			if (success){
				etComment.clearFocus();
				etComment.setText("");
				InputMethodManager imm = (InputMethodManager)ctx.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(etComment.getWindowToken(), 0);
				new AsynchTask_ReceiveComments(issueId).execute();
			} else {
				Toast.makeText(ctx, resources.getString(R.string.FailMes), Toast.LENGTH_LONG).show();
			}
			super.onPostExecute(success);
		}
	}

	/*
	 *   Receive comment asynchronous task  
	 *  
	 * @copyright   Copyright (C) 2012 - 2013 Information Technology Institute ITI-CERTH. All rights reserved.
	 * @license     GNU Affero General Public License version 3 or later; see LICENSE.txt
	 * @author      Dimitrios Ververidis for the Multimedia Group (http://mklab.iti.gr). 
	 *
	 */
	private static class AsynchTask_ReceiveComments extends AsyncTask<String, String , String >{

		private int mIssueID;
		ArrayList<Comment> mCommentL = new ArrayList<Comment>();

		public AsynchTask_ReceiveComments( int IssueID ) {
			mIssueID = IssueID;
		}


		@Override
		protected String doInBackground(String... params) {
			RestCaller rc = new RestCaller();	
			String response =  rc.now(
					Constants_API.COM_Protocol +Constants_API.ServerSTR + Constants_API.phpExec,
					"GET",  
					new String[]{"option","com_improvemycity",
							"task", Phptasks.TASK_GET_ISSUE,
							"format","json",
							"issueId", Integer.toString(mIssueID),
							"showComments", "1"}, "UTF-8", "Download_CommentsByIssueID");
			return response;
		}


		@Override
		protected void onPostExecute(String response) {

			try{
				JSONObject jOIssue   = new JSONObject(response);

				JSONArray jArray = new JSONArray(jOIssue.getString("discussion"));
				NComments = jArray.length();

				//----- Clear previous comments ------
				if (NComments>0)
					TabComments.removeAllViews();


				for(int i=0; i<NComments; i++){
					JSONObject jO = jArray.getJSONObject(i);
					int id          = jO.getInt("id"); 
					int issueid     = jO.getInt("improvemycityid"); 
					int userid      = jO.getInt("userid"); 
					String created     = jO.getString("created"); 
					String description = jO.getString("description"); 
					String fullname    = jO.getString("fullname");

					Comment mComment = new Comment(id, issueid, userid,	My_Date_Utils.ConvertToDate(created), description, fullname);

					mCommentL.add(mComment);

					//------------ Inflate Child --------
					final View ChildComm = LayoutInflater.from(FActivity_TabHost.ctx).inflate(R.layout.issue_comment_layout,null);  

					
					
					//----------- Linear Layout container  ---------
					LinearLayout llsinglecomment = (LinearLayout) ChildComm.findViewById(R.id.llsinglecomment);
					if (i%2==0)
						llsinglecomment.setBackgroundColor(Color.argb(100, 255, 255, 255));
					else 
						llsinglecomment.setBackgroundColor(Color.argb(100, 190, 190, 190));

					//-------------- Set Color of comment separator --------------
					View v_comment_sep = ChildComm.findViewById(R.id.v_comment_line);
					v_comment_sep.setBackgroundColor(resources.getColor(R.color.graylight));
							
					//----------- Set CommentId in respective TextView  ---------------------
					TextView tvCommentId = (TextView) ChildComm.findViewById(R.id.tvCommentId);
					tvCommentId.setText(Integer.toString(NComments - i));

					//----------------- Author --------
					TextView CommAuthor = (TextView) ChildComm.findViewById(R.id.tvAuthorComment);
					CommAuthor.setText(mComment._username);

					//--------- Date  ----------------------------
					String DateCreated_STR = My_Date_Utils.DateToString(  mComment._created );
					DateCreated_STR = DateCreated_STR.replace("-", "/");
					DateCreated_STR = My_Date_Utils.SubtractDate(DateCreated_STR, LangSTR);

					TextView CommentDate = (TextView) ChildComm.findViewById(R.id.tvCommentDate);
					CommentDate.setText(DateCreated_STR + " " + resources.getString(R.string.ago));

					//--------------- Content -----------
					TextView CommContent = (TextView) ChildComm.findViewById(R.id.tvContentComment);
					CommContent.setText(mComment._description);

					//-------------- Add View -----------
					TabComments.addView(ChildComm, lparams);
				}

				if (NComments==0){
					tvNoCom.setVisibility(View.VISIBLE);
				} else {
					tvNoCom.setVisibility(View.GONE);
				}
				
				ProgressBar pbar_comments_down = (ProgressBar) vfrag_comments.findViewById(R.id.pbar_comments_down);
				pbar_comments_down.setVisibility(View.GONE);

			}catch(JSONException e){
				Log.e(Constants_API.TAG, "Error parsing data "+e.toString());
			}                  	
			super.onPostExecute(response);
		}
	}
}
