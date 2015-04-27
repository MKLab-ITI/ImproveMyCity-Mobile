// Issues_ListAdapter
package com.mk4droid.IMC_Core;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mk4droid.IMC_Activities.FActivity_TabHost;
import com.mk4droid.IMC_Activities.Fragment_List;
import com.mk4droid.IMC_Constructors.IssueListItem;
import com.mk4droid.IMC_Constructors.IssuePic;
import com.mk4droid.IMC_Services.Download_Data;
import com.mk4droid.IMC_Services.Service_Data;
import com.mk4droid.IMC_Store.Constants_API;
import com.mk4droid.IMC_Utils.My_Date_Utils;
import com.mk4droid.IMC_Utils.My_System_Utils;
import com.mk4droid.IMCity_PackDemo.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;


/**
 * Adapter to view all issues in a List. 
 * 
 * @copyright   Copyright (C) 2012 - 2015 Information Technology Institute ITI-CERTH. All rights reserved.
 * @license     GNU Affero General Public License version 3 or later; see LICENSE.txt
 * @author      Dimitrios Ververidis for the Multimedia Group (http://mklab.iti.gr). 
 * 
 */
public class Issues_ListAdapter extends ArrayAdapter<IssueListItem> {

	static Context ctx;
	int layoutResourceId;    
	ArrayList<IssueListItem> data;
	Bitmap bm_noimage; 
    static int NIssues = 0;
    static Bitmap[] bmArr;
	
    /**
     * Constructor 
     * 
     * @param context is the caller context
     * @param layoutResourceId the id of the layout of the caller
     * @param data_in the issues as an ArrayList of IssueListItem(s) 
     */
	public Issues_ListAdapter(Context context, int layoutResourceId, ArrayList<IssueListItem> data_in) {
		super(context, layoutResourceId, data_in);
		this.layoutResourceId = layoutResourceId;
		this.ctx = context;
		this.data = data_in;
		NIssues = data.size();
		bmArr = new Bitmap[NIssues];
		bm_noimage =  BitmapFactory.decodeResource(FActivity_TabHost.resources, R.drawable.ic_no_image);
	}

	/**
	 * 
	 *    GET VIEW 
	 *    @position refers to the currently visible item of the list (depending on the screen size usually varies between 3 to 6)
	 *    @convertView is the previous item view that should be updated
	 *    @parent contains all items visible 
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		IssueHolder holder = null;


		if(row == null){
			LayoutInflater inflater = ((Activity)ctx).getLayoutInflater();
			row = inflater.inflate(layoutResourceId, parent, false);

			holder = new IssueHolder();
			holder.txtNo         = (TextView)row.findViewById(R.id.txtIssNo);
			holder.txtTitle      = (TextView)row.findViewById(R.id.txtTitle);
			holder.txtState      = (TextView)row.findViewById(R.id.txtState);
			holder.txtAddress    = (TextView)row.findViewById(R.id.txtAddress);
			holder.txtReported   = (TextView)row.findViewById(R.id.txtReported);
			holder.txtVotes      = (TextView)row.findViewById(R.id.txtVotes);
			holder.imgIcon       = (ImageView)row.findViewById(R.id.imgIcon);
			holder.llimage       = (LinearLayout)row.findViewById(R.id.llimage);
			holder.lltitle       = (LinearLayout)row.findViewById(R.id.lltitle);
			holder.position      = position;

			row.setTag(holder);
		} else {
			holder = (IssueHolder)row.getTag();
			holder.position      = position;
		}


		IssueListItem litem = data.get(position);
		
		//---------------- Bitmap -----------
		if (bmArr[position]==null){
			if (litem._urlphoto!=null && litem._urlphoto.length() > 0 && !litem._urlphoto.equals("null") ){

				int IssueID = Integer.parseInt( litem._id.substring(1,litem._id.length()) );
				
				if (Fragment_List.lvIssues.isShown()){
					try {
						new ThumbnailTask(position, holder, litem._urlphoto, IssueID).execute();
					} catch (Exception e){
						Log.e("IMC ILA", "Can not put thumbnail to listview");
					}
				}
				
			} else {
				holder.imgIcon.setImageBitmap(bm_noimage);
			}
		} else {
			holder.imgIcon.setImageBitmap(bmArr[position]);
		}

		//-------- Background -----------
		if (position % 2 ==0){
			row.setBackgroundColor(Color.argb(100, 240, 240, 240));
		} else {
			row.setBackgroundColor(Color.argb(200, 255, 255, 255));
		}

		//------------- Number ---------------
		holder.txtNo.setText(litem._id.replace("#", ""));

		//------------ Title -------------------
		holder.txtTitle.setText(litem._title);   

		//------------ Status ---------------------
		if (litem._currstate==1){
			holder.txtState.setText(Fragment_List.resources.getString(R.string.OpenIssue));
			holder.txtState.setTextColor(Fragment_List.resources.getColor(R.color.op));
		}else if (litem._currstate==2){
			holder.txtState.setText(Fragment_List.resources.getString(R.string.AckIssue));
			holder.txtState.setTextColor(Fragment_List.resources.getColor(R.color.acknowy));
		}else if (litem._currstate==3){
			holder.txtState.setText(Fragment_List.resources.getString(R.string.ClosedIssue));
			holder.txtState.setTextColor(Fragment_List.resources.getColor(R.color.cl));
		}

		holder.txtState.setBackgroundColor(Fragment_List.resources.getColor(R.color.graylight));
		
		//---------------- Address ----------------
		int addressend = litem._address.indexOf(",");
		String straddr = litem._address;
		if (addressend!=-1)
			straddr = litem._address.substring(0, addressend);

		holder.txtAddress.setText(straddr);

		//------------- Reported by Author and XX days ago ----------
		String TimeStampRep = litem._reported;
		TimeStampRep        = TimeStampRep.replace("-", "/");

		SharedPreferences mshPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		String LangSTR = mshPrefs.getString("LanguageAR", Constants_API.DefaultLanguage);

		holder.txtReported.setText(My_Date_Utils.SubtractDate(TimeStampRep, LangSTR)); 

		// ------------------------ Votes ------------------------------------
		holder.txtVotes.setText( litem._votes);

		return row;
	}


	/**
	 *  Asynchronous task to download issues images
	 * 
	 * @copyright   Copyright (C) 2012 - 2015 Information Technology Institute ITI-CERTH. All rights reserved.
	 * @license     GNU Affero General Public License version 3 or later; see LICENSE.txt
	 * @author      Dimitrios Ververidis for the Multimedia Group (http://mklab.iti.gr). 
	 *
	 */
	private static class ThumbnailTask extends AsyncTask<String, String , byte[] >{
		private int mPosition;
		private IssueHolder mHolder;
		private String mIssueTPhotoSTR;
		private int mIssueID;
		IssuePic mIssueThumb;

		/**
		 * Constructor
		 * 
		 * @param position is the the index of issue in the list (global position)
		 * @param holder is the object that contains data for a single item of the list
		 * @param IssueTPhotoSTR the URL of the image
		 * @param IssueID
		 */
		public ThumbnailTask(int position, IssueHolder holder, String IssueTPhotoSTR, int IssueID) {

			mPosition       = position;
			mHolder         = holder;
			mIssueTPhotoSTR = IssueTPhotoSTR;
			mIssueID        = IssueID;
			mIssueThumb     = Service_Data.dbHandler.getIssueThumb(mIssueID);
		}

		/* (non-Javadoc)
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		@Override
		protected byte[] doInBackground(String... params) {

			byte[] bmBytesFull = null;			

			// NOT EXISTS IN DB = GET FROM INTERNET and add to DB
			if (mIssueThumb._id == -1)    
				if (Service_Data.HasInternet ){

					int N_PString = mIssueTPhotoSTR.length();
					String EXT = "";
					if (N_PString > 0)
						EXT = mIssueTPhotoSTR.substring(N_PString-3, N_PString);

					String ImPath = Constants_API.COM_Protocol + Constants_API.ServerSTR + Constants_API.remoteImages +  mIssueTPhotoSTR;

					ImPath = ImPath.replace("thumbs/", "");

					if (EXT.equalsIgnoreCase("jpg"))
						bmBytesFull = Download_Data.Down_Image(ImPath);
				} 

			return bmBytesFull;
		}


		/* (non-Javadoc)
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(byte[] bmBytesFull2) {

			Bitmap bmThumb = null;
			byte[] bmBytesThumb = null;

				
			if (mIssueThumb._id == -1) {    
				// FULL PIC
				try {
					Service_Data.dbHandler.addUpdIssuePic(mIssueID, bmBytesFull2);
				} catch (IOException e) {
					Log.e("ThumbnailTask_IssDetails" , "Can not insert to SQLITE db");
				}

				Log.d(Constants_API.TAG, "IssListAdapt " + mIssueTPhotoSTR );

				if (bmBytesFull2 != null){
					Bitmap bmFull = My_System_Utils.LowMemBitmapDecoder(bmBytesFull2, ctx);

					// THUMBNAIL
					bmThumb= Bitmap.createScaledBitmap(bmFull, 120, 120, false);
					ByteArrayOutputStream stream = new ByteArrayOutputStream();
					bmThumb.compress(Bitmap.CompressFormat.JPEG, 95, stream);
					bmBytesThumb = stream.toByteArray();

					try {
						Service_Data.dbHandler.addUpdIssueThumb(mIssueID, bmBytesThumb);
					} catch (IOException e) {
						Log.e("ThumbnailTask_IssDetails" , "Can not insert to SQLITE db");
					}
				}
			} else { // Exists in DB: GET FROM LOCAL DB
					
					bmBytesThumb = mIssueThumb._IssuePicData;
					bmThumb = My_System_Utils.LowMemBitmapDecoder( bmBytesThumb, ctx );
					bmArr[mPosition] = bmThumb; 
			}

			if (mHolder.position == mPosition && bmArr[mPosition]!=null) 
				mHolder.imgIcon.setImageBitmap(bmArr[mPosition]);

			super.onPostExecute(bmBytesThumb);
		}
	}

	/**  
	 * Holder holds the widgets as defined in the custom item layout 
	 * 
	 * */
	static class IssueHolder{
		ImageView imgIcon;
		TextView txtNo;
		TextView txtTitle;
		TextView txtState;
		TextView txtAddress;
		TextView txtReported;
		TextView txtVotes;
		LinearLayout llimage;
		LinearLayout lltitle;

		int position;
	}
}