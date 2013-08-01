package com.example.pololuusbcontroller.implementations;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;

import com.example.pololuusbcontroller.interfaces.IMessageSender;

public class USBMessageSender implements IMessageSender {
	private UsbDeviceConnection connection;
	private UsbDevice device;
	private UsbManager usbManager;

	public USBMessageSender(Context context) {
		//Retrieve the USB manager
		this.usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
		for(UsbDevice device : this.usbManager.getDeviceList().values()){
			//There is only one USB device plugged
			this.device = device;
			break;
		}
		//Open the connection
		this.connection = this.usbManager.openDevice(this.device);
	}

	@Override
	public void sendSetAcceleration(int num, int acceleration) {
		this.connection.controlTransfer(0x40, 0x84, acceleration, num | 0x80, null, 0, 5000);
	}

	@Override
	public void sendSetPosition(int num, int position) {
		double tar = position;
		tar /= 100;
		tar *= 1400;
		tar +=800;
		this.connection.controlTransfer(0x40, 0x85, (int)tar*4, num, null, 0, 5000);
	}

	@Override
	public void sendSetSpeed(int num, int speed) {
		this.connection.controlTransfer(0x40, 0x84, speed, num, null, 0, 5000);
	}

}
