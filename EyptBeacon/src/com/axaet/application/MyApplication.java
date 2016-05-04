package com.axaet.application;

import com.axaet.service.BluetoothLeService;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;

public class MyApplication extends Application {

	public BluetoothAdapter mBluetoothAdapter;

	public BluetoothLeService bluetoothLeService;

	@Override
	public void onCreate() {
		super.onCreate();
		//Open system Bluetooth
		final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();
		if (mBluetoothAdapter != null && !mBluetoothAdapter.isEnabled()) {
			mBluetoothAdapter.enable();
		}
	}
}
