package com.axaet.adpter;

import java.util.Collections;
import java.util.Comparator;

import com.axaet.axaibeacon.R;
import com.axaet.beans.iBeaconClass.iBeacon;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class DeviceAdpter extends CommonBaseAdpter<iBeacon> {

	public DeviceAdpter(Context context) {
		super(context);
	}

	
	public synchronized void addData(iBeacon device) {
		if (device == null)
			return;
		boolean b = false;
		for (iBeacon iBeacon : list) {
			b = iBeacon.bluetoothAddress.equals(device.bluetoothAddress);
			if (b) {
				list.remove(iBeacon);
				list.add(device);
				break;
			}
		}
		if (!b) {
			list.add(device);
		}
		Collections.sort(this.list, comparator);
		notifyDataSetChanged();
	}

	Comparator<iBeacon> comparator = new Comparator<iBeacon>() {
		@Override
		public int compare(iBeacon h1, iBeacon h2) {
			return h2.rssi - h1.rssi;
		}
	};

	@SuppressLint("InflateParams")
	@Override
	public View getView(int position, View view, ViewGroup parent) {
		ViewHolder viewHolder;
		if (view == null) {
			view = inflater.inflate(R.layout.listitem_device, null);
			viewHolder = new ViewHolder();
			viewHolder.distanceMac = (TextView) view.findViewById(R.id.txt_mac);
			viewHolder.deviceName = (TextView) view.findViewById(R.id.txt_deviceName);
			viewHolder.deviceUUID = (TextView) view.findViewById(R.id.txt_uuid);
			viewHolder.deviceMajor = (TextView) view.findViewById(R.id.txt_major);
			viewHolder.deviceMinor = (TextView) view.findViewById(R.id.txt_minor);
			viewHolder.devicetxPower_RSSI = (TextView) view.findViewById(R.id.txt_rssi);
			view.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) view.getTag();
		}
		iBeacon device = list.get(position);
		final String deviceName = device.name;
		if (deviceName != null && deviceName.length() > 0)
			viewHolder.deviceName.setText("Name:" + device.name);
		else
			viewHolder.deviceName.setText("Name:" + resources.getString(R.string.unknown_device));

		viewHolder.distanceMac.setText("MAC:  " + device.bluetoothAddress);
		viewHolder.deviceUUID.setText("UUID:" + device.proximityUuid);
		viewHolder.deviceMajor.setText("Major:" + device.major);
		viewHolder.deviceMinor.setText("Minor:" + device.minor);
		viewHolder.devicetxPower_RSSI.setText("Rssi:" + device.rssi);
		return view;
	}

	static class ViewHolder {
		TextView deviceName;
		TextView deviceUUID;
		TextView distanceMac;
		TextView deviceMajor;
		TextView deviceMinor;
		TextView devicetxPower_RSSI;
	}

}
