package com.example.canvasjoystick;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

public class ListOfAvailableDevices extends Activity{
	
	ListView devicesList;
	ListAdapter mAdapter;
	ArrayList<BluetoothDevice> mDeviceList;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_of_devices);
		
		mDeviceList		= getIntent().getExtras().getParcelableArrayList("device.list");

		/*
        MainActivity.mConnectProgressDlg = new ProgressDialog(this);
        MainActivity.mConnectProgressDlg.setMessage("Connecting...");
        MainActivity.mConnectProgressDlg.setCancelable(true);	
        */	
		
		mAdapter = new ListAdapter(this, mDeviceList);
		mAdapter.setPairListener(new ListAdapter.OnPairButtonClickListener() {			
			@Override
			public void onPairButtonClick(int position) {
				BluetoothDevice device = mDeviceList.get(position);
				
				if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
					//MainActivity.disconnectBt(device);
					if(!MainActivity.connectedTo(device)){
						unpairDevice(device);
					}else{
						showToast("Cannot unpair while connected to the device");
					}
				} else {
					pairDevice(device);						
				}
			}
		});		
		
		mAdapter.setConnectListener(new ListAdapter.OnConnectButtonClickListener() {			
			@Override
			public void onConnectButtonClick(int position) {
				BluetoothDevice device = mDeviceList.get(position);
				
				if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
					//showToast("Device already paired");
					if(MainActivity.socketAvailable()){
						//MainActivity.mConnectProgressDlg.show();
						showToast("Please wait...Connecting Device");
						MainActivity.connectBt(device);						
					}else{
						showToast("Already Connected to " + MainActivity.mSocket
								.getRemoteDevice().getName());
					}
					/*try {
						MainActivity.mSocket = device
								.createInsecureRfcommSocketToServiceRecord(MainActivity.mUUID);
						MainActivity.mSocket.connect();
					} catch (Exception e) {
						e.printStackTrace();
						showToast(e.getLocalizedMessage());
					}*/
					//MainActivity.connectBt();
				} else {
					
				}
			}
		});				
		
		
		devicesList = (ListView) findViewById(R.id.listViewParent);
		devicesList.setAdapter(mAdapter);
		
		registerReceiver(mPairReceiver, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)); 
		
	}
	
	@Override
	public void onDestroy() {
		unregisterReceiver(mPairReceiver);
		
		super.onDestroy();
	}
	
	
	private void showToast(String message) {
		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
	}
	
    private void pairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void unpairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("removeBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }	
	
    private final BroadcastReceiver mPairReceiver = new BroadcastReceiver() {
    	@Override
	    public void onReceive(Context context, Intent intent) {
	        String action = intent.getAction();
	        
	        if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {	        	
	        	 final int state 		= intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
	        	 final int prevState	= intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);
	        	 
	        	 if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
		        	showToast("Paired");
	        	 } else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED){
	        		 showToast("Unpaired");
	        	 }
	        	 
	        	 mAdapter.notifyDataSetChanged();
	        }
	    }
	};	
}
