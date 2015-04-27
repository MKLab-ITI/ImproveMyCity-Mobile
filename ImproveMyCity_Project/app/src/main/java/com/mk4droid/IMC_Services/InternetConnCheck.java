/** InternetConnCheck */

package com.mk4droid.IMC_Services;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.mk4droid.IMC_Store.Constants_API;

/** Instant check for internet access */
public class InternetConnCheck {
	private static InternetConnCheck instance = new InternetConnCheck();
	ConnectivityManager connectivityManager;
	NetworkInfo wifiInfo, mobileInfo;
	static Context context;
	boolean connected = false;

	
	public static InternetConnCheck getInstance(Context ctx) {
		context = ctx;
		return instance;
	}

	public Boolean isOnline(Context con) {
		try {
			connectivityManager = (ConnectivityManager) con.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
			connected = networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected();
			return connected;
		} catch (Exception e) {
			Log.e(Constants_API.TAG, "InternetConnCheck: " + e.getMessage());
		}
		return connected;
	}
}