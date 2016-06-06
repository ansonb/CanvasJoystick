package com.example.canvasjoystick;

import android.bluetooth.BluetoothDevice;

public class DeviceModel {
	BluetoothDevice mDevice;
	
	public DeviceModel() {
		
	}
	
	public DeviceModel(BluetoothDevice mDevice){
		this.mDevice = mDevice;
	}
}
