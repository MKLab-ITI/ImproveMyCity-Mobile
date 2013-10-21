/** IssueListItem */

package com.mk4droid.IMC_Constructors;

import android.graphics.Bitmap;

/**
 * Construct an object containing information to display in the list of issues  
 * 
* @copyright   Copyright (C) 2012 - 2013 Information Technology Institute ITI-CERTH. All rights reserved.
* @license     GNU Affero General Public License version 3 or later; see LICENSE.txt
* @author      Dimitrios Ververidis for the Multimedia Group (http://mklab.iti.gr). 
 *
 */
public class IssueListItem {
	    
	    /** Thumbnail of the issue of the item  */
	    public Bitmap _icon;
	    
	    /** Unique identifier of the issue */
	    public String _id; 
	    
	    /** Title of the issue */
	    public String _title; 
	    
	    /** Textual address of the issue */ 
	    public String _address; 
	    
	    /** Reported time stamp */
	    public String _reported; 
	    
	    /** Votes received */
	    public String _votes;
	    
	    /** URL of the photo of the isse */
	    public String _urlphoto;
	    
	    /** Current state of the issue */
	    public int _currstate;
	    
	    /** Latitude of the location of the issue in decimal degrees 40.567 */
	    public double _latitude;
	    
	    /** Longitude of the location of the issue in decimal degrees 40.567 */
	    public double _longitude;
	    
	    /** issue object */
	    public Issue _issue;
	    
	    public IssueListItem(){
	        super();
	    }
	    
	    public IssueListItem(Bitmap bitmap, int id, String title, int state, String address,
	    		         String reported, int votes, double latitude, double longitude, String urlphoto, Issue issue) { 
	        super();
	        
	        this._icon      = bitmap;
	        this._id        = "#"+ Integer.toString(id); 
	        this._title     = title;
	        this._currstate = state;
	        this._address   = address;
	        this._reported  = reported;
	        this._votes     = Integer.toString(votes);
	        this._latitude  = latitude; 
	        this._longitude = longitude; 
	        this._urlphoto     = urlphoto;
	        this._issue     = issue;
	    }
}
