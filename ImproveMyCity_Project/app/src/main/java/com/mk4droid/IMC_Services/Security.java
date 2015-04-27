/** SecurityMD5 */
package com.mk4droid.IMC_Services;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.mk4droid.IMC_Store.Constants_API;
import com.mk4droid.IMC_Store.Phptasks;
import com.mk4droid.IMC_Utils.My_Crypt_Utils;
import com.mk4droid.IMC_Utils.RestCaller;
import com.mk4droid.IMCity_PackDemo.R;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Authenticate the user using a username and password. Password is sent encrypted using the Constants.EncKey
 * See EncWrapper.
 * 
 * @copyright   Copyright (C) 2012 - 2015 Information Technology Institute ITI-CERTH. All rights reserved.
 * @license     GNU Affero General Public License version 3 or later; see LICENSE.txt
 * @author      Dimitrios Ververidis for the Multimedia Group (http://mklab.iti.gr). 
 *
 */
public class Security  {
	static int tlv = Toast.LENGTH_LONG;
	static boolean AuthFlag = false;
	static Context ctx; 
	static String Username;
	static String Password;
	static boolean updVotes = false;


	//========================  AuthFun       ==============================================
	/**
	 * Authenticate user using username and password. Password is encrypted and then sent. 
	 * 
	 * @param Username   latin characters
	 * @param Password   actual password up to 16 chars (latin characters)
	 * @param resources  for UI
	 * @param ctx        for current Android activity
	 * @return
	 */
	public static void AuthFun(String Username_in, String Password_in, Resources resources, Context ctx_in, boolean updVotes_in){

		ctx = ctx_in;
		Username = Username_in;
		Password = Password_in;
		updVotes = updVotes_in;

		if (Username.equals(""))
			ctx.sendBroadcast(new Intent("android.intent.action.MAIN").putExtra("Authenticated", "failed"));

		if (InternetConnCheck.getInstance(ctx).isOnline(ctx)) {
			new AsyncAuth(
							Constants_API.COM_Protocol + Constants_API.ServerSTR + Constants_API.phpExec,
							"GET",
							new String[]{ "option","com_improvemycity",
									"task", Phptasks.TASK_AUTH_USER,
									"format",    "json",
									"username",   Username_in,
									"password",   EncWrapper(Password_in)},
									"UTF-8",
					"CalledBySecurity").execute();

		} else {
			Toast.makeText(ctx, resources.getString(R.string.NoInternet), tlv).show();
			ctx.sendBroadcast(new Intent("android.intent.action.MAIN").putExtra("Authenticated", "failed"));
		}
	}



    /**
     * Asynchronous user authentication check
     * 
     * @copyright   Copyright (C) 2012 - 2015 Information Technology Institute ITI-CERTH. All rights reserved.
     * @license     GNU Affero General Public License version 3 or later; see LICENSE.txt
     * @author      Dimitrios Ververidis for the Multimedia Group (http://mklab.iti.gr). 
     *
     */
	private static class AsyncAuth extends AsyncTask<String, String , String>{

		private String url;
		private String method;
		private String[] paramsJSON;
		private String encoding;
		private String calledBy;
		RestCaller rc;

		public AsyncAuth(String url_in,String  method_in, String[] paramsJSON_in,  String encoding_in, String CalledBy_in) {
			url    = url_in;
			method = method_in;
			paramsJSON = paramsJSON_in;
			encoding=encoding_in;
			calledBy=CalledBy_in; 
		}


		/* (non-Javadoc)
		 * @see android.os.AsyncTask#onPreExecute()
		 */
		@Override
		protected void onPreExecute() {

			rc = new RestCaller();

			super.onPreExecute();
		}

		@Override
		protected String doInBackground(String... params) {
			String response =  rc.now(url, method, paramsJSON, encoding, calledBy);
			return response;
		}

		@Override
		protected void onPostExecute(String response) {

			if (response==null){
				ctx.sendBroadcast(new Intent("android.intent.action.MAIN").putExtra("Authenticated", "failed"));
			} else if(response.length() > 0){

				JSONObject jO = null;
				int UserID = 0; 

				try {
					jO = new JSONObject(response);
					UserID = jO.getInt("id");
				} catch (JSONException e1) {
					Log.d(Constants_API.TAG, "SecurityMD5:AuthFun:"+e1.getMessage());
				}

				if (UserID != 0)
					AuthFlag = true;
				else 
					AuthFlag = false;

				//	--------------- parse json data --------------------------
				if (!AuthFlag){
					savePreferences("AuthFlag", false, "Boolean", ctx);
					Toast.makeText(ctx, ctx.getResources().getString(R.string.tryagain), Toast.LENGTH_LONG).show();
					ctx.sendBroadcast(new Intent("android.intent.action.MAIN").putExtra("Authenticated", "failed"));
				} else {

					String UserID_STR, UserRealName;
					try {
						UserID_STR     = jO.getString("id"); 
						UserRealName   = jO.getString("fullname");

						DatabaseHandler dbHandler = new DatabaseHandler(ctx);

						if (!dbHandler.db.isOpen())
							dbHandler = new DatabaseHandler(ctx);

						if (updVotes){
							dbHandler.AddUpdUserVotes(Username, Password, ctx);
							dbHandler.db.close();
						}

						savePreferences("UserID_STR", UserID_STR, "String", ctx);
						savePreferences("AuthFlag", true, "Boolean", ctx);
						savePreferences("UserRealName", UserRealName, "String", ctx);
						savePreferences("PasswordAR", Password, "String", ctx);
						savePreferences("UserNameAR", Username, "String", ctx);

						Toast.makeText(ctx, ctx.getResources().getString(R.string.Welcome)+", "+UserRealName, tlv).show();
						
						ctx.sendBroadcast(new Intent("android.intent.action.MAIN").putExtra("Authenticated", "success"));

					} catch (JSONException e) {
						ctx.sendBroadcast(new Intent("android.intent.action.MAIN").putExtra("Authenticated", "failed"));
						Log.e(Constants_API.TAG, "SecurityMD5:AuthFun:"+e.getMessage());
					}
				} 	
			} else {
				Toast.makeText(ctx, ctx.getResources().getString(R.string.tryagain), Toast.LENGTH_LONG).show();
				ctx.sendBroadcast(new Intent("android.intent.action.MAIN").putExtra("Authenticated", "failed"));
			}

			super.onPostExecute(response);

		} // End of postExecute
	}
	//---------------------- End of Async ------------------ 





	//========================  EncWrapper       ==============================================
	/**
	 * Encrypt Password using the EncKey and transform into string of hexadecimal chars. 
	 * 
	 * @param Password
	 * @return
	 */
	public static String EncWrapper(String Password){
		My_Crypt_Utils mcrypt = new My_Crypt_Utils();
		String encrypted = null;
		try {

			byte[] prior = mcrypt.encrypt(Password);
			encrypted = bytesToHex( prior );
		} catch (Exception e) {
			e.printStackTrace();
		}
		return encrypted;		
	}

	//======================== bytesToHex  ==============================================
	/**
	 * Convert an array of bytes to a string of hexadecimals.
	 * 
	 * @param data
	 * @return
	 */
	public static String bytesToHex(byte[] data){
		if (data==null)
			return null;

		int len = data.length;
		String str = "";
		for (int i=0; i<len; i++) {
			if ((data[i]&0xFF)<16)
				str = str + "0" + java.lang.Integer.toHexString(data[i]&0xFF);
			else
				str = str + java.lang.Integer.toHexString(data[i]&0xFF);
		}
		return str;
	}

	//================== savePreferences =============================
	/**
	 * Save in preferences if user is authenticated or not.
	 * 
	 * @param key    "AuthFlag"
	 * @param value  boolean, true or false
	 * @param type "Boolean"
	 */
	private static void savePreferences(String key, Object value, String type, Context ctx){
		SharedPreferences       shPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		SharedPreferences.Editor editor = shPrefs.edit();

		if (type.equals("String")) 
			editor.putString(key, (String) value);
		else 
			editor.putBoolean(key, (Boolean) value);

		editor.commit();
	}

}


///**                Make_DBST
// *  Make DB stored md5+salt from the Password type in the registration Phase
// */
//public static String Make_DBST(String RegPass){
//	String salt  = Salt(32); 
//	String mymd5 = md5(RegPass+salt);
//	return mymd5+":"+salt;
//}
//
///** Make random character string of N chars from the pool of number and small case letters */   
//public static String Salt(int N){
//	//------------ chars pool in Salt -----------
//	char[] symbols = new char[36];
//	for (int idx = 0; idx < 10; ++idx)
//		symbols[idx] = (char) ('0' + idx);
//	for (int idx = 10; idx < 36; ++idx)
//		symbols[idx] = (char) ('a' + idx - 10);
//	//------------------------------------------
//	Random random = new Random();
//	char[] buf = new char[N];
//	for (int i = 0; i <N; ++i) 
//	      buf[i] = symbols[random.nextInt(symbols.length)];
//	
//	return new String(buf);
//}


///** Make random character string of N chars from the pool of number */
//public String SaltNumber(int len){
//	//------------ chars pool in Salt -----------
//	char[] symbols = new char[10];
//	for (int idx = 0; idx < 10; ++idx)
//		symbols[idx] = (char) ('0' + idx);
//	
//	//------------------------------------------
//	Random random = new Random();
//	char[] buf = new char[len];
//	for (int i = 0; i <len; ++i) 
//	      buf[i] = symbols[random.nextInt(symbols.length)];
//	return new String(buf);
//}

////---------------------------------------------------------------------
///**         Compare Typed Password With DB STored password          
// * ---------------------------------------------------------------------*/
//public static boolean Compare(String myTypedPass, String DBST){
//	
//    String[] hashparts = DBST.split(":");
//    String estimatedPartZero = md5(myTypedPass + hashparts[1]);
//    boolean FlagAuth = false;
//    
//    if (estimatedPartZero.equals(hashparts[0]))
//          FlagAuth = true;	
//	
//	return FlagAuth;
//}


////--------------------------------------
///**              Generate MD5            */
////--------------------------------------
//public static String md5(String myPass){
//	
//	MessageDigest digest;
//	String hash = "not ready";
//	
//    try {
//        digest = MessageDigest.getInstance("MD5");
//        byte utf8_bytes[] = myPass.getBytes();
//        digest.update(utf8_bytes,0,utf8_bytes.length);
//        hash = new BigInteger(1, digest.digest()).toString(16);
//    } 
//    catch (NoSuchAlgorithmException e) {
//        e.printStackTrace();
//    }
//	
//    
//    return hash;
//}
