package com.example.paintapplication;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.GestureOverlayView.OnGesturePerformedListener;
import android.gesture.Prediction;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

/**
 * This is the activity class that serves as the main entry point to the 
 * app's user interface. This class contains the onCreate() method along
 * with some other methods. The onCreate() method is called by the system 
 * when the application is launched from the device home screen. 
 * This class is defined to be used as the main activity in the Android 
 * manifest file, AndroidManifest.xml, which is at the root of 
 * the project directory. In the manifest, this class is declared with an
 * <intent-filter> that includes the MAIN action and LAUNCHER category.
 * If either the MAIN action or LAUNCHER category are not declared for one 
 * of the activities, then the application icon will not appear in the Home 
 * screen's list of app's.
 * @author PrateekMehrotra
 *
 */
@SuppressLint("NewApi")
public class MainActivity extends Activity implements OnClickListener,ColorPickerDialog.OnColorChangedListener,OnGesturePerformedListener {

	private CustomViewForDrawing drawView;
	private ImageButton currPaint;
	private ImageView drawBtn;
	private float smallBrush, mediumBrush, largeBrush;
	private ImageView eraseBtn, newBtn, saveBtn, colorPickerBtn, colorFillBtn, gestureBtn;
	private ImageView btn_undo, btn_redo;
	GestureOverlayView gestures;
	private static int RESULT_LOAD_IMAGE = 1;
	ImageView imageView;
	private static final String COLOR_PREFERENCE_KEY = "color";
	GestureLibrary mLibrary;
	//boolean gestureDetected = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		System.out.println("oncreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		drawView = (CustomViewForDrawing) findViewById(R.id.drawing);
		LinearLayout paintLayout = (LinearLayout) findViewById(R.id.paint_colors);

		currPaint = (ImageButton)paintLayout.getChildAt(0);
		currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));

		//Brush initialization
		smallBrush = getResources().getInteger(R.integer.small_size);
		mediumBrush = getResources().getInteger(R.integer.medium_size);
		largeBrush = getResources().getInteger(R.integer.large_size);

		drawBtn = (ImageView) findViewById(R.id.draw_btn);
		drawBtn.setOnClickListener(this);

		drawView.setSizeForBrush(mediumBrush);

		eraseBtn = (ImageView)findViewById(R.id.erase_btn);
		eraseBtn.setOnClickListener(this);

		saveBtn = (ImageView)findViewById(R.id.save_btn);
		saveBtn.setOnClickListener(this);

		newBtn = (ImageView)findViewById(R.id.new_btn);
		newBtn.setOnClickListener(this);

		btn_undo = (ImageView) findViewById(R.id.undo);
		btn_undo.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Log.i("UndoTag","Inside on click of undo");
				drawView.onClickUndo();
			}
		});

		btn_redo=(ImageView) findViewById(R.id.redo);
		btn_redo.setOnClickListener(new OnClickListener() {

			public void onClick(View v) { 
				Log.i("RedoTag","Inside on click of redo");
				drawView.onClickRedo();
			}
		});


		ActionBar actionBar = getActionBar();
		actionBar.show();

		colorPickerBtn = (ImageView) findViewById(R.id.color_picker);
		colorPickerBtn.setOnClickListener(this);

		mLibrary = GestureLibraries.fromRawResource(this, R.raw.gestures); if(!mLibrary.load()){ finish(); }
		gestures = (GestureOverlayView) findViewById(R.id.gestures); 
		gestures.addOnGesturePerformedListener(this);

		colorFillBtn = (ImageView) findViewById(R.id.color_fill);
		colorFillBtn.setOnClickListener(this);
		//Gesture Overlay View Change 03/10/2013
		gestureBtn = (ImageView) findViewById(R.id.gesture);
		gestureBtn.setOnClickListener(this);
	}

	/**
	 * This method is called when a gesture is input from the gesture
	 * view. This method defines the logic for gesture matching using
	 * prediction scores returned by the Gesture Builder API.
	 */
	@Override
	public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture)
	{
		ArrayList<Prediction> predictions = mLibrary.recognize(gesture);
		btn_undo.setVisibility(View.GONE);
		btn_redo.setVisibility(View.GONE);
		if(predictions.size() > 0 && predictions.get(0).score > 1.0)
		{ 
			String result = predictions.get(0).name;
			
			if("new".equalsIgnoreCase(result))
			{   //Gesture Overlay View Change 03/10/2013
				//gestures.setVisibility(View.GONE);
				//drawView.setVisibility(View.VISIBLE);
				gestures.setVisibility(View.GONE);
				drawView.setBackgroundColor(Color.WHITE);
				drawView.setVisibility(View.VISIBLE);
				btn_undo.setVisibility(View.VISIBLE);
				btn_redo.setVisibility(View.VISIBLE);
				makeToast("Gesture Detected and Views Switched");
				//drawView.newStart();
				newDrawing();
				Toast.makeText(this, "new document",Toast.LENGTH_SHORT).show(); 
				//gestureDetected = true;
			} 
			else if("smooth".equalsIgnoreCase(result)) 
			{
				gestures.setVisibility(View.GONE);
				drawView.setBackgroundColor(Color.WHITE);
				btn_undo.setVisibility(View.VISIBLE);
				btn_redo.setVisibility(View.VISIBLE);
				
				drawView.setVisibility(View.VISIBLE);
				drawView.smoothStrokes=true;
				makeToast("Smoothening enabled!");
				//gestureDetected = true;
			}
			else if("curve".equalsIgnoreCase(result)) 
			{
				gestures.setVisibility(View.GONE);
				drawView.setBackgroundColor(Color.WHITE);
				drawView.setVisibility(View.VISIBLE);
				drawView.smoothStrokes=false;
				btn_undo.setVisibility(View.VISIBLE);
				btn_redo.setVisibility(View.VISIBLE);
				makeToast("Curve enabled, smoothening disabled!");
				
				//gestureDetected = true;
			}
			else if("save".equalsIgnoreCase(result))
			{ 	//Gesture Overlay View Change 03/10/2013
				gestures.setVisibility(View.GONE);
				drawView.setBackgroundColor(Color.WHITE);
				//drawView.getCanvas().drawColor(0, PorterDuff.Mode.CLEAR);
				drawView.setVisibility(View.VISIBLE);
				btn_undo.setVisibility(View.VISIBLE);
				btn_redo.setVisibility(View.VISIBLE);
				
				makeToast("Gesture Detected and Views Switched");
				saveDrawing(); 
				Toast.makeText(this, "Saving the document", Toast.LENGTH_SHORT).show();
				//gestureDetected = true;
			} 
			//Gesture Overlay View Change 03/10/2013
			else if("erase".equalsIgnoreCase(result)){
				gestures.setVisibility(View.GONE);
				drawView.setBackgroundColor(Color.WHITE);
				drawView.setVisibility(View.VISIBLE);
				btn_undo.setVisibility(View.VISIBLE);
				btn_redo.setVisibility(View.VISIBLE);
				
				makeToast("Gesture Detected and Views Switched");
				eraseDraw();
				makeToast("Select Eraser Size!");
				//gestureDetected = true;
			}
			else if("open".equalsIgnoreCase(result)){
				makeToast("Gesture Detected and Views Switched");
				//onActivityResult(1, resultCode, data);
				makeToast("Refreshing...");
				Intent i = new Intent(
						Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				btn_undo.setVisibility(View.VISIBLE);
				btn_redo.setVisibility(View.VISIBLE);
				
				startActivityForResult(i, RESULT_LOAD_IMAGE);

			}
			else if("undo".equalsIgnoreCase(result)){
				gestures.setVisibility(View.GONE);
				drawView.setBackgroundColor(Color.WHITE);
				drawView.setVisibility(View.VISIBLE);
				btn_undo.setVisibility(View.VISIBLE);
				btn_redo.setVisibility(View.VISIBLE);
				
				makeToast("Gesture Detected and Views Switched");
				drawView.onClickUndo();
			}
			else if("redo".equalsIgnoreCase(result)){
				gestures.setVisibility(View.GONE);
				drawView.setBackgroundColor(Color.WHITE);
				drawView.setVisibility(View.VISIBLE);
				btn_undo.setVisibility(View.VISIBLE);
				btn_redo.setVisibility(View.VISIBLE);
				
				makeToast("Gesture Detected and Views Switched");
				drawView.onClickRedo();
			}
			
			else if("colorpicker".equalsIgnoreCase(result)){
				gestures.setVisibility(View.GONE);
				drawView.setBackgroundColor(Color.WHITE);
				drawView.setVisibility(View.VISIBLE);
				
				int color = PreferenceManager.getDefaultSharedPreferences(
						MainActivity.this).getInt(COLOR_PREFERENCE_KEY,
								Color.WHITE);
				new ColorPickerDialog(MainActivity.this, MainActivity.this,
						color).show();
				makeToast("Choose color, center tap to select");

			}
			System.out.println(predictions.size() + " " + predictions.get(0).score);
		}
	}

	/**
	 * This method is called first when the eraser functionality is activated 
	 * by the user. This method is responsible for displaying a dialog box
	 * showing various eraser sizes for the user to choose from. This method 
	 * also defines the OnClickListener functions for each of the eraser sizes.
	 */
	public void eraseDraw(){
		System.out.println("when erase is clicked");
		final Dialog brushDialog = new Dialog(this);
		brushDialog.setTitle("Eraser size:");
		brushDialog.setContentView(R.layout.select_brush);
		//size buttons
		ImageButton smallBtn = (ImageButton)brushDialog.findViewById(R.id.small_brush);
		smallBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				drawView.setErase(true);
				drawView.setSizeForBrush(smallBrush);
				brushDialog.dismiss();
			}
		});
		ImageButton mediumBtn = (ImageButton)brushDialog.findViewById(R.id.medium_brush);
		mediumBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				drawView.setErase(true);
				drawView.setSizeForBrush(mediumBrush);
				brushDialog.dismiss();
			}
		});
		ImageButton largeBtn = (ImageButton)brushDialog.findViewById(R.id.large_brush);
		largeBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				drawView.setErase(true);
				drawView.setSizeForBrush(largeBrush);
				brushDialog.dismiss();
			}
		});
		brushDialog.show();

	}
	//Gesture Overlay View Change 03/10/2013
	//Overloaded for saving images when edited through gesture overlay
	/**
	 * This method is called when the user enables the save functionality.
	 * This method pops a dialog box asking a confirmation from the user of
	 * the desired functionality. If the user selects "yes" then saveDrawing()
	 * method is called by the "yes" button event listener to save the current 
	 * image into device gallery.
	 * @param bitmap Bitmap of current image
	 */
	public void saveDrawing(final Bitmap bitmap)
	{
		AlertDialog.Builder saveDialog = new AlertDialog.Builder(this);
		saveDialog.setTitle("Save drawing");
		saveDialog.setMessage("Save drawing to device Gallery?");
		saveDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which){
				String url = Images.Media.insertImage(getContentResolver(), bitmap, "title", ".png");
			}
		});
		saveDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which){
				dialog.cancel();
			}
		});
		saveDialog.show();
	}
	
	/**
	 * This method is called when the new drawing functionality is 
	 * enabled by the user. This function pops a dialog box to get 
	 * a confirmation on the desired functionality. If "yes" is 
	 * selected from the dialog box, the canvas is cleared, the
	 * previous drawing is removed, and the canvas object is
	 * re-initialized to its original state.
	 */
	public void newDrawing()
	{
		//new button
		AlertDialog.Builder newDialog = new AlertDialog.Builder(this);
		newDialog.setTitle("New drawing");
		newDialog.setMessage("Start new drawing (you will lose the current drawing)?");
		newDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which){
				drawView.setBackgroundColor(Color.WHITE);
				drawView.newStart();
				drawView.setSizeForBrush(mediumBrush);
				drawView.smoothStrokes=false;
				dialog.dismiss();
			}
		});
		newDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which){
				dialog.cancel();
			}
		});
		newDialog.show();
	}
	
	/**
	 * This method is called when the user selects "yes" from the save
	 * dialog box. This method saves the current image on which the user 
	 * is working to the device gallery.
	 */
	public void saveDrawing()
	{

		boolean mExternalStorageAvailable = false;
		boolean mExternalStorageWriteable = false;
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
			// We can read and write the media
			mExternalStorageAvailable = mExternalStorageWriteable = true;
			System.out.println("external available");
			System.out.println(getExternalFilesDir(null));
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			// We can only read the media
			mExternalStorageAvailable = true;
			mExternalStorageWriteable = false;
			System.out.println("external available but readonly");
		} else {
			// Something else is wrong. It may be one of many other states, but all we need
			//  to know is we can neither read nor write
			mExternalStorageAvailable = mExternalStorageWriteable = false;
		}

		AlertDialog.Builder saveDialog = new AlertDialog.Builder(this);
		saveDialog.setTitle("Save drawing");
		saveDialog.setMessage("Save drawing to device Gallery?");
		saveDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which){
				//save drawing
				Bitmap bitmap;
				//View v1 = findViewById(R.id.drawing);
				View v1 = drawView;
				v1.setDrawingCacheEnabled(true);
				bitmap = Bitmap.createBitmap(v1.getDrawingCache());
				String url = Images.Media.insertImage(getContentResolver(), bitmap, "title", ".png");
				v1.setDrawingCacheEnabled(false);
				/*v1.setDrawingCacheEnabled(false);
		    	ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		    	bitmap.compress(Bitmap.CompressFormat.JPEG, 40, bytes);
		    	String fileName = "test.jpg";
		    	File f = new File(Environment.getExternalStorageDirectory()
		    			+ File.separator + Environment.DIRECTORY_PICTURES + File.separator + fileName);*/

				/*f.createNewFile();
		    	FileOutputStream fo = null;
		    	fo = new FileOutputStream(f);
		    	fo.write(bytes.toByteArray());
		    	fo.close();*/
				//String url = Images.Media.insertImage(getContentResolver(), bitmap, "title", null);
			}
		});

		saveDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which){
				dialog.cancel();
			}
		});
		saveDialog.show();
	}
	
	/**
	 * This method is called when the user desires to change the color
	 * to paint with. This method then sets the color object of the
	 * view to the newly selected color.
	 * @param color Desired Color 
	 */
	@Override
	public void colorChanged(int color) {
		PreferenceManager.getDefaultSharedPreferences(this).edit().putInt(
				COLOR_PREFERENCE_KEY, color).commit();
		Integer i = new Integer(color);
		System.out.println("~~~~~~~~" + i.toHexString(color));
		System.out.println(Color.parseColor("#" + i.toHexString(color)));
		drawView.setColor("#" + i.toHexString(color));
	}
	
	/**
	 * This method is called to initialize the options menu in
	 * the action bar.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		System.out.println("oncreateoptionsmenu");
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	/**
	 * This method is called when an item is selected from the options
	 * menu in the actions bar.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		// same as using a normal menu
		switch(item.getItemId()) 
		{
		case R.id.item_open:
			makeToast("Refreshing...");
			Intent i = new Intent(
					Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

			startActivityForResult(i, RESULT_LOAD_IMAGE);
			break;
			//case R.id.item_save:
			//makeToast("Saving...");
			//break;
		}
		return true;
	}
	
	/**
	 * This method is called when the user desires to load an existing 
	 * image from device gallery into the application.
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode!=0)
		{
			//System.out.println("$$$$$$$$$$$$$$$$$$$$$$$"  + " " + new Integer(resultCode).toString() + " " + "$$$$$$$$$$$$$$$$$$");
			String picturePath=null;

			if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
				Uri selectedImage = data.getData();
				String[] filePathColumn = { MediaStore.Images.Media.DATA };

				Cursor cursor = getContentResolver().query(selectedImage,
						filePathColumn, null, null, null);
				cursor.moveToFirst();

				int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
				picturePath = cursor.getString(columnIndex);
				cursor.close();
			}

			//System.out.println("<<<<<<<" + " " + picturePath + " " + "<<<<");
			// String picturePath contains the path of selected Image
			//imageView = (ImageView) findViewById(R.id.new_view);

			drawView.setUpDrawing();
			drawView.setSizeForBrush(mediumBrush);
			Bitmap bitmap = BitmapFactory.decodeFile(picturePath);
			Drawable d = new BitmapDrawable(getResources(),bitmap);
			//drawView.newStart();
			drawView.getCanvas().drawColor(0, PorterDuff.Mode.CLEAR);
			drawView.setBackground(d);
			//drawView.invalidate();


			//drawView = imageView;
			//drawView.setVisibility(View.GONE);
			//imageView.setVisibility(View.VISIBLE);


			//canvasBitmap = Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);
			//drawCanvas = new Canvas(canvasBitmap);
			//Canvas canvas = new Canvas(bitmap.copy(Bitmap.Config.ARGB_8888, true));
			//drawView.draw(canvas);
			//System.out.println("!!!!! " + canvas.toString() + " !!!");
			//imageView.setImageBitmap(bitmap);
		}
		else
		{
			drawView.setVisibility(View.VISIBLE);
		}
	}
	
	/**
	 * This method is used to return the current status of the drawView
	 * @return drawView the current status of the drawView
	 */
	public CustomViewForDrawing getDrawView()
	{
		return drawView;
	}
	
	/**
	 * This method is called to display a toast message on device
	 * screen.
	 * @param message
	 */
	public void makeToast(String message) {
		// with jam obviously
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	}
	
	/**
	 * This function is called when the user desires to change
	 * the brush thickness that is being used to draw on  the 
	 * canvas. This function pops a dialog box asking the user
	 * to select a particular brush thickness. When the user 
	 * makes a selection, the event listener for each brush
	 * thickness is defined in this method which sets the 
	 * brush thickness object with the newly chosen brush 
	 * thickness.
	 * @param view
	 */
	//user clicks on paint
	public void paintClicked(View view){
		System.out.println("paintclicked");
		drawView.setErase(false);
		drawView.setSizeForBrush(drawView.getPrevBrushSize());

		if(view!=currPaint){
			ImageButton imgView = (ImageButton) view;
			String color = view.getTag().toString();

			drawView.setColor(color);

			imgView.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));
			currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint));
			currPaint = (ImageButton)view;
		}
	}
	
	/**
	 * This function defines all the event listeners for the 
	 * icons in the main layout of the application.
	 */
	@Override
	public void onClick(View v) 
	{
		//drawView.setVisibility(View.VISIBLE);
		//imageView.setVisibility(View.GONE);
		System.out.println("onclick");
		// TODO Auto-generated method stub
		if(v.getId()==R.id.draw_btn)
		{
			//draw button clicked
			final Dialog brushDialog = new Dialog(this);
			brushDialog.setTitle("Brush size:");
			brushDialog.setContentView(R.layout.select_brush);

			//Different buttons for varying size brushes; implement gestures in it
			ImageButton smallBtn = (ImageButton)brushDialog.findViewById(R.id.small_brush);
			smallBtn.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					//drawView.setErase(false);
					drawView.setSizeForBrush(smallBrush);
					drawView.setPrevBrushSize(smallBrush);
					brushDialog.dismiss();
				}
			});
			ImageButton mediumBtn = (ImageButton)brushDialog.findViewById(R.id.medium_brush);
			mediumBtn.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					//drawView.setErase(false);
					drawView.setSizeForBrush(mediumBrush);
					drawView.setPrevBrushSize(mediumBrush);
					brushDialog.dismiss();
				}
			});
			ImageButton largeBtn = (ImageButton)brushDialog.findViewById(R.id.large_brush);
			largeBtn.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					drawView.setErase(false);
					drawView.setSizeForBrush(largeBrush);
					drawView.setPrevBrushSize(largeBrush);
					brushDialog.dismiss();
				}
			});
			//show and wait for user interaction
			brushDialog.show();

		}
		else if(v.getId()==R.id.erase_btn){
			//switch to erase - choose size
			System.out.println("when erase is clicked");
			final Dialog brushDialog = new Dialog(this);
			brushDialog.setTitle("Eraser size:");
			brushDialog.setContentView(R.layout.select_brush);
			//size buttons
			ImageButton smallBtn = (ImageButton)brushDialog.findViewById(R.id.small_brush);
			smallBtn.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					drawView.setErase(true);
					drawView.setSizeForBrush(smallBrush);
					brushDialog.dismiss();
				}
			});
			ImageButton mediumBtn = (ImageButton)brushDialog.findViewById(R.id.medium_brush);
			mediumBtn.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					drawView.setErase(true);
					drawView.setSizeForBrush(mediumBrush);
					brushDialog.dismiss();
				}
			});
			ImageButton largeBtn = (ImageButton)brushDialog.findViewById(R.id.large_brush);
			largeBtn.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					drawView.setErase(true);
					drawView.setSizeForBrush(largeBrush);
					brushDialog.dismiss();
				}
			});
			brushDialog.show();
		}
		else if(v.getId()==R.id.save_btn)
		{
			saveDrawing();
		}
		else if(v.getId()==R.id.new_btn)
		{
			newDrawing();
		}
		else if(v.getId()==R.id.color_picker)
		{
			int color = PreferenceManager.getDefaultSharedPreferences(
					MainActivity.this).getInt(COLOR_PREFERENCE_KEY,
							Color.WHITE);
			new ColorPickerDialog(MainActivity.this, MainActivity.this,
					color).show();
		}
		//Gesture Overlay View Change 03/10/2013
		else if(v.getId()==R.id.color_fill)
		{
			//makeToast(new Integer(drawView.getColor()).toHexString(drawView.getColor()));
			//drawView.getCanvas().drawColor(0, PorterDuff.Mode.CLEAR);
			//drawView.getColor();
			//Drawable d = new ColorDrawable(drawView.getColor());
			//drawView.setBackground(d);
			/*int color = drawView.getColor();
			drawView.getCanvas().drawColor(color);
			drawView.setVisibility(View.VISIBLE);
			System.out.println("Hello Hello");*/
			boolean smoothStatus = drawView.smoothStrokes;
			drawView.setUpDrawing();
			drawView.smoothStrokes = smoothStatus;
			drawView.setSizeForBrush(mediumBrush);
			drawView.getCanvas().drawColor(0, PorterDuff.Mode.CLEAR);
			drawView.getCanvas().drawColor(0, PorterDuff.Mode.SRC);
			int color = drawView.getColor();
			drawView.getCanvas().drawColor(color);
			drawView.invalidate();
			//paintClicked(drawView);

		}
		//Gesture Overlay View Change 03/10/2013
		else if(v.getId()==R.id.gesture)
		{
			//drawView.getColor();
			//makeToast(new Integer(drawView.getColor()).toHexString(drawView.getColor()));
			makeToast("In gesture");
			Bitmap returnedBitmap = Bitmap.createBitmap(drawView.getWidth(), drawView.getHeight(),Bitmap.Config.ARGB_8888);
			Drawable d = new BitmapDrawable(getResources(),returnedBitmap);
			System.out.println(returnedBitmap);
			drawView.setBackground(d);
			drawView.setVisibility(View.GONE);
			gestures.setVisibility(View.VISIBLE);

			//drawView.getCanvas().drawColor(0, PorterDuff.Mode.CLEAR);
			//drawView.setBackgroundColor(drawView.getColor());
		}
	}
}