package com.example.paintapplication;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

/**
 * This class contains the attributes for the main layout of
 * our application.
 * @author PrateekMehrotra
 *
 */
@SuppressLint("NewApi")
public class CustomViewForDrawing extends View{

	private CustomPath drawPath;
	private Path lastPath;
	private Bitmap canvasBitmap;
	private Bitmap backup;

	private Paint drawPaint;
	private Paint canvasPaint;
	private int paintColor = 0xFF787878;
	private Canvas drawCanvas;
	private Canvas backUpCanvas;

	private float brushSize;
	private float prevBrushSize; // when switching, store previous

	private boolean erase = false;
	public boolean smoothStrokes = false;

	private float startX;
	private float startY;
	private float endX;
	private float endY;

	private static final String BRIGHTNESS_PREFERENCE_KEY = "brightness";
	private static final String COLOR_PREFERENCE_KEY = "color";
	TextView tv;
	private ArrayList<CustomPath> paths = new ArrayList<CustomPath>();
	private ArrayList<CustomPath> undonePaths = new ArrayList<CustomPath>();
	//private Map<Path, Integer> colorsMap = new HashMap<Path, Integer>();
	public static int selectedColor;


	/**
	 * The constructor for CustomViewForDrawing
	 * This constructor calls the setupDrawing()
	 * method. This constructor is called only 
	 * once when the application layout is first
	 * created upon launch.
	 * @param context
	 * @param attrs
	 */
	public CustomViewForDrawing(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		System.out.println("customviewfordrawing constructor");
		setUpDrawing();
	}
	
	/**
	 * This function returns the current canvas
	 * being worked upon by the user. 
	 * @return drawCanvas the current canvas being 
	 * worked upon by the user
	 */
	public Canvas getCanvas()
	{
		return drawCanvas;
	}
	
	/**
	 * This method initializes the attributes of the 
	 * CustomViewForDrawing class.
	 */
	public void setUpDrawing(){
		System.out.println("setup drawing");
		drawPaint = new Paint();;
		drawPath = new CustomPath(paintColor, brushSize);
		erase = false;
		smoothStrokes = false;
		drawPaint.setColor(paintColor);
		drawPaint.setAntiAlias(true);
		drawPaint.setStyle(Paint.Style.STROKE);
		drawPaint.setStrokeJoin(Paint.Join.ROUND);
		drawPaint.setStrokeCap(Paint.Cap.ROUND);

		canvasPaint = new Paint(Paint.DITHER_FLAG);

		
		brushSize = getResources().getInteger(R.integer.medium_size);
		prevBrushSize = brushSize;
		//selectedColor = getResources().getColor(Color.CYAN);
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int wprev, int hprev){
		super.onSizeChanged(w, h, wprev, hprev);
		System.out.println("onsizechanged");
		canvasBitmap = Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);
		drawCanvas = new Canvas(canvasBitmap);
	}
	
	/**
	 * This method is called when a stroke is drawn on the canvas
	 * as a part of the painting.
	 */
	@Override
	protected void onDraw(Canvas canvas){
		System.out.println("onDraw");
		super.onDraw(canvas);
		canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);

		for (CustomPath p : paths){
			drawPaint.setStrokeWidth(p.getBrushThickness());
			drawPaint.setColor(p.getColor());
			canvas.drawPath(p, drawPaint);
		}
		if(!drawPath.isEmpty()) {
			drawPaint.setStrokeWidth(drawPath.getBrushThickness());
			drawPaint.setColor(drawPath.getColor());
			canvas.drawPath(drawPath, drawPaint);
		}
	}
	
	/**
	 * This method acts as an event listener when a touch
	 * event is detected on the device.
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event){
		System.out.println("ontouchevent");
		float touchX = event.getX();
		float touchY = event.getY();
		//System.out.println(touchX + " " + touchY);

		switch(event.getAction()){
		case MotionEvent.ACTION_DOWN:
			drawPath.setColor(paintColor);
			drawPath.setBrushThickness(brushSize);
			if(smoothStrokes & !erase) {
				startX = touchX;
				startY = touchY;
			}
			//undonePaths.clear();
			drawPath.reset();
			drawPath.moveTo(touchX, touchY);
			break;
		case MotionEvent.ACTION_MOVE:
			drawPath.lineTo(touchX, touchY);
			break;
		case MotionEvent.ACTION_UP:
			if(smoothStrokes && !erase) {

				endX = touchX;
				endY = touchY;
				drawPath.reset();
				drawPath.moveTo(startX, startY);
				drawPath.lineTo(endX, endY);
			}
			//prob in these 2 lines
			//drawCanvas.drawPath(drawPath, drawPaint);
			
			//colorsMap.put(drawPath, paintColor);
			paths.add(drawPath);
			drawPath = new CustomPath(paintColor, brushSize);

			
			//drawPath.reset();
			break;
		default:
			return false;
		}

		invalidate();
		return true;
	}
	
	/**
	 * This function is called when the user selects the undo
	 * command from the application. This function removes the 
	 * last stroke input by the user depending on the 
	 * number of times undo has been activated.
	 */
	public void onClickUndo () {
		if (paths.size()>0)  {

			undonePaths.add(paths.remove(paths.size()-1));
			invalidate();
		} else  {
			Log.i("undo", "Undo elsecondition");
		}          
	}
	
	/**
	 * This function is called when the user selects the redo
	 * command from the application. This function redoes the 
	 * last stroke undone by the user depending on the 
	 * number of times redo has been activated.Max number of
	 * redo depends on the number of undo's activated by the user.
	 */
	public void onClickRedo (){
		if (undonePaths.size()>0)  {
			paths.add(undonePaths.remove(undonePaths.size()-1));
			invalidate();
		}
		else  {
			Log.i("undo", "Redo elsecondition");
		}          
	}

	/**
	 * This function is called when the user desires a color change.
	 * This functions sets the paintColor object of CustomViewForDrawing.
	 * @param newColor
	 */
	public void setColor(String newColor){
		System.out.println("setcolor");
		//invalidate();
		System.out.println(newColor + " !!!!!!!!!!!!!!!!!!!!!!!!!!");
		paintColor = Color.parseColor(newColor);
		drawPaint.setColor(paintColor);
	}
	
	/**
	 * This function returns the current color that has been 
	 * selected.
	 * @return current color
	 */
	public int getColor()
	{
		return paintColor;
	}
	
	/**This method is called when either the brush or the eraser
	* sizes are to be changed. This method sets the brush/eraser 
	* sizes to the new values depending on user selection.
	*/
	public void setSizeForBrush(float newSize){
		System.out.println("setsizeforbrush");
		float pixelAmount = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, newSize, getResources().getDisplayMetrics());
		brushSize = pixelAmount;
		drawPaint.setStrokeWidth(brushSize);
	}
	
	/**
	 * This functions sets the previous brush 
	 * size upon brush size change.
	 * @param prevSize
	 */
	public void setPrevBrushSize(float prevSize){
		System.out.println("prevsize");
		prevBrushSize = prevSize;
	}
	
	/**
	 * This function is called when the brush size 
	 * is changed by the user. This function returns
	 * previous brush size used prior to brush size 
	 * change
	 * @return
	 */
	public float getPrevBrushSize(){
		System.out.println("getprevbrushsize");
		return prevBrushSize;
	}
	
	/**
	 * This function sets the paint color
	 * to white when eraser functionality is 
	 * selected by the user. 
	 * @param bErase
	 */
	public void setErase(boolean bErase){
		System.out.println("setErase");
		erase = bErase;
		if(erase)
		{
			//drawPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
			paintColor = Color.parseColor("#FFFFFF");
			drawPaint.setColor(paintColor);
		}
		else
			drawPaint.setXfermode(null);
	}
	
	/**
	 * This function is called when the new file
	 * functionality is enabled by the user. This
	 * function reinitializes the CustomViewForDrawing
	 * attributes when a new canvas is produced.
	 */
	public void newStart(){
		System.out.println("newstart");
		drawCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
		//changes 03/10/2013 to set default color to gey when opening new document
		paths.clear();
		undonePaths.clear();
		drawPaint.setColor(0xFF787878);
		invalidate();
	}
}

class CustomPath extends Path{
	private int color;
	private float brushThickness;
	
	public float getBrushThickness() {
		return brushThickness;
	}

	public void setBrushThickness(float brushThickness) {
		this.brushThickness = brushThickness;
	}

	public CustomPath(int color, float brushThickness) {
		super();
		this.color = color;
		this.brushThickness = brushThickness;
	}

	public int getColor() {
		return color;
	}
	
	public void setColor(int color) {
		this.color = color;
	}
	
}