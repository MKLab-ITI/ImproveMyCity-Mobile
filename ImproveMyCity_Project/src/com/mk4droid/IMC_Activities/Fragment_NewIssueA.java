/**
 * @copyright   Copyright (C) 2012 - 2013 Information Technology Institute ITI-CERTH. All rights reserved.
 * @license     GNU Affero General Public License version 3 or later; see LICENSE.txt
 * @author      Dimitrios Ververidis for the Multimedia Group (http://mklab.iti.gr). 
 */
package com.mk4droid.IMC_Activities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.mk4droid.IMC_Constructors.Category;
import com.mk4droid.IMC_Core.SpinnerAdapter_NewIssueCateg;
import com.mk4droid.IMC_Services.Service_Data;
import com.mk4droid.IMC_Store.Constants_API;
import com.mk4droid.IMC_Utils.My_System_Utils;
import com.mk4droid.IMCity_PackDemo.R;

/**
 * A form for writing issue title, description, category, attaching photo, and proceed to localization of the issue (Fragment_NewIssueB)
 * 
 * @copyright   Copyright (C) 2012 - 2013 Information Technology Institute ITI-CERTH. All rights reserved.
 * @license     GNU Affero General Public License version 3 or later; see LICENSE.txt
 * @author      Dimitrios Ververidis for the Multimedia Group (http://mklab.iti.gr). 
 * 
 */
public class Fragment_NewIssueA extends Fragment {

	/** path of temporary image File of issue */
	public static String image_path_source_temp =               
			Environment.getExternalStorageDirectory().getAbsolutePath() + "/"+ Environment.DIRECTORY_DCIM + "/tmp_image.jpg";
	
	/** Attach image button */
	public static ImageButton btAttachImage;
	
	/** LinearLayout of fragment new issue (the first part) */
	public static LinearLayout llnewissue_a;
		
	/** Flag to indicate that new issue guis are reset  */
	public static boolean flagStarter = true;
	
	//========= Private vars =============
	// System vars 
	SharedPreferences mshPrefs;
	static Resources resources;
	DisplayMetrics metrics;
	static Context ctx;
	int tlv = Toast.LENGTH_LONG;
	
	// fragment vars 
	static Fragment_NewIssueA mfrag_nIssueA;
	View vfrag_nIssueA;
	
	static Fragment_NewIssueB mfrag_nIssueB;
	
	//  Task vars 
	Button btProceed;
	static EditText et_title,et_descr;

	static LinearLayout llUnauth;  // unauthorized message 
	
	private Bitmap Image_BMP;  // New issue Bitmap
	
	static int CAMERA_PIC_REQUEST = 1337;
	static int SELECT_PICTURE     = 1;
	private static File fimg;
	static boolean flagPictureTaken = false;

	boolean AuthFlag;

	//---------- Task Variables --------------
	static String titleData_STR = "";
	static String descriptionData_STR = "";
	
	static int[] SpinnerArrID;  // This contains category ids as in MySQL
	public static int spPosition = -1;
	static Spinner sp; // spinner of categories
	SpinnerAdapter_NewIssueCateg adapterSP; // spinner adapter
	
	String[] SpinnerArrString;		 

 	/* 
 	 *  onCreate fragment
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		mshPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		resources = SetResources();
		
		super.onCreate(savedInstanceState);
	}

	
	/**
	 *          OnCreateView 
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		flagStarter = true;
		
		FActivity_TabHost.IndexGroup = 2;
		vfrag_nIssueA = inflater.inflate(R.layout.fragment_newissue_a, container, false);
		
		mfrag_nIssueA  = this;
		
		ctx = this.getActivity();
		
		//-------- tvUnauth ---- 
		llUnauth = (LinearLayout)vfrag_nIssueA.findViewById(R.id.llUnauth);
		
		Button gotoSetup = (Button) llUnauth.findViewById(R.id.bt_nia_gosetup);
		gotoSetup.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				getActivity().finish();
				
				// Check if Activity Splash Already open
				if (Activity_Splash_Login.et_username == null){
					Log.e("NIA", "SPLASH WAS NULL");
					startActivity(new Intent(ctx, Activity_Splash_Login.class));
				}
				
				getActivity().finish();
			}
		});
		
		
		//---- Spinner -----
		ArrayList<Category> mCatL_Sorted = SortCategList(Service_Data.mCategL);
		SpinnerArrString = initSpinner(mCatL_Sorted);

		sp = (Spinner)vfrag_nIssueA.findViewById(R.id.spinnerCateg);
		
		adapterSP = new SpinnerAdapter_NewIssueCateg(getActivity(),    //--- Set spinner adapter --
												android.R.layout.simple_spinner_item, mCatL_Sorted);
		sp.setAdapter(adapterSP);
		
		
		
		sp.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View arg1,
					int arg2, long arg3) {
				 
				if (flagStarter){
					flagStarter = false;
				} else {
					spPosition = arg2;
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		
		//--------- Title -----

		et_title = (EditText)vfrag_nIssueA.findViewById(R.id.etTitle_ni);
		
		if (et_title!=null)
			if (et_title.getText().toString().length()>0)
				titleData_STR = et_title.getText().toString();


		if(titleData_STR.length()>0)
		et_title.setText(titleData_STR);

		//------ Description ----

		et_descr = (EditText)vfrag_nIssueA.findViewById(R.id.etDescription);
		if (et_descr!=null)
		if (et_descr.getText().toString().length()>0)
			descriptionData_STR = et_descr.getText().toString();

		
		//------- Bt Attach image ---
		btAttachImage = (ImageButton)vfrag_nIssueA.findViewById(R.id.btAttach_image);

		//-------- Bt Proceed -----
		btProceed = (Button)vfrag_nIssueA.findViewById(R.id.btProceed_ni_B);
	
		mshPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		resources = SetResources();
				
		//--------- Layout -------
		llnewissue_a = (LinearLayout)vfrag_nIssueA.findViewById(R.id.llnewissue_a);
		llnewissue_a.setVisibility(View.VISIBLE);
		
		//-------- Take Image button -------
		if (flagPictureTaken && Image_BMP !=null){

			btAttachImage.setScaleType(ScaleType.CENTER_CROP);

			try {
				btAttachImage.setImageBitmap(Image_BMP);
			} catch (Exception e){

				// if the btAttachImage was null set Image with some delay
				btAttachImage.postDelayed(new Runnable() {
					@Override
					public void run() {
						btAttachImage.setImageBitmap(Image_BMP);
					}
				}, 1000);
			};
		} else {
			btAttachImage.setScaleType(ScaleType.CENTER_INSIDE);
			btAttachImage.setImageResource(R.drawable.bt_custom_camera_round); //R.drawable.pattern_camera_repeater));
		}
		

		btAttachImage.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

				builder.setTitle(FActivity_TabHost.resources.getString(R.string.Attachanimage));
				builder.setIcon( android.R.drawable.ic_menu_gallery);

				// 1 select
				builder.setPositiveButton(FActivity_TabHost.resources.getString(R.string.Gallery),
						new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {

						dialog.dismiss();

						Intent intent = new Intent();
						intent.setType("image/jpeg");
						intent.setAction(Intent.ACTION_GET_CONTENT);
						startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
					}
				});

				// 2 Shoot
				builder.setNeutralButton(FActivity_TabHost.resources.getString(R.string.Camera),
						new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {

						dialog.dismiss();
						// 2 shoot 
						fimg = new File (image_path_source_temp);
						Uri uri = Uri.fromFile(fimg);

						Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
						cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);

						startActivityForResult(cameraIntent, CAMERA_PIC_REQUEST);  
					}
				});


				// 3 clear 
				builder.setNegativeButton(FActivity_TabHost.resources.getString(R.string.Clear),
						new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						//dialog.cancel();

						flagPictureTaken = false;
						File imagef = new File(image_path_source_temp);
						imagef.delete();

						dialog.dismiss();

						//btAttachImage.setCompoundDrawablesWithIntrinsicBounds( 0, R.drawable.bt_custom_camera_round, 0,  0);
						btAttachImage.setScaleType(ScaleType.CENTER_INSIDE);
						btAttachImage.setImageResource(R.drawable.bt_custom_camera_round);
						
					//	btAttachImage.setPadding(0, 40, 0, 0);
					}
				});

				builder.create();
				builder.show();
			}});

		//------------- button Proceed ----------
		btProceed.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View vBt) {
				
				// Check if title is long enough and sent
				if ( et_title.getText().toString().length() > 2 && spPosition != -1 && et_descr.getText().toString().length() > 2){                // RRR
                     
					titleData_STR = et_title.getText().toString();

					if ( et_descr.getText().toString().length() > 0)
						descriptionData_STR =  et_descr.getText().toString();

					// Close Keyboard
					InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Service.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(et_title.getWindowToken(), 0); 
					imm.hideSoftInputFromWindow(et_descr.getWindowToken(), 0);

					// Instantiate a new fragment.
					mfrag_nIssueB = new Fragment_NewIssueB();

					Bundle args = new Bundle();
					args.putInt("IndexSpinner", sp.getSelectedItemPosition());
					mfrag_nIssueB.setArguments(args);
					
					// Add the fragment to the activity, pushing this transaction
					// on to the back stack.
					FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
					ft.add(mfrag_nIssueA.getId(), mfrag_nIssueB, "FTAG_NEW_ISSUE_B");
					ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
					ft.addToBackStack(null);
					ft.commit();
				}else if (spPosition == -1){
					Toast.makeText(getActivity(), resources.getString(R.string.SelectaCategory), tlv).show();
				}else if (et_title.getText().toString().length() <= 2 ){
					Toast.makeText(getActivity(), resources.getString(R.string.LongerTitle) , tlv).show();
				}else if (et_descr.getText().toString().length() <= 2){
					Toast.makeText(getActivity(), resources.getString(R.string.LongerDescription), tlv).show();
				} 
			}
		});


		return vfrag_nIssueA;
	}// Endof Create

	//=============== onActivityResult ===============================
	/**
	 *    When returning from shooting an image from camera or selecting an image from Gallery. 
	 */
	public void onActivityResult(int requestCode, int resultCode, Intent intentdata) {  

		if ((requestCode == CAMERA_PIC_REQUEST || requestCode == SELECT_PICTURE) && resultCode == -1 ){   //  - 1 = RESULT_OK 

			flagPictureTaken = true;
			if ( requestCode == SELECT_PICTURE){
				Uri selectedImageUri = intentdata.getData();
				My_System_Utils.FCopy(image_path_source_temp, getPath(selectedImageUri));
			}

			CheckOrient();
			//btAttachImage.setCompoundDrawablesWithIntrinsicBounds(null, null, null,  null);
			
			btAttachImage.setImageBitmap(Image_BMP);
			btAttachImage.setScaleType(ScaleType.CENTER_CROP);
			
		}  else {
			flagPictureTaken = false;
			File imagef = new File(image_path_source_temp);
			imagef.delete();
		} 
	}  

	

	//============ getPath ==================================	
	/**
	 *  Get path where image was stored after shooting with camera
	 * 
	 * @param uri general uri for android metadata
	 * @return path of the image
	 */
	public String getPath(Uri uri) {
		String[] projection = { MediaStore.Images.Media.DATA };
		Cursor cursor = getActivity().getContentResolver().query(uri, projection, null, null, null);
		int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}
	
	//============ On Resume ============
	/** Resume from changing tabs */
	@Override
	public void onResume() {
		super.onResume();
		resources = SetResources();
		
		if (spPosition!=-1){
			sp.post(new Runnable() {        
			    public void run() {
			    	sp.setSelection(spPosition,true);
			    }
			  });
		}
		
		llnewissue_a.setVisibility(View.VISIBLE);
						
		//--------- Show Unauthorized message ------------
		for (int i=0; i< llnewissue_a.getChildCount(); i++)
			if (!AuthFlag)
				llnewissue_a.getChildAt(i).setVisibility(View.GONE);
			else 
				llnewissue_a.getChildAt(i).setVisibility(View.VISIBLE);

		if (!AuthFlag){
			llUnauth.setVisibility(View.VISIBLE);
		}else{
			llUnauth.setVisibility(View.GONE);
		}

		et_title.setHint(resources.getString(R.string.STitle));
		et_descr.setHint(resources.getString(R.string.Description));
	
		//----------- Flurry Analytics --------
		boolean AnalyticsSW                = mshPrefs.getBoolean("AnalyticsSW", true);
		if (AnalyticsSW)
			FlurryAgent.onStartSession(getActivity(), Constants_API.Flurry_Key);
	}  


	//============ onPause ============== 
	/** Pause when changing tab. Stop Flurry. */
	@Override
	public void onPause() {
		super.onPause();

		InputMethodManager imm = (InputMethodManager)ctx.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(et_title.getWindowToken(), 0);
		imm.hideSoftInputFromWindow(et_descr.getWindowToken(), 0);
		
		//-- Flurry Analytics --
		boolean AnalyticsSW = mshPrefs.getBoolean("AnalyticsSW", true);

		if (AnalyticsSW)
			FlurryAgent.onEndSession(getActivity());
	}  
	
	//============= Sort Category List ====================
	/** Sort categories in the list according to parent and child information */
	public ArrayList<Category> SortCategList(ArrayList<Category> InL){
		ArrayList<Category> OutL = new ArrayList<Category>(); 
		int i=0;
		while (i< InL.size()){
			if (InL.get(i)._level == 1){ // Parent
				OutL.add(InL.get(i));
				int parent_id = InL.get(i)._id; 
				int j = 0;
				while (j<InL.size()){
					if (InL.get(j)._parentid == parent_id){
						OutL.add(InL.get(j));
					}
					j=j+1;
				}
			}	  
			i=i+1;
		}
		return OutL;
	}

	//===============  initSpinner ================ 
	/** Initialize Spinner strings */
	public String[] initSpinner(ArrayList<Category> L){
		int NCategs = L.size();

		// --- Assign Strings and IDs for spinner use ----
		String[] Res     = new String[NCategs]; 
		SpinnerArrID     = new int[NCategs];  

		for (int i=0; i < NCategs; i++){
			Res[i]              = L.get(i)._name;   
			SpinnerArrID[i]     = L.get(i)._id;     
		}

		return Res;
	}

	//===========  Set Resources ==================
	/**
	 * Obtain resources from preferences 
	 * @return
	 */
	public Resources SetResources(){
		String LangSTR   = mshPrefs.getString("LanguageAR", Constants_API.DefaultLanguage);
		AuthFlag         = mshPrefs.getBoolean("AuthFlag", false);

		Configuration conf = getResources().getConfiguration();
		conf.locale = new Locale(LangSTR.substring(0, 2)); //----- Convert Greek -> el ---------
		metrics = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
		return new Resources(getActivity().getAssets(), metrics, conf);
	}
	
	
	//================ CheckOrient ============================= 	 
	/**
	 *        Check Image Orientation  
	 */
	public void CheckOrient(){  

		BitmapFactory.Options options=new BitmapFactory.Options(); // Resize is needed otherwize outofmemory exception
		options.inSampleSize = 6;

		//------------- read tmp file ------------------ 
		Image_BMP   = BitmapFactory.decodeFile(image_path_source_temp, options); // , options

		//---------------- find exif header --------
		ExifInterface exif;
		String exifOrientation = "0"; // 0 = exif not working
		try {
			exif = new ExifInterface(image_path_source_temp);
			exifOrientation = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
		} catch (IOException e1) {
			e1.printStackTrace();
		}    

		//---------------- Resize ---------------------
		if (exifOrientation.equals("0")){   
			if ( Image_BMP.getWidth() < Image_BMP.getHeight() && Image_BMP.getWidth() > 400 ){
				Image_BMP   = Bitmap.createScaledBitmap(Image_BMP, 400, 640, true); // <- To sent
			} else if ( Image_BMP.getWidth() > Image_BMP.getHeight() && Image_BMP.getWidth() > 640 ) { 
				Image_BMP = Bitmap.createScaledBitmap(Image_BMP, 640, 400, true); // <- To sent
			}	    	
		} else {

			if (exifOrientation.equals("1") && Image_BMP.getWidth() > 640 ){  // normal

				Image_BMP   = Bitmap.createScaledBitmap(Image_BMP, 640, 400, true); // <- To sent

			} else if (exifOrientation.equals("6") && Image_BMP.getWidth() > 400 ){  // rotated 90 degrees

				// Rotate
				Matrix matrix = new Matrix();

				int bmwidth  = Image_BMP.getWidth();
				int bmheight = Image_BMP.getHeight();

				matrix.postRotate(90);

				Image_BMP = Bitmap.createBitmap(Image_BMP, 0, 0, bmwidth,
						bmheight, matrix, true);

				Image_BMP = Bitmap.createScaledBitmap(Image_BMP, 400, 640, true); // <- To sent
			}
		}

		DisplayMetrics metrics = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

		//------------ now store as jpg over the temp jpg
		File imagef = new File(image_path_source_temp);
		try {
			Image_BMP.compress(Bitmap.CompressFormat.JPEG, 95, new FileOutputStream(imagef));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
    //===========================================
}