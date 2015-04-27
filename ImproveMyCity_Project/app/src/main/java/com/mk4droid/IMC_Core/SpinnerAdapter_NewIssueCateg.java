// NewIssueCateg_SpinnerAdapter  
package com.mk4droid.IMC_Core;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.mk4droid.IMC_Activities.Fragment_NewIssueA;
import com.mk4droid.IMC_Constructors.Category;
import com.mk4droid.IMCity_PackDemo.R;

import java.util.ArrayList;


/**
 * This is the adapter for the spinner in Activity_NewIssueA. Custom spinner with 
 * category icon and text dynamically retrieved from the SQLite.
 * 
 * @author Dimitrios Ververidis, Dr.
 *         Post-doctoral Researcher, 
 *         Information Technologies Institute, ITI-CERTH,
 *         Thermi, Thessaloniki, Greece      
 *         ververid@iti.gr,  
 *         http://mklab.iti.gr
 *
 */
public class SpinnerAdapter_NewIssueCateg extends ArrayAdapter<Category>{

	private Activity context;
	ArrayList<Category> data = null;
	final float dens;
	int NCategs = 0;
	Bitmap[] bmicons;
	CheckedTextView cat_rb;
	
	/** Constructor. Get density of the screen to scale icons of categories */
	public SpinnerAdapter_NewIssueCateg(Activity context, int resource, ArrayList<Category> data)
	{
		super(context, resource, data);
		
		this.context = context;
		this.data = data;

		dens = getContext().getResources().getDisplayMetrics().density;

		NCategs = data.size();
		bmicons = new Bitmap[NCategs];

		for (int i=0; i < NCategs; i++){
			Category item = data.get(i);
			bmicons[i] = BitmapFactory.decodeByteArray(item._icon, 0, item._icon.length);
			float scfact = 1;
			if (item._level == 1)
				scfact = 1.3f;

			int sc_image = (int)( 30*scfact );
			bmicons[i] = Bitmap.createScaledBitmap(bmicons[i], (int) (sc_image * dens + 0.5f), (int) (sc_image * dens *1.17f + 0.5f), true);
		}

	}
		

    /**
     *    Get current visible item. Usually this is called only when the spinner is created.
     */
	@Override
	public View getView(int position, View convertView, ViewGroup parent){ 
		
		View row;

		LayoutInflater inflater = context.getLayoutInflater();
		row = inflater.inflate(R.layout.spinner_categ_item, parent, false);

		if (parent.getChildCount() > 0 || Fragment_NewIssueA.spPosition != -1) { 
			
			Category item = data.get(position);

			// Parse the data from each object and set it.
			if(item != null){
				
				ImageView cat_imv   = (ImageView) row.findViewById(R.id.categIcon);
				CheckedTextView cat_tv = (CheckedTextView) row.findViewById(R.id.categName);

				if(cat_imv != null){
					Bitmap bmicon = BitmapFactory.decodeByteArray(item._icon, 0, item._icon.length);

					if (item._level == 2)
						bmicon = Bitmap.createScaledBitmap(bmicon, (int) (45 * dens + 0.5f), (int) (50 * dens + 0.5f), true);
					else 
						bmicon = Bitmap.createScaledBitmap(bmicon, (int) (50 * dens + 0.5f), (int) (55 * dens + 0.5f), true);

					cat_tv.setCompoundDrawablesWithIntrinsicBounds(new BitmapDrawable(bmicon), null, null, null);
					cat_tv.setCompoundDrawablePadding(20);
				} 
				
				if(cat_tv != null){
					cat_tv.setText(item._name);
					cat_tv.setTextSize(15);
				} 
			} 
			
        } else { // Before appearance of the Spinner
        	((CheckedTextView)row.findViewById(R.id.categName)).setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        }
		
		return row;
	}

	
    /**
     * This view starts when the spinner is clicked.	
     */
	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent){ 
		
		Category item = data.get(position);
		
		RelativeLayout rl;
		
		
		if(convertView == null ){
					LayoutInflater inflater = context.getLayoutInflater();
			convertView = inflater.inflate(R.layout.spinner_categ_item, parent, false);
			rl = (RelativeLayout) convertView.findViewById(R.id.rlcategitem);
		} else {
			rl = (RelativeLayout) convertView.findViewById(R.id.rlcategitem);
			View v = rl.findViewWithTag("hlbottom");
			View v2 = rl.findViewWithTag("hltop");

			if (v!=null)
				rl.removeView(v);

			if (v2!=null)
				rl.removeView(v2);
		}


		// Parse the data from each object and set it.
		if(item != null){ 
			ImageView cat_imv   = (ImageView) convertView.findViewById(R.id.categIcon);

			float scfact = 1;

			if (item._level == 1){
				scfact = 1.3f;

				rl.setBackgroundColor(Color.argb(255, 230, 230, 230));
				cat_imv.setPadding(10, 0, 0, 0);

				//------ horizontal line bottom ------
				if (position != NCategs){ // except last row
					View hlineView = new View(context);
					RelativeLayout.LayoutParams rlparams = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, 1);

					rlparams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, -1);
					rlparams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, -1);

					hlineView.setTag("hlbottom");
					hlineView.setLayoutParams(rlparams);
					hlineView.setBackgroundColor(Color.BLACK);

					rl.addView(hlineView);
				}

				//------ horizontal line top ------
				if (position != 0){  // except first row
					View hlineView2 = new View(context);
					RelativeLayout.LayoutParams rlparams2 = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, 1);

					rlparams2.addRule(RelativeLayout.ALIGN_PARENT_TOP, -1);
					rlparams2.addRule(RelativeLayout.ALIGN_PARENT_LEFT, -1);

					hlineView2.setTag("hltop");
					hlineView2.setLayoutParams(rlparams2);
					hlineView2.setBackgroundColor(Color.BLACK);

					rl.addView(hlineView2);
				}
				//-----------------------------------
			}else{
				scfact = 1f;
				cat_imv.setPadding(30, 0, 0, 0);
				rl.setBackgroundColor(Color.WHITE);
			}

			cat_rb     = (CheckedTextView)  convertView.findViewById(R.id.categName);
		
			// icon 
			if(cat_imv != null){
				cat_imv.setImageBitmap(bmicons[position]);
				
				if (Fragment_NewIssueA.spPosition != -1){
					if ( data.get(Fragment_NewIssueA.spPosition)._id == data.get(position)._id )
						cat_rb.setChecked(true);
					else 
						cat_rb.setChecked(false);
				}
			}
			
			// text size
			if(cat_rb != null){
				cat_rb.setText(item._name);
				cat_rb.setTextSize(  15 * scfact );
				if (scfact == 1)
					cat_rb.setTextColor(Color.argb(255, 100, 100, 100));
				else
					cat_rb.setTextColor(Color.BLACK);
			}
		}

		return convertView;
	}

}