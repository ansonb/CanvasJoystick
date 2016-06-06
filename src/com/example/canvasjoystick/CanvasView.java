package com.example.canvasjoystick;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;

public class CanvasView extends View {

	public int width;
	public int height;
	private Bitmap mBitmap;
	private Canvas mCanvas;
	Context context;
	private Paint mPaint;
	private Paint stickPaint;
	private Paint textPaint;
	private Paint touchDownPaint;
	private Paint shadPaint;
	private static final float TOLERANCE = 5;
	private int radius = 50;
	private int touchDownRadius = 100;

	public static int status = 2;
	public static float centerX = 360;
	public static float centerY = 540;
	public static float mX, mY;
	public static float x_pos = 127, y_pos = 127;	
	
	public boolean withinRange = false;
    
	public CanvasView(Context c, AttributeSet attrs) {
		super(c, attrs);
		context = c;		

		// and we set a new Paint with the desired attributes
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setColor(Color.RED);
		mPaint.setStyle(Paint.Style.FILL);
		mPaint.setStrokeJoin(Paint.Join.ROUND);
		mPaint.setStrokeWidth(4f);
		mPaint.setShadowLayer(70, -60, -60, Color.BLACK);
		setLayerType(LAYER_TYPE_SOFTWARE, mPaint);
		
		stickPaint = new Paint();
		stickPaint.setAntiAlias(true);
		stickPaint.setColor(Color.DKGRAY);
		stickPaint.setStyle(Paint.Style.FILL);
		stickPaint.setStrokeJoin(Paint.Join.ROUND);
		stickPaint.setStrokeWidth(10f);
		//stickPaint.setShadowLayer(10, -60, -60, Color.BLACK);
		
		shadPaint = new Paint();
		shadPaint.setAntiAlias(true);
		shadPaint.setColor(getResources().getColor(R.color.stickShadow));
		shadPaint.setStyle(Paint.Style.FILL);
		shadPaint.setStrokeWidth(3f);	
		shadPaint.setShadowLayer(15, 0, 0, Color.BLACK);
		
		textPaint = new Paint();
		textPaint.setTextSize(30f);
		textPaint.setColor(Color.BLACK);
		
		touchDownPaint = new Paint();
		touchDownPaint.setColor(Color.YELLOW);
		touchDownPaint.setStyle(Paint.Style.STROKE);
		touchDownPaint.setStrokeWidth(4f);
	}

	// override onSizeChanged
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);

		// your Canvas will draw onto the defined Bitmap
		mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		mCanvas = new Canvas(mBitmap);
	}

	// override onDraw
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
	    
		// draw the mPath with the mPaint on the canvas when onDraw
		if(status==0 || status==1){
			if(!withinRange){					
				int range = (int)Math.sqrt( Math.pow(mX-centerX, 2)+Math.pow(mY-centerY, 2));
				if(range>100){
				    x_pos = 127;
				    y_pos = 127;
				    
					canvas.drawCircle(centerX, centerY, radius, mPaint);
					canvas.drawText(Integer.toString((int)x_pos)+", "+Integer.toString((int)y_pos),
							centerX-50, centerY+100, textPaint);					
					canvas.drawCircle(centerX, centerY, touchDownRadius, touchDownPaint);
				}else{
					withinRange = true;

				    x_pos = mapX(mX);
				    y_pos = mapY(mY);	
				    
					canvas.drawLine(centerX, centerY, mX-60-(int)(35*Math.cos(findTheeta())),
							mY-60+(int)(35*Math.sin(findTheeta())), shadPaint);
					canvas.drawLine(centerX, centerY, mX, mY, stickPaint);						
					canvas.drawCircle(mX, mY, radius, mPaint);	
					canvas.drawText(Integer.toString((int)x_pos)+", "+Integer.toString((int)y_pos),
							centerX-50, centerY+100, textPaint);					
				}				
			}else{
			    x_pos = mapX(mX);
			    y_pos = mapY(mY);				
				
				canvas.drawLine(centerX, centerY, mX-60-(int)(35*Math.cos(findTheeta())),
						mY-60+(int)(35*Math.sin(findTheeta())), shadPaint);				canvas.drawLine(centerX, centerY, mX, mY, stickPaint);					
				canvas.drawCircle(mX, mY, radius, mPaint);	
				canvas.drawText(Integer.toString((int)x_pos)+", "+Integer.toString((int)y_pos),
						centerX-50, centerY+100, textPaint);				
			}
		}

		if(status==2){
		    x_pos = 127;
		    y_pos = 127;
		    
			canvas.drawCircle(centerX, centerY, radius, mPaint);
			canvas.drawText(Integer.toString((int)x_pos)+", "+Integer.toString((int)y_pos),
					centerX-50, centerY+100, textPaint);
			
			withinRange = false;
		}
	}


	public void clearCanvas() {
		invalidate();
	}
	
	private double findTheeta(){
		double Theeta;
		Theeta = Math.atan2(centerY-mY, mX-centerX);
		return Theeta;
	}
	
	private char mapY(float y) {
		char mappedVal;

		float maxY = MainActivity.getScreenHeight() 
				- getResources()
				.getDimension(R.dimen.activity_vertical_margin)
				- MainActivity.getActionBarHeight() 
				- MainActivity.getStatusBarHeight();	
		
		mappedVal =  (char)(y/maxY*255);
		
		if(mappedVal=='\n'){
			mappedVal = 11;
		}
		if(mappedVal>255){
			mappedVal = 0;
		}
		
		return mappedVal;
	}

	private char mapX(float x) {
		char mappedVal;
		
		float maxX = MainActivity.getScreenWidth() 
				- getResources()
				.getDimension(R.dimen.activity_horizontal_margin);		
		
		mappedVal =  (char)(x/maxX*255);
		
		if(mappedVal=='\n'){
			mappedVal = 11;
		}
		if(mappedVal>255){
			mappedVal = 0;
		}
		
		return mappedVal;
	}	

}