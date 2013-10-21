/** Preference_Reset */
package com.mk4droid.IMC_Core;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.widget.Toast;

import com.mk4droid.IMC_Activities.Activity_Setup;
import com.mk4droid.IMC_Activities.FActivity_TabHost;
import com.mk4droid.IMC_Services.DatabaseHandler;
import com.mk4droid.IMCity_PackDemo.R;

/**
 * Create a custom preference in Setup with the role of a button that clears all data in local data saved in SQLite db.
 * 
 * @copyright   Copyright (C) 2012 - 2013 Information Technology Institute ITI-CERTH. All rights reserved.
 * @license     GNU Affero General Public License version 3 or later; see LICENSE.txt
 * @author      Dimitrios Ververidis for the Multimedia Group (http://mklab.iti.gr). 
 *
 */
public class Preference_Reset extends Preference {
    
//    private final String TAG = getClass().getName();
    Context ctx;
    SharedPreferences mshPrefs = PreferenceManager.getDefaultSharedPreferences(FActivity_TabHost.ctx);
    
    public Preference_Reset(Context context, AttributeSet attrs) {
        super(context, attrs);
        ctx = context;
    }

    public Preference_Reset(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
 
   
   @Override
   protected void onClick() {

	    AlertDialog.Builder builder = new AlertDialog.Builder(Activity_Setup.ctx);
	    builder.setTitle(FActivity_TabHost.resources.getString(R.string.Reset));
	    builder.setIcon( android.R.drawable.ic_menu_preferences);
	    builder.setMessage(FActivity_TabHost.resources.getString(R.string.Areyousure));
	    
	    // 1 select
	    builder.setPositiveButton(FActivity_TabHost.resources.getString(R.string.Next),
	    		new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int id) {

	    		dialog.dismiss();
	    		DatabaseHandler dbHandler = new DatabaseHandler(FActivity_TabHost.ctx);
	    		SQLiteDatabase db = dbHandler.getWritableDatabase();

	    		db.execSQL("DELETE FROM " + DatabaseHandler.TABLE_Categories);
	    		db.execSQL("DELETE FROM " + DatabaseHandler.TABLE_Issues);
	    		db.execSQL("DELETE FROM " + DatabaseHandler.TABLE_IssuesPics);
	    		db.execSQL("DELETE FROM " + DatabaseHandler.TABLE_IssuesThumbs);
	    		db.execSQL("DELETE FROM " + DatabaseHandler.TABLE_Version);
	    		db.execSQL("DELETE FROM " + DatabaseHandler.TABLE_CategVersion);
	    		db.execSQL("DELETE FROM " + DatabaseHandler.TABLE_Votes);
	    		db.close();
	    		
	    		Toast.makeText(ctx, FActivity_TabHost.resources.getString(R.string.Deleted) +". " 
	    		+ FActivity_TabHost.resources.getString(R.string.Restartneed) +".", Toast.LENGTH_LONG).show();
	    	}
	    });

		// 3 clear 
	    builder.setNegativeButton(FActivity_TabHost.resources.getString(R.string.Cancel),
	    		new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int id) {
	    		dialog.dismiss();
	    	}
	    });
		
	    builder.create();
	    builder.show();
	    super.onClick();
    }
}
