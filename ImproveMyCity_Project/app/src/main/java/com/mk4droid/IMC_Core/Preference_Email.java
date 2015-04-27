/** Preference_Email */
package com.mk4droid.IMC_Core;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;

import com.mk4droid.IMC_Activities.FActivity_TabHost;

/**
 * Create a custom preference to send an e-mail to express personal opinion.
 * 
 * @copyright   Copyright (C) 2012 - 2015 Information Technology Institute ITI-CERTH. All rights reserved.
 * @license     GNU Affero General Public License version 3 or later; see LICENSE.txt
 * @author      Dimitrios Ververidis for the Multimedia Group (http://mklab.iti.gr). 
 */
public class Preference_Email extends Preference {

    Context ctx;
    SharedPreferences mshPrefs = PreferenceManager.getDefaultSharedPreferences(FActivity_TabHost.ctx);
    
    public Preference_Email(Context context, AttributeSet attrs) {
        super(context, attrs);
        ctx = context;
    }

    public Preference_Email(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
 
   
   @Override
   protected void onClick() {
	   
	    	//----------- Data ---------------			
	   		Intent emailIntentQuest = new Intent(android.content.Intent.ACTION_SEND);  
	   
	   		//	----------- Data ---------------			
	   		String aEmailListQuest[] = { "improvemycitymobile@gmail.com", };  
	   
	   		emailIntentQuest.putExtra(android.content.Intent.EXTRA_EMAIL, aEmailListQuest);  
	   		emailIntentQuest.putExtra(android.content.Intent.EXTRA_SUBJECT, "Improve my City");  
	   
	   		emailIntentQuest.setType("plain/text");  
	   		emailIntentQuest.putExtra(android.content.Intent.EXTRA_TEXT,"");
	   		// -------------------------
	   
	   		FActivity_TabHost.ctx.startActivity(Intent.createChooser(emailIntentQuest, "Send your email with:"));
	   		//	-------------------------------
	super.onClick();
    }
}
