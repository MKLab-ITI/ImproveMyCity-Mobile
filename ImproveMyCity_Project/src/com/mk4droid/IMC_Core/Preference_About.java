// Preference_About 
package com.mk4droid.IMC_Core;

import com.mk4droid.IMC_Activities.Activity_Information_Detailed;
import com.mk4droid.IMC_Activities.FActivity_TabHost;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;

/**
 * Custom preference to add in Setup view so as to have a button which leads to a new activity 
 * with information about the app and the authors
 * 
 * @copyright   Copyright (C) 2012 - 2013 Information Technology Institute ITI-CERTH. All rights reserved.
 * @license     GNU Affero General Public License version 3 or later; see LICENSE.txt
 * @author      Dimitrios Ververidis for the Multimedia Group (http://mklab.iti.gr). 
 *
 */
public class Preference_About extends Preference {
    Context ctx;
    SharedPreferences mshPrefs = PreferenceManager.getDefaultSharedPreferences(FActivity_TabHost.ctx);
    
    public Preference_About(Context context, AttributeSet attrs) {
        super(context, attrs);
        ctx = context;
    }

    public Preference_About(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
   
   @Override
   protected void onClick() {
	   FActivity_TabHost.ctx.startActivity(new Intent(FActivity_TabHost.ctx, Activity_Information_Detailed.class));
	   super.onClick();
    }
}
