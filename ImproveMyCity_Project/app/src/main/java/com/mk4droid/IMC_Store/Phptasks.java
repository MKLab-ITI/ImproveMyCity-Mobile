/** Phptasks */
package com.mk4droid.IMC_Store;


/**
 * These constants are related to the php in joomla component. They are called 
 * from mobile and they return JSON formatted string. 
 * 
 * Do not modify.
 * 
 * @copyright   Copyright (C) 2012 - 2015 Information Technology Institute ITI-CERTH. All rights reserved.
 * @license     GNU Affero General Public License version 3 or later; see LICENSE.txt
 * @author      Dimitrios Ververidis for the Multimedia Group (http://mklab.iti.gr). 
 *
 */
public class Phptasks {
	
	//====================== PHP TASKs =======================
		/** Joomla php for getting version of issues */
		public static String TASK_GET_VERSION      = "mobile.getTimestamp";
		
		/** Joomla php for getting version of categories */
		public static String TASK_GET_CATEGVERSION = "mobile.getCategoryTimestamp";
		
		/** Joomla php for getting categories */
		public static String TASK_GET_CATEG        = "mobile.getCategories";
		
		/** Joomla php for getting multiple issues in a geographical rectangle */
		public static String TASK_GET_ISSUES       = "mobile.getIssues";
		
		/** Joomla php for getting detailed information of a single issue */
		public static String TASK_GET_ISSUE        = "mobile.getIssue";
		
		/** Joomla php for authenticating user */
		public static String TASK_AUTH_USER        = "mobile.getUserInfo";
		
		/** Joomla php for voting for an issue */
		public static String TASK_VOTE             = "mobile.voteIssue";
		
		/** Joomla php for commenting an issue */
		public static String TASK_COMMENT          = "?option=com_improvemycity&task=mobile.addComment&format=json";
		
		/** Joomla php for getting votes of the authenticated user */
		public static String TASK_GET_USER_VOTES   = "mobile.getUserVotes";
		
		/** Joomla php for submitting an issue */
		public static String TASK_ADD_ISSUE        = "?option=com_improvemycity&task=mobile.addIssue&format=json";
		
		/** Joomla php for registering a user */
		public static String TASK_REGISTER_USER    = "?option=com_improvemycity&task=mobile.registerUser&format=json";
		
		/** Joomla php for reseting the password of a user if he/she has forgot it */
		public static String TASK_RESET_PASS       = "/reset";                                    
}
