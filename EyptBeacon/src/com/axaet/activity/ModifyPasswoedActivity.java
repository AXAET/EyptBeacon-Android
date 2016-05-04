package com.axaet.activity;

import com.axaet.application.MyApplication;
import com.axaet.axaibeacon.R;
import com.axaet.service.BluetoothLeService;
import com.axaet.util.SampleGattAttributes;
import com.axaet.utils.Conversion;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ModifyPasswoedActivity extends Activity implements OnClickListener {

	private TextView name_text;
	private EditText oldpassword_edit;
	private EditText newpassword_edit;
	private EditText confirmpassword_edit;
	private Button button_modify;
	private String deviceName;

	private MyApplication application;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_modifypassword);
		application = (MyApplication) getApplication();
		deviceName = getIntent().getStringExtra(SampleGattAttributes.EXTRAS_DEVICE_NAME);
		registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
		initView();
	}

	private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
			} else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
				Intent intent2 = new Intent(ModifyPasswoedActivity.this, ScanActivity.class);
				intent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent2);
				ModifyPasswoedActivity.this.finish();
			} else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
				byte[] datas = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
				if (datas == null) {
					return;
				}
				datas = Conversion.AxaBeacon_Decrypt(datas);
				if (5 == datas[0]) {
					handler.sendEmptyMessage(1);
				} else if (6 == datas[0]) {
					handler.sendEmptyMessage(0);
				}
			}
		}
	};

	private void initView() {
		name_text = (TextView) findViewById(R.id.deviceName_text);
		name_text.setText(deviceName);
		oldpassword_edit = (EditText) findViewById(R.id.oldpassword);
		newpassword_edit = (EditText) findViewById(R.id.newpassword);
		confirmpassword_edit = (EditText) findViewById(R.id.Confirmpassword);
		button_modify = (Button) findViewById(R.id.button_modify);
		button_modify.setOnClickListener(this);
	}

	private String oldpassword;
	private String newpassword;
	private String confirmpassword;

	@Override
	public void onClick(View arg0) {

		oldpassword = oldpassword_edit.getText().toString();
		newpassword = newpassword_edit.getText().toString();
		confirmpassword = confirmpassword_edit.getText().toString();

		// Determine whether the input data format is correct
		if (!TextUtils.isEmpty(oldpassword) && oldpassword.length() == 6) {
		} else {
			Toast.makeText(this, getString(R.string.toast_password_error), Toast.LENGTH_SHORT).show();
			return;
		}
		if (!TextUtils.isEmpty(newpassword) && newpassword.length() == 6 && oldpassword.length() == 6) {

		} else {
			Toast.makeText(this, getString(R.string.toast_new_password_length), Toast.LENGTH_SHORT).show();
			return;
		}
		if (!TextUtils.isEmpty(confirmpassword) && confirmpassword.equals(newpassword)) {

		} else {
			Toast.makeText(this, getString(R.string.toast_confirm_again), Toast.LENGTH_SHORT).show();
			return;
		}
		Toast.makeText(ModifyPasswoedActivity.this, getString(R.string.toast_modifying), Toast.LENGTH_SHORT).show();
		// Verify password
		byte[] bs = Conversion.str2Byte(oldpassword, (byte) 0x04);
		bs = Conversion.AxaBeacon_Encrypt(bs);
		application.bluetoothLeService.LostWriteData(bs);
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				Toast.makeText(ModifyPasswoedActivity.this, getString(R.string.toast_password_error),
						Toast.LENGTH_SHORT).show();
				break;
			case 1:
				sendData();
				break;
			}
		};
	};

	/**
	 * After the success of the password authentication, modify the new password
	 */
	private void sendData() {
		//modify the password
		byte[] bs2 = Conversion.str2Byte(oldpassword + newpassword, (byte) 0x09);
		bs2 = Conversion.AxaBeacon_Encrypt(bs2);
		application.bluetoothLeService.LostWriteData(bs2);
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		//close the device 
		byte[] data = new byte[20];
		data[0] = (byte) 0x03;
		data = Conversion.AxaBeacon_Encrypt(data);
		application.bluetoothLeService.LostWriteData(data);
		Toast.makeText(ModifyPasswoedActivity.this, getString(R.string.toast_modify_successfully), Toast.LENGTH_SHORT)
				.show();
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Intent intent = new Intent(ModifyPasswoedActivity.this, ScanActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		finish();
	}

	private IntentFilter makeGattUpdateIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
		intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
		return intentFilter;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mGattUpdateReceiver);
	}
}
