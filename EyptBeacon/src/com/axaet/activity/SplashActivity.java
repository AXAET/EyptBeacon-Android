package com.axaet.activity;

import com.axaet.axaibeacon.R;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

public class SplashActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
			Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
			finish();
		}
		if (bluetoothAdapter == null) {
			Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
		Handler handler = new Handler();
		handler.postDelayed(runnable, 1500);
	}

	Runnable runnable = new Runnable() {

		@Override
		public void run() {
			Intent intent = new Intent(SplashActivity.this, ScanActivity.class);
			startActivity(intent);
			SplashActivity.this.finish();
		}
	};
}
