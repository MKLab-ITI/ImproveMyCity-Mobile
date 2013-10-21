/** VersionDB */

package com.mk4droid.IMC_Constructors;


/**
 *  Database (remote MySQL) version object consists of an id and the timestamp of this version. 
 *   
* @copyright   Copyright (C) 2012 - 2013 Information Technology Institute ITI-CERTH. All rights reserved.
* @license     GNU Affero General Public License version 3 or later; see LICENSE.txt
* @author      Dimitrios Ververidis for the Multimedia Group (http://mklab.iti.gr). 
 *
 */
public class VersionDB {

	/** Identification of the version of the DB */
	public int _id;
	
	/** Timestamp of the version of the DB */
	public String _time;
		
	public VersionDB(){}
	
	public VersionDB(int id, String time){
	
	   this._id          = id;
	   this._time        = time;
   }

}