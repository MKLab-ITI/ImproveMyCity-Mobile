/** IssuePic */
package com.mk4droid.IMC_Constructors;

/**
 * Issue Picture as an object
 * 
* @copyright   Copyright (C) 2012 - 2013 Information Technology Institute ITI-CERTH. All rights reserved.
* @license     GNU Affero General Public License version 3 or later; see LICENSE.txt
* @author      Dimitrios Ververidis for the Multimedia Group (http://mklab.iti.gr). 
 *
 */
public class IssuePic {
	
	 /** Unique identifier of the issue that this picture belongs to */
	 public int _id;	 
	 
	 /** Image data as a byte array */
	 public byte[] _IssuePicData;
	
	// Empty constructor
	public IssuePic(){ 	}
		
	// constructor
	public IssuePic(int id, byte[] IssuePicData){
		this._id           = id;  
		this._IssuePicData = IssuePicData;
	}
}





