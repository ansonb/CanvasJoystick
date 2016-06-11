package com.nuk.joystickapp;

import com.nuk.joystickapp.LayoutObjectsContainer.LowerCircle;
import com.nuk.joystickapp.LayoutObjectsContainer.UpperCircle;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
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
	private Paint basePaint;
	private Paint shadPaint;
	private Paint btnPaint;
	private Paint overlayPaint;
	private static final float TOLERANCE = 5;
	private int radius = 50;
	private static int touchDownRadius = 100;
	

	//public static int status = 2;
	//public static float centerX = 360;
	//public static float centerY = 540;
	public static float mX, mY;
	private float bobX, bobY;
	public static float x_pos = 127, y_pos = 127;	
	
	public boolean withinRange = false;
    
	public static enum EVENT{ACTION_DOWN, ACTION_MOVE, ACTION_UP};
	public static enum REGION{CENTER_BUFFER, JOYSTICK_RANGE, OUTSIDE_RANGE};
	
	public static EVENT touchEvent = EVENT.ACTION_UP;
	public static REGION touchRegion = REGION.OUTSIDE_RANGE;
	
	private static final int JOYSTICK_REST_X = 127;
	private static final int JOYSTICK_REST_Y = 127;

	
	public CanvasView(Context c, AttributeSet attrs) {
		super(c, attrs);
		context = c;		

		definePaintAttributes();
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
	    
		if(MainActivity.firstEntryinApp){
			setLayout(canvas);
			
			updateJoystickCoordinates();
			
			drawBob(canvas);
			drawOnOffBtn(canvas);
			drawText(canvas);
						
			MainActivity.firstEntryinApp = false;
		}else{
			setLayout(canvas);
			drawOnOffBtn(canvas);

			if(!withinRange && (touchEvent==EVENT.ACTION_DOWN || touchEvent==EVENT.ACTION_MOVE)){
				drawCenterBufferCircle(canvas);
			}
						
			drawStick(canvas);
			drawBob(canvas);
			drawText(canvas);
			/*
			int centerX = LowerCircle.center_x;
			int centerY = LowerCircle.center_y;
			
			if(touchEvent==EVENT.ACTION_DOWN || touchEvent==EVENT.ACTION_MOVE){
				if(!withinRange){					
					int range = (int)Math.sqrt( Math.pow(mX-centerX, 2)+Math.pow(mY-centerY, 2));
					if(range>100){
					    x_pos = 127;
					    y_pos = 127;
					    
						//canvas.drawCircle(centerX, centerY, bobRadius, mPaint);
					    drawBob(canvas, centerX, centerY);
						canvas.drawText(Integer.toString((int)x_pos)+", "+Integer.toString((int)y_pos),
								centerX-50, centerY+100, textPaint);					
						canvas.drawCircle(centerX, centerY, touchDownRadius, touchDownPaint);
					}else{
						withinRange = true;

					    x_pos = mapX(mX);
					    y_pos = mapY(mY);	
					    
						//canvas.drawLine(centerX, centerY, mX-60-(int)(35*Math.cos(findTheeta())),
						//		mY-60+(int)(35*Math.sin(findTheeta())), shadPaint);
						canvas.drawLine(centerX, centerY, mX, mY, stickPaint);						
						//canvas.drawCircle(mX, mY, bobRadius, mPaint);	
						drawBob(canvas, mX, mY);
						canvas.drawText(Integer.toString((int)x_pos)+", "+Integer.toString((int)y_pos),
								centerX-50, centerY+100, textPaint);					
					}				
				}else{
				    x_pos = mapX(mX);
				    y_pos = mapY(mY);				
					
					//canvas.drawLine(centerX, centerY, mX-60-(int)(35*Math.cos(findTheeta())),
					//		mY-60+(int)(35*Math.sin(findTheeta())), shadPaint);				canvas.drawLine(centerX, centerY, mX, mY, stickPaint);					
					//canvas.drawCircle(mX, mY, bobRadius, mPaint);	
				    drawBob(canvas, mX, mY);
					canvas.drawText(Integer.toString((int)x_pos)+", "+Integer.toString((int)y_pos),
							centerX-50, centerY+100, textPaint);				
				}
			}

			if(touchEvent==EVENT.ACTION_UP){
			    x_pos = 127;
			    y_pos = 127;
			    
				//canvas.drawCircle(centerX, centerY, bobRadius, mPaint);
			    drawBob(canvas, centerX, centerY);
				canvas.drawText(Integer.toString((int)x_pos)+", "+Integer.toString((int)y_pos),
						centerX-50, centerY+100, textPaint);
				
				withinRange = false;
			}
			*/			
		}
		
	}
	
	public void updateJoystickCoordinates(){
		updateRegion();
		
		switch(touchEvent){
		case ACTION_UP:
			bobX = LayoutObjectsContainer.LowerCircle.center_x;
			bobY = LayoutObjectsContainer.LowerCircle.center_y;
			
			withinRange = false;
			break;
			
		case ACTION_DOWN:
			if(touchRegion == REGION.CENTER_BUFFER){
				bobX = mX;
				bobY = mY;
				
				withinRange = true;
			}else{
				bobX = LayoutObjectsContainer.LowerCircle.center_x;
				bobY = LayoutObjectsContainer.LowerCircle.center_y;
								
			}
			break;
		case ACTION_MOVE:
			if(touchRegion == REGION.CENTER_BUFFER){
				bobX = mX;
				bobY = mY;				
				
				withinRange = true;
			}else if(touchRegion == REGION.JOYSTICK_RANGE){
				if(withinRange){
					bobX = mX;
					bobY = mY;					
				}else{
					bobX = LowerCircle.center_x;
					bobY = LowerCircle.center_y;				
				}
			}else{
				int left = LowerCircle.center_x-LowerCircle.radius;
				int right = LowerCircle.center_x+LowerCircle.radius;
				int top = LowerCircle.center_y-LowerCircle.radius;
				int bottom = LowerCircle.center_y+LowerCircle.radius;
				
				if(withinRange){
					if(mX>left && mX<right){
						bobX = mX;
					}else if(mY>top && mY<bottom){
						bobY = mY;
					}else{
						//Do nothing
					}					
				}else{
					bobX = LowerCircle.center_x;
					bobY = LowerCircle.center_y;						
				}

			}
			break;
		}
		
		x_pos = newMapX(bobX);
		y_pos = newMapY(bobY);
	}
	
	private static final int MAX_JOYSTICK_VAL_X = 255;
	private static final int MAX_JOYSTICK_VAL_Y = 255;
	
	private float newMapX(float x) {
		float result;
		
		float left = LowerCircle.center_x-LowerCircle.radius;
		float right = LowerCircle.center_x+LowerCircle.radius;
		
		result = MAX_JOYSTICK_VAL_X - MAX_JOYSTICK_VAL_X*(x-left)/(right-left);
		
		return result;
	}

	private float newMapY(float y) {
		float result;
		
		float top = LowerCircle.center_y-LowerCircle.radius;
		float bottom = LowerCircle.center_y+LowerCircle.radius;
		
		result = MAX_JOYSTICK_VAL_X - MAX_JOYSTICK_VAL_X*(y-top)/(bottom-top);
		
		return result;
	}

	private void updateRegion(){
		int range = (int)Math.sqrt( Math.pow(mX-LayoutObjectsContainer.LowerCircle.center_x, 2)
				+Math.pow(mY-LayoutObjectsContainer.LowerCircle.center_y, 2));
		
		int left = LowerCircle.center_x-LowerCircle.radius;
		int right = LowerCircle.center_x+LowerCircle.radius;
		int top = LowerCircle.center_y-LowerCircle.radius;
		int bottom = LowerCircle.center_y+LowerCircle.radius;
		
		if(range<100){
			touchRegion = REGION.CENTER_BUFFER;
		}else if(mX>left && mX<right && mY>top && mY<bottom){
			touchRegion = REGION.JOYSTICK_RANGE;
		}else{
			touchRegion = REGION.OUTSIDE_RANGE;
		}
	}

	/*
	private void drawBob(Canvas canvas, float x, float y){
		canvas.drawCircle(x, y, bobRadius, mPaint);	
	}
	*/
	
	private void drawBob(Canvas canvas){
		canvas.drawCircle(bobX, bobY, bobRadius, mPaint);
	}
	
	private void drawCenterBufferCircle(Canvas canvas){
		canvas.drawCircle(LowerCircle.center_x, LowerCircle.center_y,
				touchDownRadius, touchDownPaint);
	}
	
	private void drawStick(Canvas canvas){
		canvas.drawLine(LowerCircle.center_x, LowerCircle.center_y, bobX, bobY, stickPaint);	
	}
	
	private void drawText(Canvas canvas){
		canvas.drawText(Integer.toString((int)x_pos)+", "+Integer.toString((int)y_pos),
				UpperCircle.center_x-50, UpperCircle.center_y+UpperCircle.radius, textPaint);
	}
	
	private void drawOnOffBtn(Canvas canvas){
		if(ON_OFF_BTN.getBtnState() == Button.ON){
			btnPaint.setColor(Color.GREEN);
		}else{
			btnPaint.setColor(Color.RED);
		}
		
		canvas.drawCircle(UpperCircle.center_x, UpperCircle.center_y,
				btnRadius, btnPaint);
		canvas.drawLine(UpperCircle.center_x, UpperCircle.center_y-btnRadius-22, UpperCircle.center_x,
				UpperCircle.center_y+btnRadius*0.5f, overlayPaint);
		canvas.drawLine(UpperCircle.center_x, UpperCircle.center_y-btnRadius*0.7f, 
				UpperCircle.center_x, UpperCircle.center_y+btnRadius*0.5f,btnPaint);
	}
	

	public void clearCanvas() {
		invalidate();
	}
	/*
	private double findTheeta(){
		double Theeta;
		Theeta = Math.atan2(centerY-mY, mX-centerX);
		return Theeta;
	}
	*/
	
	/*
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
	*/
	
	/*
	public static class centerOfJoystick{
		static int x;
		static int y;
	}

	private static class centerOfUpperCircle{
		static int x;
		static int y;
	}	
	*/
	
	//private int range;
	private int bobRadius;
	private float btnRadius;
	//private float btnInnerRadius;
	
	//default values; get's updated in the setLayout method
    private int rangeOffset = 100; 
	private  int centerOffset_Y = 90;
	
	private void setLayout(Canvas canvas){
		rangeOffset = MainActivity.height/12;
		centerOffset_Y = MainActivity.height/12;
		
		LowerCircle.center_x = MainActivity.width/2;
		LowerCircle.center_y = (3*MainActivity.height)/4-centerOffset_Y;
		
		UpperCircle.center_x = LowerCircle.center_x;
		UpperCircle.center_y = MainActivity.height/4-centerOffset_Y+2;

		
		LowerCircle.radius = MainActivity.height/4 - rangeOffset;
		UpperCircle.radius = MainActivity.height/4 - rangeOffset;

		bobRadius = LowerCircle.radius/5;
		btnRadius = UpperCircle.radius/4;
		//btnInnerRadius = btnRadius;
			
		
		canvas.drawCircle(LowerCircle.center_x, LowerCircle.center_y,
				LowerCircle.radius, basePaint);
		canvas.drawCircle(UpperCircle.center_x, UpperCircle.center_y,
				LowerCircle.radius, basePaint);
		
		Rect r = new Rect( LowerCircle.center_x-LowerCircle.radius, UpperCircle.center_y,
				LowerCircle.center_x+LowerCircle.radius, LowerCircle.center_y);
		
		canvas.drawRect(r, basePaint);	
		
		//bobX = LowerCircle.center_x;
		//bobY = LowerCircle.center_y;
		
		
		//canvas.drawCircle(LayoutObjectsContainer.LowerCircle.center_x, LayoutObjectsContainer.LowerCircle.center_y, 
		//		bobRadius, mPaint);		
		
		
	}
	

	
	private void definePaintAttributes(){
		// Paint attributes for joystick base
		basePaint = new Paint();
		basePaint.setColor(ColorContainer.Color.joystickBaseColor);
		basePaint.setStyle(Paint.Style.FILL);
		
		// Paint attributes for joystick bob
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setColor(Color.RED);
		mPaint.setStyle(Paint.Style.FILL);
		mPaint.setStrokeJoin(Paint.Join.ROUND);
		mPaint.setStrokeWidth(4f);
		//mPaint.setShadowLayer(70, -60, -60, Color.BLACK);
		//setLayerType(LAYER_TYPE_SOFTWARE, mPaint);
			
		// Paint attributes for joystick stick
		stickPaint = new Paint();
		stickPaint.setAntiAlias(true);
		stickPaint.setColor(Color.GRAY);
		stickPaint.setStyle(Paint.Style.FILL);
		stickPaint.setStrokeJoin(Paint.Join.ROUND);
		stickPaint.setStrokeWidth(10f);
		
		//stickPaint.setShadowLayer(10, -60, -60, Color.BLACK);
		
		/*
		shadPaint = new Paint();
		shadPaint.setAntiAlias(true);
		shadPaint.setColor(getResources().getColor(R.color.stickShadow));
		shadPaint.setStyle(Paint.Style.FILL);
		shadPaint.setStrokeWidth(3f);	
		shadPaint.setShadowLayer(15, 0, 0, Color.BLACK);
		*/
		
		textPaint = new Paint();
		textPaint.setTextSize(30f);
		textPaint.setColor(Color.WHITE);
		
		touchDownPaint = new Paint();
		touchDownPaint.setColor(Color.YELLOW);
		touchDownPaint.setStyle(Paint.Style.STROKE);
		touchDownPaint.setStrokeWidth(4f);		
		
		btnPaint = new Paint();
		btnPaint.setColor(Color.GREEN);
		btnPaint.setStyle(Paint.Style.STROKE);
		btnPaint.setStrokeWidth(15f);	
		
		overlayPaint = new Paint();
		overlayPaint.setColor(ColorContainer.Color.joystickBaseColor);
		overlayPaint.setStyle(Paint.Style.STROKE);
		overlayPaint.setStrokeWidth(15f);
	}
	
	private boolean touchRemainsInRangeOfOnOfBtn = false;
	public class Button{
		public static final int ON = 1;
		public static final int OFF = 0;
		
		private int state = 0;
		
		private Button(){
			state = ON;
		}
		
		private Button(int state){
			this.state = state;
		}		
		
		public int getBtnState(){
			return state;
		}
		
		public void toggleState(){
			state = (state+1)%2;
		}
	}
	
	private Button ON_OFF_BTN = new Button(Button.ON);
	
	public void updateOnOffBtnState(){
		boolean currTouchIsInRange = checkRange(mX,mY,
				UpperCircle.center_x,UpperCircle.center_y,btnRadius); 
		switch(touchEvent){
		case ACTION_UP:			
			if(touchRemainsInRangeOfOnOfBtn){
				ON_OFF_BTN.toggleState();
			}
			touchRemainsInRangeOfOnOfBtn = false;
			break;
			
		case ACTION_DOWN:
			if(currTouchIsInRange){
				touchRemainsInRangeOfOnOfBtn = true;
			}
			break;
			
		case ACTION_MOVE:
			if(touchRemainsInRangeOfOnOfBtn && !currTouchIsInRange){
				touchRemainsInRangeOfOnOfBtn = false;
			}
			break;
		}		
	}
	
	private boolean checkRange(float x, float y, float cx, float cy, float radius){
		double range = Math.sqrt( Math.pow(x-cx, 2)+Math.pow(y-cy, 2));
		if(range>radius){
			return false;
		}else {
			return true;
		}
	}
	
	/*
	private BtnState toggleButtonState(Button button){
		if(button == Button.ON){
			return BtnState.OFF;
		}else{
			return BtnState.ON;
		}
	}
	*/
	
	public Button getOnOffButton(){
		return ON_OFF_BTN;
	}
}

