/** Constants_API */
package com.mk4droid.IMC_Store;

/**
 * Customization for your application is feasible here.
 * HINT: Be careful when to use http:// or https:// in downloading and uploading data. It highly depends on your platform
 * HINT2: Be careful with the paths of your remote server. Paths may differ per installation configuration.

 * These constants are visible everywhere in the app.
 * 
 * @copyright   Copyright (C) 2012 - 2015 Information Technology Institute ITI-CERTH. All rights reserved.
 * @license     GNU Affero General Public License version 3 or later; see LICENSE.txt
 * @author      Dimitrios Ververidis for the Multimedia Group (http://mklab.iti.gr). 
 *
 */


public class Constants_API {

	/** TAG for Log of messages to alleviate debugging  */
	public static String TAG = "ImproveMyCity";

	/** The e-mail to contact with the support*/
	public static String ContactEmail = "improvemycitymobile@gmail.com";
	
	//==================== Communication parameters =================
	/** Transmitting protocol */
	public static String COM_Protocol   = "http://";

	/** Secure transmitting protocol. If your server supports SSL then this should be https:// */
	public static String COM_Protocol_S = "http://";
	//public static String COM_Protocol_S = "https://";
	
	
    /** Server address. It can be a XXX.XXX.XXX.XXX address instead */
	public static String ServerSTR      = "improve-my-city.com";
	
	
	/** Server path of application */
	public static String phpExec        =  "/demo/";
	
	/** Server path of issue images */ 
	public static String remoteImages   = "/demo/";

	/** Encryption key for transmitting password (16 digits). It should be the same as in your ImproveMyCity joomla component. 
	 * The default value is 1234567890123456 (choose this as a quick start to connect with your server).
	 * */
	public static String EncKey =  "8369971084512071"; // This key is for the demo server // The default is "1234567890123456" ;


	/**
	 * Default menu language, options: "en - English" or  "el - Ελληνικά"
	 * 
	 * Go also to res/myprefs.xml to android:key="LanguageAR" and set 
	 * android:defaultValue="el - Ελληνικά"  or "en - English" in order to have a correct initial value for the radius buttons.
	 */
	public static String DefaultLanguage = "en - English";
	//public static String DefaultLanguage = "el - Ελληνικά";

	//====== GEOGRAPHIC  Limits ============
	/** Geographical limits is a polygon from where issues can be sent. It can be modified in /raw/polygoncoords.txt.
    * The polygon is created by Longitude,Latitude pairs separated with a single space, e.g.
    *   
    * 23.1094 40.5789 23.1111 40.5781 23.1126 40.5774 23.1094 ... 23.1094 40.5789                 
    *                
    * HINT: The polygon should be closed, i.e. first point should match last point               
    * HINT2: Latitude and Longitude can be obtained by from GoogleMaps by right-clicking on the desired points and selecting 'What is here'.
    * */ 


    //============ Gather usage analytics ================
	/** Key for Flurry analytics that monitors usage of application see www.flurry.com 
	 *  Use your own key. 
	 *  
	 *  To set default value (enable or disable) Flurry analytics see at res/myprefs.xml go to 
	 *     
	 * android:key="AnalyticsSW"
	 * 
	 * and set 
	 * android:defaultValue="true" or "false"
	 *     
	 */
	 public static String Flurry_Key = "00000000000000000000";

    //====== Google map api key ====================
    /**
     * A) Important: You should include the "google-play-services_lib" project to your project as an external library project. 
     * 
     * B) Insert your google maps v2 key in AndroidManifest.xml -> 
     *    <meta-data android:name="com.google.android.maps.v2.API_KEY"  android:value="XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"/>
     * 
     * 	   You should have two map api keys: 
     * 	   1) For debugging related to your android debug key 
     * 	   2) For release version related to your android release key. 
     * 
     * 	   See in android developer about how to generate map api keys according to your android key.  
     * 
     */
	 
 
	 //==== Predefined User Location ===========
	 /**
	  *  Define a location which can be used when user position is unavailable,
	  *  such as provider is null, GPS fix not feasible etc.
	  * 
	  */
	 public static double locUserPred_Lat = 40.5469;
	 public static double locUserPred_Long = 23.0197;
	 
	 //===== Default view range =================
	 /**
	  * Issues are downloaded within user's range. Set default value here (meters).
	  * 20000000 m is considered the infinite range.
	  */
	 public static int initRange = 20000000 ;
}