// InfoWindowAdapterButtoned 
package com.mk4droid.IMC_Core;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.model.Marker;
import com.mk4droid.IMCity_PackDemo.R;

/**
 *  This class creates a custom Info window that has a title, a snippet and an icon.
 *   
 * @copyright   Copyright (C) 2012 - 2013 Information Technology Institute ITI-CERTH. All rights reserved.
 * @license     GNU Affero General Public License version 3 or later; see LICENSE.txt
 * @author      Dimitrios Ververidis for the Multimedia Group (http://mklab.iti.gr). 
 *
 */
public class InfoWindowAdapterButtoned implements InfoWindowAdapter{
	
	// This viewgroup contains an ImageView with id "badge" and two TextViews with id "title" and "snippet".
    private final View mWindow;
    
    /** Constructor 
     * 
     * @param a the Activity of the caller
     */
    public InfoWindowAdapterButtoned(Activity a) {
        mWindow =  a.getLayoutInflater().inflate(R.layout.custom_info_window, null);
    }

    @Override
    public View getInfoWindow(Marker marker) {
        render(marker, mWindow);
        return mWindow;
    }

    @Override
    public View getInfoContents(Marker marker) {
        render(marker, mWindow);
        return mWindow;
    }

    private void render(Marker marker, View view) {
        
        String title = marker.getTitle();
        TextView titleUi = ((TextView) view.findViewById(R.id.title));
        if (title != null) {
            titleUi.setText(title);
        } else {
            titleUi.setText("");
        }

        String snippet = marker.getSnippet();
        TextView snippetUi = ((TextView) view.findViewById(R.id.snippet));
        snippetUi.setText(snippet);
    }

}
