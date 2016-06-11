package com.example.canvasjoystick;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;

import android.support.v7.app.ActionBarActivity;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager.Query;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {

	private CanvasView customCanvas;
	
	private String deviceName = "HC-05";
	public final static UUID mUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	
	private Set<BluetoothDevice> paired;
	private BluetoothAdapter mBluetoothAdapter;
	public static BluetoothSocket mSocket;
	public static BluetoothDevice mDevice;		
	
	enum Status{
		DISCONNECTED,
		PAIRED,
		CONNECTED
	};
	
	public Status mStatus = Status.DISCONNECTED;
	
	private ProgressDialog mProgressDlg;
	public static ProgressDialog mConnectProgressDlg;
	
	Menu menu;	
	
	static int width, height;
	
	static int actionBarHeight;
	static float statusBarHeight;
	
	final int action_and_statusBar_height = 200;
		
	private ArrayList<BluetoothDevice> mDeviceList = new ArrayList<BluetoothDevice>();

	public static Object lock = new Object();
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    
		setContentView(R.layout.activity_main);	
		
	    Display display = getWindowManager().getDefaultDisplay();
	    Point p = new Point();
	    display.getSize(p);
	    width = p.x;
	    height = p.y;
	    
	    //Center of canvas not of the entire screen
	    CanvasView.centerX = (float) width/2-getResources()
	    		.getDimension(R.dimen.activity_horizontal_margin)/2;
	    CanvasView.centerY = (float) height/2-action_and_statusBar_height/2;		
		
	    //actionBarHeight = getActionBar().getHeight() ;
	    //statusBarHeight = queryStatusBarHeight();		
		
	    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	    
        mProgressDlg = new ProgressDialog(this);
        mProgressDlg.setMessage("Scanning...");
		mProgressDlg.setCancelable(false);
		mProgressDlg.setButton(DialogInterface.BUTTON_NEGATIVE, "Stop Scan", new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		        dialog.dismiss();
		        
		        mBluetoothAdapter.cancelDiscovery();
		    }
		});		
		
        mConnectProgressDlg = new ProgressDialog(this);
        mConnectProgressDlg.setMessage("Connecting...");
        mConnectProgressDlg.setCancelable(true);			
		
		
		customCanvas = (CanvasView) findViewById(R.id.signature_canvas);
		customCanvas.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				CanvasView.mX = event.getX();
			    CanvasView.mY = event.getY();
			    
			    actionBarHeight = getActionBar().getHeight() ;
			    statusBarHeight = queryStatusBarHeight();		
			    //Center of canvas not of the entire screen
			    CanvasView.centerX = (float) width/2-getResources()
			    		.getDimension(R.dimen.activity_horizontal_margin)/2;
			    CanvasView.centerY = (float) height/2-(queryStatusBarHeight()
			    		+getActionBarHeight()
			    		+getResources().getDimension(R.dimen.activity_vertical_margin))/2;				    
			  
			    /*
			    CanvasView.x_pos = mapX(CanvasView.mX);
			    CanvasView.y_pos = mapY(CanvasView.mY);
			    */
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					CanvasView.status = 0;
					customCanvas.clearCanvas();
					break;
				case MotionEvent.ACTION_MOVE:
					CanvasView.status = 1;
					customCanvas.clearCanvas();
					break;
				case MotionEvent.ACTION_UP:
					CanvasView.status = 2;				
					customCanvas.clearCanvas();
					break;
				}					
                
                /*if(mSocket!=null && mSocket.isConnected() &&
                		customCanvas.withinRange){
                    writeByte((char)CanvasView.x_pos);
                    writeByte((char)CanvasView.y_pos);
                    writeByte('\n');
                }*/	
               				
				return true;
			}
			/*
			 private char mapY(float y) {
				char mappedVal;

				float maxY = height 
						- getResources()
						.getDimension(R.dimen.activity_vertical_margin)
						- getActionBar().getHeight() 
						- queryStatusBarHeight();	
				
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
				
				float maxX = width 
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
		});
		
        IntentFilter filter = new IntentFilter();
		
		filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		filter.addAction(BluetoothDevice.ACTION_FOUND);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
		filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
		filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
		
		registerReceiver(mReceiver, filter); 
		
		
		new Thread(new Runnable(){
			@Override
			public void run(){
				while(true){
					if(mSocket!=null && mSocket.isConnected()
							&& mBluetoothAdapter.isEnabled()){
	                    writeByte((char)CanvasView.x_pos);
	                    writeByte((char)CanvasView.y_pos);
	                    writeByte('\n');					
				    }
                }								
			}
		}).start();
	}
	
	@Override
	public void onDestroy() {
		unregisterReceiver(mReceiver);
		
		super.onDestroy();
	}
		
	@Override
	public void onResume(){
		super.onResume();
		
		mDeviceList = new ArrayList<BluetoothDevice>();
	}

	public void clearCanvas(View v) {
		customCanvas.clearCanvas();
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
    	this.menu = menu;
        getMenuInflater().inflate(R.menu.main, menu);
        menu.findItem(R.id.action_BT).setIcon(R.drawable.btdis);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        if(id == R.id.action_BT){
        	
        	if(mBluetoothAdapter == null){
        		showToast("Bluetooth not supported");
        	}else{
        		if(!mBluetoothAdapter.isEnabled()){
        			enableBluetooth();
        		}else{
        			mBluetoothAdapter.startDiscovery();
        		}
        	}
        	
        	/*
        	//On first Entry
        	if(mBluetoothAdapter == null){
        		initialiseBT();
        		return true;
        	}else{
        		if(!mBluetoothAdapter.isEnabled()){
        			enableBluetooth();
            		return true;
        		}
        	}
        
        	
        	//If already connected disconnect
        	if(mSocket!=null && mSocket.isConnected()){
        		try {
					mSocket.close();
					mSocket.notifyAll();
				} catch (IOException e) {
					e.printStackTrace();
				}
        		return true;
        	}
        	
        	//If not connected connect
        	mDevice = findDevice(deviceName); //Find device in list of paired devices
        	if(mDevice==null){  //If not paired
        		scanDevice();
        		return true;
        	}else{              //If paired
        		connectBt();
        	}
        	*/
        	return true;
        }
        return super.onOptionsItemSelected(item);
    }
        
	
	//===============================Bluetooth code=============================================
    public void initialiseBT(){
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		
		if(mBluetoothAdapter == null){
		    showToast("Bluetooth not supported");
		    return;
		}
		
		if(!mBluetoothAdapter.isEnabled()){
			enableBluetooth();
		}
		
    }
    
    private void enableBluetooth(){
		Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		
	    startActivityForResult(intent, 1000);
	}
	
	public BluetoothDevice findDevice(String device){
		paired = mBluetoothAdapter.getBondedDevices();
		BluetoothDevice btDeviceDefault = null; 
		if (paired == null || paired.size() == 0) { 
			
		} else {
			for(BluetoothDevice btDevice : paired){
				if(btDevice.getName() == null){
					continue;
				}
				if(btDevice.getName().compareTo(device)==0){
					return btDevice;
				}					
			}
		}
		return null;
		//return btDeviceDefault;
	}
	
	public boolean foundDevice(String device){
		paired = mBluetoothAdapter.getBondedDevices();
		BluetoothDevice btDeviceDefault = null; 
		if (paired == null || paired.size() == 0) { 
			//showToast("No Paired Devices Found");
		} else {
			for(BluetoothDevice btDevice : paired){
				if(btDevice.getName() == null){
					continue;
				}
				if(btDevice.getName().compareTo(device)==0){
					return true;
				}					
			}
		}
		return false;
		//return btDeviceDefault;
	}
	
	public void pairDevice(BluetoothDevice device){
		try {
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
            mStatus = Status.PAIRED;
        } catch (Exception e) {
            e.printStackTrace();
            showToast(e.getLocalizedMessage());
        }
	}
	
	public void unpairDevice(BluetoothDevice device){
		 try {
	            Method method = device.getClass().getMethod("removeBond", (Class[]) null);
	            method.invoke(device, (Object[]) null);
	        } catch (Exception e) {
	            e.printStackTrace();
	            showToast(e.getLocalizedMessage());
	        }
	}
	
	public final BroadcastReceiver mReceiver = new BroadcastReceiver() {
	    @SuppressLint("NewApi") public void onReceive(Context context, Intent intent) {	    	
	        String action = intent.getAction();
	        final BluetoothDevice btDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
	        
	        if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
	        	final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
	        	 
	        	if (state == BluetoothAdapter.STATE_ON) {
	        		 //showToast("Bluetooth on");
	        		mBluetoothAdapter.startDiscovery();
	        	 }
	        	/*
	        	if(state == BluetoothAdapter.STATE_CONNECTING){
	        		 showToast("Connecting");
	        	}
	        	if(state == BluetoothAdapter.STATE_DISCONNECTING){
		        	try{
		        		mSocket.close();
		        		mStatus = Status.DISCONNECTED;
		        		showToast("Device Disconnected");
		        		menu.findItem(R.id.action_BT).setIcon(R.drawable.btdis);
		        	}catch(Exception e){
		        		e.printStackTrace();
		        		showToast(e.getLocalizedMessage());
		        	}	        	
		        }
		        */
	        } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {				
				mProgressDlg.show();
	        } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
	        	mProgressDlg.dismiss();
	        	
	        	Intent newIntent = new Intent(MainActivity.this, ListOfAvailableDevices.class);
	        	
	        	newIntent.putParcelableArrayListExtra("device.list", mDeviceList);
				
				startActivity(newIntent);	        	
	        } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
	        	BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
		        
	        	mDeviceList.add(device);
	        	
	        	showToast("Found device " + device.getName());
	        	  	
	        	/*
	        	BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
		        	        	
	        	showToast("Found device " + device.getName());
	        	
	        	if(device.getName().equals(deviceName)){
	        		mDevice = device;
	        		
	        		mBluetoothAdapter.cancelDiscovery();
		        	if(mStatus!=Status.PAIRED){
						pairDevice(mDevice);
				    }
		        	
	        	}
	        	*/
	        	
	        }  else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
	            //Device is now connected
	        	//showToast("In Broadcast acl_conn ");
	        	final BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

					if(foundDevice(device.getName())){
						try {
							//if socket is already connected reconnection will cause disconnection
		        			if(!mSocket.isConnected()){
		        				mSocket = mDevice.createInsecureRfcommSocketToServiceRecord(mUUID);
			        			mSocket.connect();
			        		}
		        			
							new Thread(new Runnable(){
								public void run(){
									readMessage_async();
								}
							}).start();
							
							mStatus = Status.CONNECTED;
							showToast("Device Connected");
							menu.findItem(R.id.action_BT).setIcon(R.drawable.bten);
							//mConnectProgressDlg.dismiss();
			            } catch (Exception e) {
							e.printStackTrace();
							showToast(e.getLocalizedMessage());
						}	
					}	        		
	        	
	

	        	/*
				//Connect only if paired
				if(foundDevice(device.getName())){
					try {
						//if socket is already connected reconnection will cause disconnection
	        			if(!mSocket.isConnected()){
	        				mSocket = mDevice.createInsecureRfcommSocketToServiceRecord(mUUID);
		        			mSocket.connect();
	        			}
	        			
						new Thread(new Runnable(){
							public void run(){
								readMessage_async();
							}
						}).start();
						
						mStatus = Status.CONNECTED;
						showToast("Device Connected");
						menu.findItem(R.id.action_BT).setIcon(R.drawable.bten);
		            } catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						showToast(e.getLocalizedMessage());
					}	
				}
				*/
	        }
	        else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {
	           //Device is about to disconnect
	        }
	        else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
	           //Device has disconnected
	        	final BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

	        	//Disconnect if device is not paired
	        	//if(!foundDevice(device.getName())){
	        		
	        	//}
	        	try{
	        		if(mSocket!=null){
		        		mSocket.close();
		        		//mSocket.notifyAll();	        			
	        		}
	        		mStatus = Status.DISCONNECTED;
	        		showToast("Device Disconnected");
	        		menu.findItem(R.id.action_BT).setIcon(R.drawable.btdis);
	        	}catch(Exception e){
	        		e.printStackTrace();
	        		showToast(e.getLocalizedMessage());
	        	}	        	
	        	/*
	        	try{
	        		if(mSocket!=null){
		        		mSocket.close();
		        		//mSocket.notifyAll();	        			
	        		}
	        		mStatus = Status.DISCONNECTED;
	        		showToast("Device Disconnected");
	        		menu.findItem(R.id.action_BT).setIcon(R.drawable.btdis);
	        	}catch(Exception e){
	        		e.printStackTrace();
	        		showToast(e.getLocalizedMessage());
	        	}
	        	*/
	        	
	        } 
	        
	    }
	};
	
    
	@SuppressLint("NewApi") public static void connectBt(){
		try {
			mSocket = mDevice.createInsecureRfcommSocketToServiceRecord(mUUID);
		    mSocket.connect();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public static void connectBt(BluetoothDevice device){
		try {
			//mConnectProgressDlg.show();
			mSocket = device.createInsecureRfcommSocketToServiceRecord(mUUID);
		    mSocket.connect();
		    //mConnectProgressDlg.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void disconnectBt(BluetoothDevice device){
		try {
			if(mSocket!=null){
				mSocket.close();
				mSocket = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}	
	
	public static boolean socketAvailable(){
		if(mSocket!=null && mSocket.isConnected()){
			return false;
		}else{
			return true;
		}
	}
	
	public static boolean connectedTo(BluetoothDevice device){
		if(mSocket!=null && mSocket.isConnected() 
				&& mSocket.getRemoteDevice().equals(device)){
			return true;
		}else{
			return false;
		}
	}
	
	@SuppressLint("NewApi") public void writeMessage(String message){	    
		try {
		    OutputStream mmOutput = mSocket.getOutputStream();
		    mmOutput.write(message.getBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			showToast(e.getLocalizedMessage());
		}
	}
	
	@SuppressLint("NewApi") public void writeByte(char message){	    
		try {
		    OutputStream mmOutput = mSocket.getOutputStream();
		    mmOutput.write(message);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			showToast(e.getLocalizedMessage());
		}
	}	
	@SuppressLint("NewApi") private void readMessage_async(){
		try {
			while(mSocket!=null && mSocket.isConnected()){
				
				InputStream mmInput = mSocket.getInputStream();
				int bytesAvailable=0;
				//Check if data is available
				if((bytesAvailable=mmInput.available())!=0){
					byte[] buffer = new byte[bytesAvailable];
					mmInput.read(buffer);
					char tmp;
					String s="";
					for(int i=0;i<bytesAvailable;i++){
						tmp = (char)buffer[i];
						s += tmp;
					}
					
					final String fs = s;				
				}
			}
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			MainActivity.this.runOnUiThread(new Runnable(){
				public void run(){
					showToast(e.getLocalizedMessage()) ;
				}
			});
		}
		
	}   
	
	public void scanDevice(){
		mBluetoothAdapter.startDiscovery();
	}
	
//====================================================================================
    private void showToast(String message){
    	Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
    }	
    
    private float queryStatusBarHeight(){
    	float statusBarHeight = 0;
    	
    	int resId = getResources().getIdentifier("status_bar_height", "dimen", "android");

    	if(resId>0){
    		statusBarHeight = getResources().getDimension(resId);
    	}
    	
    	return statusBarHeight;
    }
    
    public static int getScreenWidth(){
    	return width;
    }
    
    public static int getScreenHeight(){
    	return height;
    }
    
    public static int getActionBarHeight(){
    	return actionBarHeight;
    }
    
    public  static float getStatusBarHeight(){
    	return statusBarHeight;
    }

}
