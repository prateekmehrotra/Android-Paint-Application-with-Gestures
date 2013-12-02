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



	public CustomViewForDrawing(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		System.out.println("customviewfordrawing constructor");
		setUpDrawing();
	}

	public Canvas getCanvas()
	{
		return drawCanvas;
	}

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

	public void onClickUndo () {
		if (paths.size()>0)  {

			undonePaths.add(paths.remove(paths.size()-1));
			invalidate();
		} else  {
			Log.i("undo", "Undo elsecondition");
		}          
	}

	public void onClickRedo (){
		if (undonePaths.size()>0)  {
			paths.add(undonePaths.remove(undonePaths.size()-1));
			invalidate();
		}
		else  {
			Log.i("undo", "Redo elsecondition");
		}          
	}


	public void setColor(String newColor){
		System.out.println("setcolor");
		//invalidate();
		System.out.println(newColor + " !!!!!!!!!!!!!!!!!!!!!!!!!!");
		paintColor = Color.parseColor(newColor);
		drawPaint.setColor(paintColor);
	}

	public int getColor()
	{
		return paintColor;
	}

	public void setSizeForBrush(float newSize){
		System.out.println("setsizeforbrush");
		float pixelAmount = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, newSize, getResources().getDisplayMetrics());
		brushSize = pixelAmount;
		drawPaint.setStrokeWidth(brushSize);
	}

	public void setPrevBrushSize(float prevSize){
		System.out.println("prevsize");
		prevBrushSize = prevSize;
	}

	public float getPrevBrushSize(){
		System.out.println("getprevbrushsize");
		return prevBrushSize;
	}

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