/** Issue  */
package com.mk4droid.IMC_Constructors;

import java.io.Serializable;

/**
 * Issue structured information.
 * 
* @copyright   Copyright (C) 2012 - 2015 Information Technology Institute ITI-CERTH. All rights reserved.
* @license     GNU Affero General Public License version 3 or later; see LICENSE.txt
* @author      Dimitrios Ververidis for the Multimedia Group (http://mklab.iti.gr). 
 *
 */
public class Issue implements Serializable {

	private static final long serialVersionUID = 1L;
	
	/** Unique identifier of the issue */
	public int _id; 
	
	/** Unique identifier of category of the issue */
	public int _catid;
	
	/** Votes that the issue received */
	public int _votes;
	
	/** Current status of the issue 1 Open; 2 Ack; 3 Closed */
	public int _currentstatus;
	
	/** Unique identifier of the author of the issue */
	public int _userid;
	
	/** Joomla parameter, ignore for the time being */
	public int _ordering;
	
	/** Joomla state of the issue, ignore for the time being */
	public int _state;
	
	/** Number of times the issue was viewed from the web version */
	public int _hits;
	
	/** Location latitude in decimal degrees, e.g. 40.567 */
	public double _latitude;
	
	/** Location longitude in decimal degrees, e.g. 22.999 */
	public double _longitude;
	
	/** Title of the issue */
	public String  _title;
	
	/** Description of the issue */
	public String _description;

	/** Url of the photo of the issue */
	public String _urlphoto;
	
	/** Text address of the issue, e.g. 21 Karaoli street */
	public String _address;
	
	/** Reported time stamp */
	public String _reported;
	
	/** Acknowledged time stamp */
	public String _acknowledged;
	
	/** Closed time stamp */
	public String _closed;
	
	/** Joomla parameter, ignore */
	public String _params;
	
	/** Joomla parameter, ignore */ 
	public String _language;
	
	/** Author of the issue */
	public String _username;
	
    public Issue(){}
	
	public Issue(int id, String title, int catid, 
			double latitude, double longitude, 
			String description, String urlphoto, String address, int votes, int currentstatus, 
			String reported, String acknowledged, String closed, int userid, int ordering, String params, 
			int state, String language, int hits, String username){ //,
		
		   this._id           = id;
		   this._title        = title;
		   
		   this._catid        = catid; 
		   this._urlphoto     = urlphoto;
		   this._address      = address; 
		   this._votes        = votes; 
		   this._currentstatus= currentstatus; 
		   this._reported     = reported; 
		   this._acknowledged = acknowledged; 
		   this._closed       = closed;
		   this._userid       = userid;
		   this._ordering     = ordering;
		   this._params       = params;
		   this._state        = state;
		   this._language     = language;
		   this._hits         = hits;
		   
		   this._description = description;
		   this._latitude    = latitude;
		   this._longitude   = longitude;
		   this._username   = username;
	}
}
