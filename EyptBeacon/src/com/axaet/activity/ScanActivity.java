package com.axaet.activity;

import com.axaet.adpter.DeviceAdpter;
import com.axaet.axaibeacon.R;
import com.axaet.beans.iBeaconClass;
import com.axaet.beans.iBeaconClass.iBeacon;
import com.axaet.util.SampleGattAttributes;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

public class ScanActivity extends BaseActivity implements OnClickListener {

	private DeviceAdpter deviceAdpter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setListener();
	}

	private void setListener() {
		refreshProgress.setOnClickListener(this);
		listViewDevice.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				final iBeacon device = deviceAdpter.getItem(position);
				if (device == null || device.name == null)
					return;
				if (device.name.contains("_n")) {
					final Intent intent = new Intent(ScanActivity.this, ModifyiBeaconActivity.class);
					intent.putExtra(SampleGattAttributes.EXTRAS_DEVICE_NAME, device.name);
					intent.putExtra(SampleGattAttributes.EXTRAS_DEVICE_ADDRESS, device.bluetoothAddress);
					startActivity(intent);
				} else {
					Toast.makeText(ScanActivity.this, getString(R.string.toast_connect_tip), Toast.LENGTH_SHORT).show();
				}
			}
		});
		deviceAdpter = new DeviceAdpter(this);
		listViewDevice.setAdapter(deviceAdpter);
	}

	@Override
	protected void onResume() {
		super.onResume();
		scanLeDevice(true, mLeScanCallback);
	}

	@Override
	protected void onPause() {
		super.onPause();
		scanLeDevice(false, mLeScanCallback);
	}

	/**
	 * Scan to device callback
	 */
	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

		@Override
		public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
			if (device != null && rssi < 0 && scanRecord[5] == scanRecord[6]) {
				final iBeacon ibeacon = iBeaconClass.fromScanData(device, rssi, scanRecord);
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						deviceAdpter.addData(ibeacon);
						textDeviceNum.setText("iBeacon(" + deviceAdpter.list.size() + ")");
					}
				});
			}
		}
	};

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.progressBar:
			Toast.makeText(ScanActivity.this, getResources().getString(R.string.Searching_please_wait),
					Toast.LENGTH_SHORT).show();
			scanLeDevice(true, mLeScanCallback);
			deviceAdpter.clearData();
			textDeviceNum.setText("iBeacon(0)");
			break;
		}

	}

	private long exitTime = 0;

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
			if ((System.currentTimeMillis() - exitTime) > 2000) {
				Toast.makeText(getApplicationContext(), getString(R.string.toast_quit), Toast.LENGTH_SHORT).show();
				exitTime = System.currentTimeMillis();
			} else {
				finish();
				System.exit(0);
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

}
