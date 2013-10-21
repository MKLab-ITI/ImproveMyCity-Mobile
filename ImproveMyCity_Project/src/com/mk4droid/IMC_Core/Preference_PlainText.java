// Preference_PlainText 
package com.mk4droid.IMC_Core;

import com.mk4droid.IMC_Activities.FActivity_TabHost;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;

/**
 *   In setup include a preference showing the version of the application.
 *    
 * @copyright   Copyright (C) 2012 - 2013 Information Technology Institute ITI-CERTH. All rights reserved.
 * @license     GNU Affero General Public License version 3 or later; see LICENSE.txt
 * @author      Dimitrios Ververidis for the Multimedia Group (http://mklab.iti.gr). 
 */
public class Preference_PlainText extends Preference {
    Context ctx;
    SharedPreferences mshPrefs = PreferenceManager.getDefaultSharedPreferences(FActivity_TabHost.ctx);
    
    public Preference_PlainText(Context context, AttributeSet attrs) {
        super(context, attrs);
        ctx = context;
    }

    public Preference_PlainText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
   
   @Override
   protected void onClick() {
	super.onClick();
   }
}
