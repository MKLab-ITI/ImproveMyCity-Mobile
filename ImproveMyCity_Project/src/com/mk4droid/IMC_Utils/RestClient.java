/*
 *  How to use me
 *  
 *  
 
 RestClient client = new RestClient(LOGIN_URL);
client.AddParam("accountType", "GOOGLE");
client.AddParam("source", "tboda-widgalytics-0.1");
client.AddParam("Email", _username);
client.AddParam("Passwd", _password);
client.AddParam("service", "analytics");
client.AddHeader("GData-Version", "2");

try {
    client.Execute(RequestMethod.POST);
} catch (Exception e) {
    e.printStackTrace();
}

String response = client.getResponse();
*/

package com.mk4droid.IMC_Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import android.util.Log;

/**
 *  Http Post or Get implementations 
 *
 */
public class RestClient {

    private ArrayList <NameValuePair> params;
    private ArrayList <NameValuePair> headers;
    private String url;
    private String encoding;
    private int responseCode;
    private String message;
    private String response;

    public enum RequestMethod {    	GET, POST;     }
    public String getResponse() {        return response;     }
    public String getErrorMessage() {    return message;      }
    public int getResponseCode() {       return responseCode;  }

    public RestClient(String url, String Encoding) //, File mfile
    {
        this.url = url;
        this.encoding = Encoding;
        params = new ArrayList<NameValuePair>();
        headers = new ArrayList<NameValuePair>();
    }

    public void AddParam(String name, String value)
    {
        params.add(new BasicNameValuePair(name, value));
    }

    public void AddHeader(String name, String value)
    {
        headers.add(new BasicNameValuePair(name, value));
    }

    public String Execute(RequestMethod method)
    {
        switch(method) {
            case GET:
            {
                //add parameters
                String combinedParams = "";
                if(!params.isEmpty()){
                    combinedParams += "?";
                    for(NameValuePair p : params)
                    {
                        String paramString = "";
						try {
							paramString = p.getName() + "=" +  URLEncoder.encode(p.getValue(),encoding);
							
							if(combinedParams.length() > 1)
	                            combinedParams  +=  "&" + paramString;
	                        else
	                            combinedParams += paramString;
							
						} catch (UnsupportedEncodingException e) {
                            Log.e("RestClient", "Can not encode parameters'values");
						}
		             
                    }
                }

                String urlcombinedParams = url + combinedParams;
                HttpGet request = new HttpGet(urlcombinedParams);
                HttpParams httpParams = new BasicHttpParams();
                httpParams.setParameter("http.protocol.content-charset", "UTF-8");
                request.setParams(httpParams);
                
                //add headers
                for(NameValuePair h : headers)   {
                    request.addHeader(h.getName(), h.getValue());
                }

                executeRequest(request);
                break;
            }
            case POST:
            {
                HttpPost request = new HttpPost(url);

                //add headers
                for(NameValuePair h : headers)
                {
                    request.addHeader(h.getName(), h.getValue());
                }

                if(!params.isEmpty()){
                    try {
						request.setEntity(new UrlEncodedFormEntity(params, encoding));
					} catch (UnsupportedEncodingException e) {
					}
                }
                
                executeRequest(request);
                break;
            }
        }
        
        
        return response;
    }

    private void executeRequest(HttpUriRequest request)  {
        
    	
    	HttpParams params = new BasicHttpParams();
    	params.setParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET, HTTP.UTF_8);
    	HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
    	HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
     	DefaultHttpClient client = new  DefaultHttpClient(params);
        HttpResponse httpResponse;

        try {
            httpResponse = client.execute(request);
            responseCode = httpResponse.getStatusLine().getStatusCode();
            message = httpResponse.getStatusLine().getReasonPhrase();

            HttpEntity entity = httpResponse.getEntity();

            if (entity != null) {
                InputStream instream = entity.getContent();
                response = convertStreamToString(instream, encoding);
                // Closing the input stream will trigger connection release
                instream.close();
            }

        } catch (ClientProtocolException e)  {
            client.getConnectionManager().shutdown();

        } catch (IOException e) {
            client.getConnectionManager().shutdown();

        }
    }

    private static String convertStreamToString(InputStream is, String encod) {

        BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(is,encod));
		} catch (UnsupportedEncodingException e1) {

		}
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {  sb.append(line);   }
        } catch (IOException e) {

        } finally {
            try {
                is.close();
            } catch (IOException e) {

            }
        }
        return sb.toString();
    }
}
