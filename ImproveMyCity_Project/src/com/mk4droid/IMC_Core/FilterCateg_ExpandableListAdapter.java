/** FilterCategories_ExpandableListAdapter */

package com.mk4droid.IMC_Core;

import android.graphics.Color;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckedTextView;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.mk4droid.IMC_Activities.Fragment_Filters;
import com.mk4droid.IMC_Services.DatabaseHandler;
import com.mk4droid.IMC_Services.Service_Data;
import com.mk4droid.IMCity_PackDemo.R;

/**
 *  Expandable list of categories to be used in Filters Activity
 *
 * @copyright   Copyright (C) 2012 - 2013 Information Technology Institute ITI-CERTH. All rights reserved.
 * @license     GNU Affero General Public License version 3 or later; see LICENSE.txt
 * @author      Dimitrios Ververidis for the Multimedia Group (http://mklab.iti.gr). 
 *
 */

public class FilterCateg_ExpandableListAdapter extends BaseExpandableListAdapter{
    
	int icon_on                = android.R.drawable.checkbox_on_background;
	int icon_off               = android.R.drawable.checkbox_off_background;
	
	ExpandableListView elv;
	ScrollView scrv;
	LinearLayout llfilters;
	
	/**
	 * 
	 */
	public FilterCateg_ExpandableListAdapter(ExpandableListView elv_in, LinearLayout llfilters_in) {
	    elv = elv_in;
	    
	    llfilters = llfilters_in;
	}
	
    	
	/** Set groups */
    public void setGroupsAndValues(String[] g, boolean[] v) {
        Fragment_Filters.groups = g;
        Fragment_Filters.groups_check_values = v;
    }
    
    /** Set children of groups */
    public void setChildrenAndValues(String[][] c, boolean[][] v) {
    	Fragment_Filters.children = c;
    	Fragment_Filters.children_check_values = v;
    }
    
    /** Get Child of a certain group */
	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return Fragment_Filters.children[groupPosition][childPosition];
	}

	/** Get Child id of a certain group */
	@Override
	 public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    /** Get Child view of a certain group */ 
	@Override
	public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild,
            View convertView, ViewGroup parent) {

		//---------------- Get Layout from resources ----------------
		
		View v = Fragment_Filters.FiltInflater.inflate(R.layout.expander_child, null);

		LinearLayout llexp = (LinearLayout) v.findViewById(R.id.llexp);
		ImageView imv = (ImageView) v.findViewById(R.id.imageViewExpCat);
		final CheckedTextView ctb = (CheckedTextView) v.findViewById(R.id.checkBoxExpCat);
		
		llexp.setPadding(20, 0, 0, 0);
		imv.setImageDrawable(Fragment_Filters.children_icon_values[groupPosition][childPosition]);		
        
        //----------------- Set Text ------------
        ctb.setText(getChild(groupPosition, childPosition).toString());
        ctb.setTextColor(Color.argb(255, 100, 100, 100));
        
        // --------------- Set State and Drawable depending on State ------------
        boolean ChildState = Fragment_Filters.children_check_values[groupPosition][childPosition];
        ctb.setChecked(ChildState);

        if (ChildState)
        	ctb.setCheckMarkDrawable(icon_on);
        else
        	ctb.setCheckMarkDrawable(icon_off);
        
        //----------------- Set Listener on Click ----------------------------
        ctb.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v) {
        		Fragment_Filters.FiltersChangedFlag = true;
        		
        		Fragment_Filters.children_check_values[groupPosition][childPosition] = 
        			!Fragment_Filters.children_check_values[groupPosition][childPosition];
        			 
        		boolean ChildState = Fragment_Filters.children_check_values[groupPosition][childPosition];
        		
        		int ChildID = Fragment_Filters.children_id[groupPosition][childPosition];
        		
        		DatabaseHandler dbHandler = new DatabaseHandler(Fragment_Filters.ctx);
        	    dbHandler.setCategory(ChildID, ChildState?1:0); // Send as integer
        	    Service_Data.mCategL =  dbHandler.getAllCategories();
        		dbHandler.db.close();
        	    
       			ctb.setChecked(ChildState);
       			if (ChildState)
        			ctb.setCheckMarkDrawable(icon_on);
       			else
       				ctb.setCheckMarkDrawable(icon_off);
        	}
        });
        
        ctb.invalidate();
        v.invalidate();
        
        return v;
    }

	
	
	@Override
	 public int getChildrenCount(int groupPosition) {
        return Fragment_Filters.children[groupPosition].length;
    }

	@Override
	public Object getGroup(int groupPosition) {
        return Fragment_Filters.groups[groupPosition];
    }

	@Override
	public int getGroupCount() {
        return Fragment_Filters.groups.length;
    }

	@Override
	public long getGroupId(int groupPosition) {
        return groupPosition;
    }
	
	/** Get the view of a certain group */ 
	@Override
	public View getGroupView(final int groupPosition, boolean isExpanded, final View convertView, final ViewGroup parent) {
		
		//------------------ Get Layout from Resources ---------------------
		View v = Fragment_Filters.FiltInflater.inflate(R.layout.expander_child, null);
						
		ImageView imv = (ImageView) v.findViewById(R.id.imageViewExpCat);
		imv.setPadding(-10, 0, 0, 0);
		imv.setImageDrawable(Fragment_Filters.groups_icon_values[groupPosition]);

		final CheckedTextView ctb = (CheckedTextView) v.findViewById(R.id.checkBoxExpCat);

		// --------------- Set Text -------------------------------------------
		ctb.setText(getGroup(groupPosition).toString());
		ctb.setTextSize(16);

		// ----Set State and Drawable depending on State ----------------------
		boolean ParentState = (Boolean) Fragment_Filters.groups_check_values[groupPosition];
		ctb.setChecked( ParentState );

		if (ParentState)
			ctb.setCheckMarkDrawable(icon_on);
		else
			ctb.setCheckMarkDrawable(icon_off);
		
		//----------------- Set Listener on Click ----------------------------
		ctb.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Fragment_Filters.FiltersChangedFlag = true;

				Fragment_Filters.groups_check_values[groupPosition] = 
						!Fragment_Filters.groups_check_values[groupPosition];

				boolean ParentState = Fragment_Filters.groups_check_values[groupPosition];
				
				ctb.setChecked( ParentState );

				if (ParentState)
					ctb.setCheckMarkDrawable(icon_on);
				else
					ctb.setCheckMarkDrawable(icon_off);
				
				DatabaseHandler dbHandler = new DatabaseHandler(Fragment_Filters.ctx);
				int ParentID = Fragment_Filters.groups_id[groupPosition];
				
				dbHandler.setCategory(ParentID, ParentState?1:0); // Send as integer
				Service_Data.mCategL =  dbHandler.getAllCategories();
				dbHandler.db.close();

				int NChildren  =  Fragment_Filters.children_check_values[groupPosition].length;

				for (int iChild=0; iChild < NChildren; iChild ++ ){
					Fragment_Filters.children_check_values[groupPosition][iChild] = ParentState;

					boolean ChildState = Fragment_Filters.children_check_values[groupPosition][iChild];

					int ChildID = Fragment_Filters.children_id[groupPosition][iChild];

					dbHandler = new DatabaseHandler(Fragment_Filters.ctx);
					dbHandler.setCategory(ChildID, ChildState?1:0);
					Service_Data.mCategL =  dbHandler.getAllCategories();
					dbHandler.db.close();
				}
			}
		});
		
		return v;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}
	
}
