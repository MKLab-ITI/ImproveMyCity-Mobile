// My_Crypt_Utils 
package com.mk4droid.IMC_Utils;

import java.security.NoSuchAlgorithmException;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import android.util.Log;

import com.mk4droid.IMC_Store.Constants_API;

/**
 * Cryptography utility. Encrypt a string (the Password typed by the user) using 
 *         algorithm AES/CBC/PKCS5Padding and the Constants.EncKey. 
 *         
 * If typed password is less than 16 chars then it is padded with ' '.
 * Output is a array of bytes.         
 * 
 * @copyright   Copyright (C) 2012 - 2013 Information Technology Institute ITI-CERTH. All rights reserved.
 * @license     GNU Affero General Public License version 3 or later; see LICENSE.txt
 * @author      Dimitrios Ververidis for the Multimedia Group (http://mklab.iti.gr). 
 */
public class My_Crypt_Utils {
        private IvParameterSpec ivspec;
        private SecretKeySpec keyspec;
        private Cipher cipher;
        private String SecretKey = Constants_API.EncKey;
        private String iv = SecretKey;
        
        String TAG_Class =  getClass().getName();
        
        /** Initialize cipher AES CBC using Constants.EncKey */
        public My_Crypt_Utils(){
        	ivspec  = new IvParameterSpec(iv.getBytes());
        	keyspec = new SecretKeySpec(SecretKey.getBytes(), "AES");
        	
        	try {
        		cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        	} catch (NoSuchAlgorithmException e) {
        		Log.e(Constants_API.TAG, TAG_Class + " " + e.getMessage() );
        	} catch (NoSuchPaddingException e) {
        		Log.e(Constants_API.TAG, TAG_Class + " " + e.getMessage() );
        	}
        }
        
        //================= encrypt ================
        /**
         * Encrypt a string using Constants.EncKey
         * 
         * @param text
         * @return
         * @throws Exception
         */
        public byte[] encrypt(String text) throws Exception {
        	if(text == null || text.length() == 0)
        		throw new Exception("Empty string");

        	byte[] encrypted = null;

        	try {
        		cipher.init(Cipher.ENCRYPT_MODE, keyspec, ivspec);
        		
        		byte[] gb = padString(text).getBytes();
        		
        		encrypted = cipher.doFinal(gb);
        	} catch (Exception e){                       
        		throw new Exception("[encrypt] " + e.getMessage());
        	}
        	return encrypted;
        }

        //============ padString ============================
        /** Add empty char ' ' so that output has a length of 16 chars */
        private static String padString(String source){
        	
        	char paddingChar = ' ';
        	int size = 16;
        	int x = source.length() % size;
        	int padLength = size - x;

        	for (int i = 0; i < padLength; i++)
        		source += paddingChar;
        	
        	return source;
        }
}

//-------------------- FOR FUTURE USE -------------------------------------

///** Decrypt a string using Constants.EncKey
// * 
// * @param code
// * @return
// * @throws Exception
// */
//public byte[] decrypt(String code) throws Exception
//{
//	if(code == null || code.length() == 0)
//		throw new Exception("Empty string");
//
//	byte[] decrypted = null;
//
//	try {
//		cipher.init(Cipher.DECRYPT_MODE, keyspec, ivspec);
//
//		decrypted = cipher.doFinal(hexToBytes(code));
//	} catch (Exception e)
//	{
//		throw new Exception("[decrypt] " + e.getMessage());
//	}
//	return decrypted;
//}
//
//
//
//
//
//        
//public static byte[] hexToBytes(String str) {
//	if (str==null) 
//		return null;
//	else if (str.length() < 2) 
//		return null;
//	else {
//		int len = str.length() / 2;
//		byte[] buffer = new byte[len];
//		for (int i=0; i<len; i++) 
//			buffer[i] = (byte) Integer.parseInt(str.substring(i*2,i*2+2),16);
//
//		return buffer;
//	}
//}
