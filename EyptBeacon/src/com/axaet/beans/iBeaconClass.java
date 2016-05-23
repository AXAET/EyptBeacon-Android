package com.axaet.beans;

import com.axaet.utils.Conversion;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;

/**
 * IBeacon data processing
 * 
 */
public class iBeaconClass {

	static public class iBeacon implements Comparable<iBeacon> {
		public String name;
		public int major;
		public int minor;
		public String proximityUuid;
		public String bluetoothAddress;
	
		public int rssi;
	

		@Override
		public int compareTo(iBeacon another) {
			if (this.rssi < another.rssi)
				return 1;
			if (this.rssi > another.rssi)
				return -1;
			return 0;
		}
	}

	@SuppressLint("DefaultLocale")
	public static iBeacon fromScanData(BluetoothDevice device, int rssi, byte[] scanData) {
		byte[] Bytes = new byte[20];
		System.arraycopy(scanData, 11, Bytes, 0, 20);
		Bytes = Conversion.AxaBeacon_Decrypt(Bytes);
		iBeacon iBeacon = new iBeacon();
		iBeacon.major = (Bytes[16] & 0xff) * 0x100 + (Bytes[17] & 0xff);
		iBeacon.minor = (Bytes[18] & 0xff) * 0x100 + (Bytes[19] & 0xff);
		iBeacon.rssi = rssi;
		iBeacon.bluetoothAddress = device.getAddress();
		if (null == device.getName()) {
			iBeacon.name = "Unknown device";
		} else {
			iBeacon.name = device.getName();
		}

		byte[] proximityUuidBytes = new byte[16];
		System.arraycopy(Bytes, 0, proximityUuidBytes, 0, 16);
		String hexString = Conversion.bytesToHexString(proximityUuidBytes);
		StringBuilder sb = new StringBuilder();
		sb.append(hexString.substring(0, 8));
		sb.append("-");
		sb.append(hexString.substring(8, 12));
		sb.append("-");
		sb.append(hexString.substring(12, 16));
		sb.append("-");
		sb.append(hexString.substring(16, 20));
		sb.append("-");
		sb.append(hexString.substring(20, 32));
		iBeacon.proximityUuid = sb.toString().toUpperCase();
		return iBeacon;
	}
}
