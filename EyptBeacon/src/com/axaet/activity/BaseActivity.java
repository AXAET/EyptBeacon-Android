package com.axaet.activity;

import com.axaet.application.MyApplication;
import com.axaet.axaibeacon.R;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.view.Window;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;


/**
 * base class
 *
 */
public class BaseActivity extends Activity {

	public MyApplication application;
	public BluetoothAdapter bluetoothAdapter;
	public ProgressBar refreshProgress;
	public TextView textDeviceNum;
	public ListView listViewDevice;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		application = (MyApplication) getApplication();
		bluetoothAdapter = application.mBluetoothAdapter;
		setContentView(R.layout.activity_scan);
		initView();
	}

	/**
	 * start scanning ,stop scanning
	 * 
	 * @param enable
	 * @param mLeScanCallback
	 */
	@SuppressWarnings("deprecation")
	public void scanLeDevice(final boolean enable, BluetoothAdapter.LeScanCallback mLeScanCallback) {
		if (enable) {
			bluetoothAdapter.stopLeScan(mLeScanCallback);
			bluetoothAdapter.startLeScan(mLeScanCallback);
		} else {
			bluetoothAdapter.stopLeScan(mLeScanCallback);
		}
	}

	private void initView() {
		refreshProgress = (ProgressBar) findViewById(R.id.progressBar);
		textDeviceNum = (TextView) findViewById(R.id.text_devicenum);
		listViewDevice = (ListView) findViewById(R.id.listView_device);
	}

}
