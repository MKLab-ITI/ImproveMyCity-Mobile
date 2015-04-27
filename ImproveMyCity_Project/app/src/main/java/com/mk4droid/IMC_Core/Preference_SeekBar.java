// SeekBarPreference 

package com.mk4droid.IMC_Core;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.mk4droid.IMC_Activities.FActivity_TabHost;
import com.mk4droid.IMC_Services.InternetConnCheck;
import com.mk4droid.IMC_Store.Constants_API;
import com.mk4droid.IMC_Utils.GEO;
import com.mk4droid.IMCity_PackDemo.R;
/**
 * Implement a horizontal SeekBar for Setup->Setting range 
 * 
 * @copyright   Copyright (C) 2012 - 2015 Information Technology Institute ITI-CERTH. All rights reserved.
 * @license     GNU Affero General Public License version 3 or later; see LICENSE.txt
 * @author      Dimitrios Ververidis for the Multimedia Group (http://mklab.iti.gr). 
 *
 */
public class Preference_SeekBar extends Preference implements OnSeekBarChangeListener {
    
    private final String TAG_Class = getClass().getName();
    private static final int DEFAULT_VALUE = 50;
    private int mMaxValue      = 100;
    private int mMinValue      = 0;
    private int mInterval      = 1;
    private int mCurrentValue;

    private String mUnitsRight = "";
    private SeekBar mSeekBar;
    
    private TextView mStatusText;

    SharedPreferences mshPrefs;
    int distanceData;

	private View vgray;

	private android.widget.RelativeLayout.LayoutParams vrlparams;
    
    
	Context ctx;
	
	/**
	 * Constructor with 2 arguments
	 * 
	 * @param context
	 * @param attrs
	 */
    public Preference_SeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        mshPrefs = PreferenceManager.getDefaultSharedPreferences(FActivity_TabHost.ctx);
        distanceData  = mshPrefs.getInt("distanceData", Constants_API.initRange);
        ctx = context;
        initPreference(context, attrs);
    }

    /**
	 * Constructor with 3 arguments
	 * 
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
    public Preference_SeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initPreference(context, attrs);
    }

    private void initPreference(Context context, AttributeSet attrs) {
        setValuesFromXml(attrs);
        
        mSeekBar = new SeekBar(context, attrs);
        mSeekBar.setTag("seekBarD");
        mSeekBar.setMax(mMaxValue - mMinValue);
        mSeekBar.setOnSeekBarChangeListener(this);
        mSeekBar.setProgressDrawable(context.getResources().getDrawable(R.drawable.line_orange));
        mSeekBar.setPadding(25, 0, 25, 0);
        mSeekBar.setBackgroundColor(Color.TRANSPARENT);
        mSeekBar.setThumbOffset(25);
        mSeekBar.setThumb(FActivity_TabHost.resources.getDrawable(R.drawable.oval_orange));
        
        if (InternetConnCheck.getInstance(ctx).isOnline(ctx)){
        	mSeekBar.setEnabled(true);
    	} else {
     		mSeekBar.setEnabled(false);
    	}
        
                
        //------------VGRAY ------
        vgray = new View(ctx,attrs);
        vgray.setTag("seekBarGray");
        vgray.setBackgroundColor(Color.argb(255, 190, 190, 190));
        
        vrlparams = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,3);
        vrlparams.setMargins(25, 0, 25, 0);
    }
    
    
    /**        
     *               Set Value
     * @param attrs
     */
    private void setValuesFromXml(AttributeSet attrs) {
        mMaxValue =     100; 
        mMinValue =       0;
        
        mUnitsRight  = ""; 
        
        try {
            String newInterval =   "1";
            if(newInterval != null)
                mInterval = Integer.parseInt(newInterval);
        }
        catch(Exception e) {
            Log.e(Constants_API.TAG, TAG_Class +":Invalid interval value" + e);
        }
        
    }
   
    /**
     *    OnCreateView
     */
    @Override
    protected View onCreateView(ViewGroup parent){
        
        RelativeLayout layout =  null;
        
        try {
            LayoutInflater mInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            layout = (RelativeLayout)mInflater.inflate(R.layout.preference_seekbar, parent, false);
        } catch(Exception e){
            Log.e(Constants_API.TAG, TAG_Class + " :Error creating seek bar preference " + e);
        }
        return layout;
    }
    
    @Override
    public void onBindView(View view) {
        super.onBindView(view);

        try {
        	
            // move our seekbar to the new view we've been given
            ViewParent oldContainer = mSeekBar.getParent();
            ViewGroup newContainer  = (ViewGroup) view.findViewById(R.id.seekBarPrefBarContainer);
            
            
            if (oldContainer != newContainer) {
                // remove the seekbar from the old view
                if (oldContainer != null) {
                    ((ViewGroup) oldContainer).removeView(mSeekBar);
                    ((ViewGroup) oldContainer).removeView(vgray);
                }
                
                
                // remove the existing seekbar (there may not be one) and add ours
                newContainer.removeAllViews();
                
                // add new seekbar                
                newContainer.addView(mSeekBar, RelativeLayout.LayoutParams.FILL_PARENT, 
											   RelativeLayout.LayoutParams.WRAP_CONTENT); 
                
                ((RelativeLayout.LayoutParams) newContainer.findViewWithTag("seekBarD").getLayoutParams()
                		).addRule(RelativeLayout.CENTER_IN_PARENT);
                
                // add gray background
                newContainer.addView(vgray, RelativeLayout.LayoutParams.FILL_PARENT, 
						   					RelativeLayout.LayoutParams.WRAP_CONTENT);
                
                vrlparams = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,3);
                vrlparams.setMargins(35, 0, 35, 0);
                vgray.setLayoutParams(vrlparams);
                
                
                ((RelativeLayout.LayoutParams) newContainer.findViewWithTag("seekBarGray").getLayoutParams()
                		).addRule(RelativeLayout.CENTER_IN_PARENT);
                
                mSeekBar.bringToFront();
            }
        } catch(Exception ex) {
            Log.e(Constants_API.TAG, TAG_Class + ": Error binding view: " + ex.toString());
        }

        updateView(view);
    }
    
    /**
     * Update a SeekBarPreference view with our current state
     * @param view
     */
    protected void updateView(View view) {

        try {
            RelativeLayout layout = (RelativeLayout)view;

            mStatusText = (TextView)layout.findViewById(R.id.seekBarPrefValue);
       
            mStatusText.setText(GEO.DistanceToText(distanceData));
            mStatusText.setMinimumWidth(30);
            
            if (distanceData < Constants_API.initRange && distanceData > 10000) 
            	mSeekBar.setProgress((int) (25 + Math.sqrt(625 + distanceData/200)));  
            else if (distanceData>200 && distanceData <= 10000)
            	mSeekBar.setProgress(distanceData/200);
            else if (distanceData<=200)
            	mSeekBar.setProgress(0);
            else 
            	mSeekBar.setProgress(100);

            TextView unitsRight = (TextView)layout.findViewById(R.id.seekBarPrefUnitsRight);
            unitsRight.setText(mUnitsRight);
            
        } catch(Exception e) {
            Log.e(Constants_API.TAG, TAG_Class + " :Error updating seek bar preference " + e);
        }
    }

    /**
     *  Executes after the seekbar has changed value.
     */
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        int newValue = progress + mMinValue;
        
        if(newValue > mMaxValue)
            newValue = mMaxValue;
        else if(newValue < mMinValue)
            newValue = mMinValue;
        else if(mInterval != 1 && newValue % mInterval != 0)
            newValue = Math.round(((float)newValue)/mInterval)*mInterval;  
        
        if (newValue >= 90)  
        	distanceData = Constants_API.initRange;
        else if (newValue>50 && newValue < 90 )
        	distanceData = newValue * 200 * (newValue-50);
        else if (newValue>=1 && newValue <= 50 )
        	distanceData = newValue * 200;
        else if (newValue < 1)  
        	distanceData = 200;

        //-------- store to preferences ------
        SavePreferences("distanceData", distanceData, "int");
        
        // change rejected, revert to the previous value
        if(!callChangeListener(newValue)){
            seekBar.setProgress(mCurrentValue - mMinValue); 
            return; 
        }

        // change accepted, store it
        mCurrentValue = newValue;
        mStatusText.setText(GEO.DistanceToText(distanceData));
        persistInt(newValue);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {}

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        notifyChanged();
    }


    @Override 
    protected Object onGetDefaultValue(TypedArray ta, int index){
        int defaultValue = ta.getInt(index, DEFAULT_VALUE);
        return defaultValue;
    }


    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {

    	if(restoreValue) 
    		mCurrentValue = getPersistedInt(mCurrentValue);
    	else {
    		int temp = 0;
    		try {
    			temp = (Integer)defaultValue;
    		} catch(Exception ex) {
    			Log.e(Constants_API.TAG, TAG_Class + ": Invalid default value: " + defaultValue.toString());
    		}

    		persistInt(temp);
    		mCurrentValue = temp;
    	}

    }

    /**
     * Save a value to preferences, either string or boolean
     * 
     * @param key       name of the parameters to save
     * @param value     value of the parameter to save 
     * @param type      either "String" or "Boolean" 
     */
	private void SavePreferences(String key, Object value, String type){
		SharedPreferences shPrefs = PreferenceManager.getDefaultSharedPreferences(FActivity_TabHost.ctx);
		SharedPreferences.Editor editor = shPrefs.edit();

		if (type.equals("String")) 
			editor.putString(key, (String) value);
		else if (type.equals("int")) 
			editor.putInt(key, (Integer) value);

		editor.commit();
	}
    
    
}
