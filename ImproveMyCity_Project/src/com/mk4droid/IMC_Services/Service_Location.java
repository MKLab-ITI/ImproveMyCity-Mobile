/** Service_Location */
package com.mk4droid.IMC_Services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.mk4droid.IMC_Activities.FActivity_TabHost;
import com.mk4droid.IMC_Store.Constants_API;

/**
 *  Find current location and broadcast if location has changed
 *
 * @copyright   Copyright (C) 2012 - 2013 Information Technology Institute ITI-CERTH. All rights reserved.
 * @license     GNU Affero General Public License version 3 or later; see LICENSE.txt
 * @author      Dimitrios Ververidis for the Multimedia Group (http://mklab.iti.gr). 
 *
 */
public class Service_Location extends Service {


	/** User location */
	public static Location locUser    = new Location("point User");

	/** Location by Network */ 
	public static Location locNetwork = new Location("network");
	
	/** Location by GPS */
	public static Location locGPS = new Location("gps");

	/** Predefined location of the user in case no provider is found */
	public static Location locUserPred    = new Location("UserPred");

	/** Handler for showing debug messages on Screen */
	public static Handler handlerToast;
	
	/** Final position decided */
	public static Location locOptimum = new Location("Optimum");

	
	/** DEBUG MODE is for verbosing crucial steps in this activity */
	boolean DEBUG_LOC = false;

	int tlv  = Toast.LENGTH_LONG;

	/**  Previous session position */
	SharedPreferences mshPrefs; 

	//----------- Location Manager for GPS and WIFI -------------
	LocationManager lm;
	LocationListener locationListenerGPS,locationListenerNetwork;
	Context ctx;
	
	private final IBinder binder=new LocalBinder();

	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return 1; 
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return binder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		return false;
	}


	public class LocalBinder extends Binder {
		public Service_Location getService() {
			return Service_Location.this;
		}
	}


	//============= onCreate ===================
	/**
	 *   Set GPS, Wifi, and GPSStatus listeners
	 */
	@Override
	public void onCreate() {
		super.onCreate();


		//----- Handler for Redrawing Markers from update thread ------------
		handlerToast = new Handler()
		{
			public void handleMessage(Message msg)
			{
				if (DEBUG_LOC){
					if (msg.arg1 == 1){

						//------- Location report
						String prov = locUser.getProvider() + " vs. " + locOptimum.getProvider();
						Toast.makeText(FActivity_TabHost.ctx, prov, Toast.LENGTH_LONG).show();
						String tag =   "Dist:" + ((int)locUser.distanceTo(locOptimum)) + "m";
						Toast.makeText(FActivity_TabHost.ctx,  tag, Toast.LENGTH_LONG).show();
						String mes ="Acc: " + ((int) locUser.getAccuracy())  + "m vs. " + ((int)locOptimum.getAccuracy()) + "m";
						Toast.makeText(FActivity_TabHost.ctx,  mes, Toast.LENGTH_LONG).show();
					} else if (msg.arg1 == 2) {
						Toast.makeText(FActivity_TabHost.ctx, "DISTANCE UPD: YES", Toast.LENGTH_LONG).show();
					} else if (msg.arg1 == 3) {
						Toast.makeText(FActivity_TabHost.ctx, "DISTANCE UPD: NO", Toast.LENGTH_LONG).show();
					}
				}
				super.handleMessage(msg);
			}
		};

		//------ Predefined (Default) position ----------
		locUserPred.setLatitude(Constants_API.locUserPred_Lat);
		locUserPred.setLongitude(Constants_API.locUserPred_Long);
		locUserPred.setAccuracy(0);
		locUserPred.setProvider("Predefined");

		//---------------------
		ctx = getApplicationContext();

		mshPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);

		//------ Previous session position ------
		double locUserPreviousLat  = (double) mshPrefs.getFloat("locUserLat", 0);
		double locUserPreviousLong = (double) mshPrefs.getFloat("locUserLong", 0);
		float locUserPreviousAcc =            mshPrefs.getFloat("locUserAcc", 0);


		//-------------- LOCATION GPS and WIFI ------------------------
		lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		// Get Last known location
		Criteria crit = new Criteria();
		crit.setAccuracy(Criteria.ACCURACY_COARSE);
		final String provider = lm.getBestProvider(crit, true);

		if (provider==null){
			decideLocUPD(locUser, null, null, locUserPred, "NULL LOC PROVIDER : GET PREDEFINED LOC");
		} else{

			// --------- get previous location as a reference from previous time
			locUser.setLatitude(locUserPreviousLat);
			locUser.setLongitude(locUserPreviousLong);
			locUser.setAccuracy(locUserPreviousAcc);
			locUser.setProvider("Previous Session");
			//
			initPosListenersNetworksGPS();

			//  DELAYED COMPARISON (No optimum was found) AND EMERGENCY PLAN
			new Thread(new Runnable() {
				public void run() {

					try {
						Thread.sleep(6000);

						if (locOptimum.getLatitude()==0 && locOptimum.getLongitude()==0){ // if location not found yet from WifiGPS

							Location locLast = lm.getLastKnownLocation(provider); // GET lastlocation

							if (locLast!=null){
								if (locLast.getLatitude() != 0 && locLast.getLatitude()!=0){ // if last location is good then
									//Log.e("SL", "FROM LAST LOCATION");
									decideLocUPD(locUser, null, null, locLast, "LOC FROM LAST SESSION");
								} 
							} else {                                              // if last location zero then get predifined loc
								//Log.e("SL", "DEFAULT AT PREDEFINED LOCATION");
								//Toast.makeText(FActivity_TabHost.ctx, getResources().getString(R.string.Icannotfindyourpos), tlv).show();
								decideLocUPD(locUser, null, null, locUserPred, "NOT NULL PROV : UNABLE TO LOC : GET PREDEFINED LOC");
							}
						}  
					} catch (InterruptedException e) {
					}

				}}).start();

		}// ------- end for provider
	} // end for create

	//=========== DecideDataUpdateDueLoc ===================
	/**
	 * Broadcast current address and trigger to update data due to significant location change.
	 * 
	 * @param distanceDiff threshold of distance. If distance differs at least this value then data should be updated.
	 * @param locN The new location fix  
	 */
	public void decideLocUPD(Location locPrevSes, Location locG, Location locN, Location locP, String FromWhat){

		if (locG != null || locN !=null){		
			if (locG.getLatitude() == 0 && locG.getLongitude() == 0){ // wait for locN
				return;
			}

			if (locN.getLatitude() == 0 && locN.getLongitude() == 0){ // wait for locG
				return;
			}
		}

		Location locOpt = locPrevSes;

		if (locG == null && locN ==null){
			locOpt = locP;
		}


		if (locG != null && locN != null){
			if (locG.getAccuracy() < locN.getAccuracy() )
				locOpt = locG;
			else 
				locOpt = locN;
		}

		locOptimum = locOpt;

		float distanceDiff = locPrevSes.distanceTo(locOpt) - locPrevSes.getAccuracy() - locOpt.getAccuracy();


		Message msg = new Message();
		msg.arg1 = 1;
		handlerToast.sendMessage(msg);


		//----------- Update data if diff > 200 -----------
		if (distanceDiff > 500){

			Message msg2 = new Message();
			msg2.arg1 = 2;
			handlerToast.sendMessage(msg2);

			//--- Update userLocation				
			locUser.setLongitude(locOpt.getLongitude());
			locUser.setLatitude(locOpt.getLatitude());

			//---------- Save as previous location 
			SharedPreferences.Editor editor = mshPrefs.edit();
			editor.putFloat("locUserLat" , (float) locOpt.getLatitude());
			editor.putFloat("locUserLong", (float) locOpt.getLongitude());
			editor.putFloat("locUserAcc", (float) locOpt.getAccuracy());
			editor.commit();

			// ------- Broadcast data Refresh through a handler ---------
			sendBroadcast(new Intent("android.intent.action.MAIN").putExtra("LocChanged",   "LocChanged"));  
		} else {

			Message msg3 = new Message();
			msg3.arg1 = 3;
			handlerToast.sendMessage(msg3);
		}
	}

	/**
	 *     Initialize position listeners 1) Networks 2) GPS
	 */
	private void initPosListenersNetworksGPS() {

		//---------------  Networks Location Listener 3G or WIFI ------------
		if(InternetConnCheck.getInstance(ctx).isOnline(ctx)){

			locationListenerNetwork = new LocationListener() {
				public void onLocationChanged(Location location) {

					locNetwork = location;
					decideLocUPD(locUser, locGPS, locNetwork, null, "LOC FOUND via NETWORK");
				}

				public void onProviderDisabled(String provider) {}
				public void onProviderEnabled(String provider) {}
				public void onStatusChanged(String provider, int status, Bundle extras) {}
			};
			lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 1000f, locationListenerNetwork);
		}

		//---------------- Location Listener GPS ----------------
		locationListenerGPS = new LocationListener() {
			@Override
			public void onLocationChanged(Location location) {
				locGPS = location;
				decideLocUPD(locUser, locGPS, locNetwork, null, "LOC FOUND via GPS");
			}

			@Override
			public void onProviderDisabled(String provider) {}
			@Override
			public void onProviderEnabled(String provider) {}
			@Override
			public void onStatusChanged(String provider, int status, Bundle extras) {}
		};

		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 1000f, locationListenerGPS);



	}

	//=========== onDestroy ===================
	/** Remove all location listeners */
	public void onDestroy() {
		super.onDestroy();

		locNetwork.reset();
		locGPS.reset();
		locOptimum.reset();

		if (locationListenerNetwork!=null)
			lm.removeUpdates(locationListenerNetwork);       // Remove Wifi location listener

		if (locationListenerGPS!=null)
			lm.removeUpdates(locationListenerGPS);           // Remove GPS location listener

		stopSelf();
	};

}