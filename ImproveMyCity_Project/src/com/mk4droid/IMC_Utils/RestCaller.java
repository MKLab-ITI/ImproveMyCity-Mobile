// RestCaller
package com.mk4droid.IMC_Utils;

import android.util.Log;

import com.mk4droid.IMC_Store.Constants_API;
import com.mk4droid.IMC_Utils.RestClient.RequestMethod;

/**
 * Call REST Client with arguments.
 * 
 * @copyright   Copyright (C) 2012 - 2013 Information Technology Institute ITI-CERTH. All rights reserved.
 * @license     GNU Affero General Public License version 3 or later; see LICENSE.txt
 * @author      Dimitrios Ververidis for the Multimedia Group (http://mklab.iti.gr). 
 * 
 */
public class RestCaller {

	
	public RestCaller(){};
	//================  RestCaller          =======================================
    /**
     * Call REST (http with get or post) with specified parameters
     * 
     * @param url the url to call
     * @param rm  RestClient.RequestMethod.GET or RestClient.RequestMethod.POST 
     * @param args Pairs of parameters to send with php {"Latitude","40.5","Longitude","23"}      
     * @param encoding  "ANSI" or "UTF-8" to support multiple languages. HINT: utf-8 increases about x6 times the amount 
     *                     of transfered data 
     * @return
     */
	public String now(String url, String rmSTR, String[] args, String encoding , String CalledBy){ // String mfile
				
		RequestMethod rm;
		if (rmSTR.equals("GET"))
			rm = RestClient.RequestMethod.GET;
		else 
			rm = RestClient.RequestMethod.POST;
		
		RestClient clientR = new RestClient(url, encoding);

		// args should be pairs of Strings
		if (args!=null)
			for (int i=0; i< args.length; i=i+2) 
				clientR.AddParam(args[i], args[i+1]);
		
    	try {
    	    clientR.Execute(rm);
    	} catch (Exception e) {
    		Log.e(Constants_API.TAG, "Download_Data:RestCaller:"+ e.getMessage());
    	}
		
    	//int respcode = clientR.getResponseCode();
    	String response = clientR.getResponse();
    	    	
		return response;
	}
	
}
