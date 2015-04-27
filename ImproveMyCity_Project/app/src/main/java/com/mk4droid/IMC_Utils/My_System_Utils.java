// My_System_Utils 
package com.mk4droid.IMC_Utils;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.mk4droid.IMC_Store.Constants_API;
import com.mk4droid.IMCity_PackDemo.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.List;

/**
 * Android system utilities: Copy a file
 * 
 * @copyright   Copyright (C) 2012 - 2015 Information Technology Institute ITI-CERTH. All rights reserved.
 * @license     GNU Affero General Public License version 3 or later; see LICENSE.txt
 * @author      Dimitrios Ververidis for the Multimedia Group (http://mklab.iti.gr). 
 *
 */
public class My_System_Utils {
   
	/**
	 * Decode a byte array to a Bitmap with a process requiring a low amount of memory
	 * @param bt image as bytes
	 * @return image as bitmap
	 */
	public static Bitmap LowMemBitmapDecoder(byte[] bt, Context ctx){
		Bitmap bm = null;
		
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPreferredConfig = Bitmap.Config.RGB_565;
		options.inPurgeable = true;
		options.inInputShareable = true;
		options.inJustDecodeBounds = false;

        if (bt!=null)
            bm = BitmapFactory.decodeByteArray(bt, 0, bt.length, options);
        else
            bm = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.map_categ_default_icon);
		
		return bm;
	}
	
	//------------- Copy file to temp -------------
	/**
	 * Copy a file locally from Source to Target paths
	 * 
	 * @param fnameS
	 * @param fnameT
	 */
	public static void FCopy(String fnameS, String fnameT){

		try{

            Log.e("A",fnameS + "->" + fnameT);

			InputStream in=new FileInputStream(fnameT);
			OutputStream out = new FileOutputStream(new File(fnameS));                       



			byte[] buffer = new byte[1024];
			int read;
			while ((read = in.read(buffer)) != -1) {
				out.write(buffer, 0, read);
			}



			in.close();
			in = null;



			// write the output file
			out.flush();
			out.close();



			out = null;
		}  catch (FileNotFoundException fnfe1) {
			Log.e(Constants_API.TAG, "My_System_Utils FCopy FileNotFoundException: " + fnfe1.getMessage());
		}  catch (Exception e) {
			Log.e(Constants_API.TAG, "My_System_Utils FCopy Exception:" + e.getMessage());
		}
		//------------------------------------
	}

	
	
    /**        isServiceRunning
	 * 
	 * Check if a service is running (Data or Location)
	 * 
	 * @param serviceClassName
	 * @return
	 */
	public static boolean isServiceRunning(String serviceClassName, Context ctx){
        final ActivityManager activityManager = (ActivityManager)ctx.getSystemService(Context.ACTIVITY_SERVICE);
        final List<RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);

        for (RunningServiceInfo runningServiceInfo : services) {
            if (runningServiceInfo.service.getClassName().equals(serviceClassName)){
                return true;
            }
        }
        return false;
     }
	
	
	/**
	 * Convert InputStream to String
	 * 
	 * To convert the InputStream to String we use the BufferedReader.readLine()
	 * method. We iterate until the BufferedReader return null which means
	 * there's no more data to read. Each line will appended to a StringBuilder
	 * and returned as String.
	 */
	public static String convertStreamToString(InputStream is) {
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}
	
	/**
	 * Check for saved preferences that were stored in wrong format.
	 * @param shPrefs
	 */
	public static void CheckPrefs(SharedPreferences shPrefs){
		
		try {		
			shPrefs.getInt("distanceData"   , Constants_API.initRange);
		} catch (Exception e){
			shPrefs.edit().remove("distanceData").commit();
		}
		
		try {
			shPrefs.getInt("distanceDataOLD", Constants_API.initRange);
		} catch (Exception e){
			shPrefs.edit().remove("distanceDataOLD").commit();
		} 
	}
}


//============ Compression schemes for future use ===================

////============= UTFtoString ================
///** 
// * Convert UTF-8 word to string word
// */
//public static String UTFtoString(String myString){ 
//	
//	String str = myString.split(" ")[0];
//	str = str.replace("\\","");
//	String[] arr = str.split("u");
//	String text = "";
//	for(int i = 1; i < arr.length; i++){
//		int hexVal = Integer.parseInt(arr[i], 16);
//		text += (char)hexVal;
//	}
//	// Text will now have Hello
//	return text;
//}


///**
// * Decompresses a zlib compressed string.
// */
//public static String decompress(String compressedSTR, String Encoding)
//{
//	
//	
//	byte[] compressedBytes = null; // Compress(Encoding);
//	try {
//		compressedBytes = compressedSTR.getBytes(Encoding);
//		Log.e("compressedBytes", new String(compressedBytes,Encoding));
//	} catch (UnsupportedEncodingException e1) {
//		e1.printStackTrace();
//	}
//	
//	// ----------------  Decompress the bytes ----------
//	 int uncompressedBufferLength = 0;
//	 Inflater decompresser = new Inflater(false);
//	 
//	 decompresser.setInput(compressedBytes, 0, compressedBytes.length);
//
//	 byte[] uncompressedBuffer = new byte[700];
//	 try {
//		 uncompressedBufferLength =	 decompresser.inflate(uncompressedBuffer);
//		 Log.e("resultLength", "A " + Integer.toString(uncompressedBufferLength));
//	 } catch (DataFormatException e) {
//		 Log.e("DataFormatException","A " + e.getCause());
//		 e.printStackTrace();
//	 }
//	 decompresser.end();
//	
//	 //-------------- Convert bytes to String ----------
//	 String resSTR = "";
//	try {
//		resSTR = new String(uncompressedBuffer,Encoding);
//	} catch (UnsupportedEncodingException e) {
//		// TODO Auto-generated catch block
//		e.printStackTrace();
//	}
//	 Log.e("result", resSTR);
//    
//    return resSTR;
//}


//public static byte[] Compress(String Encoding){
//
//	//// Encode a String into bytes
//	 //String inputString = "blahblahblah??";
//	   
//	  String inputString = ReadFile("mnt/sdcard/data/result.txt");
//	   
//	 byte[] input = null;
//	 try {
//	 	input = inputString.getBytes(Encoding);
//	 } catch (UnsupportedEncodingException e1) {
//	 	e1.printStackTrace();
//	 }
//	 
//	 // Compress the bytes
//	 byte[] output = new byte[500000];
//	 Deflater compresser = new Deflater();
//	 compresser.setInput(input);
//	 compresser.finish();
//	 int compressedDataLength = compresser.deflate(output);
//	
//	  
//	 Log.e("When compressed then take bytes=", Integer.toString(compressedDataLength ));
//	 
//	 
//	 
//	 return output;
//   }
//    
//   //------------- ReadFile from sdcard ----------------
//   public static String ReadFile(String path){
//	   
//	   File file = new File(path);
//	   StringBuilder text = new StringBuilder();
//
//	   try {
//	       BufferedReader br = new BufferedReader(new FileReader(file));
//	       String line;
//
//	       while ((line = br.readLine()) != null) {
//	           text.append(line);
//	       }
//	   }
//	   catch (IOException e) {
//	   }
//
//	   return text.toString();
//   }