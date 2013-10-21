/** DatabaseHandler */
package com.mk4droid.IMC_Services;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils.InsertHelper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ProgressBar;

import com.mk4droid.IMC_Activities.Fragment_Map;
import com.mk4droid.IMC_Constructors.Category;
import com.mk4droid.IMC_Constructors.Issue;
import com.mk4droid.IMC_Constructors.IssuePic;
import com.mk4droid.IMC_Constructors.VersionDB;
import com.mk4droid.IMC_Store.Constants_API;

//======================= DatabaseHandler =================================
/**
 * Handles all operations for storing locally a subset of the remote MySQL of IMC to local SQLite
 * 
 * @copyright   Copyright (C) 2012 - 2013 Information Technology Institute ITI-CERTH. All rights reserved.
 * @license     GNU Affero General Public License version 3 or later; see LICENSE.txt
 * @author      Dimitrios Ververidis for the Multimedia Group (http://mklab.iti.gr). 
 *
 */

public class DatabaseHandler extends SQLiteOpenHelper {
	
	String TAG_Class = getClass().getName();
	
	/** Name of the local database */
	public static final String DATABASE_NAME = "ImproveMyCity";

	/** Table of Categories */ 	       
	public static final String TABLE_Categories      = "tblCategories";
	
	/** Table of Issues */             
	public static final String TABLE_Issues          = "tblIssues";
	
	/** Table of Issues Picture */     
	public static final String TABLE_IssuesPics      = "tblIssuesPics";
	
	/** Table of Issues Thumbnails */  
	public static final String TABLE_IssuesThumbs    = "tblIssuesThumbs";
	
	/** Table holding the current version of MySQL downloaded */      
	public static final String TABLE_Version         = "tblVersion";
	
	/** Table of Categories version. Categories are updated from MySQL with a different versioning than issues. */ 
	public static final String TABLE_CategVersion    = "tblCategVersion";
	
	/** Table of Votes that were downloaded locally from MySQL */              
	public static final String TABLE_Votes           = "tblVotes";
	
	/** Local SQLite version. Having multiple version of SQLite dbs. Only for debugging. */ 
	public static final int DATABASE_VERSION = 1;
	
	/** Progress bar for downloading */
	public static ProgressBar pbgeneral; 
	
	// ======= Comments Table Columns names ================
	String KEY_CommentID         ="id";
	String KEY_IssueIDComments   ="improvemycityid";
	String KEY_UserID            ="userid";
	String KEY_CommentCreated    ="created";
	String KEY_CommentDescription="description";
	
	// ======= Votes Table Columns names ================
	String KEY_VoteID        ="id";
	String KEY_IssueIDVotes  ="improvemycityid";
	String KEY_Username    ="username";
	
	// ======= Issue Pics Table Columns names ================
	 String KEY_IssueID         ="issueid";
	 String KEY_IssuePicData    ="issuepicdata";

	// ======= Issue Thumbs Table Columns names ================
	// String KEY_IssueID         ="issueid";  already defined
	 String KEY_IssueThumbData    ="issuethumbdata";
	
	// ======= Issue Categories Table Columns names ================
	 String KEY_CatID       = "categoryid";
	 String KEY_CatName     = "categoryName";
	 String KEY_CatIcon     = "categoryIcon";
	 String KEY_CatLevel    = "categoryLevel";
	 String KEY_CatParentID = "catParentID";
	 String KEY_CatVisible  = "catVisible";
		
	//============= Issue Table Columns names=================
	// String KEY_IssueID         ="issueid";  already defined    
	 String KEY_Title      ="duration";        
	// String KEY_CatID  
	 String KEY_Lat        ="latitude";
	 String KEY_Long       ="longitude"; 
	 String KEY_Description="description";
	 String KEY_Photo      ="photo";
	 String KEY_Address    ="address";
	
	 String KEY_Votes    ="votes";
	 String KEY_CurrStat ="currentstatus";
	 String KEY_Reported ="reported";
	 String KEY_Ack      ="ack";
	 String KEY_Closed   ="closed";
	// String KEY_UserID   ="userid"; already defined
	 String KEY_Ordering ="ordering";
	 String KEY_Params ="params";
	 String KEY_State    ="state";
	 String KEY_Lang    ="language";
	 String KEY_Hits     ="hits";
	// String KEY_Username ="username"; already defined

    //=========== Version Table================================
	 String KEY_VersionID = "id";
	 String KEY_VersionTimestamp = "timestamp";
	
	/** Holds the local database */
	public SQLiteDatabase db; 
	
	/** Open the database and assign a handler for operations */
	public DatabaseHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		db = this.getWritableDatabase();
	}

	//================= onCreate  ==========================================
	/** Create tables */
	@Override
	public void onCreate(SQLiteDatabase db) {
		
		String CREATE_Categ_TABLE = "CREATE TABLE " + TABLE_Categories + "("
				+ KEY_CatID       + " INTEGER PRIMARY KEY," 
				+ KEY_CatName     + " TEXT,"
				+ KEY_CatIcon     + " BLOB,"
				+ KEY_CatLevel    + " INTEGER,"
				+ KEY_CatParentID + " INTEGER,"
				+ KEY_CatVisible  + " INTEGER)";
		
		db.execSQL(CREATE_Categ_TABLE);
		
		
		String CREATE_Issues_TABLE = "CREATE TABLE " + TABLE_Issues + "("
		+ KEY_IssueID    + " INTEGER PRIMARY KEY,"
		+ KEY_Title + " TEXT,"
		+ KEY_CatID + " INTEGER,"
		+ KEY_Lat   + " TEXT," 
		+ KEY_Long  + " TEXT," 
		+ KEY_Description + " TEXT,"
		+ KEY_Photo       + " TEXT,"
		+ KEY_Address     + " TEXT," 
		+ KEY_Votes       + " INTEGER,"
		+ KEY_CurrStat    + " INTEGER,"    
		+ KEY_Reported    + " TEXT,"
		+ KEY_Ack         + " TEXT,"
		+ KEY_Closed      + " TEXT,"
		+ KEY_UserID      + " INTEGER," 
		+ KEY_Ordering    + " INTEGER,"
		+ KEY_Params      + " TEXT,"
		+ KEY_State       + " INTEGER,"
		+ KEY_Lang        + " TEXT,"
		+ KEY_Hits        + " INTEGER,"
		+ KEY_Username    + " TEXT)";
			
		db.execSQL(CREATE_Issues_TABLE);
		
		
		String CREATE_IssuesPics_TABLE = "CREATE TABLE " + TABLE_IssuesPics + "("
		+ KEY_IssueID   + " INTEGER PRIMARY KEY," 
		+ KEY_IssuePicData + " BLOB )";


        db.execSQL(CREATE_IssuesPics_TABLE);
		
        
		String CREATE_IssuesThumbs_TABLE = "CREATE TABLE " + TABLE_IssuesThumbs + "("
		+ KEY_IssueID   + " INTEGER PRIMARY KEY," 
		+ KEY_IssueThumbData + " BLOB )";


        db.execSQL(CREATE_IssuesThumbs_TABLE);
        
		String CREATE_Version_TABLE = "CREATE TABLE " + TABLE_Version + "("
		+ KEY_VersionID   + " INTEGER PRIMARY KEY," 
		+ KEY_VersionTimestamp + " TEXT );";
		
		db.execSQL(CREATE_Version_TABLE);
		
		String CREATE_CategVersion_TABLE = "CREATE TABLE " + TABLE_CategVersion + "("
		+ KEY_VersionID   + " INTEGER PRIMARY KEY," 
		+ KEY_VersionTimestamp + " TEXT );";
		
        db.execSQL(CREATE_CategVersion_TABLE);
        

        
        String CREATE_Votes_TABLE = "CREATE TABLE " + TABLE_Votes + "("
		+ KEY_VoteID         + " INTEGER PRIMARY KEY,"
		+ KEY_IssueIDVotes   + " INTEGER);";
	

        db.execSQL(CREATE_Votes_TABLE);
	}

	//================= onUpgrade  ==========================================
	/** OnUpgrade delete any previous tables and create them again */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Drop older table if existed
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_Categories);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_Issues);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_IssuesPics);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_IssuesThumbs);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_Version);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_CategVersion);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_Votes);
		
		// Create tables again
		onCreate(db);
	}

	//================= AddUpdUserVotes  ==========================================
    /**
     * Download and Update locally table of votes for the current user so as not to be able to vote multiple times for an issue. 
     * 
     * @param UserNameSTR
     * @param PasswordSTR
     * @return
     */
	public int AddUpdUserVotes(String UserNameSTR, String PasswordSTR, Context ctx){
		if (UserNameSTR.length() == 0)
			return 0;
		
		if (!db.isOpen())
    		db = this.getWritableDatabase();
			
		db.execSQL("DELETE FROM " + TABLE_Votes);
		String response = Download_Data.Download_UserVotes( UserNameSTR, PasswordSTR);
		
		if (response == null)
			return 0;
		
    	try {
    		//-------- Get Info from HTTP post --------
			JSONArray jArr =  new JSONArray(response);
			int NVotes         = jArr.length(); 
			Log.e("UPD", "Votes");
			for (int i=0; i<NVotes; i++){
				
                float prog = 100*((float) (i+1)) / ((float) NVotes); 
                ctx.sendBroadcast(new Intent("android.intent.action.MAIN").putExtra("progressval", (int) (83 + prog*0.17)));
                
				JSONArray jArrCurr = new JSONArray(jArr.get(i).toString());
				
				int VoteID           = jArrCurr.getInt(0); //"id");
				int IssueID          = jArrCurr.getInt(1); //Int("improvemycityid");
				
     			//------------ See if exists in mySQL ---------
     	        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_Votes + " WHERE "+ 
     	        		KEY_IssueIDVotes + "=" + Integer.toString(IssueID), null);
	        
    	        //-------------- Prepare values for add or upd ----------
    	        ContentValues values = new ContentValues();
    	        values.put(KEY_VoteID      ,       Integer.toString(VoteID));
      			values.put(KEY_IssueIDVotes,       Integer.toString(IssueID));
      			
      			//---------- Insert Vote to SQLite --------------
      			if (!cursor.moveToFirst())
      				db.insert(TABLE_Votes, null, values); 
      			
    			cursor.close();
			}
    	} catch (JSONException e1) {
			e1.printStackTrace();
		}  
    	
    	return response.getBytes().length;
	}
	
	//================= addUpdIssues  ==========================================
	/**
	 *   Download and update the local table of Issues. 
	 *   
	 * @param CurrLong  Download center position longitude
	 * @param CurrLat   Download center position latitude
	 * @param distance  Range around center position to download
	 * @param IssuesNo  Max number of issues to download
	 * @return downloaded bytes number 
	 */
	public int addUpdIssues(double CurrLong, double CurrLat, int distance, int IssuesNo, Context ctx){
		
		// Make borders of Long and Lat based on distance 
		double x0up   = (CurrLong + (distance*0.0115)/1000);
		double x0down = (CurrLong - (distance*0.0115)/1000);

		double y0up   = (CurrLat + (distance*0.0090)/1000);
		double y0down = (CurrLat - (distance*0.0090)/1000);
        
		// Download
		String response = Download_Data.Download_Issues(x0down, x0up, y0down, y0up, IssuesNo);		
		
		int response_BytesLength = response.getBytes().length;
		
		if (response==null || response_BytesLength == 0)
			return 0;
		
    	if (!db.isOpen())
    		db = this.getWritableDatabase();

    	// Delete *
    	db.execSQL("DELETE FROM " + TABLE_Issues);
    	
    	// Insert
    	try {
			JSONArray jArrIssues =  new JSONArray(response);
			
        	int NIssues = jArrIssues.length();
        				
			//--------- Create Helpers for Local db -----------------
			final InsertHelper iHelpI = new InsertHelper(db, TABLE_Issues);
			
			int c1 = iHelpI.getColumnIndex(KEY_IssueID);        
			int c2 = iHelpI.getColumnIndex(KEY_Title );
			int c3 = iHelpI.getColumnIndex(KEY_CatID  );
			int c4 = iHelpI.getColumnIndex(KEY_Lat );
			int c5 = iHelpI.getColumnIndex(KEY_Long);
			
			int c6 = iHelpI.getColumnIndex(KEY_Description);              
			int c7 = iHelpI.getColumnIndex(KEY_Photo); 
			int c8 = iHelpI.getColumnIndex(KEY_Address);      
			int c9 = iHelpI.getColumnIndex(KEY_Votes); 
			
			int c10 = iHelpI.getColumnIndex(KEY_CurrStat);
			int c11 = iHelpI.getColumnIndex(KEY_Reported); 
			int c12 = iHelpI.getColumnIndex(KEY_Ack); 		
			int c13 = iHelpI.getColumnIndex(KEY_Closed);
			
			int c14 = iHelpI.getColumnIndex(KEY_UserID);  
			int c15 = iHelpI.getColumnIndex(KEY_Ordering);
			
			int c16 = iHelpI.getColumnIndex(KEY_Params);
			int c17 = iHelpI.getColumnIndex(KEY_State);   
			int c18 = iHelpI.getColumnIndex(KEY_Lang);
			int c19 = iHelpI.getColumnIndex(KEY_Hits);    
			int c20 = iHelpI.getColumnIndex(KEY_Username);

			
			try
			{
				db.beginTransaction();
				
				 Log.e("UPD", "Issues");
				
				for (int i=0; i<NIssues; i++){
										
					float prog = 67  + 16*((float) (i+1)) / ((float) NIssues); 
					
					ctx.sendBroadcast(new Intent("android.intent.action.MAIN").putExtra("progressval", prog));
					
					int    IssueID        = jArrIssues.getJSONArray(i).getInt(0); // "id"
					String IssueTitle     = jArrIssues.getJSONArray(i).getString(1).trim(); // "title"
					
					int    CatID          = jArrIssues.getJSONArray(i).getInt(2);	// "catid"
					double Latitude       = jArrIssues.getJSONArray(i).getDouble(3);// "latitude"
					double Longitude      = jArrIssues.getJSONArray(i).getDouble(4);// "longitude"
					
			        String Description    = jArrIssues.getJSONArray(i).getString(5).trim(); // "description"
			        String Photo          = jArrIssues.getJSONArray(i).getString(6);        // "photo"
			        String Address        = jArrIssues.getJSONArray(i).getString(7).trim(); // "address"
			        int    votes          = jArrIssues.getJSONArray(i).getInt(8); // "votes"
			        
			        int Currentstatus     = jArrIssues.getJSONArray(i).getInt(9);      // "currentstatus"
			        String Reported       = jArrIssues.getJSONArray(i).getString(10);  // "reported"
			        String Ack            = jArrIssues.getJSONArray(i).getString(11);  // "acknowledged"
			        String Closed         = jArrIssues.getJSONArray(i).getString(12);  // "closed"
			        
			        int    UserID         = jArrIssues.getJSONArray(i).getInt(13);    // "userid"
			        int    Ordering       = jArrIssues.getJSONArray(i).getInt(14);    // "ordering"
			        String Params         = jArrIssues.getJSONArray(i).getString(15); // "params"
			        int    State          = jArrIssues.getJSONArray(i).getInt(16);    // "state"
			        String Language       = jArrIssues.getJSONArray(i).getString(17); // "language"
			        int    Hits           = jArrIssues.getJSONArray(i).getInt(18);    // "hits"
			        
			        String Username       = jArrIssues.getJSONArray(i).getString(23); // "name"
			        			        
					// Local db
					Cursor cursorI = db.rawQuery( "SELECT ("+ KEY_IssueID +") FROM " 
				         + TABLE_Issues + " WHERE " + KEY_IssueID + "=" + Integer.toString(IssueID), null);
		
					if (cursorI.moveToFirst()){
						iHelpI.prepareForReplace();
					} else {
						iHelpI.prepareForInsert();
					}
					cursorI.close();
					
					iHelpI.bind(c1, IssueID); 	
					iHelpI.bind(c2, IssueTitle); 	
					iHelpI.bind(c3, CatID);
					iHelpI.bind(c4, Latitude);
					iHelpI.bind(c5, Longitude);
					iHelpI.bind(c6, Description);
					iHelpI.bind(c7, Photo);
					iHelpI.bind(c8, Address);
					iHelpI.bind(c9, votes);
					iHelpI.bind(c10, Currentstatus);
					iHelpI.bind(c11, Reported);
					iHelpI.bind(c12, Ack);
					iHelpI.bind(c13, Closed);
					iHelpI.bind(c14, UserID);
					iHelpI.bind(c15, Ordering);
					iHelpI.bind(c16, Params);
					iHelpI.bind(c17, State );
					iHelpI.bind(c18, Language);
					iHelpI.bind(c19, Hits);
					iHelpI.bind(c20, Username);
									
					iHelpI.execute();
				}
				db.setTransactionSuccessful();
			} finally {
   			  db.endTransaction();
			} // TRY OF TRANSACTION 
		} catch (JSONException e1) {
			e1.printStackTrace();
		}  // TRY OF JSONARRAY
    	

    	return response_BytesLength;
	}
	
	//================= AddUpdVersion  ==========================================
	/**
	 * 	 Insert values or updates values of Issues Version table
	 *  
	 * @param mVersionDB the downloaded version
	 */
	public void AddUpdVersion(VersionDB mVersionDB){
    		if (!db.isOpen())
        		db = this.getWritableDatabase();
			
    		db.delete(TABLE_Version, null, null);
    		
    		String sqlSTR = "INSERT INTO " + TABLE_Version + " ("+ KEY_VersionID      +","+ KEY_VersionTimestamp  +")" +
			" VALUES (" + Integer.toString(mVersionDB._id) + ",\"" + mVersionDB._time + "\")";
    		    		
			db.execSQL(sqlSTR);
	}
	
	//================= AddUpdCategVersion =====================
	/**
	 * Insert values or updates values of Categories Version table
	 * 
	 * @param mVersionDB the downloaded version 
	 */
	public void AddUpdCategVersion(VersionDB mVersionDB){
		if (!db.isOpen())
    		db = this.getWritableDatabase();
		
		db.delete(TABLE_CategVersion, null, null);
		
		String sqlSTR = "INSERT INTO " + TABLE_CategVersion + " ("+ KEY_VersionID      +","+ KEY_VersionTimestamp  +")" +
		" VALUES (" + Integer.toString(mVersionDB._id) + ",\"" + mVersionDB._time + "\")";
		    		
		db.execSQL(sqlSTR);
	}
	
	//================= addUpdCateg  ==========================================
	/**
	 *            Categories : Insert categories or update categories table
	 *   
	 * @return number of downloaded bytes 
	 */
	public int addUpdCateg(Context ctx){
		
		int bdown = 0;
		
		String response = Download_Data.Download_Categories();
	
		if (response!=null)
			bdown += response.length();
		
    	try {
			JSONArray jArrCategs =  new JSONArray(response);
        	int NCateg = jArrCategs.length();
		
        	if (!db.isOpen())
        		db = this.getWritableDatabase();
        	
			//--------- Create Helpers for Local db -----------------
			final InsertHelper iHelpC = new InsertHelper(db, TABLE_Categories);
			
			int c1 = iHelpC.getColumnIndex(KEY_CatID);
			int c2 = iHelpC.getColumnIndex(KEY_CatName);
			int c3 = iHelpC.getColumnIndex(KEY_CatIcon);
			int c4 = iHelpC.getColumnIndex(KEY_CatLevel);
			int c5 = iHelpC.getColumnIndex(KEY_CatParentID);
			int c6 = iHelpC.getColumnIndex(KEY_CatVisible);
			
			try
			{   
				db.beginTransaction();
				Log.e("UPD", "Categs");
				for (int i=0; i<NCateg; i++){
					
                    float prog = 100*((float) (i+1)) / ((float) NCateg); 
					
                    ctx.sendBroadcast(new Intent("android.intent.action.MAIN").putExtra("progressval", (int) (prog*0.67)));
                                        
					
					JSONArray jArrData  = new JSONArray(jArrCategs.get(i).toString());
					
					int CategID        = jArrData.getInt(0);        
					String CategName   = jArrData.getString(1);
					int CategLevel     = jArrData.getInt(2);
					int CategParentId  = jArrData.getInt(3);
					String CategParams = jArrData.getString(4);
					
					JSONObject cpOb = new JSONObject(CategParams);
					String CategIconPath = cpOb.getString("image"); 
					
					String fullPath = Constants_API.COM_Protocol + Constants_API.ServerSTR + 
                              Constants_API.remoteImages  + CategIconPath;
					
					// Download icon
				    byte[] CategIcon = Download_Data.Down_Image(fullPath );
				    
				    //------- Resize icon based on the device needs and store in db. --------------------
				    Bitmap CategIconBM = BitmapFactory.decodeByteArray(CategIcon, 0, CategIcon.length);
				    CategIconBM = Bitmap.createScaledBitmap(CategIconBM, (int) ((float)Fragment_Map.metrics.densityDpi/4.5), 
                                                               (int) ((float)Fragment_Map.metrics.densityDpi/4), true);

				    ByteArrayOutputStream stream = new ByteArrayOutputStream();
				    CategIconBM.compress(Bitmap.CompressFormat.PNG, 100, stream);
				    CategIcon = stream.toByteArray(); 
				    //---------------------------------------------------------
				    
				    bdown += CategIcon.length;
				    
					// Local db
					Cursor cursorC = db.rawQuery( "SELECT "+ KEY_CatID + 
							"," + KEY_CatVisible +" FROM " + TABLE_Categories + " WHERE " + KEY_CatID + "=" + 
                                         Integer.toString(CategID), null);
					
					if (cursorC.moveToFirst()){                   // Update 
						iHelpC.prepareForReplace();
						iHelpC.bind(c6, cursorC.getInt(1)==1 );
					} else {
						iHelpC.prepareForInsert();
						iHelpC.bind(c6, 1 );                   // Insert
					}
					
					iHelpC.bind(c1, CategID); 	
					iHelpC.bind(c2, CategName); 	
					iHelpC.bind(c3, CategIcon);
					iHelpC.bind(c4, CategLevel);
					iHelpC.bind(c5, CategParentId);
					cursorC.close();
									
			        iHelpC.execute();
				}
				db.setTransactionSuccessful();
			}
			finally
			{
   			 db.endTransaction();
			} // TRY OF TRANSACTION 
		} catch (JSONException e1) {
			e1.printStackTrace();
			Log.e(Constants_API.TAG, TAG_Class + ": Categories update failed");
		}  // TRY OF JSONARRAY
    	
    	return bdown;
	}

	//================= addUpdIssueThumb  ==========================================
    /**
     *  Insert or update Thumbnails of issues in IssuesThumbs table
     *   
     * Issue Thumb: Adding or Update
     * 
     * @param IssueID     
     * @param IssueThumb     image in array of bytes format
     * @throws IOException
     */
	public void addUpdIssueThumb(int IssueID, byte[] IssueThumb) throws IOException {
		String IssueID_STR = Integer.toString(IssueID);
		
		String selectQuery = "SELECT ("+ KEY_IssueID +") FROM " + TABLE_IssuesThumbs + " WHERE " 
		                                                        + KEY_IssueID + "=" + IssueID_STR;
		if (!db.isOpen())
  	    	db = this.getWritableDatabase();
		
		Cursor cursor = db.rawQuery(selectQuery, null);
		
        ContentValues values = new ContentValues();
		values.put(KEY_IssueThumbData     , IssueThumb);
		
		//---------- Insert Movie to SQLite --------------
		if (!cursor.moveToFirst()){
	        values.put(KEY_IssueID,       IssueID);
			db.insert(TABLE_IssuesThumbs, null, values); 
		} else {
			db.update(TABLE_IssuesThumbs, values, KEY_IssueID + " = ?", new String[] { IssueID_STR });
		}
		
		cursor.close();
		
		if (db.isOpen())
    		db.close();
	}
	
	//================= addUpdIssuePic  ==========================================
    /**  Insert or update Issue Image in Table_IssuesPics
     *  
     * @param IssueID
     * @param IssuePic   Image of the issues as an array of bytes
     * @throws IOException
     */
	public void addUpdIssuePic(int IssueID, byte[] IssuePic) throws IOException {
		String IssueID_STR = Integer.toString(IssueID);
		
		String selectQuery = "SELECT ("+ KEY_IssueID +") FROM " + TABLE_IssuesPics + " WHERE " 
		          + KEY_IssueID + "=" + IssueID_STR;
		
		if (!db.isOpen())
  	    	db = this.getWritableDatabase();
		
		Cursor cursor = db.rawQuery(selectQuery, null);
		
        ContentValues values = new ContentValues();
		values.put(KEY_IssuePicData     , IssuePic);
		
		//---------- Insert Movie to SQLite --------------
		if (!cursor.moveToFirst()){
	        values.put(KEY_IssueID,       IssueID);
			db.insert(TABLE_IssuesPics, null, values); 
		} else {
			db.update(TABLE_IssuesPics, values, KEY_IssueID + " = ?", new String[] { IssueID_STR });
		}
		
		cursor.close();
		
		if (db.isOpen())
    		db.close();
	}
	
	//================= getIssueThumb  ==========================================
	/**
	 *  Get Issue Thumb from SQLite table according to issue id.
	 * 
	 * @param IssueID
	 * @return
	 */
	public IssuePic getIssueThumb(int IssueID){
		SQLiteDatabase db = this.getReadableDatabase();
		IssuePic mIssueThumb;
		
		if (!db.isOpen())
    		db = this.getWritableDatabase();
		
		Cursor cr = db.query(TABLE_IssuesThumbs, 
				             new String[] {KEY_IssueID, KEY_IssueThumbData},  
				             KEY_IssueID + "=?",
				             new String[] { Integer.toString(IssueID) }, null, null, null, null);
		
		boolean ExistsRes = cr.moveToFirst();		
        		
        if (!ExistsRes){
        	mIssueThumb = new IssuePic(-1, null);
		} else {
			mIssueThumb = new IssuePic( cr.getInt(0), cr.getBlob(1) ) ;
		}
		cr.close();
		
		if (db.isOpen())
    		db.close();
		

		
		return mIssueThumb;
	}
	
	//================= getIssuePic  ==========================================
    /**
     * Get issue picture from SQlite according to issue id
     * 
     * @param IssueID
     * @return
     */
	public IssuePic getIssuePic(int IssueID){
		SQLiteDatabase db = this.getReadableDatabase();
		IssuePic mIssuePic;
		
		if (!db.isOpen())
    		db = this.getWritableDatabase();
		
		Cursor cr = db.query(TABLE_IssuesPics, 
				new String[] {KEY_IssueID, KEY_IssuePicData},     
				 KEY_IssueID + "=?",
				new String[] { Integer.toString(IssueID) }, null, null, null, null);
		
		boolean ExistsRes = cr.moveToFirst();		
        		
        if (!ExistsRes){
        	mIssuePic = new IssuePic(-1, null);
		} else {
			
			
			mIssuePic = new IssuePic( cr.getInt(0), cr.getBlob(1) ) ;
		}
		cr.close();
		if (db.isOpen())
    		db.close();
		
		return mIssuePic;
	}

	//================= CheckIfHasVoted  ==========================================
	/**
	 * Check if user has voted based on IssueID
	 *  
	 * @param IssueID
	 * @return true if has voted
	 */
	public boolean CheckIfHasVoted(int IssueID) {
		
		boolean HasVoted = false;
		
		String selectQuery = "SELECT * FROM " + TABLE_Votes + " WHERE "
		                      + KEY_IssueIDVotes  + "=" + Integer.toString(IssueID); 
		
		if (!db.isOpen())
    		db = this.getWritableDatabase();
		
		Cursor cursor = db.rawQuery(selectQuery, null);
 
		if (cursor.moveToFirst()) 
			HasVoted = true;
				
		cursor.close();
		if (db.isOpen())
    		db.close();
		return HasVoted;
	}
	
	//================= check if own issue ==========================================
	/**
	 * Check if user has submitted a certain issue.
	 * 
	 * @return
	 */
	public boolean checkIfOwnIssue(String IssueID, String UserID ) {
		String selectQuery = "SELECT * FROM " + TABLE_Issues + " WHERE "+ KEY_IssueID +"=" + IssueID + " and " + KEY_UserID + "=" + UserID;

		boolean res = false;
		
		if (!db.isOpen())
			db = this.getWritableDatabase();

		Cursor cursor = db.rawQuery(selectQuery, null);

		if (cursor.moveToFirst()) 
			res = true;
				
		cursor.close();
		if (db.isOpen())
    		db.close();
		
		return res;
	}
	
	
	//================= getAllCategories  ==========================================
	/**
	 *    Getting all categories
	 * @return
	 */
	public ArrayList<Category> getAllCategories() {
		ArrayList<Category> mCategL = new ArrayList<Category>();
		
		// Select All Query
		String selectQuery = "SELECT * FROM " + TABLE_Categories; 		

		if (!db.isOpen())
			db = this.getWritableDatabase();
		
		Cursor cursor = db.rawQuery(selectQuery, null);

		if (cursor.moveToFirst()) {
			do {
				Category mCategory = 
					new Category(cursor.getInt(0), cursor.getString(1), cursor.getBlob(2), 
							     cursor.getInt(3), cursor.getInt(4)   , cursor.getInt(5));

				mCategL.add(mCategory);
			} while (cursor.moveToNext());
		}
		
		cursor.close();
		return mCategL; 		
	}
	
	//================= setCategory  ==========================================
	/**
	 *  Set visibility (true or false) of a certain category for filtering issues
	 * 
	 * @param CatID
	 * @param CatVisibilityINT
	 */
	public void setCategory(int CatID, int CatVisibilityINT){
		if (!db.isOpen())
			db = this.getWritableDatabase();

		Cursor cursor = db.rawQuery("UPDATE " + TABLE_Categories + " SET " + 
			     KEY_CatVisible +"=" + Integer.toString(CatVisibilityINT) + " WHERE "+
			      KEY_CatID + "=?", new String[]{ Integer.toString(CatID)});
		 
		cursor.moveToFirst(); // importand for update !!
		cursor.close();
		
		if (db.isOpen())
		       db.close();
	
	}

	//================= getAllIssues  ==========================================
	/**
	 * Getting all issues.
	 * 
	 * @return
	 */
	public ArrayList<Issue> getAllIssues() {
		ArrayList<Issue> mIssueL = new ArrayList<Issue>();
		// Select All Query
		String selectQuery = "SELECT * FROM " + TABLE_Issues + " ORDER BY "+ KEY_IssueID +" DESC";

		if (!db.isOpen())
			db = this.getWritableDatabase();
		
		Cursor cursor = db.rawQuery(selectQuery, null);

		if (cursor.moveToFirst()) {
			do {
				Issue mIssue = 
					new Issue(cursor.getInt(0), cursor.getString(1), cursor.getInt(2), 
							  cursor.getDouble(3), cursor.getDouble(4), cursor.getString(5),
							  cursor.getString(6), cursor.getString(7), cursor.getInt(8),
							  cursor.getInt(9), cursor.getString(10), cursor.getString(11),
							  cursor.getString(12), cursor.getInt(13), cursor.getInt(14),
							  cursor.getString(15), cursor.getInt(16), cursor.getString(17),
							  cursor.getInt(18), cursor.getString(19));

				mIssueL.add(mIssue);
			} while (cursor.moveToNext());
		}

		cursor.close();
		return mIssueL;
	}
	
	//================= getCategVersion   ==========================================
	/**
	 * Get local version of categories table 
	 * 
	 * @return
	 */
	public VersionDB getCategVersion() {
		
		VersionDB mVersionDB = new VersionDB(0,null);
		
		if (!db.isOpen())
			db = this.getWritableDatabase();
		
		Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_CategVersion, null);

		if (cursor.moveToFirst()) 
			mVersionDB = new VersionDB(cursor.getInt(0),cursor.getString(1));
		
		cursor.close();
		if (db.isOpen())
		       db.close();
		
		return mVersionDB;
	}
	
	
	//================= getVersion  ==========================================
	/**
	 * Get local version of issues 
	 * 
	 * @return
	 */
	public VersionDB getVersion() {
		
		VersionDB mVersionDB = new VersionDB(0,null);
		
		
		if (!db.isOpen())
			db = this.getWritableDatabase();
		
		Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_Version, null);

		if (cursor.moveToFirst()) 
			mVersionDB = new VersionDB(cursor.getInt(0),cursor.getString(1));
		
		cursor.close();
		if (db.isOpen())
		       db.close();
		
		return mVersionDB;
	}
	
	//================= finalize  ==========================================
	@Override
	protected void finalize() throws Throwable {
		if (db.isOpen())
			db.close();
		super.finalize();
	}
}