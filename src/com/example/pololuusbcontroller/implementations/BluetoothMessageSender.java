package com.example.pololuusbcontroller.implementations;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.example.pololuusbcontroller.interfaces.IMessageSender;

public class BluetoothMessageSender implements IMessageSender  {

	private BluetoothSocket mmSocket;
	private OutputStream out;

	public BluetoothMessageSender(){
		Log.i("server", "BluetoothMessageSender constructor called.");
		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		BluetoothDevice bluetoothDevice = null;
		if (mBluetoothAdapter != null) {
			if (mBluetoothAdapter.isEnabled()) {
				Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
				if (pairedDevices.size() > 0) {
					for (BluetoothDevice device : pairedDevices) {
						// Add the name and address to an array adapter to show in a ListView
						bluetoothDevice = device;
					}

					try {
						this.mmSocket = bluetoothDevice.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
						try{
							this.mmSocket.connect();
						} catch (IOException connectException) {
							Log.i("server", "Socket can't connect : "+connectException.getMessage());
							connectException.printStackTrace();
							try {
								this.mmSocket.close();
							} catch (IOException closeException) {
								Log.i("server", "Can't close opened socket : "+closeException.getMessage());
							}
						}
						this.out = this.mmSocket.getOutputStream();
						Log.i("server", "OutputStream initialized.");
					}catch(Exception e){
						Log.i("server", "Something went wrong with the bluetooth connection : "+e.getMessage());
					}
				}
			}
		}
	}

	@Override
	public void sendSetAcceleration(int num, int acceleration) {
		/**
		 * Not tested
		 */
		byte [] serialBytes = new byte[6];
		serialBytes[0] = (byte)0xAA;
		serialBytes[1] = (byte)0;
		serialBytes[2] = (byte)0x09;
		serialBytes[3] = (byte)num;
		serialBytes[4] = (byte)(acceleration & 0xFF);
		serialBytes[5] = (byte)(acceleration >> 8 & 0xFF);
		try {
			this.out.write(serialBytes);
			this.out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void sendSetPosition(int num, int position) {
		byte [] serialBytes = new byte[3];
		serialBytes[0] = (byte)0xFF;
		serialBytes[1] = (byte)num;
		serialBytes[2] = (byte)(position*2.5f);
		try {
			this.out.write(serialBytes);
			this.out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void sendSetSpeed(int num, int speed) {
		/**
		 * Not tested
		 */
		byte [] serialBytes = new byte[6];
		serialBytes[0] = (byte)0xAA;
		serialBytes[1] = (byte)0;
		serialBytes[2] = (byte)0x07;
		serialBytes[3] = (byte)num;
		serialBytes[4] = (byte)(speed & 0xFF);
		serialBytes[5] = (byte)(speed >> 8 & 0xFF);
		try {
			this.out.write(serialBytes);
			this.out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
