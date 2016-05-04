/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.axaet.activity;

import java.lang.reflect.Method;

import com.axaet.application.MyApplication;
import com.axaet.axaibeacon.R;
import com.axaet.service.BluetoothLeService;
import com.axaet.util.CustomProgress;
import com.axaet.util.SampleGattAttributes;
import com.axaet.utils.Conversion;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ModifyiBeaconActivity extends Activity implements OnClickListener {

	private int time = 200;
	private KeyboardView keyboardView;
	private Button modifyButton;
	private String mDeviceName;
	private String mDeviceAddress;
	private BluetoothLeService mBluetoothLeService;
	private int num = 32;
	private String Major;
	private String Minor;
	private String Period;
	private int txPowerNumber;
	private String uuid;
	private ArrayAdapter<String> arrayAdapter;

	private EditText uuid_text;
	private EditText major_text;
	private EditText minor_text;
	private AutoCompleteTextView period_text;
	private EditText deviceName;
	private EditText password;
	private TextView number_text;
	private EditText edit_txpower;
	private ImageView setpassword;

	private boolean isActive = true;

	private Dialog dialog;

	private MyApplication application;

	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {

			switch (msg.what) {
			case 0:
				Toast.makeText(ModifyiBeaconActivity.this, getString(R.string.toast_password_error), Toast.LENGTH_SHORT)
						.show();
				break;
			case 1:
				if (isActive) {
					sendData();
				}
				break;
			}
		}
	};

	private final ServiceConnection mServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName componentName, IBinder service) {
			mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
			application.bluetoothLeService = mBluetoothLeService;
			if (!mBluetoothLeService.initialize()) {
				finish();
			}
			mBluetoothLeService.connect(mDeviceAddress);
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			mBluetoothLeService = null;
		}
	};

	/**
	 * Bluetooth device callback
	 */
	private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
		@SuppressLint("DefaultLocale")
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
			} else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
				dialog.dismiss();
				Toast.makeText(ModifyiBeaconActivity.this, getString(R.string.toast_connect_failed), Toast.LENGTH_SHORT)
						.show();
				finish();
			} else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
				byte[] datas = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
				datas = Conversion.AxaBeacon_Decrypt(datas);
				if (dialog != null && dialog.isShowing()) {
					dialog.dismiss();
				}
				if (datas == null) {
					return;
				}
				String uuidTemp = Conversion.bytesToHexString(datas);
				if (5 == datas[0]) {
					Message message = handler.obtainMessage();
					message.what = 1;
					message.sendToTarget();
				} else if (6 == datas[0]) {
					Message message = handler.obtainMessage();
					message.what = 0;
					message.sendToTarget();
				} else if (17 == datas[0]) {
					uuid = uuidTemp.substring(2, 34).toUpperCase();
					uuid_text.setText(uuid);
				} else if (18 == datas[0]) {
					Major = Long.parseLong(uuidTemp.substring(2, 6), 16) + "";
					Minor = Long.parseLong(uuidTemp.substring(6, 10), 16) + "";
					Period = Long.parseLong(uuidTemp.substring(10, 14), 16) + "";
					txPowerNumber = datas[7];
					edit_txpower.setText(txPowerNumber + "");
					major_text.setText(Major);
					minor_text.setText(Minor);
					period_text.setText(Period);
				}
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_modifyibeacon);
		application = (MyApplication) getApplication();
		initView();
		Intent intent = getIntent();
		mDeviceName = intent.getStringExtra(SampleGattAttributes.EXTRAS_DEVICE_NAME);
		mDeviceAddress = intent.getStringExtra(SampleGattAttributes.EXTRAS_DEVICE_ADDRESS);

		Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
		bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

		deviceName.setText(mDeviceName);
		keyboardView = (KeyboardView) findViewById(R.id.keyboard_view);
		keyboardView.setKeyboard(new Keyboard(this, R.layout.keycontent));
		keyboardView.setEnabled(true);
		keyboardView.setPreviewEnabled(false);
		setedit(uuid_text);
		registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
		if (mBluetoothLeService != null) {
			mBluetoothLeService.connect(mDeviceAddress);
		}
		dialog = CustomProgress.show(this, getString(R.string.dialog_connecting), true, keyListener);
	}

	OnKeyListener keyListener = new OnKeyListener() {
		@Override
		public boolean onKey(DialogInterface arg0, int arg1, KeyEvent arg2) {
			if (dialog != null) {
				dialog.dismiss();
			}
			if (mBluetoothLeService != null) {
				mBluetoothLeService.close();
			}
			finish();
			return false;
		}
	};

	private void initView() {
		uuid_text = (EditText) findViewById(R.id.uuid_text);
		major_text = (EditText) findViewById(R.id.major_text);
		minor_text = (EditText) findViewById(R.id.minor_text);
		period_text = (AutoCompleteTextView) findViewById(R.id.period_text);

		String[] arr = { "0", "500", "1000", "2000" };
		arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arr);
		period_text.setAdapter(arrayAdapter);

		deviceName = (EditText) findViewById(R.id.deviceName);
		password = (EditText) findViewById(R.id.password);
		number_text = (TextView) findViewById(R.id.number_text);

		major_text.setOnTouchListener(onTouchListener);
		minor_text.setOnTouchListener(onTouchListener);
		period_text.setOnTouchListener(onTouchListener);
		deviceName.setOnTouchListener(onTouchListener);
		password.setOnTouchListener(onTouchListener);
		uuid_text.addTextChangedListener(new TextWatcher() {
			private CharSequence temp;
			private int selectionStart;
			private int selectionEnd;

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before, int count) {
				temp = s;
			}

			public void afterTextChanged(Editable s) {
				int number = s.length();
				number_text.setText(number + "/32");
				selectionStart = uuid_text.getSelectionStart();
				selectionEnd = uuid_text.getSelectionEnd();
				if (temp.length() > num) {
					s.delete(selectionStart - 1, selectionEnd);
					int tempSelection = selectionEnd;
					uuid_text.setText(s);
					uuid_text.setSelection(tempSelection);
				}
			}
		});
		edit_txpower = (EditText) findViewById(R.id.edit_txpower);
		modifyButton = (Button) findViewById(R.id.button_modify);
		modifyButton.setOnClickListener(this);
		setpassword = (ImageView) findViewById(R.id.setpassword);
		setpassword.setOnClickListener(this);
	};

	@Override
	protected void onResume() {
		super.onResume();
		isActive = true;
	}

	private static IntentFilter makeGattUpdateIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
		intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
		return intentFilter;
	}

	private void showKeyboard() {
		int visibility = keyboardView.getVisibility();
		if (visibility == View.GONE || visibility == View.INVISIBLE) {
			keyboardView.setVisibility(View.VISIBLE);
		}
	}

	private void hideKeyboard() {
		int visibility = keyboardView.getVisibility();
		if (visibility == View.VISIBLE) {
			keyboardView.setVisibility(View.INVISIBLE);
		}
	}

	private void hintKbTwo() {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm.isActive() && getCurrentFocus() != null) {
			if (getCurrentFocus().getWindowToken() != null) {
				imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
			}
		}
	}

	public void setedit(final EditText edit) {
		if (android.os.Build.VERSION.SDK_INT <= 10) {
			edit.setInputType(InputType.TYPE_NULL);
		} else {
			this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
			try {
				Class<EditText> cls = EditText.class;
				Method setShowSoftInputOnFocus;
				setShowSoftInputOnFocus = cls.getMethod("setShowSoftInputOnFocus", boolean.class);
				setShowSoftInputOnFocus.setAccessible(true);
				setShowSoftInputOnFocus.invoke(edit, false);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		edit.setCursorVisible(true);
		edit.setSingleLine(false);
		edit.setOnTouchListener(new OnTouchListener() {

			@SuppressLint("ClickableViewAccessibility")
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				hintKbTwo();
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					showKeyboard();
					keyboardView.setSelected(true);
					keyboardView.setOnKeyboardActionListener(new OnKeyboardActionListener() {
						public void onKey(int primaryCode, int[] keyCodes) {
							Editable editable = edit.getText();
							int start = edit.getSelectionStart();
							if (primaryCode == Keyboard.KEYCODE_CANCEL) {
								hideKeyboard();
							} else if (primaryCode == Keyboard.KEYCODE_DELETE) {
								if (editable != null && editable.length() > 0) {
									try {
										editable.delete(start - 1, start);
									} catch (Exception e) {
									}
								}
							} else if (primaryCode == 57419) {
								if (start > 0) {
									edit.setText("");
								}
							} else if (primaryCode == 57421) {
								if (start < edit.length()) {
									edit.setSelection(start + 1);
								}
							} else {
								editable.insert(start, Character.toString((char) primaryCode));
							}
						}

						@Override
						public void onPress(int primaryCode) {
						}

						@Override
						public void onRelease(int primaryCode) {
						}

						@Override
						public void onText(CharSequence text) {
						}

						@Override
						public void swipeDown() {
						}

						@Override
						public void swipeLeft() {
						}

						@Override
						public void swipeRight() {
						}

						@Override
						public void swipeUp() {
						}
					});
				}
				return false;
			}
		});
	}

	OnTouchListener onTouchListener = new OnTouchListener() {

		@SuppressLint("ClickableViewAccessibility")
		@Override
		public boolean onTouch(View arg0, MotionEvent arg1) {
			hideKeyboard();
			return false;
		}
	};

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			if (keyboardView.isShown()) {
				hideKeyboard();
				hintKbTwo();
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (R.id.button_modify == id) {
			initData();
		} else if (R.id.setpassword == id) {
			Intent intent = new Intent(this, ModifyPasswoedActivity.class);
			intent.putExtra(SampleGattAttributes.EXTRAS_DEVICE_NAME, mDeviceName);
			startActivity(intent);
		}
	}

	private String MajorTemp = "";
	private String MinorTemp = "";
	private String PeriodTemp = "";
	private String NameTemp = "";
	private String uuidTemp = "";

	/**
	 * Determine whether the input data format is correct, and send a password
	 * to verify
	 */
	private void initData() {
		if (TextUtils.isEmpty(password.getText().toString())) {
			Toast.makeText(ModifyiBeaconActivity.this, getString(R.string.toast_input_password), Toast.LENGTH_LONG)
					.show();
			return;
		}
		MajorTemp = major_text.getText().toString();
		MinorTemp = minor_text.getText().toString();
		PeriodTemp = period_text.getText().toString();
		uuidTemp = uuid_text.getText().toString();
		NameTemp = deviceName.getText().toString();
		String txpower = edit_txpower.getText().toString();
		if (!TextUtils.isEmpty(uuidTemp) && uuidTemp.length() == 32) {
		} else {
			Toast.makeText(ModifyiBeaconActivity.this, getString(R.string.toast_input_uuid), Toast.LENGTH_SHORT).show();
			return;
		}
		if (TextUtils.isEmpty(MajorTemp)) {
			Toast.makeText(ModifyiBeaconActivity.this, getString(R.string.toast_input_major), Toast.LENGTH_SHORT)
					.show();
			return;
		} else if (Integer.parseInt(MajorTemp) > 65535) {
			Toast.makeText(ModifyiBeaconActivity.this, getString(R.string.toast_major_range), Toast.LENGTH_SHORT)
					.show();
			return;
		}
		if (TextUtils.isEmpty(MinorTemp)) {
			Toast.makeText(ModifyiBeaconActivity.this, getString(R.string.toast_input_minor), Toast.LENGTH_SHORT)
					.show();
			return;
		}
		if (Integer.parseInt(MinorTemp) > 65535) {
			Toast.makeText(ModifyiBeaconActivity.this, getString(R.string.toast_minor_range), Toast.LENGTH_SHORT)
					.show();
			return;
		}
		if (TextUtils.isEmpty(PeriodTemp)) {
			Toast.makeText(ModifyiBeaconActivity.this, getString(R.string.toast_input_period), Toast.LENGTH_SHORT)
					.show();
			return;
		} else {
			if (Integer.parseInt(PeriodTemp) <= 100 || Integer.parseInt(PeriodTemp) > 9800) {
				Toast.makeText(ModifyiBeaconActivity.this, "9800>=Period>100", Toast.LENGTH_SHORT).show();
				return;
			}
		}
		if (TextUtils.isEmpty(txpower)) {
			Toast.makeText(ModifyiBeaconActivity.this, getString(R.string.toast_input_txpower), Toast.LENGTH_SHORT)
					.show();
			return;
		}
		try {
			txPowerNumber = Integer.valueOf(txpower);
		} catch (Exception e) {
			Toast.makeText(ModifyiBeaconActivity.this, "2>=txpower>=-21", Toast.LENGTH_SHORT).show();
			return;
		}
		if (txPowerNumber >= -21 && txPowerNumber <= 2) {

		} else {
			Toast.makeText(ModifyiBeaconActivity.this, "2>=txpower>=-21", Toast.LENGTH_SHORT).show();
			return;
		}

		Toast.makeText(ModifyiBeaconActivity.this, getString(R.string.toast_modifying), Toast.LENGTH_SHORT).show();

		byte[] bs = Conversion.str2Byte(password.getText().toString(), (byte) 0x04);
		bs = Conversion.AxaBeacon_Encrypt(bs);
		mBluetoothLeService.LostWriteData(bs);
		// Prevents data from being sent too fast,below the same
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * After the password authentication is successful, send the modified data
	 */
	private void sendData() {
		// modify the uuid
		byte[] bs2 = Conversion.hex2Byte(uuidTemp);
		bs2 = Conversion.AxaBeacon_Encrypt(bs2);
		mBluetoothLeService.LostWriteData(bs2);
		// Prevents data from being sent too fast,below the same
		try {
			Thread.sleep(500);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}

		// modify the major , minor,period and txpower
		byte[] data = new byte[20];
		data[0] = (byte) 0x02;
		data[1] = (byte) (Integer.parseInt(MajorTemp) / 256);
		data[2] = (byte) (Integer.parseInt(MajorTemp) % 256);
		data[3] = (byte) (Integer.parseInt(MinorTemp) / 256);
		data[4] = (byte) (Integer.parseInt(MinorTemp) % 256);
		data[5] = (byte) (Integer.parseInt(PeriodTemp) / 256);
		data[6] = (byte) (Integer.parseInt(PeriodTemp) % 256);
		data[7] = (byte) txPowerNumber;
		data = Conversion.AxaBeacon_Encrypt(data);
		mBluetoothLeService.LostWriteData(data);
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// modify the deviceName
		if (!TextUtils.isEmpty(NameTemp) && !mDeviceName.equals(NameTemp)) {
			NameTemp = deviceName.getText().toString();
			byte[] Namebs = Conversion.str2ByteDeviceName(NameTemp);
			Namebs = Conversion.AxaBeacon_Encrypt(Namebs);
			mBluetoothLeService.LostWriteData(Namebs);
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		// close the device
		byte[] data2 = new byte[20];
		data2[0] = (byte) 0x03;
		data2 = Conversion.AxaBeacon_Encrypt(data2);
		mBluetoothLeService.LostWriteData(data2);
		Toast.makeText(ModifyiBeaconActivity.this, getString(R.string.toast_modify_successfully), Toast.LENGTH_SHORT)
				.show();
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		finish();
	}

	@Override
	protected void onPause() {
		super.onPause();
		isActive = false;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mGattUpdateReceiver);
		mBluetoothLeService.disconnect();
		unbindService(mServiceConnection);
		mBluetoothLeService = null;
	}
}
