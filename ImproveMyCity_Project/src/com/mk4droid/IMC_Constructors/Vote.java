/** Vote */
package com.mk4droid.IMC_Constructors;



/**
 * Construct a vote consisting of the vote id and the issue id that the vote is for 
 * 
 * 
 * @copyright   Copyright (C) 2012 - 2013 Information Technology Institute ITI-CERTH. All rights reserved.
 * @license     GNU Affero General Public License version 3 or later; see LICENSE.txt
 * @author      Dimitrios Ververidis for the Multimedia Group (http://mklab.iti.gr). 
 *
 */

public class Vote {
	
        /** Vote unique identifier */
		public int _voteid;
		
		/** Issue that this vote was for */
		public int _issueid;
				
		public Vote(){}
		
		public Vote(int voteid, int issueid){
		
		   this._voteid       = voteid;
		   this._issueid      = issueid;
		}
		   
		
	

}
