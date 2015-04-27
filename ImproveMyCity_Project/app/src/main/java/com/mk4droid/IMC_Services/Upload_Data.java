// Upload_Data 
package com.mk4droid.IMC_Services;

import android.content.Context;
import android.util.Log;

import com.mk4droid.IMC_Store.Constants_API;
import com.mk4droid.IMC_Store.Phptasks;
import com.mk4droid.IMC_Utils.RestCaller;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
/**
 * Major methods to upload data to remote server are gathered in this class.
 *
 * @copyright   Copyright (C) 2012 - 2015 Information Technology Institute ITI-CERTH. All rights reserved.
 * @license     GNU Affero General Public License version 3 or later; see LICENSE.txt
 * @author      Dimitrios Ververidis for the Multimedia Group (http://mklab.iti.gr). 
 *
 */
public class Upload_Data {
	
	// ------------------------ SendVote ------------------
	/**
	 * Sent vote for IssueID by UserID
	 * 
	 * @param UserID
	 * @param IssueID
	 * @param UserNameSTR
	 * @param PasswordSTR
	 * @return
	 */
    public static boolean SendVote(int UserID, int IssueID, String UserNameSTR, String PasswordSTR){
    	
    	RestCaller rc = new RestCaller();	
		String response =  rc.now(
    			Constants_API.COM_Protocol + Constants_API.ServerSTR + Constants_API.phpExec , "GET", 
    			                 new String[]{
    					                      "option", "com_improvemycity", 
    					                      "task",    Phptasks.TASK_VOTE,
    					                      "format", "json",
    					                      "issueId", Integer.toString(IssueID),
    					                      "userid",  Integer.toString(UserID),
    					                      "username", UserNameSTR,
    					                      "password", Security.EncWrapper(PasswordSTR)}, "UTF-8", "SendVote");
    	if (response!=null)
    		return true;
    	else 
    		return false;
    		
	}
	
    //================ Register ==================
    /**
     * Register user
     *  
     * @param username    ASCII chars
     * @param email       
     * @param password    up to 16 ASCII chars
     * @param name        UTF-8 chars
     * @return
     */
    public static String SendRegistrStreaming(String username, String email, String password, String name){
    	
    	HttpURLConnection connection = null;
    	DataOutputStream outputStream = null;

    	String lineEnd    = "\r\n";
    	String twoHyphens = "--";
    	String boundary   =  "*****";

    	try
    	{
    		String urlSTR = Constants_API.COM_Protocol_S + 
    				        Constants_API.ServerSTR + 
    				        Constants_API.remoteImages + 
    				        Phptasks.TASK_REGISTER_USER;
    		
    		URL url    = new URL( urlSTR  );
    		connection = (HttpURLConnection) url.openConnection();

    		// Allow Inputs & Outputs
    		connection.setDoInput(true);
    		connection.setDoOutput(true);
    		connection.setUseCaches(false);

    		// Enable POST method
    		connection.setRequestMethod("POST");

    		connection.setRequestProperty("Connection", "Keep-Alive");
    		connection.setRequestProperty("Content-Type",  "multipart/form-data;boundary="+boundary);

    		outputStream = new DataOutputStream(connection.getOutputStream() );
    	    	    		
    		//---------------- Params ------------
    		// 1 Title
    		outputStream.writeBytes(twoHyphens + boundary + lineEnd);
    		outputStream.writeBytes("Content-Disposition: form-data; name=\"username\"" + lineEnd);
    		outputStream.writeBytes(lineEnd);
    		outputStream.writeBytes(username + lineEnd); 
    		
    		// 2 userid
    		outputStream.writeBytes(twoHyphens + boundary + lineEnd);
    		outputStream.writeBytes("Content-Disposition: form-data; name=\"email\"" + lineEnd);
    		outputStream.writeBytes(lineEnd);
    		outputStream.writeBytes(email + lineEnd);
         		
    		// 3 passwordSTR 
    		outputStream.writeBytes(twoHyphens + boundary + lineEnd);
    		outputStream.writeBytes("Content-Disposition: form-data; name=\"password\"" + lineEnd);
    		outputStream.writeBytes(lineEnd);
    		String passEnc = Security.EncWrapper(password);
    		outputStream.writeBytes( passEnc + lineEnd);
    		
    		// 4 name
    		outputStream.writeBytes(twoHyphens + boundary + lineEnd);
    		outputStream.writeBytes("Content-Disposition: form-data; name=\"name\"" + lineEnd);
    		outputStream.writeBytes(lineEnd);
    		outputStream.write((name + lineEnd).getBytes("UTF-8")); //"|"  
    		
    		outputStream.writeBytes(lineEnd);
    		outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
    		    		
    		// Responses from the server (code and message)
    		int serverResponseCode       = connection.getResponseCode();
    		String serverResponseMessage = connection.getResponseMessage();
    		
    		Log.e("serverResponseCode Message", Integer.toString(serverResponseCode) + " " + serverResponseMessage);
    		
    		outputStream.flush();
    		outputStream.close();
    		
    		String responseUTF = "error";
    		
    		//----------- response ---------
    		try {
    			InputStream in = connection.getInputStream(); 

    			//read it with BufferedReader
    	    	BufferedReader br = new BufferedReader(new InputStreamReader(in,"UTF-8"));
    	    	StringBuilder sb = new StringBuilder();
    	 
    	    	String line;
    	    	while ((line = br.readLine()) != null) {
    	    		sb.append(line);
    	    	} 
    		    String response = sb.toString();
    		    
    		    responseUTF=  response;
    		} catch (IOException e) {
    			Log.e(Constants_API.TAG, "Upload_Data:SendRegistrStreaming:"+ e.getMessage()); 
    		}
    		
    		connection.disconnect(); // Let's practice good hygiene
    		
    		return responseUTF;
    	}
    	catch (Exception ex){
    		Log.e(Constants_API.TAG, "Upload_Data:" + ex.getMessage());
    		return "false";
    	}
    }
    
    //---------------------------------------------
    //          SENT Comment String 
    //---------------------------------------------
    public static boolean SendCommentStreaming(int IssueID, int UserID, String CommentContent, Context ctx,
            String username, String password	   ){

    	HttpURLConnection connection = null;
    	DataOutputStream outputStream = null;

    	String lineEnd    = "\r\n";
    	String twoHyphens = "--";
    	String boundary   =  "*****";

    	try
    	{
    		String urlSTR = Constants_API.COM_Protocol_S + Constants_API.ServerSTR + 
    				        Constants_API.remoteImages +  Phptasks.TASK_COMMENT;
    		
    		URL url    = new URL( urlSTR  );
    		connection = (HttpURLConnection) url.openConnection();

    		// Allow Inputs & Outputs
    		connection.setDoInput(true);
    		connection.setDoOutput(true);
    		connection.setUseCaches(false);

    		// Enable POST method
    		connection.setRequestMethod("POST");
    		connection.setRequestProperty("Connection", "Keep-Alive");
    		connection.setRequestProperty("Content-Type",  "multipart/form-data;boundary="+boundary);

    		outputStream = new DataOutputStream(connection.getOutputStream() );
    	    	    		
    		//---------------- Params ------------
    		// 1 Title
    		outputStream.writeBytes(twoHyphens + boundary + lineEnd);
    		outputStream.writeBytes("Content-Disposition: form-data; name=\"issueId\"" + lineEnd);
    		outputStream.writeBytes(lineEnd);
    		outputStream.writeBytes(Integer.toString(IssueID) + lineEnd); 
    		
    		// 2 userid
    		outputStream.writeBytes(twoHyphens + boundary + lineEnd);
    		outputStream.writeBytes("Content-Disposition: form-data; name=\"userid\"" + lineEnd);
    		outputStream.writeBytes(lineEnd);
    		outputStream.writeBytes(Integer.toString(UserID) + lineEnd);
    		
    		// 3 Description
    		outputStream.writeBytes(twoHyphens + boundary + lineEnd);
    		outputStream.writeBytes("Content-Disposition: form-data; name=\"description\"" + lineEnd);
    		outputStream.writeBytes(lineEnd);
    		outputStream.write((CommentContent + lineEnd).getBytes("UTF-8")); //"|"  
    		
    		//4  UserName 
    		outputStream.writeBytes(twoHyphens + boundary + lineEnd);
    		outputStream.writeBytes("Content-Disposition: form-data; name=\"username\"" + lineEnd);
    		outputStream.writeBytes(lineEnd);
    		outputStream.writeBytes(username + lineEnd);
    		
    		// 5 passwordSTR 
    		outputStream.writeBytes(twoHyphens + boundary + lineEnd);
    		outputStream.writeBytes("Content-Disposition: form-data; name=\"password\"" + lineEnd);
    		outputStream.writeBytes(lineEnd);
    		String passEnc = Security.EncWrapper(password);
    		outputStream.writeBytes( passEnc + lineEnd);
    		
    		outputStream.writeBytes(lineEnd);
    		outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
    		    		
    		// Responses from the server (code and message)
    		int serverResponseCode       = connection.getResponseCode();
    		
    		Log.d(Constants_API.TAG + " serverResponseCode", " " + serverResponseCode);
    		
    		String serverResponseMessage = connection.getResponseMessage();
    		
    		outputStream.flush();
    		outputStream.close();
    		
    		if (serverResponseMessage.equals("OK"))
    			return true;
    		else 
    			return false;
    	}
    	catch (Exception ex){
    		Log.e(Constants_API.TAG, "Upload_Data: SendCommentStreaming:" + ex.getMessage());
    		return false;
    	}
    	
    }// Endof POST/*
    
    
    //=========   SENT ISSUE ====================== 
    /**
     * Submit issue to remote server.
     * 
     * @param ImPATHFN_Source  Image local path
     * @param ImFN_Target      Image path to store on remote server 
     * @param title            Issue title 
     * @param catid            Category of the issue    
     * @param Lat_D            latitude
     * @param Long_D           longitude
     * @param descriptionData_STR  Issue description
     * @param Address_STR          Address in textual format 
     * @param UserNameSTR         
     * @param PasswordSTR
     * @return
     */
    public static boolean SendIssue(String ImPATHFN_Source, String ImFN_Target, 
    		String title, int catid, double Lat_D, double Long_D, 
    		String descriptionData_STR, String Address_STR, String UserNameSTR, String PasswordSTR	   ){

    	HttpURLConnection connection = null;
    	DataOutputStream outputStream = null;

    	String lineEnd    = "\r\n";
    	String twoHyphens = "--";
    	String boundary   =  "*****";

    	int bytesRead, bytesAvailable, bufferSize;
    	byte[] buffer;
    	int maxBufferSize = 1*1024*1024;

    	try	{
    		String urlSTR = Constants_API.COM_Protocol_S + Constants_API.ServerSTR +  
    				        Constants_API.remoteImages +  Phptasks.TASK_ADD_ISSUE;

            Log.e("Upload urlSTR", urlSTR);

    		URL url    = new URL( urlSTR  );
    		connection = (HttpURLConnection) url.openConnection();

    		// Allow Inputs & Outputs
    		connection.setDoInput(true);
    		connection.setDoOutput(true);
    		connection.setUseCaches(false);

    		// Enable POST method
    		connection.setRequestMethod("POST");

    		connection.setRequestProperty("Connection", "Keep-Alive");
    		connection.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);

    		outputStream = new DataOutputStream(connection.getOutputStream() );

    		//---------------- Params ------------
    		// 1 Title
    		outputStream.writeBytes(twoHyphens + boundary + lineEnd);
    		outputStream.writeBytes("Content-Disposition: form-data; name=\"title\"" + lineEnd);
    		outputStream.writeBytes(lineEnd);
    		outputStream.write(("|" +title + lineEnd).getBytes("UTF-8")); 

    		// 2 catid
    		outputStream.writeBytes(twoHyphens + boundary + lineEnd);
    		outputStream.writeBytes("Content-Disposition: form-data; name=\"catid\"" + lineEnd);
    		outputStream.writeBytes(lineEnd);
    		outputStream.writeBytes(Integer.toString(catid) + lineEnd);

    		// 3 Lat
    		outputStream.writeBytes(twoHyphens + boundary + lineEnd);
    		outputStream.writeBytes("Content-Disposition: form-data; name=\"latitude\"" + lineEnd);
    		outputStream.writeBytes(lineEnd);
    		outputStream.writeBytes(Double.toString(Lat_D) + lineEnd);

    		// 4 Long
    		outputStream.writeBytes(twoHyphens + boundary + lineEnd);
    		outputStream.writeBytes("Content-Disposition: form-data; name=\"longitude\"" + lineEnd);
    		outputStream.writeBytes(lineEnd);
    		outputStream.writeBytes(Double.toString(Long_D) + lineEnd);

    		// 5 Description
    		outputStream.writeBytes(twoHyphens + boundary + lineEnd);
    		outputStream.writeBytes("Content-Disposition: form-data; name=\"description\"" + lineEnd);
    		outputStream.writeBytes(lineEnd);
    		outputStream.write(("|" + descriptionData_STR + lineEnd).getBytes("UTF-8")); 

    		// 6 address
    		outputStream.writeBytes(twoHyphens + boundary + lineEnd);
    		outputStream.writeBytes("Content-Disposition: form-data; name=\"address\"" + lineEnd);
    		outputStream.writeBytes(lineEnd);
    		outputStream.write(("|" + Address_STR + lineEnd).getBytes("UTF-8")); 


    		// 7 UserName 
    		outputStream.writeBytes(twoHyphens + boundary + lineEnd);
    		outputStream.writeBytes("Content-Disposition: form-data; name=\"username\"" + lineEnd);
    		outputStream.writeBytes(lineEnd);
    		outputStream.writeBytes(UserNameSTR + lineEnd);

    		// 8 passwordSTR 
    		outputStream.writeBytes(twoHyphens + boundary + lineEnd);
    		outputStream.writeBytes("Content-Disposition: form-data; name=\"password\"" + lineEnd);
    		outputStream.writeBytes(lineEnd);
    		String passEnc = Security.EncWrapper(PasswordSTR);
    		outputStream.writeBytes( passEnc + lineEnd);

    		//---------------- Image --------------
    		if (ImFN_Target.length()>0){
    			outputStream.writeBytes(twoHyphens + boundary + lineEnd);
    			outputStream.writeBytes("Content-Disposition: form-data; " +
    					"name=\"jform[photo]\";filename=\"" + ImFN_Target + "\"" + lineEnd  +"Content-Type: image/jpeg" +  lineEnd);
    			outputStream.writeBytes(lineEnd);

    			FileInputStream fileInputStream = new FileInputStream(new File(ImPATHFN_Source));

    			bytesAvailable = fileInputStream.available();


    			bufferSize = Math.min(bytesAvailable, maxBufferSize);
    			buffer = new byte[bufferSize];

    			// Read file
    			bytesRead = fileInputStream.read(buffer, 0, bufferSize);
    			while (bytesRead > 0)
    			{
    				outputStream.write(buffer, 0, bufferSize);
    				bytesAvailable = fileInputStream.available();
    				bufferSize = Math.min(bytesAvailable, maxBufferSize);
    				bytesRead = fileInputStream.read(buffer, 0, bufferSize);
    			}
    			fileInputStream.close();

    		} 

    		outputStream.writeBytes(lineEnd);
    		outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

    		// Responses from the server (code and message)
    		int serverResponseCode       = connection.getResponseCode();
    		String serverResponseMessage = connection.getResponseMessage();

    		Log.e("serverResponseCode", " " + serverResponseCode);
    		
    		outputStream.flush();
    		outputStream.close();

    		if (serverResponseMessage.equals("OK"))
    			return true;
    		else 
    			return false;

    	}
    	catch (Exception ex){
    		Log.e(Constants_API.TAG, "Upload_Data:SendIssue:" + ex.getMessage());
    		return false;
    	}

    }// Endof Image Sent/*
}
