package com.nuk.joystickapp;

import java.util.ArrayList;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.location.GpsStatus.Listener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class ListAdapter extends ArrayAdapter<BluetoothDevice>{
	
	ArrayList<BluetoothDevice> devicesList;
	Activity context;
	private OnPairButtonClickListener mPairListener;
	private OnConnectButtonClickListener mConnectListener;
	
	//private final int backgroundOdd = 0x18ffffff; 
	//private final int backgroundEven = 0xffccf5ff; 
	
	public ListAdapter(Activity context, ArrayList<BluetoothDevice> devicesList) {
		super(context, R.layout.list_of_devices, R.id.listViewParent, devicesList);
		
		this.context = context;
		this.devicesList = devicesList;		
	}
	
	static class ViewHolder{
		TextView deviceName;
		TextView deviceAddress;
		Button mPairButton;
		Button mConnectButton;
	}
	
	public BluetoothDevice getItem(int position){
		return devicesList.get(position);
	}

	public void setPairListener(OnPairButtonClickListener listener) {
		mPairListener = listener;
	}
	
	public void setConnectListener(OnConnectButtonClickListener listener) {
		mConnectListener = listener;
	}	
	
	@Override
	public View getView(final int position, View convertView, ViewGroup parent)
	{
		ViewHolder holder;
		BluetoothDevice device = devicesList.get(position);
		if(convertView == null)
		{
			LayoutInflater inflater = context.getLayoutInflater();
			convertView=inflater.inflate(R.layout.list_of_devices_child, null);

			holder = new ViewHolder();

			holder.deviceName = (TextView) convertView.findViewById(R.id.text_id);
			holder.deviceAddress = (TextView) convertView.findViewById(R.id.address_text_id);
			holder.mPairButton = (Button) convertView.findViewById(R.id.pair_button_id);
			holder.mConnectButton = (Button) convertView.findViewById(R.id.connect_button_id);

			convertView.setTag(holder);
		}
		else
		{
			holder=(ViewHolder)convertView.getTag();
		}
		
		if(position%2 == 0){
			convertView.setBackgroundColor(ColorContainer.Color.backgroundEven);
		}else{
			convertView.setBackgroundColor(ColorContainer.Color.backgroundOdd);
		}
		
		holder.deviceName.setText(device.getName());
		holder.deviceAddress.setText(device.getAddress());

		if(device.getBondState() == BluetoothDevice.BOND_BONDED)
		{
			holder.mPairButton.setText("Unpair");
		}
		else 
		{
			holder.mPairButton.setText("Pair");
		}
		
		/*
		holder.mPairButton.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				if (mPairListener != null) {
					mPairListener.onPairButtonClick(position);
				}
			}
		});
		*/
		
		final ViewHolder container = holder;
		holder.mPairButton.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch(event.getAction()){
				case MotionEvent.ACTION_DOWN:
				case MotionEvent.ACTION_MOVE:
					container.mPairButton.setBackgroundResource(R.drawable.rounded_corners_click);
					return true;
				case MotionEvent.ACTION_CANCEL:
					container.mPairButton.setBackgroundResource(R.drawable.rounded_corners_gray);
					return true;
				case MotionEvent.ACTION_UP:
					container.mPairButton.setBackgroundResource(R.drawable.rounded_corners_gray);
					if (mPairListener != null) {
						mPairListener.onPairButtonClick(position);
					}					
				    return true;
				default:
					return false;
				}
			}
		});
		
		/*
		holder.mConnectButton.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				if (mConnectListener != null) {
					mConnectListener.onConnectButtonClick(position);
				}
			}
		});		
		*/
		
		holder.mConnectButton.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch(event.getAction()){
				case MotionEvent.ACTION_DOWN:
				case MotionEvent.ACTION_MOVE:
					container.mConnectButton.setBackgroundResource(R.drawable.rounded_corners_click);
					return true;
				case MotionEvent.ACTION_CANCEL:
					container.mConnectButton.setBackgroundResource(R.drawable.rounded_corners_gray);
					return true;					
				case MotionEvent.ACTION_UP:
					container.mConnectButton.setBackgroundResource(R.drawable.rounded_corners_gray);
					if (mConnectListener != null) {
						mConnectListener.onConnectButtonClick(position);
					}				    
					return true;
				default:
					return false;
				}
			}
		});		
		

		return convertView;
	}
	
	public interface OnPairButtonClickListener {
		public abstract void onPairButtonClick(int position);
	}	
	
	public interface OnConnectButtonClickListener {
		public abstract void onConnectButtonClick(int position);
	}	

}
