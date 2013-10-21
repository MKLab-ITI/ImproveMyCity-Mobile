/** GEO */
package com.mk4droid.IMC_Utils;

import java.io.InputStream;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.mk4droid.IMC_Activities.Fragment_NewIssueB;
import com.mk4droid.IMC_Store.Constants_API;
import com.mk4droid.IMCity_PackDemo.R;

/**
 * Reverse geocoding: (Latitude, Longitude) -> X MyStreet, MyCountry
 * 
 * @copyright   Copyright (C) 2012 - 2013 Information Technology Institute ITI-CERTH. All rights reserved.
 * @license     GNU Affero General Public License version 3 or later; see LICENSE.txt
 * @author      Dimitrios Ververidis for the Multimedia Group (http://mklab.iti.gr). 
 *
 */
public class GEO {

	//===========  ConvertGeoPointToAddress ============================
	/**
	 * Major method using the java class object of google api. 
	 * If not success, then try to get with REST from google http api.
	 *  
	 * @param pt    Longitude and latitude information
	 * @param ctx   current activity context
	 * @return
	 */
	static PolygonOptions options;

	public static String ConvertGeoPointToAddress(LatLng pt, Context ctx){

		Address maddress = null;
		try {
			Geocoder geocoder = new Geocoder(ctx, Locale.getDefault());
			List<Address> list = geocoder.getFromLocation(pt.latitude, pt.longitude, 1);
			if (list != null && list.size() > 0) {
				maddress = list.get(0);
			}
		} catch (Exception e) {
			Log.e(Constants_API.TAG, "Gecoder falied: I will try with REST");
			new RevGEO_Try2_Asynch(pt.latitude, pt.longitude).execute();
		}

		String Address_STR = "";

		if (maddress!=null){
			for (int i=0; i< maddress.getMaxAddressLineIndex(); i++)
				Address_STR += maddress.getAddressLine(i) + ", ";

			Address_STR += maddress.getCountryName();
		}

		return Address_STR; 
	}


	/**
	 * 
	 * Reverse geocoding with rest
	 * 
	 * @param lat
	 * @param lng
	 * @return
	 */
	private static class RevGEO_Try2_Asynch extends AsyncTask<String, String, String>{

		double lat;
		double lng;

		public RevGEO_Try2_Asynch(double lat_in, double lng_in){
			lat = lat_in;
			lng = lng_in;
		}

		@Override
		public String doInBackground(String... params) {

			RestCaller rc = new RestCaller();	
			String response =  rc.now(  //"http://maps.googleapis.com/maps/api/geocode/json?"latlng=40.567,22.99&sensor=false,
					"http://maps.googleapis.com/maps/api/geocode/json", 
					"GET",  
					new String[]{
							"latlng",""+lat+","+lng,
							"sensor","false"}, "UTF-8","RevGEO_Try2");

			if (response == null)
				response = "";
			

			return response;
		}


		/* (non-Javadoc)
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(String response) {

			try {
				JSONObject jo = new JSONObject(response  );
				JSONArray ja = new JSONArray(jo.getString("results"));
				JSONObject jo1 = new JSONObject( ja.get(0).toString());
				String out = jo1.get("formatted_address").toString();

				if (Fragment_NewIssueB.etAddress!=null)
					Fragment_NewIssueB.etAddress.setText(out);

			} catch (JSONException e) {
				if (Fragment_NewIssueB.ctx != null && Fragment_NewIssueB.resources!=null && Fragment_NewIssueB.etAddress!=null){
					Toast.makeText(Fragment_NewIssueB.ctx, 
							Fragment_NewIssueB.resources.getString(R.string.WriteAddress), 
							Toast.LENGTH_LONG).show();

					Fragment_NewIssueB.etAddress.setText("");
				}
			}

			super.onPostExecute(response);
		}
	}

	/**
	 * Draw polygon borders on the map defining the municipality
	 * 
	 * @param mgmap
	 * @param resources
	 */
	public static Polygon MakeBorders (GoogleMap mgmap, Resources res) {  

		String str = "";
		// parse from raw.polygoncoords.txt
		try {
			InputStream in_s = res.openRawResource(R.raw.polygoncoords);
			byte[] b = new byte[in_s.available()];
			in_s.read(b);
			str =  new String(b) ;
		} catch (Exception e) {
			Log.e("Error","can't read polygon.");
		}

		Polygon mPoly = null;

		if (options == null){
			if (str.length() > 0){
				String[] points = str.split(" ");
				options = new PolygonOptions();
				for (int i=0; i<points.length; i=i+2)
					options.add(new LatLng(Double.parseDouble(points[i+1]), Double.parseDouble(points[i]) ));
			}	
		} 

		if (mgmap != null)
			mPoly = mgmap.addPolygon(options
					.strokeWidth(4)
					.strokeColor(Color.BLACK)
					.fillColor(Color.argb(10, 0, 100, 0)));

		return mPoly;
	}


	/** 
	 * Check whether a the marker position is inside the municipality borders (polygon)
	 * 
	 * @poly the polygon borders
	 * @lng longitude of marker
	 * @lat latitude of marker      
	 */
	public static boolean insidePoly(Polygon poly, double lng, double  lat){

		List<LatLng> p = poly.getPoints();

		int polyPoints = poly.getPoints().size();
		int polySides  = polyPoints - 1;

		double[] polyY = new double[polyPoints];
		double[] polyX = new double[polyPoints];

		for (int i = 0; i < polyPoints; i++){
			polyY[i] = p.get(i).latitude;
			polyX[i] = p.get(i).longitude;
		}

		boolean oddTransitions = false;
		for( int i = 0, j = polySides -1; i < polySides; j = i++ ) {
			if( ( polyY[ i ] < lat && polyY[ j ] >= lat ) || ( polyY[ j ] < lat && polyY[ i ] >= lat ) ) {
				if( polyX[ i ] + ( lat - polyY[ i ] ) / ( polyY[ j ] - polyY[ i ] ) * ( polyX[ j ] - polyX[ i ] ) < lng ) {
					oddTransitions = !oddTransitions;          
				}
			}
		}
		return oddTransitions;
	}



    /**
     * Convert the distance to a string of distance plus its units e.g. m or km 
     *     
     * @param distanceData distance (range of data)
     * @return
     */
    public static String DistanceToText(int distanceData){
    	String res = "";
    	
		if (distanceData >= 10000 && distanceData < Constants_API.initRange )
			res = Float.toString(distanceData/1000) + "km";
        else if (distanceData < 10000)
        	res = Integer.toString(distanceData) + "m";
        else if (distanceData == Constants_API.initRange)
        	res = "Inf";
        
		return res;
	}
}

