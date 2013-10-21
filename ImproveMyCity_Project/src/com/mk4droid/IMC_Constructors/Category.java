/** Category */
package com.mk4droid.IMC_Constructors;


/**
 * Construct a Category. Issues are categorized according to the id_cat they include.
 * 
 * 
* @copyright   Copyright (C) 2012 - 2013 Information Technology Institute ITI-CERTH. All rights reserved.
* @license     GNU Affero General Public License version 3 or later; see LICENSE.txt
* @author      Dimitrios Ververidis for the Multimedia Group (http://mklab.iti.gr). 
 *
 */
public class Category {

	/** Name of the category */
	public String _name;
	
	/** Unique database identifier */
	public int _id;
	
	/** Image of the category */
	public byte[] _icon;
	
	/** Categories are structured into parents and children so to have a structure, where 1 is parent; 2 is child */
	public int _level;
	
	/** If this instance of category is a child then this has its parent id else null */
	public int _parentid;
	
	/** Filtering preference of the user */
	public int _visible;
	
	public Category(){}
	
	public Category(int id, String name, byte[] icon, int level, int parentid, int visible){
	
	   this._id          = id;
	   this._name        = name;
	   this._icon        = icon;
	   this._level       = level;
	   this._parentid    = parentid;
	   this._visible    = visible;
   }
}